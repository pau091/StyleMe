package co.styleme.infraestructura.adaptador.entrada.rest;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Límite simple de mensajes por persona (por dirección IP) y por minuto, para no gastar
 * el cupo gratuito de Gemini. Vive en memoria: si el backend se reinicia, se reinicia el conteo.
 */
@Component
public class LimitadorPeticiones {

    static final int MAXIMO_POR_MINUTO = 15;
    private static final long UN_MINUTO_MS = 60_000;

    private final Map<String, Deque<Long>> peticiones = new ConcurrentHashMap<>();

    /** ¿Puede hacer otra petición? Si sí, la cuenta. */
    public boolean permitir(String clave) {
        long ahora = System.currentTimeMillis();
        if (peticiones.size() > 5000) {
            peticiones.clear(); // protección de memoria
        }
        Deque<Long> lista = peticiones.computeIfAbsent(clave, c -> new ArrayDeque<>());
        synchronized (lista) {
            while (!lista.isEmpty() && ahora - lista.peekFirst() > UN_MINUTO_MS) {
                lista.pollFirst();
            }
            if (lista.size() >= MAXIMO_POR_MINUTO) {
                return false;
            }
            lista.addLast(ahora);
            return true;
        }
    }
}
