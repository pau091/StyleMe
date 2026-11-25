package co.styleme.infraestructura.adaptador.entrada.rest.dto;

import java.time.LocalDateTime;

/** Formato único de los errores de la API: la iu siempre recibe la misma forma. */
public record ErrorRespuesta(int estado, String error, String mensaje, String ruta, LocalDateTime fecha) {

    public static ErrorRespuesta de(int estado, String error, String mensaje, String ruta) {
        return new ErrorRespuesta(estado, error, mensaje, ruta, LocalDateTime.now());
    }
}
