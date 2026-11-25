package co.styleme.dominio.modelo.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Reglas de la tienda, sin Spring ni base de datos. */
class TiendaTest {

    private static final Categoria ROPA = new Categoria((short) 1, "ropa", "Ropa y moda");

    private static Tienda tienda(Coordenadas coordenadas, boolean activa, Contacto contacto) {
        return new Tienda(1L, "bohor", "BOHOR", null, ROPA, null, null, coordenadas, null, contacto, null, false, activa);
    }

    @Test
    @DisplayName("Una tienda sin coordenadas existe, pero no aparece en el mapa")
    void sinCoordenadasNoApareceEnMapa() {
        Tienda tienda = tienda(null, true, null);
        assertThat(tienda.apareceEnMapa()).isFalse();
        assertThat(tienda.sirveParaSalidaDeCompras()).isFalse();
    }

    @Test
    @DisplayName("Una tienda activa con coordenadas sirve para la salida de compras")
    void conCoordenadasSirveParaSalida() {
        Tienda tienda = tienda(new Coordenadas(new BigDecimal("7.1118"), new BigDecimal("-73.1080")), true, null);
        assertThat(tienda.apareceEnMapa()).isTrue();
        assertThat(tienda.sirveParaSalidaDeCompras()).isTrue();
    }

    @Test
    @DisplayName("Una tienda inactiva no entra en la salida de compras aunque tenga coordenadas")
    void inactivaNoSirveParaSalida() {
        Tienda tienda = tienda(new Coordenadas(new BigDecimal("7.1"), new BigDecimal("-73.1")), false, null);
        assertThat(tienda.sirveParaSalidaDeCompras()).isFalse();
    }

    @Test
    @DisplayName("El nombre es obligatorio")
    void nombreObligatorio() {
        assertThatThrownBy(() -> new Tienda(1L, "x", "  ", null, ROPA, null, null, null, null, null, null, false, true))
                .isInstanceOf(DatoInvalidoException.class)
                .hasMessageContaining("nombre");
    }

    @Test
    @DisplayName("Si no se da contacto, la tienda queda con un contacto vacío (nunca null)")
    void contactoNuncaNull() {
        Tienda tienda = tienda(null, true, null);
        assertThat(tienda.contacto()).isNotNull();
        assertThat(tienda.contacto().esContactable()).isFalse();
    }

    @Test
    @DisplayName("El enlace de WhatsApp se arma con wa.me")
    void enlaceWhatsapp() {
        Tienda tienda = tienda(null, true, new Contacto("573012951439", null, null, null));
        assertThat(tienda.contacto().enlaceWhatsapp()).isEqualTo("https://wa.me/573012951439");
    }

    @Test
    @DisplayName("El WhatsApp solo admite números")
    void whatsappSoloNumeros() {
        assertThatThrownBy(() -> new Contacto("+57 300 123", null, null, null))
                .isInstanceOf(DatoInvalidoException.class);
    }

    @Test
    @DisplayName("Las coordenadas rechazan una latitud imposible")
    void latitudImposible() {
        assertThatThrownBy(() -> new Coordenadas(new BigDecimal("200"), new BigDecimal("-73")))
                .isInstanceOf(DatoInvalidoException.class);
    }

    @Test
    @DisplayName("Si falta latitud o longitud, no hay coordenadas (null)")
    void coordenadasIncompletas() {
        assertThat(Coordenadas.siEstanCompletas(new BigDecimal("7.1"), null)).isNull();
    }
}
