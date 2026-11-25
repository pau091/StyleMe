package co.styleme.infraestructura.adaptador.entrada.rest.dto;

import co.styleme.dominio.modelo.comunidad.Favorito;
import java.time.LocalDateTime;

/** Un favorito guardado: tipo (TIENDA/PRODUCTO), su slug o código y su nombre. */
public record FavoritoRespuesta(String tipo, String referencia, String nombre, LocalDateTime fecha) {

    public static FavoritoRespuesta de(Favorito f) {
        return new FavoritoRespuesta(f.objetivo().tipo().name(), f.objetivo().referencia(), f.nombre(), f.fecha());
    }
}
