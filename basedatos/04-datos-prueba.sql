-- =====================================================================
-- STYLEME · 04 · DATOS DE PRUEBA PARA EL ANÁLISIS DE DATOS
-- PostgreSQL 17
--
-- ⚠️ DATOS SIMULADOS (no son personas reales). Sirven para calcular los indicadores del
-- informe de análisis mientras la página no tenga visitantes reales. Todo queda MARCADO:
--   · usuarios con correo ...@prueba.styleme
--   · eventos y conversaciones con sesion_id que empieza por "prueba-"
--   · eventos con detalle {"prueba": true}
-- Para borrarlos: ver el final de este archivo.
--
-- Simula 8 semanas de uso con patrones realistas:
--   · más visitas en Cabecera y en las tiendas más conocidas
--   · ~68 % de las sesiones desde el celular; picos al mediodía y en la noche; más uso el fin de semana
--   · las tiendas SIN WhatsApp no pueden recibir clics de WhatsApp (afecta su conversión)
--   · ~35 % de las búsquedas son de cosas que StyleMe no tiene (zapatos, perfume, bolsos...)
--
-- setseed: los números "al azar" salen SIEMPRE iguales → el informe es reproducible.
-- "+ 0 * n" / "+ 0 * e.id": truco para que PostgreSQL sortee en CADA fila. Sin él, el planificador
-- puede calcular el random() una sola vez y repetir el mismo valor en todas las filas.
--
-- Aplicar en una base que ya existe (sin borrar el índice de la IA ni las evaluaciones):
--   docker compose exec -T basedatos psql -U styleme -d styleme -f /docker-entrypoint-initdb.d/04-datos-prueba.sql
-- =====================================================================

BEGIN;

SELECT setseed(0.42);

-- ---------------------------------------------------------------------
-- 0 · Tablas de apoyo temporales (desaparecen al terminar)
-- ---------------------------------------------------------------------
-- Popularidad relativa de cada tienda (más peso = más visitas)
CREATE TEMP TABLE peso_tienda (slug text, peso int, calificacion numeric, tasa_whatsapp numeric) ON COMMIT DROP;
INSERT INTO peso_tienda VALUES
    ('mattelsa',          18, 4.2, 0.12),
    ('pandora-makeup',    16, 4.4, 0.20),
    ('mirada-de-angel',   14, 4.6, 0.24),
    ('bohor',             12, 4.0, 0.15),
    ('so-beauty',         10, 4.3, 0.18),
    ('guacamaya-colors',   9, 4.1, 0.16),
    ('gogo-makeup',        6, 3.7, 0.14),
    ('samylu-boutique',    6, 3.9, 0.14),
    ('queen',              5, 3.6, 0.14),
    ('henars-eleganzza',   4, 4.5, 0.14);

-- Peso de cada categoría en las vistas de productos
CREATE TEMP TABLE peso_categoria (slug text, peso int) ON COMMIT DROP;
INSERT INTO peso_categoria VALUES ('skincare', 35), ('maquillaje', 30), ('capilar', 20), ('ropa', 15);

-- Días de las últimas 8 semanas; sábado y domingo cuentan doble (más uso el fin de semana)
CREATE TEMP TABLE dia AS
SELECT d::date AS fecha
FROM generate_series(current_date - 56, current_date - 1, interval '1 day') d,
     generate_series(1, CASE WHEN extract(isodow FROM d) IN (6, 7) THEN 2 ELSE 1 END);

-- Horas con su peso (picos al mediodía y entre 6 y 9 de la noche)
CREATE TEMP TABLE hora AS
SELECT unnest(ARRAY[8,9,10,10,11,11,12,12,12,13,13,14,15,15,16,16,17,17,18,18,18,19,19,19,19,20,20,20,20,21,21,21,22,22,23]) AS h;

