package co.styleme.dominio.modelo.comunidad;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import java.util.Objects;

/**
 * A qué se refiere una reseña o un favorito: a UNA tienda (por su slug) o a UN producto (por su código).
 * Es la misma regla "una de dos" de la base de datos, pero en el dominio.
 */
public record Objetivo(Tipo tipo, String referencia) {

    public enum Tipo { TIENDA, PRODUCTO }

    public Objetivo {
        Objects.requireNonNull(tipo, "El objetivo necesita un tipo");
        if (referencia == null || referencia.isBlank()) {
            throw new DatoInvalidoException("Indica la tienda o el producto");
        }
        referencia = referencia.trim();
    }

    public static Objetivo tienda(String slug) {
        return new Objetivo(Tipo.TIENDA, slug);
    }

    public static Objetivo producto(String codigo) {
        return new Objetivo(Tipo.PRODUCTO, codigo);
    }

    /**
     * Arma el objetivo desde los dos campos que llegan de la página: debe venir exactamente uno.
     */
    public static Objetivo deUno(String slugTienda, String codigoProducto) {
        boolean hayTienda = slugTienda != null && !slugTienda.isBlank();
        boolean hayProducto = codigoProducto != null && !codigoProducto.isBlank();
        if (hayTienda == hayProducto) {
            throw new DatoInvalidoException("Indica una tienda o un producto (solo uno de los dos)");
        }
        return hayTienda ? tienda(slugTienda) : producto(codigoProducto);
    }

    public boolean esTienda() {
        return tipo == Tipo.TIENDA;
    }
}
