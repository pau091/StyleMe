package co.styleme.dominio.modelo.comunidad;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Reseña de 1 a 5 corazones (D-006) sobre una tienda o un producto.
 *
 * Reglas del negocio:
 *  - Corazones entre 1 y 5; comentario opcional de máximo 500 caracteres.
 *  - Toda reseña nueva queda PENDIENTE: se publica cuando la modera n8n con IA (o un administrador).
 *  - Solo se puede moderar una reseña que está PENDIENTE.
 *  - La autoría es anónima por sesión (D-066): se muestra el nombre que la persona quiera, o "Anónimo".
 */
public record Resena(
        Long id,
        String sesionId,
        String autor,
        Objetivo objetivo,
        int corazones,
        String comentario,
        Estado estado,
        LocalDateTime fecha) {

    public enum Estado { PENDIENTE, PUBLICADA, RECHAZADA }

    public static final int LARGO_MAXIMO_COMENTARIO = 500;

    public Resena {
        Objects.requireNonNull(objetivo, "La reseña necesita una tienda o un producto");
        Objects.requireNonNull(estado, "La reseña necesita un estado");
        if (corazones < 1 || corazones > 5) {
            throw new DatoInvalidoException("La calificación debe ser de 1 a 5 corazones");
        }
        comentario = (comentario == null || comentario.isBlank()) ? null : comentario.trim();
        if (comentario != null && comentario.length() > LARGO_MAXIMO_COMENTARIO) {
            throw new DatoInvalidoException("El comentario no puede superar " + LARGO_MAXIMO_COMENTARIO + " caracteres");
        }
        autor = (autor == null || autor.isBlank()) ? "Anónimo" : autor.trim();
        if (autor.length() > 80) {
            autor = autor.substring(0, 80);
        }
    }

    /** Una reseña recién escrita: siempre empieza PENDIENTE. */
    public static Resena nueva(String sesionId, String autor, Objetivo objetivo, int corazones, String comentario) {
        if (sesionId == null || sesionId.isBlank() || sesionId.length() > 36) {
            throw new DatoInvalidoException("Falta el identificador de sesión");
        }
        return new Resena(null, sesionId.trim(), autor, objetivo, corazones, comentario, Estado.PENDIENTE, null);
    }

    /** Resultado de la moderación. Solo se modera lo pendiente. */
    public Resena moderada(Estado nuevoEstado) {
        if (estado != Estado.PENDIENTE) {
            throw new DatoInvalidoException("La reseña ya fue moderada (estado: " + estado + ")");
        }
        if (nuevoEstado == Estado.PENDIENTE) {
            throw new DatoInvalidoException("La moderación debe publicar o rechazar la reseña");
        }
        return new Resena(id, sesionId, autor, objetivo, corazones, comentario, nuevoEstado, fecha);
    }
}
