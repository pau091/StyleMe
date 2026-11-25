package co.styleme.infraestructura.adaptador.salida.conocimiento;

import co.styleme.aplicacion.puerto.salida.ia.PlantillaInstruccionesPuerto;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida: lee las instrucciones de Lía desde src/main/resources/prompts/lia-instrucciones.md.
 * Tenerlas fuera del código permite ajustar su personalidad sin tocar Java.
 * Se leen una sola vez, al arrancar.
 */
@Component
public class PlantillaClasspathAdaptador implements PlantillaInstruccionesPuerto {

    private static final String ARCHIVO = "prompts/lia-instrucciones.md";

    private final String instrucciones;

    public PlantillaClasspathAdaptador() {
        try {
            this.instrucciones = new ClassPathResource(ARCHIVO).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se encontró la plantilla de Lía: " + ARCHIVO, e);
        }
        if (!instrucciones.contains(TIENDAS) || !instrucciones.contains(CONTEXTO)) {
            throw new IllegalStateException("La plantilla de Lía debe tener los huecos " + TIENDAS + " y " + CONTEXTO);
        }
    }

    @Override
    public String instruccionesLia() {
        return instrucciones;
    }
}