-- "Bolsas" para escoger al azar según el peso
CREATE TEMP TABLE bolsa AS
SELECT (SELECT array_agg(t.id) FROM peso_tienda pt JOIN tienda t ON t.slug = pt.slug, generate_series(1, pt.peso)) AS tiendas,
       (SELECT array_agg(p.id) FROM producto p
                JOIN categoria c ON c.id = p.categoria_id
                JOIN peso_categoria pc ON pc.slug = c.slug,
                generate_series(1, pc.peso + CASE WHEN p.codigo IN ('p035', 'p029', 'p016', 'p043', 'p008') THEN 25 ELSE 0 END)
         WHERE NOT p.es_ejemplo)                                                       AS productos,
       (SELECT array_agg(c.id) FROM categoria c JOIN peso_categoria pc ON pc.slug = c.slug, generate_series(1, pc.peso)) AS categorias,
       (SELECT array_agg(fecha) FROM dia)                                               AS fechas,
       (SELECT array_agg(h) FROM hora)                                                  AS horas;

-- Momento al azar dentro de las 8 semanas (respetando los pesos de días y horas)
CREATE FUNCTION pg_temp.momento() RETURNS timestamp AS $$
    SELECT (b.fechas[1 + floor(random() * array_length(b.fechas, 1))::int]
            + make_interval(hours => b.horas[1 + floor(random() * array_length(b.horas, 1))::int],
                            mins => floor(random() * 60)::int))::timestamp
    FROM bolsa b
$$ LANGUAGE sql VOLATILE;

-- Sesión al azar entre 900; el dispositivo depende de la sesión (68 % celular)
CREATE FUNCTION pg_temp.sesion() RETURNS int AS $$ SELECT 1 + floor(random() * 900)::int $$ LANGUAGE sql VOLATILE;
CREATE FUNCTION pg_temp.dispositivo(s int) RETURNS text AS $$
    SELECT CASE WHEN s % 100 < 68 THEN 'celular' ELSE 'computador' END
$$ LANGUAGE sql IMMUTABLE;

-- ---------------------------------------------------------------------
-- 1 · USUARIOS DE PRUEBA (sin contraseña: no pueden iniciar sesión)
-- ---------------------------------------------------------------------
INSERT INTO usuario (nombre, correo, contrasena_hash, fecha_registro)
SELECT 'Persona de prueba ' || i, 'persona' || i || '@prueba.styleme', 'SIN_ACCESO', current_date - 60
FROM generate_series(1, 80) i;

-- ---------------------------------------------------------------------
-- 2 · EVENTOS
-- ---------------------------------------------------------------------
-- 2.1 Visitas al perfil de una tienda
INSERT INTO evento (tipo, sesion_id, tienda_id, dispositivo, detalle, fecha_hora)
SELECT 'VER_TIENDA', 'prueba-' || s, b.tiendas[1 + floor(random() * array_length(b.tiendas, 1))::int],
       pg_temp.dispositivo(s), '{"prueba": true}', pg_temp.momento()
FROM bolsa b, generate_series(1, 3000) AS g(n), LATERAL (SELECT pg_temp.sesion() + 0 * n AS s) x;

-- 2.2 Clics en WhatsApp: solo tiendas CON WhatsApp, con su tasa propia; pocos minutos después de la visita
INSERT INTO evento (tipo, sesion_id, tienda_id, dispositivo, detalle, fecha_hora)
SELECT 'CLIC_WHATSAPP', e.sesion_id, e.tienda_id, e.dispositivo, '{"prueba": true}',
       e.fecha_hora + make_interval(mins => 1 + floor(random() * 5)::int)
FROM evento e
JOIN tienda t ON t.id = e.tienda_id
JOIN peso_tienda pt ON pt.slug = t.slug
WHERE e.tipo = 'VER_TIENDA' AND e.sesion_id LIKE 'prueba-%' AND t.whatsapp IS NOT NULL
  AND random() + 0 * e.id < pt.tasa_whatsapp;   -- "+ 0 * e.id" obliga a sortear en CADA visita (si no, PostgreSQL sortea una vez por tienda)

-- 2.3 Clics en "cómo llegar": solo tiendas con coordenadas
INSERT INTO evento (tipo, sesion_id, tienda_id, dispositivo, detalle, fecha_hora)
SELECT 'CLIC_COMO_LLEGAR', e.sesion_id, e.tienda_id, e.dispositivo, '{"prueba": true}',
       e.fecha_hora + make_interval(mins => 1 + floor(random() * 8)::int)
