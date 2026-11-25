package co.styleme.dominio.modelo.lia;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import java.util.List;

/**
 * Lo que la persona le pregunta a Lía, con la conversación anterior.
 *
 * @param sesionId identificador ANÓNIMO de la conversación (lo genera la página); no identifica a la persona
 */
public record PreguntaLia(String mensaje, List<MensajeConversacion> historia, String sesionId) {

    public static final int LARGO_MAXIMO_MENSAJE = 500;
    /** Historial corto: solo los últimos turnos se reenvían a la IA (ahorra tokens). */
    public static final int TURNOS_RECORDADOS = 12;

    public PreguntaLia {
        if (mensaje == null || mensaje.isBlank()) {
            throw new DatoInvalidoException("Escríbele algo a Lía");
        }
        mensaje = mensaje.trim();
        if (mensaje.length() > LARGO_MAXIMO_MENSAJE) {
            throw new DatoInvalidoException("El mensaje no puede superar " + LARGO_MAXIMO_MENSAJE + " caracteres");
        }
        historia = historia == null ? List.of()
                : List.copyOf(historia.subList(Math.max(0, historia.size() - TURNOS_RECORDADOS), historia.size()));
        sesionId = (sesionId == null || sesionId.isBlank()) ? null : sesionId.trim();
        if (sesionId != null && sesionId.length() > 36) {
            throw new DatoInvalidoException("Identificador de sesión inválido");
        }
    }

    /**
     * Texto para la búsqueda semántica: el mensaje actual más los 2 últimos de la persona.
     * Así "¿y un protector solar?" no pierde el hilo de "tengo la piel grasa".
     */
    public String consultaParaBuscar() {
        List<String> anteriores = historia.stream()
                .filter(MensajeConversacion::esDeLaPersona)
                .map(MensajeConversacion::texto)
                .toList();
        List<String> ultimos = anteriores.subList(Math.max(0, anteriores.size() - 2), anteriores.size());
        String consulta = String.join(". ", ultimos);
        consulta = consulta.isEmpty() ? mensaje : consulta + ". " + mensaje;
        return consulta.length() <= 300 ? consulta : consulta.substring(consulta.length() - 300);
    }
}
