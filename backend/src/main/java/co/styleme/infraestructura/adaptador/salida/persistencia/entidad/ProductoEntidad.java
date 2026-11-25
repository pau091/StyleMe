package co.styleme.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Fila de la tabla "producto", con sus ingredientes (ordenados) y sus etiquetas. */
@Entity
@Table(name = "producto")
public class ProductoEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tienda_id", nullable = false)
    private TiendaEntidad tienda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaEntidad categoria;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 80)
    private String marca;

    private Integer precio;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 500)
    private String foto;

    @Column(length = 500)
    private String fuente;

    @Column(name = "fuente_ingredientes", length = 500)
    private String fuenteIngredientes;

    @Column(name = "tipo_prenda", length = 10)
    private String tipoPrenda;

    @Column(name = "foto_probador", length = 255)
    private String fotoProbador;

    @Column(name = "es_ejemplo", nullable = false)
    private boolean esEjemplo;

    @Column(nullable = false)
    private boolean activo;

    @Column(name = "fecha_registro", nullable = false, insertable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    // Relación con dato extra (orden): se recorre ya ordenada desde la base
    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
    @OrderBy("orden ASC")
    private List<ProductoIngredienteEntidad> ingredientes = new ArrayList<>();

    // Relación muchos a muchos simple: la tabla intermedia solo tiene las dos llaves
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "producto_etiqueta",
            joinColumns = @JoinColumn(name = "producto_id"),
            inverseJoinColumns = @JoinColumn(name = "etiqueta_id"))
    private Set<EtiquetaEntidad> etiquetas = new LinkedHashSet<>();

    protected ProductoEntidad() {
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public TiendaEntidad getTienda() {
        return tienda;
    }

    public CategoriaEntidad getCategoria() {
        return categoria;
    }

    public String getNombre() {
        return nombre;
    }

    public String getMarca() {
        return marca;
    }

    public Integer getPrecio() {
        return precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFoto() {
        return foto;
    }

    public String getFuente() {
        return fuente;
    }

    public String getFuenteIngredientes() {
        return fuenteIngredientes;
    }

    public String getTipoPrenda() {
        return tipoPrenda;
    }

    public String getFotoProbador() {
        return fotoProbador;
    }

    public boolean isEsEjemplo() {
        return esEjemplo;
    }

    public boolean isActivo() {
        return activo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public List<ProductoIngredienteEntidad> getIngredientes() {
        return ingredientes;
    }

    public Set<EtiquetaEntidad> getEtiquetas() {
        return etiquetas;
    }
}