FROM evento e
JOIN tienda t ON t.id = e.tienda_id
WHERE e.tipo = 'VER_TIENDA' AND e.sesion_id LIKE 'prueba-%' AND t.lat IS NOT NULL AND random() + 0 * e.id < 0.09;

-- 2.4 Vistas de productos (más skincare y maquillaje; algunos productos son "estrella")
INSERT INTO evento (tipo, sesion_id, producto_id, dispositivo, detalle, fecha_hora)
SELECT 'VER_PRODUCTO', 'prueba-' || s, b.productos[1 + floor(random() * array_length(b.productos, 1))::int],
       pg_temp.dispositivo(s), '{"prueba": true}', pg_temp.momento()
FROM bolsa b, generate_series(1, 4500) AS g(n), LATERAL (SELECT pg_temp.sesion() + 0 * n AS s) x;

-- 2.5 Filtros por categoría en el catálogo
INSERT INTO evento (tipo, sesion_id, categoria_id, dispositivo, detalle, fecha_hora)
SELECT 'FILTRAR_CATEGORIA', 'prueba-' || s, b.categorias[1 + floor(random() * array_length(b.categorias, 1))::int],
       pg_temp.dispositivo(s), '{"prueba": true}', pg_temp.momento()
FROM bolsa b, generate_series(1, 1200) AS g(n), LATERAL (SELECT pg_temp.sesion() + 0 * n AS s) x;

-- 2.6 Búsquedas CON resultados (cosas que sí hay)
INSERT INTO evento (tipo, sesion_id, texto_busqueda, cantidad_resultados, dispositivo, detalle, fecha_hora)
SELECT 'BUSQUEDA', 'prueba-' || s,
       (ARRAY['serum','protector solar','labial','base','shampoo','vestido negro','jean','rubor','pestañina','crema para rizos','acondicionador','tónico'])[1 + floor(random() * 12)::int],
       1 + floor(random() * 6)::int, pg_temp.dispositivo(s), '{"prueba": true}', pg_temp.momento()
FROM generate_series(1, 720) AS g(n), LATERAL (SELECT pg_temp.sesion() + 0 * n AS s) x;

-- 2.7 Búsquedas SIN resultados (demanda que StyleMe no cubre): cada término con su frecuencia
INSERT INTO evento (tipo, sesion_id, texto_busqueda, cantidad_resultados, dispositivo, detalle, fecha_hora)
SELECT 'BUSQUEDA', 'prueba-' || s, termino, 0, pg_temp.dispositivo(s), '{"prueba": true}', pg_temp.momento()
FROM (VALUES ('zapatos', 30), ('perfume', 25), ('bolsos', 22), ('ropa de hombre', 18), ('vestido de baño', 15),
             ('tenis', 14), ('uñas acrílicas', 12), ('gafas de sol', 10), ('esmalte', 9), ('joyería', 8)) AS t(termino, veces),
     generate_series(1, veces) AS g(n),
     LATERAL (SELECT pg_temp.sesion() + 0 * n AS s) x;

-- 2.8 Uso de las herramientas: probador, recomendador, salida de compras y Lía
INSERT INTO evento (tipo, sesion_id, producto_id, dispositivo, detalle, fecha_hora)
SELECT 'USAR_PROBADOR', 'prueba-' || s, p.id, pg_temp.dispositivo(s), '{"prueba": true}', pg_temp.momento()
FROM generate_series(1, 350) AS g(n),
     LATERAL (SELECT pg_temp.sesion() + 0 * n AS s) x,
     LATERAL (SELECT id FROM producto WHERE tipo_prenda IS NOT NULL AND NOT es_ejemplo AND x.s > 0 ORDER BY random() LIMIT 1) p;

INSERT INTO evento (tipo, sesion_id, dispositivo, detalle, fecha_hora)
SELECT 'USAR_RECOMENDADOR', 'prueba-' || s, pg_temp.dispositivo(s),
       jsonb_build_object('prueba', true,
           'rutina', CASE WHEN random() < 0.62 THEN 'skincare' ELSE 'capilar' END,
           'tipo', (ARRAY['grasa','mixta','seca','normal','sensible','rizado','liso','ondulado','afro'])[1 + floor(random() * 9)::int]),
       pg_temp.momento()
