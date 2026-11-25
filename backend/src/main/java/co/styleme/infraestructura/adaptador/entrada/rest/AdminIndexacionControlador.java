package co.styleme.infraestructura.adaptador.entrada.rest;

import co.styleme.aplicacion.puerto.entrada.ia.IndexarConocimientoCasoUso;
import co.styleme.dominio.modelo.ia.ResultadoIndexacion;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.IndexacionRespuesta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para administrar el índice vectorial.
 * ⚠️ En la rama feature/autenticacion este endpoint quedará solo para el rol ADMIN.
 * También lo usará n8n para reindexar cuando cambie un producto (rama feature/n8n-docker).
 */
@RestController
@RequestMapping("/api/admin/indexacion")
@Tag(name = "Administración de la IA", description = "Índice vectorial de tiendas, productos y guías")
public class AdminIndexacionControlador {

    private final IndexarConocimientoCasoUso indexarConocimiento;

    public AdminIndexacionControlador(IndexarConocimientoCasoUso indexarConocimiento) {
        this.indexarConocimiento = indexarConocimiento;
    }

    /** POST /api/admin/indexacion */
    @PostMapping
    @Operation(summary = "Reindexar el conocimiento",
            description = "Calcula los embeddings de lo nuevo o modificado (compara hashes) y borra lo que ya no existe.")
    @ApiResponse(responseCode = "503", description = "El servicio de IA no está disponible")
    public IndexacionRespuesta reindexar() {
        long inicio = System.currentTimeMillis();
        ResultadoIndexacion r = indexarConocimiento.indexar();
        return new IndexacionRespuesta(r.total(), r.nuevos(), r.cambiados(), r.sinCambios(), r.eliminados(),
                r.embeddingsCalculados(), r.modelo(), System.currentTimeMillis() - inicio);
    }
}
