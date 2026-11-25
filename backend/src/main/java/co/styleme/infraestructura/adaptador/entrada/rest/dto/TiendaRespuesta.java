package co.styleme.infraestructura.adaptador.entrada.rest.dto;

import java.math.BigDecimal;

/**
 * Cómo sale una tienda en el JSON de la API (regla R6: el DTO no es el modelo del dominio).
 * No expone datos internos (id numérico, comerciante): la iu identifica la tienda por su slug.
 */
public record TiendaRespuesta(
        String slug,
        String nombre,
        String descripcion,
        CategoriaRespuesta categoria,
        String zona,
        String direccion,
        Coordenadas coordenadas,
        String horario,
        Contacto contacto,
        String foto,
        boolean verificada,
        boolean apareceEnMapa) {

    public record Coordenadas(BigDecimal latitud, BigDecimal longitud) {
    }

    public record Contacto(String whatsapp, String enlaceWhatsapp, String telefono, String instagram, String web) {
    }
}
