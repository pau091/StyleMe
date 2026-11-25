package co.styleme.infraestructura.adaptador.entrada.rest;

import co.styleme.aplicacion.puerto.entrada.comunidad.ModerarResenasCasoUso;
import co.styleme.dominio.excepcion.DatoInvalidoException;
import co.styleme.dominio.modelo.comunidad.Resena;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.ResenaRespuesta;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.SolicitudModeracion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para moderar reseñas. Lo usará el flujo de n8n (Gemini decide si la reseña
 * es apropiada) o un administrador. Protegido con la clave de administración.
 */
@RestController
@SecurityRequirement(name = "claveAdmin") // pide la clave de administración (FiltroClaveAdmin)
@RequestMapping("/api/admin/resenas")
@Tag(name = "Administración de reseñas", description = "Moderación de las reseñas pendientes")
public class AdminResenaControlador {

    private final ModerarResenasCasoUso moderar;

    public AdminResenaControlador(ModerarResenasCasoUso moderar) {
        this.moderar = moderar;
    }

    /** GET /api/admin/resenas/pendientes?limite=20 */
    @GetMapping("/pendientes")
    @Operation(summary = "Reseñas pendientes de moderación (las más antiguas primero)")
    public List<ResenaRespuesta> pendientes(@Parameter(example = "20") @RequestParam(defaultValue = "20") int limite) {
        return moderar.pendientes(limite).stream().map(ResenaRespuesta::de).toList();
    }

    /** PATCH /api/admin/resenas/15  {"estado":"PUBLICADA"} */
    @PatchMapping("/{id}")
    @Operation(summary = "Publicar o rechazar una reseña")
    @ApiResponse(responseCode = "400", description = "Estado inválido o la reseña ya fue moderada")
    @ApiResponse(responseCode = "404", description = "No existe la reseña")
    public ResenaRespuesta moderar(@PathVariable long id, @RequestBody SolicitudModeracion solicitud) {
        return ResenaRespuesta.de(moderar.moderar(id, estado(solicitud.estado())));
    }

    private static Resena.Estado estado(String texto) {
        try {
            return Resena.Estado.valueOf(texto == null ? "" : texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DatoInvalidoException("El estado debe ser PUBLICADA o RECHAZADA");
        }
    }
}
