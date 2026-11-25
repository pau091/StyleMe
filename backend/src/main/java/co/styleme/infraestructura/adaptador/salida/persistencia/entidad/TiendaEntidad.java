package co.styleme.infraestructura.adaptador.salida.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Fila de la tabla "tienda". */
@Entity
@Table(name = "tienda")
public class TiendaEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String slug;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    // LAZY: la categoría se carga solo si se usa (y en lote, ver application.yml)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_principal_id", nullable = false)
    private CategoriaEntidad categoriaPrincipal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id")
    private ZonaEntidad zona;

    @Column(length = 150)
    private String direccion;

    @Column(precision = 9, scale = 6)
    private BigDecimal lat;

    @Column(precision = 9, scale = 6)
    private BigDecimal lng;

    @Column(length = 120)
    private String horario;

    @Column(length = 15)
    private String whatsapp;

    @Column(length = 15)
    private String telefono;

    @Column(length = 60)
    private String instagram;

    @Column(length = 150)
    private String web;

    @Column(length = 255)
    private String foto;

    @Column(nullable = false)
    private boolean verificado;

    @Column(nullable = false)
    private boolean activa;

    @Column(name = "comerciante_id")
    private Long comercianteId;

    @Column(name = "fecha_registro", nullable = false, insertable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    protected TiendaEntidad() {
    }

    public Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public CategoriaEntidad getCategoriaPrincipal() {
        return categoriaPrincipal;
    }

    public ZonaEntidad getZona() {
        return zona;
    }

    public String getDireccion() {
        return direccion;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public BigDecimal getLng() {
        return lng;
    }

    public String getHorario() {
        return horario;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getInstagram() {
        return instagram;
    }

    public String getWeb() {
        return web;
    }

    public String getFoto() {
        return foto;
    }

    public boolean isVerificado() {
        return verificado;
    }

    public boolean isActiva() {
        return activa;
    }

    public Long getComercianteId() {
        return comercianteId;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}
