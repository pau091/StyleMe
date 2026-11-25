package co.styleme.aplicacion.puerto.entrada.ia;

import co.styleme.dominio.modelo.ia.ResultadoIndexacion;

/**
 * Puerto de entrada: (re)construir el índice vectorial con tiendas, productos y guías.
 * Solo calcula embeddings de lo nuevo o lo que cambió.
 */
public interface IndexarConocimientoCasoUso {

    ResultadoIndexacion indexar();
}
