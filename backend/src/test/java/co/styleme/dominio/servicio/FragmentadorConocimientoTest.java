package co.styleme.dominio.servicio;

import static org.assertj.core.api.Assertions.assertThat;

import co.styleme.dominio.modelo.catalogo.Categoria;
import co.styleme.dominio.modelo.catalogo.Contacto;
import co.styleme.dominio.modelo.catalogo.Etiqueta;
import co.styleme.dominio.modelo.catalogo.GrupoEtiqueta;
import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.catalogo.Tienda;
import co.styleme.dominio.modelo.catalogo.TiendaReferencia;
import co.styleme.dominio.modelo.catalogo.Zona;
import co.styleme.dominio.modelo.ia.FragmentoConocimiento;
import co.styleme.dominio.modelo.ia.Guia;
import co.styleme.dominio.modelo.ia.TipoFuente;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FragmentadorConocimientoTest {

    private final FragmentadorConocimiento fragmentador = new FragmentadorConocimiento();
    private final Categoria skincare = new Categoria((short) 3, "skincare", "Skincare");

    private Producto serum(Integer precio) {
        return new Producto(28L, "p028", new TiendaReferencia(6L, "pandora-makeup", "Pandora Makeup"), skincare,
                "Sérum Centella", "SKIN1004", precio, "Calma la piel sensible.", null, null, null, null, false, true,
                List.of("Water", "Glycerin", "Centella Asiatica Extract"),
                List.of(new Etiqueta(GrupoEtiqueta.TIPO_PIEL, "todas"), new Etiqueta(GrupoEtiqueta.PASO, "serum"),
                        new Etiqueta(GrupoEtiqueta.LIBRE_DE, "fragancia")));
    }

    @Test
    @DisplayName("Un producto se convierte en un texto natural con sus características")
    void producto() {
        FragmentoConocimiento f = fragmentador.deProducto(serum(85_001));
        assertThat(f.tipo()).isEqualTo(TipoFuente.PRODUCTO);
        assertThat(f.productoId()).isEqualTo(28L);
        assertThat(f.texto()).contains("Producto: Sérum Centella", "Marca: SKIN1004", "Tienda: Pandora Makeup",
                "Precio: $85.001", "Tipo de piel: todas", "Paso de la rutina: serum", "Libre de: fragancia",
                "Ingredientes principales: Water, Glycerin, Centella Asiatica Extract");
    }

    @Test
    @DisplayName("Un producto sin precio lo dice en el texto (Lía no inventa precios)")
    void productoSinPrecio() {
        assertThat(fragmentador.deProducto(serum(null)).texto()).contains("Precio: por confirmar con la tienda");
    }

    @Test
    @DisplayName("Una tienda se convierte en un fragmento con zona, horario y contacto")
    void tienda() {
        Tienda bohor = new Tienda(2L, "bohor", "BOHOR", "Tienda de ropa y moda.", new Categoria((short) 1, "ropa", "Ropa y moda"),
                new Zona((short) 1, "Cabecera"), "Carrera 36 #53-32", null, "Lun–Sáb 10:00–19:00",
                new Contacto("573012951439", null, null, "bohor.com.co"), null, false, true);
        FragmentoConocimiento f = fragmentador.deTienda(bohor);
        assertThat(f.tipo()).isEqualTo(TipoFuente.TIENDA);
        assertThat(f.texto()).contains("Tienda: BOHOR", "Zona: Cabecera", "Horario: Lun–Sáb", "WhatsApp", "bohor.com.co");
    }

    @Test
    @DisplayName("Una guía se parte en un fragmento por sección")
    void guiaPorSecciones() {
        var guia = new Guia("prueba.md", """
                # Guía de prueba

                ## Primera sección
                Texto de la primera sección.

                ## Segunda sección
                Texto de la segunda sección.
                """);
        List<FragmentoConocimiento> fragmentos = fragmentador.deGuia(guia);
        assertThat(fragmentos).hasSize(2);
        assertThat(fragmentos.get(0).titulo()).isEqualTo("Guía de prueba · Primera sección");
        assertThat(fragmentos.get(0).texto()).startsWith("Guía de prueba. Primera sección: Texto de la primera");
        assertThat(fragmentos).allMatch(f -> f.tipo() == TipoFuente.GUIA);
    }

    @Test
    @DisplayName("Una sección muy larga se parte sin cortar frases y sin pasar el máximo")
    void seccionLarga() {
        String frase = "Esta es una frase de prueba que se repite muchas veces. ";
        var guia = new Guia("larga.md", "# Larga\n\n## Sección\n" + frase.repeat(80));
        List<FragmentoConocimiento> fragmentos = fragmentador.deGuia(guia);
        assertThat(fragmentos).hasSizeGreaterThan(1);
        assertThat(fragmentos).allMatch(f -> f.texto().length() <= FragmentoConocimiento.LARGO_MAXIMO);
        assertThat(fragmentos).allMatch(f -> f.texto().trim().endsWith("."));
        assertThat(fragmentos.get(0).titulo()).endsWith("(parte 1)");
    }

    @Test
    @DisplayName("El mismo texto produce el mismo hash; un texto distinto, otro hash")
    void hash() {
        String h1 = fragmentador.deProducto(serum(85_001)).hashTexto();
        String h2 = fragmentador.deProducto(serum(85_001)).hashTexto();
        String h3 = fragmentador.deProducto(serum(90_000)).hashTexto();
        assertThat(h1).hasSize(64).isEqualTo(h2).isNotEqualTo(h3);
    }
}
