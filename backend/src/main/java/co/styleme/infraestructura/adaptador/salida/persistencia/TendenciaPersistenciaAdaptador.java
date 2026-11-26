package co.styleme.infraestructura.adaptador.salida.persistencia;

import co.styleme.aplicacion.puerto.salida.persistencia.TendenciaRepositorioPuerto;
import co.styleme.dominio.modelo.analitica.Tendencias.Conteo;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de salida: cuenta en la tabla evento lo más buscado y visto en los últimos días.
 * Siempre cuenta PERSONAS (sesiones distintas), no clics: así una sola persona no infla el ranking.
 */
@Component
@Transactional(readOnly = true)
public class TendenciaPersistenciaAdaptador implements TendenciaRepositorioPuerto {

    private final JdbcTemplate jdbc;

    public TendenciaPersistenciaAdaptador(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Conteo> busquedas(int dias, int minimoSesiones, int limite) {
        // Se agrupan sin importar mayúsculas ni espacios de más ("Jean " cuenta como "jean")
        return jdbc.query("""
                SELECT lower(btrim(texto_busqueda)) AS referencia, count(DISTINCT sesion_id) AS personas
                FROM evento
                WHERE tipo = 'BUSQUEDA'
                  AND cantidad_resultados > 0
                  AND length(btrim(texto_busqueda)) BETWEEN 2 AND 40
                  AND fecha_hora >= now() - make_interval(days => ?)
                GROUP BY 1
                HAVING count(DISTINCT sesion_id) >= ?
                ORDER BY personas DESC, referencia
                LIMIT ?
                """, (f, n) -> new Conteo(f.getString("referencia"), f.getLong("personas")), dias, minimoSesiones, limite);
    }

    @Override
    public List<Conteo> tiendasMasVistas(int dias, int limite) {
        return jdbc.query("""
                SELECT t.slug AS referencia, count(DISTINCT e.sesion_id) AS personas
                FROM evento e
                JOIN tienda t ON t.id = e.tienda_id AND t.activa
                WHERE e.tipo = 'VER_TIENDA'
                  AND e.fecha_hora >= now() - make_interval(days => ?)
                GROUP BY t.slug
                ORDER BY personas DESC, referencia
                LIMIT ?
                """, (f, n) -> new Conteo(f.getString("referencia"), f.getLong("personas")), dias, limite);
    }

    @Override
    public List<Conteo> productosMasVistos(int dias, int limite) {
        return jdbc.query("""
                SELECT p.codigo AS referencia, count(DISTINCT e.sesion_id) AS personas
                FROM evento e
                JOIN producto p ON p.id = e.producto_id AND NOT p.es_ejemplo
                WHERE e.tipo = 'VER_PRODUCTO'
                  AND e.fecha_hora >= now() - make_interval(days => ?)
                GROUP BY p.codigo
                ORDER BY personas DESC, referencia
                LIMIT ?
                """, (f, n) -> new Conteo(f.getString("referencia"), f.getLong("personas")), dias, limite);
    }
}
