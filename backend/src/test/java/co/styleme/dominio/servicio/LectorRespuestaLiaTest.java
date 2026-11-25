package co.styleme.dominio.servicio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LectorRespuestaLiaTest {

    @Test
    @DisplayName("Muestra el texto mientras llega y nunca muestra los datos después del separador")
    void separaTextoYDatos() {
        var lector = new LectorRespuestaLia();
        var mostrado = new StringBuilder();
        for (String pedazo : new String[] {"Hola, te recomiendo ", "el Suero Detox para piel grasa.", "\n@@@\n", "{\"emocion\":\"felicidad\"}"}) {
            mostrado.append(lector.agregar(pedazo));
        }
        mostrado.append(lector.terminar());

        assertThat(mostrado.toString().trim()).isEqualTo("Hola, te recomiendo el Suero Detox para piel grasa.");
        assertThat(lector.textoVisible()).doesNotContain("@@@", "emocion");
        assertThat(lector.jsonDatos()).contains("{\"emocion\":\"felicidad\"}");
    }

    @Test
    @DisplayName("Detecta el separador aunque llegue partido entre dos pedazos")
    void separadorPartido() {
        var lector = new LectorRespuestaLia();
        var mostrado = new StringBuilder();
        for (String pedazo : new String[] {"Texto de Lía.\n@", "@", "@\n{\"a\":1}"}) {
            mostrado.append(lector.agregar(pedazo));
        }
        assertThat(mostrado.toString()).doesNotContain("@");
        assertThat(lector.jsonDatos()).contains("{\"a\":1}");
    }

    @Test
    @DisplayName("Si la IA olvida el separador, todo es texto y no hay datos")
    void sinSeparador() {
        var lector = new LectorRespuestaLia();
        String mostrado = lector.agregar("Solo texto, sin datos.") + lector.terminar();
        assertThat(mostrado).isEqualTo("Solo texto, sin datos.");
        assertThat(lector.jsonDatos()).isEmpty();
    }

    @Test
    @DisplayName("Quita los ```json que a veces agrega la IA")
    void quitaBloqueDeCodigo() {
        var lector = new LectorRespuestaLia();
        lector.agregar("Texto.\n@@@\n```json\n{\"emocion\":\"duda\"}\n```");
        assertThat(lector.jsonDatos()).contains("{\"emocion\":\"duda\"}");
    }
}
