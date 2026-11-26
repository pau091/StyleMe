package co.styleme.aplicacion.puerto.salida.ia;

import co.styleme.dominio.modelo.probador.Imagen;

/** Puerto de salida: trae la foto de una prenda desde la tienda en línea (la dirección guardada en el catálogo). */
public interface DescargadorImagenPuerto {

    Imagen descargar(String url);
}
