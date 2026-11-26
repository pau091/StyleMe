package co.styleme.infraestructura.adaptador.entrada.rest;

import co.styleme.aplicacion.puerto.entrada.comunidad.GestionarResenasCasoUso;
import co.styleme.dominio.excepcion.DatoInvalidoException;
import co.styleme.dominio.modelo.comunidad.Objetivo;
import co.styleme.dominio.modelo.comunidad.Resena;
import co.styleme.dominio.modelo.comunidad.ResumenCalificacion;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.ResenaRespuesta;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.ResenasRespuesta;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.SolicitudResena;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Adaptador de entrada REST: reseñas anónimas (D-066). Toda reseña nueva espera moderación. */
@RestController
@RequestMapping("/api/resenas")
@Tag(name = "Reseñas", description = "Calificaciones con corazones (1 a 5) de tiendas y productos")
public class ResenaControlador {

    private final GestionarResenasCasoUso resenas;
    private final LimitadorPeticiones limitador;

    public ResenaControlador(GestionarResenasCasoUso resenas, LimitadorPeticiones limitador) {
        this.resenas = resenas;
        this.limitador = limitador;
    }

    /** GET /api/resenas?tienda=bohor  o  ?producto=p049 */
    @GetMapping
    @Operation(summary = "Reseñas publicadas y calificación promedio", description = "Indica tienda O producto (solo uno).")
    @ApiResponse(responseCode = "400", description = "No se indicó tienda ni producto, o se indicaron los dos")
    public ResenasRespuesta publicadas(
            @Parameter(example = "bohor") @RequestParam(required = false) String tienda,
            @Parameter(example = "p049") @RequestParam(required = false) String producto) {
        Objetivo objetivo = Objetivo.deUno(tienda, producto);
        ResumenCalificacion resumen = resenas.resumen(objetivo);
        return new ResenasRespuesta(resumen.promedio(), resumen.cantidad(),
                resenas.publicadas(objetivo).stream().map(ResenaRespuesta::de).toList());
    }

    /** GET /api/resenas/recientes?limite=6 */
    @GetMapping("/recientes")
    @Operation(summary = "Últimas reseñas publicadas con comentario",
            description = "De cualquier tienda o producto, para la portada. Todas pasaron por moderación. Máximo 12.")
    public ResponseEntity<List<ResenaRespuesta>> recientes(@Parameter(example = "6") @RequestParam(defaultValue = "6") int limite) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(2)).cachePublic())
                .body(resenas.recientes(limite).stream().map(ResenaRespuesta::de).toList());
    }

    /** POST /api/resenas */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Escribir una reseña", description = "Queda PENDIENTE hasta que se modere. Una reseña por sesión y por tienda/producto.")
    @ApiResponse(responseCode = "201", description = "Reseña recibida (pendiente de moderación)")
    @ApiResponse(responseCode = "400", description = "Datos inválidos (corazones fuera de 1 a 5, comentario muy largo...)")
    @ApiResponse(responseCode = "404", description = "La tienda o el producto no existe")
    @ApiResponse(responseCode = "409", description = "Esta sesión ya reseñó esa tienda o producto")
    @ApiResponse(responseCode = "429", description = "Demasiadas reseñas en poco tiempo")
    public ResenaRespuesta escribir(@RequestBody SolicitudResena solicitud, HttpServletRequest peticion) {
        if (!limitador.permitir("resena:" + peticion.getRemoteAddr())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Demasiadas reseñas seguidas; espera un minuto");
        }
        if (solicitud.corazones() == null) {
            throw new DatoInvalidoException("Indica cuántos corazones (1 a 5)");
        }
        Resena nueva = Resena.nueva(solicitud.sesionId(), solicitud.autor(),
                Objetivo.deUno(solicitud.tienda(), solicitud.producto()), solicitud.corazones(), solicitud.comentario());
        return ResenaRespuesta.de(resenas.escribir(nueva));
    }
}
