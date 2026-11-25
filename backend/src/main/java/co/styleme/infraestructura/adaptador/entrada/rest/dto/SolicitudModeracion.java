package co.styleme.infraestructura.adaptador.entrada.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Decisión de moderación: PUBLICADA o RECHAZADA. */
public record SolicitudModeracion(@Schema(example = "PUBLICADA", allowableValues = {"PUBLICADA", "RECHAZADA"}) String estado) {
}
