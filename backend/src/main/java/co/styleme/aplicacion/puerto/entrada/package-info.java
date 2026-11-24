/**
 * Puertos de entrada.
 *
 * Regla R4: una interfaz por caso de uso, con sufijo CasoUso
 * (ej.: BuscarSemanticoCasoUso, ChatearConLiaCasoUso).
 * Los adaptadores de entrada (REST, webhooks) solo llaman a estas interfaces.
 *
 * Ver docs/planeacion/16-arquitectura-hexagonal.md
 */
package co.styleme.aplicacion.puerto.entrada;
