package co.styleme.infraestructura.adaptador.entrada.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

/** Un evento de la página (ver tienda, clic en WhatsApp, búsqueda...). */
public record SolicitudEvento(
        @Schema(example = "CLIC_WHATSAPP") String tipo,
        @Schema(example = "3f6c2a0e-8d1b-4c55-9a77-1b2c3d4e5f60") String sesionId,
        @Schema(example = "bohor") String tienda,
        String producto,
        String categoria,
        String textoBusqueda,
        Integer cantidadResultados,
        @Schema(example = "celular", allowableValues = {"celular", "computador"}) String dispositivo,
        Map<String, String> detalle) {
}
