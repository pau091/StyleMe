package co.styleme.infraestructura.adaptador.entrada.rest.dto;

import co.styleme.dominio.modelo.analitica.Indicador;
import java.util.List;
import java.util.Map;

/** Un indicador del informe de datos: su código, qué mide y sus filas (las columnas de la vista SQL). */
public record IndicadorRespuesta(String codigo, String descripcion, List<Map<String, Object>> filas) {

    public static IndicadorRespuesta de(Indicador i) {
        return new IndicadorRespuesta(i.codigo().codigo(), i.codigo().descripcion(), i.filas());
    }
}
