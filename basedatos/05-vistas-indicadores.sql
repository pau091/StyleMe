-- =====================================================================
-- STYLEME · 05 · VISTAS DE INDICADORES
-- PostgreSQL 17
--
-- Cada vista es un INDICADOR del informe de análisis de datos. Una vista es una
-- consulta guardada con nombre: se consulta como una tabla (SELECT * FROM v_...)
-- pero siempre calcula con los datos actuales.
--
-- El backend las expone en GET /api/admin/indicadores (IndicadorPersistenciaAdaptador).
-- Las consultas de estas vistas son las que se explican en docs/informe-datos/.
--
-- Aplicar en una base que ya existe (sin borrar datos):
--   docker compose exec -T basedatos psql -U styleme -d styleme -f /docker-entrypoint-initdb.d/05-vistas-indicadores.sql
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1 · CONVERSIÓN POR TIENDA: de cada 100 visitas al perfil, cuántas terminan en contacto
--     (clic en WhatsApp o en "cómo llegar"). Responde: ¿qué tiendas convierten mejor?
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW v_conversion_tiendas AS
SELECT t.slug,
       t.nombre,
       (t.whatsapp IS NOT NULL)                                          AS tiene_whatsapp,
       count(*) FILTER (WHERE e.tipo = 'VER_TIENDA')                     AS visitas,
       count(*) FILTER (WHERE e.tipo = 'CLIC_WHATSAPP')                  AS clics_whatsapp,
       count(*) FILTER (WHERE e.tipo = 'CLIC_COMO_LLEGAR')               AS clics_como_llegar,
       round(100.0 * count(*) FILTER (WHERE e.tipo IN ('CLIC_WHATSAPP', 'CLIC_COMO_LLEGAR'))
             / NULLIF(count(*) FILTER (WHERE e.tipo = 'VER_TIENDA'), 0), 1) AS tasa_conversion
FROM tienda t
LEFT JOIN evento e ON e.tienda_id = t.id
GROUP BY t.id
ORDER BY tasa_conversion DESC NULLS LAST;

-- ---------------------------------------------------------------------
-- 2 · CALIFICACIÓN DE TIENDAS (solo reseñas PUBLICADAS)
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW v_calificacion_tiendas AS
SELECT t.slug,
       t.nombre,
       count(r.id)                     AS resenas,
       round(avg(r.corazones), 2)      AS promedio_corazones,
       count(r.id) FILTER (WHERE r.corazones <= 2) AS resenas_negativas
FROM tienda t
LEFT JOIN resena r ON r.tienda_id = t.id AND r.estado = 'PUBLICADA'
GROUP BY t.id
ORDER BY promedio_corazones DESC NULLS LAST, resenas DESC;

-- ---------------------------------------------------------------------
-- 3 · CALIFICACIÓN DE PRODUCTOS (con al menos una reseña publicada)
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW v_calificacion_productos AS
SELECT p.codigo,
       p.nombre,
       c.slug                          AS categoria,
       count(r.id)                     AS resenas,
       round(avg(r.corazones), 2)      AS promedio_corazones
FROM producto p
JOIN categoria c ON c.id = p.categoria_id
JOIN resena r ON r.producto_id = p.id AND r.estado = 'PUBLICADA'
GROUP BY p.id, c.slug
ORDER BY promedio_corazones DESC, resenas DESC;

-- ---------------------------------------------------------------------
-- 4 · PRODUCTOS MÁS VISTOS, con sus favoritos y su ranking (función de ventana RANK)
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW v_productos_mas_vistos AS
SELECT RANK() OVER (ORDER BY count(DISTINCT e.id) DESC) AS posicion,
       p.codigo,
       p.nombre,
       c.slug                                            AS categoria,
       p.precio,
       count(DISTINCT e.id)                              AS vistas,
       count(DISTINCT f.id)                              AS favoritos
FROM producto p
JOIN categoria c ON c.id = p.categoria_id
LEFT JOIN evento e   ON e.producto_id = p.id AND e.tipo = 'VER_PRODUCTO'
LEFT JOIN favorito f ON f.producto_id = p.id
WHERE NOT p.es_ejemplo
GROUP BY p.id, c.slug
ORDER BY vistas DESC;

-- ---------------------------------------------------------------------
-- 5 · INTERÉS POR CATEGORÍA: vistas de productos, filtros usados y favoritos
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW v_categorias AS
SELECT c.slug                                                             AS categoria,
       c.nombre,
       (SELECT count(*) FROM evento e JOIN producto p ON p.id = e.producto_id
         WHERE e.tipo = 'VER_PRODUCTO' AND p.categoria_id = c.id)         AS vistas_productos,
       (SELECT count(*) FROM evento e
         WHERE e.tipo = 'FILTRAR_CATEGORIA' AND e.categoria_id = c.id)    AS filtros,
       (SELECT count(*) FROM favorito f JOIN producto p ON p.id = f.producto_id
         WHERE p.categoria_id = c.id)                                     AS favoritos,
       (SELECT round(avg(p.precio)) FROM producto p
         WHERE p.categoria_id = c.id AND p.precio IS NOT NULL)            AS precio_promedio
FROM categoria c
ORDER BY vistas_productos DESC;

