package co.styleme.aplicacion.puerto.entrada.comunidad;

import co.styleme.dominio.modelo.comunidad.Objetivo;
import co.styleme.dominio.modelo.comunidad.Resena;
import co.styleme.dominio.modelo.comunidad.ResumenCalificacion;
import java.util.List;

/** Puerto de entrada: escribir reseñas y ver las publicadas. */
public interface GestionarResenasCasoUso {

    /** Guarda la reseña (queda PENDIENTE de moderación). */
    Resena escribir(Resena nueva);

    List<Resena> publicadas(Objetivo objetivo);

    ResumenCalificacion resumen(Objetivo objetivo);

    /** Las últimas reseñas publicadas con comentario (para la portada). Máximo 12. */
    List<Resena> recientes(int limite);
}
