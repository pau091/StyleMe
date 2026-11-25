package co.styleme.infraestructura.adaptador.entrada.rest;

import co.styleme.aplicacion.puerto.entrada.comunidad.GestionarFavoritosCasoUso;
import co.styleme.dominio.modelo.comunidad.Objetivo;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.FavoritoRespuesta;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.SolicitudFavorito;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST: favoritos de la sesión anónima del navegador (D-066). */
@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Favoritos", description = "Tiendas y productos guardados por la sesión")
public class FavoritoControlador {

    private final GestionarFavoritosCasoUso favoritos;

    public FavoritoControlador(GestionarFavoritosCasoUso favoritos) {
        this.favoritos = favoritos;
    }

    /** GET /api/favoritos?sesionId=... */
    @GetMapping
    @Operation(summary = "Favoritos de la sesión (los más recientes primero)")
    public List<FavoritoRespuesta> listar(@RequestParam String sesionId) {
        return favoritos.listar(sesionId).stream().map(FavoritoRespuesta::de).toList();
    }

    /** POST /api/favoritos {"sesionId":"...","tienda":"bohor"} */
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Guardar un favorito", description = "Si ya estaba guardado, no pasa nada (no se duplica).")
    @ApiResponse(responseCode = "404", description = "La tienda o el producto no existe")
    public void agregar(@RequestBody SolicitudFavorito solicitud) {
        favoritos.agregar(solicitud.sesionId(), Objetivo.deUno(solicitud.tienda(), solicitud.producto()));
    }

    /** DELETE /api/favoritos?sesionId=...&tienda=bohor */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Quitar un favorito")
    public void quitar(@RequestParam String sesionId, @RequestParam(required = false) String tienda,
            @RequestParam(required = false) String producto) {
        favoritos.quitar(sesionId, Objetivo.deUno(tienda, producto));
    }
}
