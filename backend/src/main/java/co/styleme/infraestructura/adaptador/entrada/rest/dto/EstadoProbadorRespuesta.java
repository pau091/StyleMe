package co.styleme.infraestructura.adaptador.entrada.rest.dto;

import java.util.List;

/** Si el probador con IA está activo, con qué proveedor y qué poses ofrece. */
public record EstadoProbadorRespuesta(boolean disponible, String proveedor, List<PoseRespuesta> poses) {

    public record PoseRespuesta(String codigo, String nombre) {
    }
}
