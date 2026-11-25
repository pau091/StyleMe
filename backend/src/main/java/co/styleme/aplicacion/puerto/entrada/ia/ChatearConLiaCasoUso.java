package co.styleme.aplicacion.puerto.entrada.ia;

import co.styleme.dominio.modelo.lia.PreguntaLia;
import co.styleme.dominio.modelo.lia.RespuestaLia;
import java.util.function.Consumer;

/** Puerto de entrada: conversar con Lía (RAG: recuperar → aumentar → generar → validar). */
public interface ChatearConLiaCasoUso {

    /**
     * @param alRecibirTexto recibe el texto de Lía a medida que se genera (para mostrarlo mientras "habla")
     * @return la respuesta final revisada: emoción, productos reales, acciones y sugerencias
     */
    RespuestaLia conversar(PreguntaLia pregunta, Consumer<String> alRecibirTexto);
}
