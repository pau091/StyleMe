package co.styleme.aplicacion.puerto.entrada.analitica;

import co.styleme.dominio.modelo.analitica.Indicador;
import java.util.List;

/** Puerto de entrada: los indicadores del informe de análisis de datos (panel de administración). */
public interface ConsultarIndicadoresCasoUso {

    List<Indicador> todos();

    Indicador uno(Indicador.Codigo codigo);
}
