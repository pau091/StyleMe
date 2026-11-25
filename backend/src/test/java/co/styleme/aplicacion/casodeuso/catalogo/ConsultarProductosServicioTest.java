package co.styleme.aplicacion.casodeuso.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.styleme.aplicacion.puerto.salida.persistencia.ProductoRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.TiendaRepositorioPuerto;
import co.styleme.dominio.excepcion.ProductoNoEncontradoException;
import co.styleme.dominio.excepcion.TiendaNoEncontradaException;
import co.styleme.dominio.modelo.catalogo.Categoria;
import co.styleme.dominio.modelo.catalogo.FiltroProductos;
import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.catalogo.TiendaReferencia;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Caso de uso probado con puertos FALSOS (Mockito): no hay base de datos ni Spring.
 * Así se comprueba la lógica de la aplicación sola. Esa es la ventaja de los puertos.
 */
class ConsultarProductosServicioTest {

    private ProductoRepositorioPuerto productos;
    private TiendaRepositorioPuerto tiendas;
    private ConsultarProductosServicio servicio;

    private final Producto serum = new Producto(28L, "p028", new TiendaReferencia(6L, "pandora-makeup", "Pandora"),
            new Categoria((short) 3, "skincare", "Skincare"), "Sérum", null, 85_001, null, null, null,
            null, null, false, true, null, null);

    @BeforeEach
    void preparar() {
        productos = mock(ProductoRepositorioPuerto.class);
        tiendas = mock(TiendaRepositorioPuerto.class);
        servicio = new ConsultarProductosServicio(productos, tiendas);
    }

    @Test
    @DisplayName("Obtener un producto que existe lo devuelve")
    void obtenerExistente() {
        when(productos.buscarPorCodigo("p028")).thenReturn(Optional.of(serum));
        assertThat(servicio.obtener("p028")).isEqualTo(serum);
    }

    @Test
    @DisplayName("Obtener un producto que no existe lanza ProductoNoEncontradoException")
    void obtenerInexistente() {
        when(productos.buscarPorCodigo("p999")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> servicio.obtener("p999"))
                .isInstanceOf(ProductoNoEncontradoException.class)
                .hasMessageContaining("p999");
    }

    @Test
    @DisplayName("Los productos de una tienda inexistente son un error, no una lista vacía")
    void tiendaInexistente() {
        when(tiendas.existe("no-existe")).thenReturn(false);
        assertThatThrownBy(() -> servicio.listarDeTienda("no-existe"))
                .isInstanceOf(TiendaNoEncontradaException.class);
        verify(productos, never()).buscar(any());
    }

    @Test
    @DisplayName("Los productos de una tienda se buscan con el filtro de esa tienda")
    void productosDeTienda() {
        when(tiendas.existe("pandora-makeup")).thenReturn(true);
        when(productos.buscar(new FiltroProductos(null, "pandora-makeup", null, null))).thenReturn(List.of(serum));
        assertThat(servicio.listarDeTienda("pandora-makeup")).containsExactly(serum);
    }

    @Test
    @DisplayName("Buscar sin filtro equivale a buscar todos")
    void buscarSinFiltro() {
        when(productos.buscar(FiltroProductos.todos())).thenReturn(List.of(serum));
        assertThat(servicio.buscar(null)).containsExactly(serum);
    }
}
