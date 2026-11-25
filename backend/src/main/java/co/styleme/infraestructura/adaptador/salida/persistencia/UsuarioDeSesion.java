package co.styleme.infraestructura.adaptador.salida.persistencia;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Usuario ANÓNIMO de una sesión (D-066). La tabla resena/favorito exige un usuario; como StyleMe
 * no pide registro, cada sesión del navegador tiene un usuario sin contraseña ni datos personales:
 *   correo  = <sesionId>@sesion.styleme   (identificador técnico, no es un correo real)
 *   nombre  = el nombre que la persona escriba en su reseña, o "Anónimo"
 */
final class UsuarioDeSesion {

    private UsuarioDeSesion() {
    }

    static String correo(String sesionId) {
        return sesionId + "@sesion.styleme";
    }

    /** Devuelve el id del usuario de la sesión, creándolo si no existe. */
    static long obtener(JdbcTemplate jdbc, String sesionId, String nombre) {
        Long id = jdbc.queryForObject("""
                INSERT INTO usuario (nombre, correo, contrasena_hash)
                VALUES (?, ?, 'SIN_ACCESO')
                ON CONFLICT (correo) DO UPDATE SET nombre = CASE WHEN EXCLUDED.nombre = 'Anónimo'
                                                                 THEN usuario.nombre ELSE EXCLUDED.nombre END
                RETURNING id
                """, Long.class, nombre == null ? "Anónimo" : nombre, correo(sesionId));
        return id == null ? 0 : id;
    }
}
