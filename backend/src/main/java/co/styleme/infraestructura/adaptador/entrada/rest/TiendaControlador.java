package co.styleme.infraestructura.adaptador.entrada.rest;

import co.styleme.aplicacion.puerto.entrada.catalogo.ConsultarProductosCasoUso;
import co.styleme.aplicacion.puerto.entrada.catalogo.ConsultarTiendasCasoUso;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.ProductoRespuesta;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.TiendaRespuesta;
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

/**
 * Adaptador de entrada REST para las tiendas.
 * Solo recibe la petición, llama al caso de uso y convierte la respuesta (regla R7: no usa repositorios).
 */
@RestController
@RequestMapping("/api/tiendas")
@Tag(name = "Tiendas", description = "Tiendas físicas de Bucaramanga")
public class TiendaControlador {

    private final ConsultarTiendasCasoUso consultarTiendas;
    private final ConsultarProductosCasoUso consultarProductos;

    public TiendaControlador(ConsultarTiendasCasoUso consultarTiendas, ConsultarProductosCasoUso consultarProductos) {
        this.consultarTiendas = consultarTiendas;
        this.consultarProductos = consultarProductos;
    }

    /** GET /api/tiendas?categoria=ropa&zona=Cabecera */
    @GetMapping
    @Operation(summary = "Listar tiendas activas", description = "Filtros opcionales por categoría principal y zona.")
    public List<TiendaRespuesta> listar(
            @Parameter(description = "Slug de la categoría", example = "ropa") @RequestParam(required = false) String categoria,
            @Parameter(description = "Zona de la ciudad", example = "Cabecera") @RequestParam(required = false) String zona) {
        return consultarTiendas.listar(categoria, zona).stream().map(CatalogoRestMapeador::aRespuesta).toList();
    }

    /** GET /api/tiendas/bohor */
    @GetMapping("/{slug}")
    @Operation(summary = "Obtener una tienda por su slug")
    @ApiResponse(responseCode = "200", description = "La tienda existe")
    @ApiResponse(responseCode = "404", description = "No existe una tienda con ese slug")
    public TiendaRespuesta obtener(@Parameter(example = "bohor") @PathVariable String slug) {
        return CatalogoRestMapeador.aRespuesta(consultarTiendas.obtener(slug));
    }

    /** GET /api/tiendas/pandora-makeup/productos */
    @GetMapping("/{slug}/productos")
    @Operation(summary = "Productos de una tienda")
    @ApiResponse(responseCode = "404", description = "No existe una tienda con ese slug")
    public List<ProductoRespuesta> productos(@Parameter(example = "pandora-makeup") @PathVariable String slug) {
        return consultarProductos.listarDeTienda(slug).stream().map(CatalogoRestMapeador::aRespuesta).toList();
    }
}
