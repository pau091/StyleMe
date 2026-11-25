package co.styleme.dominio.modelo.analitica;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Algo que pasó en la página (una visita, un clic, una búsqueda...). Es la materia prima de
 * los indicadores del informe de análisis de datos. Es anónimo: solo lleva el id de la sesión.
 *
 * Reglas del negocio:
 *  - VER_TIENDA, CLIC_WHATSAPP y CLIC_COMO_LLEGAR necesitan la tienda.
 *  - VER_PRODUCTO necesita el producto; FILTRAR_CATEGORIA, la categoría.
 *  - BUSQUEDA necesita el texto buscado y cuántos resultados hubo (0 = no encontró nada).
 */
public record Evento(
        Tipo tipo,
        String sesionId,
        String tienda,
        String producto,
        String categoria,
        String textoBusqueda,
        Integer cantidadResultados,
        String dispositivo,
        Map<String, String> detalle) {

    public enum Tipo {
        VER_TIENDA, VER_PRODUCTO, CLIC_WHATSAPP, CLIC_COMO_LLEGAR, BUSQUEDA,
        FILTRAR_CATEGORIA, USAR_PROBADOR, USAR_RECOMENDADOR, ARMAR_SALIDA, ABRIR_LIA
    }

    private static final Set<String> DISPOSITIVOS = Set.of("celular", "computador");
    private static final int LARGO_MAXIMO_BUSQUEDA = 100;
    private static final int MAXIMO_DATOS_DETALLE = 10;

    public Evento {
        Objects.requireNonNull(tipo, "El evento necesita un tipo");
        if (sesionId == null || sesionId.isBlank() || sesionId.length() > 36) {
            throw new DatoInvalidoException("Falta el identificador de sesión");
        }
        tienda = vacioANull(tienda);
        producto = vacioANull(producto);
        categoria = vacioANull(categoria);
        textoBusqueda = vacioANull(textoBusqueda);
        if (textoBusqueda != null && textoBusqueda.length() > LARGO_MAXIMO_BUSQUEDA) {
            textoBusqueda = textoBusqueda.substring(0, LARGO_MAXIMO_BUSQUEDA);
        }
        switch (tipo) {
            case VER_TIENDA, CLIC_WHATSAPP, CLIC_COMO_LLEGAR -> exigir(tienda, "la tienda");
            case VER_PRODUCTO -> exigir(producto, "el producto");
            case FILTRAR_CATEGORIA -> exigir(categoria, "la categoría");
            case BUSQUEDA -> {
                exigir(textoBusqueda, "el texto buscado");
                if (cantidadResultados == null || cantidadResultados < 0) {
                    throw new DatoInvalidoException("Una búsqueda necesita la cantidad de resultados");
                }
            }
            default -> { }
        }
        dispositivo = (dispositivo != null && DISPOSITIVOS.contains(dispositivo)) ? dispositivo : null;
        detalle = detalle == null ? Map.of() : Map.copyOf(detalle);
        if (detalle.size() > MAXIMO_DATOS_DETALLE) {
            throw new DatoInvalidoException("El detalle del evento tiene demasiados datos");
        }
    }

    /** ¿La búsqueda no encontró nada? (demanda que StyleMe no cubre) */
    public boolean esBusquedaSinResultados() {
        return tipo == Tipo.BUSQUEDA && cantidadResultados == 0;
    }

    private static void exigir(String valor, String que) {
        if (valor == null) {
            throw new DatoInvalidoException("Este tipo de evento necesita " + que);
        }
    }

    private static String vacioANull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
