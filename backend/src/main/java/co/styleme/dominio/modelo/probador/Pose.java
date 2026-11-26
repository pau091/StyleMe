package co.styleme.dominio.modelo.probador;

import co.styleme.dominio.excepcion.DatoInvalidoException;

/**
 * Las 4 poses predeterminadas del probador con IA (P-017), para ver el outfit desde varios ángulos.
 * La descripción es lo que se le pide al modelo de imágenes.
 */
public enum Pose {

    FRENTE("de frente", "de pie y de frente a la cámara, con los brazos relajados a los lados"),
    TRES_CUARTOS("tres cuartos", "girada tres cuartos hacia un lado, con una mano en la cintura, mirando a la cámara"),
    PERFIL("de perfil", "de perfil, de pie junto a un perchero, mostrando cómo cae la prenda de lado"),
    CAMINANDO("caminando", "caminando hacia la cámara por el pasillo de la tienda, con paso natural");

    private final String nombre;
    private final String descripcion;

    Pose(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String nombre() {
        return nombre;
    }

    public String descripcion() {
        return descripcion;
    }

    public static Pose desde(String texto) {
        try {
            return Pose.valueOf(texto == null ? "" : texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DatoInvalidoException("Pose desconocida: " + texto + " (usa FRENTE, TRES_CUARTOS, PERFIL o CAMINANDO)");
        }
    }
}
