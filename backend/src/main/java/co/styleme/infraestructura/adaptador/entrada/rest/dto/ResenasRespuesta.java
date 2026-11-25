package co.styleme.infraestructura.adaptador.entrada.rest.dto;

import java.util.List;

/** Calificación promedio + las reseñas publicadas de una tienda o producto. */
public record ResenasRespuesta(double promedio, int cantidad, List<ResenaRespuesta> resenas) {
}
