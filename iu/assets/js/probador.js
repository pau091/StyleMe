/*
 * PROBADOR VIRTUAL
 * ----------------
 * 1. La persona elige una prenda de una tienda de ropa y sube su foto.
 * 2. Un modelo de inteligencia artificial (MediaPipe Pose Landmarker, de Google)
 *    detecta los puntos del cuerpo: hombros y caderas.
 * 3. Con esos puntos se calcula dónde y de qué tamaño dibujar la prenda.
 * 4. La persona puede ajustar tamaño y posición a mano.
 *
 * Privacidad: el modelo se descarga y corre DENTRO del navegador.
 * La foto nunca se envía a un servidor.
 *
 * Este archivo es un "módulo" (type="module") porque importa MediaPipe.
 * Por eso la página debe abrirse con un servidor local (Live Server), no con doble clic.
 */

const VERSION_MEDIAPIPE = "0.10.14";
const URL_MEDIAPIPE = `https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision@${VERSION_MEDIAPIPE}`;
const URL_MODELO =
  "https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/1/pose_landmarker_lite.task";

const ANCHO_MAXIMO_LIENZO = 720;

// Puntos del cuerpo según MediaPipe
const HOMBRO_IZQ = 11, HOMBRO_DER = 12, CADERA_IZQ = 23, CADERA_DER = 24;

/* ---------- Elementos de la página ---------- */
const lienzo = document.getElementById("lienzo");
const ctx = lienzo.getContext("2d");
const selectorPrenda = document.getElementById("prenda");
const casillaConsentimiento = document.getElementById("consentimiento");
const campoFoto = document.getElementById("foto");
const ajustes = document.getElementById("ajustes");
const escala = document.getElementById("escala");
const moverX = document.getElementById("mover-x");
const moverY = document.getElementById("mover-y");
const quitarBlanco = document.getElementById("quitar-blanco");
const estado = document.getElementById("estado");
const infoPrenda = document.getElementById("info-prenda");
const contenedorLienzo = lienzo.parentElement; // .lienzo-contenedor (ahí va la franja de escaneo)
const menosMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)");

/* ---------- Estado ---------- */
let detector = null;        // modelo de IA (se carga la primera vez que se usa)
let fotoPersona = null;     // imagen subida
let fotoEscalada = null;    // la misma foto al tamaño del lienzo (para la IA)
let urlFotoPersona = null;
let imagenPrenda = null;
let posicionBase = null;    // { centroX, arriba, ancho } calculado por la IA

/* ---------- Prendas disponibles ---------- */
// Solo prendas con fotoProbador (PNG sin fondo). Las fotos de las tiendas tienen fondo
// y modelo, así que no sirven para ponerlas encima de otra persona.
// El catálogo llega del backend (api.js): este archivo es un módulo, así que puede esperarlo con await
await StyleMe.datos.catch(() => {});
const prendas = PRODUCTOS.filter((p) => p.categoria === "ropa" && p.fotoProbador);

if (!prendas.length) {
  selectorPrenda.innerHTML = '<option value="">Aún no hay prendas con foto</option>';
  selectorPrenda.disabled = true;
  estado.textContent = "Todavía no hay prendas con foto disponibles para el probador.";
} else {
  selectorPrenda.innerHTML = prendas
    .map((p) => {
      const tienda = buscarTienda(p.tiendaId);
      return `<option value="${escaparHTML(p.id)}">${escaparHTML(p.nombre)} · ${escaparHTML(tienda?.nombre)}</option>`;
    })
    .join("");
  const pedida = parametroURL("producto");
  if (pedida && prendas.some((p) => p.id === pedida)) selectorPrenda.value = pedida;
  else if (pedida) estado.textContent = "Esa prenda aún no tiene foto para el probador. Elige otra.";
  cargarPrenda();
}

dibujarMensaje("Aquí verás el resultado");

/* ---------- Funciones ---------- */

function dibujarMensaje(texto) {
  lienzo.width = 600;
  lienzo.height = 400;
  ctx.fillStyle = "#7d4253";
  ctx.font = "20px system-ui, sans-serif";
  ctx.textAlign = "center";
  ctx.fillText(texto, lienzo.width / 2, lienzo.height / 2);
}

function prendaSeleccionada() {
  return buscarProducto(selectorPrenda.value);
}

function cargarPrenda() {
  const producto = prendaSeleccionada();
  if (!producto) return;

  mostrarInfoPrenda(producto);
  imagenPrenda = new Image();
  imagenPrenda.onload = () => {
    if (fotoPersona) ubicarAutomaticamente();
  };
  imagenPrenda.onerror = () => {
    estado.textContent = "No se pudo cargar la foto de la prenda (foto_probador en la base de datos).";
  };
  imagenPrenda.src = producto.fotoProbador;
}

