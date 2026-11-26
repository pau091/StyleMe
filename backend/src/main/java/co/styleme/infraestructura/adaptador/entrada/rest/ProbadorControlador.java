package co.styleme.infraestructura.adaptador.entrada.rest;

import co.styleme.aplicacion.puerto.entrada.probador.ProbarPrendaCasoUso;
import co.styleme.dominio.excepcion.DatoInvalidoException;
import co.styleme.dominio.modelo.probador.Imagen;
import co.styleme.dominio.modelo.probador.Pose;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.EstadoProbadorRespuesta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Adaptador de entrada REST: probador con IA (P-017).
 * Privacidad: la foto llega en memoria, se manda al proveedor de IA y se descarta. No se guarda ni se registra.
 */
@RestController
@RequestMapping("/api/probador")
@Tag(name = "Probador con IA", description = "La persona dentro de la tienda con la prenda, en 4 poses")
public class ProbadorControlador {

    private final ProbarPrendaCasoUso probador;
    private final LimitadorPeticiones limitador;

    public ProbadorControlador(ProbarPrendaCasoUso probador, LimitadorPeticiones limitador) {
        this.probador = probador;
        this.limitador = limitador;
    }

    /** GET /api/probador/estado */
    @GetMapping("/estado")
    @Operation(summary = "¿Está activo el probador con IA?", description = "Si no, la página usa el probador local (la foto no sale del navegador).")
    public EstadoProbadorRespuesta estado() {
        ProbarPrendaCasoUso.Estado e = probador.estado();
        return new EstadoProbadorRespuesta(e.disponible(), e.proveedor(),
                e.poses().stream().map(p -> new EstadoProbadorRespuesta.PoseRespuesta(p.name(), p.nombre())).toList());
    }

    /** POST /api/probador (multipart: foto, producto, pose y, opcional, fondo) → la imagen generada */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Generar una pose", description = "Devuelve una imagen (PNG o JPG). La página pide las 4 poses una por una.")
    @ApiResponse(responseCode = "200", description = "La imagen generada")
    @ApiResponse(responseCode = "400", description = "Foto inválida (no es JPG/PNG/WEBP), pose desconocida o el producto no es ropa")
    @ApiResponse(responseCode = "404", description = "El producto no existe")
    @ApiResponse(responseCode = "413", description = "La foto pesa más de 8 MB")
    @ApiResponse(responseCode = "429", description = "Demasiadas pruebas en poco tiempo")
    @ApiResponse(responseCode = "503", description = "El probador con IA no está activado o el proveedor no respondió")
    public ResponseEntity<byte[]> probar(
            @Parameter(description = "Foto de cuerpo completo de la persona") @RequestPart("foto") MultipartFile foto,
            @Parameter(example = "p001") @RequestParam String producto,
            @Parameter(example = "FRENTE") @RequestParam String pose,
            @Parameter(description = "Foto de la tienda (opcional)") @RequestPart(value = "fondo", required = false) MultipartFile fondo,
            HttpServletRequest peticion) {
        if (!limitador.permitir("probador:" + peticion.getRemoteAddr())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Demasiadas pruebas seguidas; espera un minuto");
        }
        Imagen resultado = probador.probar(aImagen(foto), producto, Pose.desde(pose), fondo == null || fondo.isEmpty() ? null : aImagen(fondo));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resultado.tipoMime()))
                .cacheControl(CacheControl.noStore()) // ni el navegador ni nginx guardan la imagen
                .body(resultado.datos());
    }

    private static Imagen aImagen(MultipartFile archivo) {
        try {
            return new Imagen(archivo.getBytes(), archivo.getContentType());
        } catch (IOException e) {
            throw new DatoInvalidoException("No se pudo leer la imagen");
        }
    }
}
