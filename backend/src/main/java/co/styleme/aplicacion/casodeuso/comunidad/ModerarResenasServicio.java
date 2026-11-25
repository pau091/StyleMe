package co.styleme.aplicacion.casodeuso.comunidad;

import co.styleme.aplicacion.puerto.entrada.comunidad.ModerarResenasCasoUso;
import co.styleme.aplicacion.puerto.salida.persistencia.ResenaRepositorioPuerto;
import co.styleme.dominio.excepcion.DatoInvalidoException;
import co.styleme.dominio.excepcion.ResenaNoEncontradaException;
import co.styleme.dominio.modelo.comunidad.Resena;
import java.util.List;
import java.util.Objects;

/** Caso de uso: moderar reseñas pendientes (publicarlas o rechazarlas). */
public class ModerarResenasServicio implements ModerarResenasCasoUso {

    static final int LIMITE_MAXIMO = 100;

    private final ResenaRepositorioPuerto resenas;

    public ModerarResenasServicio(ResenaRepositorioPuerto resenas) {
        this.resenas = Objects.requireNonNull(resenas);
    }

    @Override
    public List<Resena> pendientes(int limite) {
        if (limite < 1 || limite > LIMITE_MAXIMO) {
            throw new DatoInvalidoException("El límite debe estar entre 1 y " + LIMITE_MAXIMO);
        }
        return resenas.pendientes(limite);
    }

    @Override
    public Resena moderar(long id, Resena.Estado estado) {
        Resena actual = resenas.buscar(id).orElseThrow(() -> new ResenaNoEncontradaException(id));
        Resena moderada = actual.moderada(estado); // el dominio valida que esté PENDIENTE
        resenas.cambiarEstado(id, moderada.estado());
        return moderada;
    }
}
