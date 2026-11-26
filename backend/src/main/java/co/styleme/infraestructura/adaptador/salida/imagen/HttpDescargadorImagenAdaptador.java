package co.styleme.infraestructura.adaptador.salida.imagen;

import co.styleme.aplicacion.puerto.salida.ia.DescargadorImagenPuerto;
import co.styleme.dominio.excepcion.DatoInvalidoException;
import co.styleme.dominio.excepcion.ServicioIaNoDisponibleException;
import co.styleme.dominio.modelo.probador.Imagen;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adaptador de salida: descarga la foto de la prenda desde la tienda en línea (Shopify, WordPress...).
 * Solo direcciones https del catálogo, con tiempo máximo y tamaño máximo (8 MB).
 */
@Component
public class HttpDescargadorImagenAdaptador implements DescargadorImagenPuerto {

    private final RestClient cliente;

    @Autowired
    public HttpDescargadorImagenAdaptador() {
        this(RestClient.builder().requestFactory(OpenAiImagenAdaptador.fabrica(Duration.ofSeconds(15))).build());
    }

    /** Para las pruebas: permite usar un servidor simulado. */
    HttpDescargadorImagenAdaptador(RestClient cliente) {
        this.cliente = cliente;
    }

    @Override
    public Imagen descargar(String url) {
        if (url == null || !url.startsWith("https://")) {
            throw new DatoInvalidoException("La foto de la prenda no es una dirección segura (https)");
        }
        try {
            ResponseEntity<byte[]> respuesta = cliente.get().uri(url).retrieve().toEntity(byte[].class);
            String tipo = respuesta.getHeaders().getContentType() == null ? "" : respuesta.getHeaders().getContentType().toString();
            byte[] datos = respuesta.getBody() == null ? new byte[0] : respuesta.getBody();
            return new Imagen(datos, tipo); // valida tipo (JPG, PNG, WEBP) y tamaño
        } catch (RestClientException e) {
            throw new ServicioIaNoDisponibleException("No se pudo traer la foto de la prenda desde la tienda", e);
        }
    }
}
