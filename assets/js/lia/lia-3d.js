/*
 * LÍA EN 3D (Fase 2)
 * ------------------
 * Dibuja a Lía en 3D dentro de su arco de vitrina, con el modelo que se exporta de VRoid:
 *   assets/lia/lia.vrm
 * lia.js carga este archivo SOLO si ese modelo existe. Si no existe o algo falla,
 * Lía sigue siendo su retrato (no se rompe nada).
 *
 * Qué hace Lía en 3D:
 *   - Respira y se balancea (nunca se queda congelada).
 *   - Parpadea sola cada 2,5–6 s (a veces doble).
 *   - Te sigue con la mirada (el mouse) y mueve un poquito la cabeza.
 *   - Su cabello se mece con la física que trae el modelo de VRoid.
 *   - Cambia la cara según la emoción: felicidad, preocupación, tristeza, duda, asombro o neutra.
 *   - Mueve la boca con las vocales mientras habla (lip sync).
 *   - Pensando: mira hacia arriba y a un lado.
 *
 * Usa Three.js y three-vrm (gratis), cargados desde un CDN con el "importmap" de cada página.
 * Todo se calcula con el TIEMPO REAL: se ve igual de suave a 60, 120 o 144 Hz.
 */
import * as THREE from "three";
import { GLTFLoader } from "three/addons/loaders/GLTFLoader.js";
import { VRMLoaderPlugin, VRMUtils } from "@pixiv/three-vrm";

/* ---------- ✏️ EMOCIONES: cuánto se activa cada expresión de la cara (0 a 1) ---------- */
// Expresiones que trae todo modelo VRM: happy (alegría), sad (tristeza), surprised (sorpresa),
// angry (enojo), relaxed (sonrisa suave). Se mezclan para lograr cada emoción.
// "cabeza": inclinación en radianes → x: arriba(-)/abajo(+), z: de lado.
// "mirada": hacia dónde miran los ojos además de seguir el mouse (x: lado, y: arriba).
// "cara": formas extra de los modelos de VRoid (si el modelo las tiene) para cejas y ojos.
const EMOCIONES = {
  neutra:       { expresiones: { relaxed: 0.25 }, cabeza: { x: 0, z: 0 }, mirada: { x: 0, y: 0 } },
  felicidad:    { expresiones: { happy: 0.75 }, cabeza: { x: -0.05, z: 0.04 }, mirada: { x: 0, y: 0 } },
  preocupacion: {
    expresiones: { sad: 0.35, surprised: 0.1 },
    cara: { Fcl_BRW_Sorrow: 0.75, Fcl_EYE_Sorrow: 0.2 },
    cabeza: { x: 0.08, z: 0.1 }, mirada: { x: 0, y: 0 }
  },
  tristeza:     { expresiones: { sad: 0.7 }, cabeza: { x: 0.16, z: -0.04 }, mirada: { x: 0, y: -0.15 } },
  duda: {
    expresiones: { surprised: 0.2, relaxed: 0.1 },
    cara: { Fcl_BRW_Surprised: 0.35, Fcl_EYE_Close_R: 0.25 },
    cabeza: { x: -0.04, z: 0.2 }, mirada: { x: 0.25, y: 0.25 }
  },
  asombro:     { expresiones: { surprised: 0.9 }, cabeza: { x: -0.2, z: 0.03 }, mirada: { x: 0, y: 0 } },
  pensando:     { expresiones: { relaxed: 0.1 }, cabeza: { x: -0.06, z: -0.08 }, mirada: { x: -0.3, y: 0.3 } }
};

// Vocales → bocas del modelo VRM
const BOCA = { a: "aa", e: "ee", i: "ih", o: "oh", u: "ou", á: "aa", é: "ee", í: "ih", ó: "oh", ú: "ou" };

