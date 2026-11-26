package co.styleme.dominio.modelo.analitica;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import java.util.List;
import java.util.Objects;

/**
 * Lo más buscado y visto en los últimos días, para la portada ("Lo más buscado esta semana").
 * Son solo totales: no dicen quién buscó ni quién vio.
 *
 * Reglas del negocio:
 *  - El periodo va de 1 a 90 días.
 *  - Una búsqueda solo se muestra si la hicieron al menos {@link #MINIMO_SESIONES} personas
 *    (sesiones) distintas y tuvo resultados: así nunca se publica algo que escribió una sola persona.
 */
public record Tendencias(int dias, List<Conteo> busquedas, List<Conteo> tiendas, List<Conteo> productos) {

    public static final int DIAS_MAXIMO = 90;
    public static final int MINIMO_SESIONES = 3;
    public static final int LIMITE = 5;

    /** Una búsqueda, tienda (slug) o producto (código) y cuántas personas distintas la buscaron o vieron. */
    public record Conteo(String referencia, long personas) {
        public Conteo {
            Objects.requireNonNull(referencia);
        }
    }

    public Tendencias {
        validarDias(dias);
        busquedas = busquedas == null ? List.of() : List.copyOf(busquedas);
        tiendas = tiendas == null ? List.of() : List.copyOf(tiendas);
        productos = productos == null ? List.of() : List.copyOf(productos);
    }

    public static void validarDias(int dias) {
        if (dias < 1 || dias > DIAS_MAXIMO) {
            throw new DatoInvalidoException("El periodo debe ser de 1 a " + DIAS_MAXIMO + " días");
        }
    }
}
