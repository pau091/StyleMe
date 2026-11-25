package co.styleme.aplicacion.casodeuso.analitica;

import co.styleme.aplicacion.puerto.entrada.analitica.ConsultarIndicadoresCasoUso;
import co.styleme.aplicacion.puerto.salida.persistencia.IndicadorRepositorioPuerto;
import co.styleme.dominio.modelo.analitica.Indicador;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Caso de uso: calcular los indicadores del informe de análisis de datos. */
public class ConsultarIndicadoresServicio implements ConsultarIndicadoresCasoUso {

    private final IndicadorRepositorioPuerto indicadores;

    public ConsultarIndicadoresServicio(IndicadorRepositorioPuerto indicadores) {
        this.indicadores = Objects.requireNonNull(indicadores);
    }

    @Override
    public List<Indicador> todos() {
        return Arrays.stream(Indicador.Codigo.values()).map(indicadores::consultar).toList();
    }

    @Override
    public Indicador uno(Indicador.Codigo codigo) {
        return indicadores.consultar(Objects.requireNonNull(codigo));
    }
}
