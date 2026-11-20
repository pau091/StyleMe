/*
 * PRODUCTOS DE STYLEME
 * --------------------
 * ✅ Los productos con pendiente: false son REALES: se tomaron de la tienda en línea
 *    (o del catálogo) de cada tienda el 20 de noviembre de 2026. El enlace está en "fuente".
 *    ⚠️ Los precios pueden cambiar: confírmalos con la tienda antes de la entrega.
 * ⚠️ Los productos con pendiente: true son EJEMPLOS (tiendas sin catálogo en internet):
 *    Samylu Boutique, Queen, Henar's y Eleganzza y Gogó Makeup.
 *
 * Campos:
 *   id          → único, por ejemplo "p001"
 *   tiendaId    → debe ser igual al id de una tienda en tiendas.js
 *   nombre      → nombre del producto
 *   marca       → (opcional) marca del producto; también sirve para el buscador
 *   categoria   → "ropa" | "maquillaje" | "skincare" | "capilar"
 *   precio      → número sin puntos (45000) o null si no se sabe
 *   descripcion → una frase corta sobre el producto
 *   ingredientes→ (maquillaje, skincare, capilar) lista de ingredientes.
 *                 "…" significa que la lista original es más larga y se resumió.
 *                 [] si no se encontró la lista.
 *   ingredientesFuente → de dónde salió la lista de ingredientes
 *   foto        → ruta a la imagen (local "assets/img/productos/p001.png" o enlace de la tienda)
 *   fuente      → enlace a la página del producto en la tienda
 *   pendiente   → true mientras sea un ejemplo; false cuando es real
 *
 * Solo para ROPA (probador virtual):
 *   tipoPrenda  → "superior" (blusa, camisa, chaqueta)
 *                 "inferior" (pantalón, falda)
 *                 "completa" (vestido, enterizo)
 *   fotoProbador→ (opcional) PNG SIN FONDO de la prenda, guardado en assets/img/productos/.
 *                 Solo las prendas con fotoProbador aparecen en el probador.
 *
 * Solo para SKINCARE y CUIDADO CAPILAR (recomendador):
 *   paso        → skincare: "limpiador" | "tonico" | "serum" | "hidratante" | "protector"
 *                 capilar:  "shampoo" | "acondicionador" | "tratamiento" | "peinar"
 *   tipoPiel    → ["seca", "grasa", "mixta", "normal", "sensible"] o ["todas"]
 *   tipoCabello → ["liso", "ondulado", "rizado", "afro"] o ["todos"]
 *   cuero       → (capilar) ["graso", "seco", "normal", "sensible"] o ["todos"]
 *   objetivos   → skincare: "hidratar", "grasa", "imperfecciones", "manchas", "proteger", "antiedad"
 *                 capilar:  "hidratar", "frizz", "reparar", "rizos", "fortalecer", "grasa"
 *   libreDe     → lo que la etiqueta dice que NO contiene:
 *                 "fragancia", "alcohol", "parabenos", "sulfatos", "siliconas"
 *   Si no sabes un dato, deja la lista vacía []. El recomendador avisa que hay que confirmarlo.
 */
