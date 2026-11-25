package co.styleme.infraestructura.adaptador.entrada.rest.mapeador;

import co.styleme.dominio.modelo.catalogo.Categoria;
import co.styleme.dominio.modelo.catalogo.Etiqueta;
import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.catalogo.Tienda;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.CategoriaRespuesta;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.ProductoRespuesta;
import co.styleme.infraestructura.adaptador.entrada.rest.dto.TiendaRespuesta;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Convierte modelos del dominio en DTO de la API (regla R6). */
public final class CatalogoRestMapeador {

    private CatalogoRestMapeador() {
    }

    public static CategoriaRespuesta aRespuesta(Categoria c) {
        return new CategoriaRespuesta(c.slug(), c.nombre());
    }

    public static TiendaRespuesta aRespuesta(Tienda t) {
        var coordenadas = t.apareceEnMapa()
                ? new TiendaRespuesta.Coordenadas(t.coordenadas().latitud(), t.coordenadas().longitud())
                : null;
        var c = t.contacto();
        return new TiendaRespuesta(
                t.slug(),
                t.nombre(),
                t.descripcion(),
                aRespuesta(t.categoriaPrincipal()),
                t.zona() == null ? null : t.zona().nombre(),
                t.direccion(),
                coordenadas,
                t.horario(),
                new TiendaRespuesta.Contacto(c.whatsapp(), c.enlaceWhatsapp(), c.telefono(), c.instagram(), c.web()),
                t.foto(),
                t.verificada(),
                t.apareceEnMapa());
    }

    public static ProductoRespuesta aRespuesta(Producto p) {
        return new ProductoRespuesta(
                p.codigo(),
                p.nombre(),
                p.marca(),
                p.precio(),
                p.descripcion(),
                p.foto(),
                p.fuente(),
                aRespuesta(p.categoria()),
                new ProductoRespuesta.TiendaResumen(p.tienda().slug(), p.tienda().nombre()),
                p.tipoPrenda() == null ? null : p.tipoPrenda().codigo(),
                p.disponibleEnProbador(),
                p.esEjemplo(),
                p.ingredientes(),
                agruparEtiquetas(p.etiquetas()));
    }

    /** [(tipo_piel, grasa), (tipo_piel, mixta)] → {"tipo_piel": ["grasa", "mixta"]} */
    private static Map<String, List<String>> agruparEtiquetas(List<Etiqueta> etiquetas) {
        return etiquetas.stream().collect(Collectors.groupingBy(
                e -> e.grupo().codigo(),
                LinkedHashMap::new,
                Collectors.mapping(Etiqueta::valor, Collectors.toList())));
    }
}
