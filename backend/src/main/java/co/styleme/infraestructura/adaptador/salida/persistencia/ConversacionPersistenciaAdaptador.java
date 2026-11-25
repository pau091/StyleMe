package co.styleme.infraestructura.adaptador.salida.persistencia;

import co.styleme.aplicacion.puerto.salida.persistencia.ConversacionRepositorioPuerto;
import co.styleme.dominio.modelo.lia.ConversacionLia;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de salida: guarda el RESUMEN de las conversaciones con Lía (conversacion_lia)
 * y cuántas veces recomendó cada producto (lia_recomendacion). Nunca guarda el texto de la persona.
 * Usa SQL directo porque son actualizaciones simples de contadores.
 */
@Component
@Transactional
public class ConversacionPersistenciaAdaptador implements ConversacionRepositorioPuerto {

    private final JdbcTemplate jdbc;

    public ConversacionPersistenciaAdaptador(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConversacionLia> buscarPorSesion(String sesionId) {
        List<ConversacionLia> filas = jdbc.query("""
                SELECT c.id, c.sesion_id, c.tema, cat.slug AS categoria, c.sentimiento_final, c.cantidad_mensajes, c.modo,
                       c.similitud_promedio, c.latencia_promedio_ms, c.tokens_entrada, c.tokens_salida, c.inicio, c.fin
                FROM conversacion_lia c
                LEFT JOIN categoria cat ON cat.id = c.categoria_id
                WHERE c.sesion_id = ?
                ORDER BY c.inicio DESC
                LIMIT 1
                """,
                (f, n) -> new ConversacionLia(
                        f.getLong("id"), f.getString("sesion_id"), f.getString("tema"), f.getString("categoria"),
                        f.getString("sentimiento_final"), f.getInt("cantidad_mensajes"), f.getString("modo"),
                        f.getBigDecimal("similitud_promedio") == null ? 0 : f.getBigDecimal("similitud_promedio").doubleValue(),
                        f.getInt("latencia_promedio_ms"), f.getInt("tokens_entrada"), f.getInt("tokens_salida"),
                        f.getTimestamp("inicio").toLocalDateTime(),
                        f.getTimestamp("fin") == null ? null : f.getTimestamp("fin").toLocalDateTime()),
                sesionId);
        return filas.stream().findFirst();
    }

    @Override
    public ConversacionLia guardar(ConversacionLia c) {
        BigDecimal similitud = BigDecimal.valueOf(c.similitudPromedio()).setScale(3, RoundingMode.HALF_UP);
        if (c.id() == null) {
            Long id = jdbc.queryForObject("""
                    INSERT INTO conversacion_lia (sesion_id, tema, categoria_id, sentimiento_final, cantidad_mensajes, modo,
                                                  similitud_promedio, latencia_promedio_ms, tokens_entrada, tokens_salida, inicio, fin)
                    VALUES (?, ?, (SELECT id FROM categoria WHERE slug = ?), ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    RETURNING id
                    """, Long.class,
                    c.sesionId(), c.tema(), c.categoria(), c.sentimientoFinal(), c.cantidadMensajes(), c.modo(), similitud,
                    c.latenciaPromedioMs(), c.tokensEntrada(), c.tokensSalida(), Timestamp.valueOf(c.inicio()), Timestamp.valueOf(c.fin()));
            return new ConversacionLia(id, c.sesionId(), c.tema(), c.categoria(), c.sentimientoFinal(), c.cantidadMensajes(),
                    c.modo(), c.similitudPromedio(), c.latenciaPromedioMs(), c.tokensEntrada(), c.tokensSalida(), c.inicio(), c.fin());
        }
        jdbc.update("""
                UPDATE conversacion_lia
                SET tema = ?, categoria_id = (SELECT id FROM categoria WHERE slug = ?), sentimiento_final = ?,
                    cantidad_mensajes = ?, modo = ?, similitud_promedio = ?, latencia_promedio_ms = ?,
                    tokens_entrada = ?, tokens_salida = ?, fin = ?
                WHERE id = ?
                """,
                c.tema(), c.categoria(), c.sentimientoFinal(), c.cantidadMensajes(), c.modo(), similitud,
                c.latenciaPromedioMs(), c.tokensEntrada(), c.tokensSalida(), Timestamp.valueOf(c.fin()), c.id());
        return c;
    }

    @Override
    public void sumarRecomendaciones(long conversacionId, List<String> codigosProducto) {
        for (String codigo : codigosProducto) {
            // Si ya se había recomendado en esta conversación, suma 1; si no, crea la fila con 1
            jdbc.update("""
                    INSERT INTO lia_recomendacion (conversacion_id, producto_id, veces)
                    SELECT ?, id, 1 FROM producto WHERE codigo = ?
                    ON CONFLICT (conversacion_id, producto_id) DO UPDATE SET veces = lia_recomendacion.veces + 1
                    """, conversacionId, codigo);
        }
    }
}
