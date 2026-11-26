/*
 * DEMO DEL PROBADOR EN LA PORTADA (idea 6)
 * ----------------------------------------
 * Un maniquí dibujado (SVG) y prendas reales del catálogo encima. Se elige una prenda y con el
 * deslizador se compara "antes" (maniquí solo) y "con la prenda".
 * Usa el mismo recorte y la misma forma de acomodar la prenda que el probador (probador-prendas.js),
 * pero en su versión liviana: solo prendas fotografiadas solas sobre fondo claro, sin modelos de IA.
 */
import { prepararPrendaPlana, transformacionAlCuerpo } from "./probador-prendas.js?v=20261126";

// ✏️ Prendas de la demo (códigos de producto). Deben ser fotos de la prenda SOLA sobre fondo claro.
const PRENDAS_DEMO = ["p008", "p005", "p006", "p007"];

// Hombros y caderas del maniquí, en píxeles del lienzo (el dibujo mide 240 × 520 y el lienzo es el doble)
const CUERPO_MANIQUI = {
  hombroIzq: { x: 168 * 2, y: 112 * 2 },
  hombroDer: { x: 72 * 2, y: 112 * 2 },
  caderaIzq: { x: 142 * 2, y: 262 * 2 },
  caderaDer: { x: 98 * 2, y: 262 * 2 }
};

const escena = document.getElementById("demo-escena");
const lienzo = document.getElementById("demo-prenda");
const ctx = lienzo.getContext("2d");
const deslizador = document.getElementById("demo-comparar");
const botones = document.getElementById("demo-prendas");
const enlace = document.getElementById("demo-enlace");
const menosMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

await StyleMe.datos.catch(() => {});
const prendas = PRENDAS_DEMO.map(buscarProducto).filter((p) => p?.foto);

if (!prendas.length) {
  document.getElementById("demo-probador").hidden = true;
} else {
  botones.innerHTML = prendas.map((p, i) => `
    <button type="button" class="demo-boton" data-codigo="${escaparHTML(p.id)}" aria-pressed="${i === 0}" title="${escaparHTML(p.nombre)}">
      <img src="${escaparHTML(p.foto)}" alt="${escaparHTML(p.nombre)}" loading="lazy" decoding="async">
    </button>`).join("");
  botones.addEventListener("click", (evento) => {
    const boton = evento.target.closest(".demo-boton");
    if (boton) ponerPrenda(boton.dataset.codigo);
  });
  deslizador.addEventListener("input", () => escena.style.setProperty("--corte", `${deslizador.value}%`));
  escena.style.setProperty("--corte", "50%");

  // La primera prenda se prepara cuando la tarjeta está por aparecer, y el deslizador hace un barrido de muestra
  const vigia = new IntersectionObserver((entradas) => {
    if (!entradas.some((e) => e.isIntersecting)) return;
    vigia.disconnect();
    ponerPrenda(prendas[0].id).then(barridoDeMuestra);
  }, { rootMargin: "200px 0px" });
  vigia.observe(escena);
}

async function ponerPrenda(codigo) {
  const producto = buscarProducto(codigo);
  botones.querySelectorAll(".demo-boton").forEach((b) => b.setAttribute("aria-pressed", String(b.dataset.codigo === codigo)));
  enlace.href = `probador.html?producto=${encodeURIComponent(codigo)}`;
  escena.classList.add("cargando");
  try {
    const prenda = await prepararPrendaPlana(producto);
    const { matriz } = transformacionAlCuerpo(prenda, CUERPO_MANIQUI, producto.tipoPrenda);
    ctx.resetTransform();
    ctx.clearRect(0, 0, lienzo.width, lienzo.height);
    ctx.setTransform(matriz);
    ctx.drawImage(prenda.lienzo, 0, 0);
    ctx.resetTransform();
  } catch (e) {
    ctx.clearRect(0, 0, lienzo.width, lienzo.height); // sin la foto, queda el maniquí solo
  } finally {
    escena.classList.remove("cargando");
  }
}

// El corte va de un lado al otro una vez, para que se entienda que se puede deslizar
function barridoDeMuestra() {
  if (menosMovimiento) return;
  const inicio = performance.now();
  const DURACION = 1600;
  function cuadro(ahora) {
    const t = Math.min((ahora - inicio) / DURACION, 1);
    const valor = 50 + Math.sin(t * Math.PI * 2) * 30 * (1 - t); // 50 → 80 → 20 → 50, cada vez más suave
    escena.style.setProperty("--corte", `${valor}%`);
    deslizador.value = Math.round(valor);
    if (t < 1) requestAnimationFrame(cuadro);
  }
  requestAnimationFrame(cuadro);
}
