package co.styleme.dominio.modelo.ia;

import co.styleme.dominio.excepcion.DatoInvalidoException;

/**
 * Guía de conocimiento escrita para StyleMe (en Markdown): rutina de skincare, cuidado capilar...
 * Le da a Lía información que no está en el catálogo.
 *
 * @param archivo   nombre del archivo ("rutina-skincare.md"), sirve como identificador
 * @param contenido texto en Markdown: "# Título" y secciones "## ..."
 */
public record Guia(String archivo, String contenido) {

    public Guia {
        if (archivo == null || archivo.isBlank() || contenido == null || contenido.isBlank()) {
            throw new DatoInvalidoException("Una guía necesita nombre de archivo y contenido");
        }
    }

    /** El título es la primera línea "# ...". Si no hay, se usa el nombre del archivo. */
    public String titulo() {
        return contenido.lines()
                .map(String::trim)
                .filter(linea -> linea.startsWith("# "))
                .map(linea -> linea.substring(2).trim())
                .findFirst()
                .orElse(archivo.replace(".md", ""));
    }
}
