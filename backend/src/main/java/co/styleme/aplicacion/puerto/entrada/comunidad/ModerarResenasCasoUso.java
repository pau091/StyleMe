package co.styleme.aplicacion.puerto.entrada.comunidad;

import co.styleme.dominio.modelo.comunidad.Resena;
import java.util.List;

/** Puerto de entrada: moderar reseñas (lo usa n8n con IA, o un administrador). */
public interface ModerarResenasCasoUso {

    List<Resena> pendientes(int limite);

    Resena moderar(long id, Resena.Estado estado);
}
