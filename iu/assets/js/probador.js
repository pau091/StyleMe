/*
 * PROBADOR VIRTUAL RÁPIDO (sin servicios externos)
 * ------------------------------------------------
 * 1. La persona elige una prenda de una tienda de ropa.
 * 2. La prenda se recorta sola desde la foto de la tienda (probador-prendas.js).
 * 3. La persona sube una foto o enciende la CÁMARA (computador o celular).
 * 4. Un modelo de IA (MediaPipe Pose Landmarker, de Google) encuentra hombros y caderas,
 *    y la prenda se acomoda a esos puntos. Con la cámara, la prenda sigue a la persona en vivo.
 * 5. Con la cámara se pueden tomar hasta 4 fotos (con cuenta regresiva) y guardarlas.
 *
 * Privacidad: los modelos se descargan y corren DENTRO del navegador.
 * Ni la foto ni el video se envían a un servidor.
 *
 * Este archivo es un "módulo" (type="module") porque importa MediaPipe.
 * Por eso la página debe abrirse con un servidor (Docker o Live Server), no con doble clic.
 */
import { prepararPrenda, HOMBRO_IZQ, HOMBRO_DER, CADERA_IZQ, CADERA_DER } from "./probador-prendas.js";

const VERSION_MEDIAPIPE = "0.10.14";
const URL_MEDIAPIPE = `https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision@${VERSION_MEDIAPIPE}`;
const URL_MODELO_POSE =
  "https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/1/pose_landmarker_lite.task";
const URL_MODELO_ROPA =
  "https://storage.googleapis.com/mediapipe-models/image_segmenter/selfie_multiclass_256x256/float32/1/selfie_multiclass_256x256.tflite";

const ANCHO_MAXIMO_LIENZO = 720;
const SUAVIZADO = 0.45;          // con la cámara: cuánto se mueve la prenda hacia la nueva posición en cada cuadro
const CUADROS_SIN_PERSONA = 12;  // con la cámara: cuadros sin ver a la persona antes de ocultar la prenda
const MAXIMO_CAPTURAS = 4;
const POSES_SUGERIDAS = ["de frente", "tres cuartos", "de perfil", "caminando"];

/* ---------- Elementos de la página ---------- */
const lienzo = document.getElementById("lienzo");
const ctx = lienzo.getContext("2d");
const contenedorLienzo = lienzo.parentElement; // .lienzo-contenedor (ahí va la franja de escaneo)
const selectorPrenda = document.getElementById("prenda");
const casillaConsentimiento = document.getElementById("consentimiento");
const campoFoto = document.getElementById("foto");
const botonCamara = document.getElementById("camara");
const controlesCamara = document.getElementById("controles-camara");
const botonCapturar = document.getElementById("capturar");
const botonApagar = document.getElementById("apagar-camara");
const capturas = document.getElementById("capturas");
const ajustes = document.getElementById("ajustes");
const escala = document.getElementById("escala");
const moverX = document.getElementById("mover-x");
const moverY = document.getElementById("mover-y");
const estado = document.getElementById("estado");
const infoPrenda = document.getElementById("info-prenda");
const menosMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)");
// El video de la cámara no se muestra: se dibuja en el lienzo junto con la prenda.
// Queda dentro de la página (invisible) porque algunos celulares (iPhone) no lo reproducen si está afuera.
const video = document.createElement("video");
video.muted = true;
video.playsInline = true;
video.setAttribute("aria-hidden", "true");
video.className = "video-oculto";
contenedorLienzo.appendChild(video);

/* ---------- Estado ---------- */
let modo = "nada";          // "nada" | "foto" | "camara"
let fotoPersona = null;     // foto subida, al tamaño del lienzo
let puntosPersona = null;   // hombros y caderas de la persona, en píxeles del lienzo
let prenda = null;          // { lienzo, anclas, estimadas } de probador-prendas.js
let flujoCamara = null;
let ultimoTiempoVideo = -1;
let tamanoVideo = "";       // si la cámara cambia de tamaño, el lienzo se ajusta
let cuadrosSinPersona = 0;
let cuentaRegresiva = null;
let urlsCapturas = [];
let generacionCapturas = 0; // cambia al borrar: una captura que estaba guardándose ya no se muestra

