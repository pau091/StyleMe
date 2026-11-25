package co.styleme.aplicacion.puerto.salida.notificacion;

import co.styleme.dominio.modelo.comunidad.Resena;

/**
 * Puerto de salida: avisar hacia afuera que pasó algo importante.
 * El adaptador actual le avisa a n8n (N8nNotificadorAdaptador), que modera la reseña con IA.
 * Si mañana se cambia n8n por otra herramienta, solo cambia el adaptador.
 */
public interface NotificadorPuerto {

    /** Se escribió una reseña nueva (queda PENDIENTE de moderación). No debe lanzar errores. */
    void resenaCreada(Resena resena);
}
