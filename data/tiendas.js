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
 * telefono: (opcional) solo si NO tiene WhatsApp. Solo números. Ejemplo fijo: "6076571340"
 * instagram: solo el usuario, sin @. Ejemplo: "styleme.bga"
 * web: página web sin https://. Ejemplo: "bohor.com.co"
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
    lat: 7.1209,
    lng: -73.1088,
    horario: "Por confirmar",
    whatsapp: "",
    instagram: "",
    web: "",
    foto: "assets/img/tiendas/samylu-boutique.webp",
    verificado: false
  },
  {
    id: "bohor",
    nombre: "BOHOR",
    tipo: "ropa",
    descripcion: "Tienda de ropa y moda.",
    direccion: "Carrera 36 #53-32, Cabecera del Llano",
    zona: "Cabecera",
    lat: 7.1118,
    lng: -73.1080,
    horario: "Lun–Sáb 10:00–19:00 · Dom 11:00–16:00",
    whatsapp: "573012951439",
    instagram: "",
    web: "bohor.com.co",
    foto: "assets/img/tiendas/bohor.jpg",
    verificado: false
  },
  {
    id: "queen",
    nombre: "Queen",
    tipo: "ropa",
    descripcion: "Ropa de segunda mano en Cabecera del Llano.",
    direccion: "Carrera 35 #52-146, Cabecera del Llano",
    zona: "Cabecera",
    lat: 7.1121,
    lng: -73.1091,
    horario: "Lun–Sáb 9:30–11:30 y 14:30–18:30 · Dom cerrado",
    whatsapp: "",
    telefono: "6076571340",
    instagram: "",
    web: "",
    foto: "assets/img/tiendas/queen.webp",
    verificado: false
  },
  {
    id: "henars-eleganzza",
    nombre: "Henar's y Eleganzza",
    tipo: "ropa",
    descripcion: "Ropa de alta costura para dama en tallas grandes, de la diseñadora Sandra Soto.",
    direccion: "Avenida Calle 105 #21-93",
    zona: "Sur",
    lat: 7.0855,
    lng: -73.1162,
    horario: "Por confirmar",
    whatsapp: "",
    instagram: "",
    web: "",
    foto: "assets/img/tiendas/henars-eleganzza.webp",
    verificado: false
  },
  {
    id: "mattelsa",
    nombre: "Mattelsa",
    tipo: "ropa",
    descripcion: "Marca colombiana de ropa casual. Sede La Aurora.",
    direccion: "Carrera 31 #32-45, La Aurora",
    zona: "La Aurora",
    lat: 7.1260,
    lng: -73.1146,
    horario: "Lun–Sáb 10:00–20:00 · Dom 10:00–18:00",
    whatsapp: "573005071000",
    instagram: "",
    web: "",
    foto: "assets/img/tiendas/mattelsa.webp",
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
    web: "pandoramaquillaje.com",
    foto: "assets/img/tiendas/pandora-makeup.png",
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
    web: "guacamayacolors.com",
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
    web: "gogomakeup.com.co",
    foto: "assets/img/tiendas/gogo-makeup.jpg",
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
    web: "",
    foto: "assets/img/tiendas/so-beauty.jpg",
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
    web: "miradadeangel.com.co",
    foto: "assets/img/tiendas/mirada-de-angel.jpg",
    verificado: false
  }
];
