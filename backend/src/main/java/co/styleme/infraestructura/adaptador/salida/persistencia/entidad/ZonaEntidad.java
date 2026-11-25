package co.styleme.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Fila de la tabla "zona". */
@Entity
@Table(name = "zona")
public class ZonaEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    protected ZonaEntidad() {
    }

    public Short getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
