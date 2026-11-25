package co.styleme.aplicacion.puerto.salida.ia;

import co.styleme.dominio.modelo.ia.Guia;
import java.util.List;

/** Puerto de salida: las guías de conocimiento (hoy son archivos Markdown del proyecto). */
public interface GuiaRepositorioPuerto {

    List<Guia> listar();
}
