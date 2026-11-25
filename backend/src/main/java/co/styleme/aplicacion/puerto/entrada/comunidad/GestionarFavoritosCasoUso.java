package co.styleme.aplicacion.puerto.entrada.comunidad;

import co.styleme.dominio.modelo.comunidad.Favorito;
import co.styleme.dominio.modelo.comunidad.Objetivo;
import java.util.List;

/** Puerto de entrada: guardar y quitar favoritos de una sesión. */
public interface GestionarFavoritosCasoUso {

    void agregar(String sesionId, Objetivo objetivo);

    void quitar(String sesionId, Objetivo objetivo);

    List<Favorito> listar(String sesionId);
}
