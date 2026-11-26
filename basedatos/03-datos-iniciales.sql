-- =====================================================================
-- STYLEME · 03 · DATOS INICIALES DEL CATÁLOGO
-- PostgreSQL 17
--
-- El catálogo real de StyleMe: 10 tiendas y 49 productos de Bucaramanga,
-- con sus ingredientes y etiquetas (tipo de piel, objetivos, "libre de"...).
--
-- Origen: los antiguos iu/data/tiendas.js e iu/data/productos.js (tomados de las tiendas el 2026-11-20).
-- Desde la rama feature/iu-api esos archivos ya no existen: ESTE script es la fuente del catálogo.
-- 2026-11-25 (rama feature/catalogo-completo): dirección, coordenadas, teléfono y horario de 6 tiendas
--   tomados de su ficha pública de Google Maps (detalle y nivel de confianza en docs/datos/tiendas-fuentes.csv).
--   Los celulares sin WhatsApp confirmado van en "telefono". Ninguna tienda queda "verificada":
--   verificado = true solo cuando la tienda misma confirma sus datos.
-- Conversión aplicada:
--   · "" y "Por confirmar"  → NULL (en la base, "no se sabe" es NULL)
--   · pendiente: true       → es_ejemplo = true (productos de ejemplo, no reales)
--   · "…" en ingredientes   → se omite (indica que la lista original es más larga)
--   · ids de texto          → tienda.slug y producto.codigo; los id numéricos los genera la base
--
-- Las llaves foráneas se resuelven con subconsultas por slug / código, así el
-- script no depende de qué número le tocó a cada fila.
--
-- Los EMBEDDINGS no van aquí: los calcula el backend con Gemini y los guarda
-- en fragmento_conocimiento.
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
-- 1 · CATEGORÍAS (D-004)
-- ---------------------------------------------------------------------
INSERT INTO categoria (slug, nombre) VALUES
    ('ropa', 'Ropa y moda'),
    ('maquillaje', 'Maquillaje'),
    ('skincare', 'Skincare'),
    ('capilar', 'Cuidado capilar');

-- ---------------------------------------------------------------------
-- 2 · ZONAS
-- ---------------------------------------------------------------------
INSERT INTO zona (nombre) VALUES
    ('Cabecera'),
    ('Sur'),
    ('La Aurora'),
    ('Antonia Santos');

-- ---------------------------------------------------------------------
-- 3 · TIENDAS (10)
-- ---------------------------------------------------------------------
INSERT INTO tienda (slug, nombre, descripcion, categoria_principal_id, zona_id, direccion, lat, lng,
                    horario, whatsapp, telefono, instagram, web, foto, verificado)
VALUES ('samylu-boutique', 'Samylu Boutique', 'Boutique de ropa para mujer en Cabecera del Llano.',
        (SELECT id FROM categoria WHERE slug = 'ropa'),
        (SELECT id FROM zona WHERE nombre = 'Cabecera'),
        'Carrera 37 #42-75, Cabecera del Llano', 7.1209, -73.1088,
        NULL, NULL, NULL, NULL, NULL, 'assets/img/tiendas/samylu-boutique.webp', false);

INSERT INTO tienda (slug, nombre, descripcion, categoria_principal_id, zona_id, direccion, lat, lng,
                    horario, whatsapp, telefono, instagram, web, foto, verificado)
VALUES ('bohor', 'BOHOR', 'Tienda de ropa y moda.',
        (SELECT id FROM categoria WHERE slug = 'ropa'),
        (SELECT id FROM zona WHERE nombre = 'Cabecera'),
        'Carrera 36 #53-32, Cabecera del Llano', 7.1118, -73.108,
        'Lun–Sáb 10:00–19:00 · Dom 11:00–16:00', '573012951439', NULL, NULL, 'bohor.com.co', 'assets/img/tiendas/bohor.jpg', false);

INSERT INTO tienda (slug, nombre, descripcion, categoria_principal_id, zona_id, direccion, lat, lng,
                    horario, whatsapp, telefono, instagram, web, foto, verificado)
VALUES ('queen', 'Queen', 'Ropa de segunda mano en Cabecera del Llano.',
        (SELECT id FROM categoria WHERE slug = 'ropa'),
        (SELECT id FROM zona WHERE nombre = 'Cabecera'),
        'Carrera 35 #52-146, Cabecera del Llano', 7.1121, -73.1091,
        'Lun–Sáb 9:30–11:30 y 14:30–18:30 · Dom cerrado', NULL, '6076571340', NULL, NULL, 'assets/img/tiendas/queen.webp', false);

INSERT INTO tienda (slug, nombre, descripcion, categoria_principal_id, zona_id, direccion, lat, lng,
                    horario, whatsapp, telefono, instagram, web, foto, verificado)
VALUES ('henars-eleganzza', 'Henar''s y Eleganzza', 'Ropa de alta costura para dama en tallas grandes, de la diseñadora Sandra Soto.',
        (SELECT id FROM categoria WHERE slug = 'ropa'),
        NULL,
        'Carrera 22 #45-13', 7.116554, -73.119093,
        'Lun–Sáb 9:00–19:00 · Dom cerrado', NULL, '3176756806', NULL, NULL, 'assets/img/tiendas/henars-eleganzza.webp', false);

INSERT INTO tienda (slug, nombre, descripcion, categoria_principal_id, zona_id, direccion, lat, lng,
                    horario, whatsapp, telefono, instagram, web, foto, verificado)
VALUES ('mattelsa', 'Mattelsa', 'Marca colombiana de ropa casual. Sede La Aurora.',
        (SELECT id FROM categoria WHERE slug = 'ropa'),
        (SELECT id FROM zona WHERE nombre = 'La Aurora'),
        'Carrera 31 #32-45, La Aurora', 7.126, -73.1146,
        'Lun–Sáb 10:00–20:00 · Dom 10:00–18:00', '573005071000', NULL, NULL, NULL, 'assets/img/tiendas/mattelsa.webp', false);

INSERT INTO tienda (slug, nombre, descripcion, categoria_principal_id, zona_id, direccion, lat, lng,
                    horario, whatsapp, telefono, instagram, web, foto, verificado)
VALUES ('pandora-makeup', 'Pandora Makeup', 'Tienda de maquillaje en Cabecera.',
        (SELECT id FROM categoria WHERE slug = 'maquillaje'),
        (SELECT id FROM zona WHERE nombre = 'Cabecera'),
        'Carrera 33 #46-59, Cabecera del Llano', 7.117588, -73.111067,
        'Lun–Sáb 10:00–18:50 · Dom 10:00–15:50', '573213273357', NULL, NULL, 'pandoramaquillaje.com', 'assets/img/tiendas/pandora-makeup.png', false);

INSERT INTO tienda (slug, nombre, descripcion, categoria_principal_id, zona_id, direccion, lat, lng,
                    horario, whatsapp, telefono, instagram, web, foto, verificado)
VALUES ('guacamaya-colors', 'Guacamaya Colors', 'Maquillaje y skincare nacional e importado.',
        (SELECT id FROM categoria WHERE slug = 'maquillaje'),
        (SELECT id FROM zona WHERE nombre = 'Cabecera'),
        'Carrera 36 #51-120, Cabecera del Llano', 7.113889, -73.108132,
        'Lun–Sáb 10:00–19:00 · Dom cerrado', NULL, '3008370913', NULL, 'guacamayacolors.com', 'assets/img/tiendas/guacamaya-colors.png', false);

INSERT INTO tienda (slug, nombre, descripcion, categoria_principal_id, zona_id, direccion, lat, lng,
                    horario, whatsapp, telefono, instagram, web, foto, verificado)
VALUES ('gogo-makeup', 'Gogó Makeup', 'Tienda de maquillaje en Cabecera.',
        (SELECT id FROM categoria WHERE slug = 'maquillaje'),
        (SELECT id FROM zona WHERE nombre = 'Cabecera'),
        'Carrera 35 #52-103, Cabecera del Llano', 7.112519, -73.109001,
        'Lun–Sáb 9:00–19:00 · Dom 10:00–18:00', NULL, '3172785281', NULL, NULL, 'assets/img/tiendas/gogo-makeup.jpg', false);

INSERT INTO tienda (slug, nombre, descripcion, categoria_principal_id, zona_id, direccion, lat, lng,
                    horario, whatsapp, telefono, instagram, web, foto, verificado)
VALUES ('so-beauty', 'So Beauty Maquillaje', 'Maquillaje al detal y por mayor, con más de 600 referencias.',
        (SELECT id FROM categoria WHERE slug = 'maquillaje'),
        (SELECT id FROM zona WHERE nombre = 'Cabecera'),
        'Carrera 36 #52-28, local 1, Cabecera del Llano', 7.113199, -73.108244,
        'Lun–Sáb 10:00–19:00 · Dom cerrado', NULL, '3174691111', 'sobeautymaquillajebga', NULL, 'assets/img/tiendas/so-beauty.jpg', false);

