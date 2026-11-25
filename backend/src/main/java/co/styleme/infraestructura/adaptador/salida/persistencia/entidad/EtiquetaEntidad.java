package co.styleme.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Fila de la tabla "etiqueta": un par (grupo, valor), por ejemplo (tipo_piel, grasa). */
@Entity
@Table(name = "etiqueta")
public class EtiquetaEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, length = 15)
    private String grupo;

    @Column(nullable = false, length = 30)
    private String valor;

    protected EtiquetaEntidad() {
    }

    public Short getId() {
        return id;
    }

    public String getGrupo() {
        return grupo;
    }

    public String getValor() {
        return valor;
    }
}
