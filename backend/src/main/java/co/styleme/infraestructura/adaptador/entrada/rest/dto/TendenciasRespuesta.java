package co.styleme.infraestructura.adaptador.entrada.rest.dto;

import co.styleme.dominio.modelo.analitica.Tendencias;
import java.util.List;

/** Tendencias para la portada: solo totales anónimos (cuántas personas), nunca quién. */
public record TendenciasRespuesta(int dias, List<Conteo> busquedas, List<Conteo> tiendas, List<Conteo> productos) {

    public record Conteo(String referencia, long personas) {
    }

    public static TendenciasRespuesta de(Tendencias t) {
        return new TendenciasRespuesta(t.dias(), lista(t.busquedas()), lista(t.tiendas()), lista(t.productos()));
    }

    private static List<Conteo> lista(List<Tendencias.Conteo> conteos) {
        return conteos.stream().map(c -> new Conteo(c.referencia(), c.personas())).toList();
    }
}
