package co.styleme.aplicacion.casodeuso.ia;

import co.styleme.aplicacion.puerto.entrada.ia.IndexarConocimientoCasoUso;
import co.styleme.aplicacion.puerto.salida.ia.AlmacenVectorialPuerto;
import co.styleme.aplicacion.puerto.salida.ia.EmbeddingPuerto;
import co.styleme.aplicacion.puerto.salida.ia.GuiaRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ProductoRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.TiendaRepositorioPuerto;
import co.styleme.dominio.modelo.catalogo.FiltroProductos;
import co.styleme.dominio.modelo.ia.Embedding;
import co.styleme.dominio.modelo.ia.FragmentoConocimiento;
import co.styleme.dominio.modelo.ia.ResultadoIndexacion;
import co.styleme.dominio.servicio.FragmentadorConocimiento;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Caso de uso: INDEXAR el conocimiento de StyleMe (generación de embeddings + indexación vectorial).
 *
 * Pasos:
 *  1. Arma los fragmentos vigentes: tiendas activas, productos reales activos y secciones de las guías.
 *  2. Compara el hash de cada uno con el guardado: si es igual, NO pide el embedding (ahorra tokens).
 *  3. Pide a la IA los embeddings de los nuevos y cambiados, en lotes.
 *  4. Los guarda en el almacén vectorial (pgvector) y borra los que ya no existen.
 */
public class IndexarConocimientoServicio implements IndexarConocimientoCasoUso {

    /** Gemini acepta hasta 100 textos por llamada. */
    static final int TAMANO_LOTE = 100;

    private final TiendaRepositorioPuerto tiendas;
    private final ProductoRepositorioPuerto productos;
    private final GuiaRepositorioPuerto guias;
    private final EmbeddingPuerto embeddings;
    private final AlmacenVectorialPuerto almacen;
    private final FragmentadorConocimiento fragmentador;

    public IndexarConocimientoServicio(TiendaRepositorioPuerto tiendas, ProductoRepositorioPuerto productos,
            GuiaRepositorioPuerto guias, EmbeddingPuerto embeddings, AlmacenVectorialPuerto almacen,
            FragmentadorConocimiento fragmentador) {
        this.tiendas = Objects.requireNonNull(tiendas);
        this.productos = Objects.requireNonNull(productos);
        this.guias = Objects.requireNonNull(guias);
        this.embeddings = Objects.requireNonNull(embeddings);
        this.almacen = Objects.requireNonNull(almacen);
        this.fragmentador = Objects.requireNonNull(fragmentador);
    }

    @Override
    public ResultadoIndexacion indexar() {
        // 1. Fragmentos vigentes (LinkedHashMap: sin claves repetidas y en orden estable)
        Map<String, FragmentoConocimiento> vigentes = new LinkedHashMap<>();
        tiendas.buscarActivas(null, null).forEach(t -> agregar(vigentes, fragmentador.deTienda(t)));
        // Solo productos REALES: los de ejemplo no tienen precio ni existen en la tienda, Lía no debe recomendarlos
        productos.buscar(FiltroProductos.todos()).stream()
                .filter(p -> !p.esEjemplo())
                .forEach(p -> agregar(vigentes, fragmentador.deProducto(p)));
        guias.listar().forEach(g -> fragmentador.deGuia(g).forEach(f -> agregar(vigentes, f)));

        // 2. ¿Cuáles cambiaron? Se compara el hash del texto con el guardado
        String modelo = embeddings.modelo();
        Map<String, String> guardados = almacen.hashesPorClave(modelo);
        List<FragmentoConocimiento> porCalcular = new ArrayList<>();
        int nuevos = 0;
        int cambiados = 0;
        for (FragmentoConocimiento f : vigentes.values()) {
            String hashGuardado = guardados.get(f.clave());
            if (hashGuardado == null) {
                nuevos++;
                porCalcular.add(f);
            } else if (!hashGuardado.equals(f.hashTexto())) {
                cambiados++;
                porCalcular.add(f);
            }
        }

        // 3 y 4. Embeddings en lotes y guardado
        for (int inicio = 0; inicio < porCalcular.size(); inicio += TAMANO_LOTE) {
            List<FragmentoConocimiento> lote = porCalcular.subList(inicio, Math.min(inicio + TAMANO_LOTE, porCalcular.size()));
            List<Embedding> vectores = embeddings.deDocumentos(lote.stream().map(FragmentoConocimiento::texto).toList());
            if (vectores.size() != lote.size()) {
                throw new IllegalStateException("La IA devolvió " + vectores.size() + " embeddings para " + lote.size() + " textos");
            }
            for (int i = 0; i < lote.size(); i++) {
                almacen.guardar(lote.get(i), vectores.get(i), modelo);
            }
        }
        int eliminados = almacen.eliminarExcepto(vigentes.keySet());

        int sinCambios = vigentes.size() - nuevos - cambiados;
        return new ResultadoIndexacion(vigentes.size(), nuevos, cambiados, sinCambios, eliminados, modelo);
    }

    private static void agregar(Map<String, FragmentoConocimiento> vigentes, FragmentoConocimiento f) {
        vigentes.put(f.clave(), f);
    }
}
