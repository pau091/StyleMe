package co.styleme.dominio.modelo.probador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import co.styleme.dominio.modelo.catalogo.Categoria;
import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.catalogo.TiendaReferencia;
import co.styleme.dominio.modelo.catalogo.TipoPrenda;
import co.styleme.dominio.servicio.InstruccionesProbador;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Reglas del probador con IA: imágenes válidas, poses e instrucciones para el modelo. */
class ProbadorDominioTest {

    static final Producto BLUSA = new Producto(1L, "p001", new TiendaReferencia(1L, "bohor", "BOHOR"),
            new Categoria((short) 1, "ropa", "Ropa y moda"), "Blusa Tango Negro", null, 185000, null,
            "https://cdn.shopify.com/blusa.jpg", null, TipoPrenda.SUPERIOR, null, false, true, List.of(), List.of());

    @Test
    @DisplayName("Una imagen debe ser JPG, PNG o WEBP, no vacía y de máximo 8 MB")
    void imagenValida() {
        assertThat(new Imagen(new byte[] {1}, "image/jpg").tipoMime()).isEqualTo("image/jpeg");
        assertThat(new Imagen(new byte[] {1}, "image/png; charset=binary").extension()).isEqualTo("png");
        assertThatThrownBy(() -> new Imagen(new byte[0], "image/png")).isInstanceOf(DatoInvalidoException.class);
        assertThatThrownBy(() -> new Imagen(new byte[] {1}, "application/pdf")).isInstanceOf(DatoInvalidoException.class);
        assertThatThrownBy(() -> new Imagen(new byte[Imagen.TAMANO_MAXIMO + 1], "image/png")).isInstanceOf(DatoInvalidoException.class);
        assertThat(new Imagen(new byte[] {1, 2}, "image/webp").toString()).doesNotContain("[1, 2]");
    }

    @Test
    @DisplayName("Hay 4 poses y se leen sin importar mayúsculas")
    void poses() {
        assertThat(Pose.values()).hasSize(4);
        assertThat(Pose.desde("tres_cuartos")).isEqualTo(Pose.TRES_CUARTOS);
        assertThatThrownBy(() -> Pose.desde("acostada")).isInstanceOf(DatoInvalidoException.class);
    }

    @Test
    @DisplayName("Las instrucciones nombran la prenda, la tienda y la pose, y protegen el cuerpo de la persona")
    void instrucciones() {
        String texto = new InstruccionesProbador().para(BLUSA, Pose.PERFIL, false);
        assertThat(texto).contains("Blusa Tango Negro", "BOHOR", "prenda superior", Pose.PERFIL.descripcion(),
                "forma del cuerpo", "No inventes otra prenda", "Sin texto");
        assertThat(texto).doesNotContain("TERCERA imagen");
        assertThat(new InstruccionesProbador().para(BLUSA, Pose.FRENTE, true)).contains("TERCERA imagen");
    }
}
