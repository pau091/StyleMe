package co.styleme.aplicacion.puerto.salida.persistencia;

import co.styleme.dominio.modelo.lia.ConversacionLia;
import java.util.List;
import java.util.Optional;

/** Puerto de salida: el resumen de las conversaciones con Lía (tablas conversacion_lia y lia_recomendacion). */
public interface ConversacionRepositorioPuerto {

    Optional<ConversacionLia> buscarPorSesion(String sesionId);

    /** Guarda (inserta o actualiza) y devuelve la conversación con su id. */
    ConversacionLia guardar(ConversacionLia conversacion);

    /** Suma 1 a las veces que Lía recomendó cada producto en esa conversación. */
    void sumarRecomendaciones(long conversacionId, List<String> codigosProducto);
}
