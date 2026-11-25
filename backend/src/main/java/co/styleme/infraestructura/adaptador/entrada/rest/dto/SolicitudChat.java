package co.styleme.infraestructura.adaptador.entrada.rest.dto;

import java.util.List;

/**
 * Lo que la página le manda a Lía (el mismo formato que usaba la función de Netlify).
 *
 * @param mensaje  lo que la persona acaba de escribir
 * @param historia turnos anteriores: rol "persona" o "lia"
 * @param sesionId identificador anónimo de la conversación (opcional)
 */
public record SolicitudChat(String mensaje, List<Turno> historia, String sesionId) {

    public record Turno(String rol, String texto) {
    }
}
