package co.styleme.dominio.modelo.ia;

import co.styleme.dominio.excepcion.DatoInvalidoException;
import java.util.Arrays;

/**
 * Embedding: una lista de números que representa el SIGNIFICADO de un texto.
 * Textos con significado parecido tienen vectores que apuntan en direcciones parecidas.
 *
 * Es un objeto de valor inmutable y siempre está NORMALIZADO (largo = 1). Con vectores
 * normalizados, la similitud coseno es simplemente el producto punto.
 */
public final class Embedding {

    /** Debe coincidir con la columna VECTOR(768) de fragmento_conocimiento. */
    public static final int DIMENSIONES = 768;

    private final float[] valores;

    private Embedding(float[] valores) {
        this.valores = valores;
    }

    /** Crea el embedding normalizándolo. Rechaza vectores de otro tamaño o vacíos. */
    public static Embedding de(float[] valores) {
        if (valores == null || valores.length != DIMENSIONES) {
            throw new DatoInvalidoException("Un embedding debe tener " + DIMENSIONES + " dimensiones");
        }
        double suma = 0;
        for (float v : valores) {
            suma += (double) v * v;
        }
        double largo = Math.sqrt(suma);
        if (largo == 0) {
            throw new DatoInvalidoException("Un embedding no puede ser un vector de ceros");
        }
        float[] normalizado = new float[valores.length];
        for (int i = 0; i < valores.length; i++) {
            normalizado[i] = (float) (valores[i] / largo);
        }
        return new Embedding(normalizado);
    }

    /**
     * Similitud coseno entre dos embeddings: 1 = mismo significado, 0 = nada que ver.
     * (Es la misma medida que usa pgvector con el operador {@code <=>}, que devuelve 1 - similitud.)
     */
    public double similitud(Embedding otro) {
        double producto = 0;
        for (int i = 0; i < valores.length; i++) {
            producto += (double) valores[i] * otro.valores[i];
        }
        return producto;
    }

    /** Copia de los valores (nadie puede modificar el embedding desde afuera). */
    public float[] valores() {
        return valores.clone();
    }

    @Override
    public boolean equals(Object otro) {
        return otro instanceof Embedding e && Arrays.equals(valores, e.valores);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(valores);
    }

    @Override
    public String toString() {
        return "Embedding[" + DIMENSIONES + " dimensiones]";
    }
}