export async function montarLia3D(contenedor, urlModelo) {
  const menosMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  /* ---------- Escena, cámara y luz ---------- */
  const ancho = contenedor.clientWidth || 180;
  const alto = contenedor.clientHeight || 240;
  const renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true, powerPreference: "low-power" });
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
  renderer.setSize(ancho, alto);
  renderer.outputColorSpace = THREE.SRGBColorSpace;
  renderer.domElement.className = "lia-lienzo";

  const escena = new THREE.Scene();
  const camara = new THREE.PerspectiveCamera(22, ancho / alto, 0.1, 20);
  const luzPrincipal = new THREE.DirectionalLight(0xfff1e6, 2.2); // luz cálida de boutique
  luzPrincipal.position.set(0.6, 1.6, 2);
  escena.add(luzPrincipal);
  escena.add(new THREE.HemisphereLight(0xfff6f0, 0xd9c2c8, 1.4));

  /* ---------- Cargar el modelo de VRoid ---------- */
  const cargador = new GLTFLoader();
  cargador.register((parser) => new VRMLoaderPlugin(parser));
  const gltf = await cargador.loadAsync(urlModelo);
  const vrm = gltf.userData.vrm;
  if (!vrm) throw new Error("El archivo no es un modelo VRM");
  VRMUtils.removeUnnecessaryVertices(gltf.scene);
  if (VRMUtils.combineSkeletons) VRMUtils.combineSkeletons(gltf.scene);
  VRMUtils.rotateVRM0(vrm); // los modelos VRM 0.x miran al otro lado: se corrigen
  vrm.scene.traverse((o) => { o.frustumCulled = false; });
  escena.add(vrm.scene);

  const hueso = (nombre) => vrm.humanoid?.getNormalizedBoneNode(nombre);
  const expresiones = vrm.expressionManager;

  // Brazos abajo (los modelos vienen con los brazos abiertos en "T")
  const brazoIzq = hueso("leftUpperArm");
  const brazoDer = hueso("rightUpperArm");
  const antebrazoIzq = hueso("leftLowerArm");
  const antebrazoDer = hueso("rightLowerArm");
  if (brazoIzq) brazoIzq.rotation.z = -1.22;
  if (brazoDer) brazoDer.rotation.z = 1.22;
  if (antebrazoIzq) antebrazoIzq.rotation.z = -0.12;
  if (antebrazoDer) antebrazoDer.rotation.z = 0.12;

  // Encuadre: cabeza y hombros dentro del arco
  vrm.update(0);
  escena.updateMatrixWorld(true);
  const posCabeza = new THREE.Vector3();
  (hueso("head") || vrm.scene).getWorldPosition(posCabeza);
  // ✏️ Encuadre: 0.06 = un poco por debajo de los ojos; 1.15 = distancia (más = Lía más pequeña)
  camara.position.set(0, posCabeza.y - 0.02, 1.15);
  camara.lookAt(0, posCabeza.y - 0.06, 0);

  // Lo que miran sus ojos (se mueve con el mouse)
  const objetivoMirada = new THREE.Object3D();
  escena.add(objetivoMirada);
  if (vrm.lookAt) vrm.lookAt.target = objetivoMirada;

  // Formas extra de la cara de VRoid (cejas, ojos), si el modelo las tiene
  const formasCara = [];
  vrm.scene.traverse((o) => {
    if (o.isMesh && o.morphTargetDictionary) formasCara.push(o);
  });
  const tocadas = new Set(); // formas que movimos (para devolverlas a 0 en cada cuadro)
  function ponerForma(nombre, valor) {
    formasCara.forEach((malla) => {
      // VRoid nombra las formas con un prefijo, p. ej. "Face.M_F00_000_00_Fcl_BRW_Sorrow"
      const clave = Object.keys(malla.morphTargetDictionary).find((k) => k.endsWith(nombre));
      if (clave === undefined) return;
      const i = malla.morphTargetDictionary[clave];
      malla.morphTargetInfluences[i] = Math.max(malla.morphTargetInfluences[i], valor);
      tocadas.add([malla, i]);
    });
  }
  function soltarFormas() {
    tocadas.forEach(([malla, i]) => { malla.morphTargetInfluences[i] = 0; });
    tocadas.clear();
  }

  contenedor.append(renderer.domElement);

  /* ---------- Estado ---------- */
  let emocion = "neutra";
  let intensidad = 0.6;
  let actividad = "quieta"; // quieta | pensando | hablando
  const valores = {};       // valor actual de cada expresión (para cambiar suave)
  const formasActuales = {};
  const cabeza = { x: 0, y: 0, z: 0 };
  const mouse = { x: 0, y: 0 };
  const vocales = [];       // vocales que faltan por "decir"
  let bocaActual = null;
  let tiempoVocal = 0;
  let proximoParpadeo = 3;
  let parpadeo = -1;        // -1 = no está parpadeando; si no, segundos desde que empezó

  // Suaviza un valor hacia otro según el tiempo (igual a cualquier Hz)
  const suavizar = (actual, meta, rapidez, dt) => actual + (meta - actual) * (1 - Math.exp(-rapidez * dt));

  /* ---------- Cada cuadro ---------- */
  const reloj = new THREE.Clock();
  let visible = true;
  let animando = true;

  function cuadro() {
    if (!animando) return;
    requestAnimationFrame(cuadro);
    if (!visible || document.hidden) return;
    const dt = Math.min(reloj.getDelta(), 0.1);
    const t = reloj.elapsedTime;

    const config = EMOCIONES[actividad === "pensando" ? "pensando" : emocion] || EMOCIONES.neutra;
    const fuerza = 0.5 + intensidad * 0.5;

    // 1. Expresión de la emoción (cambia suave en ~300 ms)
    if (expresiones) {
      ["happy", "sad", "surprised", "angry", "relaxed"].forEach((nombre) => {
        let meta = (config.expresiones[nombre] || 0) * fuerza;
        // Mientras habla, la boca de la emoción se reduce para no "pelear" con las vocales
        if (actividad === "hablando") meta *= 0.6;
        valores[nombre] = suavizar(valores[nombre] || 0, meta, 10, dt);
        expresiones.setValue(nombre, valores[nombre]);
      });
    }

    // 2. Boca al hablar (una vocal cada ~85 ms)
    tiempoVocal -= dt;
    if (tiempoVocal <= 0) {
      tiempoVocal = 0.085;
      if (vocales.length) bocaActual = vocales.shift();
      else if (actividad === "hablando") bocaActual = ["aa", "oh", "ee", "ih", null][Math.floor(Math.random() * 5)];
      else bocaActual = null;
    }
    if (expresiones) {
      ["aa", "ee", "ih", "oh", "ou"].forEach((boca) => {
        const meta = boca === bocaActual ? 0.65 : 0;
        valores[boca] = suavizar(valores[boca] || 0, meta, 22, dt);
        expresiones.setValue(boca, valores[boca]);
      });
    }

    // 3. Parpadeo: cierra en 0,12 s y abre en 0,16 s
    proximoParpadeo -= dt;
    if (parpadeo < 0 && proximoParpadeo <= 0) {
      parpadeo = 0;
      proximoParpadeo += 3;
    }
    if (parpadeo >= 0) {
      parpadeo += dt;
      const cierre = parpadeo < 0.12 ? parpadeo / 0.12 : Math.max(0, 1 - (parpadeo - 0.12) / 0.16);
      expresiones?.setValue("blink", cierre);
      if (parpadeo > 0.28) {
        parpadeo = -1;
        expresiones?.setValue("blink", 0);
      }
    }

    const movimiento = menosMovimiento ? 0.25 : 1;

    // 5. Cabeza: emoción + seguir al mouse + pequeño movimiento al hablar
    const habla = actividad === "hablando" ? Math.sin(t * 9) * 0.02 : 0;
    const asiente = emocion === "felicidad" ? Math.sin(t * 4) * 0.055 * movimiento : 0;
    cabeza.x = suavizar(cabeza.x, config.cabeza.x * fuerza - mouse.y * 0.12 + habla + asiente, 6, dt);
    cabeza.y = suavizar(cabeza.y, mouse.x * 0.25, 6, dt);
    cabeza.z = suavizar(cabeza.z, config.cabeza.z * fuerza + Math.sin(t * 0.6) * 0.015 * movimiento, 6, dt);
    const nodoCabeza = hueso("head");
    const nodoCuello = hueso("neck");
    if (nodoCabeza) nodoCabeza.rotation.set(cabeza.x * 0.6, cabeza.y * 0.6, cabeza.z * 0.6);
    if (nodoCuello) nodoCuello.rotation.set(cabeza.x * 0.4, cabeza.y * 0.4, cabeza.z * 0.4);

    // 6. Mirada: a la cámara + mouse + hacia donde mira la emoción
    objetivoMirada.position.set(
      camara.position.x + (mouse.x + config.mirada.x) * 0.6,
      camara.position.y + (mouse.y + config.mirada.y) * 0.4,
      camara.position.z
    );

    soltarFormas(); // las formas extra del cuadro anterior vuelven a 0
    vrm.update(dt); // aplica expresiones, mirada y física del cabello

    // 7. Formas extra de VRoid (después de vrm.update para que no se borren)
    Object.keys(formasActuales).forEach((k) => (formasActuales[k] = suavizar(formasActuales[k], 0, 10, dt)));
    Object.entries(config.cara || {}).forEach(([k, v]) => {
      formasActuales[k] = suavizar(formasActuales[k] || 0, v * fuerza, 10, dt);
    });
    Object.entries(formasActuales).forEach(([k, v]) => v > 0.01 && ponerForma(k, v));

    renderer.render(escena, camara);
  }
  requestAnimationFrame(cuadro);

  // Solo se dibuja cuando Lía se ve en pantalla
  new IntersectionObserver(([e]) => { visible = e.isIntersecting; }).observe(contenedor);

  // La mirada sigue al mouse (−1 a 1)
  window.addEventListener("pointermove", (e) => {
    const caja = contenedor.getBoundingClientRect();
    mouse.x = Math.max(-1, Math.min(1, (e.clientX - (caja.left + caja.width / 2)) / (window.innerWidth / 2)));
    mouse.y = Math.max(-1, Math.min(1, -(e.clientY - (caja.top + caja.height * 0.3)) / (window.innerHeight / 2)));
  }, { passive: true });

  /* ---------- Lo que lia.js le puede pedir ---------- */
  return {
    ponerEmocion(nueva, nuevaIntensidad = 0.6) {
      emocion = EMOCIONES[nueva] ? nueva : "neutra";
      intensidad = nuevaIntensidad;
    },
    ponerActividad(nueva) {
      actividad = nueva;
      if (nueva !== "hablando") vocales.length = 0;
    },
    // Recibe el texto que va diciendo y lo convierte en vocales para la boca
    decir(texto) {
      for (const letra of texto.toLowerCase()) {
        if (BOCA[letra]) vocales.push(BOCA[letra]);
        else if (letra === " " && vocales.length && vocales[vocales.length - 1] !== null) vocales.push(null);
      }
      if (vocales.length > 400) vocales.splice(0, vocales.length - 400);
    },
    // Cuánto le falta por "decir" (en ms), para saber cuándo deja de mover la boca
    tiempoRestante() {
      return vocales.length * 85;
    },
    destruir() {
      animando = false;
      renderer.dispose();
      renderer.domElement.remove();
    }
  };
}
