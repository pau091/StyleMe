package co.styleme.dominio.modelo.lia;

import java.util.List;

/**
 * Los datos que la IA escribe después del separador, TAL COMO LLEGAN (sin revisar).
 * Pueden traer valores inválidos o productos inventados: por eso pasan por ValidadorRecomendaciones.
 */
public record DatosModelo(
        String sentimiento,
        String intencion,
        String emocion,
        Double intensidad,
        List<String> productos,
        List<AccionLia> acciones,
        List<String> sugerencias) {

    public static DatosModelo vacios() {
        return new DatosModelo(null, null, null, null, List.of(), List.of(), List.of());
    }
}
