package co.styleme.infraestructura.adaptador.entrada.rest;

import co.styleme.aplicacion.puerto.entrada.analitica.RegistrarEventosCasoUso;
import co.styleme.dominio.excepcion.DatoInvalidoException;
import co.styleme.dominio.modelo.analitica.Evento;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.EventosRegistradosRespuesta;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.SolicitudEvento;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST: la página manda en lotes lo que hace la persona (sin datos personales).
 * Con estos eventos se calculan los indicadores del informe de análisis de datos.
 */
@RestController
@RequestMapping("/api/eventos")
@Tag(name = "Analítica", description = "Registro de eventos de uso para los indicadores")
public class EventoControlador {

    private final RegistrarEventosCasoUso registrar;

    public EventoControlador(RegistrarEventosCasoUso registrar) {
        this.registrar = registrar;
    }

    /** POST /api/eventos  [ {...}, {...} ] */
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Registrar un lote de eventos",
            description = "Máximo " + RegistrarEventosCasoUso.MAXIMO_POR_LOTE + " eventos por envío.")
    @ApiResponse(responseCode = "202", description = "Eventos guardados")
    @ApiResponse(responseCode = "400", description = "Algún evento es inválido o el lote es muy grande (no se guarda ninguno)")
    public EventosRegistradosRespuesta registrar(@RequestBody List<SolicitudEvento> lote) {
        if (lote.size() > RegistrarEventosCasoUso.MAXIMO_POR_LOTE) {
            throw new DatoInvalidoException("Máximo " + RegistrarEventosCasoUso.MAXIMO_POR_LOTE + " eventos por envío");
        }
        return new EventosRegistradosRespuesta(registrar.registrar(lote.stream().map(EventoControlador::aEvento).toList()));
    }

    private static Evento aEvento(SolicitudEvento s) {
        if (s == null) {
            throw new DatoInvalidoException("El lote tiene un evento vacío");
        }
        return new Evento(tipo(s.tipo()), s.sesionId(), s.tienda(), s.producto(), s.categoria(), s.textoBusqueda(),
                s.cantidadResultados(), s.dispositivo(), s.detalle());
    }

    private static Evento.Tipo tipo(String texto) {
        try {
            return Evento.Tipo.valueOf(texto == null ? "" : texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DatoInvalidoException("Tipo de evento desconocido: " + texto);
        }
    }
}
