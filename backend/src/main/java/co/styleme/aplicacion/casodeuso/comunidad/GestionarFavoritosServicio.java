package co.styleme.aplicacion.casodeuso.comunidad;

import co.styleme.aplicacion.puerto.entrada.comunidad.GestionarFavoritosCasoUso;
import co.styleme.aplicacion.puerto.salida.persistencia.FavoritoRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ProductoRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.TiendaRepositorioPuerto;
import co.styleme.dominio.excepcion.DatoInvalidoException;
import co.styleme.dominio.excepcion.ProductoNoEncontradoException;
import co.styleme.dominio.excepcion.TiendaNoEncontradaException;
import co.styleme.dominio.modelo.comunidad.Favorito;
import co.styleme.dominio.modelo.comunidad.Objetivo;
import java.util.List;
import java.util.Objects;

/** Caso de uso: favoritos de una sesión. Agregar dos veces lo mismo no duplica nada. */
public class GestionarFavoritosServicio implements GestionarFavoritosCasoUso {

    private final FavoritoRepositorioPuerto favoritos;
    private final TiendaRepositorioPuerto tiendas;
    private final ProductoRepositorioPuerto productos;

    public GestionarFavoritosServicio(FavoritoRepositorioPuerto favoritos, TiendaRepositorioPuerto tiendas,
            ProductoRepositorioPuerto productos) {
        this.favoritos = Objects.requireNonNull(favoritos);
        this.tiendas = Objects.requireNonNull(tiendas);
        this.productos = Objects.requireNonNull(productos);
    }

    @Override
    public void agregar(String sesionId, Objetivo objetivo) {
        validarSesion(sesionId);
        if (objetivo.esTienda() && !tiendas.existe(objetivo.referencia())) {
            throw new TiendaNoEncontradaException(objetivo.referencia());
        }
        if (!objetivo.esTienda() && productos.buscarPorCodigo(objetivo.referencia()).isEmpty()) {
            throw new ProductoNoEncontradoException(objetivo.referencia());
        }
        favoritos.agregar(sesionId, objetivo);
    }

    @Override
    public void quitar(String sesionId, Objetivo objetivo) {
        validarSesion(sesionId);
        favoritos.quitar(sesionId, objetivo);
    }

    @Override
    public List<Favorito> listar(String sesionId) {
        validarSesion(sesionId);
        return favoritos.listar(sesionId);
    }

    private static void validarSesion(String sesionId) {
        if (sesionId == null || sesionId.isBlank() || sesionId.length() > 36) {
            throw new DatoInvalidoException("Falta el identificador de sesión");
        }
    }
}
