package co.styleme.aplicacion.puerto.entrada.analitica;

import co.styleme.dominio.modelo.analitica.Tendencias;

/** Puerto de entrada: lo más buscado y visto en los últimos días (portada, datos públicos y anónimos). */
public interface ConsultarTendenciasCasoUso {

    Tendencias ultimosDias(int dias);
}
