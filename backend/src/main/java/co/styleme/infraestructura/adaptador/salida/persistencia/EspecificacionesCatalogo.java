package co.styleme.infraestructura.adaptador.salida.persistencia;

import co.styleme.dominio.modelo.catalogo.FiltroProductos;
import co.styleme.infraestructura.adaptador.salida.persistencia.entidad.ProductoEntidad;
import co.styleme.infraestructura.adaptador.salida.persistencia.entidad.TiendaEntidad;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinámicos del catálogo (Criteria API).
 * Cada filtro solo se agrega si viene: así un mismo método sirve para "todos los productos"
 * y para "skincare de Pandora de máximo $50.000 que diga sérum".
 */
final class EspecificacionesCatalogo {

    private static final String CON_TILDE = "áéíóúüñ";
    private static final String SIN_TILDE = "aeiouun";

    private EspecificacionesCatalogo() {
    }

    static Specification<TiendaEntidad> tiendasActivas(String categoria, String zona) {
        return (raiz, consulta, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            condiciones.add(cb.isTrue(raiz.get("activa")));
            if (categoria != null) {
                condiciones.add(cb.equal(raiz.get("categoriaPrincipal").get("slug"), categoria));
            }
            if (zona != null) {
                condiciones.add(cb.equal(cb.lower(raiz.get("zona").get("nombre")), zona.toLowerCase()));
            }
            return cb.and(condiciones.toArray(Predicate[]::new));
        };
    }

    static Specification<ProductoEntidad> productos(FiltroProductos filtro) {
        return (raiz, consulta, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            condiciones.add(cb.isTrue(raiz.get("activo")));
            if (filtro.categoria() != null) {
                condiciones.add(cb.equal(raiz.get("categoria").get("slug"), filtro.categoria()));
            }
            if (filtro.tienda() != null) {
                condiciones.add(cb.equal(raiz.get("tienda").get("slug"), filtro.tienda()));
            }
            if (filtro.precioMaximo() != null) {
                // Los productos sin precio no cumplen un presupuesto (regla del dominio)
                condiciones.add(cb.lessThanOrEqualTo(raiz.get("precio"), filtro.precioMaximo()));
            }
            if (filtro.texto() != null) {
                String patron = "%" + sinTildes(filtro.texto()) + "%";
                condiciones.add(cb.or(
                        cb.like(sinTildes(cb, raiz.get("nombre")), patron),
                        cb.like(sinTildes(cb, raiz.get("marca")), patron)));
            }
            return cb.and(condiciones.toArray(Predicate[]::new));
        };
    }

    /** "Sérum" → "serum" en Java, para comparar sin importar tildes ni mayúsculas. */
    static String sinTildes(String texto) {
        String sinMarcas = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinMarcas.toLowerCase().trim();
    }

    /** Lo mismo, pero en la base de datos: translate(lower(columna), 'áéí…', 'aei…'). */
    private static Expression<String> sinTildes(CriteriaBuilder cb, Expression<String> columna) {
        return cb.function("translate", String.class, cb.lower(columna), cb.literal(CON_TILDE), cb.literal(SIN_TILDE));
    }
}
