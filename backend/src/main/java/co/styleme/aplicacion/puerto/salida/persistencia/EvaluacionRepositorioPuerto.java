package co.styleme.aplicacion.puerto.salida.persistencia;

import co.styleme.dominio.modelo.evaluacion.ResultadoCaso;

/** Puerto de salida: guarda cada resultado de la evaluación (tabla evaluacion_ia) para el informe. */
public interface EvaluacionRepositorioPuerto {

    void guardar(String ejecucion, ResultadoCaso resultado);
}
