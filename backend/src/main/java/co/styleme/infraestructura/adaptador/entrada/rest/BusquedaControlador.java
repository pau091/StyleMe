package co.styleme.infraestructura.adaptador.entrada.rest;

import co.styleme.aplicacion.puerto.entrada.ia.BuscarSemanticoCasoUso;
import co.styleme.dominio.modelo.ia.FiltroBusqueda;
import co.styleme.dominio.modelo.ia.ResultadoBusqueda;
import co.styleme.dominio.modelo.ia.TipoFuente;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.ResultadoBusquedaRespuesta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST para la búsqueda semántica (por significado). */
@RestController
@RequestMapping("/api/busqueda")
@Tag(name = "Búsqueda semántica", description = "Búsqueda por significado con embeddings y pgvector")
public class BusquedaControlador {

    private final BuscarSemanticoCasoUso buscarSemantico;

    public BusquedaControlador(BuscarSemanticoCasoUso buscarSemantico) {
        this.buscarSemantico = buscarSemantico;
    }

    /** GET /api/busqueda?q=algo para que no me brille la cara&categoria=skincare&precioMaximo=60000 */
    @GetMapping
    @Operation(summary = "Buscar por significado",
            description = """
                    Convierte la consulta en un embedding y devuelve los fragmentos más parecidos (tiendas, productos y guías).
                    Búsqueda híbrida: primero aplica los filtros (categoría, presupuesto, tipo) y luego ordena por similitud.""")
    @ApiResponse(responseCode = "400", description = "Consulta vacía o filtros inválidos")
    @ApiResponse(responseCode = "503", description = "El servicio de IA no está disponible")
    public List<ResultadoBusquedaRespuesta> buscar(
            @Parameter(description = "Lo que la persona busca, en sus palabras", example = "algo para que no me brille la cara")
            @RequestParam(name = "q", required = false) String consulta,
            @Parameter(description = "Slug de la categoría", example = "skincare") @RequestParam(required = false) String categoria,
            @Parameter(description = "Presupuesto máximo en pesos (solo productos)", example = "60000") @RequestParam(required = false) Integer precioMaximo,
            @Parameter(description = "tienda, producto o guia") @RequestParam(required = false) String tipo,
            @Parameter(description = "Cuántos resultados (1 a 20)", example = "5") @RequestParam(defaultValue = "5") int limite,
            @Parameter(description = "Similitud mínima (0 a 1)", example = "0.5") @RequestParam(defaultValue = "0.5") double similitudMinima) {
        var filtro = new FiltroBusqueda(categoria, precioMaximo, tipo == null ? null : TipoFuente.desdeCodigo(tipo),
                limite, similitudMinima);
        return buscarSemantico.buscar(consulta, filtro).stream().map(BusquedaControlador::aRespuesta).toList();
    }

    private static ResultadoBusquedaRespuesta aRespuesta(ResultadoBusqueda r) {
        return new ResultadoBusquedaRespuesta(r.tipo().codigo(), r.titulo(), r.texto(), r.codigoProducto(), r.slugTienda(),
                Math.round(r.similitud() * 1000) / 1000.0);
    }
}
