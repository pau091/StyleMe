package co.styleme.aplicacion.casodeuso.ia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.styleme.aplicacion.puerto.entrada.ia.ChatearConLiaCasoUso;
import co.styleme.aplicacion.puerto.salida.persistencia.EvaluacionRepositorioPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ProductoRepositorioPuerto;
import co.styleme.dominio.modelo.catalogo.Categoria;
import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.catalogo.TiendaReferencia;
import co.styleme.dominio.modelo.evaluacion.CasoEvaluacion;
import co.styleme.dominio.modelo.evaluacion.InformeEvaluacion;
import co.styleme.dominio.modelo.evaluacion.TipoPregunta;
import co.styleme.dominio.modelo.ia.FragmentoConocimiento;
import co.styleme.dominio.modelo.lia.PreguntaLia;
import co.styleme.dominio.modelo.lia.RespuestaLia;
import co.styleme.dominio.servicio.CalificadorEvaluacion;
import java.time.Clock;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class EvaluarIaServicioTest {

    @Test
    @DisplayName("Evalúa cada pregunta, guarda los resultados con un mismo id de ejecución y no guarda conversaciones")
    void evaluaYGuarda() {
        var categoria = new Categoria((short) 3, "skincare", "Skincare");
        var tienda = new TiendaReferencia(1L, "so-beauty", "So Beauty");
        var serum = new Producto(37L, "p37", tienda, categoria, "Sérum Control Oleosidad", null, 35_000,
                null, null, null, null, null, false, true, null, null);

        var embeddings = new Falsos.EmbeddingFalso();
        var almacen = new Falsos.AlmacenFalso();
        var f = FragmentoConocimiento.deProducto(37, "Sérum Control Oleosidad", "serum control oleosidad piel grasa brillo");
        almacen.guardar(f, Falsos.EmbeddingFalso.vectorizar(f.texto()), "modelo-falso");

        ProductoRepositorioPuerto productos = mock(ProductoRepositorioPuerto.class);
        when(productos.buscar(any())).thenReturn(List.of(serum));
        ChatearConLiaCasoUso lia = mock(ChatearConLiaCasoUso.class);
        when(lia.conversar(any(), any())).thenReturn(new RespuestaLia("Te recomiendo el sérum.", "neutra", 0.5, "neutral",
                "catalogo", List.of("p37"), List.of(), List.of(), RespuestaLia.Modo.IA));
        EvaluacionRepositorioPuerto evaluaciones = mock(EvaluacionRepositorioPuerto.class);

        var servicio = new EvaluarIaServicio(
                () -> List.of(
                        new CasoEvaluacion("C1", "serum piel grasa brillo", TipoPregunta.CATALOGO, Set.of("p37"), null),
                        new CasoEvaluacion("C2", "serum control oleosidad", TipoPregunta.CATALOGO, Set.of("p37"), 30_000)),
                embeddings, almacen, productos, lia, evaluaciones, new CalificadorEvaluacion(), Clock.systemDefaultZone(), 0);

        InformeEvaluacion informe = servicio.evaluar();

        assertThat(informe.total()).isEqualTo(2);
        assertThat(informe.aciertoTop3Semantico()).isEqualTo(1.0);
        assertThat(informe.porcentajeRespetaPresupuesto()).isEqualTo(0.0); // C2: el sérum cuesta 35.000 > 30.000
        verify(evaluaciones, times(2)).guardar(eq(informe.ejecucion()), any());

        var preguntas = ArgumentCaptor.forClass(PreguntaLia.class);
        verify(lia, times(2)).conversar(preguntas.capture(), any());
        assertThat(preguntas.getAllValues()).allMatch(p -> p.sesionId() == null); // no ensucia el informe de conversaciones
    }
}
