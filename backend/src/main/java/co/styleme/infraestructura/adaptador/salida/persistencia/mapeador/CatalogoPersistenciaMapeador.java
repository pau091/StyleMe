package co.styleme.infraestructura.adaptador.salida.persistencia.mapeador;

import co.styleme.dominio.modelo.catalogo.Categoria;
import co.styleme.dominio.modelo.catalogo.Contacto;
import co.styleme.dominio.modelo.catalogo.Coordenadas;
import co.styleme.dominio.modelo.catalogo.Etiqueta;
import co.styleme.dominio.modelo.catalogo.GrupoEtiqueta;
import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.catalogo.Tienda;
import co.styleme.dominio.modelo.catalogo.TiendaReferencia;
import co.styleme.dominio.modelo.catalogo.TipoPrenda;
import co.styleme.dominio.modelo.catalogo.Zona;
import co.styleme.infraestructura.adaptador.salida.persistencia.entidad.CategoriaEntidad;
import co.styleme.infraestructura.adaptador.salida.persistencia.entidad.EtiquetaEntidad;
import co.styleme.infraestructura.adaptador.salida.persistencia.entidad.ProductoEntidad;
import co.styleme.infraestructura.adaptador.salida.persistencia.entidad.TiendaEntidad;
import co.styleme.infraestructura.adaptador.salida.persistencia.entidad.ZonaEntidad;
import java.util.Comparator;

/**
 * Convierte filas de la base (entidades JPA) en modelos del dominio (regla R6).
 * El dominio nunca ve una entidad JPA, y las entidades nunca salen de este adaptador.
 */
public final class CatalogoPersistenciaMapeador {

    private CatalogoPersistenciaMapeador() {
    }

    public static Categoria aDominio(CategoriaEntidad e) {
        return new Categoria(e.getId(), e.getSlug(), e.getNombre());
    }

    public static Zona aDominio(ZonaEntidad e) {
        return e == null ? null : new Zona(e.getId(), e.getNombre());
    }

    public static Tienda aDominio(TiendaEntidad e) {
        return new Tienda(
                e.getId(),
                e.getSlug(),
                e.getNombre(),
                e.getDescripcion(),
                aDominio(e.getCategoriaPrincipal()),
                aDominio(e.getZona()),
                e.getDireccion(),
                Coordenadas.siEstanCompletas(e.getLat(), e.getLng()),
                e.getHorario(),
                new Contacto(e.getWhatsapp(), e.getTelefono(), e.getInstagram(), e.getWeb()),
                e.getFoto(),
                e.isVerificado(),
                e.isActiva());
    }

    public static Producto aDominio(ProductoEntidad e) {
        TiendaEntidad tienda = e.getTienda();
        return new Producto(
                e.getId(),
                e.getCodigo(),
                new TiendaReferencia(tienda.getId(), tienda.getSlug(), tienda.getNombre()),
                aDominio(e.getCategoria()),
                e.getNombre(),
                e.getMarca(),
                e.getPrecio(),
                e.getDescripcion(),
                e.getFoto(),
                e.getFuente(),
                TipoPrenda.desdeCodigo(e.getTipoPrenda()),
                e.getFotoProbador(),
                e.isEsEjemplo(),
                e.isActivo(),
                e.getIngredientes().stream().map(pi -> pi.getIngrediente().getNombre()).toList(),
                e.getEtiquetas().stream()
                        .sorted(Comparator.comparing(EtiquetaEntidad::getId))
                        .map(et -> new Etiqueta(GrupoEtiqueta.desdeCodigo(et.getGrupo()), et.getValor()))
                        .toList());
    }
}
