/*
 * PRODUCTOS DE STYLEME
 * --------------------
 * ⚠️ Todos estos productos son EJEMPLOS para que la página funcione.
 *    Reemplázalos por productos reales del Instagram de cada tienda.
 *
 * Campos:
 *   id          → único, por ejemplo "p001"
 *   tiendaId    → debe ser igual al id de una tienda en tiendas.js
 *   categoria   → "ropa" | "maquillaje" | "skincare" | "capilar"
 *   precio      → número sin puntos (45000) o null si no se sabe
 *   foto        → ruta a la imagen, por ejemplo "assets/img/productos/p001.png"
 *   fuente      → enlace a la publicación de Instagram de donde salió
 *   pendiente   → true mientras sea un ejemplo; ponlo en false cuando sea real
 *
 * Solo para ROPA (probador virtual):
 *   tipoPrenda  → "superior" (blusa, camisa, chaqueta)
 *                 "inferior" (pantalón, falda)
 *                 "completa" (vestido, enterizo)
 *   Para el probador, la foto ideal es un PNG SIN FONDO.
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
  // ---------- ROPA ----------
  { id: "p001", tiendaId: "samylu-boutique", nombre: "Blusa (ejemplo)", categoria: "ropa", tipoPrenda: "superior", precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p002", tiendaId: "samylu-boutique", nombre: "Vestido (ejemplo)", categoria: "ropa", tipoPrenda: "completa", precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p003", tiendaId: "bohor", nombre: "Camisa (ejemplo)", categoria: "ropa", tipoPrenda: "superior", precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p004", tiendaId: "bohor", nombre: "Pantalón (ejemplo)", categoria: "ropa", tipoPrenda: "inferior", precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p005", tiendaId: "sea-and-seams", nombre: "Chaqueta (ejemplo)", categoria: "ropa", tipoPrenda: "superior", precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p006", tiendaId: "sea-and-seams", nombre: "Falda (ejemplo)", categoria: "ropa", tipoPrenda: "inferior", precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p007", tiendaId: "henars-eleganzza", nombre: "Blusa (ejemplo)", categoria: "ropa", tipoPrenda: "superior", precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p008", tiendaId: "henars-eleganzza", nombre: "Vestido (ejemplo)", categoria: "ropa", tipoPrenda: "completa", precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p009", tiendaId: "mattelsa", nombre: "Camiseta (ejemplo)", categoria: "ropa", tipoPrenda: "superior", precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p010", tiendaId: "mattelsa", nombre: "Jean (ejemplo)", categoria: "ropa", tipoPrenda: "inferior", precio: null, foto: "", fuente: "", pendiente: true },

  // ---------- MAQUILLAJE ----------
  { id: "p011", tiendaId: "pandora-makeup", nombre: "Base líquida (ejemplo)", categoria: "maquillaje", precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p012", tiendaId: "pandora-makeup", nombre: "Labial (ejemplo)", categoria: "maquillaje", precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p013", tiendaId: "guacamaya-colors", nombre: "Paleta de sombras (ejemplo)", categoria: "maquillaje", precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p015", tiendaId: "gogo-makeup", nombre: "Máscara de pestañas (ejemplo)", categoria: "maquillaje", precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p017", tiendaId: "so-beauty", nombre: "Rubor (ejemplo)", categoria: "maquillaje", precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p018", tiendaId: "so-beauty", nombre: "Delineador (ejemplo)", categoria: "maquillaje", precio: null, foto: "", fuente: "", pendiente: true },

  // ---------- SKINCARE ----------
  { id: "p014", tiendaId: "guacamaya-colors", nombre: "Sérum hidratante (ejemplo)", categoria: "skincare", paso: "serum", tipoPiel: ["todas"], objetivos: ["hidratar"], libreDe: ["fragancia"], precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p016", tiendaId: "gogo-makeup", nombre: "Protector solar (ejemplo)", categoria: "skincare", paso: "protector", tipoPiel: ["todas"], objetivos: ["proteger", "manchas"], libreDe: [], precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p021", tiendaId: "guacamaya-colors", nombre: "Gel limpiador facial (ejemplo)", categoria: "skincare", paso: "limpiador", tipoPiel: ["grasa", "mixta"], objetivos: ["grasa", "imperfecciones"], libreDe: ["alcohol"], precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p022", tiendaId: "guacamaya-colors", nombre: "Crema hidratante (ejemplo)", categoria: "skincare", paso: "hidratante", tipoPiel: ["seca", "normal", "sensible"], objetivos: ["hidratar"], libreDe: ["fragancia", "alcohol"], precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p023", tiendaId: "so-beauty", nombre: "Agua micelar (ejemplo)", categoria: "skincare", paso: "limpiador", tipoPiel: ["todas"], objetivos: ["hidratar"], libreDe: ["alcohol"], precio: null, foto: "", fuente: "", pendiente: true },

  // ---------- CUIDADO CAPILAR ----------
  { id: "p019", tiendaId: "mirada-de-angel", nombre: "Shampoo sin sulfatos (ejemplo)", categoria: "capilar", paso: "shampoo", tipoCabello: ["todos"], cuero: ["seco", "normal", "sensible"], objetivos: ["hidratar"], libreDe: ["sulfatos"], precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p020", tiendaId: "mirada-de-angel", nombre: "Tratamiento reparador (ejemplo)", categoria: "capilar", paso: "tratamiento", tipoCabello: ["todos"], cuero: ["todos"], objetivos: ["reparar", "fortalecer"], libreDe: [], precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p024", tiendaId: "mirada-de-angel", nombre: "Crema para peinar rizos (ejemplo)", categoria: "capilar", paso: "peinar", tipoCabello: ["ondulado", "rizado", "afro"], cuero: ["todos"], objetivos: ["rizos", "frizz", "hidratar"], libreDe: ["siliconas"], precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p025", tiendaId: "mirada-de-angel", nombre: "Shampoo para cabello graso (ejemplo)", categoria: "capilar", paso: "shampoo", tipoCabello: ["todos"], cuero: ["graso"], objetivos: ["grasa"], libreDe: [], precio: null, foto: "", fuente: "", pendiente: true },
  { id: "p026", tiendaId: "mirada-de-angel", nombre: "Acondicionador antifrizz (ejemplo)", categoria: "capilar", paso: "acondicionador", tipoCabello: ["liso", "ondulado", "rizado", "afro"], cuero: ["todos"], objetivos: ["frizz", "hidratar"], libreDe: ["parabenos"], precio: null, foto: "", fuente: "", pendiente: true }
];
