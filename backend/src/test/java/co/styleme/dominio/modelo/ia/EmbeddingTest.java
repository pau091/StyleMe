package co.styleme.dominio.modelo.ia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmbeddingTest {

    private static float[] vector(int posicion, float valor) {
        float[] v = new float[Embedding.DIMENSIONES];
        v[posicion] = valor;
        return v;
    }

    @Test
    @DisplayName("Se normaliza: dos vectores en la misma dirección tienen similitud 1")
    void normaliza() {
        Embedding a = Embedding.de(vector(0, 5f));
        Embedding b = Embedding.de(vector(0, 0.3f));
        assertThat(a.similitud(b)).isCloseTo(1.0, within(1e-6));
    }

    @Test
    @DisplayName("Vectores perpendiculares (significados sin relación) tienen similitud 0")
    void perpendiculares() {
        assertThat(Embedding.de(vector(0, 1f)).similitud(Embedding.de(vector(1, 1f)))).isCloseTo(0.0, within(1e-6));
    }

    @Test
    @DisplayName("Rechaza vectores de otro tamaño o de ceros")
    void invalidos() {
        assertThatThrownBy(() -> Embedding.de(new float[3])).isInstanceOf(DatoInvalidoException.class);
        assertThatThrownBy(() -> Embedding.de(new float[Embedding.DIMENSIONES])).isInstanceOf(DatoInvalidoException.class);
    }

    @Test
    @DisplayName("Nadie puede modificar el embedding desde afuera")
    void inmutable() {
        Embedding e = Embedding.de(vector(0, 1f));
        e.valores()[0] = 99f;
        assertThat(e.valores()[0]).isEqualTo(1f);
    }

    @Test
    @DisplayName("Un fragmento de producto sin productoId es incoherente")
    void fragmentoIncoherente() {
        assertThatThrownBy(() -> new FragmentoConocimiento(TipoFuente.PRODUCTO, null, null, "t", "x", null))
                .isInstanceOf(DatoInvalidoException.class);
    }
}