function mostrarInfoPrenda(producto) {
  const tienda = buscarTienda(producto.tiendaId);
  if (!tienda) {
    infoPrenda.innerHTML = "";
    return;
  }
  const mensaje = `Hola, vi en StyleMe la prenda "${producto.nombre}". ¿La tienen disponible?`;
  const whatsapp = enlaceWhatsapp(tienda.whatsapp, mensaje);
  infoPrenda.innerHTML = `
    <h2>${escaparHTML(producto.nombre)}</h2>
    <p class="precio">${formatoPrecio(producto.precio)}</p>
    <p>${escaparHTML(tienda.nombre)} · ${escaparHTML(tienda.direccion)}</p>
    <div class="grupo-botones">
      ${whatsapp ? `<a class="boton boton-whatsapp" href="${whatsapp}" target="_blank" rel="noopener" ${eventoTienda(tienda)}>Preguntar por WhatsApp</a>` : ""}
      <a class="boton boton-secundario" href="tienda.html?id=${encodeURIComponent(tienda.id)}">Ver tienda</a>
    </div>`;
}

// Carga el modelo de IA solo una vez
async function obtenerDetector() {
  if (detector) return detector;
  estado.textContent = "Cargando la inteligencia artificial… (solo la primera vez)";
  const { FilesetResolver, PoseLandmarker } = await import(`${URL_MEDIAPIPE}/vision_bundle.mjs`);
  const archivos = await FilesetResolver.forVisionTasks(`${URL_MEDIAPIPE}/wasm`);
  detector = await PoseLandmarker.createFromOptions(archivos, {
    baseOptions: { modelAssetPath: URL_MODELO, delegate: "CPU" },
    runningMode: "IMAGE",
    numPoses: 1
  });
  return detector;
}

// Usa la IA para encontrar hombros y caderas, y calcula dónde va la prenda
async function ubicarAutomaticamente() {
  if (!fotoPersona || !imagenPrenda?.complete) return;
  const producto = prendaSeleccionada();
  const W = lienzo.width;
  const H = lienzo.height;

  // Posición de respaldo por si la IA no encuentra a la persona
  posicionBase = { centroX: W / 2, arriba: H * 0.2, ancho: W * 0.5 };

  // Franja de luz rosa que recorre la foto mientras la IA trabaja (movimiento.css)
  contenedorLienzo.classList.add("escaneando");
  let seEncontro = false;

  try {
    const modelo = await obtenerDetector();
    estado.textContent = "Buscando tu silueta…";
    await esperarDosCuadros(); // deja que la franja se vea antes de que la IA ocupe el navegador
    const resultado = modelo.detect(fotoEscalada);
    const puntos = resultado.landmarks?.[0];

    if (!puntos) {
      estado.textContent = "No encontramos una persona en la foto. Ajusta la prenda a mano o prueba otra foto.";
    } else {
      const p = (i) => ({ x: puntos[i].x * W, y: puntos[i].y * H });
      const hIzq = p(HOMBRO_IZQ), hDer = p(HOMBRO_DER), cIzq = p(CADERA_IZQ), cDer = p(CADERA_DER);
      const proporcion = imagenPrenda.naturalHeight / imagenPrenda.naturalWidth;

      if (producto.tipoPrenda === "inferior") {
        const anchoCaderas = Math.abs(cIzq.x - cDer.x);
        const ancho = anchoCaderas * 2.6;
        posicionBase = {
          centroX: (cIzq.x + cDer.x) / 2,
          arriba: (cIzq.y + cDer.y) / 2 - ancho * proporcion * 0.05,
          ancho
        };
      } else {
        // "superior" y "completa" se ubican desde los hombros
        const anchoHombros = Math.abs(hIzq.x - hDer.x);
        const ancho = anchoHombros * 1.9;
        posicionBase = {
          centroX: (hIzq.x + hDer.x) / 2,
          arriba: (hIzq.y + hDer.y) / 2 - ancho * proporcion * 0.12,
          ancho
        };
      }
      estado.textContent = "¡Listo! Si hace falta, ajusta el tamaño y la posición.";
      seEncontro = true;
      mostrarPuntosIA([puntos[HOMBRO_IZQ], puntos[HOMBRO_DER], puntos[CADERA_IZQ], puntos[CADERA_DER]]);
    }
  } catch (error) {
    console.error(error);
    estado.textContent = "No se pudo cargar la IA (¿sin internet?). Puedes ajustar la prenda a mano.";
  } finally {
    contenedorLienzo.classList.remove("escaneando");
  }

  escala.value = 100;
  moverX.value = 0;
  moverY.value = 0;
  if (seEncontro) aparecerPrenda();
  else dibujar();
}

/* ---------- Detalles de movimiento ---------- */

function esperarDosCuadros() {
  return new Promise((resolver) => requestAnimationFrame(() => requestAnimationFrame(resolver)));
}

// Muestra por un momento los puntos que encontró la IA (hombros y caderas).
// Son solo un efecto visual: aparecen sobre la foto y se borran solos.
function mostrarPuntosIA(puntosNormalizados) {
  if (menosMovimiento.matches) return;
  contenedorLienzo.querySelectorAll(".punto-ia").forEach((p) => p.remove());
  // Se mide una sola vez dónde está el lienzo dentro de su recuadro
  const izquierda = lienzo.offsetLeft;
  const arriba = lienzo.offsetTop;
  const ancho = lienzo.clientWidth;
  const alto = lienzo.clientHeight;
  puntosNormalizados.forEach((punto, i) => {
    const marca = document.createElement("span");
    marca.className = "punto-ia";
    marca.setAttribute("aria-hidden", "true");
    marca.style.left = `${izquierda + punto.x * ancho}px`;
    marca.style.top = `${arriba + punto.y * alto}px`;
    marca.style.setProperty("--i", i);
    marca.addEventListener("animationend", () => marca.remove());
    contenedorLienzo.appendChild(marca);
  });
}

