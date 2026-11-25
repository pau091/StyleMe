package co.styleme.infraestructura.adaptador.salida.conocimiento;

import co.styleme.aplicacion.puerto.salida.ia.GuiaRepositorioPuerto;
import co.styleme.dominio.modelo.ia.Guia;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida: lee las guías de conocimiento desde los archivos Markdown del proyecto
 * (src/main/resources/conocimiento/guias/*.md). Si mañana las guías vivieran en la base de datos,
 * solo se cambiaría este adaptador.
 */
@Component
public class GuiasClasspathAdaptador implements GuiaRepositorioPuerto {

    private static final String PATRON = "classpath*:conocimiento/guias/*.md";

    @Override
    public List<Guia> listar() {
        try {
            Resource[] archivos = new PathMatchingResourcePatternResolver().getResources(PATRON);
            return Arrays.stream(archivos)
                    .filter(Resource::isReadable)
                    .sorted(Comparator.comparing(r -> Objects.requireNonNullElse(r.getFilename(), "")))
                    .map(GuiasClasspathAdaptador::leer)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudieron leer las guías de conocimiento", e);
        }
    }

    private static Guia leer(Resource archivo) {
        try {
            return new Guia(archivo.getFilename(), archivo.getContentAsString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la guía " + archivo.getFilename(), e);
        }
    }
}
