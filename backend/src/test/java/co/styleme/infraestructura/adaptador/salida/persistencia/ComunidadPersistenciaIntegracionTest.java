package co.styleme.infraestructura.adaptador.salida.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import co.styleme.dominio.modelo.analitica.Evento;
import co.styleme.dominio.modelo.analitica.Indicador;
import co.styleme.dominio.modelo.comunidad.Objetivo;
import co.styleme.dominio.modelo.comunidad.Resena;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** Reseñas, favoritos, eventos e indicadores contra la base REAL; @Transactional deshace todo al terminar. */
@SpringBootTest
@Transactional
class ComunidadPersistenciaIntegracionTest {

    private static final String SESION = "prueba-integracion-comunidad";

    @Autowired
    private ResenaPersistenciaAdaptador resenas;

    @Autowired
    private FavoritoPersistenciaAdaptador favoritos;

    @Autowired
    private EventoPersistenciaAdaptador eventos;

    @Autowired
    private IndicadorPersistenciaAdaptador indicadores;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("Reseña: se guarda PENDIENTE, no se ve hasta publicarla y entonces cuenta en el promedio")
    void cicloDeUnaResena() {
        var objetivo = Objetivo.producto("p049");
        int antes = resenas.resumen(objetivo).cantidad();
        Resena guardada = resenas.guardar(Resena.nueva(SESION, "Ana", objetivo, 5, "Fija muy bien"));

        assertThat(guardada.id()).isNotNull();
        assertThat(resenas.existe(SESION, objetivo)).isTrue();
        assertThat(resenas.pendientes(500)).extracting(Resena::id).contains(guardada.id());
        assertThat(resenas.publicadas(objetivo, 50)).extracting(Resena::id).doesNotContain(guardada.id());

        resenas.cambiarEstado(guardada.id(), Resena.Estado.PUBLICADA);

        assertThat(resenas.publicadas(objetivo, 50)).extracting(Resena::autor).contains("Ana");
        assertThat(resenas.recientes(12)).as("la más nueva sale en la portada").extracting(Resena::id).contains(guardada.id());
        assertThat(resenas.resumen(objetivo).cantidad()).isEqualTo(antes + 1);
    }

    @Test
    @DisplayName("Favoritos: agregar dos veces no duplica; quitar lo borra")
    void favoritos() {
        favoritos.agregar(SESION, Objetivo.tienda("bohor"));
        favoritos.agregar(SESION, Objetivo.tienda("bohor"));
        favoritos.agregar(SESION, Objetivo.producto("p035"));
        assertThat(favoritos.listar(SESION)).hasSize(2).extracting("nombre").contains("BOHOR");

        favoritos.quitar(SESION, Objetivo.tienda("bohor"));
        assertThat(favoritos.listar(SESION)).hasSize(1);
    }

    @Test
    @DisplayName("Eventos: se guardan en lote; una tienda que no existe queda en NULL")
    void eventos() {
        int guardados = eventos.guardar(List.of(
                new Evento(Evento.Tipo.VER_TIENDA, SESION, "bohor", null, null, null, null, "celular", null),
                new Evento(Evento.Tipo.VER_TIENDA, SESION, "tienda-inventada", null, null, null, null, null, null),
                new Evento(Evento.Tipo.BUSQUEDA, SESION, null, null, null, "zapatos", 0, "computador", Map.of("origen", "catalogo"))));

        assertThat(guardados).isEqualTo(3);
        Map<String, Object> fila = jdbc.queryForMap("""
                SELECT count(*) AS total, count(tienda_id) AS con_tienda, count(*) FILTER (WHERE detalle->>'origen' = 'catalogo') AS con_detalle
                FROM evento WHERE sesion_id = ?""", SESION);
        assertThat(fila).containsEntry("total", 3L).containsEntry("con_tienda", 1L).containsEntry("con_detalle", 1L);
    }

    @Test
    @DisplayName("Los 13 indicadores se calculan sin errores y los principales tienen datos")
    void indicadores() {
        for (Indicador.Codigo codigo : Indicador.Codigo.values()) {
            assertThat(indicadores.consultar(codigo).codigo()).isEqualTo(codigo);
        }
        assertThat(indicadores.consultar(Indicador.Codigo.CONVERSION_TIENDAS).filas()).hasSize(10);
        assertThat(indicadores.consultar(Indicador.Codigo.CATEGORIAS).filas()).hasSize(4);
        assertThat(indicadores.consultar(Indicador.Codigo.CALIDAD_DATOS).filas()).hasSize(6);
    }
}
