package co.styleme.dominio.modelo.catalogo;

/**
 * Lo mínimo que un producto necesita saber de su tienda.
 * Así un producto no carga la tienda completa (contacto, horario, coordenadas...).
 */
public record TiendaReferencia(Long id, String slug, String nombre) {

    public TiendaReferencia {
        slug = Validar.obligatorio(slug, "slug de la tienda");
        nombre = Validar.obligatorio(nombre, "nombre de la tienda");
    }
}
