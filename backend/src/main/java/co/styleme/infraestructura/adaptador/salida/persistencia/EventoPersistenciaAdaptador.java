package co.styleme.infraestructura.adaptador.salida.persistencia;

import co.styleme.aplicacion.puerto.salida.persistencia.EventoRepositorioPuerto;
import co.styleme.dominio.modelo.analitica.Evento;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * Adaptador de salida: guarda los eventos en lote (batchUpdate: una sola ida a la base para todo el lote).
 * Las tiendas, productos y categorías se buscan por su slug/código; si no existen, quedan en NULL.
 */
@Component
@Transactional
public class EventoPersistenciaAdaptador implements EventoRepositorioPuerto {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final JdbcTemplate jdbc;

    public EventoPersistenciaAdaptador(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int guardar(List<Evento> eventos) {
        jdbc.batchUpdate("""
                INSERT INTO evento (tipo, sesion_id, tienda_id, producto_id, categoria_id, texto_busqueda,
                                    cantidad_resultados, dispositivo, detalle)
                VALUES (?, ?, (SELECT id FROM tienda WHERE slug = ?), (SELECT id FROM producto WHERE codigo = ?),
                        (SELECT id FROM categoria WHERE slug = ?), ?, ?, ?, CAST(? AS jsonb))
                """,
                eventos.stream().map(e -> new Object[] {
                        e.tipo().name(), e.sesionId(), e.tienda(), e.producto(), e.categoria(), e.textoBusqueda(),
                        e.cantidadResultados(), e.dispositivo(), e.detalle().isEmpty() ? null : JSON.writeValueAsString(e.detalle())
                }).toList());
        return eventos.size();
    }
}
