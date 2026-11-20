/* PÁGINA DE INICIO: collage, categorías, banner, tiendas y herramientas */

/*
 * ✏️ FOTOS DE LAS CATEGORÍAS (círculos de "Explora por categoría")
 * Las fotos están en assets/img/logos/. Al hacer clic en un círculo,
 * la persona va al catálogo con esa categoría ya filtrada.
 *   foto   → ruta de la imagen
 *   centro → qué parte de la foto se ve dentro del círculo
 *            ("50% 50%" = el centro; "20% 30%" = más a la izquierda y arriba)
 */
const FOTOS_CATEGORIA = {
  ropa: { foto: "assets/img/logos/ropa y moda.jpg", centro: "24% 40%" },
  maquillaje: { foto: "assets/img/logos/logo maquillaje.png", centro: "50% 50%" },
  skincare: { foto: "assets/img/logos/skincare.jpg", centro: "55% 35%" },
  capilar: { foto: "assets/img/logos/cuidado capilar.jpg", centro: "50% 40%" }
};

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
  // Collage de la portada. Cada foto se descubre como una persiana (data-revelar="telon"),
  // una después de la otra (--orden). La primera es la más importante: se pide primero.
  document.getElementById("collage").innerHTML = FOTOS_PORTADA.map(
    (ruta, i) => `
      <div class="collage-foto" data-revelar="telon" style="--orden:${i + 1}">
        <img src="${escaparHTML(ruta)}" alt="" decoding="async" ${i === 0 ? 'fetchpriority="high"' : ""}>
      </div>`
  ).join("");

  // Banner de salida de compras (está más abajo: se carga cuando la persona se acerca)
  document.getElementById("promo-imagenes").innerHTML =
    `<img src="${escaparHTML(FOTO_BANNER)}" alt="" loading="lazy" decoding="async">`;

  // Categorías en círculos con foto: cada una lleva al catálogo ya filtrado
  document.getElementById("lista-categorias").innerHTML = Object.entries(CATEGORIAS)
    .map(([clave, cat]) => {
      const foto = FOTOS_CATEGORIA[clave];
      return `
        <a class="categoria" href="productos.html?categoria=${clave}" aria-label="Ver productos de ${escaparHTML(cat.nombre)}">
          <span class="categoria-circulo">
            <span class="categoria-recorte">
              <img src="${encodeURI(foto.foto)}" alt="" loading="lazy" decoding="async"
                   style="object-position: ${foto.centro}">
            </span>
          </span>
          ${cat.nombre}
        </a>`;
    })
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
