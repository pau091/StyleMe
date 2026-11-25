package co.styleme.dominio.modelo.catalogo;

import co.styleme.dominio.excepcion.DatoInvalidoException;

/**
 * Formas de contactar una tienda. StyleMe no vende: la persona le escribe a la tienda.
 *
 * @param whatsapp  solo números, con el 57 adelante ("573001234567")
 * @param telefono  solo números; se usa cuando la tienda no tiene WhatsApp
 * @param instagram usuario sin @
 * @param web       dirección sin https://
 */
public record Contacto(String whatsapp, String telefono, String instagram, String web) {

    public Contacto {
        whatsapp = soloNumeros(Validar.opcional(whatsapp), "WhatsApp");
        telefono = soloNumeros(Validar.opcional(telefono), "teléfono");
        instagram = Validar.opcional(instagram);
        web = Validar.opcional(web);
    }

    private static String soloNumeros(String valor, String campo) {
        if (valor != null && !valor.matches("\\d+")) {
            throw new DatoInvalidoException("El " + campo + " debe tener solo números");
        }
        return valor;
    }

    public boolean tieneWhatsapp() {
        return whatsapp != null;
    }

    /** Enlace para abrir el chat de WhatsApp, o null si la tienda no tiene. */
    public String enlaceWhatsapp() {
        return tieneWhatsapp() ? "https://wa.me/" + whatsapp : null;
    }

    /** ¿Hay al menos una forma de contactar la tienda? */
    public boolean esContactable() {
        return whatsapp != null || telefono != null || instagram != null || web != null;
    }
}
