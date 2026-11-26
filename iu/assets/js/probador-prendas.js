/*
 * PRENDAS DEL PROBADOR RÁPIDO
 * ---------------------------
 * Convierte la foto que publica la tienda en una prenda recortada (con fondo transparente)
 * y calcula dónde quedan sus hombros y caderas, para ponerla encima de la persona.
 *
 * Según cómo sea la foto:
 *  a) Foto con modelo (ej. Henar's): la IA encuentra el cuerpo de la modelo (MediaPipe Pose) y separa
 *     la ropa del pelo, la piel y el fondo (MediaPipe Image Segmenter, modelo "selfie multiclass").
 *     Los hombros y caderas son los de la modelo: así la prenda se acomoda mejor al cuerpo de la persona.
 *  b) Prenda sola sobre fondo claro (ej. Mattelsa): se borra el fondo empezando desde los bordes
 *     y los hombros/caderas se estiman por la forma de la prenda.
 *  c) Si el producto ya tiene foto_probador (PNG sin fondo), se usa tal cual.
 *
 * Todo pasa dentro del navegador: las fotos de las tiendas permiten leerse desde otra página (CORS).
 */

const LADO_MAXIMO = 640;     // px: suficiente para verse bien y recortar rápido
// Categorías del modelo "selfie multiclass": 0 fondo, 1 pelo, 2 piel, 3 cara, 4 ropa, 5 otros
const CATEGORIA_PIEL = 2, CATEGORIA_CARA = 3, CATEGORIA_ROPA = 4;
const MINIMO_PIEL = 0.015;   // al menos 1,5 % de la foto con piel o cara para que cuente como "con modelo"
const TOLERANCIA_FONDO = 30; // diferencia de color que todavía cuenta como fondo

// Puntos del cuerpo según MediaPipe
export const HOMBRO_IZQ = 11, HOMBRO_DER = 12, CADERA_IZQ = 23, CADERA_DER = 24;

const preparadas = new Map();

/**
 * Devuelve { lienzo, anclas, estimadas, origen }:
 *  - lienzo: la prenda recortada (canvas con transparencia)
 *  - anclas: { hombroIzq, hombroDer, caderaIzq, caderaDer } en píxeles del lienzo
 *  - estimadas: true si las anclas salen de la forma de la prenda (no de una modelo)
 * ia: { detectorFotos(), segmentador() } — los modelos se cargan solo cuando hacen falta.
 */
export function prepararPrenda(producto, ia) {
  if (!preparadas.has(producto.id)) {
    const promesa = preparar(producto, ia).catch((error) => {
      preparadas.delete(producto.id); // si falló (ej. sin internet), se puede volver a intentar
      throw error;
    });
    preparadas.set(producto.id, promesa);
  }
  return preparadas.get(producto.id);
}

async function preparar(producto, ia) {
  if (producto.fotoProbador) {
    const lienzo = aLienzo(await cargarImagen(producto.fotoProbador));
    return { lienzo, anclas: anclasEstimadas(lienzo, producto.tipoPrenda), estimadas: true, origen: "png" };
  }

  const lienzo = aLienzo(await cargarImagen(producto.foto));
  const detector = await ia.detectorFotos();
  const puntos = detector.detect(lienzo).landmarks?.[0];

  if (puntos && cuerpoVisible(puntos)) {
    const segmentador = await ia.segmentador();
    const anclas = anclasDePose(puntos, lienzo);
    // Una prenda sola puede "parecer" un cuerpo: solo es foto con modelo si de verdad se ve piel o cara
    if (recortarRopaDeModelo(lienzo, segmentador, anclas, producto.tipoPrenda)) {
      return { lienzo, anclas, estimadas: false, origen: "modelo" };
    }
  }

  borrarFondoClaro(lienzo);
  return { lienzo, anclas: anclasEstimadas(lienzo, producto.tipoPrenda), estimadas: true, origen: "plana" };
}

/* ---------- Carga ---------- */

function cargarImagen(url) {
  return new Promise((listo, error) => {
    const imagen = new Image();
    imagen.crossOrigin = "anonymous"; // sin esto el navegador no deja leer los píxeles de otra página
    imagen.onload = () => listo(imagen);
    imagen.onerror = () => error(new Error("No se pudo cargar la foto de la prenda."));
    imagen.src = url;
  });
}

function aLienzo(imagen) {
  const factor = Math.min(1, LADO_MAXIMO / Math.max(imagen.naturalWidth, imagen.naturalHeight));
  const lienzo = document.createElement("canvas");
  lienzo.width = Math.round(imagen.naturalWidth * factor);
  lienzo.height = Math.round(imagen.naturalHeight * factor);
  lienzo.getContext("2d", { willReadFrequently: true }).drawImage(imagen, 0, 0, lienzo.width, lienzo.height);
  return lienzo;
}

/* ---------- a) Foto con modelo ---------- */

const NARIZ = 0;
function cuerpoVisible(puntos) {
  return [NARIZ, HOMBRO_IZQ, HOMBRO_DER, CADERA_IZQ, CADERA_DER].every((i) => (puntos[i].visibility ?? 1) > 0.5);
}

