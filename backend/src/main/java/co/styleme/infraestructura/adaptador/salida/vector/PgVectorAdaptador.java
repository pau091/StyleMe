package co.styleme.infraestructura.adaptador.salida.vector;

import co.styleme.aplicacion.puerto.salida.ia.AlmacenVectorialPuerto;
import co.styleme.dominio.modelo.ia.Embedding;
import co.styleme.dominio.modelo.ia.FiltroBusqueda;
import co.styleme.dominio.modelo.ia.FragmentoConocimiento;
import co.styleme.dominio.modelo.ia.ResultadoBusqueda;
import co.styleme.dominio.modelo.ia.TipoFuente;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de salida: implementa AlmacenVectorialPuerto con PostgreSQL + pgvector.
 *
 * Usa SQL directo (JdbcTemplate) porque JPA no conoce el tipo VECTOR ni el operador {@code <=>}.
 *
 * Búsqueda:
 *   ORDER BY embedding <=> :consulta   → distancia coseno; usa el índice HNSW (idx_fragmento_embedding_hnsw)
 *   similitud = 1 - distancia          → 1 = mismo significado, 0 = nada que ver
 *   hnsw.iterative_scan                → si los filtros descartan resultados, el índice sigue buscando
 *                                        hasta completar el top-k (pgvector 0.8+)
 */
@Component
@Transactional
public class PgVectorAdaptador implements AlmacenVectorialPuerto {

    /** Cuántos candidatos revisa el índice HNSW por búsqueda (más = más preciso, algo más lento). */
    private static final int EF_SEARCH = 100;

    private final JdbcTemplate jdbc;

