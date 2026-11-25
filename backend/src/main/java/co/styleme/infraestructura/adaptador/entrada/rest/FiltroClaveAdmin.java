package co.styleme.infraestructura.adaptador.entrada.rest;

import co.styleme.infraestructura.adaptador.entrada.rest.dto.ErrorRespuesta;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

/**
 * Protege las rutas de administración (/api/admin/*) con una clave (D-066).
 *
 * Quien llame a esas rutas (el panel de administración, n8n o Swagger) debe mandar la cabecera
 *   X-Admin-Key: <la clave de STYLEME_ADMIN_CLAVE en el .env>
 * El resto de la API (catálogo, búsqueda, Lía, reseñas, favoritos, eventos) sigue abierta.
 *
 * Es un filtro: se ejecuta ANTES de que la petición llegue al controlador.
 */
@Component
public class FiltroClaveAdmin extends OncePerRequestFilter {

    public static final String CABECERA = "X-Admin-Key";
    private static final String RUTA_PROTEGIDA = "/api/admin";
    private static final Logger LOG = LoggerFactory.getLogger(FiltroClaveAdmin.class);

    private final byte[] clave;
    private final JsonMapper json;

    public FiltroClaveAdmin(@Value("${styleme.admin.clave:}") String clave, JsonMapper json) {
        this.clave = clave == null ? new byte[0] : clave.trim().getBytes(StandardCharsets.UTF_8);
        this.json = json;
        if (this.clave.length == 0) {
            LOG.warn("STYLEME_ADMIN_CLAVE no está configurada: las rutas /api/admin/* quedan bloqueadas");
        }
    }

    /** Solo se revisan las rutas de administración; las peticiones OPTIONS (permiso CORS del navegador) pasan. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest peticion) {
        String ruta = peticion.getRequestURI().substring(peticion.getContextPath().length());
        boolean esAdmin = ruta.equals(RUTA_PROTEGIDA) || ruta.startsWith(RUTA_PROTEGIDA + "/");
        return !esAdmin || "OPTIONS".equalsIgnoreCase(peticion.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        if (clave.length == 0) {
            rechazar(respuesta, peticion, HttpStatus.SERVICE_UNAVAILABLE,
                    "La administración está desactivada: falta STYLEME_ADMIN_CLAVE en el .env");
            return;
        }
        String recibida = peticion.getHeader(CABECERA);
        // MessageDigest.isEqual tarda lo mismo acierte o no: no deja adivinar la clave midiendo tiempos
        if (recibida == null || !MessageDigest.isEqual(clave, recibida.trim().getBytes(StandardCharsets.UTF_8))) {
            LOG.warn("Acceso de administración rechazado a {} desde {}", peticion.getRequestURI(), peticion.getRemoteAddr());
            rechazar(respuesta, peticion, HttpStatus.UNAUTHORIZED, "Falta la clave de administración o no es válida (cabecera " + CABECERA + ")");
            return;
        }
        cadena.doFilter(peticion, respuesta);
    }

    private void rechazar(HttpServletResponse respuesta, HttpServletRequest peticion, HttpStatus estado, String mensaje)
            throws IOException {
        respuesta.setStatus(estado.value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        json.writeValue(respuesta.getOutputStream(),
                ErrorRespuesta.de(estado.value(), estado.getReasonPhrase(), mensaje, peticion.getRequestURI()));
    }
}
