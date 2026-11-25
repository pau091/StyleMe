package co.styleme.aplicacion.puerto.salida.ia;

import co.styleme.dominio.modelo.evaluacion.CasoEvaluacion;
import java.util.List;

/** Puerto de salida: el conjunto de preguntas de evaluación con su respuesta esperada. */
public interface ConjuntoEvaluacionPuerto {

    List<CasoEvaluacion> casos();
}
