package co.styleme.infraestructura.adaptador.entrada.rest;

import co.styleme.aplicacion.puerto.entrada.catalogo.ConsultarProductosCasoUso;
import co.styleme.dominio.modelo.catalogo.FiltroProductos;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.ProductoRespuesta;
import co.styleme.infraestructura.adaptador.entrada.rest.mapeador.CatalogoRestMapeador;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST para los productos del catálogo. */
@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Catálogo de productos reales de las tiendas")
public class ProductoControlador {

    private final ConsultarProductosCasoUso consultarProductos;

    public ProductoControlador(ConsultarProductosCasoUso consultarProductos) {
        this.consultarProductos = consultarProductos;
    }

    /** GET /api/productos?categoria=skincare&tienda=pandora-makeup&precioMaximo=50000&q=serum */
    @GetMapping
    @Operation(summary = "Buscar productos",
            description = "Todos los filtros son opcionales. La búsqueda de texto ignora tildes y mayúsculas.")
    @ApiResponse(responseCode = "400", description = "Un filtro tiene un valor inválido (por ejemplo, precio negativo)")
    public List<ProductoRespuesta> buscar(
            @Parameter(description = "Slug de la categoría", example = "skincare") @RequestParam(required = false) String categoria,
            @Parameter(description = "Slug de la tienda", example = "pandora-makeup") @RequestParam(required = false) String tienda,
            @Parameter(description = "Presupuesto máximo en pesos", example = "50000") @RequestParam(required = false) Integer precioMaximo,
            @Parameter(description = "Texto a buscar en nombre o marca", example = "serum") @RequestParam(name = "q", required = false) String texto) {
        var filtro = new FiltroProductos(categoria, tienda, precioMaximo, texto);
        return consultarProductos.buscar(filtro).stream().map(CatalogoRestMapeador::aRespuesta).toList();
    }

    /** GET /api/productos/p028 */
    @GetMapping("/{codigo}")
    @Operation(summary = "Obtener un producto por su código")
    @ApiResponse(responseCode = "404", description = "No existe un producto con ese código")
    public ProductoRespuesta obtener(@Parameter(example = "p028") @PathVariable String codigo) {
        return CatalogoRestMapeador.aRespuesta(consultarProductos.obtener(codigo));
    }
}
