package co.styleme.aplicacion.puerto.entrada.analitica;

import co.styleme.dominio.modelo.analitica.Evento;
import java.util.List;

/** Puerto de entrada: registrar lo que pasa en la página (para los indicadores). */
public interface RegistrarEventosCasoUso {

    /** Máximo de eventos por envío (la página los manda en lotes). */
    int MAXIMO_POR_LOTE = 50;

    int registrar(List<Evento> eventos);
}
