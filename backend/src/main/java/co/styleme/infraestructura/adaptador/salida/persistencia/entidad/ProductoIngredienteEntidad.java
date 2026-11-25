package co.styleme.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

/**
 * Fila de "producto_ingrediente". Tiene su propia clase (y no un simple @ManyToMany)
 * porque guarda un dato extra de la relación: el ORDEN del ingrediente en la etiqueta.
 */
@Entity
@Table(name = "producto_ingrediente")
public class ProductoIngredienteEntidad {

    @EmbeddedId
    private ProductoIngredienteId id;

    @MapsId("productoId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private ProductoEntidad producto;

    @MapsId("ingredienteId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingrediente_id")
    private IngredienteEntidad ingrediente;

    @Column(nullable = false)
    private Short orden;

    protected ProductoIngredienteEntidad() {
    }

    public IngredienteEntidad getIngrediente() {
        return ingrediente;
    }

    public Short getOrden() {
        return orden;
    }
}
