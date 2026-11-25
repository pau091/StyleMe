package co.styleme.infraestructura.adaptador.entrada.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Lo que manda la página al escribir una reseña. Debe venir la tienda O el producto (solo uno). */
public record SolicitudResena(
        @Schema(description = "Identificador anónimo de la sesión del navegador", example = "3f6c2a0e-8d1b-4c55-9a77-1b2c3d4e5f60") String sesionId,
        @Schema(description = "Nombre que se mostrará (opcional)", example = "Ana") String autor,
        @Schema(description = "Slug de la tienda", example = "bohor") String tienda,
        @Schema(description = "Código del producto", example = "p049") String producto,
        @Schema(description = "De 1 a 5", example = "5") Integer corazones,
        @Schema(description = "Opcional, máximo 500 caracteres", example = "Excelente atención") String comentario) {
}
