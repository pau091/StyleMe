package co.styleme.infraestructura.adaptador.entrada.programador;

import co.styleme.aplicacion.puerto.entrada.ia.IndexarConocimientoCasoUso;
import co.styleme.dominio.excepcion.ServicioIaNoDisponibleException;
import co.styleme.dominio.modelo.ia.ResultadoIndexacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada "programado": nadie lo llama por HTTP, lo dispara el reloj.
 * Cada noche reindexa el conocimiento; como compara hashes, si nada cambió no gasta tokens.
 * Se apaga con styleme.ia.reindexacion-programada=false.
 */
@Component
@ConditionalOnProperty(name = "styleme.ia.reindexacion-programada", havingValue = "true", matchIfMissing = true)
public class ReindexacionProgramador {

    private static final Logger LOG = LoggerFactory.getLogger(ReindexacionProgramador.class);

    private final IndexarConocimientoCasoUso indexarConocimiento;

    public ReindexacionProgramador(IndexarConocimientoCasoUso indexarConocimiento) {
        this.indexarConocimiento = indexarConocimiento;
    }

    /** Todos los días a las 3:00 a. m. (hora de Colombia). */
    @Scheduled(cron = "${styleme.ia.reindexacion-cron:0 0 3 * * *}", zone = "America/Bogota")
    public void reindexar() {
        try {
            ResultadoIndexacion r = indexarConocimiento.indexar();
            LOG.info("Reindexación nocturna: {} fragmentos, {} embeddings calculados, {} eliminados",
                    r.total(), r.embeddingsCalculados(), r.eliminados());
        } catch (ServicioIaNoDisponibleException e) {
            // Sin IA no se reindexa, pero el backend sigue funcionando (se intenta de nuevo mañana)
            LOG.warn("No se pudo reindexar: {}", e.getMessage());
        }
    }
}