INSERT INTO tienda (slug, nombre, descripcion, categoria_principal_id, zona_id, direccion, lat, lng,
                    horario, whatsapp, telefono, instagram, web, foto, verificado)
VALUES ('mirada-de-angel', 'Mirada de Ángel', 'Cosméticos, maquillaje y cuidado capilar.',
        (SELECT id FROM categoria WHERE slug = 'maquillaje'),
        (SELECT id FROM zona WHERE nombre = 'Antonia Santos'),
        'Carrera 23 #35-14, local 1, Edificio Zentri, Antonia Santos', 7.120725, -73.120225,
        'Lun–Vie 8:30–18:30 · Sáb 9:00–17:00 · Dom 9:00–15:00', '573222209159', NULL, NULL, 'miradadeangel.com.co', 'assets/img/tiendas/mirada-de-angel.jpg', false);

-- ---------------------------------------------------------------------
-- 4 · PRODUCTOS (49)
-- ---------------------------------------------------------------------
-- ROPA (14)
INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p001', (SELECT id FROM tienda WHERE slug = 'bohor'), (SELECT id FROM categoria WHERE slug = 'ropa'),
        'Blusa Tango Negro', NULL, 185000, 'Blusa entallada de manga larga con cortes en el escote y lazo posterior ajustable. Negra, tallas S, M y L.',
        'https://cdn.shopify.com/s/files/1/0605/2066/1217/files/rn-image_picker_lib_temp_9d4a4497-bea2-4aef-b55b-e80aed0f47ab.jpg?v=1791409853&width=500',
        'https://bohor.com.co/products/blusa-tango-negro',
        NULL, 'superior', NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p002', (SELECT id FROM tienda WHERE slug = 'bohor'), (SELECT id FROM categoria WHERE slug = 'ropa'),
        'Jean Serenata', NULL, 260000, 'Jean de denim oscuro, silueta amplia y tiro bajo, con bolsillos curvos estampados con ondas blancas. Tallas S a XL.',
        'https://cdn.shopify.com/s/files/1/0605/2066/1217/files/Ecomerce00161-1.jpg?v=1785342954&width=500',
        'https://bohor.com.co/products/jean-serenata',
        NULL, 'inferior', NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p003', (SELECT id FROM tienda WHERE slug = 'bohor'), (SELECT id FROM categoria WHERE slug = 'ropa'),
        'Falda Bochorno Negra', NULL, 170000, 'Falda midi recta translúcida con falda corta interior de licra, tela liviana. Negra, tallas S y M.',
        'https://cdn.shopify.com/s/files/1/0605/2066/1217/files/rn-image_picker_lib_temp_17a7840b-a2ea-4cfe-b33c-c868887580be.jpg?v=1791404749&width=500',
        'https://bohor.com.co/products/falda-bochorno-negro',
        NULL, 'inferior', NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p004', (SELECT id FROM tienda WHERE slug = 'bohor'), (SELECT id FROM categoria WHERE slug = 'ropa'),
        'Vestido Media Noche Negro', NULL, 295000, 'Vestido de dos piezas (top bustier con lazo y sobreprenda con falda abullonada) en satín ligero. Negro, tallas S/M y M/L.',
        'https://cdn.shopify.com/s/files/1/0605/2066/1217/files/rn-image_picker_lib_temp_fc18c1e0-34dd-42a2-880d-fcd4d646215f.jpg?v=1791403852&width=500',
        'https://bohor.com.co/products/vestido-media-noche-negro',
        NULL, 'completa', NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p005', (SELECT id FROM tienda WHERE slug = 'mattelsa'), (SELECT id FROM categoria WHERE slug = 'ropa'),
        'Body Negro Básico', NULL, 69500, 'Body de algodón (95% algodón, 5% spandex) con escote en U y espalda descubierta. Negro, tallas S a XL.',
        'https://b2cmattelsa.vteximg.com.br/arquivos/ids/751177-500-auto/95711-1.jpg?v=639300280901430000',
        'https://www.mattelsa.net/body-negro-basico-95711/p',
        NULL, 'superior', NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p006', (SELECT id FROM tienda WHERE slug = 'mattelsa'), (SELECT id FROM categoria WHERE slug = 'ropa'),
        'Jean Wide Leg Tiro Alto Azul Medio', NULL, 99500, 'Jean de tiro alto en denim 100% algodón, con pinzas y bordado en el bolsillo posterior. Azul medio.',
        'https://b2cmattelsa.vteximg.com.br/arquivos/ids/742022-500-auto/90372-1.jpg?v=639239816341470000',
        'https://www.mattelsa.net/jeans-wide-leg-tiro-alto-azul-medio-lavanderia-90372/p',
        NULL, 'inferior', NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p007', (SELECT id FROM tienda WHERE slug = 'mattelsa'), (SELECT id FROM categoria WHERE slug = 'ropa'),
        'Pantalón Straight Fit Tiro Alto Negro', NULL, 109500, 'Pantalón recto de tiro alto, 100% algodón. Negro.',
        'https://b2cmattelsa.vteximg.com.br/arquivos/ids/732475-500-auto/Pantalon-Straight-Fit-Tiro-Alto-Negro-Basico-Talla-S.jpg?v=639172964753070000',
        'https://www.mattelsa.net/pantalon-straight-fit-tiro-alto-negro-basico-92275/p',
        NULL, 'inferior', NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p008', (SELECT id FROM tienda WHERE slug = 'mattelsa'), (SELECT id FROM categoria WHERE slug = 'ropa'),
        'Vestido Negro Ícono', NULL, 74500, 'Vestido de manga larga en jersey (93% poliamida, 7% elastano), doble tela y abertura en la espalda. Negro, tallas S a XL.',
        'https://b2cmattelsa.vteximg.com.br/arquivos/ids/753697-500-auto/Vestido-Negro-Icono-Talla-S.jpg?v=639300281962730000',
        'https://www.mattelsa.net/vestido-negro-icono-95719/p',
        NULL, 'completa', NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p009', (SELECT id FROM tienda WHERE slug = 'samylu-boutique'), (SELECT id FROM categoria WHERE slug = 'ropa'),
        'Blusa (ejemplo)', NULL, NULL, NULL,
        NULL,
        NULL,
        NULL, 'superior', NULL, true);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p010', (SELECT id FROM tienda WHERE slug = 'samylu-boutique'), (SELECT id FROM categoria WHERE slug = 'ropa'),
        'Vestido (ejemplo)', NULL, NULL, NULL,
        NULL,
        NULL,
        NULL, 'completa', NULL, true);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p011', (SELECT id FROM tienda WHERE slug = 'queen'), (SELECT id FROM categoria WHERE slug = 'ropa'),
        'Chaqueta (ejemplo)', NULL, NULL, NULL,
        NULL,
        NULL,
        NULL, 'superior', NULL, true);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p012', (SELECT id FROM tienda WHERE slug = 'queen'), (SELECT id FROM categoria WHERE slug = 'ropa'),
        'Falda (ejemplo)', NULL, NULL, NULL,
        NULL,
        NULL,
        NULL, 'inferior', NULL, true);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p013', (SELECT id FROM tienda WHERE slug = 'henars-eleganzza'), (SELECT id FROM categoria WHERE slug = 'ropa'),
        'Blusa (ejemplo)', NULL, NULL, NULL,
        NULL,
        NULL,
        NULL, 'superior', NULL, true);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p014', (SELECT id FROM tienda WHERE slug = 'henars-eleganzza'), (SELECT id FROM categoria WHERE slug = 'ropa'),
        'Vestido (ejemplo)', NULL, NULL, NULL,
        NULL,
        NULL,
        NULL, 'completa', NULL, true);

