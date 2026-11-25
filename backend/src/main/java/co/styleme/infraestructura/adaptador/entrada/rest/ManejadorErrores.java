package co.styleme.infraestructura.adaptador.entrada.rest;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import co.styleme.dominio.excepcion.RecursoDuplicadoException;
import co.styleme.dominio.excepcion.RecursoNoEncontradoException;
import co.styleme.dominio.excepcion.ServicioIaNoDisponibleException;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.ErrorRespuesta;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduce las excepciones a respuestas HTTP (regla R9).
 * El dominio lanza "TiendaNoEncontradaException"; aquí se convierte en un 404 con un JSON claro.
 * Así el dominio no sabe nada de HTTP.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> noEncontrado(RecursoNoEncontradoException e, HttpServletRequest peticion) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage(), peticion);
    }

    @ExceptionHandler(DatoInvalidoException.class)
    public ResponseEntity<ErrorRespuesta> datoInvalido(DatoInvalidoException e, HttpServletRequest peticion) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage(), peticion);
    }

    /** Por ejemplo, la misma sesión intenta reseñar dos veces la misma tienda: 409 = "conflicto". */
    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErrorRespuesta> duplicado(RecursoDuplicadoException e, HttpServletRequest peticion) {
        return responder(HttpStatus.CONFLICT, e.getMessage(), peticion);
    }

    /** Errores con su código HTTP ya decidido en el controlador (por ejemplo 429 = demasiadas peticiones). */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorRespuesta> conEstado(ResponseStatusException e, HttpServletRequest peticion) {
        HttpStatus estado = HttpStatus.valueOf(e.getStatusCode().value());
        return responder(estado, e.getReason() == null ? estado.getReasonPhrase() : e.getReason(), peticion);
    }

    /** Falta un parámetro obligatorio, por ejemplo ?sesionId= en los favoritos. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorRespuesta> faltaParametro(MissingServletRequestParameterException e, HttpServletRequest peticion) {
        return responder(HttpStatus.BAD_REQUEST, "Falta el parámetro '" + e.getParameterName() + "'", peticion);
    }

    /** La IA no respondió (sin clave, sin cupo, sin internet): 503 = "servicio no disponible por ahora". */
    @ExceptionHandler(ServicioIaNoDisponibleException.class)
    public ResponseEntity<ErrorRespuesta> iaNoDisponible(ServicioIaNoDisponibleException e, HttpServletRequest peticion) {
        LOG.warn("IA no disponible en {}: {}", peticion.getRequestURI(), e.getMessage());
        return responder(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de inteligencia artificial no está disponible en este momento", peticion);
    }

    /** Por ejemplo, ?precioMaximo=mucho (no es un número). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> tipoInvalido(MethodArgumentTypeMismatchException e, HttpServletRequest peticion) {
        return responder(HttpStatus.BAD_REQUEST, "El parámetro '" + e.getName() + "' no tiene un valor válido", peticion);
    }

    /** El cuerpo de la petición no es un JSON válido (o no está en UTF-8). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> jsonInvalido(HttpMessageNotReadableException e, HttpServletRequest peticion) {
        return responder(HttpStatus.BAD_REQUEST, "La petición no tiene un JSON válido", peticion);
    }

    /** Una dirección que no existe en la API. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorRespuesta> rutaInexistente(NoResourceFoundException e, HttpServletRequest peticion) {
        return responder(HttpStatus.NOT_FOUND, "La dirección no existe", peticion);
    }

    /** Cualquier otro error: se registra completo en el log, pero a la iu no se le muestran detalles internos. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> inesperado(Exception e, HttpServletRequest peticion) {
        LOG.error("Error inesperado en {}", peticion.getRequestURI(), e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado", peticion);
    }

    private ResponseEntity<ErrorRespuesta> responder(HttpStatus estado, String mensaje, HttpServletRequest peticion) {
        return ResponseEntity.status(estado)
                .body(ErrorRespuesta.de(estado.value(), estado.getReasonPhrase(), mensaje, peticion.getRequestURI()));
    }
}
