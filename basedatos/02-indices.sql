-- =====================================================================
-- STYLEME · 02 · ÍNDICES
-- PostgreSQL 17 + pgvector
--
-- Un índice es como el índice de un libro: permite encontrar filas sin
-- leer la tabla completa. Cuesta un poco de espacio y hace las escrituras
-- algo más lentas, por eso solo se crean donde hay consultas frecuentes.
--
-- PostgreSQL ya crea índices solos para PRIMARY KEY y UNIQUE.
-- Lo que NO crea solo son índices para las llaves foráneas (REFERENCES):
-- por eso la mayoría de los de abajo son para ellas.
-- =====================================================================

-- Extensión para buscar por parecido de letras (trigramas).
-- Se usa en la búsqueda "por palabras", que se compara con la búsqueda
-- semántica en la evaluación de IA (con embeddings vs. sin embeddings).
CREATE EXTENSION IF NOT EXISTS pg_trgm;


-- =====================================================================
-- 1 · ÍNDICE VECTORIAL (el corazón de la búsqueda semántica)
-- =====================================================================
-- HNSW (Hierarchical Navigable Small World) organiza los vectores como un
-- grafo de "vecinos" en varios niveles. Para encontrar los más parecidos
-- no compara la pregunta contra todos los vectores: salta de vecino en
-- vecino hacia la zona correcta. Es una búsqueda APROXIMADA, muy rápida.
--
--   vector_cosine_ops → mide la distancia coseno, la que se usa con el
--                       operador <=> (los embeddings de Gemini se comparan así)
--   m = 16            → cuántos vecinos guarda cada vector (más = más preciso, más memoria)
--   ef_construction = 64 → cuántos candidatos revisa al construir el grafo (más = mejor grafo, más lento de crear)
--
-- Al buscar, el backend puede ajustar la precisión con: SET hnsw.ef_search = 40;
CREATE INDEX idx_fragmento_embedding_hnsw
    ON fragmento_conocimiento
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

-- Para encontrar rápido los fragmentos de una tienda o un producto
-- (al reindexar cuando cambian sus datos)
CREATE INDEX idx_fragmento_tienda   ON fragmento_conocimiento (tienda_id)   WHERE tienda_id IS NOT NULL;
CREATE INDEX idx_fragmento_producto ON fragmento_conocimiento (producto_id) WHERE producto_id IS NOT NULL;

-- Para la búsqueda híbrida: filtrar por tipo de fuente antes de comparar vectores
CREATE INDEX idx_fragmento_tipo ON fragmento_conocimiento (tipo_fuente);


-- =====================================================================
-- 2 · CATÁLOGO
-- =====================================================================

-- Llaves foráneas de tienda (filtros por categoría y por zona en la iu)
CREATE INDEX idx_tienda_categoria ON tienda (categoria_principal_id);
CREATE INDEX idx_tienda_zona      ON tienda (zona_id);

-- Llaves foráneas de producto
CREATE INDEX idx_producto_tienda ON producto (tienda_id);

-- Filtro más común del catálogo: productos ACTIVOS de una categoría, por precio.
-- Es un índice compuesto y parcial: solo incluye productos activos.
CREATE INDEX idx_producto_categoria_precio
    ON producto (categoria_id, precio)
    WHERE activo;

-- Búsqueda por palabras en el nombre del producto (ILIKE '%serum%'),
-- con trigramas: encuentra "sérum" aunque se escriba "serum" o "seru".
CREATE INDEX idx_producto_nombre_trgm
    ON producto
    USING gin (nombre gin_trgm_ops);

-- Tablas intermedias: la PRIMARY KEY (producto_id, x) ya sirve para buscar por producto.
-- Falta el sentido contrario: "¿qué productos tienen este ingrediente / esta etiqueta?"
CREATE INDEX idx_producto_ingrediente_ingrediente ON producto_ingrediente (ingrediente_id);
CREATE INDEX idx_producto_etiqueta_etiqueta       ON producto_etiqueta (etiqueta_id);


-- =====================================================================
-- 3 · COMUNIDAD
-- =====================================================================
-- Los UNIQUE (usuario_id, tienda_id) y (usuario_id, producto_id) ya sirven
-- para buscar por usuario. Faltan los de tienda y producto.

-- Promedio de corazones de una tienda o un producto: solo cuentan las PUBLICADAS
CREATE INDEX idx_resena_tienda_publicada   ON resena (tienda_id)   WHERE estado = 'PUBLICADA';
CREATE INDEX idx_resena_producto_publicada ON resena (producto_id) WHERE estado = 'PUBLICADA';

-- Cola de moderación de n8n: reseñas pendientes, de la más antigua a la más nueva
CREATE INDEX idx_resena_pendiente ON resena (fecha) WHERE estado = 'PENDIENTE';

-- "Más guardados en favoritos"
CREATE INDEX idx_favorito_tienda   ON favorito (tienda_id)   WHERE tienda_id IS NOT NULL;
CREATE INDEX idx_favorito_producto ON favorito (producto_id) WHERE producto_id IS NOT NULL;


-- =====================================================================
-- 4 · ANÁLISIS DE DATOS (consultas del informe)
-- =====================================================================

-- Casi todas las consultas del informe filtran por tipo de evento y rango de fechas
CREATE INDEX idx_evento_tipo_fecha ON evento (tipo, fecha_hora);

-- Conversión por tienda: visitas al perfil vs. clics en WhatsApp
CREATE INDEX idx_evento_tienda_tipo ON evento (tienda_id, tipo) WHERE tienda_id IS NOT NULL;

-- Productos más vistos
CREATE INDEX idx_evento_producto ON evento (producto_id) WHERE producto_id IS NOT NULL;

-- "Lo que la gente busca y no encuentra": solo búsquedas con 0 resultados
CREATE INDEX idx_evento_busqueda_sin_resultados
    ON evento (texto_busqueda)
    WHERE tipo = 'BUSQUEDA' AND cantidad_resultados = 0;

-- Conversaciones con Lía por fecha, y productos que más recomienda
CREATE INDEX idx_conversacion_inicio       ON conversacion_lia (inicio);
CREATE INDEX idx_lia_recomendacion_producto ON lia_recomendacion (producto_id);

-- Resultados de una ejecución de la evaluación de IA
CREATE INDEX idx_evaluacion_ejecucion ON evaluacion_ia (ejecucion);