-- MAQUILLAJE (13)
INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p015', (SELECT id FROM tienda WHERE slug = 'pandora-makeup'), (SELECT id FROM categoria WHERE slug = 'maquillaje'),
        'Base Pro Coverage', 'L.A. Girl', 43700, 'Base líquida ligera de alta cobertura y larga duración, con antioxidantes.',
        'https://pandoramaquillaje.com/wp-content/uploads/2024/09/w800h800fitpad-300x300.avif',
        'https://pandoramaquillaje.com/producto/base-pro-coverage-la-girl/',
        'https://incidecoder.com/products/la-girl-pro-coverage-foundation', NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p016', (SELECT id FROM tienda WHERE slug = 'pandora-makeup'), (SELECT id FROM categoria WHERE slug = 'maquillaje'),
        'Labial SuperStay Matte Ink', 'Maybelline', 65000, 'Labial líquido mate que no se transfiere y dura hasta 16 horas, con aplicador en flecha.',
        'https://pandoramaquillaje.com/wp-content/uploads/2024/12/nuevo-equipo-3-300x300.png',
        'https://pandoramaquillaje.com/producto/labial-super-stay-matte-maybelline/',
        'https://incidecoder.com/products/maybelline-superstay-matte-ink-liquid-lipstick', NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p017', (SELECT id FROM tienda WHERE slug = 'pandora-makeup'), (SELECT id FROM categoria WHERE slug = 'maquillaje'),
        'Pestañina Lash Sensational', 'Maybelline', 63000, 'Pestañina con cepillo en abanico de seis tipos de cerdas, para volumen efecto abanico.',
        'https://pandoramaquillaje.com/wp-content/uploads/2025/02/IMG_9292-300x300.webp',
        'https://pandoramaquillaje.com/producto/pestanina-lash-sensational-maybelline/',
        'https://incidecoder.com/products/maybelline-lash-sensational-r-washable-mascara', NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p018', (SELECT id FROM tienda WHERE slug = 'guacamaya-colors'), (SELECT id FROM categoria WHERE slug = 'maquillaje'),
        'Base Skinfinite Hidratante', 'SHEGLAM', 57900, 'Base cremosa de cobertura media, hidratante y ligera, con acabado natural luminoso.',
        'https://cdn.shopify.com/s/files/1/0557/7421/6384/files/WhatsAppImage2023-11-17at7.23.29PM.jpg?v=1700267075&width=500',
        'https://guacamayacolors.com/products/base-de-maquillaje-hidratante-skinfinite-sheglam',
        'https://incidecoder.com/products/sheglam-skinfinite-hydrating-foundation', NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p019', (SELECT id FROM tienda WHERE slug = 'guacamaya-colors'), (SELECT id FROM categoria WHERE slug = 'maquillaje'),
        'Pestañina All-in-One Volume & Length', 'SHEGLAM', 42000, 'Pestañina de doble punta con dos fórmulas: volumen, largo y separación sin grumos.',
        'https://cdn.shopify.com/s/files/1/0557/7421/6384/files/WhatsAppImage2023-10-13at4.20.22PM.jpg?v=1697232601&width=500',
        'https://guacamayacolors.com/products/copia-de-all-in-one-volume-length-mascara-sheglam',
        'https://incidecoder.com/products/sheglam-all-in-one-volume-length-mascara', NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p020', (SELECT id FROM tienda WHERE slug = 'guacamaya-colors'), (SELECT id FROM categoria WHERE slug = 'maquillaje'),
        'Rubor Líquido Color Bloom Mate', 'SHEGLAM', 33000, 'Rubor líquido en gel-crema con vitamina C y aplicador de esponja, de larga duración.',
        'https://cdn.shopify.com/s/files/1/0557/7421/6384/files/WhatsAppImage2024-07-06at8.45.12AM.jpg?v=1720273696&width=500',
        'https://guacamayacolors.com/products/color-bloom-liquid-blush-matte-finish-sheglam',
        'https://www.sheglam.com/product/Color%20Bloom%20Liquid%20Blush%20Matte%20Finish-Love-Cake-7957494104229', NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p021', (SELECT id FROM tienda WHERE slug = 'mirada-de-angel'), (SELECT id FROM categoria WHERE slug = 'maquillaje'),
        'Iluminador Rosa', 'Ruby Rose', 20800, 'Iluminador en polvo muy pigmentado, acabado satinado, para pómulos y puntos de luz (10 g).',
        'https://miradadeangel.com.co/wp-content/uploads/2021/02/ruby1-min-300x300.jpg',
        'https://miradadeangel.com.co/maquillaje/iluminador-rosa-ruby-rose/',
        'https://miradadeangel.com.co/maquillaje/iluminador-rosa-ruby-rose/', NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p022', (SELECT id FROM tienda WHERE slug = 'mirada-de-angel'), (SELECT id FROM categoria WHERE slug = 'maquillaje'),
        'Polvo Compacto Efecto Total 6', 'Vogue', 10000, 'Polvo compacto 6 en 1 de acabado mate y alta cobertura, con filtro solar y aceite de argán.',
        'https://miradadeangel.com.co/wp-content/uploads/2020/04/polvo-vogue-porcelana-300x300.jpg',
        'https://miradadeangel.com.co/maquillaje/polvo-compacto-efecto-total-6-vogue-cosmeticos/',
        NULL, NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p023', (SELECT id FROM tienda WHERE slug = 'mirada-de-angel'), (SELECT id FROM categoria WHERE slug = 'maquillaje'),
        'Base Detox and Protect (05 Honey Beige)', 'Note Cosmetics', 86990, 'Base líquida SPF 15 sin parabenos, con aceite de almendras, alga y vitamina E (35 ml).',
        'https://miradadeangel.com.co/wp-content/uploads/2022/11/base-detox-honey-05-300x300.png',
        'https://miradadeangel.com.co/maquillaje/base-piel-mixta-note-detox-and-protect-foundation/',
        NULL, NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p024', (SELECT id FROM tienda WHERE slug = 'so-beauty'), (SELECT id FROM categoria WHERE slug = 'maquillaje'),
        'Base Skin Hidratante', 'Samy', 32000, 'Base de maquillaje hidratante.',
        NULL,
        'https://drive.google.com/file/d/1TQo1pG98jX_rKKAmnefeF3Fp9vrIHQuv/view',
        NULL, NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p025', (SELECT id FROM tienda WHERE slug = 'so-beauty'), (SELECT id FROM categoria WHERE slug = 'maquillaje'),
        'Rubor Hydro Blush', 'Ruby Rose', 25000, 'Rubor de Ruby Rose.',
        NULL,
        'https://drive.google.com/file/d/1TQo1pG98jX_rKKAmnefeF3Fp9vrIHQuv/view',
        NULL, NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p026', (SELECT id FROM tienda WHERE slug = 'so-beauty'), (SELECT id FROM categoria WHERE slug = 'maquillaje'),
        'Tinta para Labios Skin', 'Samy', 25000, 'Tinta para labios.',
        NULL,
        'https://drive.google.com/file/d/1TQo1pG98jX_rKKAmnefeF3Fp9vrIHQuv/view',
        NULL, NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p027', (SELECT id FROM tienda WHERE slug = 'gogo-makeup'), (SELECT id FROM categoria WHERE slug = 'maquillaje'),
        'Máscara de pestañas (ejemplo)', NULL, NULL, NULL,
        NULL,
        NULL,
        NULL, NULL, NULL, true);

