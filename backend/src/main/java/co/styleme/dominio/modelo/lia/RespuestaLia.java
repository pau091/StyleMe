package co.styleme.dominio.modelo.lia;

import java.util.List;

/**
 * La respuesta de Lía ya REVISADA: todo lo que trae es válido y existe en el catálogo.
 *
 * @param emocion    expresión del personaje: felicidad, preocupacion, tristeza, duda, asombro o neutra
 * @param intencion  catalogo, general, social o aclaracion (sirve para el tema de la conversación)
 * @param productos  códigos de productos REALES recomendados (máximo 3)
 * @param modo       IA si respondió el modelo de lenguaje; PLAN_B si respondió con reglas
 */
public record RespuestaLia(
        String texto,
        String emocion,
        double intensidad,
        String sentimiento,
        String intencion,
        List<String> productos,
        List<AccionLia> acciones,
        List<String> sugerencias,
        Modo modo) {

    public enum Modo { IA, PLAN_B }

    public RespuestaLia {
        productos = productos == null ? List.of() : List.copyOf(productos);
        acciones = acciones == null ? List.of() : List.copyOf(acciones);
        sugerencias = sugerencias == null ? List.of() : List.copyOf(sugerencias);
    }

    public RespuestaLia conTexto(String nuevoTexto) {
        return new RespuestaLia(nuevoTexto, emocion, intensidad, sentimiento, intencion, productos, acciones, sugerencias, modo);
    }
}
