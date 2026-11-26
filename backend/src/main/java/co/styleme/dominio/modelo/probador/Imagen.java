package co.styleme.dominio.modelo.probador;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import java.util.Objects;
import java.util.Set;

/**
 * Una imagen en memoria (foto de la persona, foto de la prenda, fondo de la tienda o imagen generada).
 * Reglas: solo JPG, PNG o WEBP y como máximo 8 MB. Nunca se guarda en la base ni en disco.
 */
public record Imagen(byte[] datos, String tipoMime) {

    public static final int TAMANO_MAXIMO = 8 * 1024 * 1024;
    private static final Set<String> TIPOS = Set.of("image/jpeg", "image/png", "image/webp");

    public Imagen {
        Objects.requireNonNull(datos, "La imagen no tiene datos");
        if (datos.length == 0) {
            throw new DatoInvalidoException("La imagen está vacía");
        }
        if (datos.length > TAMANO_MAXIMO) {
            throw new DatoInvalidoException("La imagen pesa más de 8 MB");
        }
        tipoMime = tipoMime == null ? "" : tipoMime.toLowerCase().split(";")[0].trim();
        if ("image/jpg".equals(tipoMime)) {
            tipoMime = "image/jpeg";
        }
        if (!TIPOS.contains(tipoMime)) {
            throw new DatoInvalidoException("La imagen debe ser JPG, PNG o WEBP");
        }
    }

    /** Extensión de archivo (algunos proveedores la piden al subir la imagen). */
    public String extension() {
        return switch (tipoMime) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }

    @Override
    public String toString() {
        return "Imagen[" + tipoMime + ", " + datos.length + " bytes]"; // nunca imprime el contenido
    }
}