-- SKINCARE (12)
INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p028', (SELECT id FROM tienda WHERE slug = 'pandora-makeup'), (SELECT id FROM categoria WHERE slug = 'skincare'),
        'Sérum Madagascar Centella Ampoule 55 ml', 'SKIN1004', 85001, 'Sérum de centella asiática que calma la piel sensible, fortalece la barrera e hidrata.',
        'https://pandoramaquillaje.com/wp-content/uploads/2026/10/SERUM-CETELLA-300x300.jpg',
        'https://pandoramaquillaje.com/producto/serum-ampolla-centella-skin1004-55-ml/',
        'https://incidecoder.com/products/skin1004-madagascar-centella-ampoule', NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p029', (SELECT id FROM tienda WHERE slug = 'pandora-makeup'), (SELECT id FROM categoria WHERE slug = 'skincare'),
        'Protector Solar Hyalu-Cica Water-Fit SPF 50+', 'SKIN1004', 95000, 'Protector solar coreano en sérum, de amplio espectro, que hidrata y deja la piel luminosa.',
        'https://pandoramaquillaje.com/wp-content/uploads/2026/10/8809576261592_1-300x300.png',
        'https://pandoramaquillaje.com/producto/protector-solar-hyalu-cica-water-fit-skin1004-50ml/',
        'https://incidecoder.com/products/skin1004-hyalu-cica-water-fit-sun-serum', NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p030', (SELECT id FROM tienda WHERE slug = 'pandora-makeup'), (SELECT id FROM categoria WHERE slug = 'skincare'),
        'Crema 345 Relief Cream', 'Dr. Althea', 120000, 'Crema calmante que hidrata y repara la barrera de la piel sensible o irritada.',
        'https://pandoramaquillaje.com/wp-content/uploads/2026/06/51dWGrl3-dL-300x300.jpg',
        'https://pandoramaquillaje.com/producto/crema-reparadora-dr-althea-345-relief-cream/',
        'https://incidecoder.com/products/dr-althea-345-relief-cream', NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p031', (SELECT id FROM tienda WHERE slug = 'guacamaya-colors'), (SELECT id FROM categoria WHERE slug = 'skincare'),
        'BHA Clarify Gel Cream', 'Good Molecules', 83900, 'Gel-crema ligero con ácido salicílico, té verde y centella que calma e hidrata la piel con brotes.',
        'https://cdn.shopify.com/s/files/1/0557/7421/6384/files/WhatsAppImage2024-05-03at5.01.47PM_2.jpg?v=1714773861&width=500',
        'https://guacamayacolors.com/products/bha-clarify-gel-cream-good-molecules',
        'https://incidecoder.com/products/good-molecules-bha-clarify-gel-cream', NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p032', (SELECT id FROM tienda WHERE slug = 'guacamaya-colors'), (SELECT id FROM categoria WHERE slug = 'skincare'),
        'Desmaquillante Dual 260 ml', 'Samy', 43900, 'Desmaquillante bifásico con aloe que retira el maquillaje de rostro y labios, incluso el a prueba de agua.',
        'https://cdn.shopify.com/s/files/1/0557/7421/6384/files/WhatsAppImage2024-04-02at5.45.39PM_5.jpg?v=1712099261&width=500',
        'https://guacamayacolors.com/products/desmaquillante-dual-samy',
        'https://incidecoder.com/products/samy-desmaquillante-dual', NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p033', (SELECT id FROM tienda WHERE slug = 'mirada-de-angel'), (SELECT id FROM categoria WHERE slug = 'skincare'),
        'Jabón Facial Detox 160 g', 'Trendy', 16000, 'Limpiador facial con carbón activado y ácido salicílico que controla la grasa y previene imperfecciones.',
        'https://miradadeangel.com.co/wp-content/uploads/2023/05/jabon-facial-detox-300x300.png',
        'https://miradadeangel.com.co/maquillaje/rostro/jabon-facial-detox-trendy-160g/',
        'https://miradadeangel.com.co/maquillaje/rostro/jabon-facial-detox-trendy-160g/', NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p034', (SELECT id FROM tienda WHERE slug = 'mirada-de-angel'), (SELECT id FROM categoria WHERE slug = 'skincare'),
        'Tónico Ácido Hialurónico', 'Bioaqua', 18000, 'Tónico hidratante y refrescante con ácido hialurónico que ayuda a retener el agua en la piel.',
        'https://miradadeangel.com.co/wp-content/uploads/2022/01/Tonico-acido-hialuronico-bioaqua-min-300x300.jpg',
        'https://miradadeangel.com.co/facial/tonico-acido-hialuronico-bioaqua/',
        NULL, NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p035', (SELECT id FROM tienda WHERE slug = 'mirada-de-angel'), (SELECT id FROM categoria WHERE slug = 'skincare'),
        'Suero Detox Niacinamida + Zinc', 'Trendy', 32000, 'Sérum con niacinamida al 10%, zinc y pantenol que regula la grasa y minimiza los poros.',
        'https://miradadeangel.com.co/wp-content/uploads/2022/05/suero-niamicidas-min-300x300.jpg',
        'https://miradadeangel.com.co/facial/suero-detox-niamicida-zinc-trendy/',
        'https://miradadeangel.com.co/facial/suero-detox-niamicida-zinc-trendy/', NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p036', (SELECT id FROM tienda WHERE slug = 'so-beauty'), (SELECT id FROM categoria WHERE slug = 'skincare'),
        'Agua Micelar', 'Samy', 18000, 'Agua micelar para limpiar y desmaquillar el rostro.',
        NULL,
        'https://drive.google.com/file/d/1TQo1pG98jX_rKKAmnefeF3Fp9vrIHQuv/view',
        NULL, NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p037', (SELECT id FROM tienda WHERE slug = 'so-beauty'), (SELECT id FROM categoria WHERE slug = 'skincare'),
        'Sérum Control Oleosidad', 'Samy', 35000, 'Sérum facial para controlar la grasa.',
        NULL,
        'https://drive.google.com/file/d/1TQo1pG98jX_rKKAmnefeF3Fp9vrIHQuv/view',
        NULL, NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p038', (SELECT id FROM tienda WHERE slug = 'so-beauty'), (SELECT id FROM categoria WHERE slug = 'skincare'),
        'Protector Solar Facial', 'Ana María', 65000, 'Protector solar para el rostro.',
        NULL,
        'https://drive.google.com/file/d/1TQo1pG98jX_rKKAmnefeF3Fp9vrIHQuv/view',
        NULL, NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p039', (SELECT id FROM tienda WHERE slug = 'gogo-makeup'), (SELECT id FROM categoria WHERE slug = 'skincare'),
        'Protector solar (ejemplo)', NULL, NULL, NULL,
        NULL,
        NULL,
        NULL, NULL, NULL, true);

-- CAPILAR (10)
INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p040', (SELECT id FROM tienda WHERE slug = 'mirada-de-angel'), (SELECT id FROM categoria WHERE slug = 'capilar'),
        'Shampoo en Crema Sin Sulfatos', 'Cantu', 49900, 'Shampoo en crema sin sulfatos para cabello ondulado, rizado y afro, que limpia y humecta.',
        'https://miradadeangel.com.co/wp-content/uploads/2022/02/shampoo-cream-cantu-min-300x300.jpg',
        'https://miradadeangel.com.co/capilar/cantu-sulfate-free-cleansing-cream-shampoo/',
        'https://miradadeangel.com.co/capilar/cantu-sulfate-free-cleansing-cream-shampoo/', NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p041', (SELECT id FROM tienda WHERE slug = 'mirada-de-angel'), (SELECT id FROM categoria WHERE slug = 'capilar'),
        'Acondicionador Hidratante Sin Sulfatos', 'Cantu', 49900, 'Acondicionador con manteca de karité que hidrata el cabello seco y quebradizo y reduce el quiebre.',
        'https://miradadeangel.com.co/wp-content/uploads/2022/02/acondicionador-cantu-min-300x300.jpg',
        'https://miradadeangel.com.co/capilar/cantu-sulfate-free-hydrating-cream-conditioner/',
        'https://miradadeangel.com.co/capilar/cantu-sulfate-free-hydrating-cream-conditioner/', NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p042', (SELECT id FROM tienda WHERE slug = 'mirada-de-angel'), (SELECT id FROM categoria WHERE slug = 'capilar'),
        'Mascarilla Deep Treatment Masque', 'Cantu', 70000, 'Mascarilla de acondicionamiento profundo con karité y aceites que hidrata, fortalece y repara los rizos.',
        'https://miradadeangel.com.co/wp-content/uploads/2022/05/ScreenShot2023-07-17at1.02.58AM_1024x1024-1-300x300.webp',
        'https://miradadeangel.com.co/capilar/deep-treatment-masque-cantu/',
        'https://incidecoder.com/products/cantu-shea-butter-for-natural-hair-deep-treatment-masque', NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p043', (SELECT id FROM tienda WHERE slug = 'mirada-de-angel'), (SELECT id FROM categoria WHERE slug = 'capilar'),
        'Crema Activadora de Rizos', 'Cantu', 59900, 'Crema activadora de rizos con karité que define, da volumen y controla el frizz.',
        'https://miradadeangel.com.co/wp-content/uploads/2022/02/activador-curl-cantu-min-300x300.jpg',
        'https://miradadeangel.com.co/capilar/cantu-moisturizing-curl-activator-cream/',
        'https://miradadeangel.com.co/capilar/cantu-moisturizing-curl-activator-cream/', NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p044', (SELECT id FROM tienda WHERE slug = 'mirada-de-angel'), (SELECT id FROM categoria WHERE slug = 'capilar'),
        'Crema Divino Potão Mais Crespo 2 en 1 (1000 g)', 'Skala', 39900, 'Crema 2 en 1 (tratamiento o crema para peinar sin enjuague) para cabello crespo o en transición, con ricino, argán y queratina vegetal.',
        'https://miradadeangel.com.co/wp-content/uploads/2023/04/divino-potato.png',
        'https://miradadeangel.com.co/capilar/skala-expert-crema-divino-potao-crespo-2-en-1-x-1000gr/',
        NULL, NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p045', (SELECT id FROM tienda WHERE slug = 'mirada-de-angel'), (SELECT id FROM categoria WHERE slug = 'capilar'),
        'Shampoo Liss Control', 'Salon In', 30000, 'Shampoo suave sin sal con extracto de lichi, proteína de seda y queratina para prolongar el alisado.',
        'https://miradadeangel.com.co/wp-content/uploads/2021/08/SHAMPOO-CONTROL-SALON-min-300x300.jpg',
        'https://miradadeangel.com.co/capilar/shampoo-liss-control-salon-in/',
        NULL, NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p046', (SELECT id FROM tienda WHERE slug = 'pandora-makeup'), (SELECT id FROM categoria WHERE slug = 'capilar'),
        'Shampoo Control Grasa', 'La Poción', 39900, 'Shampoo con bicarbonato de sodio y bioactivos marinos que regula la grasa y purifica el cuero cabelludo.',
        'https://pandoramaquillaje.com/wp-content/uploads/2025/11/172676-800-auto-300x300.webp',
        'https://pandoramaquillaje.com/producto/shampoo-control-grasa-la-pocion/',
        NULL, NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p047', (SELECT id FROM tienda WHERE slug = 'pandora-makeup'), (SELECT id FROM categoria WHERE slug = 'capilar'),
        'Crema para Peinar Dual', 'La Poción', 35000, 'Crema para peinar que desenreda, sella la cutícula y controla el frizz sin dejar peso.',
        'https://pandoramaquillaje.com/wp-content/uploads/2025/11/crema-de-peinar-la-pocion-dual-x440ml-4h00203-300x300.webp',
        'https://pandoramaquillaje.com/producto/crema-para-peinar-dual-la-pocion/',
        NULL, NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p048', (SELECT id FROM tienda WHERE slug = 'guacamaya-colors'), (SELECT id FROM categoria WHERE slug = 'capilar'),
        'Keratina Líquida Sublim Protect', 'Byphasse', 33900, 'Queratina líquida que revitaliza el cabello seco y le devuelve brillo, suavidad y cuerpo.',
        'https://cdn.shopify.com/s/files/1/0557/7421/6384/files/imagnesdeespecialparatifondoblanco-2025-09-18T173220.395.jpg?v=1758234978',
        'https://guacamayacolors.com/products/keratina-liquida-byphasse',
        'https://incidecoder.com/products/byphasse-liquid-keratin-sublim-protect-dry-hair', NULL, NULL, false);

