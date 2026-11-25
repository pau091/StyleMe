package co.styleme.dominio.modelo.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Reglas del producto, sin Spring ni base de datos. */
class ProductoTest {

    private static final TiendaReferencia PANDORA = new TiendaReferencia(6L, "pandora-makeup", "Pandora Makeup");
    private static final Categoria SKINCARE = new Categoria((short) 3, "skincare", "Skincare");

    private static Producto serum(Integer precio, List<Etiqueta> etiquetas) {
        return new Producto(28L, "p028", PANDORA, SKINCARE, "Sérum Centella", "SKIN1004", precio, null, null, null,
                null, null, false, true, List.of("Water", "Niacinamide"), etiquetas);
    }

    @Test
    @DisplayName("El precio no puede ser 0 ni negativo")
    void precioPositivo() {
        assertThatThrownBy(() -> serum(0, List.of())).isInstanceOf(DatoInvalidoException.class);
        assertThatThrownBy(() -> serum(-5, List.of())).isInstanceOf(DatoInvalidoException.class);
    }

    @Test
    @DisplayName("Un producto sin precio está permitido, pero no cumple ningún presupuesto")
    void sinPrecioNoCumplePresupuesto() {
        Producto producto = serum(null, List.of());
        assertThat(producto.tienePrecio()).isFalse();
        assertThat(producto.cuestaMaximo(1_000_000)).isFalse();
    }

    @Test
    @DisplayName("Respeta el presupuesto")
    void presupuesto() {
        Producto producto = serum(85_001, List.of());
        assertThat(producto.cuestaMaximo(90_000)).isTrue();
        assertThat(producto.cuestaMaximo(50_000)).isFalse();
    }

    @Test
    @DisplayName("Un producto para 'todas' las pieles sirve para piel grasa")
    void etiquetaUniversal() {
        Producto producto = serum(85_001, List.of(new Etiqueta(GrupoEtiqueta.TIPO_PIEL, "todas")));
        assertThat(producto.sirvePara(GrupoEtiqueta.TIPO_PIEL, "grasa")).isTrue();
    }

    @Test
    @DisplayName("Un producto para piel seca no sirve para piel grasa")
    void etiquetaEspecifica() {
        Producto producto = serum(85_001, List.of(new Etiqueta(GrupoEtiqueta.TIPO_PIEL, "seca")));
        assertThat(producto.sirvePara(GrupoEtiqueta.TIPO_PIEL, "grasa")).isFalse();
        assertThat(producto.sirvePara(GrupoEtiqueta.TIPO_PIEL, "SECA")).isTrue();
    }

    @Test
    @DisplayName("Busca ingredientes sin importar mayúsculas")
    void ingredientes() {
        assertThat(serum(1, List.of()).contieneIngrediente("niacinamide")).isTrue();
        assertThat(serum(1, List.of()).contieneIngrediente("retinol")).isFalse();
    }

    @Test
    @DisplayName("Las listas del producto no se pueden modificar desde afuera")
    void listasInmutables() {
        List<String> ingredientes = new ArrayList<>(List.of("Water"));
        Producto producto = new Producto(1L, "p1", PANDORA, SKINCARE, "X", null, 1, null, null, null,
                null, null, false, true, ingredientes, null);
        ingredientes.add("Retinol");
        assertThat(producto.ingredientes()).containsExactly("Water");
        assertThatThrownBy(() -> producto.ingredientes().add("Otro")).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Solo las prendas con foto sin fondo están en el probador")
    void probador() {
        Categoria ropa = new Categoria((short) 1, "ropa", "Ropa y moda");
        Producto blusa = new Producto(1L, "p1", PANDORA, ropa, "Blusa", null, 1, null, null, null,
                TipoPrenda.SUPERIOR, "assets/img/productos/p1.png", false, true, null, null);
        Producto sinFoto = new Producto(2L, "p2", PANDORA, ropa, "Falda", null, 1, null, null, null,
                TipoPrenda.INFERIOR, null, false, true, null, null);
        assertThat(blusa.disponibleEnProbador()).isTrue();
        assertThat(sinFoto.disponibleEnProbador()).isFalse();
    }

    @Test
    @DisplayName("El filtro rechaza un precio máximo negativo")
    void filtroInvalido() {
        assertThatThrownBy(() -> new FiltroProductos(null, null, -1, null)).isInstanceOf(DatoInvalidoException.class);
    }
}
