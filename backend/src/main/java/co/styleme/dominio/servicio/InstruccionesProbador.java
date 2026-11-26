package co.styleme.dominio.servicio;

import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.probador.Pose;
import java.util.Objects;

/**
 * Arma las instrucciones para el modelo de imágenes del probador con IA (Java puro, se prueba sin IA).
 *
 * Las reglas protegen a la persona y a la tienda: no cambiar el cuerpo ni la cara, no inventar la prenda,
 * no prometer tallas y no agregar texto ni marcas a la imagen.
 */
public class InstruccionesProbador {

    public String para(Producto prenda, Pose pose, boolean conFondoDeTienda) {
        Objects.requireNonNull(prenda, "Falta la prenda");
        Objects.requireNonNull(pose, "Falta la pose");
        String tipo = prenda.tipoPrenda() == null ? "prenda" : switch (prenda.tipoPrenda().codigo()) {
            case "superior" -> "prenda superior (blusa, camisa o chaqueta)";
            case "inferior" -> "prenda inferior (pantalón o falda)";
            default -> "prenda completa (vestido o enterizo)";
        };
        String fondo = conFondoDeTienda
                ? "El fondo es el interior de la tienda de la TERCERA imagen (" + prenda.tienda().nombre() + "): conserva su ambiente."
                : "El fondo es el interior de una boutique de ropa en Bucaramanga (" + prenda.tienda().nombre() + "), luminosa y ordenada.";
        return String.join("\n",
                "Genera UNA fotografía realista, vertical y de cuerpo completo para un probador virtual.",
                "PRIMERA imagen: la persona. Conserva exactamente su cara, tono de piel, cabello, edad y forma del cuerpo. No la adelgaces ni la cambies.",
                "SEGUNDA imagen: la " + tipo + " \"" + prenda.nombre() + "\" de la tienda " + prenda.tienda().nombre() + ".",
                "Viste a la persona con esa prenda copiando fielmente su color, estampado, largo y detalles. No inventes otra prenda.",
                "Si la prenda es solo superior o inferior, completa el resto del outfit con ropa neutra y sencilla.",
                "Pose: " + pose.descripcion() + ".",
                fondo,
                "Luz natural y suave. Sin texto, sin logotipos, sin marcas de agua.");
    }
}
