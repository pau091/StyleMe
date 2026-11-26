package co.styleme.infraestructura.adaptador.salida.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import co.styleme.dominio.modelo.catalogo.FiltroProductos;
import co.styleme.dominio.modelo.catalogo.GrupoEtiqueta;
import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.catalogo.Tienda;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de integración: los adaptadores JPA contra la base REAL (Docker con los datos de
 * basedatos/03-datos-iniciales.sql). Necesita la base encendida: docker compose up -d
 */
@SpringBootTest
class PersistenciaCatalogoIntegracionTest {

    @Autowired
    private TiendaPersistenciaAdaptador tiendas;

    @Autowired
    private ProductoPersistenciaAdaptador productos;

    @Autowired
    private CategoriaPersistenciaAdaptador categorias;

    @Test
    @DisplayName("Lee las 4 categorías y las 10 tiendas del catálogo")
    void catalogoCompleto() {
        assertThat(categorias.listarTodas()).extracting("slug")
                .containsExactly("ropa", "maquillaje", "skincare", "capilar");
        assertThat(tiendas.buscarActivas(null, null)).hasSize(10);
    }

    @Test
    @DisplayName("Filtra tiendas por categoría y por zona")
    void filtrosDeTiendas() {
        assertThat(tiendas.buscarActivas("ropa", null)).hasSize(5);
        assertThat(tiendas.buscarActivas(null, "cabecera")).allMatch(t -> t.zona().nombre().equals("Cabecera"));
    }

    @Test
    @DisplayName("Convierte bien una tienda: contacto, coordenadas y regla del mapa")
    void tiendaCompleta() {
        Tienda bohor = tiendas.buscarPorSlug("bohor").orElseThrow();
        assertThat(bohor.contacto().enlaceWhatsapp()).isEqualTo("https://wa.me/573012951439");
        assertThat(bohor.apareceEnMapa()).isTrue();
        // Desde la rama feature/catalogo-completo las 10 tiendas tienen coordenadas (D-069)
        assertThat(tiendas.buscarPorSlug("pandora-makeup").orElseThrow().apareceEnMapa()).isTrue();
        assertThat(tiendas.buscarActivas(null, null)).hasSize(10).allMatch(Tienda::apareceEnMapa);
    }

    @Test
    @DisplayName("Un producto trae sus ingredientes EN ORDEN y sus etiquetas")
    void productoConIngredientesYEtiquetas() {
        Producto serum = productos.buscarPorCodigo("p028").orElseThrow();
        assertThat(serum.tienda().slug()).isEqualTo("pandora-makeup");
        assertThat(serum.ingredientes()).startsWith("Water", "Glycerin");
        assertThat(serum.sirvePara(GrupoEtiqueta.TIPO_PIEL, "grasa")).isTrue();
    }

    @Test
    @DisplayName("Buscar 'serum' (sin tilde) encuentra los productos que dicen 'Sérum'")
    void busquedaSinTildes() {
        List<Producto> resultado = productos.buscar(new FiltroProductos(null, null, null, "serum"));
        assertThat(resultado).isNotEmpty().anyMatch(p -> p.nombre().contains("Sérum"));
    }

    @Test
    @DisplayName("El filtro de presupuesto no trae productos más caros ni sin precio")
    void presupuesto() {
        List<Producto> baratos = productos.buscar(new FiltroProductos("skincare", null, 40_000, null));
        assertThat(baratos).isNotEmpty().allMatch(p -> p.cuestaMaximo(40_000));
    }

    @Test
    @DisplayName("Total de productos activos del catálogo (49 del 03, menos p022 que borra el 06)")
    void totalProductos() {
        assertThat(productos.buscar(FiltroProductos.todos())).hasSize(48);
    }
}
