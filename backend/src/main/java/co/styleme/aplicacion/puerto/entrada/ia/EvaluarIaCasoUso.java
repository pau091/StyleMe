package co.styleme.aplicacion.puerto.entrada.ia;

import co.styleme.dominio.modelo.evaluacion.InformeEvaluacion;

/**
 * Puerto de entrada: medir la calidad de la IA con el conjunto de 30 preguntas.
 * Cada ejecución queda guardada en evaluacion_ia (evidencia para el informe y la sustentación).
 */
public interface EvaluarIaCasoUso {

    InformeEvaluacion evaluar();
}
