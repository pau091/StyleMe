package co.styleme.aplicacion.casodeuso.ia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.styleme.aplicacion.puerto.salida.ia.GuiaRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ProductoRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.TiendaRepositorioPuerto;
import co.styleme.dominio.modelo.catalogo.Categoria;
import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.catalogo.Tienda;
import co.styleme.dominio.modelo.catalogo.TiendaReferencia;
import co.styleme.dominio.modelo.ia.Guia;
import co.styleme.dominio.modelo.ia.ResultadoIndexacion;
import co.styleme.dominio.servicio.FragmentadorConocimiento;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IndexarConocimientoServicioTest {

    private final Categoria skincare = new Categoria((short) 3, "skincare", "Skincare");
    private final Tienda pandora = new Tienda(6L, "pandora-makeup", "Pandora Makeup", null, skincare,
            null, null, null, null, null, null, false, true);

    private TiendaRepositorioPuerto tiendas;
    private ProductoRepositorioPuerto productos;
    private GuiaRepositorioPuerto guias;
    private Falsos.EmbeddingFalso embeddings;
    private Falsos.AlmacenFalso almacen;
    private IndexarConocimientoServicio servicio;

    private Producto producto(long id, String nombre, int precio) {
        return new Producto(id, "p" + id, pandora.comoReferencia(), skincare, nombre, null, precio,
                null, null, null, null, null, false, true, null, null);
    }

    @BeforeEach
    void preparar() {
        tiendas = mock(TiendaRepositorioPuerto.class);
        productos = mock(ProductoRepositorioPuerto.class);
        guias = mock(GuiaRepositorioPuerto.class);
        embeddings = new Falsos.EmbeddingFalso();
        almacen = new Falsos.AlmacenFalso();
        servicio = new IndexarConocimientoServicio(tiendas, productos, guias, embeddings, almacen, new FragmentadorConocimiento());

        when(tiendas.buscarActivas(null, null)).thenReturn(List.of(pandora));
        when(productos.buscar(any())).thenReturn(List.of(producto(1, "Sérum", 50_000), producto(2, "Tónico", 30_000)));
        when(guias.listar()).thenReturn(List.of(new Guia("g.md", "# Guía\n\n## Uno\nTexto uno.\n\n## Dos\nTexto dos.")));
    }

    @Test
    @DisplayName("La primera vez calcula el embedding de todo: 1 tienda + 2 productos + 2 secciones")
    void primeraIndexacion() {
        ResultadoIndexacion r = servicio.indexar();
        assertThat(r.total()).isEqualTo(5);
        assertThat(r.nuevos()).isEqualTo(5);
        assertThat(r.embeddingsCalculados()).isEqualTo(5);
        assertThat(almacen.contar()).isEqualTo(5);
    }

    @Test
    @DisplayName("La segunda vez, si nada cambió, NO pide ningún embedding (ahorra tokens)")
    void segundaIndexacionSinCambios() {
        servicio.indexar();
        int antes = embeddings.textosCalculados;

        ResultadoIndexacion r = servicio.indexar();

        assertThat(r.sinCambios()).isEqualTo(5);
        assertThat(r.embeddingsCalculados()).isZero();
        assertThat(embeddings.textosCalculados).isEqualTo(antes);
    }

    @Test
    @DisplayName("Si cambia el precio de un producto, solo se recalcula ese producto")
    void soloRecalculaLoQueCambio() {
        servicio.indexar();
        when(productos.buscar(any())).thenReturn(List.of(producto(1, "Sérum", 45_000), producto(2, "Tónico", 30_000)));

        ResultadoIndexacion r = servicio.indexar();

        assertThat(r.cambiados()).isEqualTo(1);
        assertThat(r.sinCambios()).isEqualTo(4);
    }

    @Test
    @DisplayName("Si un producto deja de existir, su fragmento se borra del índice")
    void borraLoQueYaNoExiste() {
        servicio.indexar();
        when(productos.buscar(any())).thenReturn(List.of(producto(1, "Sérum", 50_000)));

        ResultadoIndexacion r = servicio.indexar();

        assertThat(r.eliminados()).isEqualTo(1);
        assertThat(almacen.contar()).isEqualTo(4);
    }

    @Test
    @DisplayName("Los embeddings se piden en lotes, no uno por uno")
    void porLotes() {
        servicio.indexar();
        assertThat(embeddings.llamadas).isEqualTo(1);
    }
}
