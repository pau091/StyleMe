package co.styleme.aplicacion.casodeuso.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.styleme.aplicacion.puerto.salida.persistencia.TiendaRepositorioPuerto;
import co.styleme.dominio.excepcion.TiendaNoEncontradaException;
import co.styleme.dominio.modelo.catalogo.Categoria;
import co.styleme.dominio.modelo.catalogo.Tienda;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConsultarTiendasServicioTest {

    private final TiendaRepositorioPuerto tiendas = mock(TiendaRepositorioPuerto.class);
    private final ConsultarTiendasServicio servicio = new ConsultarTiendasServicio(tiendas);
    private final Tienda bohor = new Tienda(2L, "bohor", "BOHOR", null, new Categoria((short) 1, "ropa", "Ropa"),
            null, null, null, null, null, null, false, true);

    @Test
    @DisplayName("Listar pasa los filtros al repositorio")
    void listar() {
        when(tiendas.buscarActivas("ropa", "Cabecera")).thenReturn(List.of(bohor));
        assertThat(servicio.listar("ropa", "Cabecera")).containsExactly(bohor);
    }

    @Test
    @DisplayName("Obtener una tienda inexistente lanza TiendaNoEncontradaException")
    void obtenerInexistente() {
        when(tiendas.buscarPorSlug("x")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> servicio.obtener("x")).isInstanceOf(TiendaNoEncontradaException.class);
    }

    @Test
    @DisplayName("Obtener una tienda existente la devuelve")
    void obtenerExistente() {
        when(tiendas.buscarPorSlug("bohor")).thenReturn(Optional.of(bohor));
        assertThat(servicio.obtener("bohor").nombre()).isEqualTo("BOHOR");
    }
}
