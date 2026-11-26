package co.styleme.infraestructura.adaptador.salida.imagen;

import co.styleme.aplicacion.puerto.salida.ia.GeneradorImagenPuerto;
import co.styleme.dominio.excepcion.ServicioIaNoDisponibleException;
import co.styleme.dominio.modelo.probador.Imagen;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida cuando NO hay proveedor de imágenes (STYLEME_PROBADOR_PROVEEDOR=ninguno, el valor por defecto).
 * Responde "no disponible" y la página usa el probador local (MediaPipe, la foto no sale del navegador).
 */
@Component
@ConditionalOnProperty(name = "styleme.probador.proveedor", havingValue = "ninguno", matchIfMissing = true)
public class ProbadorApagadoAdaptador implements GeneradorImagenPuerto {

    @Override
    public boolean disponible() {
        return false;
    }

    @Override
    public String proveedor() {
        return null;
    }

    @Override
    public Imagen generar(String instrucciones, List<Imagen> referencias) {
        throw new ServicioIaNoDisponibleException("El probador con IA no está activado");
    }
}
