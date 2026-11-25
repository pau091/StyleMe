package co.styleme.aplicacion.puerto.salida.ia;

/**
 * Puerto de salida: las instrucciones de Lía (su personalidad y reglas), guardadas como plantilla
 * FUERA del código. La plantilla tiene dos huecos que llena el caso de uso:
 *   {{TIENDAS}}  → las tiendas que existen
 *   {{CONTEXTO}} → los fragmentos recuperados de la base vectorial
 */
public interface PlantillaInstruccionesPuerto {

    String TIENDAS = "{{TIENDAS}}";
    String CONTEXTO = "{{CONTEXTO}}";

    String instruccionesLia();
}