FROM generate_series(1, 300) AS g(n), LATERAL (SELECT pg_temp.sesion() + 0 * n AS s) x;

INSERT INTO evento (tipo, sesion_id, dispositivo, detalle, fecha_hora)
SELECT 'ARMAR_SALIDA', 'prueba-' || s, pg_temp.dispositivo(s),
       jsonb_build_object('prueba', true, 'tiendas', 2 + floor(random() * 3)::int), pg_temp.momento()
FROM generate_series(1, 200) AS g(n), LATERAL (SELECT pg_temp.sesion() + 0 * n AS s) x;

INSERT INTO evento (tipo, sesion_id, dispositivo, detalle, fecha_hora)
SELECT 'ABRIR_LIA', 'prueba-' || s, pg_temp.dispositivo(s), '{"prueba": true}', pg_temp.momento()
FROM generate_series(1, 650) AS g(n), LATERAL (SELECT pg_temp.sesion() + 0 * n AS s) x;

-- ---------------------------------------------------------------------
-- 3 · RESEÑAS (una por persona y tienda/producto; ~88 % publicadas, el resto pendiente o rechazada)
-- ---------------------------------------------------------------------
-- 3.1 Reseñas de tiendas: la calificación gira alrededor de la de cada tienda
INSERT INTO resena (usuario_id, tienda_id, corazones, comentario, estado, fecha)
SELECT par.usuario_id, par.tienda_id, c.corazones,
       CASE WHEN c.corazones >= 5 THEN (ARRAY['Excelente atención, volveré.','Me encantó, muy recomendada.','Todo de muy buena calidad.'])[1 + floor(random() * 3)::int]
            WHEN c.corazones = 4 THEN (ARRAY['Muy buena tienda, buenos precios.','Me atendieron bien.','Buena variedad.'])[1 + floor(random() * 3)::int]
            WHEN c.corazones = 3 THEN (ARRAY['Normal, nada especial.','Bien, pero un poco cara.'])[1 + floor(random() * 2)::int]
            ELSE (ARRAY['No tenían lo que vi en la página.','La atención fue lenta.'])[1 + floor(random() * 2)::int] END,
       CASE WHEN random() < 0.88 THEN 'PUBLICADA' WHEN random() < 0.66 THEN 'PENDIENTE' ELSE 'RECHAZADA' END,
       pg_temp.momento()
FROM (SELECT u.id AS usuario_id, t.id AS tienda_id, pt.calificacion
      FROM usuario u CROSS JOIN tienda t JOIN peso_tienda pt ON pt.slug = t.slug
      WHERE u.correo LIKE '%@prueba.styleme'
      ORDER BY random() * (30 - pt.peso)      -- las tiendas populares reciben más reseñas
      LIMIT 140) par,
     LATERAL (SELECT greatest(1, least(5, round(par.calificacion + (random() - 0.5) * 2.4)))::smallint AS corazones) c;

-- 3.2 Reseñas de productos reales
INSERT INTO resena (usuario_id, producto_id, corazones, comentario, estado, fecha)
SELECT par.usuario_id, par.producto_id,
       greatest(1, least(5, round(4.2 + CASE WHEN par.codigo IN ('p035', 'p043', 'p029') THEN 0.5 ELSE 0 END + (random() - 0.5) * 2.2)))::smallint,
       (ARRAY['Funciona muy bien.','Buena relación calidad-precio.','Lo volvería a comprar.','No noté mucha diferencia.'])[1 + floor(random() * 4)::int],
       CASE WHEN random() < 0.9 THEN 'PUBLICADA' ELSE 'PENDIENTE' END,
       pg_temp.momento()
FROM (SELECT u.id AS usuario_id, p.id AS producto_id, p.codigo
      FROM usuario u CROSS JOIN producto p
      WHERE u.correo LIKE '%@prueba.styleme' AND NOT p.es_ejemplo
      ORDER BY random() LIMIT 170) par;