INSERT INTO producto (codigo, tienda_id, categoria_id, nombre, marca, precio, descripcion, foto, fuente,
                      fuente_ingredientes, tipo_prenda, foto_probador, es_ejemplo)
VALUES ('p049', (SELECT id FROM tienda WHERE slug = 'guacamaya-colors'), (SELECT id FROM categoria WHERE slug = 'capilar'),
        'Gel de Fijación got2b Mini 35 g', 'got2b (Schwarzkopf)', 37000, 'Gel de fijación extrema resistente al agua y al sudor, de fórmula vegana.',
        'https://cdn.shopify.com/s/files/1/0557/7421/6384/files/WhatsAppImage2024-04-04at12.54.04PM_6.jpg?v=1712263944&width=500',
        'https://guacamayacolors.com/products/gel-got2b-mini-35g',
        NULL, NULL, NULL, false);

-- ---------------------------------------------------------------------
-- 5 · INGREDIENTES (183 únicos, en el orden en que aparecen)
-- ---------------------------------------------------------------------
INSERT INTO ingrediente (nombre) VALUES
    ('Water (Aqua)'),
    ('Cyclopentasiloxane'),
    ('Phenyl Trimethicone'),
    ('Cyclohexasiloxane'),
    ('Glycerin'),
    ('Isododecane'),
    ('Propylene Glycol'),
    ('Cetyl PEG/PPG-10/1 Dimethicone'),
    ('Hydrogenated Polyisobutene'),
    ('Polycyclopentadiene'),
    ('Tribehenin'),
    ('Tocopheryl Acetate'),
    ('Ascorbyl Palmitate'),
    ('Retinyl Palmitate'),
    ('Fragrance (Parfum)'),
    ('Dimethicone'),
    ('Trimethylsiloxysilicate'),
    ('Nylon-611/Dimethicone Copolymer'),
    ('Dimethicone Crosspolymer'),
    ('C30-45 Alkyldimethylsilyl Polypropylsilsesquioxane'),
    ('Lauroyl Lysine'),
    ('Alumina'),
    ('Silica Silylate'),
    ('Disodium Stearoyl Glutamate'),
    ('Phenoxyethanol'),
    ('Caprylyl Glycol'),
    ('Parfum/Fragrance'),
    ('pigmentos (varían según el tono)'),
    ('Aqua/Water'),
    ('Styrene/Acrylates/Ammonium Methacrylate Copolymer'),
    ('Polyurethane-35'),
    ('Cera Alba (Beeswax)'),
    ('Synthetic Fluorphlogopite'),
    ('Glyceryl Stearate'),
    ('Cetyl Alcohol'),
    ('PEG-200 Glyceryl Stearate'),
    ('Copernicia Cerifera Cera'),
    ('Stearic Acid'),
    ('Alcohol Denat'),
    ('Paraffin'),
    ('Sodium Laureth Sulfate'),
    ('Iron Oxides (CI 77499)'),
    ('Butylene Glycol'),
    ('Tridecyl Trimellitate'),
    ('Caprylic/Capric Triglyceride'),
    ('Ethylhexyl Palmitate'),
    ('Mica'),
    ('Titanium Dioxide (CI 77891)'),
    ('Iron Oxides'),
    ('Disteardimonium Hectorite'),
    ('Ceresin'),
    ('Talc'),
    ('Microcrystalline Wax'),
    ('Alcohol'),
    ('Dextrin Palmitate/Ethylhexanoate'),
    ('Polysorbate 60'),
    ('Polyethylene'),
    ('Cera Alba'),
    ('Water/Aqua'),
    ('Isohexadecane'),
    ('Isononyl Isononanoate'),
    ('Vinyl Dimethicone/Methicone Silsesquioxane Crosspolymer'),
    ('Fragrance/Parfum'),
    ('3-O-Ethyl Ascorbic Acid'),
    ('Tocopherol'),
    ('pigmentos'),
    ('Agua'),
    ('Glicerina'),
    ('Ciclometicona'),
    ('Isononanoato'),
    ('Goma xantana'),
    ('Aceite de ricino etoxilado hidrogenado'),
    ('Silicato de aluminio y magnesio'),
    ('Cuarzo'),
    ('Fenoxietanol'),
    ('Poliacrilamida'),
    ('Water'),
    ('Centella Asiatica Extract'),
    ('1,2-Hexanediol'),
    ('Cellulose Gum'),
    ('Ethylhexylglycerin'),
    ('Purified Water'),
    ('Dibutyl Adipate'),
    ('Propanediol'),
    ('Diethylamino Hydroxybenzoyl Hexyl Benzoate'),
    ('Polymethylsilsesquioxane'),
    ('Ethylhexyl Triazone'),
    ('Methylene Bis-Benzotriazolyl Tetramethylbutylphenol'),
    ('Niacinamide'),
    ('Coco-Caprylate/Caprate'),
    ('Caprylyl Methicone'),
    ('Diethylhexyl Butamido Triazone'),
    ('Panthenol'),
    ('Polyglycerin-3'),
    ('Polyglyceryl-10 Stearate'),
    ('Centella Asiatica Leaf Extract'),
    ('Sodium Hyaluronate'),
    ('Ceramide NP'),
    ('Resveratrol'),
    ('Adenosine'),
    ('Diheptyl Succinate'),
    ('Ammonium Acryloyldimethyltaurate/VP Copolymer'),
    ('Salicylic Acid (0,4%)'),
    ('Tromethamine'),
    ('Allantoin'),
    ('Bisabolol'),
    ('Punica Granatum Seed Oil'),
    ('Vitis Vinifera Seed Oil'),
    ('Camellia Sinensis Leaf Extract'),
    ('Aqua'),
    ('Aloe Barbadensis Extract'),
    ('Sodium Chloride'),
    ('Imidazolidinyl Urea'),
    ('Benzalkonium Chloride'),
    ('EDTA'),
    ('CI 16035'),
    ('CI 19140'),
    ('CI 14720'),
    ('CI 42090'),
    ('Carbón activado'),
    ('Ácido salicílico'),
    ('Seboreductyl'),
    ('Pantenol (vitamina B5)'),
    ('Extractos de frambuesa, fresa, kiwi, durazno, papaya y pepino'),
    ('Niacinamide (10%)'),
    ('Zinc'),
    ('Extracto de té verde'),
    ('Extracto de café verde'),
    ('Extracto de cacao'),
    ('Sodium Lauroyl Methyl Isethionate'),
    ('Cocamidopropyl Betaine'),
    ('Acrylates Copolymer'),
    ('Sodium Lauroyl Sarcosinate'),
    ('PEG/PPG-120/10 Trimethylolpropane Trioleate'),
    ('Laureth-2'),
    ('Cocamide MIPA'),
    ('Citric Acid'),
    ('Fragrance'),
    ('Polyester-11'),
    ('Glycol Distearate'),
    ('Tetrasodium EDTA'),
    ('Sodium Hydroxide'),
    ('PPG-20 Methyl Glucose Ether'),
    ('Polyquaternium-10'),
    ('Sodium PCA'),
    ('Stearyl Alcohol'),
    ('Butyrospermum Parkii (Shea) Butter'),
    ('Stearamidopropyl Dimethylamine'),
    ('Neopentyl Glycol Diheptanoate'),
    ('Lactic Acid'),
    ('Disodium EDTA'),
    ('Benzyl Benzoate'),
    ('Canola Oil'),
    ('Cetearyl Alcohol'),
    ('Ceteareth-20'),
    ('Beeswax'),
    ('Glycol Stearate'),
    ('Cocos Nucifera Oil'),
    ('Persea Gratissima Oil'),
    ('Argania Spinosa Kernel Oil'),
    ('Silk Amino Acids'),
    ('Stearalkonium Chloride'),
    ('Coconut Oil'),
    ('Shea Butter'),
    ('Aloe Vera Leaf Juice'),
    ('Sweet Almond Oil'),
    ('Avocado Oil'),
    ('Argan Kernel Oil'),
    ('Polysorbate-60'),
    ('PEG-40 Hydrogenated Castor Oil'),
    ('Polyquaternium-6'),
    ('Parfum'),
    ('Hydrolyzed Keratin'),
    ('Polyquaternium-11'),
    ('2-Bromo-2-Nitropropane-1,3-Diol'),
    ('Potassium Sorbate'),
    ('Amyl Cinnamal'),
    ('Linalool'),
    ('Benzyl Salicylate'),
    ('Butylphenyl Methylpropional'),
    ('Limonene'),
    ('Hydroxycitronellal'),
    ('CI 15985');

