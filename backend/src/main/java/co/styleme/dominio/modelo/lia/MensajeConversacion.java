package co.styleme.dominio.modelo.lia;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import java.util.Objects;

/** Un turno de la conversación: lo que dijo la persona o lo que dijo Lía. */
public record MensajeConversacion(Rol rol, String texto) {

    /** Largo máximo de un turno anterior que se le reenvía a la IA (ahorra tokens). */
    public static final int LARGO_MAXIMO_HISTORIA = 1200;

    public enum Rol { PERSONA, LIA }

    public MensajeConversacion {
        Objects.requireNonNull(rol, "El mensaje necesita un rol");
        if (texto == null || texto.isBlank()) {
            throw new DatoInvalidoException("Un mensaje de la conversación no puede estar vacío");
        }
        texto = texto.trim();
        if (texto.length() > LARGO_MAXIMO_HISTORIA) {
            texto = texto.substring(0, LARGO_MAXIMO_HISTORIA);
        }
    }

    public boolean esDeLaPersona() {
        return rol == Rol.PERSONA;
    }
}
