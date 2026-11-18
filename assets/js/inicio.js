/* PÁGINA DE INICIO: collage, ventajas, categorías, banner, tiendas y herramientas */

const ICONO_CATEGORIA = { ropa: "gancho", maquillaje: "labial", skincare: "frasco", capilar: "peine" };

const VENTAJAS = [
  { icono: "tienda", titulo: "Tiendas locales", texto: "Marcas y emprendimientos de la ciudad" },
  { icono: "chat", titulo: "Contacto directo", texto: "Escríbeles por WhatsApp o llámalos" },
  { icono: "ubicacion", titulo: "Cómo llegar", texto: "Mapa y rutas a cada tienda" },
  { icono: "candado", titulo: "Tu privacidad", texto: "Tu foto no sale de tu celular" }
];

const HERRAMIENTAS = [
  {
    href: "probador.html",
    icono: "gancho",
    titulo: "Probador virtual",
    texto: "Sube tu foto y mira cómo te quedaría una prenda."
  },
  {
    href: "recomendador.html",
    icono: "destello",
    titulo: "¿Qué producto es para mí?",
    texto: "Recibe una rutina sugerida para tu piel o tu cabello."
  }
];

/*
 * ✏️ FOTOS DE LA PORTADA (collage al lado del título)
 * El orden importa:
 *   1.ª → la foto GRANDE (izquierda)
 *   2.ª → la foto pequeña de arriba
 *   3.ª → la foto pequeña de abajo
 * Para cambiar una foto, pon la nueva en assets/img/decoracion/ y cambia aquí su nombre.
 */
const FOTOS_PORTADA = [
  "assets/img/decoracion/modelo-maquillaje.jpg",
  "assets/img/decoracion/modelo-skincare.jpg",
  "assets/img/decoracion/modelo-cuidado-capilar.jpg"
];

// ✏️ FOTO DEL BANNER "Arma tu salida de compras" (a la derecha del texto)
const FOTO_BANNER = "assets/img/decoracion/bolsas-de-compras.jpg";

document.addEventListener("DOMContentLoaded", () => {
  // Collage de la portada
  document.getElementById("collage").innerHTML = FOTOS_PORTADA.map(
    (ruta) => `<div class="collage-foto"><img src="${escaparHTML(ruta)}" alt=""></div>`
  ).join("");

  // Banner de salida de compras
  document.getElementById("promo-imagenes").innerHTML =
    `<img src="${escaparHTML(FOTO_BANNER)}" alt="">`;

  // Ventajas
  document.getElementById("ventajas").innerHTML = VENTAJAS.map(
    (v) => `
      <div class="ventaja">
        ${icono(v.icono)}
        <div><strong>${v.titulo}</strong><span>${v.texto}</span></div>
      </div>`
  ).join("");

  // Categorías en círculos: cada una lleva al catálogo ya filtrado
  document.getElementById("lista-categorias").innerHTML = Object.entries(CATEGORIAS)
    .map(
      ([clave, cat]) => `
        <a class="categoria" href="productos.html?categoria=${clave}">
          <span class="categoria-circulo">${icono(ICONO_CATEGORIA[clave])}</span>
          ${cat.nombre}
        </a>`
    )
    .join("");

  // Tiendas separadas por tipo
  document.getElementById("tiendas-ropa").innerHTML = TIENDAS.filter((t) => t.tipo === "ropa")
    .map(tarjetaTienda)
    .join("");

  document.getElementById("tiendas-maquillaje").innerHTML = TIENDAS.filter((t) => t.tipo === "maquillaje")
    .map(tarjetaTienda)
    .join("");

  // Herramientas
  document.getElementById("herramientas").innerHTML = HERRAMIENTAS.map(
    (h) => `
      <a class="herramienta" href="${h.href}">
        <span class="icono-grande">${icono(h.icono)}</span>
        <div><h3>${h.titulo}</h3><p>${h.texto}</p></div>
      </a>`
  ).join("");
});
