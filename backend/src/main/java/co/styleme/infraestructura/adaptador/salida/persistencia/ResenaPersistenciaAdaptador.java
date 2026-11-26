package co.styleme.infraestructura.adaptador.salida.persistencia;

import co.styleme.aplicacion.puerto.salida.persistencia.ResenaRepositorioPuerto;
import co.styleme.dominio.modelo.comunidad.Objetivo;
import co.styleme.dominio.modelo.comunidad.Resena;
import co.styleme.dominio.modelo.comunidad.ResumenCalificacion;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador de salida: implementa ResenaRepositorioPuerto con SQL directo (JdbcTemplate). */
@Component
@Transactional
public class ResenaPersistenciaAdaptador implements ResenaRepositorioPuerto {

    private static final String SELECCION = """
            SELECT r.id, u.nombre AS autor, t.slug AS tienda, p.codigo AS producto,
                   r.corazones, r.comentario, r.estado, r.fecha
            FROM resena r
            JOIN usuario u        ON u.id = r.usuario_id
            LEFT JOIN tienda t    ON t.id = r.tienda_id
            LEFT JOIN producto p  ON p.id = r.producto_id
            """;

    private final JdbcTemplate jdbc;

    public ResenaPersistenciaAdaptador(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Resena guardar(Resena r) {
        long usuario = UsuarioDeSesion.obtener(jdbc, r.sesionId(), r.autor());
        Map<String, Object> creada = jdbc.queryForMap("""
                INSERT INTO resena (usuario_id, tienda_id, producto_id, corazones, comentario, estado)
                VALUES (?, (SELECT id FROM tienda WHERE slug = ?), (SELECT id FROM producto WHERE codigo = ?), ?, ?, ?)
                RETURNING id, fecha
                """, usuario, tienda(r.objetivo()), producto(r.objetivo()), r.corazones(), r.comentario(), r.estado().name());
        return new Resena(((Number) creada.get("id")).longValue(), r.sesionId(), r.autor(), r.objetivo(), r.corazones(),
                r.comentario(), r.estado(), ((java.sql.Timestamp) creada.get("fecha")).toLocalDateTime());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existe(String sesionId, Objetivo objetivo) {
        Boolean existe = jdbc.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM resena r JOIN usuario u ON u.id = r.usuario_id
                    WHERE u.correo = ?
                      AND r.tienda_id   IS NOT DISTINCT FROM (SELECT id FROM tienda WHERE slug = ?)
                      AND r.producto_id IS NOT DISTINCT FROM (SELECT id FROM producto WHERE codigo = ?))
                """, Boolean.class, UsuarioDeSesion.correo(sesionId), tienda(objetivo), producto(objetivo));
        return Boolean.TRUE.equals(existe);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Resena> publicadas(Objetivo objetivo, int limite) {
        return jdbc.query(SELECCION + " WHERE r.estado = 'PUBLICADA' AND " + condicion(objetivo) + " ORDER BY r.fecha DESC LIMIT ?",
                this::aResena, objetivo.referencia(), limite);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenCalificacion resumen(Objetivo objetivo) {
        return jdbc.queryForObject("""
                SELECT coalesce(round(avg(r.corazones), 2), 0) AS promedio, count(*) AS cantidad
                FROM resena r
                LEFT JOIN tienda t   ON t.id = r.tienda_id
                LEFT JOIN producto p ON p.id = r.producto_id
                WHERE r.estado = 'PUBLICADA'
                """ + " AND " + condicion(objetivo),
                (f, n) -> new ResumenCalificacion(f.getDouble("promedio"), f.getInt("cantidad")),
                objetivo.referencia());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Resena> recientes(int limite) {
        return jdbc.query(SELECCION + " WHERE r.estado = 'PUBLICADA' AND r.comentario IS NOT NULL ORDER BY r.fecha DESC LIMIT ?",
                this::aResena, limite);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Resena> pendientes(int limite) {
        return jdbc.query(SELECCION + " WHERE r.estado = 'PENDIENTE' ORDER BY r.fecha LIMIT ?", this::aResena, limite);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Resena> buscar(long id) {
        return jdbc.query(SELECCION + " WHERE r.id = ?", this::aResena, id).stream().findFirst();
    }

    @Override
    public void cambiarEstado(long id, Resena.Estado estado) {
        jdbc.update("UPDATE resena SET estado = ? WHERE id = ?", estado.name(), id);
    }

    private Resena aResena(ResultSet f, int n) throws SQLException {
        String slug = f.getString("tienda");
        Objetivo objetivo = slug != null ? Objetivo.tienda(slug) : Objetivo.producto(f.getString("producto"));
        // El id de sesión NO se devuelve (no se expone quién escribió la reseña)
        return new Resena(f.getLong("id"), null, f.getString("autor"), objetivo, f.getInt("corazones"),
                f.getString("comentario"), Resena.Estado.valueOf(f.getString("estado")), f.getTimestamp("fecha").toLocalDateTime());
    }

    private static String condicion(Objetivo objetivo) {
        return objetivo.esTienda() ? "t.slug = ?" : "p.codigo = ?";
    }

    private static String tienda(Objetivo o) {
        return o.esTienda() ? o.referencia() : null;
    }

    private static String producto(Objetivo o) {
        return o.esTienda() ? null : o.referencia();
    }
}