-- ---------------------------------------------------------------------
-- 6 · BÚSQUEDAS SIN RESULTADOS: lo que la gente busca y StyleMe no tiene (demanda insatisfecha)
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW v_busquedas_sin_resultado AS
SELECT lower(trim(texto_busqueda))  AS busqueda,
       count(*)                      AS veces,
       count(DISTINCT sesion_id)     AS personas
FROM evento
WHERE tipo = 'BUSQUEDA' AND cantidad_resultados = 0
GROUP BY lower(trim(texto_busqueda))
HAVING count(*) >= 2
ORDER BY veces DESC;

-- ---------------------------------------------------------------------
-- 7 · ACTIVIDAD POR HORA DEL DÍA (0 a 23)
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW v_actividad_por_hora AS
SELECT extract(hour FROM fecha_hora)::int AS hora,
       count(*)                            AS eventos,
       count(DISTINCT sesion_id)           AS sesiones
FROM evento
GROUP BY 1
ORDER BY 1;

-- ---------------------------------------------------------------------
-- 8 · ACTIVIDAD POR DÍA DE LA SEMANA (1 = lunes … 7 = domingo)
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW v_actividad_por_dia AS
SELECT extract(isodow FROM fecha_hora)::int AS dia,
       (ARRAY['lunes','martes','miércoles','jueves','viernes','sábado','domingo'])[extract(isodow FROM fecha_hora)::int] AS nombre,
       count(*)                              AS eventos,
       count(DISTINCT sesion_id)             AS sesiones
FROM evento
GROUP BY 1, 2
ORDER BY 1;

-- ---------------------------------------------------------------------
-- 9 · DISPOSITIVOS: celular vs. computador (con porcentaje del total)
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW v_dispositivos AS
SELECT coalesce(dispositivo, 'desconocido')                              AS dispositivo,
       count(DISTINCT sesion_id)                                          AS sesiones,
       round(100.0 * count(DISTINCT sesion_id)
             / sum(count(DISTINCT sesion_id)) OVER (), 1)                 AS porcentaje
FROM evento
GROUP BY 1
ORDER BY sesiones DESC;

-- ---------------------------------------------------------------------
-- 10 · ACTIVIDAD POR ZONA DE LA CIUDAD (visitas y contactos a las tiendas de cada zona)
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW v_zonas AS
SELECT coalesce(z.nombre, 'Sin zona')                                     AS zona,
       count(DISTINCT t.id)                                               AS tiendas,
       count(e.id) FILTER (WHERE e.tipo = 'VER_TIENDA')                   AS visitas,
       count(e.id) FILTER (WHERE e.tipo IN ('CLIC_WHATSAPP', 'CLIC_COMO_LLEGAR')) AS contactos
FROM tienda t
LEFT JOIN zona z   ON z.id = t.zona_id
LEFT JOIN evento e ON e.tienda_id = t.id
GROUP BY z.nombre
ORDER BY visitas DESC;

-- ---------------------------------------------------------------------
-- 11 · USO DE LÍA por tema: conversaciones, % sin IA (plan B), tokens y tiempos
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW v_uso_lia AS
SELECT tema,
       count(*)                                                           AS conversaciones,
       round(avg(cantidad_mensajes), 1)                                   AS mensajes_promedio,
       round(100.0 * count(*) FILTER (WHERE modo <> 'IA') / count(*), 1)  AS porcentaje_sin_ia,
       round(avg(similitud_promedio), 3)                                  AS similitud_promedio,
       round(avg(latencia_promedio_ms))                                   AS latencia_promedio_ms,
       round(avg(tokens_entrada + tokens_salida))                         AS tokens_promedio
FROM conversacion_lia
GROUP BY tema
ORDER BY conversaciones DESC;

-- ---------------------------------------------------------------------
-- 12 · PRODUCTOS QUE MÁS RECOMIENDA LÍA
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW v_productos_recomendados_lia AS
SELECT p.codigo,
       p.nombre,
       c.slug                        AS categoria,
       sum(r.veces)                  AS recomendaciones,
       count(DISTINCT r.conversacion_id) AS conversaciones
FROM lia_recomendacion r
JOIN producto p  ON p.id = r.producto_id
JOIN categoria c ON c.id = p.categoria_id
GROUP BY p.id, c.slug
ORDER BY recomendaciones DESC;

-- ---------------------------------------------------------------------
-- 13 · CALIDAD DE LOS DATOS DEL CATÁLOGO (qué tan completos y verificados están)
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW v_calidad_datos AS
SELECT 'tiendas verificadas'          AS medida, round(100.0 * avg(verificado::int), 1) AS porcentaje FROM tienda
UNION ALL
SELECT 'tiendas con coordenadas',            round(100.0 * avg((lat IS NOT NULL)::int), 1) FROM tienda
UNION ALL
SELECT 'tiendas con WhatsApp',               round(100.0 * avg((whatsapp IS NOT NULL)::int), 1) FROM tienda
UNION ALL
SELECT 'productos reales (no de ejemplo)',   round(100.0 * avg((NOT es_ejemplo)::int), 1) FROM producto
UNION ALL
SELECT 'productos con precio',               round(100.0 * avg((precio IS NOT NULL)::int), 1) FROM producto
UNION ALL
SELECT 'productos con descripción',          round(100.0 * avg((descripcion IS NOT NULL)::int), 1) FROM producto;
