package co.styleme.dominio.modelo.catalogo;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import java.math.BigDecimal;

/**
 * Ubicación de una tienda en el mapa. Es un "objeto de valor": se define por sus datos,
 * no tiene id, y siempre es válido (no se puede crear con una latitud de 200).
 */
public record Coordenadas(BigDecimal latitud, BigDecimal longitud) {

    private static final BigDecimal NOVENTA = BigDecimal.valueOf(90);
    private static final BigDecimal CIENTO_OCHENTA = BigDecimal.valueOf(180);

    public Coordenadas {
        if (latitud == null || longitud == null) {
            throw new DatoInvalidoException("Las coordenadas necesitan latitud y longitud");
        }
        if (latitud.abs().compareTo(NOVENTA) > 0) {
            throw new DatoInvalidoException("La latitud debe estar entre -90 y 90");
        }
        if (longitud.abs().compareTo(CIENTO_OCHENTA) > 0) {
            throw new DatoInvalidoException("La longitud debe estar entre -180 y 180");
        }
    }

    /**
     * Crea las coordenadas solo si vienen las dos; si falta alguna, devuelve null
     * (la tienda existe, pero no sale en el mapa).
     */
    public static Coordenadas siEstanCompletas(BigDecimal latitud, BigDecimal longitud) {
        return (latitud == null || longitud == null) ? null : new Coordenadas(latitud, longitud);
    }
}