-- ---------------------------------------------------------------------
-- 6 · PRODUCTO ↔ INGREDIENTE (265 relaciones; orden = posición en la etiqueta)
-- ---------------------------------------------------------------------
INSERT INTO producto_ingrediente (producto_id, ingrediente_id, orden)
SELECT p.id, i.id, v.orden
FROM (VALUES
    ('p015', 'Water (Aqua)', 1),
    ('p015', 'Cyclopentasiloxane', 2),
    ('p015', 'Phenyl Trimethicone', 3),
    ('p015', 'Cyclohexasiloxane', 4),
    ('p015', 'Glycerin', 5),
    ('p015', 'Isododecane', 6),
    ('p015', 'Propylene Glycol', 7),
    ('p015', 'Cetyl PEG/PPG-10/1 Dimethicone', 8),
    ('p015', 'Hydrogenated Polyisobutene', 9),
    ('p015', 'Polycyclopentadiene', 10),
    ('p015', 'Tribehenin', 11),
    ('p015', 'Tocopheryl Acetate', 12),
    ('p015', 'Ascorbyl Palmitate', 13),
    ('p015', 'Retinyl Palmitate', 14),
    ('p015', 'Fragrance (Parfum)', 15),
    ('p016', 'Dimethicone', 1),
    ('p016', 'Trimethylsiloxysilicate', 2),
    ('p016', 'Isododecane', 3),
    ('p016', 'Nylon-611/Dimethicone Copolymer', 4),
    ('p016', 'Dimethicone Crosspolymer', 5),
    ('p016', 'C30-45 Alkyldimethylsilyl Polypropylsilsesquioxane', 6),
    ('p016', 'Lauroyl Lysine', 7),
    ('p016', 'Alumina', 8),
    ('p016', 'Silica Silylate', 9),
    ('p016', 'Disodium Stearoyl Glutamate', 10),
    ('p016', 'Phenoxyethanol', 11),
    ('p016', 'Caprylyl Glycol', 12),
    ('p016', 'Parfum/Fragrance', 13),
    ('p016', 'pigmentos (varían según el tono)', 14),
    ('p017', 'Aqua/Water', 1),
    ('p017', 'Propylene Glycol', 2),
    ('p017', 'Styrene/Acrylates/Ammonium Methacrylate Copolymer', 3),
    ('p017', 'Polyurethane-35', 4),
    ('p017', 'Cera Alba (Beeswax)', 5),
    ('p017', 'Synthetic Fluorphlogopite', 6),
    ('p017', 'Glyceryl Stearate', 7),
    ('p017', 'Cetyl Alcohol', 8),
    ('p017', 'PEG-200 Glyceryl Stearate', 9),
    ('p017', 'Copernicia Cerifera Cera', 10),
    ('p017', 'Stearic Acid', 11),
    ('p017', 'Alcohol Denat', 12),
    ('p017', 'Paraffin', 13),
    ('p017', 'Sodium Laureth Sulfate', 14),
    ('p017', 'Iron Oxides (CI 77499)', 15),
    ('p018', 'Aqua/Water', 1),
    ('p018', 'Cyclopentasiloxane', 2),
    ('p018', 'Glycerin', 3),
    ('p018', 'Isododecane', 4),
    ('p018', 'Trimethylsiloxysilicate', 5),
    ('p018', 'Phenyl Trimethicone', 6),
    ('p018', 'Butylene Glycol', 7),
    ('p018', 'Cetyl PEG/PPG-10/1 Dimethicone', 8),
    ('p018', 'Tridecyl Trimellitate', 9),
    ('p018', 'Caprylic/Capric Triglyceride', 10),
    ('p018', 'Ethylhexyl Palmitate', 11),
    ('p018', 'Mica', 12),
    ('p018', 'Titanium Dioxide (CI 77891)', 13),
    ('p018', 'Iron Oxides', 14),
    ('p019', 'Isododecane', 1),
    ('p019', 'Trimethylsiloxysilicate', 2),
    ('p019', 'Hydrogenated Polyisobutene', 3),
    ('p019', 'Disteardimonium Hectorite', 4),
    ('p019', 'Ceresin', 5),
    ('p019', 'Talc', 6),
    ('p019', 'Microcrystalline Wax', 7),
    ('p019', 'Alcohol', 8),
    ('p019', 'Dextrin Palmitate/Ethylhexanoate', 9),
    ('p019', 'Polysorbate 60', 10),
    ('p019', 'Polyethylene', 11),
    ('p019', 'Cera Alba', 12),
    ('p019', 'Phenoxyethanol', 13),
    ('p019', 'Iron Oxides (CI 77499)', 14),
    ('p020', 'Water/Aqua', 1),
    ('p020', 'Isododecane', 2),
    ('p020', 'Isohexadecane', 3),
    ('p020', 'Isononyl Isononanoate', 4),
    ('p020', 'Tridecyl Trimellitate', 5),
    ('p020', 'Propylene Glycol', 6),
    ('p020', 'Trimethylsiloxysilicate', 7),
    ('p020', 'Cetyl PEG/PPG-10/1 Dimethicone', 8),
    ('p020', 'Vinyl Dimethicone/Methicone Silsesquioxane Crosspolymer', 9),
    ('p020', 'Dimethicone', 10),
    ('p020', 'Glycerin', 11),
    ('p020', 'Fragrance/Parfum', 12),
    ('p020', '3-O-Ethyl Ascorbic Acid', 13),
    ('p020', 'Tocopherol', 14),
    ('p020', 'pigmentos', 15),
    ('p021', 'Agua', 1),
    ('p021', 'Mica', 2),
    ('p021', 'Glicerina', 3),
    ('p021', 'Ciclometicona', 4),
    ('p021', 'Isononanoato', 5),
    ('p021', 'Goma xantana', 6),
    ('p021', 'Aceite de ricino etoxilado hidrogenado', 7),
    ('p021', 'Silicato de aluminio y magnesio', 8),
    ('p021', 'Cuarzo', 9),
    ('p021', 'Fenoxietanol', 10),
    ('p021', 'Poliacrilamida', 11),
    ('p028', 'Water', 1),
    ('p028', 'Glycerin', 2),
    ('p028', 'Butylene Glycol', 3),
    ('p028', 'Centella Asiatica Extract', 4),
    ('p028', '1,2-Hexanediol', 5),
    ('p028', 'Cellulose Gum', 6),
    ('p028', 'Ethylhexylglycerin', 7),
    ('p029', 'Purified Water', 1),
    ('p029', 'Dibutyl Adipate', 2),
    ('p029', 'Propanediol', 3),
    ('p029', 'Diethylamino Hydroxybenzoyl Hexyl Benzoate', 4),
    ('p029', 'Polymethylsilsesquioxane', 5),
    ('p029', 'Ethylhexyl Triazone', 6),
    ('p029', 'Methylene Bis-Benzotriazolyl Tetramethylbutylphenol', 7),
    ('p029', 'Niacinamide', 8),
    ('p029', 'Coco-Caprylate/Caprate', 9),
    ('p029', 'Caprylyl Methicone', 10),
    ('p029', 'Diethylhexyl Butamido Triazone', 11),
    ('p029', 'Glycerin', 12),
    ('p029', '1,2-Hexanediol', 13),
    ('p029', 'Butylene Glycol', 14),
    ('p029', 'Centella Asiatica Extract', 15),
    ('p030', 'Water', 1),
    ('p030', 'Propanediol', 2),
    ('p030', 'Glycerin', 3),
    ('p030', 'Cyclohexasiloxane', 4),
    ('p030', '1,2-Hexanediol', 5),
    ('p030', 'Caprylic/Capric Triglyceride', 6),
    ('p030', 'Niacinamide', 7),
    ('p030', 'Panthenol', 8),
    ('p030', 'Polyglycerin-3', 9),
    ('p030', 'Polyglyceryl-10 Stearate', 10),
    ('p030', 'Centella Asiatica Leaf Extract', 11),
    ('p030', 'Sodium Hyaluronate', 12),
    ('p030', 'Ceramide NP', 13),
    ('p030', 'Resveratrol', 14),
    ('p030', 'Adenosine', 15),
    ('p031', 'Water', 1),
    ('p031', 'Propanediol', 2),
    ('p031', 'Diheptyl Succinate', 3),
    ('p031', 'Glycerin', 4),
    ('p031', 'Ammonium Acryloyldimethyltaurate/VP Copolymer', 5),
    ('p031', 'Salicylic Acid (0,4%)', 6),
    ('p031', 'Phenoxyethanol', 7),
    ('p031', 'Caprylyl Glycol', 8),
    ('p031', 'Tromethamine', 9),
    ('p031', 'Ethylhexylglycerin', 10),
    ('p031', 'Allantoin', 11),
    ('p031', 'Bisabolol', 12),
    ('p031', 'Punica Granatum Seed Oil', 13),
    ('p031', 'Vitis Vinifera Seed Oil', 14),
    ('p031', 'Centella Asiatica Extract', 15),
    ('p031', 'Camellia Sinensis Leaf Extract', 16),
    ('p032', 'Cyclopentasiloxane', 1),
    ('p032', 'Cyclohexasiloxane', 2),
    ('p032', 'Aqua', 3),
    ('p032', 'Aloe Barbadensis Extract', 4),
    ('p032', 'Propylene Glycol', 5),
    ('p032', 'Sodium Chloride', 6),
    ('p032', 'Imidazolidinyl Urea', 7),
    ('p032', 'Benzalkonium Chloride', 8),
    ('p032', 'EDTA', 9),
    ('p032', 'CI 16035', 10),
    ('p032', 'CI 19140', 11),
    ('p032', 'CI 14720', 12),
    ('p032', 'CI 42090', 13),
    ('p033', 'Carbón activado', 1),
    ('p033', 'Ácido salicílico', 2),
    ('p033', 'Seboreductyl', 3),
    ('p033', 'Pantenol (vitamina B5)', 4),
    ('p033', 'Extractos de frambuesa, fresa, kiwi, durazno, papaya y pepino', 5),
    ('p035', 'Niacinamide (10%)', 1),
    ('p035', 'Zinc', 2),
    ('p035', 'Panthenol', 3),
    ('p035', 'Extracto de té verde', 4),
    ('p035', 'Extracto de café verde', 5),
    ('p035', 'Extracto de cacao', 6),
    ('p040', 'Water', 1),
    ('p040', 'Sodium Lauroyl Methyl Isethionate', 2),
    ('p040', 'Cocamidopropyl Betaine', 3),
    ('p040', 'Acrylates Copolymer', 4),
    ('p040', 'Sodium Lauroyl Sarcosinate', 5),
    ('p040', 'PEG/PPG-120/10 Trimethylolpropane Trioleate', 6),
    ('p040', 'Laureth-2', 7),
    ('p040', 'Cocamide MIPA', 8),
    ('p040', 'Citric Acid', 9),
    ('p040', 'Panthenol', 10),
    ('p040', 'Fragrance', 11),
    ('p040', 'Polyester-11', 12),
    ('p040', 'Glycol Distearate', 13),
    ('p040', 'Tetrasodium EDTA', 14),
    ('p040', 'Sodium Hydroxide', 15),
    ('p040', 'PPG-20 Methyl Glucose Ether', 16),
    ('p040', 'Polyquaternium-10', 17),
    ('p040', 'Sodium PCA', 18),
    ('p040', 'Phenoxyethanol', 19),
    ('p040', 'Ethylhexylglycerin', 20),
    ('p041', 'Water', 1),
    ('p041', 'Stearyl Alcohol', 2),
    ('p041', 'Cetyl Alcohol', 3),
    ('p041', 'Butyrospermum Parkii (Shea) Butter', 4),
    ('p041', 'Stearamidopropyl Dimethylamine', 5),
    ('p041', 'Glyceryl Stearate', 6),
    ('p041', 'Phenoxyethanol', 7),
    ('p041', 'Fragrance', 8),
    ('p041', 'Neopentyl Glycol Diheptanoate', 9),
    ('p041', 'Lactic Acid', 10),
    ('p041', 'Isododecane', 11),
    ('p041', 'Ethylhexylglycerin', 12),
    ('p041', 'Polyester-11', 13),
    ('p041', 'Disodium EDTA', 14),
    ('p041', 'Benzyl Benzoate', 15),
    ('p042', 'Water', 1),
    ('p042', 'Canola Oil', 2),
    ('p042', 'Cetearyl Alcohol', 3),
    ('p042', 'Ceteareth-20', 4),
    ('p042', 'Glycerin', 5),
    ('p042', 'Fragrance (Parfum)', 6),
    ('p042', 'Beeswax', 7),
    ('p042', 'Cetyl Alcohol', 8),
    ('p042', 'Butyrospermum Parkii (Shea) Butter', 9),
    ('p042', 'Glycol Stearate', 10),
    ('p042', 'Polyquaternium-10', 11),
    ('p042', 'Cocos Nucifera Oil', 12),
    ('p042', 'Persea Gratissima Oil', 13),
    ('p042', 'Argania Spinosa Kernel Oil', 14),
    ('p042', 'Silk Amino Acids', 15),
    ('p042', 'Phenoxyethanol', 16),
    ('p042', 'Ethylhexylglycerin', 17),
    ('p043', 'Water', 1),
    ('p043', 'Glycerin', 2),
    ('p043', 'Propanediol', 3),
    ('p043', 'Fragrance', 4),
    ('p043', 'Polyquaternium-10', 5),
    ('p043', 'Stearalkonium Chloride', 6),
    ('p043', 'Stearyl Alcohol', 7),
    ('p043', 'Coconut Oil', 8),
    ('p043', 'Shea Butter', 9),
    ('p043', 'Aloe Vera Leaf Juice', 10),
    ('p043', 'Sweet Almond Oil', 11),
    ('p043', 'Avocado Oil', 12),
    ('p043', 'Argan Kernel Oil', 13),
    ('p043', 'Silk Amino Acids', 14),
    ('p043', 'Cetyl Alcohol', 15),
    ('p043', 'Cetearyl Alcohol', 16),
    ('p043', 'Polysorbate-60', 17),
    ('p043', 'Phenoxyethanol', 18),
    ('p043', 'Ethylhexylglycerin', 19),
    ('p048', 'Aqua', 1),
    ('p048', 'Alcohol Denat', 2),
    ('p048', 'PEG-40 Hydrogenated Castor Oil', 3),
    ('p048', 'Polyquaternium-6', 4),
    ('p048', 'Parfum', 5),
    ('p048', 'Hydrolyzed Keratin', 6),
    ('p048', 'Polyquaternium-11', 7),
    ('p048', '2-Bromo-2-Nitropropane-1,3-Diol', 8),
    ('p048', 'Phenoxyethanol', 9),
    ('p048', 'Citric Acid', 10),
    ('p048', 'Potassium Sorbate', 11),
    ('p048', 'Ethylhexylglycerin', 12),
    ('p048', 'Amyl Cinnamal', 13),
    ('p048', 'Linalool', 14),
    ('p048', 'Benzyl Salicylate', 15),
    ('p048', 'Butylphenyl Methylpropional', 16),
    ('p048', 'Limonene', 17),
    ('p048', 'Hydroxycitronellal', 18),
    ('p048', 'CI 15985', 19)
) AS v (codigo, ingrediente, orden)
JOIN producto    p ON p.codigo = v.codigo
JOIN ingrediente i ON i.nombre = v.ingrediente;

