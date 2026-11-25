package co.styleme.dominio.modelo.comunidad;

import java.time.LocalDateTime;
import java.util.Objects;

/** Una tienda o un producto que la persona guardó (el corazón de favoritos). */
public record Favorito(Objetivo objetivo, String nombre, LocalDateTime fecha) {

    public Favorito {
        Objects.requireNonNull(objetivo, "El favorito necesita una tienda o un producto");
    }
}
