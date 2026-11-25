package co.styleme.dominio.modelo.ia;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Un trozo de texto que Lía y el buscador pueden consultar. Se convierte en embedding y se
 * guarda en la tabla fragmento_conocimiento.
 *
 * Reglas del negocio:
 *  - Un fragmento de TIENDA tiene tiendaId; uno de PRODUCTO tiene productoId; uno de GUIA no tiene ninguno.
 *  - El hash (SHA-256 del texto) permite saber si el texto cambió: si no cambió, NO se vuelve
 *    a pedir el embedding a Gemini (ahorra tokens y tiempo).
 */
public record FragmentoConocimiento(
        TipoFuente tipo,
        Long tiendaId,
        Long productoId,
        String titulo,
        String texto,
        String hashTexto) {

    /** Largo máximo de un fragmento (en caracteres), para que el embedding sea preciso. */
    public static final int LARGO_MAXIMO = 1500;

    public FragmentoConocimiento {
        Objects.requireNonNull(tipo, "El fragmento necesita un tipo de fuente");
        if (titulo == null || titulo.isBlank() || texto == null || texto.isBlank()) {
            throw new DatoInvalidoException("El fragmento necesita título y texto");
        }
        if (texto.length() > LARGO_MAXIMO) {
            throw new DatoInvalidoException("Un fragmento no puede superar " + LARGO_MAXIMO + " caracteres");
        }
        boolean coherente = switch (tipo) {
            case TIENDA -> tiendaId != null && productoId == null;
            case PRODUCTO -> productoId != null && tiendaId == null;
            case GUIA -> tiendaId == null && productoId == null;
        };
        if (!coherente) {
            throw new DatoInvalidoException("Las llaves del fragmento no coinciden con su tipo " + tipo);
        }
        titulo = titulo.trim();
        texto = texto.trim();
        hashTexto = calcularHash(texto);
    }

    public static FragmentoConocimiento deTienda(long tiendaId, String titulo, String texto) {
        return new FragmentoConocimiento(TipoFuente.TIENDA, tiendaId, null, titulo, texto, null);
    }

    public static FragmentoConocimiento deProducto(long productoId, String titulo, String texto) {
        return new FragmentoConocimiento(TipoFuente.PRODUCTO, null, productoId, titulo, texto, null);
    }

    public static FragmentoConocimiento deGuia(String titulo, String texto) {
        return new FragmentoConocimiento(TipoFuente.GUIA, null, null, titulo, texto, null);
    }

    /**
     * Identidad del fragmento: de qué fuente sale y con qué título. Sirve para saber, al reindexar,
     * si un fragmento ya existe (y comparar su hash) o es nuevo.
     */
    public String clave() {
        return clave(tipo, tiendaId, productoId, titulo);
    }

    public static String clave(TipoFuente tipo, Long tiendaId, Long productoId, String titulo) {
        return tipo.codigo() + "|" + Objects.toString(tiendaId, "") + "|" + Objects.toString(productoId, "") + "|" + titulo;
    }

    private static String calcularHash(String texto) {
        try {
            byte[] huella = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(huella);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible", e);
        }
    }
}
