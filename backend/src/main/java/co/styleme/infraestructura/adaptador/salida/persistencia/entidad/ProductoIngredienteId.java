package co.styleme.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/** Llave compuesta de "producto_ingrediente": la PAREJA (producto, ingrediente). */
@Embeddable
public class ProductoIngredienteId implements Serializable {

    @Column(name = "producto_id")
    private Long productoId;

    @Column(name = "ingrediente_id")
    private Long ingredienteId;

    protected ProductoIngredienteId() {
    }

    // Una llave compuesta DEBE comparar por valor: dos objetos con la misma pareja son la misma llave
    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof ProductoIngredienteId that)) {
            return false;
        }
        return Objects.equals(productoId, that.productoId) && Objects.equals(ingredienteId, that.ingredienteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productoId, ingredienteId);
    }
}
