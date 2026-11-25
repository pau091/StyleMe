package co.styleme.infraestructura.adaptador.entrada.rest;

import co.styleme.aplicacion.puerto.entrada.analitica.ConsultarIndicadoresCasoUso;
import co.styleme.dominio.modelo.analitica.Indicador;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.IndicadorRespuesta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST: indicadores para el panel de administración y el informe de datos. */
@RestController
@RequestMapping("/api/admin/indicadores")
@Tag(name = "Administración: indicadores", description = "Indicadores calculados con las vistas SQL (basedatos/05-vistas-indicadores.sql)")
public class AdminIndicadorControlador {

    private final ConsultarIndicadoresCasoUso indicadores;

    public AdminIndicadorControlador(ConsultarIndicadoresCasoUso indicadores) {
        this.indicadores = indicadores;
    }

    /** GET /api/admin/indicadores */
    @GetMapping
    @Operation(summary = "Todos los indicadores")
    public List<IndicadorRespuesta> todos() {
        return indicadores.todos().stream().map(IndicadorRespuesta::de).toList();
    }

    /** GET /api/admin/indicadores/conversion_tiendas */
    @GetMapping("/{codigo}")
    @Operation(summary = "Un indicador por su código")
    @ApiResponse(responseCode = "400", description = "Código de indicador desconocido")
    public IndicadorRespuesta uno(@Parameter(example = "conversion_tiendas") @PathVariable String codigo) {
        return IndicadorRespuesta.de(indicadores.uno(Indicador.Codigo.desdeCodigo(codigo)));
    }
}
