package co.styleme.dominio.modelo.lia;

import java.util.Objects;

/**
 * Lo que devolvió el modelo de lenguaje, separado en sus dos partes:
 *
 * @param textoVisible  lo que Lía le dice a la persona (antes del separador @@@)
 * @param datos         los datos que escribió después del separador, SIN revisar todavía
 * @param tokensEntrada tokens enviados (se suman en conversacion_lia para el informe de costos)
 * @param tokensSalida  tokens generados
 */
public record RespuestaModelo(String textoVisible, DatosModelo datos, int tokensEntrada, int tokensSalida) {

    public RespuestaModelo {
        textoVisible = textoVisible == null ? "" : textoVisible.trim();
        datos = Objects.requireNonNullElse(datos, DatosModelo.vacios());
    }
}
