package co.styleme.dominio.modelo.lia;

import java.util.List;

/**
 * Lo que se le envía al modelo de lenguaje (la parte "AUMENTAR" del RAG).
 *
 * @param instrucciones reglas de Lía + el CONTEXTO recuperado de la base vectorial
 * @param historia      historial corto de la conversación
 * @param mensaje       lo que la persona acaba de escribir
 */
public record Prompt(String instrucciones, List<MensajeConversacion> historia, String mensaje) {

    public Prompt {
        historia = historia == null ? List.of() : List.copyOf(historia);
    }
}
