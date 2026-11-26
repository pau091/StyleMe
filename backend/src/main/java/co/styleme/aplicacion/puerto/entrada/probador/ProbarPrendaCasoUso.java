package co.styleme.aplicacion.puerto.entrada.probador;

import co.styleme.dominio.modelo.probador.Imagen;
import co.styleme.dominio.modelo.probador.Pose;
import java.util.List;

/** Puerto de entrada: probador con IA (la persona dentro de la tienda, con la prenda, en una pose). */
public interface ProbarPrendaCasoUso {

    /** Si está activo y con qué proveedor (la página decide si usa el probador con IA o el local). */
    Estado estado();

    /**
     * Genera UNA imagen (una pose). La página pide las 4 poses una por una para mostrarlas a medida que llegan.
     *
     * @param fondo foto de la tienda; puede ser null
     */
    Imagen probar(Imagen persona, String codigoProducto, Pose pose, Imagen fondo);

    record Estado(boolean disponible, String proveedor, List<Pose> poses) {
    }
}
