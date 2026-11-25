package co.styleme.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Fila de la tabla "categoria". Es el modelo de la BASE DE DATOS, no el del dominio (regla R6). */
@Entity
@Table(name = "categoria")
public class CategoriaEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, unique = true, length = 20)
    private String slug;

    @Column(nullable = false, length = 40)
    private String nombre;

    protected CategoriaEntidad() {
        // JPA necesita un constructor vacío
    }

    public Short getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getNombre() {
        return nombre;
    }
}
