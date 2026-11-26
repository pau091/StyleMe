package co.styleme.infraestructura.adaptador.entrada.rest;

import co.styleme.aplicacion.puerto.entrada.analitica.ConsultarTendenciasCasoUso;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.TendenciasRespuesta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST: tendencias públicas para la portada ("Lo más buscado esta semana"). */
@RestController
@RequestMapping("/api/tendencias")
@Tag(name = "Tendencias", description = "Lo más buscado y visto en los últimos días (totales anónimos)")
public class TendenciaControlador {

    private final ConsultarTendenciasCasoUso tendencias;

    public TendenciaControlador(ConsultarTendenciasCasoUso tendencias) {
        this.tendencias = tendencias;
    }

    /** GET /api/tendencias?dias=7 */
    @GetMapping
    @Operation(summary = "Lo más buscado y visto",
            description = "Búsquedas (solo si las hicieron 3 personas o más y tuvieron resultados), tiendas y productos más vistos.")
    @ApiResponse(responseCode = "400", description = "El periodo no está entre 1 y 90 días")
    public ResponseEntity<TendenciasRespuesta> ultimosDias(@Parameter(example = "7") @RequestParam(defaultValue = "7") int dias) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic())
                .body(TendenciasRespuesta.de(tendencias.ultimosDias(dias)));
    }
}
