package co.styleme.aplicacion.puerto.salida.persistencia;

import co.styleme.dominio.modelo.analitica.Tendencias.Conteo;
import java.util.List;

/** Puerto de salida: contar búsquedas y visitas de los últimos días (tabla evento). */
public interface TendenciaRepositorioPuerto {

    /** Búsquedas con resultados hechas por al menos minimoSesiones sesiones distintas. */
    List<Conteo> busquedas(int dias, int minimoSesiones, int limite);

    /** Tiendas activas más visitadas (por sesiones distintas). */
    List<Conteo> tiendasMasVistas(int dias, int limite);

    /** Productos reales (no de ejemplo) más vistos (por sesiones distintas). */
    List<Conteo> productosMasVistos(int dias, int limite);
}
