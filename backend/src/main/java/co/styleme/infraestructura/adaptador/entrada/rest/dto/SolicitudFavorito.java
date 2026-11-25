package co.styleme.infraestructura.adaptador.entrada.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Guardar un favorito: la tienda O el producto (solo uno). */
public record SolicitudFavorito(
        @Schema(example = "3f6c2a0e-8d1b-4c55-9a77-1b2c3d4e5f60") String sesionId,
        @Schema(example = "bohor") String tienda,
        @Schema(example = "p035") String producto) {
}
