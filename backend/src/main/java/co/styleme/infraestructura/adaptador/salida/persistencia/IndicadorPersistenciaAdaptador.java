package co.styleme.infraestructura.adaptador.salida.persistencia;

import co.styleme.aplicacion.puerto.salida.persistencia.IndicadorRepositorioPuerto;
import co.styleme.dominio.modelo.analitica.Indicador;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de salida: cada indicador es una VISTA de basedatos/05-vistas-indicadores.sql (v_<código>).
 * El nombre de la vista sale del enum Indicador.Codigo (nunca de lo que escribe un usuario),
 * así que armar el SQL con él no permite inyección.
 */
@Component
@Transactional(readOnly = true)
public class IndicadorPersistenciaAdaptador implements IndicadorRepositorioPuerto {

    private final JdbcTemplate jdbc;

    public IndicadorPersistenciaAdaptador(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Indicador consultar(Indicador.Codigo codigo) {
        return new Indicador(codigo, jdbc.queryForList("SELECT * FROM v_" + codigo.codigo()));
    }
}
