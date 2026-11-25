package co.styleme.aplicacion.casodeuso.ia;

import co.styleme.aplicacion.puerto.salida.ia.AlmacenVectorialPuerto;
import co.styleme.aplicacion.puerto.salida.ia.EmbeddingPuerto;
import co.styleme.dominio.modelo.ia.Embedding;
import co.styleme.dominio.modelo.ia.FiltroBusqueda;
import co.styleme.dominio.modelo.ia.FragmentoConocimiento;
import co.styleme.dominio.modelo.ia.ResultadoBusqueda;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementaciones FALSAS de los puertos de IA, para probar los casos de uso sin Gemini ni pgvector.
 * Así se ve la ventaja de la arquitectura hexagonal: el caso de uso no nota la diferencia.
 */
final class Falsos {

    private Falsos() {
    }

    /**
     * Embeddings de juguete: cada palabra del texto suma en una posición del vector.
     * Textos que comparten palabras quedan "cerca", como pasa (mucho mejor) con un modelo real.
     */
    static final class EmbeddingFalso implements EmbeddingPuerto {

        int textosCalculados = 0;
        int llamadas = 0;

        @Override
        public List<Embedding> deDocumentos(List<String> textos) {
            llamadas++;
            textosCalculados += textos.size();
            return textos.stream().map(EmbeddingFalso::vectorizar).toList();
        }

        @Override
        public Embedding deConsulta(String texto) {
            return vectorizar(texto);
        }

        @Override
        public String modelo() {
            return "modelo-falso";
        }

        static Embedding vectorizar(String texto) {
            float[] v = new float[Embedding.DIMENSIONES];
            v[0] = 0.01f; // nunca un vector de ceros
            for (String palabra : texto.toLowerCase().split("[^\\p{L}0-9]+")) {
                if (palabra.length() > 2) {
                    int posicion = Math.floorMod(new String(palabra.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8).hashCode(), Embedding.DIMENSIONES);
                    v[posicion] += 1f;
                }
            }
            return Embedding.de(v);
        }
    }

    /** Almacén vectorial en memoria: guarda en un mapa y busca comparando con todos. */
    static final class AlmacenFalso implements AlmacenVectorialPuerto {

        final Map<String, FragmentoConocimiento> fragmentos = new HashMap<>();
        final Map<String, Embedding> vectores = new HashMap<>();

        @Override
        public Map<String, String> hashesPorClave(String modelo) {
            Map<String, String> hashes = new HashMap<>();
            fragmentos.forEach((clave, f) -> hashes.put(clave, f.hashTexto()));
            return hashes;
        }

        @Override
        public void guardar(FragmentoConocimiento fragmento, Embedding embedding, String modelo) {
            fragmentos.put(fragmento.clave(), fragmento);
            vectores.put(fragmento.clave(), embedding);
        }

        @Override
        public int eliminarExcepto(Set<String> clavesVigentes) {
            List<String> sobrantes = new ArrayList<>(fragmentos.keySet());
            sobrantes.removeAll(clavesVigentes);
            sobrantes.forEach(c -> {
                fragmentos.remove(c);
                vectores.remove(c);
            });
            return sobrantes.size();
        }

        @Override
        public List<ResultadoBusqueda> buscarParecidos(Embedding consulta, FiltroBusqueda filtro) {
            return fragmentos.entrySet().stream()
                    .filter(e -> filtro.tipo() == null || e.getValue().tipo() == filtro.tipo())
                    .map(e -> new ResultadoBusqueda(e.getValue().tipo(), e.getValue().titulo(), e.getValue().texto(),
                            null, null, vectores.get(e.getKey()).similitud(consulta)))
                    .filter(r -> r.similitud() >= filtro.similitudMinima())
                    .sorted(Comparator.comparingDouble(ResultadoBusqueda::similitud).reversed())
                    .limit(filtro.limite())
                    .toList();
        }

        @Override
        public long contar() {
            return fragmentos.size();
        }
    }
}
