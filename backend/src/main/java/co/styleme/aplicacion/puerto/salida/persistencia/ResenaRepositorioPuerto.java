package co.styleme.aplicacion.puerto.salida.persistencia;

import co.styleme.dominio.modelo.comunidad.Objetivo;
import co.styleme.dominio.modelo.comunidad.Resena;
import co.styleme.dominio.modelo.comunidad.ResumenCalificacion;
import java.util.List;
import java.util.Optional;

/** Puerto de salida: las reseñas (tabla resena). */
public interface ResenaRepositorioPuerto {

    /** Guarda una reseña nueva y la devuelve con su id y fecha. */
    Resena guardar(Resena resena);

    /** ¿Esta sesión ya reseñó esa tienda o ese producto? */
    boolean existe(String sesionId, Objetivo objetivo);

    List<Resena> publicadas(Objetivo objetivo, int limite);

    ResumenCalificacion resumen(Objetivo objetivo);

    /** Las pendientes de moderar, de la más antigua a la más nueva. */
    List<Resena> pendientes(int limite);

    Optional<Resena> buscar(long id);

    void cambiarEstado(long id, Resena.Estado estado);
}
