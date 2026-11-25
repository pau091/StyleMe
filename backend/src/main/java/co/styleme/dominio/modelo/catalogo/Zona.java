package co.styleme.dominio.modelo.catalogo;

/** Zona de Bucaramanga donde queda una tienda (Cabecera, Sur, La Aurora...). */
public record Zona(Short id, String nombre) {

    public Zona {
        nombre = Validar.obligatorio(nombre, "nombre de la zona");
    }
}
