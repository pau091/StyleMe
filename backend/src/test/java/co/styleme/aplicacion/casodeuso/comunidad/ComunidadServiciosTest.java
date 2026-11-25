package co.styleme.aplicacion.casodeuso.comunidad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.styleme.aplicacion.casodeuso.analitica.RegistrarEventosServicio;
import co.styleme.aplicacion.puerto.salida.persistencia.EventoRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.FavoritoRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ProductoRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ResenaRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.TiendaRepositorioPuerto;
import co.styleme.dominio.excepcion.DatoInvalidoException;
import co.styleme.dominio.excepcion.RecursoDuplicadoException;
import co.styleme.dominio.excepcion.ResenaNoEncontradaException;
import co.styleme.dominio.excepcion.TiendaNoEncontradaException;
import co.styleme.dominio.modelo.analitica.Evento;
import co.styleme.dominio.modelo.comunidad.Objetivo;
import co.styleme.dominio.modelo.comunidad.Resena;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Casos de uso de comunidad y analítica con puertos falsos (Mockito). */
class ComunidadServiciosTest {

    private ResenaRepositorioPuerto resenas;
    private TiendaRepositorioPuerto tiendas;
    private ProductoRepositorioPuerto productos;
    private GestionarResenasServicio gestionar;
    private ModerarResenasServicio moderar;

    @BeforeEach
    void preparar() {
        resenas = mock(ResenaRepositorioPuerto.class);
        tiendas = mock(TiendaRepositorioPuerto.class);
        productos = mock(ProductoRepositorioPuerto.class);
        gestionar = new GestionarResenasServicio(resenas, tiendas, productos);
        moderar = new ModerarResenasServicio(resenas);
        when(tiendas.existe("bohor")).thenReturn(true);
    }

    @Test
    @DisplayName("Escribir una reseña de una tienda que existe la guarda")
    void escribir() {
        Resena nueva = Resena.nueva("s1", "Ana", Objetivo.tienda("bohor"), 5, "Excelente");
        when(resenas.guardar(nueva)).thenReturn(nueva);
        assertThat(gestionar.escribir(nueva).estado()).isEqualTo(Resena.Estado.PENDIENTE);
    }

    @Test
    @DisplayName("Reseñar una tienda que no existe es un 404, y no se guarda nada")
    void tiendaInexistente() {
        Resena nueva = Resena.nueva("s1", null, Objetivo.tienda("no-existe"), 4, null);
        assertThatThrownBy(() -> gestionar.escribir(nueva)).isInstanceOf(TiendaNoEncontradaException.class);
        verify(resenas, never()).guardar(any());
    }

    @Test
    @DisplayName("Una sesión no puede reseñar dos veces lo mismo (409)")
    void duplicada() {
        Resena nueva = Resena.nueva("s1", null, Objetivo.tienda("bohor"), 4, null);
        when(resenas.existe("s1", Objetivo.tienda("bohor"))).thenReturn(true);
        assertThatThrownBy(() -> gestionar.escribir(nueva)).isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    @DisplayName("Moderar publica una reseña pendiente; una que no existe es un 404")
    void moderacion() {
        Resena pendiente = new Resena(7L, "s1", null, Objetivo.tienda("bohor"), 5, null, Resena.Estado.PENDIENTE, null);
        when(resenas.buscar(7L)).thenReturn(Optional.of(pendiente));
        assertThat(moderar.moderar(7L, Resena.Estado.PUBLICADA).estado()).isEqualTo(Resena.Estado.PUBLICADA);
        verify(resenas).cambiarEstado(7L, Resena.Estado.PUBLICADA);

        when(resenas.buscar(8L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> moderar.moderar(8L, Resena.Estado.PUBLICADA)).isInstanceOf(ResenaNoEncontradaException.class);
    }

    @Test
    @DisplayName("Favoritos: no se puede guardar una tienda que no existe")
    void favoritos() {
        FavoritoRepositorioPuerto favoritos = mock(FavoritoRepositorioPuerto.class);
        var servicio = new GestionarFavoritosServicio(favoritos, tiendas, productos);
        servicio.agregar("s1", Objetivo.tienda("bohor"));
        verify(favoritos).agregar("s1", Objetivo.tienda("bohor"));
        assertThatThrownBy(() -> servicio.agregar("s1", Objetivo.tienda("no-existe"))).isInstanceOf(TiendaNoEncontradaException.class);
        assertThatThrownBy(() -> servicio.listar(" ")).isInstanceOf(DatoInvalidoException.class);
    }

    @Test
    @DisplayName("Eventos: se registran en lotes de máximo 50")
    void eventosEnLotes() {
        EventoRepositorioPuerto repositorio = mock(EventoRepositorioPuerto.class);
        when(repositorio.guardar(any())).thenAnswer(i -> ((List<?>) i.getArgument(0)).size());
        var servicio = new RegistrarEventosServicio(repositorio);
        Evento e = new Evento(Evento.Tipo.ABRIR_LIA, "s1", null, null, null, null, null, null, null);

        assertThat(servicio.registrar(List.of(e, e))).isEqualTo(2);
        assertThat(servicio.registrar(List.of())).isZero();
        assertThatThrownBy(() -> servicio.registrar(Collections.nCopies(51, e))).isInstanceOf(DatoInvalidoException.class);
    }
}
