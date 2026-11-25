package co.styleme.ia;

import static org.assertj.core.api.Assertions.assertThat;

import co.styleme.aplicacion.puerto.entrada.catalogo.ConsultarProductosCasoUso;
import co.styleme.aplicacion.puerto.entrada.ia.BuscarSemanticoCasoUso;
import co.styleme.aplicacion.puerto.entrada.ia.IndexarConocimientoCasoUso;
import co.styleme.dominio.modelo.catalogo.FiltroProductos;
import co.styleme.dominio.modelo.ia.FiltroBusqueda;
import co.styleme.dominio.modelo.ia.ResultadoBusqueda;
import co.styleme.dominio.modelo.ia.ResultadoIndexacion;
import co.styleme.dominio.modelo.ia.TipoFuente;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de la búsqueda semántica con embeddings REALES de Gemini y el índice HNSW de pgvector.
 *
 * Solo corre si hay una clave de Gemini (variable GEMINI_API_KEY o el archivo .env); si no, se salta.
 * La primera vez indexa todo el catálogo (unos 80 embeddings); las siguientes, como compara hashes,
 * no gasta tokens en la indexación. El índice queda guardado en la base (es el índice real).
 */
@SpringBootTest
@Tag("ia")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIf("hayClaveGemini")
class BusquedaSemanticaGeminiIntegracionTest {

    @Autowired
    private IndexarConocimientoCasoUso indexar;

    @Autowired
    private BuscarSemanticoCasoUso buscar;

    @Autowired
    private ConsultarProductosCasoUso productos;

    private ResultadoIndexacion primeraIndexacion;

    @BeforeAll
    void indexarCatalogo() {
        primeraIndexacion = indexar.indexar();
    }

    private List<ResultadoBusqueda> top(String consulta, TipoFuente tipo, String categoria, int k) {
        return buscar.buscar(consulta, new FiltroBusqueda(categoria, null, tipo, k, 0.0));
    }

    @Test
    @DisplayName("Indexa tiendas, productos y guías; la segunda vez no recalcula nada")
    void indexacion() {
        assertThat(primeraIndexacion.total()).isGreaterThanOrEqualTo(10 + 49 + 15);
        ResultadoIndexacion segunda = indexar.indexar();
        assertThat(segunda.embeddingsCalculados()).isZero();
        assertThat(segunda.sinCambios()).isEqualTo(segunda.total());
    }

    @Test
    @DisplayName("'algo para que no me brille la cara' encuentra productos para piel grasa")
    void pielGrasaSinDecirGrasa() {
        List<ResultadoBusqueda> r = top("algo para que no me brille la cara", TipoFuente.PRODUCTO, "skincare", 3);
        assertThat(r).isNotEmpty();
        assertThat(r).anyMatch(x -> x.texto().toLowerCase().contains("grasa"));
    }

    @Test
    @DisplayName("'shampoo sin sulfatos para rizos' encuentra productos capilares para cabello rizado")
    void cabelloRizado() {
        List<ResultadoBusqueda> r = top("shampoo sin sulfatos para mis rizos", TipoFuente.PRODUCTO, null, 3);
        assertThat(r).anyMatch(x -> x.texto().contains("rizado") || x.texto().contains("sulfatos"));
    }

    @Test
    @DisplayName("Una pregunta general encuentra la sección correcta de una guía")
    void guia() {
        List<ResultadoBusqueda> r = top("¿tengo que usar bloqueador aunque esté nublado?", TipoFuente.GUIA, null, 1);
        assertThat(r).isNotEmpty();
        assertThat(r.get(0).titulo()).contains("Protector solar");
    }

    @Test
    @DisplayName("Embeddings vs. palabras: la búsqueda por palabras no encuentra nada; la semántica sí")
    void semanticaGanaAPalabras() {
        String consulta = "que no me brille";
        int porPalabras = productos.buscar(new FiltroProductos("skincare", null, null, consulta)).size();
        int porSignificado = top(consulta, TipoFuente.PRODUCTO, "skincare", 5).size();
        assertThat(porPalabras).isZero();
        assertThat(porSignificado).isPositive();
    }

    /** ¿Hay una clave de Gemini? Busca la variable de entorno y luego el archivo .env (raíz del proyecto). */
    static boolean hayClaveGemini() {
        if (esClaveValida(System.getenv("GEMINI_API_KEY"))) {
            return true;
        }
        for (Path env : List.of(Path.of("..", ".env"), Path.of(".env"))) {
            try {
                if (Files.exists(env) && Files.readAllLines(env).stream()
                        .map(String::trim)
                        .filter(l -> l.startsWith("GEMINI_API_KEY="))
                        .anyMatch(l -> esClaveValida(l.substring("GEMINI_API_KEY=".length())))) {
                    return true;
                }
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    private static boolean esClaveValida(String clave) {
        return clave != null && !clave.isBlank() && !clave.trim().startsWith("pega-aqui");
    }
}
