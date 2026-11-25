package co.styleme.aplicacion.puerto.salida.persistencia;

import co.styleme.dominio.modelo.analitica.Evento;
import java.util.List;

/** Puerto de salida: el registro de eventos (tabla evento). */
public interface EventoRepositorioPuerto {

    /** Guarda los eventos y devuelve cuántos se guardaron. */
    int guardar(List<Evento> eventos);
}
