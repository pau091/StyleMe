-- =====================================================================
-- STYLEME · 06 · FOTOS DE PRODUCTOS, PRODUCTOS DE MUESTRA Y CONTACTOS
-- PostgreSQL 17 · 2026-11-26 (rama style/iu-visual)
--
-- Se ejecuta solo, después del 03, cuando Docker crea la base por primera vez.
-- En una base que ya existe se aplica con (no borra nada más y se puede repetir):
--   docker exec styleme-basedatos psql -U styleme -d styleme -f /docker-entrypoint-initdb.d/06-fotos-y-muestras.sql
-- Después hay que reindexar los embeddings (panel de administración → "Reindexar embeddings").
--
-- Qué hace:
--  1. FOTOS de 21 productos reales que no tenían foto (So Beauty) o cuya foto estaba bloqueada
--     (la página de Mirada de Ángel muestra "This image was hotlinked" en cualquier otro sitio).
--     Las fotos son del producto de la misma marca, tomadas de la marca o de tiendas colombianas
--     que permiten mostrarlas (Samy, Ruby Rose, Note, Falabella, Olímpica, La Rebaja).
--     Nombre y precio NO cambian: son los que publica cada tienda de StyleMe.
--  2. PRODUCTOS DE MUESTRA para las tiendas sin catálogo en línea (Samylu, Queen, Henar's y Gogó):
--     reemplazan a los 8 productos "de ejemplo". Son ILUSTRATIVOS (decisión de la autora para la
--     presentación): nombre, foto y precio inventados (fotos libres de Unsplash o del producto real de
--     la marca). Los precios de la ropa son de referencia para una boutique de Bucaramanga; los de
--     Gogó son los de esos productos en tiendas colombianas (La Rebaja, Olímpica).
--  3. SE BORRA p022 "Polvo Compacto Efecto Total 6" (Vogue): no existe un polvo compacto con ese
--     nombre ("Efecto Total" es una pestañina) y no se encontró información para corregirlo.
--  4. p035 (Suero Detox Niacinamida + Zinc): su foto de la tienda está bloqueada y no hay otra pública
--     del producto, así que lleva una foto ILUSTRATIVA de un sérum con gotero (Unsplash, sin marca).
--  5. CONTACTOS de las tiendas encontrados en su página, Linktree o directorios (confianza alta o media).
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
-- 1 · Fotos de productos reales (solo cambia la foto)
-- ---------------------------------------------------------------------
UPDATE producto AS p SET foto = f.foto
FROM (VALUES
    -- So Beauty Maquillaje (Samy, Ruby Rose, Ana María)
    ('p024', 'https://samycosmetics.vteximg.com.br/arquivos/ids/159372-600-600/SKNLCS_Base-Liquida--01.jpg'),
    ('p025', 'https://cdn.shopify.com/s/files/1/0277/4833/5703/files/BLUSH_PINK.webp?v=1785940403&width=600'),
    ('p026', 'https://samycosmetics.vteximg.com.br/arquivos/ids/159291-600-600/Tono_01_Peach_Frente.jpg'),
    ('p036', 'https://samycosmetics.vteximg.com.br/arquivos/ids/156081-600-600/062999-1.jpg'),
    ('p037', 'https://samycosmetics.vteximg.com.br/arquivos/ids/159599-600-600/frasco.jpg'),
    ('p038', 'https://olimpica.vteximg.com.br/arquivos/ids/1895431-600-600/image-a000ff15188340fbb18b30efe465c9d6.jpg'),
    -- Mirada de Ángel (Ruby Rose, Note, Trendy, Bioaqua, Cantu, Skala, Salon In)
    ('p021', 'https://cdn.shopify.com/s/files/1/0277/4833/5703/files/PINK_CHAMPAGNE.webp?v=1785958877&width=600'),
    ('p023', 'https://cdn.shopify.com/s/files/1/0069/1070/5725/products/detox-_-protect-foundation--spf-15-05-honey-beige.jpg?v=1676489311&width=600'),
    ('p033', 'https://media.falabella.com.co/falabellaCO/143120052_01/w=600,h=600,fit=pad'),
    ('p034', 'https://media.falabella.com.co/falabellaCO/131582618_01/w=600,h=600,fit=pad'),
    ('p040', 'https://media.falabella.com.co/falabellaCO/121767557_01/w=600,h=600,fit=pad'),
    ('p041', 'https://media.falabella.com.co/falabellaCO/121767558_1/w=600,h=600,fit=pad'),
    ('p042', 'https://media.falabella.com.co/falabellaCO/121767585_01/w=600,h=600,fit=pad'),
    ('p043', 'https://media.falabella.com.co/falabellaCO/121767550_01/w=600,h=600,fit=pad'),
    ('p044', 'https://olimpica.vteximg.com.br/arquivos/ids/2151300-600-600/image-01219de011f54f5dab01695ba9f487d4.jpg'),
    ('p045', 'https://olimpica.vteximg.com.br/arquivos/ids/2299925-600-600/image-02251f6a95754839b65c382a35411132.jpg'),
    -- Foto ilustrativa (la del producto en la tienda está bloqueada y no hay otra pública)
    ('p035', 'https://images.unsplash.com/photo-1576426863848-c21f53c60b19?w=700&q=80&auto=format&fit=crop')
) AS f(codigo, foto)
WHERE p.codigo = f.codigo;

-- ---------------------------------------------------------------------
-- 2 · Productos de muestra (reemplazan a los 8 "de ejemplo")
-- ---------------------------------------------------------------------
UPDATE producto AS p
SET nombre = m.nombre, marca = m.marca, precio = v.precio, descripcion = m.descripcion,
    foto = m.foto, fuente = m.fuente, tipo_prenda = m.tipo_prenda, es_ejemplo = false
FROM (VALUES
    -- Samylu Boutique (ropa para mujer)
    ('p009', 'Blusa Blanca Bordada con Flores', NULL,
     'Blusa blanca de algodón con bordado floral, para un look fresco y femenino.',
     'https://images.unsplash.com/photo-1564257631407-4deb1f99d992?w=700&q=80&auto=format&fit=crop',
     'https://unsplash.com/photos/womens-white-and-multicolored-floral-top-gbq6pdUX6EY', 'superior'),
    ('p010', 'Vestido Camisero Mostaza con Cinturón', NULL,
     'Vestido camisero sin mangas color mostaza, con cinturón anudado y falda con vuelo.',
     'https://images.unsplash.com/photo-1789111161022-1970ba92f3b9?w=700&q=80&auto=format&fit=crop',
     'https://unsplash.com/photos/jz7FWasCmdY', 'completa'),
    -- Queen (venta y alquiler de vestidos)
    ('p011', 'Vestido de Gala Largo Plateado', NULL,
     'Vestido largo de gala en tela plateada brillante, con escote en V y caída fluida. Para venta o alquiler.',
     'https://images.unsplash.com/photo-1765229280095-b143ba9ea00b?w=700&q=80&auto=format&fit=crop',
     'https://unsplash.com/photos/a-woman-in-a-sparkling-silver-gown-stands-confidently-pvD7DRzTqYk', 'completa'),
    ('p012', 'Vestido de Cóctel Azul Rey con Volante', NULL,
     'Vestido corto de cóctel azul rey, ajustado, con volante en el hombro y cuello con pedrería. Para venta o alquiler.',
     'https://images.unsplash.com/photo-1622079400125-5b6679552976?w=700&q=80&auto=format&fit=crop',
     'https://unsplash.com/photos/woman-in-blue-sleeveless-dress-alcLDJ6QxD0', 'completa'),
    -- Henar's y Eleganzza (alta costura en tallas grandes)
    ('p013', 'Blusa Satinada Champaña', NULL,
     'Blusa elegante de satín color champaña con botones, confeccionada a la medida en tallas grandes.',
     'https://images.unsplash.com/photo-1612889002991-fbfaf775d231?w=700&q=80&auto=format&fit=crop',
     'https://unsplash.com/photos/white-button-up-long-sleeve-shirt-R5DDNmRnF8M', 'superior'),
    ('p014', 'Vestido de Gala Strapless Lila', NULL,
     'Vestido largo de gala strapless en gasa lila con cintura satinada, diseñado para tallas grandes.',
     'https://images.unsplash.com/photo-1790502041559-46a83e83815a?w=700&q=80&auto=format&fit=crop',
     'https://unsplash.com/photos/woman-in-purple-strapless-gown-nOEiImh3M0Q', 'completa'),
    -- Gogó Makeup (productos reales de marcas que se venden en tiendas de maquillaje)
    ('p027', 'Pestañina Lash Sensational Sky High Lavable', 'Maybelline',
     'Pestañina que alarga y da volumen, de fórmula lavable.',
     'https://copservir.vteximg.com.br/arquivos/ids/1916686-600-600/PESTANINA-MAYBELLINE-SKY-HIGH_F.png',
     'https://www.larebajavirtual.com/pestanina-maybelline-sky-high-lavable-153725/p', NULL),
    ('p039', 'Protector Solar Fusion Water SPF 50', 'ISDIN',
     'Protector solar facial de textura ultraligera y acabado invisible, para uso diario.',
     'https://olimpica.vteximg.com.br/arquivos/ids/720323-600-600/8429420195950.jpg',
     'https://www.olimpica.com/fotoprotector-fusion-water-spf50-x-50-ml-8429420195950-2207861/p', NULL)
) AS m(codigo, nombre, marca, descripcion, foto, fuente, tipo_prenda)
JOIN (VALUES
    ('p009',  89900), ('p010', 139900),   -- Samylu
    ('p011', 450000), ('p012', 220000),   -- Queen (precio de venta)
    ('p013', 159900), ('p014', 389900),   -- Henar's y Eleganzza (a la medida, tallas grandes)
    ('p027',  79300), ('p039', 121450)    -- Gogó (precio en tiendas colombianas)
) AS v(codigo, precio) ON v.codigo = m.codigo
WHERE p.codigo = m.codigo;

-- ---------------------------------------------------------------------
-- 3 · Producto sin información: se borra
--     (sus ingredientes, etiquetas, reseñas y favoritos se borran solos: ON DELETE CASCADE)
-- ---------------------------------------------------------------------
DELETE FROM producto WHERE codigo = 'p022';

-- ---------------------------------------------------------------------
-- 5 · Contactos de las tiendas (fuente y confianza de cada dato en docs/datos/contactos-fuentes.csv)
-- ---------------------------------------------------------------------
UPDATE tienda SET instagram = 'bohor_' WHERE slug = 'bohor';
UPDATE tienda SET instagram = 'mattelsa', web = 'mattelsa.net' WHERE slug = 'mattelsa';
UPDATE tienda SET instagram = 'miradadeangelcosmetics' WHERE slug = 'mirada-de-angel';
UPDATE tienda SET instagram = 'maquillajebucaramanga_1' WHERE slug = 'pandora-makeup';
UPDATE tienda SET instagram = 'guacamaya.colors', horario = 'Lun–Sáb 10:00–19:30 · Dom cerrado' WHERE slug = 'guacamaya-colors';
-- Gogó: su Linktree oficial enlaza este número por WhatsApp
UPDATE tienda SET instagram = 'gogobeauty__', whatsapp = '573172785281', telefono = NULL WHERE slug = 'gogo-makeup';
-- Queen: dos directorios coinciden en que vende y alquila vestidos de dama
UPDATE tienda SET descripcion = 'Venta y alquiler de vestidos de dama: novia, quinceañera, gala y cóctel.' WHERE slug = 'queen';

COMMIT;
