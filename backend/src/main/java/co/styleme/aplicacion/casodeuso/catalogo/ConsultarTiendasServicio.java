package co.styleme.aplicacion.casodeuso.catalogo;

import co.styleme.aplicacion.puerto.entrada.catalogo.ConsultarTiendasCasoUso;
import co.styleme.aplicacion.puerto.salida.persistencia.TiendaRepositorioPuerto;
import co.styleme.dominio.excepcion.TiendaNoEncontradaException;
import co.styleme.dominio.modelo.catalogo.Tienda;
import java.util.List;
import java.util.Objects;

/**
 * Caso de uso: consultar tiendas.
 * Sin anotaciones de Spring (regla R3): se registra como bean en ConfiguracionBeans.
 */
public class ConsultarTiendasServicio implements ConsultarTiendasCasoUso {

    private final TiendaRepositorioPuerto tiendas;

    public ConsultarTiendasServicio(TiendaRepositorioPuerto tiendas) {
        this.tiendas = Objects.requireNonNull(tiendas);
    }

    @Override
    public List<Tienda> listar(String categoria, String zona) {
        return tiendas.buscarActivas(categoria, zona);
    }

    @Override
    public Tienda obtener(String slug) {
        return tiendas.buscarPorSlug(slug)
                .orElseThrow(() -> new TiendaNoEncontradaException(slug));
    }
}
