package co.styleme.infraestructura.adaptador.entrada.rest.dto;

import co.styleme.dominio.modelo.comunidad.Resena;
import java.time.LocalDateTime;

/** Una reseña tal como la ve la página. No incluye la sesión de quien la escribió. */
public record ResenaRespuesta(Long id, String autor, String tienda, String producto, int corazones,
        String comentario, String estado, LocalDateTime fecha) {

    public static ResenaRespuesta de(Resena r) {
        boolean esTienda = r.objetivo().esTienda();
        return new ResenaRespuesta(r.id(), r.autor(), esTienda ? r.objetivo().referencia() : null,
                esTienda ? null : r.objetivo().referencia(), r.corazones(), r.comentario(), r.estado().name(), r.fecha());
    }
}