function anclasDePose(puntos, lienzo) {
  const p = (i) => ({ x: puntos[i].x * lienzo.width, y: puntos[i].y * lienzo.height });
  return { hombroIzq: p(HOMBRO_IZQ), hombroDer: p(HOMBRO_DER), caderaIzq: p(CADERA_IZQ), caderaDer: p(CADERA_DER) };
}

// Deja solo la ropa (y solo la parte que corresponde a la prenda: arriba o abajo de la cintura).
// Devuelve false (sin tocar el lienzo) si en la foto casi no hay piel: entonces no hay modelo.
function recortarRopaDeModelo(lienzo, segmentador, anclas, tipoPrenda) {
  const resultado = segmentador.segment(lienzo);
  const mascaras = resultado.confidenceMasks;
  const ropa = mascaras[CATEGORIA_ROPA].getAsFloat32Array();
  const piel = mascaras[CATEGORIA_PIEL].getAsFloat32Array();
  const cara = mascaras[CATEGORIA_CARA].getAsFloat32Array();
  const anchoMascara = mascaras[CATEGORIA_ROPA].width;
  const altoMascara = mascaras[CATEGORIA_ROPA].height;
  let pixelesDePiel = 0;
  for (let i = 0; i < piel.length; i++) if (piel[i] > 0.5 || cara[i] > 0.5) pixelesDePiel++;
  if (pixelesDePiel / piel.length < MINIMO_PIEL) {
    mascaras.forEach((m) => m.close());
    return false;
  }

  const yHombros = (anclas.hombroIzq.y + anclas.hombroDer.y) / 2;
  const yCaderas = (anclas.caderaIzq.y + anclas.caderaDer.y) / 2;
  const torso = yCaderas - yHombros;
  // Límites verticales según la prenda
  const desde = tipoPrenda === "inferior" ? yCaderas - torso * 0.22 : -Infinity;
  const hasta = tipoPrenda === "superior" ? yCaderas + torso * 0.12 : Infinity;

  const ctx = lienzo.getContext("2d", { willReadFrequently: true });
  const pixeles = ctx.getImageData(0, 0, lienzo.width, lienzo.height);
  const datos = pixeles.data;
  for (let y = 0; y < lienzo.height; y++) {
    const fila = Math.min(altoMascara - 1, Math.floor((y * altoMascara) / lienzo.height));
    for (let x = 0; x < lienzo.width; x++) {
      const columna = Math.min(anchoMascara - 1, Math.floor((x * anchoMascara) / lienzo.width));
      const confianza = ropa[fila * anchoMascara + columna];
      // Borde suave: 0 % de ropa por debajo de 0,35 y 100 % desde 0,65
      let alfa = Math.max(0, Math.min(1, (confianza - 0.35) / 0.3));
      if (y < desde || y > hasta) alfa = 0;
      datos[(y * lienzo.width + x) * 4 + 3] = Math.round(alfa * 255);
    }
  }
  ctx.putImageData(pixeles, 0, 0);
  mascaras.forEach((m) => m.close());
  return true;
}

/* ---------- b) Prenda sola sobre fondo claro ---------- */