    public PgVectorAdaptador(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> hashesPorClave(String modelo) {
        Map<String, String> hashes = new HashMap<>();
        jdbc.query("""
                SELECT tipo_fuente, tienda_id, producto_id, titulo, hash_texto
                FROM fragmento_conocimiento
                WHERE modelo_embedding = ?
                """,
                fila -> {
                    hashes.put(clave(fila.getString("tipo_fuente"), fila.getObject("tienda_id", Long.class),
                            fila.getObject("producto_id", Long.class), fila.getString("titulo")), fila.getString("hash_texto"));
                },
                modelo);
        return hashes;
    }

    @Override
    public void guardar(FragmentoConocimiento f, Embedding embedding, String modelo) {
        String vector = aTextoVector(embedding);
        // Si ya hay un fragmento con la misma clave, se actualiza; si no, se inserta.
        // IS NOT DISTINCT FROM compara bien aunque los valores sean NULL (NULL = NULL → verdadero).
        int actualizados = jdbc.update("""
                UPDATE fragmento_conocimiento
                SET texto = ?, hash_texto = ?, embedding = CAST(? AS vector), modelo_embedding = ?, actualizado = now()
                WHERE tipo_fuente = ? AND tienda_id IS NOT DISTINCT FROM ? AND producto_id IS NOT DISTINCT FROM ? AND titulo = ?
                """,
                f.texto(), f.hashTexto(), vector, modelo, f.tipo().codigo(), f.tiendaId(), f.productoId(), f.titulo());
        if (actualizados == 0) {
            jdbc.update("""
                    INSERT INTO fragmento_conocimiento
                        (tipo_fuente, tienda_id, producto_id, titulo, texto, hash_texto, embedding, modelo_embedding)
                    VALUES (?, ?, ?, ?, ?, ?, CAST(? AS vector), ?)
                    """,
                    f.tipo().codigo(), f.tiendaId(), f.productoId(), f.titulo(), f.texto(), f.hashTexto(), vector, modelo);
        }
    }

    @Override
    public int eliminarExcepto(Set<String> clavesVigentes) {
        List<Long> sobrantes = new ArrayList<>();
        jdbc.query("SELECT id, tipo_fuente, tienda_id, producto_id, titulo FROM fragmento_conocimiento", fila -> {
            String clave = clave(fila.getString("tipo_fuente"), fila.getObject("tienda_id", Long.class),
                    fila.getObject("producto_id", Long.class), fila.getString("titulo"));
            if (!clavesVigentes.contains(clave)) {
                sobrantes.add(fila.getLong("id"));
            }
        });
        sobrantes.forEach(id -> jdbc.update("DELETE FROM fragmento_conocimiento WHERE id = ?", id));
        return sobrantes.size();
    }

    @Override
    public List<ResultadoBusqueda> buscarParecidos(Embedding consulta, FiltroBusqueda filtro) {
        // SET LOCAL: solo dura esta transacción (no afecta a otras consultas)
        jdbc.execute("SET LOCAL hnsw.ef_search = " + EF_SEARCH);
        jdbc.execute("SET LOCAL hnsw.iterative_scan = relaxed_order");

        String vector = aTextoVector(consulta);
        List<Object> parametros = new ArrayList<>();
        var sql = new StringBuilder("""
                SELECT f.tipo_fuente, f.titulo, f.texto,
                       p.codigo AS codigo_producto,
                       COALESCE(t.slug, tp.slug) AS slug_tienda,
                       1 - (f.embedding <=> CAST(? AS vector)) AS similitud
                FROM fragmento_conocimiento f
                LEFT JOIN producto  p  ON p.id = f.producto_id
                LEFT JOIN tienda    tp ON tp.id = p.tienda_id
                LEFT JOIN tienda    t  ON t.id = f.tienda_id
                LEFT JOIN categoria c  ON c.id = COALESCE(p.categoria_id, t.categoria_principal_id)
                WHERE (p.id IS NULL OR p.activo) AND (t.id IS NULL OR t.activa)
                """);
        parametros.add(vector);

        // Filtros "duros" de la búsqueda híbrida
        if (filtro.tipo() != null) {
            sql.append(" AND f.tipo_fuente = ?");
            parametros.add(filtro.tipo().codigo());
        }
        if (filtro.categoria() != null) {
            sql.append(" AND c.slug = ?");
            parametros.add(filtro.categoria());
        }
        if (filtro.precioMaximo() != null) {
            // El presupuesto solo aplica a productos; un producto sin precio no lo cumple
            sql.append(" AND (f.tipo_fuente <> 'producto' OR p.precio <= ?)");
            parametros.add(filtro.precioMaximo());
        }
        sql.append(" ORDER BY f.embedding <=> CAST(? AS vector) LIMIT ?");
        parametros.add(vector);
        parametros.add(filtro.limite());

        List<ResultadoBusqueda> resultados = jdbc.query(sql.toString(), (fila, n) -> new ResultadoBusqueda(
                TipoFuente.desdeCodigo(fila.getString("tipo_fuente")),
                fila.getString("titulo"),
                fila.getString("texto"),
                fila.getString("codigo_producto"),
                fila.getString("slug_tienda"),
                fila.getDouble("similitud")),
                parametros.toArray());

        // Umbral de relevancia y orden final (relaxed_order puede devolver un orden casi exacto)
        return resultados.stream()
                .filter(r -> r.similitud() >= filtro.similitudMinima())
                .sorted(Comparator.comparingDouble(ResultadoBusqueda::similitud).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long contar() {
        Long total = jdbc.queryForObject("SELECT count(*) FROM fragmento_conocimiento", Long.class);
        return total == null ? 0 : total;
    }

    /** pgvector recibe los vectores como texto: "[0.12,-0.03,...]". */
    static String aTextoVector(Embedding embedding) {
        float[] valores = embedding.valores();
        var texto = new StringBuilder(valores.length * 12).append('[');
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) {
                texto.append(',');
            }
            texto.append(valores[i]);
        }
        return texto.append(']').toString();
    }

    private static String clave(String tipo, Long tiendaId, Long productoId, String titulo) {
        return FragmentoConocimiento.clave(TipoFuente.desdeCodigo(tipo), tiendaId, productoId, titulo);
    }
}
