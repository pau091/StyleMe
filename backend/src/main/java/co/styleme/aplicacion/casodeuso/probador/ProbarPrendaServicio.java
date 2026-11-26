package co.styleme.aplicacion.casodeuso.probador;

import co.styleme.aplicacion.puerto.entrada.probador.ProbarPrendaCasoUso;
import co.styleme.aplicacion.puerto.salida.ia.DescargadorImagenPuerto;
import co.styleme.aplicacion.puerto.salida.ia.GeneradorImagenPuerto;
import co.styleme.aplicacion.puerto.salida.persistencia.ProductoRepositorioPuerto;
import co.styleme.dominio.excepcion.DatoInvalidoException;
import co.styleme.dominio.excepcion.ProductoNoEncontradoException;
import co.styleme.dominio.excepcion.ServicioIaNoDisponibleException;
import co.styleme.dominio.modelo.catalogo.Producto;
import co.styleme.dominio.modelo.probador.Imagen;
import co.styleme.dominio.modelo.probador.Pose;
import co.styleme.dominio.servicio.InstruccionesProbador;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Caso de uso: probador con IA (P-017).
 * Reglas: solo prendas de ropa con foto; la foto de la persona solo se usa para esta imagen y no se guarda.
 */
public class ProbarPrendaServicio implements ProbarPrendaCasoUso {

    private final ProductoRepositorioPuerto productos;
    private final GeneradorImagenPuerto generador;
    private final DescargadorImagenPuerto descargador;
    private final InstruccionesProbador instrucciones;

    public ProbarPrendaServicio(ProductoRepositorioPuerto productos, GeneradorImagenPuerto generador,
            DescargadorImagenPuerto descargador, InstruccionesProbador instrucciones) {
        this.productos = Objects.requireNonNull(productos);
        this.generador = Objects.requireNonNull(generador);
        this.descargador = Objects.requireNonNull(descargador);
        this.instrucciones = Objects.requireNonNull(instrucciones);
    }

    @Override
    public Estado estado() {
        boolean activo = generador.disponible();
        return new Estado(activo, activo ? generador.proveedor() : null, List.of(Pose.values()));
    }

    @Override
    public Imagen probar(Imagen persona, String codigoProducto, Pose pose, Imagen fondo) {
        Objects.requireNonNull(persona, "Falta la foto de la persona");
        Objects.requireNonNull(pose, "Falta la pose");
        if (!generador.disponible()) {
            throw new ServicioIaNoDisponibleException("El probador con IA no está activado");
        }
        Producto prenda = productos.buscarPorCodigo(codigoProducto)
                .orElseThrow(() -> new ProductoNoEncontradoException(codigoProducto));
        if (!"ropa".equals(prenda.categoria().slug())) {
            throw new DatoInvalidoException("El probador es solo para prendas de ropa");
        }
        if (prenda.foto() == null) {
            throw new DatoInvalidoException("Esta prenda no tiene foto para el probador");
        }

        List<Imagen> referencias = new ArrayList<>(List.of(persona, descargador.descargar(prenda.foto())));
        if (fondo != null) {
            referencias.add(fondo);
        }
        return generador.generar(instrucciones.para(prenda, pose, fondo != null), referencias);
    }
}
