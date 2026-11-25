package co.styleme.dominio.modelo.catalogo;

import java.util.Objects;

/**
 * Tienda física de Bucaramanga.
 *
 * Reglas del negocio:
 *  - Toda tienda tiene slug, nombre y categoría principal.
 *  - Sin coordenadas, la tienda existe pero NO aparece en el mapa ni en la salida de compras.
 *  - "verificada" indica que sus datos se confirmaron con la tienda (datos honestos).
 */
public record Tienda(
        Long id,
        String slug,
        String nombre,
        String descripcion,
        Categoria categoriaPrincipal,
        Zona zona,
        String direccion,
        Coordenadas coordenadas,
        String horario,
        Contacto contacto,
        String foto,
        boolean verificada,
        boolean activa) {

    public Tienda {
        slug = Validar.obligatorio(slug, "slug de la tienda");
        nombre = Validar.obligatorio(nombre, "nombre de la tienda");
        Objects.requireNonNull(categoriaPrincipal, "La tienda necesita una categoría principal");
        descripcion = Validar.opcional(descripcion);
        direccion = Validar.opcional(direccion);
        horario = Validar.opcional(horario);
        foto = Validar.opcional(foto);
        contacto = (contacto == null) ? new Contacto(null, null, null, null) : contacto;
    }

    /** Solo las tiendas con coordenadas se dibujan en el mapa. */
    public boolean apareceEnMapa() {
        return coordenadas != null;
    }

    /** Solo las tiendas activas, con coordenadas, pueden entrar en una salida de compras. */
    public boolean sirveParaSalidaDeCompras() {
        return activa && apareceEnMapa();
    }

    /** Referencia corta para mostrar la tienda dentro de un producto. */
    public TiendaReferencia comoReferencia() {
        return new TiendaReferencia(id, slug, nombre);
    }
}