-- ---------------------------------------------------------------------
-- 7 · ETIQUETAS (36) · grupo + valor
-- ---------------------------------------------------------------------
INSERT INTO etiqueta (grupo, valor) VALUES
    ('paso', 'serum'),
    ('paso', 'protector'),
    ('paso', 'hidratante'),
    ('paso', 'limpiador'),
    ('paso', 'tonico'),
    ('paso', 'shampoo'),
    ('paso', 'acondicionador'),
    ('paso', 'tratamiento'),
    ('paso', 'peinar'),
    ('tipo_piel', 'todas'),
    ('tipo_piel', 'sensible'),
    ('tipo_piel', 'seca'),
    ('tipo_piel', 'normal'),
    ('tipo_piel', 'mixta'),
    ('tipo_piel', 'grasa'),
    ('tipo_cabello', 'ondulado'),
    ('tipo_cabello', 'rizado'),
    ('tipo_cabello', 'afro'),
    ('tipo_cabello', 'liso'),
    ('tipo_cabello', 'todos'),
    ('cuero', 'todos'),
    ('cuero', 'graso'),
    ('objetivo', 'hidratar'),
    ('objetivo', 'proteger'),
    ('objetivo', 'manchas'),
    ('objetivo', 'imperfecciones'),
    ('objetivo', 'grasa'),
    ('objetivo', 'rizos'),
    ('objetivo', 'reparar'),
    ('objetivo', 'fortalecer'),
    ('objetivo', 'frizz'),
    ('libre_de', 'fragancia'),
    ('libre_de', 'alcohol'),
    ('libre_de', 'parabenos'),
    ('libre_de', 'sulfatos'),
    ('libre_de', 'siliconas');

