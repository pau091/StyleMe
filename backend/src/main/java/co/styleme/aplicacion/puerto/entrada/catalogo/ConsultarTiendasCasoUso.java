package co.styleme.aplicacion.puerto.entrada.catalogo;

import co.styleme.dominio.modelo.catalogo.Tienda;
import java.util.List;

/** Puerto de entrada: lo que StyleMe permite hacer con las tiendas. */
public interface ConsultarTiendasCasoUso {

    /**
     * Tiendas activas, con filtros opcionales.
     *
     * @param categoria slug de la categoría principal ("ropa"), o null para todas
     * @param zona      nombre de la zona ("Cabecera"), o null para todas
     */
    List<Tienda> listar(String categoria, String zona);

    /** Una tienda por su slug. Lanza TiendaNoEncontradaException si no existe. */
    Tienda obtener(String slug);
}
