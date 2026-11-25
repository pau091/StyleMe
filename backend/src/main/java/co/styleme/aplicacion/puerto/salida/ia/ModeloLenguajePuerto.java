package co.styleme.aplicacion.puerto.salida.ia;

import co.styleme.dominio.modelo.lia.Prompt;
import co.styleme.dominio.modelo.lia.RespuestaModelo;
import java.util.function.Consumer;

/**
 * Puerto de salida: el modelo de lenguaje que GENERA la respuesta de Lía.
 * Hoy lo implementa Gemini; cambiar de proveedor es escribir otro adaptador.
 */
public interface ModeloLenguajePuerto {

    /**
     * Genera la respuesta POR PARTES (streaming).
     *
     * @param prompt          instrucciones + contexto recuperado + historial + mensaje
     * @param alRecibirTexto  se llama con cada pedazo de texto VISIBLE a medida que llega
     *                        (nunca con los datos que van después del separador)
     * @return el texto completo, los datos sin revisar y los tokens gastados
     */
    RespuestaModelo generar(Prompt prompt, Consumer<String> alRecibirTexto);
}