-- ---------------------------------------------------------------------
-- 8 · PRODUCTO ↔ ETIQUETA (143 relaciones)
-- ---------------------------------------------------------------------
INSERT INTO producto_etiqueta (producto_id, etiqueta_id)
SELECT p.id, e.id
FROM (VALUES
    ('p028', 'paso', 'serum'),
    ('p028', 'tipo_piel', 'todas'),
    ('p028', 'objetivo', 'hidratar'),
    ('p028', 'libre_de', 'fragancia'),
    ('p028', 'libre_de', 'alcohol'),
    ('p028', 'libre_de', 'parabenos'),
    ('p028', 'libre_de', 'sulfatos'),
    ('p028', 'libre_de', 'siliconas'),
    ('p029', 'paso', 'protector'),
    ('p029', 'tipo_piel', 'todas'),
    ('p029', 'objetivo', 'proteger'),
    ('p029', 'objetivo', 'manchas'),
    ('p029', 'objetivo', 'hidratar'),
    ('p030', 'paso', 'hidratante'),
    ('p030', 'tipo_piel', 'sensible'),
    ('p030', 'tipo_piel', 'seca'),
    ('p030', 'objetivo', 'hidratar'),
    ('p030', 'libre_de', 'fragancia'),
    ('p030', 'libre_de', 'alcohol'),
    ('p030', 'libre_de', 'parabenos'),
    ('p030', 'libre_de', 'sulfatos'),
    ('p031', 'paso', 'hidratante'),
    ('p031', 'tipo_piel', 'todas'),
    ('p031', 'objetivo', 'imperfecciones'),
    ('p031', 'objetivo', 'grasa'),
    ('p031', 'objetivo', 'hidratar'),
    ('p031', 'libre_de', 'fragancia'),
    ('p031', 'libre_de', 'parabenos'),
    ('p031', 'libre_de', 'sulfatos'),
    ('p031', 'libre_de', 'siliconas'),
    ('p032', 'paso', 'limpiador'),
    ('p032', 'tipo_piel', 'todas'),
    ('p032', 'libre_de', 'fragancia'),
    ('p032', 'libre_de', 'alcohol'),
    ('p032', 'libre_de', 'parabenos'),
    ('p032', 'libre_de', 'sulfatos'),
    ('p033', 'paso', 'limpiador'),
    ('p033', 'tipo_piel', 'normal'),
    ('p033', 'tipo_piel', 'mixta'),
    ('p033', 'tipo_piel', 'grasa'),
    ('p033', 'objetivo', 'grasa'),
    ('p033', 'objetivo', 'imperfecciones'),
    ('p034', 'paso', 'tonico'),
    ('p034', 'tipo_piel', 'todas'),
    ('p034', 'objetivo', 'hidratar'),
    ('p035', 'paso', 'serum'),
    ('p035', 'tipo_piel', 'mixta'),
    ('p035', 'tipo_piel', 'grasa'),
    ('p035', 'objetivo', 'grasa'),
    ('p035', 'objetivo', 'imperfecciones'),
    ('p035', 'objetivo', 'hidratar'),
    ('p036', 'paso', 'limpiador'),
    ('p036', 'tipo_piel', 'todas'),
    ('p036', 'objetivo', 'hidratar'),
    ('p037', 'paso', 'serum'),
    ('p037', 'tipo_piel', 'grasa'),
    ('p037', 'tipo_piel', 'mixta'),
    ('p037', 'objetivo', 'grasa'),
    ('p038', 'paso', 'protector'),
    ('p038', 'tipo_piel', 'todas'),
    ('p038', 'objetivo', 'proteger'),
    ('p039', 'paso', 'protector'),
    ('p039', 'tipo_piel', 'todas'),
    ('p039', 'objetivo', 'proteger'),
    ('p039', 'objetivo', 'manchas'),
    ('p040', 'paso', 'shampoo'),
    ('p040', 'tipo_cabello', 'ondulado'),
    ('p040', 'tipo_cabello', 'rizado'),
    ('p040', 'tipo_cabello', 'afro'),
    ('p040', 'cuero', 'todos'),
    ('p040', 'objetivo', 'hidratar'),
    ('p040', 'objetivo', 'rizos'),
    ('p040', 'libre_de', 'sulfatos'),
    ('p040', 'libre_de', 'parabenos'),
    ('p040', 'libre_de', 'siliconas'),
    ('p041', 'paso', 'acondicionador'),
    ('p041', 'tipo_cabello', 'ondulado'),
    ('p041', 'tipo_cabello', 'rizado'),
    ('p041', 'tipo_cabello', 'afro'),
    ('p041', 'cuero', 'todos'),
    ('p041', 'objetivo', 'hidratar'),
    ('p041', 'objetivo', 'reparar'),
    ('p041', 'libre_de', 'sulfatos'),
    ('p041', 'libre_de', 'parabenos'),
    ('p041', 'libre_de', 'siliconas'),
    ('p042', 'paso', 'tratamiento'),
    ('p042', 'tipo_cabello', 'rizado'),
    ('p042', 'tipo_cabello', 'afro'),
    ('p042', 'cuero', 'todos'),
    ('p042', 'objetivo', 'hidratar'),
    ('p042', 'objetivo', 'fortalecer'),
    ('p042', 'objetivo', 'reparar'),
    ('p042', 'objetivo', 'rizos'),
    ('p042', 'libre_de', 'sulfatos'),
    ('p042', 'libre_de', 'parabenos'),
    ('p042', 'libre_de', 'siliconas'),
    ('p043', 'paso', 'peinar'),
    ('p043', 'tipo_cabello', 'ondulado'),
    ('p043', 'tipo_cabello', 'rizado'),
    ('p043', 'tipo_cabello', 'afro'),
    ('p043', 'cuero', 'todos'),
    ('p043', 'objetivo', 'rizos'),
    ('p043', 'objetivo', 'frizz'),
    ('p043', 'objetivo', 'hidratar'),
    ('p043', 'libre_de', 'sulfatos'),
    ('p043', 'libre_de', 'parabenos'),
    ('p043', 'libre_de', 'siliconas'),
    ('p044', 'paso', 'peinar'),
    ('p044', 'tipo_cabello', 'rizado'),
    ('p044', 'tipo_cabello', 'afro'),
    ('p044', 'cuero', 'todos'),
    ('p044', 'objetivo', 'reparar'),
    ('p044', 'objetivo', 'frizz'),
    ('p044', 'objetivo', 'hidratar'),
    ('p044', 'objetivo', 'rizos'),
    ('p044', 'libre_de', 'sulfatos'),
    ('p044', 'libre_de', 'parabenos'),
    ('p044', 'libre_de', 'siliconas'),
    ('p045', 'paso', 'shampoo'),
    ('p045', 'tipo_cabello', 'liso'),
    ('p045', 'cuero', 'todos'),
    ('p045', 'objetivo', 'hidratar'),
    ('p045', 'objetivo', 'frizz'),
    ('p046', 'paso', 'shampoo'),
    ('p046', 'tipo_cabello', 'todos'),
    ('p046', 'cuero', 'graso'),
    ('p046', 'objetivo', 'grasa'),
    ('p047', 'paso', 'peinar'),
    ('p047', 'tipo_cabello', 'todos'),
    ('p047', 'cuero', 'todos'),
    ('p047', 'objetivo', 'frizz'),
    ('p047', 'objetivo', 'hidratar'),
    ('p048', 'paso', 'tratamiento'),
    ('p048', 'tipo_cabello', 'todos'),
    ('p048', 'cuero', 'todos'),
    ('p048', 'objetivo', 'hidratar'),
    ('p048', 'objetivo', 'reparar'),
    ('p048', 'libre_de', 'parabenos'),
    ('p048', 'libre_de', 'sulfatos'),
    ('p048', 'libre_de', 'siliconas'),
    ('p049', 'paso', 'peinar'),
    ('p049', 'tipo_cabello', 'todos'),
    ('p049', 'cuero', 'todos')
) AS v (codigo, grupo, valor)
JOIN producto p ON p.codigo = v.codigo
JOIN etiqueta e ON e.grupo = v.grupo AND e.valor = v.valor;

COMMIT;
