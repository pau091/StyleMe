package co.styleme.dominio.modelo.lia;

/**
 * Algo que Lía ofrece hacer y la página muestra como botón.
 *
 * @param tipo     ver_tienda, contactar_tienda, abrir_probador, armar_salida o ir_a_pasillo
 * @param tienda   slug de la tienda (si aplica)
 * @param producto código del producto (si aplica)
 * @param pasillo  categoría: ropa, maquillaje, skincare o capilar (si aplica)
 */
public record AccionLia(String tipo, String tienda, String producto, String pasillo) {
}