// La prenda aparece con un fundido de 400 ms.
// Basado en el tiempo real: dura lo mismo a 60, 120 o 144 Hz.
function aparecerPrenda(duracion = 400) {
  if (menosMovimiento.matches) return dibujar();
  const inicio = performance.now();
  function cuadro(ahora) {
    const t = Math.min((ahora - inicio) / duracion, 1);
    dibujar(t);
    if (t < 1) requestAnimationFrame(cuadro);
  }
  requestAnimationFrame(cuadro);
}

// Los deslizadores pueden mandar muchos cambios por cuadro: se dibuja solo una vez por cuadro
let dibujoPendiente = false;
function dibujarEnElSiguienteCuadro() {
  if (dibujoPendiente) return;
  dibujoPendiente = true;
  requestAnimationFrame(() => {
    dibujoPendiente = false;
    dibujar();
  });
}

// opacidadPrenda: de 0 (invisible) a 1 (normal); se usa para el fundido de entrada
function dibujar(opacidadPrenda = 1) {
  if (!fotoPersona) return;
  ctx.globalCompositeOperation = "source-over";
  ctx.clearRect(0, 0, lienzo.width, lienzo.height);
  ctx.drawImage(fotoPersona, 0, 0, lienzo.width, lienzo.height);

  if (!imagenPrenda?.complete || !posicionBase) return;

  const ancho = posicionBase.ancho * (Number(escala.value) / 100);
  const alto = ancho * (imagenPrenda.naturalHeight / imagenPrenda.naturalWidth);
  const x = posicionBase.centroX - ancho / 2 + (Number(moverX.value) / 100) * lienzo.width;
  const y = posicionBase.arriba + (Number(moverY.value) / 100) * lienzo.height;

  // "multiply" hace que el blanco de la foto de la prenda se vuelva transparente
  ctx.globalCompositeOperation = quitarBlanco.checked ? "multiply" : "source-over";
  ctx.globalAlpha = opacidadPrenda;
  ctx.drawImage(imagenPrenda, x, y, ancho, alto);
  ctx.globalAlpha = 1;
  ctx.globalCompositeOperation = "source-over";
}

function borrarFoto() {
  if (urlFotoPersona) URL.revokeObjectURL(urlFotoPersona);
  fotoPersona = null;
  fotoEscalada = null;
  urlFotoPersona = null;
  posicionBase = null;
  campoFoto.value = "";
  ajustes.disabled = true;
  dibujarMensaje("Foto borrada");
  estado.textContent = "Tu foto fue borrada.";
}

/* ---------- Eventos ---------- */

selectorPrenda.addEventListener("change", cargarPrenda);

casillaConsentimiento.addEventListener("change", () => {
  campoFoto.disabled = !casillaConsentimiento.checked || !prendas.length;
  if (!casillaConsentimiento.checked) borrarFoto();
});

campoFoto.addEventListener("change", () => {
  const archivo = campoFoto.files[0];
  if (!archivo) return;
  if (urlFotoPersona) URL.revokeObjectURL(urlFotoPersona);

  urlFotoPersona = URL.createObjectURL(archivo); // dirección temporal, solo en este navegador
  const imagen = new Image();
  imagen.onload = () => {
    // Ajustar el lienzo al tamaño de la foto (máximo 720 px de ancho)
    const factor = Math.min(1, ANCHO_MAXIMO_LIENZO / imagen.naturalWidth);
    lienzo.width = Math.round(imagen.naturalWidth * factor);
    lienzo.height = Math.round(imagen.naturalHeight * factor);
    fotoPersona = imagen;
    StyleMe.registrarEvento("USAR_PROBADOR", { producto: selectorPrenda.value });

    // Copia de la foto al tamaño del lienzo, para que los puntos de la IA coincidan
    const copia = document.createElement("canvas");
    copia.width = lienzo.width;
    copia.height = lienzo.height;
    copia.getContext("2d").drawImage(imagen, 0, 0, copia.width, copia.height);
    fotoEscalada = copia;

    ajustes.disabled = false;
    dibujar();
    ubicarAutomaticamente();
  };
  imagen.onerror = () => {
    estado.textContent = "No pudimos abrir esa imagen. Prueba con una foto JPG o PNG.";
  };
  imagen.src = urlFotoPersona;
});

[escala, moverX, moverY].forEach((c) => c.addEventListener("input", dibujarEnElSiguienteCuadro));
quitarBlanco.addEventListener("change", () => dibujar());
document.getElementById("reubicar").addEventListener("click", ubicarAutomaticamente);
document.getElementById("borrar").addEventListener("click", borrarFoto);
