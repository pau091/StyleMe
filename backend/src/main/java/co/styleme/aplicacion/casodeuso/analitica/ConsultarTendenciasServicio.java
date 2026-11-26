package co.styleme.aplicacion.casodeuso.analitica;

import co.styleme.aplicacion.puerto.entrada.analitica.ConsultarTendenciasCasoUso;
import co.styleme.aplicacion.puerto.salida.persistencia.TendenciaRepositorioPuerto;
import co.styleme.dominio.modelo.analitica.Tendencias;
import java.util.Objects;

/** Caso de uso: las tendencias de la portada, con la regla de privacidad de Tendencias. */
public class ConsultarTendenciasServicio implements ConsultarTendenciasCasoUso {

    private final TendenciaRepositorioPuerto tendencias;

    public ConsultarTendenciasServicio(TendenciaRepositorioPuerto tendencias) {
        this.tendencias = Objects.requireNonNull(tendencias);
    }

    @Override
    public Tendencias ultimosDias(int dias) {
        Tendencias.validarDias(dias); // antes de consultar la base
        return new Tendencias(dias,
                tendencias.busquedas(dias, Tendencias.MINIMO_SESIONES, Tendencias.LIMITE),
                tendencias.tiendasMasVistas(dias, Tendencias.LIMITE),
                tendencias.productosMasVistos(dias, Tendencias.LIMITE));
    }
}
