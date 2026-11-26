package co.styleme.aplicacion.puerto.salida.ia;

import co.styleme.dominio.modelo.probador.Imagen;
import java.util.List;

/**
 * Puerto de salida: un modelo de IA que genera imágenes a partir de instrucciones e imágenes de referencia.
 * Adaptadores: OpenAI (gpt-image) o Gemini (imagen). Se elige en el .env con STYLEME_PROBADOR_PROVEEDOR.
 */
public interface GeneradorImagenPuerto {

    /** ¿Hay un proveedor configurado (con su clave)? Si no, la página usa el probador local. */
    boolean disponible();

    /** Nombre del proveedor, para mostrarlo en el aviso de privacidad ("OpenAI", "Google Gemini"). */
    String proveedor();

    /**
     * @param referencias en orden: persona, prenda y (opcional) fondo de la tienda
     * @throws co.styleme.dominio.excepcion.ServicioIaNoDisponibleException si el proveedor no responde o no hay cupo
     */
    Imagen generar(String instrucciones, List<Imagen> referencias);
}