const PRODUCTOS = [
  // =====================================================================
  // ROPA
  // =====================================================================

  // ---------- BOHOR (bohor.com.co) · precios regulares ----------
  {
    id: "p001", tiendaId: "bohor", nombre: "Blusa Tango Negro", categoria: "ropa", tipoPrenda: "superior",
    precio: 185000,
    descripcion: "Blusa entallada de manga larga con cortes en el escote y lazo posterior ajustable. Negra, tallas S, M y L.",
    foto: "https://cdn.shopify.com/s/files/1/0605/2066/1217/files/rn-image_picker_lib_temp_9d4a4497-bea2-4aef-b55b-e80aed0f47ab.jpg?v=1791409853",
    fuente: "https://bohor.com.co/products/blusa-tango-negro", pendiente: false
  },
  {
    id: "p002", tiendaId: "bohor", nombre: "Jean Serenata", categoria: "ropa", tipoPrenda: "inferior",
    precio: 260000,
    descripcion: "Jean de denim oscuro, silueta amplia y tiro bajo, con bolsillos curvos estampados con ondas blancas. Tallas S a XL.",
    foto: "https://cdn.shopify.com/s/files/1/0605/2066/1217/files/Ecomerce00161-1.jpg?v=1785342954",
    fuente: "https://bohor.com.co/products/jean-serenata", pendiente: false
  },
  {
    id: "p003", tiendaId: "bohor", nombre: "Falda Bochorno Negra", categoria: "ropa", tipoPrenda: "inferior",
    precio: 170000,
    descripcion: "Falda midi recta translúcida con falda corta interior de licra, tela liviana. Negra, tallas S y M.",
    foto: "https://cdn.shopify.com/s/files/1/0605/2066/1217/files/rn-image_picker_lib_temp_17a7840b-a2ea-4cfe-b33c-c868887580be.jpg?v=1791404749",
    fuente: "https://bohor.com.co/products/falda-bochorno-negro", pendiente: false
  },
  {
    id: "p004", tiendaId: "bohor", nombre: "Vestido Media Noche Negro", categoria: "ropa", tipoPrenda: "completa",
    precio: 295000,
    descripcion: "Vestido de dos piezas (top bustier con lazo y sobreprenda con falda abullonada) en satín ligero. Negro, tallas S/M y M/L.",
    foto: "https://cdn.shopify.com/s/files/1/0605/2066/1217/files/rn-image_picker_lib_temp_fc18c1e0-34dd-42a2-880d-fcd4d646215f.jpg?v=1791403852",
    fuente: "https://bohor.com.co/products/vestido-media-noche-negro", pendiente: false
  },

  // ---------- MATTELSA (mattelsa.net) · ⚠️ precios en rebaja (Sale Mujer); el regular va en el comentario ----------
  {
    id: "p005", tiendaId: "mattelsa", nombre: "Body Negro Básico", categoria: "ropa", tipoPrenda: "superior",
    precio: 69500, // regular 139.000
    descripcion: "Body de algodón (95% algodón, 5% spandex) con escote en U y espalda descubierta. Negro, tallas S a XL.",
    foto: "https://b2cmattelsa.vteximg.com.br/arquivos/ids/751177/95711-1.jpg?v=639300280901430000",
    fuente: "https://www.mattelsa.net/body-negro-basico-95711/p", pendiente: false
  },
  {
    id: "p006", tiendaId: "mattelsa", nombre: "Jean Wide Leg Tiro Alto Azul Medio", categoria: "ropa", tipoPrenda: "inferior",
    precio: 99500, // regular 199.000
    descripcion: "Jean de tiro alto en denim 100% algodón, con pinzas y bordado en el bolsillo posterior. Azul medio.",
    foto: "https://b2cmattelsa.vteximg.com.br/arquivos/ids/742022/90372-1.jpg?v=639239816341470000",
    fuente: "https://www.mattelsa.net/jeans-wide-leg-tiro-alto-azul-medio-lavanderia-90372/p", pendiente: false
  },
  {
    id: "p007", tiendaId: "mattelsa", nombre: "Pantalón Straight Fit Tiro Alto Negro", categoria: "ropa", tipoPrenda: "inferior",
    precio: 109500, // regular 219.000
    descripcion: "Pantalón recto de tiro alto, 100% algodón. Negro.",
    foto: "https://b2cmattelsa.vteximg.com.br/arquivos/ids/732475/Pantalon-Straight-Fit-Tiro-Alto-Negro-Basico-Talla-S.jpg?v=639172964753070000",
    fuente: "https://www.mattelsa.net/pantalon-straight-fit-tiro-alto-negro-basico-92275/p", pendiente: false
  },
  {
    id: "p008", tiendaId: "mattelsa", nombre: "Vestido Negro Ícono", categoria: "ropa", tipoPrenda: "completa",
    precio: 74500, // regular 149.000
    descripcion: "Vestido de manga larga en jersey (93% poliamida, 7% elastano), doble tela y abertura en la espalda. Negro, tallas S a XL.",
    foto: "https://b2cmattelsa.vteximg.com.br/arquivos/ids/753697/Vestido-Negro-Icono-Talla-S.jpg?v=639300281962730000",
    fuente: "https://www.mattelsa.net/vestido-negro-icono-95719/p", pendiente: false
  },

  // ---------- EJEMPLOS: tiendas sin catálogo en internet (pídeles fotos y precios) ----------
  { id: "p009", tiendaId: "samylu-boutique", nombre: "Blusa (ejemplo)", categoria: "ropa", tipoPrenda: "superior", precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p010", tiendaId: "samylu-boutique", nombre: "Vestido (ejemplo)", categoria: "ropa", tipoPrenda: "completa", precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p011", tiendaId: "queen", nombre: "Chaqueta (ejemplo)", categoria: "ropa", tipoPrenda: "superior", precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p012", tiendaId: "queen", nombre: "Falda (ejemplo)", categoria: "ropa", tipoPrenda: "inferior", precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p013", tiendaId: "henars-eleganzza", nombre: "Blusa (ejemplo)", categoria: "ropa", tipoPrenda: "superior", precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p014", tiendaId: "henars-eleganzza", nombre: "Vestido (ejemplo)", categoria: "ropa", tipoPrenda: "completa", precio: null, foto: "", fuente: "", pendiente: true },

  // =====================================================================
  // MAQUILLAJE
  // =====================================================================

  // ---------- PANDORA MAKEUP (pandoramaquillaje.com) ----------
  {
    id: "p015", tiendaId: "pandora-makeup", nombre: "Base Pro Coverage", marca: "L.A. Girl", categoria: "maquillaje",
    precio: 43700,
    descripcion: "Base líquida ligera de alta cobertura y larga duración, con antioxidantes.",
    ingredientes: ["Water (Aqua)", "Cyclopentasiloxane", "Phenyl Trimethicone", "Cyclohexasiloxane", "Glycerin", "Isododecane", "Propylene Glycol", "Cetyl PEG/PPG-10/1 Dimethicone", "Hydrogenated Polyisobutene", "Polycyclopentadiene", "Tribehenin", "…", "Tocopheryl Acetate", "Ascorbyl Palmitate", "Retinyl Palmitate", "Fragrance (Parfum)"],
    ingredientesFuente: "https://incidecoder.com/products/la-girl-pro-coverage-foundation",
    foto: "https://pandoramaquillaje.com/wp-content/uploads/2024/09/w800h800fitpad.avif",
    fuente: "https://pandoramaquillaje.com/producto/base-pro-coverage-la-girl/", pendiente: false
  },
  {
    id: "p016", tiendaId: "pandora-makeup", nombre: "Labial SuperStay Matte Ink", marca: "Maybelline", categoria: "maquillaje",
    precio: 65000,
    descripcion: "Labial líquido mate que no se transfiere y dura hasta 16 horas, con aplicador en flecha.",
    ingredientes: ["Dimethicone", "Trimethylsiloxysilicate", "Isododecane", "Nylon-611/Dimethicone Copolymer", "Dimethicone Crosspolymer", "C30-45 Alkyldimethylsilyl Polypropylsilsesquioxane", "Lauroyl Lysine", "Alumina", "Silica Silylate", "Disodium Stearoyl Glutamate", "Phenoxyethanol", "Caprylyl Glycol", "Parfum/Fragrance", "…", "pigmentos (varían según el tono)"],
    ingredientesFuente: "https://incidecoder.com/products/maybelline-superstay-matte-ink-liquid-lipstick",
    foto: "https://pandoramaquillaje.com/wp-content/uploads/2024/12/nuevo-equipo-3.png",
    fuente: "https://pandoramaquillaje.com/producto/labial-super-stay-matte-maybelline/", pendiente: false
  },
  {
    id: "p017", tiendaId: "pandora-makeup", nombre: "Pestañina Lash Sensational", marca: "Maybelline", categoria: "maquillaje",
    precio: 63000,
    descripcion: "Pestañina con cepillo en abanico de seis tipos de cerdas, para volumen efecto abanico.",
    ingredientes: ["Aqua/Water", "Propylene Glycol", "Styrene/Acrylates/Ammonium Methacrylate Copolymer", "Polyurethane-35", "Cera Alba (Beeswax)", "Synthetic Fluorphlogopite", "Glyceryl Stearate", "Cetyl Alcohol", "PEG-200 Glyceryl Stearate", "Copernicia Cerifera Cera", "Stearic Acid", "Alcohol Denat", "Paraffin", "…", "Sodium Laureth Sulfate", "Iron Oxides (CI 77499)"],
    ingredientesFuente: "https://incidecoder.com/products/maybelline-lash-sensational-r-washable-mascara",
    foto: "https://pandoramaquillaje.com/wp-content/uploads/2025/02/IMG_9292.webp",
    fuente: "https://pandoramaquillaje.com/producto/pestanina-lash-sensational-maybelline/", pendiente: false
  },

  // ---------- GUACAMAYA COLORS (guacamayacolors.com) ----------
  {
    id: "p018", tiendaId: "guacamaya-colors", nombre: "Base Skinfinite Hidratante", marca: "SHEGLAM", categoria: "maquillaje",
    precio: 57900, // en oferta, antes 89.900
    descripcion: "Base cremosa de cobertura media, hidratante y ligera, con acabado natural luminoso.",
    ingredientes: ["Aqua/Water", "Cyclopentasiloxane", "Glycerin", "Isododecane", "Trimethylsiloxysilicate", "Phenyl Trimethicone", "Butylene Glycol", "Cetyl PEG/PPG-10/1 Dimethicone", "Tridecyl Trimellitate", "Caprylic/Capric Triglyceride", "Ethylhexyl Palmitate", "…", "Mica", "Titanium Dioxide (CI 77891)", "Iron Oxides"],
    ingredientesFuente: "https://incidecoder.com/products/sheglam-skinfinite-hydrating-foundation",
    foto: "https://cdn.shopify.com/s/files/1/0557/7421/6384/files/WhatsAppImage2023-11-17at7.23.29PM.jpg?v=1700267075",
    fuente: "https://guacamayacolors.com/products/base-de-maquillaje-hidratante-skinfinite-sheglam", pendiente: false
  },
  {
    id: "p019", tiendaId: "guacamaya-colors", nombre: "Pestañina All-in-One Volume & Length", marca: "SHEGLAM", categoria: "maquillaje",
    precio: 42000,
    descripcion: "Pestañina de doble punta con dos fórmulas: volumen, largo y separación sin grumos.",
    ingredientes: ["Isododecane", "Trimethylsiloxysilicate", "Hydrogenated Polyisobutene", "Disteardimonium Hectorite", "Ceresin", "Talc", "Microcrystalline Wax", "Alcohol", "Dextrin Palmitate/Ethylhexanoate", "Polysorbate 60", "Polyethylene", "Cera Alba", "…", "Phenoxyethanol", "Iron Oxides (CI 77499)"],
    ingredientesFuente: "https://incidecoder.com/products/sheglam-all-in-one-volume-length-mascara",
    foto: "https://cdn.shopify.com/s/files/1/0557/7421/6384/files/WhatsAppImage2023-10-13at4.20.22PM.jpg?v=1697232601",
    fuente: "https://guacamayacolors.com/products/copia-de-all-in-one-volume-length-mascara-sheglam", pendiente: false
  },
  {
    id: "p020", tiendaId: "guacamaya-colors", nombre: "Rubor Líquido Color Bloom Mate", marca: "SHEGLAM", categoria: "maquillaje",
    precio: 33000, // en oferta, antes 53.000
    descripcion: "Rubor líquido en gel-crema con vitamina C y aplicador de esponja, de larga duración.",
    ingredientes: ["Water/Aqua", "Isododecane", "Isohexadecane", "Isononyl Isononanoate", "Tridecyl Trimellitate", "Propylene Glycol", "Trimethylsiloxysilicate", "Cetyl PEG/PPG-10/1 Dimethicone", "Vinyl Dimethicone/Methicone Silsesquioxane Crosspolymer", "Dimethicone", "Glycerin", "…", "Fragrance/Parfum", "3-O-Ethyl Ascorbic Acid", "Tocopherol", "pigmentos"],
    ingredientesFuente: "https://www.sheglam.com/product/Color%20Bloom%20Liquid%20Blush%20Matte%20Finish-Love-Cake-7957494104229",
    foto: "https://cdn.shopify.com/s/files/1/0557/7421/6384/files/WhatsAppImage2024-07-06at8.45.12AM.jpg?v=1720273696",
    fuente: "https://guacamayacolors.com/products/color-bloom-liquid-blush-matte-finish-sheglam", pendiente: false
  },

  // ---------- MIRADA DE ÁNGEL (miradadeangel.com.co) ----------
  {
    id: "p021", tiendaId: "mirada-de-angel", nombre: "Iluminador Rosa", marca: "Ruby Rose", categoria: "maquillaje",
    precio: 20800,
    descripcion: "Iluminador en polvo muy pigmentado, acabado satinado, para pómulos y puntos de luz (10 g).",
    ingredientes: ["Agua", "Mica", "Glicerina", "Ciclometicona", "Isononanoato", "Goma xantana", "Aceite de ricino etoxilado hidrogenado", "Silicato de aluminio y magnesio", "Cuarzo", "Fenoxietanol", "Poliacrilamida"],
    ingredientesFuente: "https://miradadeangel.com.co/maquillaje/iluminador-rosa-ruby-rose/",
    foto: "https://miradadeangel.com.co/wp-content/uploads/2021/02/ruby1-min.jpg",
    fuente: "https://miradadeangel.com.co/maquillaje/iluminador-rosa-ruby-rose/", pendiente: false
  },
  {
    id: "p022", tiendaId: "mirada-de-angel", nombre: "Polvo Compacto Efecto Total 6", marca: "Vogue", categoria: "maquillaje",
    precio: 10000,
    descripcion: "Polvo compacto 6 en 1 de acabado mate y alta cobertura, con filtro solar y aceite de argán.",
    ingredientes: [], ingredientesFuente: "",
    foto: "https://miradadeangel.com.co/wp-content/uploads/2020/04/polvo-vogue-porcelana.jpg",
    fuente: "https://miradadeangel.com.co/maquillaje/polvo-compacto-efecto-total-6-vogue-cosmeticos/", pendiente: false
  },
  {
    id: "p023", tiendaId: "mirada-de-angel", nombre: "Base Detox and Protect (05 Honey Beige)", marca: "Note Cosmetics", categoria: "maquillaje",
    precio: 86990,
    descripcion: "Base líquida SPF 15 sin parabenos, con aceite de almendras, alga y vitamina E (35 ml).",
    ingredientes: [], ingredientesFuente: "",
    foto: "https://miradadeangel.com.co/wp-content/uploads/2022/11/base-detox-honey-05.png",
    fuente: "https://miradadeangel.com.co/maquillaje/base-piel-mixta-note-detox-and-protect-foundation/", pendiente: false
  },

  // ---------- SO BEAUTY (catálogo al detal en PDF, sin fecha · sin fotos) ----------
  {
    id: "p024", tiendaId: "so-beauty", nombre: "Base Skin Hidratante", marca: "Samy", categoria: "maquillaje",
    precio: 32000, descripcion: "Base de maquillaje hidratante.",
    ingredientes: [], ingredientesFuente: "", foto: "",
    fuente: "https://drive.google.com/file/d/1TQo1pG98jX_rKKAmnefeF3Fp9vrIHQuv/view", pendiente: false
  },
  {
    id: "p025", tiendaId: "so-beauty", nombre: "Rubor Hydro Blush", marca: "Ruby Rose", categoria: "maquillaje",
    precio: 25000, descripcion: "Rubor de Ruby Rose.",
    ingredientes: [], ingredientesFuente: "", foto: "",
    fuente: "https://drive.google.com/file/d/1TQo1pG98jX_rKKAmnefeF3Fp9vrIHQuv/view", pendiente: false
  },
  {
    id: "p026", tiendaId: "so-beauty", nombre: "Tinta para Labios Skin", marca: "Samy", categoria: "maquillaje",
    precio: 25000, descripcion: "Tinta para labios.",
    ingredientes: [], ingredientesFuente: "", foto: "",
    fuente: "https://drive.google.com/file/d/1TQo1pG98jX_rKKAmnefeF3Fp9vrIHQuv/view", pendiente: false
  },

  // ---------- EJEMPLO: Gogó Makeup (su página web no funciona; pídeles productos y precios) ----------
  { id: "p027", tiendaId: "gogo-makeup", nombre: "Máscara de pestañas (ejemplo)", categoria: "maquillaje", precio: null, foto: "", fuente: "", pendiente: true },

  // =====================================================================
  // SKINCARE
  // =====================================================================
  {
    id: "p028", tiendaId: "pandora-makeup", nombre: "Sérum Madagascar Centella Ampoule 55 ml", marca: "SKIN1004", categoria: "skincare",
    precio: 85001,
    descripcion: "Sérum de centella asiática que calma la piel sensible, fortalece la barrera e hidrata.",
    ingredientes: ["Water", "Glycerin", "Butylene Glycol", "Centella Asiatica Extract", "1,2-Hexanediol", "Cellulose Gum", "Ethylhexylglycerin"],
    ingredientesFuente: "https://incidecoder.com/products/skin1004-madagascar-centella-ampoule",
    foto: "https://pandoramaquillaje.com/wp-content/uploads/2026/10/SERUM-CETELLA.jpg",
    fuente: "https://pandoramaquillaje.com/producto/serum-ampolla-centella-skin1004-55-ml/", pendiente: false,
    paso: "serum", tipoPiel: ["todas"], objetivos: ["hidratar"], libreDe: ["fragancia", "alcohol", "parabenos", "sulfatos", "siliconas"]
  },
  {
    id: "p029", tiendaId: "pandora-makeup", nombre: "Protector Solar Hyalu-Cica Water-Fit SPF 50+", marca: "SKIN1004", categoria: "skincare",
    precio: 95000,
    descripcion: "Protector solar coreano en sérum, de amplio espectro, que hidrata y deja la piel luminosa.",
    ingredientes: ["Purified Water", "Dibutyl Adipate", "Propanediol", "Diethylamino Hydroxybenzoyl Hexyl Benzoate", "Polymethylsilsesquioxane", "Ethylhexyl Triazone", "Methylene Bis-Benzotriazolyl Tetramethylbutylphenol", "Niacinamide", "Coco-Caprylate/Caprate", "Caprylyl Methicone", "Diethylhexyl Butamido Triazone", "Glycerin", "1,2-Hexanediol", "Butylene Glycol", "Centella Asiatica Extract", "…"],
    ingredientesFuente: "https://incidecoder.com/products/skin1004-hyalu-cica-water-fit-sun-serum",
    foto: "https://pandoramaquillaje.com/wp-content/uploads/2026/10/8809576261592_1.png",
    fuente: "https://pandoramaquillaje.com/producto/protector-solar-hyalu-cica-water-fit-skin1004-50ml/", pendiente: false,
    paso: "protector", tipoPiel: ["todas"], objetivos: ["proteger", "manchas", "hidratar"], libreDe: []
  },
  {
    id: "p030", tiendaId: "pandora-makeup", nombre: "Crema 345 Relief Cream", marca: "Dr. Althea", categoria: "skincare",
    precio: 120000,
    descripcion: "Crema calmante que hidrata y repara la barrera de la piel sensible o irritada.",
    ingredientes: ["Water", "Propanediol", "Glycerin", "Cyclohexasiloxane", "1,2-Hexanediol", "Caprylic/Capric Triglyceride", "Niacinamide", "Panthenol", "Polyglycerin-3", "Polyglyceryl-10 Stearate", "…", "Centella Asiatica Leaf Extract", "Sodium Hyaluronate", "Ceramide NP", "Resveratrol", "Adenosine"],
    ingredientesFuente: "https://incidecoder.com/products/dr-althea-345-relief-cream",
    foto: "https://pandoramaquillaje.com/wp-content/uploads/2026/06/51dWGrl3-dL.jpg",
    fuente: "https://pandoramaquillaje.com/producto/crema-reparadora-dr-althea-345-relief-cream/", pendiente: false,
    paso: "hidratante", tipoPiel: ["sensible", "seca"], objetivos: ["hidratar"], libreDe: ["fragancia", "alcohol", "parabenos", "sulfatos"]
  },
  {
    id: "p031", tiendaId: "guacamaya-colors", nombre: "BHA Clarify Gel Cream", marca: "Good Molecules", categoria: "skincare",
    precio: 83900,
    descripcion: "Gel-crema ligero con ácido salicílico, té verde y centella que calma e hidrata la piel con brotes.",
    ingredientes: ["Water", "Propanediol", "Diheptyl Succinate", "Glycerin", "Ammonium Acryloyldimethyltaurate/VP Copolymer", "Salicylic Acid (0,4%)", "Phenoxyethanol", "Caprylyl Glycol", "Tromethamine", "Ethylhexylglycerin", "…", "Allantoin", "Bisabolol", "Punica Granatum Seed Oil", "Vitis Vinifera Seed Oil", "Centella Asiatica Extract", "Camellia Sinensis Leaf Extract"],
    ingredientesFuente: "https://incidecoder.com/products/good-molecules-bha-clarify-gel-cream",
    foto: "https://cdn.shopify.com/s/files/1/0557/7421/6384/files/WhatsAppImage2024-05-03at5.01.47PM_2.jpg?v=1714773861",
    fuente: "https://guacamayacolors.com/products/bha-clarify-gel-cream-good-molecules", pendiente: false,
    paso: "hidratante", tipoPiel: ["todas"], objetivos: ["imperfecciones", "grasa", "hidratar"], libreDe: ["fragancia", "parabenos", "sulfatos", "siliconas"]
  },
  {
    id: "p032", tiendaId: "guacamaya-colors", nombre: "Desmaquillante Dual 260 ml", marca: "Samy", categoria: "skincare",
    precio: 43900,
    descripcion: "Desmaquillante bifásico con aloe que retira el maquillaje de rostro y labios, incluso el a prueba de agua.",
    ingredientes: ["Cyclopentasiloxane", "Cyclohexasiloxane", "Aqua", "Aloe Barbadensis Extract", "Propylene Glycol", "Sodium Chloride", "Imidazolidinyl Urea", "Benzalkonium Chloride", "EDTA", "CI 16035", "CI 19140", "CI 14720", "CI 42090"],
    ingredientesFuente: "https://incidecoder.com/products/samy-desmaquillante-dual",
    foto: "https://cdn.shopify.com/s/files/1/0557/7421/6384/files/WhatsAppImage2024-04-02at5.45.39PM_5.jpg?v=1712099261",
    fuente: "https://guacamayacolors.com/products/desmaquillante-dual-samy", pendiente: false,
    paso: "limpiador", tipoPiel: ["todas"], objetivos: [], libreDe: ["fragancia", "alcohol", "parabenos", "sulfatos"]
  },
  {
    id: "p033", tiendaId: "mirada-de-angel", nombre: "Jabón Facial Detox 160 g", marca: "Trendy", categoria: "skincare",
    precio: 16000,
    descripcion: "Limpiador facial con carbón activado y ácido salicílico que controla la grasa y previene imperfecciones.",
    ingredientes: ["Carbón activado", "Ácido salicílico", "Seboreductyl", "Pantenol (vitamina B5)", "Extractos de frambuesa, fresa, kiwi, durazno, papaya y pepino", "… (solo los activos que publica la tienda)"],
    ingredientesFuente: "https://miradadeangel.com.co/maquillaje/rostro/jabon-facial-detox-trendy-160g/",
    foto: "https://miradadeangel.com.co/wp-content/uploads/2023/05/jabon-facial-detox.png",
    fuente: "https://miradadeangel.com.co/maquillaje/rostro/jabon-facial-detox-trendy-160g/", pendiente: false,
    paso: "limpiador", tipoPiel: ["normal", "mixta", "grasa"], objetivos: ["grasa", "imperfecciones"], libreDe: []
  },
  {
    id: "p034", tiendaId: "mirada-de-angel", nombre: "Tónico Ácido Hialurónico", marca: "Bioaqua", categoria: "skincare",
    precio: 18000,
    descripcion: "Tónico hidratante y refrescante con ácido hialurónico que ayuda a retener el agua en la piel.",
    ingredientes: [], ingredientesFuente: "",
    foto: "https://miradadeangel.com.co/wp-content/uploads/2022/01/Tonico-acido-hialuronico-bioaqua-min.jpg",
    fuente: "https://miradadeangel.com.co/facial/tonico-acido-hialuronico-bioaqua/", pendiente: false,
    paso: "tonico", tipoPiel: ["todas"], objetivos: ["hidratar"], libreDe: []
  },
  {
    id: "p035", tiendaId: "mirada-de-angel", nombre: "Suero Detox Niacinamida + Zinc", marca: "Trendy", categoria: "skincare",
    precio: 32000,
    descripcion: "Sérum con niacinamida al 10%, zinc y pantenol que regula la grasa y minimiza los poros.",
    ingredientes: ["Niacinamide (10%)", "Zinc", "Panthenol", "Extracto de té verde", "Extracto de café verde", "Extracto de cacao", "… (solo los activos que publica la tienda)"],
    ingredientesFuente: "https://miradadeangel.com.co/facial/suero-detox-niamicida-zinc-trendy/",
    foto: "https://miradadeangel.com.co/wp-content/uploads/2022/05/suero-niamicidas-min.jpg",
    fuente: "https://miradadeangel.com.co/facial/suero-detox-niamicida-zinc-trendy/", pendiente: false,
    paso: "serum", tipoPiel: ["mixta", "grasa"], objetivos: ["grasa", "imperfecciones", "hidratar"], libreDe: []
  },
  {
    id: "p036", tiendaId: "so-beauty", nombre: "Agua Micelar", marca: "Samy", categoria: "skincare",
    precio: 18000, descripcion: "Agua micelar para limpiar y desmaquillar el rostro.",
    ingredientes: [], ingredientesFuente: "", foto: "",
    fuente: "https://drive.google.com/file/d/1TQo1pG98jX_rKKAmnefeF3Fp9vrIHQuv/view", pendiente: false,
    paso: "limpiador", tipoPiel: ["todas"], objetivos: ["hidratar"], libreDe: []
  },
  {
    id: "p037", tiendaId: "so-beauty", nombre: "Sérum Control Oleosidad", marca: "Samy", categoria: "skincare",
    precio: 35000, descripcion: "Sérum facial para controlar la grasa.",
    ingredientes: [], ingredientesFuente: "", foto: "",
    fuente: "https://drive.google.com/file/d/1TQo1pG98jX_rKKAmnefeF3Fp9vrIHQuv/view", pendiente: false,
    paso: "serum", tipoPiel: ["grasa", "mixta"], objetivos: ["grasa"], libreDe: []
  },
  {
    id: "p038", tiendaId: "so-beauty", nombre: "Protector Solar Facial", marca: "Ana María", categoria: "skincare",
    precio: 65000, descripcion: "Protector solar para el rostro.",
    ingredientes: [], ingredientesFuente: "", foto: "",
    fuente: "https://drive.google.com/file/d/1TQo1pG98jX_rKKAmnefeF3Fp9vrIHQuv/view", pendiente: false,
    paso: "protector", tipoPiel: ["todas"], objetivos: ["proteger"], libreDe: []
  },
  // EJEMPLO: Gogó Makeup
  { id: "p039", tiendaId: "gogo-makeup", nombre: "Protector solar (ejemplo)", categoria: "skincare", paso: "protector", tipoPiel: ["todas"], objetivos: ["proteger", "manchas"], libreDe: [], precio: null, foto: "", fuente: "", pendiente: true },

  // =====================================================================
  // CUIDADO CAPILAR
  // =====================================================================
  {
    id: "p040", tiendaId: "mirada-de-angel", nombre: "Shampoo en Crema Sin Sulfatos", marca: "Cantu", categoria: "capilar",
    precio: 49900,
    descripcion: "Shampoo en crema sin sulfatos para cabello ondulado, rizado y afro, que limpia y humecta.",
    ingredientes: ["Water", "Sodium Lauroyl Methyl Isethionate", "Cocamidopropyl Betaine", "Acrylates Copolymer", "Sodium Lauroyl Sarcosinate", "PEG/PPG-120/10 Trimethylolpropane Trioleate", "Laureth-2", "Cocamide MIPA", "Citric Acid", "Panthenol", "Fragrance", "Polyester-11", "Glycol Distearate", "Tetrasodium EDTA", "Sodium Hydroxide", "PPG-20 Methyl Glucose Ether", "Polyquaternium-10", "Sodium PCA", "Phenoxyethanol", "Ethylhexylglycerin"],
    ingredientesFuente: "https://miradadeangel.com.co/capilar/cantu-sulfate-free-cleansing-cream-shampoo/",
    foto: "https://miradadeangel.com.co/wp-content/uploads/2022/02/shampoo-cream-cantu-min.jpg",
    fuente: "https://miradadeangel.com.co/capilar/cantu-sulfate-free-cleansing-cream-shampoo/", pendiente: false,
    paso: "shampoo", tipoCabello: ["ondulado", "rizado", "afro"], cuero: ["todos"], objetivos: ["hidratar", "rizos"], libreDe: ["sulfatos", "parabenos", "siliconas"]
  },
  {
    id: "p041", tiendaId: "mirada-de-angel", nombre: "Acondicionador Hidratante Sin Sulfatos", marca: "Cantu", categoria: "capilar",
    precio: 49900,
    descripcion: "Acondicionador con manteca de karité que hidrata el cabello seco y quebradizo y reduce el quiebre.",
    ingredientes: ["Water", "Stearyl Alcohol", "Cetyl Alcohol", "Butyrospermum Parkii (Shea) Butter", "Stearamidopropyl Dimethylamine", "Glyceryl Stearate", "Phenoxyethanol", "Fragrance", "Neopentyl Glycol Diheptanoate", "Lactic Acid", "Isododecane", "Ethylhexylglycerin", "Polyester-11", "Disodium EDTA", "Benzyl Benzoate"],
    ingredientesFuente: "https://miradadeangel.com.co/capilar/cantu-sulfate-free-hydrating-cream-conditioner/",
    foto: "https://miradadeangel.com.co/wp-content/uploads/2022/02/acondicionador-cantu-min.jpg",
    fuente: "https://miradadeangel.com.co/capilar/cantu-sulfate-free-hydrating-cream-conditioner/", pendiente: false,
    paso: "acondicionador", tipoCabello: ["ondulado", "rizado", "afro"], cuero: ["todos"], objetivos: ["hidratar", "reparar"], libreDe: ["sulfatos", "parabenos", "siliconas"]
  },
  {
    id: "p042", tiendaId: "mirada-de-angel", nombre: "Mascarilla Deep Treatment Masque", marca: "Cantu", categoria: "capilar",
    precio: 70000,
    descripcion: "Mascarilla de acondicionamiento profundo con karité y aceites que hidrata, fortalece y repara los rizos.",
    ingredientes: ["Water", "Canola Oil", "Cetearyl Alcohol", "Ceteareth-20", "Glycerin", "Fragrance (Parfum)", "Beeswax", "Cetyl Alcohol", "Butyrospermum Parkii (Shea) Butter", "Glycol Stearate", "Polyquaternium-10", "Cocos Nucifera Oil", "Persea Gratissima Oil", "Argania Spinosa Kernel Oil", "Silk Amino Acids", "Phenoxyethanol", "Ethylhexylglycerin", "…"],
    ingredientesFuente: "https://incidecoder.com/products/cantu-shea-butter-for-natural-hair-deep-treatment-masque",
    foto: "https://miradadeangel.com.co/wp-content/uploads/2022/05/ScreenShot2023-07-17at1.02.58AM_1024x1024-1.webp",
    fuente: "https://miradadeangel.com.co/capilar/deep-treatment-masque-cantu/", pendiente: false,
    paso: "tratamiento", tipoCabello: ["rizado", "afro"], cuero: ["todos"], objetivos: ["hidratar", "fortalecer", "reparar", "rizos"], libreDe: ["sulfatos", "parabenos", "siliconas"]
  },
  {
    id: "p043", tiendaId: "mirada-de-angel", nombre: "Crema Activadora de Rizos", marca: "Cantu", categoria: "capilar",
    precio: 59900,
    descripcion: "Crema activadora de rizos con karité que define, da volumen y controla el frizz.",
    ingredientes: ["Water", "Glycerin", "Propanediol", "Fragrance", "Polyquaternium-10", "Stearalkonium Chloride", "Stearyl Alcohol", "Coconut Oil", "Shea Butter", "Aloe Vera Leaf Juice", "Sweet Almond Oil", "Avocado Oil", "Argan Kernel Oil", "Silk Amino Acids", "Cetyl Alcohol", "Cetearyl Alcohol", "Polysorbate-60", "Phenoxyethanol", "Ethylhexylglycerin", "…"],
    ingredientesFuente: "https://miradadeangel.com.co/capilar/cantu-moisturizing-curl-activator-cream/",
    foto: "https://miradadeangel.com.co/wp-content/uploads/2022/02/activador-curl-cantu-min.jpg",
    fuente: "https://miradadeangel.com.co/capilar/cantu-moisturizing-curl-activator-cream/", pendiente: false,
    paso: "peinar", tipoCabello: ["ondulado", "rizado", "afro"], cuero: ["todos"], objetivos: ["rizos", "frizz", "hidratar"], libreDe: ["sulfatos", "parabenos", "siliconas"]
  },
  {
    id: "p044", tiendaId: "mirada-de-angel", nombre: "Crema Divino Potão Mais Crespo 2 en 1 (1000 g)", marca: "Skala", categoria: "capilar",
    precio: 39900,
    descripcion: "Crema 2 en 1 (tratamiento o crema para peinar sin enjuague) para cabello crespo o en transición, con ricino, argán y queratina vegetal.",
    ingredientes: [], ingredientesFuente: "",
    foto: "https://miradadeangel.com.co/wp-content/uploads/2023/04/divino-potato.png",
    fuente: "https://miradadeangel.com.co/capilar/skala-expert-crema-divino-potao-crespo-2-en-1-x-1000gr/", pendiente: false,
    paso: "peinar", tipoCabello: ["rizado", "afro"], cuero: ["todos"], objetivos: ["reparar", "frizz", "hidratar", "rizos"], libreDe: ["sulfatos", "parabenos", "siliconas"]
  },
  {
    id: "p045", tiendaId: "mirada-de-angel", nombre: "Shampoo Liss Control", marca: "Salon In", categoria: "capilar",
    precio: 30000,
    descripcion: "Shampoo suave sin sal con extracto de lichi, proteína de seda y queratina para prolongar el alisado.",
    ingredientes: [], ingredientesFuente: "",
    foto: "https://miradadeangel.com.co/wp-content/uploads/2021/08/SHAMPOO-CONTROL-SALON-min.jpg",
    fuente: "https://miradadeangel.com.co/capilar/shampoo-liss-control-salon-in/", pendiente: false,
    paso: "shampoo", tipoCabello: ["liso"], cuero: ["todos"], objetivos: ["hidratar", "frizz"], libreDe: []
  },
  {
    id: "p046", tiendaId: "pandora-makeup", nombre: "Shampoo Control Grasa", marca: "La Poción", categoria: "capilar",
    precio: 39900,
    descripcion: "Shampoo con bicarbonato de sodio y bioactivos marinos que regula la grasa y purifica el cuero cabelludo.",
    ingredientes: [], ingredientesFuente: "",
    foto: "https://pandoramaquillaje.com/wp-content/uploads/2025/11/172676-800-auto.webp",
    fuente: "https://pandoramaquillaje.com/producto/shampoo-control-grasa-la-pocion/", pendiente: false,
    paso: "shampoo", tipoCabello: ["todos"], cuero: ["graso"], objetivos: ["grasa"], libreDe: []
  },
  {
    id: "p047", tiendaId: "pandora-makeup", nombre: "Crema para Peinar Dual", marca: "La Poción", categoria: "capilar",
    precio: 35000,
    descripcion: "Crema para peinar que desenreda, sella la cutícula y controla el frizz sin dejar peso.",
    ingredientes: [], ingredientesFuente: "",
    foto: "https://pandoramaquillaje.com/wp-content/uploads/2025/11/crema-de-peinar-la-pocion-dual-x440ml-4h00203.webp",
    fuente: "https://pandoramaquillaje.com/producto/crema-para-peinar-dual-la-pocion/", pendiente: false,
    paso: "peinar", tipoCabello: ["todos"], cuero: ["todos"], objetivos: ["frizz", "hidratar"], libreDe: []
  },
  {
    id: "p048", tiendaId: "guacamaya-colors", nombre: "Keratina Líquida Sublim Protect", marca: "Byphasse", categoria: "capilar",
    precio: 33900,
    descripcion: "Queratina líquida que revitaliza el cabello seco y le devuelve brillo, suavidad y cuerpo.",
    ingredientes: ["Aqua", "Alcohol Denat", "PEG-40 Hydrogenated Castor Oil", "Polyquaternium-6", "Parfum", "Hydrolyzed Keratin", "Polyquaternium-11", "2-Bromo-2-Nitropropane-1,3-Diol", "Phenoxyethanol", "Citric Acid", "Potassium Sorbate", "Ethylhexylglycerin", "Amyl Cinnamal", "Linalool", "Benzyl Salicylate", "Butylphenyl Methylpropional", "Limonene", "Hydroxycitronellal", "CI 15985"],
    ingredientesFuente: "https://incidecoder.com/products/byphasse-liquid-keratin-sublim-protect-dry-hair",
    foto: "https://cdn.shopify.com/s/files/1/0557/7421/6384/files/imagnesdeespecialparatifondoblanco-2025-09-18T173220.395.jpg?v=1758234978",
    fuente: "https://guacamayacolors.com/products/keratina-liquida-byphasse", pendiente: false,
    paso: "tratamiento", tipoCabello: ["todos"], cuero: ["todos"], objetivos: ["hidratar", "reparar"], libreDe: ["parabenos", "sulfatos", "siliconas"]
  },
  {
    id: "p049", tiendaId: "guacamaya-colors", nombre: "Gel de Fijación got2b Mini 35 g", marca: "got2b (Schwarzkopf)", categoria: "capilar",
    precio: 37000, // en oferta, antes 47.900
    descripcion: "Gel de fijación extrema resistente al agua y al sudor, de fórmula vegana.",
    ingredientes: [], ingredientesFuente: "",
    foto: "https://cdn.shopify.com/s/files/1/0557/7421/6384/files/WhatsAppImage2024-04-04at12.54.04PM_6.jpg?v=1712263944",
    fuente: "https://guacamayacolors.com/products/gel-got2b-mini-35g", pendiente: false,
    paso: "peinar", tipoCabello: ["todos"], cuero: ["todos"], objetivos: [], libreDe: []
  }
];
