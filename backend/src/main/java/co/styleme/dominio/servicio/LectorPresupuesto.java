package co.styleme.dominio.servicio;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Entiende el presupuesto escrito por la persona: "máximo 50 mil", "hasta 150.000", "menos de 80k".
 * Se usa como FILTRO DURO en la recuperación: Lía nunca recibe productos más caros que eso.
 */
public class LectorPresupuesto {

    private static final Pattern PRESUPUESTO = Pattern.compile(
            "(?:m[aá]ximo|hasta|menos de|no m[aá]s de|presupuesto(?: de)?|tengo)\\s*\\$?\\s*(\\d{1,3}(?:[.\\s]\\d{3})+|\\d+)\\s*(mil|k)?",
            Pattern.CASE_INSENSITIVE);

    public Optional<Integer> leer(String texto) {
        if (texto == null) {
            return Optional.empty();
        }
        Matcher m = PRESUPUESTO.matcher(texto.toLowerCase());
        if (!m.find()) {
            return Optional.empty();
        }
        long valor = Long.parseLong(m.group(1).replaceAll("[.\\s]", ""));
        // "50 mil", "80k" o un número pequeño como "50" se entienden en miles
        if (m.group(2) != null || valor < 1000) {
            valor *= 1000;
        }
        return (valor >= 1000 && valor <= 100_000_000) ? Optional.of((int) valor) : Optional.empty();
    }
}