/* ---------- Modelos de IA (se cargan la primera vez que se usan) ---------- */
let archivosVision = null;
const modelos = {};

async function vision() {
  if (!archivosVision) {
    const modulo = await import(`${URL_MEDIAPIPE}/vision_bundle.mjs`);
    archivosVision = { modulo, archivos: await modulo.FilesetResolver.forVisionTasks(`${URL_MEDIAPIPE}/wasm`) };
  }
  return archivosVision;
}

function unaVez(nombre, crear) {
  return () => (modelos[nombre] ??= crear().catch((error) => {
    delete modelos[nombre];
    throw error;
  }));
}

const ia = {
  detectorFotos: unaVez("fotos", async () => {
    const { modulo, archivos } = await vision();
    return modulo.PoseLandmarker.createFromOptions(archivos, {
      baseOptions: { modelAssetPath: URL_MODELO_POSE, delegate: "CPU" }, runningMode: "IMAGE", numPoses: 1
    });
  }),
  detectorVideo: unaVez("video", async () => {
    const { modulo, archivos } = await vision();
    return modulo.PoseLandmarker.createFromOptions(archivos, {
      baseOptions: { modelAssetPath: URL_MODELO_POSE, delegate: "CPU" }, runningMode: "VIDEO", numPoses: 1
    });
  }),
  segmentador: unaVez("ropa", async () => {
    const { modulo, archivos } = await vision();
    return modulo.ImageSegmenter.createFromOptions(archivos, {
      baseOptions: { modelAssetPath: URL_MODELO_ROPA, delegate: "CPU" },
      runningMode: "IMAGE", outputCategoryMask: false, outputConfidenceMasks: true
    });
  })
};

