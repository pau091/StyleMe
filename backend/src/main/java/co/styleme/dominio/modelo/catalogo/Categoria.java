package co.styleme.dominio.modelo.catalogo;

/**
 * Categoría del catálogo: ropa, maquillaje, skincare o capilar (D-004).
 *
 * @param slug   identificador para las direcciones y filtros ("skincare")
 * @param nombre nombre para mostrar ("Cuidado capilar")
 */
public record Categoria(Short id, String slug, String nombre) {

    public Categoria {
        slug = Validar.obligatorio(slug, "slug de la categoría");
        nombre = Validar.obligatorio(nombre, "nombre de la categoría");
    }
}
