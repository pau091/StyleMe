package co.styleme.aplicacion.casodeuso.analitica;

import co.styleme.aplicacion.puerto.entrada.analitica.RegistrarEventosCasoUso;
import co.styleme.aplicacion.puerto.salida.persistencia.EventoRepositorioPuerto;
import co.styleme.dominio.excepcion.DatoInvalidoException;
import co.styleme.dominio.modelo.analitica.Evento;
import java.util.List;
import java.util.Objects;

/**
 * Caso de uso: registrar eventos de uso de la página (en lotes, para no hacer una petición por clic).
 * Si un evento menciona una tienda o un producto que no existe, se guarda sin esa referencia
 * (el evento cuenta para las estadísticas generales, pero no se le atribuye a nadie).
 */
public class RegistrarEventosServicio implements RegistrarEventosCasoUso {

    private final EventoRepositorioPuerto eventos;

    public RegistrarEventosServicio(EventoRepositorioPuerto eventos) {
        this.eventos = Objects.requireNonNull(eventos);
    }

    @Override
    public int registrar(List<Evento> lote) {
        if (lote == null || lote.isEmpty()) {
            return 0;
        }
        if (lote.size() > MAXIMO_POR_LOTE) {
            throw new DatoInvalidoException("Máximo " + MAXIMO_POR_LOTE + " eventos por envío");
        }
        return eventos.guardar(lote);
    }
}