/* ---------- Prendas disponibles ---------- */
// Prendas de ropa con foto de la tienda (se recortan solas) o con foto_probador (PNG sin fondo).
// El catálogo llega del backend (api.js): este archivo es un módulo, así que puede esperarlo con await
await StyleMe.datos.catch(() => {});
const prendas = PRODUCTOS.filter((p) => p.categoria === "ropa" && (p.fotoProbador || /^https:\/\//.test(p.foto ?? "")));

if (!navigator.mediaDevices?.getUserMedia) botonCamara.hidden = true;

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

/* ---------- Prenda ---------- */

function prendaSeleccionada() {
  return buscarProducto(selectorPrenda.value);
}

async function cargarPrenda() {
  const producto = prendaSeleccionada();
  if (!producto) return;
  mostrarInfoPrenda(producto);
  prenda = null;
  estado.textContent = "Preparando la prenda… (la primera vez tarda un poco más)";
  contenedorLienzo.classList.add("escaneando");
  try {
    const lista = await prepararPrenda(producto, ia);
    if (prendaSeleccionada() !== producto) return; // la persona eligió otra mientras tanto
    prenda = lista;
    estado.textContent = modo === "nada"
      ? "Prenda lista. Ahora sube tu foto o usa la cámara."
      : "¡Listo! Si hace falta, ajusta el tamaño y la posición.";
    if (modo === "foto") aparecerPrenda();
  } catch (error) {
    console.error(error);
    estado.textContent = "No se pudo preparar la prenda (¿sin internet?). Prueba con otra.";
  } finally {
    contenedorLienzo.classList.remove("escaneando");
  }
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

/* ---------- Dónde va la prenda ---------- */

function puntosDe(landmarks) {
  const p = (i) => ({ x: landmarks[i].x * lienzo.width, y: landmarks[i].y * lienzo.height });
  return { hombroIzq: p(HOMBRO_IZQ), hombroDer: p(HOMBRO_DER), caderaIzq: p(CADERA_IZQ), caderaDer: p(CADERA_DER) };
}

const medio = (a, b) => ({ x: (a.x + b.x) / 2, y: (a.y + b.y) / 2 });
const distancia = (a, b) => Math.hypot(a.x - b.x, a.y - b.y);

const grados = (v) => (Math.atan2(v.y, v.x) * 180) / Math.PI;

// Lleva la prenda al cuerpo de la persona:
//  - punto de apoyo: hombros (prendas de arriba y vestidos) o caderas (prendas de abajo)
//  - giro: el eje del cuerpo (de los hombros a las caderas), así sigue a la persona si se inclina
//  - tamaño: por el ancho (hombros o caderas), pero nunca menos que 3/4 del largo del torso.
//    El largo del torso casi no cambia cuando la persona se pone de lado; el ancho sí.
//  - con anclas reales (foto con modelo), el largo de la prenda sigue exactamente el largo del torso
function transformacionPrenda() {
  const A = prenda.anclas;
  const P = puntosPersona;
  if (!P) {
    // Sin persona encontrada: la prenda al centro, a la mitad del ancho
    const factor = (lienzo.width * 0.5) / prenda.lienzo.width;
    return { matriz: new DOMMatrix().translate(lienzo.width * 0.25, lienzo.height * 0.2).scale(factor), pivote: { x: lienzo.width / 2, y: lienzo.height / 2 } };
  }
  const abajo = prendaSeleccionada()?.tipoPrenda === "inferior";
  const hombrosA = medio(A.hombroIzq, A.hombroDer), caderasA = medio(A.caderaIzq, A.caderaDer);
  const hombrosP = medio(P.hombroIzq, P.hombroDer), caderasP = medio(P.caderaIzq, P.caderaDer);
  const anchoA = abajo ? distancia(A.caderaIzq, A.caderaDer) : distancia(A.hombroIzq, A.hombroDer);
  const anchoP = abajo ? distancia(P.caderaIzq, P.caderaDer) : distancia(P.hombroIzq, P.hombroDer);
  const kAncho = anchoP / (anchoA || 1);
  const kTorso = distancia(hombrosP, caderasP) / (distancia(hombrosA, caderasA) || 1);
  let kx = Math.max(kAncho, kTorso * 0.75);
  let ky = kx;
  if (!abajo && !prenda.estimadas) {
    ky = kTorso;
    kx = Math.min(kTorso * 1.2, kx);
  }
  const ejeA = grados({ x: caderasA.x - hombrosA.x, y: caderasA.y - hombrosA.y }) - 90;
  const ejeP = grados({ x: caderasP.x - hombrosP.x, y: caderasP.y - hombrosP.y }) - 90;
  const origen = abajo ? caderasA : hombrosA;
  const destino = abajo ? caderasP : hombrosP;
  const matriz = new DOMMatrix()
    .translate(destino.x, destino.y)
    .rotate(ejeP)
    .scale(kx, ky)
    .rotate(-ejeA)
    .translate(-origen.x, -origen.y);
  return { matriz, pivote: destino };
}

/* ---------- Dibujo ---------- */

function dibujarMensaje(texto) {
  lienzo.width = 600;
  lienzo.height = 400;
  ctx.fillStyle = "#7d4253";
  ctx.font = "20px system-ui, sans-serif";
  ctx.textAlign = "center";
  ctx.fillText(texto, lienzo.width / 2, lienzo.height / 2);
}

// opacidadPrenda: de 0 (invisible) a 1 (normal); se usa para el fundido de entrada
function dibujar(opacidadPrenda = 1) {
  const fondo = modo === "camara" ? video : fotoPersona;
  if (!fondo) return;
  ctx.resetTransform();
  ctx.clearRect(0, 0, lienzo.width, lienzo.height);
  ctx.drawImage(fondo, 0, 0, lienzo.width, lienzo.height);
  if (!prenda || (modo === "camara" && !puntosPersona)) return;

  const { matriz, pivote } = transformacionPrenda();
  if (!matriz) return;
  const k = Number(escala.value) / 100;
  const ajuste = new DOMMatrix()
    .translate((Number(moverX.value) / 100) * lienzo.width, (Number(moverY.value) / 100) * lienzo.height)
    .translate(pivote.x, pivote.y).scale(k).translate(-pivote.x, -pivote.y);
  ctx.setTransform(ajuste.multiply(matriz));
  ctx.globalAlpha = opacidadPrenda;
  ctx.drawImage(prenda.lienzo, 0, 0);
  ctx.globalAlpha = 1;
  ctx.resetTransform();
}

// Los deslizadores pueden mandar muchos cambios por cuadro: se dibuja solo una vez por cuadro
let dibujoPendiente = false;
function dibujarEnElSiguienteCuadro() {
  if (dibujoPendiente || modo === "camara") return; // con la cámara ya se dibuja en cada cuadro
  dibujoPendiente = true;
  requestAnimationFrame(() => {
    dibujoPendiente = false;
    dibujar();
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

function esperarDosCuadros() {
  return new Promise((resolver) => requestAnimationFrame(() => requestAnimationFrame(resolver)));
}

// Muestra por un momento los puntos que encontró la IA (hombros y caderas). Solo es un efecto visual.
function mostrarPuntosIA(puntos) {
  if (menosMovimiento.matches) return;
  contenedorLienzo.querySelectorAll(".punto-ia").forEach((p) => p.remove());
  const escalaX = lienzo.clientWidth / lienzo.width;
  const escalaY = lienzo.clientHeight / lienzo.height;
  [puntos.hombroIzq, puntos.hombroDer, puntos.caderaIzq, puntos.caderaDer].forEach((punto, i) => {
    const marca = document.createElement("span");
    marca.className = "punto-ia";
    marca.setAttribute("aria-hidden", "true");
    marca.style.left = `${lienzo.offsetLeft + punto.x * escalaX}px`;
    marca.style.top = `${lienzo.offsetTop + punto.y * escalaY}px`;
    marca.style.setProperty("--i", i);
    marca.addEventListener("animationend", () => marca.remove());
    contenedorLienzo.appendChild(marca);
  });
}

function reiniciarAjustes() {
  escala.value = 100;
  moverX.value = 0;
  moverY.value = 0;
}

function ajustarLienzo(ancho, alto) {
  const factor = Math.min(1, ANCHO_MAXIMO_LIENZO / ancho);
  lienzo.width = Math.round(ancho * factor);
  lienzo.height = Math.round(alto * factor);
}

/* ---------- Modo foto ---------- */

// Usa la IA para encontrar hombros y caderas en la foto de la persona
async function ubicarAutomaticamente() {
  if (modo !== "foto" || !fotoPersona) return;
  contenedorLienzo.classList.add("escaneando"); // franja de luz rosa mientras la IA trabaja (movimiento.css)
  puntosPersona = null;
  try {
    estado.textContent = "Cargando la inteligencia artificial… (solo la primera vez)";
    const detector = await ia.detectorFotos();
    estado.textContent = "Buscando tu silueta…";
    await esperarDosCuadros(); // deja que la franja se vea antes de que la IA ocupe el navegador
    const landmarks = detector.detect(fotoPersona).landmarks?.[0];
    if (!landmarks) {
      estado.textContent = "No encontramos una persona en la foto. Ajusta la prenda a mano o prueba otra foto.";
    } else {
      puntosPersona = puntosDe(landmarks);
      estado.textContent = prenda ? "¡Listo! Si hace falta, ajusta el tamaño y la posición." : "Preparando la prenda…";
      mostrarPuntosIA(puntosPersona);
    }
  } catch (error) {
    console.error(error);
    estado.textContent = "No se pudo cargar la IA (¿sin internet?). Puedes ajustar la prenda a mano.";
  } finally {
    contenedorLienzo.classList.remove("escaneando");
  }
  reiniciarAjustes();
  aparecerPrenda();
}

function usarFoto(archivo) {
  apagarCamara();
  const url = URL.createObjectURL(archivo); // dirección temporal, solo en este navegador
  const imagen = new Image();
  imagen.onload = () => {
    URL.revokeObjectURL(url);
    ajustarLienzo(imagen.naturalWidth, imagen.naturalHeight);
    // Copia de la foto al tamaño del lienzo: así los puntos de la IA coinciden con lo que se ve
    const copia = document.createElement("canvas");
    copia.width = lienzo.width;
    copia.height = lienzo.height;
    copia.getContext("2d").drawImage(imagen, 0, 0, copia.width, copia.height);
    fotoPersona = copia;
    modo = "foto";
    StyleMe.registrarEvento("USAR_PROBADOR", { producto: selectorPrenda.value, detalle: { modo: "foto" } });
    ajustes.disabled = false;
    dibujar();
    ubicarAutomaticamente();
  };
  imagen.onerror = () => {
    URL.revokeObjectURL(url);
    estado.textContent = "No pudimos abrir esa imagen. Prueba con una foto JPG o PNG.";
  };
  imagen.src = url;
}

/* ---------- Modo cámara (computador o celular) ---------- */

async function encenderCamara() {
  botonCamara.disabled = true;
  estado.textContent = "Pidiendo permiso para usar la cámara…";
  try {
    flujoCamara = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: "user", width: { ideal: 1280 }, height: { ideal: 720 } }, audio: false
    });
    video.srcObject = flujoCamara;
    await video.play();
    estado.textContent = "Cargando la inteligencia artificial… (solo la primera vez)";
    const detector = await ia.detectorVideo();
    if (!flujoCamara) return; // se apagó mientras cargaba

    fotoPersona = null;
    campoFoto.value = "";
    puntosPersona = null;
    cuadrosSinPersona = 0;
    ultimoTiempoVideo = -1;
    tamanoVideo = "";
    reiniciarAjustes();
    modo = "camara";
    contenedorLienzo.classList.add("espejo");
    controlesCamara.hidden = false;
    botonCamara.hidden = true;
    ajustes.disabled = false;
    estado.textContent = "Aléjate hasta que se vean tus hombros y tu cintura.";
    StyleMe.registrarEvento("USAR_PROBADOR", { producto: selectorPrenda.value, detalle: { modo: "camara" } });
    requestAnimationFrame((t) => cuadroCamara(detector, t));
  } catch (error) {
    console.error(error);
    apagarCamara();
    estado.textContent = error.name === "NotAllowedError"
      ? "No diste permiso para usar la cámara. Puedes activarlo en el candado de la barra de direcciones, o subir una foto."
      : error.name === "NotFoundError"
        ? "No encontramos una cámara en este equipo. Sube una foto."
        : "No se pudo encender la cámara. Sube una foto.";
  } finally {
    botonCamara.disabled = !casillaConsentimiento.checked;
  }
}

// En cada cuadro: la IA busca a la persona en el video y la prenda la sigue (suavizado para que no tiemble)
function cuadroCamara(detector, ahora) {
  if (modo !== "camara") return;
  const { ancho, alto } = medidasVideo();
  const tamano = `${ancho}x${alto}`;
  if (ancho && tamano !== tamanoVideo) {
    tamanoVideo = tamano;
    ajustarLienzo(ancho, alto);
    puntosPersona = null;
  }
  if (tamanoVideo && video.currentTime !== ultimoTiempoVideo && video.readyState >= 2) {
    ultimoTiempoVideo = video.currentTime;
    const landmarks = detector.detectForVideo(video, ahora).landmarks?.[0];
    if (landmarks) {
      const nuevos = puntosDe(landmarks);
      puntosPersona = puntosPersona
        ? Object.fromEntries(Object.entries(nuevos).map(([nombre, p]) => {
            const antes = puntosPersona[nombre];
            return [nombre, { x: antes.x + (p.x - antes.x) * SUAVIZADO, y: antes.y + (p.y - antes.y) * SUAVIZADO }];
          }))
        : nuevos;
      cuadrosSinPersona = 0;
    } else if (++cuadrosSinPersona > CUADROS_SIN_PERSONA) {
      puntosPersona = null;
    }
  }
  dibujar();
  requestAnimationFrame((t) => cuadroCamara(detector, t));
}

// Medidas del video; si el navegador aún no las da, las de la cámara
function medidasVideo() {
  if (video.videoWidth >= 16) return { ancho: video.videoWidth, alto: video.videoHeight };
  const ajustesCamara = flujoCamara?.getVideoTracks()[0]?.getSettings() ?? {};
  return { ancho: ajustesCamara.width ?? 0, alto: ajustesCamara.height ?? 0 };
}

function apagarCamara() {
  cancelarCuentaRegresiva();
  flujoCamara?.getTracks().forEach((pista) => pista.stop());
  flujoCamara = null;
  video.srcObject = null;
  contenedorLienzo.classList.remove("espejo");
  controlesCamara.hidden = true;
  botonCamara.hidden = !navigator.mediaDevices?.getUserMedia;
  if (modo === "camara") {
    modo = "nada";
    puntosPersona = null;
    ajustes.disabled = true;
    dibujarMensaje("Cámara apagada");
  }
}

// Cuenta regresiva de 3 s para alcanzar a posar lejos del computador
function capturarConCuenta() {
  if (cuentaRegresiva) return;
  const pose = POSES_SUGERIDAS[urlsCapturas.length % POSES_SUGERIDAS.length];
  let quedan = 3;
  botonCapturar.disabled = true;
  contenedorLienzo.dataset.cuenta = quedan;
  estado.textContent = `Ponte ${pose}… ${quedan}`;
  cuentaRegresiva = setInterval(() => {
    quedan--;
    if (quedan > 0) {
      contenedorLienzo.dataset.cuenta = quedan;
      estado.textContent = `Ponte ${pose}… ${quedan}`;
      return;
    }
    cancelarCuentaRegresiva();
    guardarCaptura(pose);
  }, 1000);
}

function cancelarCuentaRegresiva() {
  clearInterval(cuentaRegresiva);
  cuentaRegresiva = null;
  delete contenedorLienzo.dataset.cuenta;
  botonCapturar.disabled = false;
}

// La foto se guarda como se ve en pantalla (en espejo). Solo existe en este navegador.
function guardarCaptura(pose) {
  const copia = document.createElement("canvas");
  copia.width = lienzo.width;
  copia.height = lienzo.height;
  const c = copia.getContext("2d");
  c.translate(copia.width, 0);
  c.scale(-1, 1);
  c.drawImage(lienzo, 0, 0);
  const generacion = generacionCapturas;
  copia.toBlob((blob) => {
    if (!blob || generacion !== generacionCapturas) return;
    if (urlsCapturas.length >= MAXIMO_CAPTURAS) {
      URL.revokeObjectURL(urlsCapturas.shift());
      capturas.firstElementChild?.remove();
    }
    const url = URL.createObjectURL(blob);
    urlsCapturas.push(url);
    const producto = prendaSeleccionada();
    capturas.insertAdjacentHTML("beforeend", `
      <figure class="captura">
        <img src="${url}" alt="Tú con ${escaparHTML(producto?.nombre ?? "la prenda")}, ${escaparHTML(pose)}">
        <figcaption>${escaparHTML(pose)} · <a href="${url}" download="styleme-${escaparHTML(producto?.id ?? "prenda")}-${urlsCapturas.length}.jpg">Guardar</a></figcaption>
      </figure>`);
    capturas.hidden = false;
    estado.textContent = `Foto ${urlsCapturas.length} de ${MAXIMO_CAPTURAS} lista (${pose}). Solo está en tu navegador.`;
  }, "image/jpeg", 0.9);
}

function borrarCapturas() {
  generacionCapturas++;
  urlsCapturas.forEach((u) => URL.revokeObjectURL(u));
  urlsCapturas = [];
  capturas.innerHTML = "";
  capturas.hidden = true;
}

/* ---------- Borrar todo ---------- */

function borrarTodo() {
  apagarCamara();
  borrarCapturas();
  modo = "nada";
  fotoPersona = null;
  puntosPersona = null;
  campoFoto.value = "";
  ajustes.disabled = true;
  dibujarMensaje("Foto borrada");
  estado.textContent = "Tu foto y tus capturas fueron borradas.";
}

/* ---------- Eventos ---------- */

selectorPrenda.addEventListener("change", () => {
  reiniciarAjustes();
  cargarPrenda();
});

casillaConsentimiento.addEventListener("change", () => {
  const permitido = casillaConsentimiento.checked && prendas.length > 0;
  campoFoto.disabled = !permitido;
  botonCamara.disabled = !permitido;
  if (!casillaConsentimiento.checked) borrarTodo();
});

campoFoto.addEventListener("change", () => {
  if (campoFoto.files[0]) usarFoto(campoFoto.files[0]);
});

botonCamara.addEventListener("click", encenderCamara);
botonApagar.addEventListener("click", apagarCamara);
botonCapturar.addEventListener("click", capturarConCuenta);
[escala, moverX, moverY].forEach((c) => c.addEventListener("input", dibujarEnElSiguienteCuadro));
document.getElementById("reubicar").addEventListener("click", () => {
  if (modo === "camara") reiniciarAjustes();
  else ubicarAutomaticamente();
});
document.getElementById("borrar").addEventListener("click", borrarTodo);
// Al salir de la página, la cámara se apaga
window.addEventListener("pagehide", apagarCamara);
