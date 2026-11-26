package co.styleme.aplicacion.casodeuso.probador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.styleme.aplicacion.puerto.salida.ia.DescargadorImagenPuerto;
import co.styleme.aplicacion.puerto.salida.ia.GeneradorImagenPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ProductoRepositorioPuerto;
import co.styleme.dominio.excepcion.DatoInvalidoException;
import co.styleme.dominio.excepcion.ProductoNoEncontradoException;
import co.styleme.dominio.excepcion.ServicioIaNoDisponibleException;
import co.styleme.dominio.modelo.catalogo.Categoria;
import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.catalogo.TiendaReferencia;
import co.styleme.dominio.modelo.catalogo.TipoPrenda;
import co.styleme.dominio.modelo.probador.Imagen;
import co.styleme.dominio.modelo.probador.Pose;
import co.styleme.dominio.servicio.InstruccionesProbador;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** El caso de uso del probador con IA, con puertos falsos (sin llamar a ningún proveedor). */
class ProbarPrendaServicioTest {

    private static final Imagen PERSONA = new Imagen(new byte[] {1}, "image/jpeg");
    private static final Imagen PRENDA = new Imagen(new byte[] {2}, "image/jpeg");
    private static final Imagen FONDO = new Imagen(new byte[] {3}, "image/webp");
    private static final Imagen RESULTADO = new Imagen(new byte[] {9}, "image/png");
    private static final Producto LABIAL = new Producto(16L, "p016", new TiendaReferencia(6L, "pandora-makeup", "Pandora"),
            new Categoria((short) 2, "maquillaje", "Maquillaje"), "Labial", null, 50000, null, "https://x/labial.jpg", null,
            null, null, false, true, List.of(), List.of());

    private static final Producto BLUSA = new Producto(1L, "p001", new TiendaReferencia(1L, "bohor", "BOHOR"),
            new Categoria((short) 1, "ropa", "Ropa y moda"), "Blusa Tango Negro", null, 185000, null,
            "https://cdn.shopify.com/blusa.jpg", null, TipoPrenda.SUPERIOR, null, false, true, List.of(), List.of());

    private ProductoRepositorioPuerto productos;
    private GeneradorImagenPuerto generador;
    private DescargadorImagenPuerto descargador;
    private ProbarPrendaServicio servicio;

    @BeforeEach
    void preparar() {
        productos = mock(ProductoRepositorioPuerto.class);
        generador = mock(GeneradorImagenPuerto.class);
        descargador = mock(DescargadorImagenPuerto.class);
        servicio = new ProbarPrendaServicio(productos, generador, descargador, new InstruccionesProbador());
        when(generador.disponible()).thenReturn(true);
        when(generador.proveedor()).thenReturn("OpenAI");
        when(productos.buscarPorCodigo("p001")).thenReturn(Optional.of(BLUSA));
        when(productos.buscarPorCodigo("p016")).thenReturn(Optional.of(LABIAL));
        when(descargador.descargar(anyString())).thenReturn(PRENDA);
        when(generador.generar(anyString(), anyList())).thenReturn(RESULTADO);
    }

    @Test
    @DisplayName("Manda al modelo la persona, la prenda y el fondo, en ese orden, con las instrucciones de la pose")
    @SuppressWarnings("unchecked")
    void genera() {
        assertThat(servicio.probar(PERSONA, "p001", Pose.CAMINANDO, FONDO)).isEqualTo(RESULTADO);
        ArgumentCaptor<List<Imagen>> referencias = ArgumentCaptor.forClass(List.class);
        verify(generador).generar(contains(Pose.CAMINANDO.descripcion()), referencias.capture());
        assertThat(referencias.getValue()).containsExactly(PERSONA, PRENDA, FONDO);
        verify(descargador).descargar("https://cdn.shopify.com/blusa.jpg");
    }

    @Test
    @DisplayName("Sin fondo, manda solo la persona y la prenda")
    @SuppressWarnings("unchecked")
    void sinFondo() {
        servicio.probar(PERSONA, "p001", Pose.FRENTE, null);
        ArgumentCaptor<List<Imagen>> referencias = ArgumentCaptor.forClass(List.class);
        verify(generador).generar(anyString(), referencias.capture());
        assertThat(referencias.getValue()).containsExactly(PERSONA, PRENDA);
    }

    @Test
    @DisplayName("Si no hay proveedor, es un 503 y no se descarga ni se envía nada")
    void apagado() {
        when(generador.disponible()).thenReturn(false);
        assertThat(servicio.estado().disponible()).isFalse();
        assertThat(servicio.estado().proveedor()).isNull();
        assertThatThrownBy(() -> servicio.probar(PERSONA, "p001", Pose.FRENTE, null)).isInstanceOf(ServicioIaNoDisponibleException.class);
        verify(descargador, never()).descargar(any());
        verify(generador, never()).generar(any(), any());
    }

    @Test
    @DisplayName("Solo prendas de ropa que existan")
    void soloRopa() {
        assertThatThrownBy(() -> servicio.probar(PERSONA, "p016", Pose.FRENTE, null)).isInstanceOf(DatoInvalidoException.class);
        assertThatThrownBy(() -> servicio.probar(PERSONA, "p999", Pose.FRENTE, null)).isInstanceOf(ProductoNoEncontradoException.class);
        verify(generador, never()).generar(any(), any());
    }

    @Test
    @DisplayName("El estado ofrece las 4 poses y el nombre del proveedor")
    void estado() {
        assertThat(servicio.estado().poses()).containsExactly(Pose.values());
        assertThat(servicio.estado().proveedor()).isEqualTo("OpenAI");
    }
}
