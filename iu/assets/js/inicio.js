/*
 * PÁGINA DE INICIO: collage, categorías, cintas, herramientas con IA (cuadrícula bento) y tiendas.
 * Las otras partes de la portada están en archivos aparte:
 *   inicio-buscador.js  → buscador con IA (resultados mientras escribes)
 *   inicio-cerca.js     → "Abiertas ahora", mini mapa y tiendas cercanas
 *   inicio-comunidad.js → lo más buscado (datos anónimos) y reseñas recientes
 *   inicio-probador.js  → demo del probador con el maniquí
 */

/*
 * ✏️ FOTOS DE LAS CATEGORÍAS (círculos de "Explora por categoría")
 * Al hacer clic en un círculo, la persona va al catálogo con esa categoría ya filtrada.
 * ⚡ Se usan las versiones LIVIANAS de assets/img/categorias/ (480 px, ~40 KB cada una).
 *    (Las originales pesadas se borraron porque no se usaban.)
 *    Si cambias una foto, guárdala pequeña: unos 480 px de ancho y menos de 100 KB.
 *   foto   → ruta de la imagen
 *   centro → qué parte de la foto se ve dentro del círculo
 *            ("50% 50%" = el centro; "20% 30%" = más a la izquierda y arriba)
 */
const FOTOS_CATEGORIA = {
  ropa: { foto: "assets/img/categorias/ropa.jpg", centro: "24% 40%" },
  maquillaje: { foto: "assets/img/categorias/maquillaje.jpg", centro: "50% 50%" },
  skincare: { foto: "assets/img/categorias/skincare.jpg", centro: "55% 35%" },
  capilar: { foto: "assets/img/categorias/capilar.jpg", centro: "50% 40%" }
};

/*
 * ✏️ CINTAS CRUZADAS (las dos franjas inclinadas que corren entre secciones)
 * Cambia o agrega palabras aquí. Se repiten solas para llenar toda la franja.
 */
const PALABRAS_CINTA_ROSA = ["Ropa y moda", "Maquillaje", "Skincare", "Cuidado capilar"];
const PALABRAS_CINTA_CIRUELA = ["Hecho en Bucaramanga", "Tiendas locales", "Vamos de compras"];

/*
 * ✏️ PREGUNTAS DE EJEMPLO PARA LÍA (tarjeta grande de "Herramientas con IA")
 * Al tocar una, Lía se abre y la responde con el catálogo real (RAG).
 */
const PREGUNTAS_LIA = [
  "¿Qué me pongo para un matrimonio de día?",
  "Busco una base para piel grasa",
  "¿Dónde consigo un jean en Cabecera?"
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


document.addEventListener("DOMContentLoaded", () => {
  // Collage de la portada. Cada foto se descubre como una persiana (data-revelar="telon"),
  // una después de la otra (--orden). La primera es la más importante: se pide primero.
  document.getElementById("collage").innerHTML = FOTOS_PORTADA.map(
    (ruta, i) => `
      <div class="collage-foto" data-revelar="telon" style="--orden:${i + 1}">
        <img src="${escaparHTML(ruta)}" alt="" decoding="async" ${i === 0 ? 'fetchpriority="high"' : ""}>
      </div>`
  ).join("");

  document.querySelector(".bento-recomendador .icono-grande").innerHTML = icono("destello");

  // Preguntas de ejemplo para Lía: abren a Lía y le preguntan
  const preguntas = document.getElementById("preguntas-lia");
  preguntas.innerHTML = PREGUNTAS_LIA.map(
    (p) => `<button type="button" class="pregunta-lia">${icono("chat")}<span>${escaparHTML(p)}</span></button>`
  ).join("");
  preguntas.addEventListener("click", (evento) => {
    const boton = evento.target.closest(".pregunta-lia");
    if (!boton) return;
    if (!window.Lia) return (window.location.hash = "lia");
    window.Lia.abrir();
    window.Lia.enviar(boton.textContent.trim());
  });

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

  // Cintas cruzadas: cada franja lleva su texto repetido dos veces (una mitad
  // es copia de la otra), así al correr hasta la mitad vuelve a empezar sin corte.
  const textoCinta = (palabras) => {
    const mitad = Array(3).fill(palabras).flat()
      .map((p) => `<span>${escaparHTML(p)}</span>`)
      .join("");
    return `<div class="cinta-pista">${mitad}${mitad}</div>`;
  };
  document.getElementById("cintas").innerHTML = `
    <div class="cinta-cruzada cinta-ciruela">${textoCinta(PALABRAS_CINTA_CIRUELA)}</div>
    <div class="cinta-cruzada cinta-rosa">${textoCinta(PALABRAS_CINTA_ROSA)}</div>`;

  // Tiendas separadas por tipo (llegan del backend: se pintan cuando estén listas)
  cuandoHayaDatos(() => {
    document.getElementById("tiendas-ropa").innerHTML = TIENDAS.filter((t) => t.tipo === "ropa")
      .map(tarjetaTienda)
      .join("");

    document.getElementById("tiendas-maquillaje").innerHTML = TIENDAS.filter((t) => t.tipo === "maquillaje")
      .map(tarjetaTienda)
      .join("");
  });
});

/*
 * CARRUSEL (productos más vistos y reseñas): una fila que se desliza con el dedo, la rueda o el teclado.
 * En computador se agregan dos flechas. No usa librerías: es CSS (scroll-snap) + este poquito de JS.
 */
function activarCarrusel(carrusel) {
  if (carrusel.dataset.carruselListo) return;
  carrusel.dataset.carruselListo = "1";
  const envoltura = document.createElement("div");
  envoltura.className = "carrusel-envoltura";
  carrusel.before(envoltura);
  envoltura.append(carrusel);
  const flecha = (direccion, texto) => {
    const boton = document.createElement("button");
    boton.type = "button";
    boton.className = `carrusel-flecha ${direccion}`;
    boton.setAttribute("aria-label", texto);
    boton.textContent = direccion === "anterior" ? "‹" : "›";
    boton.addEventListener("click", () => {
      const paso = carrusel.clientWidth * 0.8 * (direccion === "anterior" ? -1 : 1);
      carrusel.scrollBy({ left: paso, behavior: matchMedia("(prefers-reduced-motion: reduce)").matches ? "auto" : "smooth" });
    });
    return boton;
  };
  const anterior = flecha("anterior", "Ver los anteriores");
  const siguiente = flecha("siguiente", "Ver los siguientes");
  envoltura.append(anterior, siguiente);
  const actualizar = () => {
    anterior.disabled = carrusel.scrollLeft < 8;
    siguiente.disabled = carrusel.scrollLeft + carrusel.clientWidth > carrusel.scrollWidth - 8;
  };
  carrusel.addEventListener("scroll", actualizar, { passive: true });
  window.addEventListener("resize", actualizar);
  actualizar();
}
