package co.styleme.infraestructura.adaptador.salida.persistencia;

import co.styleme.aplicacion.puerto.salida.persistencia.FavoritoRepositorioPuerto;
import co.styleme.dominio.modelo.comunidad.Favorito;
import co.styleme.dominio.modelo.comunidad.Objetivo;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador de salida: implementa FavoritoRepositorioPuerto con SQL directo. */
@Component
@Transactional
public class FavoritoPersistenciaAdaptador implements FavoritoRepositorioPuerto {

    private final JdbcTemplate jdbc;

    public FavoritoPersistenciaAdaptador(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void agregar(String sesionId, Objetivo objetivo) {
        long usuario = UsuarioDeSesion.obtener(jdbc, sesionId, null);
        // ON CONFLICT DO NOTHING: si ya era favorito, la regla UNIQUE lo detecta y no se duplica
        jdbc.update("""
                INSERT INTO favorito (usuario_id, tienda_id, producto_id)
                VALUES (?, (SELECT id FROM tienda WHERE slug = ?), (SELECT id FROM producto WHERE codigo = ?))
                ON CONFLICT DO NOTHING
                """, usuario, objetivo.esTienda() ? objetivo.referencia() : null, objetivo.esTienda() ? null : objetivo.referencia());
    }

    @Override
    public void quitar(String sesionId, Objetivo objetivo) {
        String sql = objetivo.esTienda()
                ? "DELETE FROM favorito f USING usuario u, tienda t WHERE f.usuario_id = u.id AND f.tienda_id = t.id AND u.correo = ? AND t.slug = ?"
                : "DELETE FROM favorito f USING usuario u, producto p WHERE f.usuario_id = u.id AND f.producto_id = p.id AND u.correo = ? AND p.codigo = ?";
        jdbc.update(sql, UsuarioDeSesion.correo(sesionId), objetivo.referencia());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Favorito> listar(String sesionId) {
        return jdbc.query("""
                SELECT t.slug, p.codigo, coalesce(t.nombre, p.nombre) AS nombre, f.fecha
                FROM favorito f
                JOIN usuario u       ON u.id = f.usuario_id
                LEFT JOIN tienda t   ON t.id = f.tienda_id
                LEFT JOIN producto p ON p.id = f.producto_id
                WHERE u.correo = ?
                ORDER BY f.fecha DESC
                """,
                (fila, n) -> new Favorito(
                        fila.getString("slug") != null ? Objetivo.tienda(fila.getString("slug")) : Objetivo.producto(fila.getString("codigo")),
                        fila.getString("nombre"),
                        fila.getTimestamp("fecha").toLocalDateTime()),
                UsuarioDeSesion.correo(sesionId));
    }
}
