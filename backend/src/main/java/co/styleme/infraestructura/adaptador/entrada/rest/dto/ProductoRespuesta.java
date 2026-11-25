package co.styleme.infraestructura.adaptador.entrada.rest.dto;

import java.util.List;
import java.util.Map;

/**
 * Cómo sale un producto en el JSON de la API.
 * Las etiquetas se agrupan para que la iu las use directo:
 * {"tipo_piel": ["grasa", "mixta"], "objetivo": ["hidratar"]}
 */
public record ProductoRespuesta(
        String codigo,
        String nombre,
        String marca,
        Integer precio,
        String descripcion,
        String foto,
        String fuente,
        CategoriaRespuesta categoria,
        TiendaResumen tienda,
        String tipoPrenda,
        boolean disponibleEnProbador,
        boolean esEjemplo,
        List<String> ingredientes,
        Map<String, List<String>> etiquetas) {

    public record TiendaResumen(String slug, String nombre) {
    }
}
