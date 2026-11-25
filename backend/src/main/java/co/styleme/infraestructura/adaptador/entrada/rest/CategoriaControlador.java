package co.styleme.infraestructura.adaptador.entrada.rest;

import co.styleme.aplicacion.puerto.entrada.catalogo.ConsultarCategoriasCasoUso;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.CategoriaRespuesta;
import co.styleme.infraestructura.adaptador.entrada.rest.mapeador.CatalogoRestMapeador;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST para las categorías. */
@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "Ropa, maquillaje, skincare y cuidado capilar")
public class CategoriaControlador {

    private final ConsultarCategoriasCasoUso consultarCategorias;

    public CategoriaControlador(ConsultarCategoriasCasoUso consultarCategorias) {
        this.consultarCategorias = consultarCategorias;
    }

    /** GET /api/categorias */
    @GetMapping
    @Operation(summary = "Listar las categorías del catálogo")
    public List<CategoriaRespuesta> listar() {
        return consultarCategorias.listar().stream().map(CatalogoRestMapeador::aRespuesta).toList();
    }
}
