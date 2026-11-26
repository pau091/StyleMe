package co.styleme.aplicacion.casodeuso.comunidad;

import co.styleme.aplicacion.puerto.entrada.comunidad.GestionarResenasCasoUso;
import co.styleme.aplicacion.puerto.salida.notificacion.NotificadorPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ProductoRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ResenaRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.TiendaRepositorioPuerto;
import co.styleme.dominio.excepcion.ProductoNoEncontradoException;
import co.styleme.dominio.excepcion.RecursoDuplicadoException;
import co.styleme.dominio.excepcion.TiendaNoEncontradaException;
import co.styleme.dominio.modelo.comunidad.Objetivo;
import co.styleme.dominio.modelo.comunidad.Resena;
import co.styleme.dominio.modelo.comunidad.ResumenCalificacion;
import java.util.List;
import java.util.Objects;

/**
 * Caso de uso: escribir y consultar reseñas.
 * Reglas: la tienda o el producto deben existir; una sesión reseña cada cosa una sola vez;
 * solo se muestran las reseñas PUBLICADAS (las demás esperan moderación).
 * Cada reseña nueva se avisa al notificador (n8n la modera con IA).
 */
public class GestionarResenasServicio implements GestionarResenasCasoUso {

    static final int MAXIMO_MOSTRADAS = 50;
    static final int MAXIMO_RECIENTES = 12;

    private final ResenaRepositorioPuerto resenas;
    private final TiendaRepositorioPuerto tiendas;
    private final ProductoRepositorioPuerto productos;
    private final NotificadorPuerto notificador;

    public GestionarResenasServicio(ResenaRepositorioPuerto resenas, TiendaRepositorioPuerto tiendas,
            ProductoRepositorioPuerto productos, NotificadorPuerto notificador) {
        this.resenas = Objects.requireNonNull(resenas);
        this.tiendas = Objects.requireNonNull(tiendas);
        this.productos = Objects.requireNonNull(productos);
        this.notificador = Objects.requireNonNull(notificador);
    }

    @Override
    public Resena escribir(Resena nueva) {
        verificarQueExiste(nueva.objetivo());
        if (resenas.existe(nueva.sesionId(), nueva.objetivo())) {
            throw new RecursoDuplicadoException("Ya escribiste una reseña para esto");
        }
        Resena guardada = resenas.guardar(nueva);
        try {
            notificador.resenaCreada(guardada);
        } catch (RuntimeException e) {
            // Si el aviso falla, la reseña igual quedó guardada: la moderación programada la encontrará
        }
        return guardada;
    }

    @Override
    public List<Resena> publicadas(Objetivo objetivo) {
        verificarQueExiste(objetivo);
        return resenas.publicadas(objetivo, MAXIMO_MOSTRADAS);
    }

    @Override
    public ResumenCalificacion resumen(Objetivo objetivo) {
        verificarQueExiste(objetivo);
        return resenas.resumen(objetivo);
    }

    @Override
    public List<Resena> recientes(int limite) {
        return resenas.recientes(Math.max(1, Math.min(limite, MAXIMO_RECIENTES)));
    }

    private void verificarQueExiste(Objetivo objetivo) {
        if (objetivo.esTienda() && !tiendas.existe(objetivo.referencia())) {
            throw new TiendaNoEncontradaException(objetivo.referencia());
        }
        if (!objetivo.esTienda() && productos.buscarPorCodigo(objetivo.referencia()).isEmpty()) {
            throw new ProductoNoEncontradoException(objetivo.referencia());
        }
    }
}