-- ---------------------------------------------------------------------
-- 4 · FAVORITOS (productos y tiendas, sin repetir; los productos "estrella" se guardan más)
-- ---------------------------------------------------------------------
INSERT INTO favorito (usuario_id, producto_id, fecha)
SELECT u.id, p.id, pg_temp.momento()
FROM usuario u CROSS JOIN producto p
WHERE u.correo LIKE '%@prueba.styleme' AND NOT p.es_ejemplo
ORDER BY random() * CASE WHEN p.codigo IN ('p035', 'p029', 'p016', 'p043', 'p008') THEN 0.25 ELSE 1 END
LIMIT 260;

INSERT INTO favorito (usuario_id, tienda_id, fecha)
SELECT u.id, t.id, pg_temp.momento()
FROM usuario u CROSS JOIN tienda t JOIN peso_tienda pt ON pt.slug = t.slug
WHERE u.correo LIKE '%@prueba.styleme'
ORDER BY random() * (30 - pt.peso)
LIMIT 90;

-- ---------------------------------------------------------------------
-- 5 · CONVERSACIONES CON LÍA (solo el resumen, como las reales)
-- ---------------------------------------------------------------------
INSERT INTO conversacion_lia (sesion_id, tema, categoria_id, sentimiento_final, cantidad_mensajes, modo,
                              similitud_promedio, latencia_promedio_ms, tokens_entrada, tokens_salida, inicio, fin)
SELECT 'prueba-lia-' || i, x.tema,
       CASE WHEN x.tema = 'catalogo' THEN b.categorias[1 + floor(random() * array_length(b.categorias, 1))::int] END,
       (ARRAY['positivo','positivo','positivo','neutral','neutral','negativo'])[1 + floor(random() * 6)::int],
       x.mensajes,
       CASE WHEN random() < 0.90 THEN 'IA' WHEN random() < 0.7 THEN 'PLAN_B' ELSE 'MIXTO' END,
       round((0.58 + random() * 0.22)::numeric, 3),
       1500 + floor(random() * 3000)::int,
       x.mensajes * (1900 + floor(random() * 900)::int),
       x.mensajes * (150 + floor(random() * 250)::int),
       x.inicio, x.inicio + make_interval(mins => x.mensajes * (1 + floor(random() * 2)::int))
FROM bolsa b, generate_series(1, 240) i,
     LATERAL (SELECT CASE WHEN r < 0.60 THEN 'catalogo' WHEN r < 0.75 THEN 'social'
                          WHEN r < 0.90 THEN 'general' ELSE 'aclaracion' END AS tema,
                     1 + floor(random() * 6)::int AS mensajes,
                     pg_temp.momento() AS inicio
              FROM (SELECT random() + i * 0 AS r) z) x;

-- Productos recomendados en las conversaciones de compras (de la categoría de la conversación)
INSERT INTO lia_recomendacion (conversacion_id, producto_id, veces)
SELECT c.id, p.id, 1 + floor(random() * 2)::int
FROM conversacion_lia c
CROSS JOIN LATERAL (SELECT id FROM producto
                    WHERE categoria_id = c.categoria_id AND NOT es_ejemplo AND random() >= 0 * c.id
                    ORDER BY random() * CASE WHEN codigo IN ('p035', 'p043', 'p029', 'p016') THEN 0.3 ELSE 1 END
                    LIMIT 1 + floor(random() * 3)::int) p
WHERE c.sesion_id LIKE 'prueba-lia-%' AND c.tema = 'catalogo'
ON CONFLICT DO NOTHING;

DROP TABLE dia, hora, bolsa;

COMMIT;

-- =====================================================================
-- BORRAR LOS DATOS DE PRUEBA (cuando haya datos reales suficientes):
--
-- DELETE FROM evento           WHERE sesion_id LIKE 'prueba-%';
-- DELETE FROM conversacion_lia WHERE sesion_id LIKE 'prueba-%';   -- borra también sus recomendaciones
-- DELETE FROM resena   WHERE usuario_id IN (SELECT id FROM usuario WHERE correo LIKE '%@prueba.styleme');
-- DELETE FROM favorito WHERE usuario_id IN (SELECT id FROM usuario WHERE correo LIKE '%@prueba.styleme');
-- DELETE FROM usuario  WHERE correo LIKE '%@prueba.styleme';
-- =====================================================================
