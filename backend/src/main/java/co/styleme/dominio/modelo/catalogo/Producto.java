package co.styleme.dominio.modelo.catalogo;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import java.util.List;
import java.util.Objects;

/**
 * Producto real de una tienda de Bucaramanga.
 *
 * Reglas del negocio:
 *  - Todo producto tiene código, nombre, tienda y categoría.
 *  - El precio, si se conoce, es mayor que 0 (pesos, sin decimales). null = "por confirmar".
 *  - "esEjemplo" marca los productos de ejemplo (no reales): StyleMe es honesto con sus datos.
 *  - Los ingredientes van en el orden de la etiqueta (el primero es el de mayor concentración).
 */
public record Producto(
        Long id,
        String codigo,
        TiendaReferencia tienda,
        Categoria categoria,
        String nombre,
        String marca,
        Integer precio,
        String descripcion,
        String foto,
        String fuente,
        TipoPrenda tipoPrenda,
        String fotoProbador,
        boolean esEjemplo,
        boolean activo,
        List<String> ingredientes,
        List<Etiqueta> etiquetas) {

    public Producto {
        codigo = Validar.obligatorio(codigo, "código del producto");
        nombre = Validar.obligatorio(nombre, "nombre del producto");
        Objects.requireNonNull(tienda, "El producto necesita una tienda");
        Objects.requireNonNull(categoria, "El producto necesita una categoría");
        if (precio != null && precio <= 0) {
            throw new DatoInvalidoException("El precio debe ser mayor que 0");
        }
        marca = Validar.opcional(marca);
        descripcion = Validar.opcional(descripcion);
        foto = Validar.opcional(foto);
        fuente = Validar.opcional(fuente);
        fotoProbador = Validar.opcional(fotoProbador);
        // Copias inmutables: nadie puede cambiar las listas desde afuera
        ingredientes = (ingredientes == null) ? List.of() : List.copyOf(ingredientes);
        etiquetas = (etiquetas == null) ? List.of() : List.copyOf(etiquetas);
    }

    public boolean tienePrecio() {
        return precio != null;
    }

    /** ¿Cuesta como máximo ese valor? Los productos sin precio no cumplen (no se sabe). */
    public boolean cuestaMaximo(int presupuesto) {
        return tienePrecio() && precio <= presupuesto;
    }

    /** Solo se puede probar en el probador virtual si es una prenda con foto sin fondo. */
    public boolean disponibleEnProbador() {
        return tipoPrenda != null && fotoProbador != null;
    }

    /** Valores de un grupo de etiquetas, por ejemplo los tipos de piel. */
    public List<String> valoresDe(GrupoEtiqueta grupo) {
        return etiquetas.stream().filter(e -> e.grupo() == grupo).map(Etiqueta::valor).toList();
    }

    /**
     * ¿Sirve para ese valor de un grupo? Por ejemplo, sirvePara(TIPO_PIEL, "grasa").
     * Un producto marcado "todas"/"todos" sirve para cualquiera.
     */
    public boolean sirvePara(GrupoEtiqueta grupo, String valor) {
        String buscado = valor == null ? "" : valor.trim().toLowerCase();
        return etiquetas.stream()
                .filter(e -> e.grupo() == grupo)
                .anyMatch(e -> e.esUniversal() || e.valor().equals(buscado));
    }

    /** ¿Lo contiene? Compara sin importar mayúsculas: "niacinamide" encuentra "Niacinamide". */
    public boolean contieneIngrediente(String ingrediente) {
        return ingrediente != null && ingredientes.stream().anyMatch(i -> i.equalsIgnoreCase(ingrediente.trim()));
    }
}
