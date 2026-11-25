package co.styleme.aplicacion.puerto.salida.persistencia;

import co.styleme.dominio.modelo.analitica.Indicador;

/** Puerto de salida: calcular un indicador del informe de análisis de datos. */
public interface IndicadorRepositorioPuerto {

    Indicador consultar(Indicador.Codigo codigo);
}