// Borra el fondo "inundándolo" desde los bordes: solo se borra lo que toca el borde y se parece al fondo,
// así no se borra el blanco que haya DENTRO de la prenda.
function borrarFondoClaro(lienzo) {
  const { width: ancho, height: alto } = lienzo;
  const ctx = lienzo.getContext("2d", { willReadFrequently: true });
  const pixeles = ctx.getImageData(0, 0, ancho, alto);
  const datos = pixeles.data;

  // Color del fondo: el promedio de los bordes
  let r = 0, g = 0, b = 0, n = 0;
  const sumar = (x, y) => {
    const i = (y * ancho + x) * 4;
    r += datos[i]; g += datos[i + 1]; b += datos[i + 2]; n++;
  };
  for (let x = 0; x < ancho; x++) { sumar(x, 0); sumar(x, alto - 1); }
  for (let y = 0; y < alto; y++) { sumar(0, y); sumar(ancho - 1, y); }
  r /= n; g /= n; b /= n;

  const pareceFondo = (p) => {
    const i = p * 4;
    return Math.max(Math.abs(datos[i] - r), Math.abs(datos[i + 1] - g), Math.abs(datos[i + 2] - b)) < TOLERANCIA_FONDO;
  };

  const esFondo = new Uint8Array(ancho * alto);
  const pila = [];
  const empujar = (p) => {
    if (!esFondo[p] && pareceFondo(p)) { esFondo[p] = 1; pila.push(p); }
  };
  for (let x = 0; x < ancho; x++) { empujar(x); empujar((alto - 1) * ancho + x); }
  for (let y = 0; y < alto; y++) { empujar(y * ancho); empujar(y * ancho + ancho - 1); }
  while (pila.length) {
    const p = pila.pop();
    const x = p % ancho;
    if (x > 0) empujar(p - 1);
    if (x < ancho - 1) empujar(p + 1);
    if (p >= ancho) empujar(p - ancho);
    if (p < ancho * (alto - 1)) empujar(p + ancho);
  }

  // Huecos encerrados del color del fondo (ej. el cuello de un body halter): se borran si son grandes.
  // Los pequeños (un logo o un estampado blanco) se conservan.
  const minimoHueco = ancho * alto * 0.01;
  const visitado = new Uint8Array(ancho * alto);
  for (let inicio = 0; inicio < esFondo.length; inicio++) {
    if (esFondo[inicio] || visitado[inicio] || !pareceFondo(inicio)) continue;
    const zona = [inicio];
    visitado[inicio] = 1;
    for (let k = 0; k < zona.length; k++) {
      const p = zona[k];
      const x = p % ancho;
      for (const vecino of [x > 0 ? p - 1 : -1, x < ancho - 1 ? p + 1 : -1, p - ancho, p + ancho]) {
        if (vecino >= 0 && vecino < esFondo.length && !visitado[vecino] && !esFondo[vecino] && pareceFondo(vecino)) {
          visitado[vecino] = 1;
          zona.push(vecino);
        }
      }
    }
    if (zona.length > minimoHueco) zona.forEach((p) => (esFondo[p] = 1));
  }

  // El contorno se borra (quedaba un halo del color del fondo) y el siguiente anillo queda semitransparente
  const contorno = new Uint8Array(ancho * alto);
  for (let p = 0; p < esFondo.length; p++) if (!esFondo[p] && tocaFondo(esFondo, p, ancho, alto)) contorno[p] = 1;
  for (let p = 0; p < esFondo.length; p++) {
    if (esFondo[p] || contorno[p]) datos[p * 4 + 3] = 0;
    else if (tocaFondo(contorno, p, ancho, alto)) datos[p * 4 + 3] = 160;
  }
  ctx.putImageData(pixeles, 0, 0);
}

function tocaFondo(esFondo, p, ancho, alto) {
  const x = p % ancho;
  return (x > 0 && esFondo[p - 1]) || (x < ancho - 1 && esFondo[p + 1]) ||
    (p >= ancho && esFondo[p - ancho]) || (p < ancho * (alto - 1) && esFondo[p + ancho]);
}

/* ---------- Anclas estimadas por la forma de la prenda ---------- */

// Caja de la parte visible de la prenda y ancho de una fila (para estimar hombros y caderas)
function medirPrenda(lienzo) {
  const { width: ancho, height: alto } = lienzo;
  const datos = lienzo.getContext("2d", { willReadFrequently: true }).getImageData(0, 0, ancho, alto).data;
  const visible = (x, y) => datos[(y * ancho + x) * 4 + 3] > 128;
  let arriba = alto, abajo = -1, izquierda = ancho, derecha = -1;
  for (let y = 0; y < alto; y++) {
    for (let x = 0; x < ancho; x++) {
      if (!visible(x, y)) continue;
      if (y < arriba) arriba = y;
      if (y > abajo) abajo = y;
      if (x < izquierda) izquierda = x;
      if (x > derecha) derecha = x;
    }
  }
  if (abajo < 0) return { arriba: 0, abajo: alto, izquierda: 0, derecha: ancho, anchoFila: () => ancho };
  const anchoFila = (proporcion) => {
    const y = Math.round(arriba + (abajo - arriba) * proporcion);
    let min = -1, max = -1;
    for (let x = 0; x < ancho; x++) if (visible(x, y)) { if (min < 0) min = x; max = x; }
    return min < 0 ? derecha - izquierda : max - min;
  };
  return { arriba, abajo, izquierda, derecha, anchoFila };
}

// Las prendas se fotografían de frente: el hombro izquierdo de quien la usaría queda a la DERECHA de la foto
function anclasEstimadas(lienzo, tipoPrenda) {
  const caja = medirPrenda(lienzo);
  const alto = caja.abajo - caja.arriba;
  const centro = (caja.izquierda + caja.derecha) / 2;
  if (tipoPrenda === "inferior") {
    const separacion = caja.anchoFila(0.04) * 0.28; // las caderas (articulación) quedan por dentro de la pretina
    const y = caja.arriba + alto * 0.1;
    return {
      caderaIzq: { x: centro + separacion, y }, caderaDer: { x: centro - separacion, y },
      hombroIzq: { x: centro + separacion * 1.6, y: y - separacion * 3.6 }, hombroDer: { x: centro - separacion * 1.6, y: y - separacion * 3.6 }
    };
  }
  const separacion = caja.anchoFila(0.35) * 0.36;
  const y = caja.arriba + alto * 0.07;
  return {
    hombroIzq: { x: centro + separacion, y }, hombroDer: { x: centro - separacion, y },
    caderaIzq: { x: centro + separacion * 0.7, y: y + separacion * 2.9 }, caderaDer: { x: centro - separacion * 0.7, y: y + separacion * 2.9 }
  };
}
