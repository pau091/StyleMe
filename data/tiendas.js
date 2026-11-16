/*
 * TIENDAS DE STYLEME
 * ------------------
 * Cada tienda es un objeto dentro de la lista.
 *
 * ⚠️ POR VERIFICAR antes de publicar: dirección, horario, WhatsApp e Instagram.
 *    Los datos salen de directorios de internet y pueden estar desactualizados.
 *
 * Cómo sacar lat y lng: en Google Maps, clic derecho sobre el local →
 * clic en los números que aparecen arriba (se copian) → pegarlos aquí.
 * Ejemplo: lat: 7.1189, lng: -73.1105
 * Mientras lat y lng estén en null, la tienda NO aparece en el mapa ni en la salida de compras.
 *
 * whatsapp: solo números, con el 57 adelante. Ejemplo: "573001234567"
 * instagram: solo el usuario, sin @. Ejemplo: "styleme.bga"
 */
const TIENDAS = [
  // ---------- ROPA ----------
  {
    id: "samylu-boutique",
    nombre: "Samylu Boutique",
    tipo: "ropa",
    descripcion: "Boutique de ropa para mujer en Cabecera del Llano.",
    direccion: "Carrera 37 #42-75, Cabecera del Llano",
    zona: "Cabecera",
    lat: null,
    lng: null,
    horario: "Por confirmar",
    whatsapp: "",
    instagram: "",
    foto: "",
    verificado: false
  },
  {
    id: "bohor",
    nombre: "BOHOR",
    tipo: "ropa",
    descripcion: "Tienda de ropa y moda.",
    direccion: "Carrera 36 #53-32",
    zona: "Cabecera",
    lat: null,
    lng: null,
    horario: "Por confirmar",
    whatsapp: "",
    instagram: "",
    foto: "",
    verificado: false
  },
  {
    id: "sea-and-seams",
    nombre: "Sea & Seams",
    tipo: "ropa",
    descripcion: "Ropa de segunda mano seleccionada.",
    direccion: "Carrera 17B1 #57-70, Ricaurte",
    zona: "Ricaurte",
    lat: null,
    lng: null,
    horario: "Por confirmar",
    whatsapp: "",
    instagram: "",
    foto: "",
    verificado: false
  },
  {
    id: "henars-eleganzza",
    nombre: "Henar's y Eleganzza",
    tipo: "ropa",
    descripcion: "Ropa para dama en tallas grandes y pequeñas.",
    direccion: "Carrera 22 #45-13, La Concordia",
    zona: "La Concordia",
    lat: null,
    lng: null,
    horario: "Por confirmar",
    whatsapp: "",
    instagram: "",
    foto: "",
    verificado: false
  },
  {
    id: "mattelsa",
    nombre: "Mattelsa",
    tipo: "ropa",
    descripcion: "Marca colombiana de ropa casual.",
    direccion: "Carrera 31 #32-45",
    zona: "Por confirmar",
    lat: null,
    lng: null,
    horario: "Por confirmar",
    whatsapp: "",
    instagram: "",
    foto: "",
    verificado: false
  },

  // ---------- MAQUILLAJE ----------
  {
    id: "pandora-makeup",
    nombre: "Pandora Makeup",
    tipo: "maquillaje",
    descripcion: "Tienda de maquillaje en Cabecera.",
    direccion: "Carrera 33 #46-59, Cabecera",
    zona: "Cabecera",
    lat: null,
    lng: null,
    horario: "Por confirmar",
    whatsapp: "573213273357",
    instagram: "",
    foto: "",
    verificado: false
  },
  {
    id: "guacamaya-colors",
    nombre: "Guacamaya Colors",
    tipo: "maquillaje",
    descripcion: "Maquillaje y skincare nacional e importado.",
    direccion: "Carrera 36 #51-120, Cabecera del Llano",
    zona: "Cabecera",
    lat: null,
    lng: null,
    horario: "Por confirmar",
    whatsapp: "",
    instagram: "",
    foto: "",
    verificado: false
  },
  {
    id: "gogo-makeup",
    nombre: "Gogó Makeup",
    tipo: "maquillaje",
    descripcion: "Tienda de maquillaje en Cabecera.",
    direccion: "Carrera 35 #52-103, Cabecera",
    zona: "Cabecera",
    lat: null,
    lng: null,
    horario: "Lun–Sáb 9:00–20:00 · Dom 10:00–18:00",
    whatsapp: "",
    instagram: "",
    foto: "",
    verificado: false
  },
  {
    id: "so-beauty",
    nombre: "So Beauty Maquillaje",
    tipo: "maquillaje",
    descripcion: "Maquillaje al detal y por mayor, con más de 600 referencias.",
    direccion: "Carrera 36 #52-28, local 1, Cabecera",
    zona: "Cabecera",
    lat: null,
    lng: null,
    horario: "Por confirmar",
    whatsapp: "",
    instagram: "sobeautymaquillajebga",
    foto: "",
    verificado: false
  },
  {
    id: "mirada-de-angel",
    nombre: "Mirada de Ángel",
    tipo: "maquillaje",
    descripcion: "Cosméticos, maquillaje y cuidado capilar.",
    direccion: "Carrera 23 #35-14, local 1, Edificio Zentri",
    zona: "Por confirmar",
    lat: null,
    lng: null,
    horario: "Por confirmar",
    whatsapp: "573222209159",
    instagram: "",
    foto: "",
    verificado: false
  }
];
