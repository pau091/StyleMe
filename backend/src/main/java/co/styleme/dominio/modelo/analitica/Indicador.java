package co.styleme.dominio.modelo.analitica;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Un indicador del informe de análisis de datos, ya calculado: su código y sus filas
 * (cada fila es un mapa columna → valor, porque cada indicador tiene columnas distintas).
 */
public record Indicador(Codigo codigo, List<Map<String, Object>> filas) {

    /** Los 13 indicadores (cada uno corresponde a una vista v_... de basedatos/05-vistas-indicadores.sql). */
    public enum Codigo {
        CONVERSION_TIENDAS("conversion_tiendas", "De cada 100 visitas al perfil de una tienda, cuántas terminan en contacto"),
        CALIFICACION_TIENDAS("calificacion_tiendas", "Promedio de corazones de cada tienda (reseñas publicadas)"),
        CALIFICACION_PRODUCTOS("calificacion_productos", "Promedio de corazones de cada producto (reseñas publicadas)"),
        PRODUCTOS_MAS_VISTOS("productos_mas_vistos", "Ranking de productos por vistas, con sus favoritos"),
        CATEGORIAS("categorias", "Interés por categoría: vistas, filtros, favoritos y precio promedio"),
        BUSQUEDAS_SIN_RESULTADO("busquedas_sin_resultado", "Lo que la gente busca y StyleMe no tiene"),
        ACTIVIDAD_POR_HORA("actividad_por_hora", "Eventos y sesiones por hora del día"),
        ACTIVIDAD_POR_DIA("actividad_por_dia", "Eventos y sesiones por día de la semana"),
        DISPOSITIVOS("dispositivos", "Sesiones desde celular y desde computador"),
        ZONAS("zonas", "Visitas y contactos a las tiendas de cada zona de la ciudad"),
        USO_LIA("uso_lia", "Conversaciones con Lía por tema: % sin IA, tokens y tiempos"),
        PRODUCTOS_RECOMENDADOS_LIA("productos_recomendados_lia", "Productos que más recomienda Lía"),
        CALIDAD_DATOS("calidad_datos", "Qué tan completos y verificados están los datos del catálogo");

        private final String codigo;
        private final String descripcion;

        Codigo(String codigo, String descripcion) {
            this.codigo = codigo;
            this.descripcion = descripcion;
        }

        public String codigo() {
            return codigo;
        }

        public String descripcion() {
            return descripcion;
        }

        public static Codigo desdeCodigo(String codigo) {
            for (Codigo c : values()) {
                if (c.codigo.equalsIgnoreCase(codigo)) {
                    return c;
                }
            }
            throw new DatoInvalidoException("Indicador desconocido: " + codigo);
        }
    }

    public Indicador {
        Objects.requireNonNull(codigo);
        filas = filas == null ? List.of() : List.copyOf(filas);
    }
}
