package co.styleme.dominio.servicio;

import co.styleme.dominio.modelo.catalogo.GrupoEtiqueta;
import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.catalogo.Tienda;
import co.styleme.dominio.modelo.ia.FragmentoConocimiento;
import co.styleme.dominio.modelo.ia.Guia;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * CHUNKING: convierte tiendas, productos y guías en fragmentos de texto listos para embeddings.
 *
 * Decisiones:
 *  - Una tienda = 1 fragmento; un producto = 1 fragmento (sus datos caben en un texto corto).
 *  - Una guía se parte por secciones "## ...": cada sección habla de UN tema, y un fragmento
 *    con un solo tema produce un embedding más preciso que uno con toda la guía mezclada.
 *  - Si una sección es muy larga, se parte por párrafos (sin cortar frases a la mitad).
 *  - El texto se escribe en lenguaje natural ("Tipo de piel: grasa"), igual a como pregunta
 *    la gente; así la pregunta y el fragmento quedan cerca en el espacio de embeddings.
 */
public class FragmentadorConocimiento {

    private static final int MAX_INGREDIENTES = 8;
    private static final NumberFormat PESOS = NumberFormat.getIntegerInstance(Locale.of("es", "CO"));

    public FragmentoConocimiento deTienda(Tienda t) {
        var texto = new StringBuilder();
        texto.append("Tienda: ").append(t.nombre()).append(". ");
        texto.append("Categoría principal: ").append(t.categoriaPrincipal().nombre()).append(". ");
        agregar(texto, null, t.descripcion());
        agregar(texto, "Zona", t.zona() == null ? null : t.zona().nombre());
        agregar(texto, "Dirección", t.direccion());
        agregar(texto, "Horario", t.horario());
        var c = t.contacto();
        if (c.tieneWhatsapp()) {
            texto.append("Se puede contactar por WhatsApp. ");
        }
        agregar(texto, "Instagram", c.instagram() == null ? null : "@" + c.instagram());
        agregar(texto, "Página web", c.web());
        return FragmentoConocimiento.deTienda(t.id(), t.nombre(), texto.toString());
    }

    public FragmentoConocimiento deProducto(Producto p) {
        var texto = new StringBuilder();
        texto.append("Producto: ").append(p.nombre()).append(". ");
        agregar(texto, "Marca", p.marca());
        texto.append("Categoría: ").append(p.categoria().nombre()).append(". ");
        texto.append("Tienda: ").append(p.tienda().nombre()).append(". ");
        texto.append("Precio: ").append(p.tienePrecio() ? "$" + PESOS.format(p.precio()) : "por confirmar con la tienda").append(". ");
        agregar(texto, null, p.descripcion());
        agregar(texto, "Tipo de prenda", p.tipoPrenda() == null ? null : p.tipoPrenda().codigo());
        agregar(texto, "Paso de la rutina", unir(p.valoresDe(GrupoEtiqueta.PASO)));
        agregar(texto, "Tipo de piel", unir(p.valoresDe(GrupoEtiqueta.TIPO_PIEL)));
        agregar(texto, "Tipo de cabello", unir(p.valoresDe(GrupoEtiqueta.TIPO_CABELLO)));
        agregar(texto, "Cuero cabelludo", unir(p.valoresDe(GrupoEtiqueta.CUERO)));
        agregar(texto, "Sirve para", unir(p.valoresDe(GrupoEtiqueta.OBJETIVO)));
        agregar(texto, "Libre de", unir(p.valoresDe(GrupoEtiqueta.LIBRE_DE)));
        // Solo los primeros ingredientes: son los de mayor concentración y los que más describen el producto
        agregar(texto, "Ingredientes principales", unir(p.ingredientes().stream().limit(MAX_INGREDIENTES).toList()));
        return FragmentoConocimiento.deProducto(p.id(), p.nombre(), recortar(texto.toString()));
    }

    /** Una guía → un fragmento por sección "## ...". */
    public List<FragmentoConocimiento> deGuia(Guia guia) {
        String tituloGuia = guia.titulo();
        List<FragmentoConocimiento> fragmentos = new ArrayList<>();
        String seccion = null;
        var contenido = new StringBuilder();
        for (String linea : guia.contenido().lines().toList()) {
            String limpia = linea.trim();
            if (limpia.startsWith("## ")) {
                cerrarSeccion(fragmentos, tituloGuia, seccion, contenido);
                seccion = limpia.substring(3).trim();
                contenido.setLength(0);
            } else if (!limpia.startsWith("# ")) {
                contenido.append(linea).append('\n');
            }
        }
        cerrarSeccion(fragmentos, tituloGuia, seccion, contenido);
        return fragmentos;
    }

    private void cerrarSeccion(List<FragmentoConocimiento> fragmentos, String tituloGuia, String seccion, StringBuilder contenido) {
        String cuerpo = contenido.toString().trim();
        if (cuerpo.isEmpty()) {
            return;
        }
        String titulo = seccion == null ? tituloGuia : tituloGuia + " · " + seccion;
        String encabezado = seccion == null ? tituloGuia + ". " : tituloGuia + ". " + seccion + ": ";
        List<String> partes = partirPorParrafos(cuerpo, FragmentoConocimiento.LARGO_MAXIMO - encabezado.length());
        for (int i = 0; i < partes.size(); i++) {
            String tituloParte = partes.size() == 1 ? titulo : titulo + " (parte " + (i + 1) + ")";
            fragmentos.add(FragmentoConocimiento.deGuia(tituloParte, encabezado + partes.get(i)));
        }
    }

    /** Junta párrafos mientras quepan; si un párrafo solo no cabe, lo parte por frases. */
    static List<String> partirPorParrafos(String texto, int maximo) {
        List<String> unidades = new ArrayList<>();
        for (String parrafo : texto.split("\\n\\s*\\n")) {
            String p = parrafo.replaceAll("\\s+", " ").trim();
            if (p.isEmpty()) {
                continue;
            }
            if (p.length() <= maximo) {
                unidades.add(p);
            } else {
                unidades.addAll(List.of(p.split("(?<=[.!?])\\s+")));
            }
        }
        List<String> partes = new ArrayList<>();
        var actual = new StringBuilder();
        for (String unidad : unidades) {
            if (!actual.isEmpty() && actual.length() + 1 + unidad.length() > maximo) {
                partes.add(actual.toString());
                actual.setLength(0);
            }
            if (!actual.isEmpty()) {
                actual.append(' ');
            }
            actual.append(unidad.length() > maximo ? unidad.substring(0, maximo) : unidad);
        }
        if (!actual.isEmpty()) {
            partes.add(actual.toString());
        }
        return partes;
    }

    private static void agregar(StringBuilder texto, String etiqueta, String valor) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        String limpio = valor.trim();
        texto.append(etiqueta == null ? limpio : etiqueta + ": " + limpio);
        texto.append(limpio.endsWith(".") ? " " : ". ");
    }

    private static String unir(List<String> valores) {
        return valores.isEmpty() ? null : String.join(", ", valores);
    }

    private static String recortar(String texto) {
        return texto.length() <= FragmentoConocimiento.LARGO_MAXIMO
                ? texto
                : texto.substring(0, FragmentoConocimiento.LARGO_MAXIMO);
    }
}
