package co.styleme.infraestructura.adaptador.salida.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import co.styleme.dominio.modelo.lia.ConversacionLia;
import co.styleme.dominio.modelo.lia.RespuestaLia;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** El resumen de las conversaciones contra la base REAL; @Transactional deshace todo al terminar. */
@SpringBootTest
@Transactional
class ConversacionPersistenciaIntegracionTest {

    @Autowired
    private ConversacionPersistenciaAdaptador conversaciones;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("Guarda una conversación nueva, la actualiza y la vuelve a leer por su sesión")
    void guardarYActualizar() {
        var ahora = LocalDateTime.now().withNano(0);
        var uno = new ConversacionLia.Intercambio("catalogo", "skincare", "positivo", RespuestaLia.Modo.IA, 0.71, 1200, 900, 80);
        ConversacionLia guardada = conversaciones.guardar(ConversacionLia.nueva("sesion-prueba-1", uno, ahora));
        assertThat(guardada.id()).isNotNull();

        var dos = new ConversacionLia.Intercambio("social", null, "positivo", RespuestaLia.Modo.PLAN_B, 0.0, 300, 0, 0);
        conversaciones.guardar(guardada.con(dos, ahora.plusMinutes(1)));

        ConversacionLia leida = conversaciones.buscarPorSesion("sesion-prueba-1").orElseThrow();
        assertThat(leida.cantidadMensajes()).isEqualTo(2);
        assertThat(leida.tema()).isEqualTo("catalogo");
        assertThat(leida.categoria()).isEqualTo("skincare");
        assertThat(leida.modo()).isEqualTo("MIXTO");
        assertThat(leida.tokensEntrada()).isEqualTo(900);
    }

    @Test
    @DisplayName("Suma las veces que Lía recomendó cada producto en la conversación")
    void sumarRecomendaciones() {
        var i = new ConversacionLia.Intercambio("catalogo", null, "neutral", RespuestaLia.Modo.IA, 0.6, 1000, 1, 1);
        long id = conversaciones.guardar(ConversacionLia.nueva("sesion-prueba-2", i, LocalDateTime.now())).id();

        conversaciones.sumarRecomendaciones(id, List.of("p028", "p035"));
        conversaciones.sumarRecomendaciones(id, List.of("p028", "p999")); // p999 no existe: se ignora

        Integer vecesSerum = jdbc.queryForObject("""
                SELECT r.veces FROM lia_recomendacion r JOIN producto p ON p.id = r.producto_id
                WHERE r.conversacion_id = ? AND p.codigo = 'p028'""", Integer.class, id);
        Integer filas = jdbc.queryForObject("SELECT count(*) FROM lia_recomendacion WHERE conversacion_id = ?", Integer.class, id);
        assertThat(vecesSerum).isEqualTo(2);
        assertThat(filas).isEqualTo(2);
    }

    @Test
    @DisplayName("Una sesión que no existe no devuelve nada")
    void sesionInexistente() {
        assertThat(conversaciones.buscarPorSesion("no-existe")).isEmpty();
    }
}
