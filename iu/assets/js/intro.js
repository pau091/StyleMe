/*
 * INTRO 3D — "ENTRAR A LA TIENDA"
 * -------------------------------
 * Se carga en el <head> de las 9 páginas, justo después de arranque.js.
 * Sale en la primera página de cada visita (o siempre con ?intro en la dirección).
 * Los estilos y los tiempos están en assets/css/intro.css.
 *
 * Cómo funciona:
 *   1. Una calle de Bucaramanga. La cámara avanza y aparece la tienda.
 *      El cielo depende de la hora real en Bucaramanga: día, atardecer o noche.
 *   2. La escena se detiene y dice "Toca la puerta para entrar".
 *      Con el mouse, la calle gira un poquito siguiendo el cursor (se nota el 3D).
 *   3. Al tocar (o hacer clic, o presionar una tecla): suena el timbre y el ruido de la calle
 *      está activado), se abren las puertas y la cámara entra por la luz.
 *   4. La luz se desvanece y aparece la página.
 *
 * ✏️ Para una presentación:
 *      index.html?intro            → sale la intro aunque ya la hayas visto
 *      index.html?intro=noche      → fuerza la noche (también: dia, atardecer)
 */
(function () {
  const A = window.StyleMeArranque;
  if (!A) return;
  const raiz = document.documentElement;

  /* ---------- ¿Se muestra la intro? ---------- */
  const esPrimeraVez = !A.leer("styleme-bienvenida");
  A.guardar("styleme-bienvenida", "1");
  const parametro = window.location.search.match(/[?&]intro(?:=(\w+))?/);
  const forzarIntro = !!parametro;

  if (!(esPrimeraVez || forzarIntro) || A.vieneDeCambio || A.menosMovimiento) return;
  if (!raiz.classList.contains("con-movimiento")) return;

  /* ---------- ✏️ Ajustes ---------- */
  // ✏️ ms: si nadie toca la puerta, entra sola. 0 = espera el toque, así el sonido SIEMPRE suena
  //    (el navegador no deja sonar nada hasta que la persona toca la página).
  const ESPERA_AUTOMATICA = 0;
  const GIRO_MAXIMO = { y: 5, x: 2.5 }; // grados que gira la calle con el mouse

  // Ropa de los maniquíes de la vitrina (un color por maniquí)
  const MANIQUIES = [
    { x: "9%", ropa: "#7d4253" },
    { x: "21%", ropa: "#c9a48f" },
    { x: "71%", ropa: "#4f2c3a" },
    { x: "84%", ropa: "#a35d6d" }
  ];
  // Faroles: x = lado (izquierda/derecha), z = qué tan cerca de la cámara
  const FAROLES = [
    { x: "8%", z: "240px" },
    { x: "92%", z: "240px" },
    { x: "-4%", z: "560px" },
    { x: "104%", z: "560px" }
  ];

  /* ---------- Día, atardecer o noche según la hora de Bucaramanga ---------- */
  function momentoDelDia() {
    const forzado = parametro?.[1];
    if (["dia", "atardecer", "noche"].includes(forzado)) return forzado;
    let hora = new Date().getHours();
    try {
      // Hora de Colombia, aunque el computador esté en otra zona horaria
      hora = Number(
        new Intl.DateTimeFormat("en-US", { timeZone: "America/Bogota", hour: "numeric", hourCycle: "h23" }).format(new Date())
      );
    } catch (e) { /* navegador viejo: se usa la hora del computador */ }
    if (hora >= 6 && hora < 17) return "dia";        // ✏️ horas del día
    if (hora >= 17 && hora < 19) return "atardecer"; // ✏️ horas del atardecer
    return "noche";
  }

  /* ---------- Armar la escena ---------- */
  const intro = document.createElement("div");
  intro.className = "intro";
  intro.dataset.momento = momentoDelDia();
  intro.setAttribute("aria-hidden", "true");
  intro.innerHTML = `
    <div class="intro-giro">
      <div class="intro-camara">
        <div class="intro-suelo"><div class="intro-cebra"></div></div>
        <div class="intro-muro"></div>
        <div class="intro-fachada">
          <div class="intro-vitrina">
            <div class="intro-interior"></div>
            <div class="intro-focos"></div>
            <span class="intro-letrero">STYLE<span>ME</span></span>
            ${MANIQUIES.map((m) => `<span class="maniqui" style="--x:${m.x};--ropa:${m.ropa}"></span>`).join("")}
          </div>
          <div class="intro-puerta">
            <div class="intro-luz"></div>
            <div class="intro-hoja izquierda"></div>
            <div class="intro-hoja derecha"></div>
          </div>
        </div>
        ${FAROLES.map((f) => `<div class="intro-farol" style="--x:${f.x};--z:${f.z}"></div>`).join("")}
      </div>
    </div>
    <p class="intro-tocar">Toca la puerta para entrar</p>
    <div class="intro-destello"></div>`;

  // Las animaciones de la página esperan a que se abra la puerta (seguro de 2 minutos por si algo falla)
  A.esperarTelon(ESPERA_AUTOMATICA ? ESPERA_AUTOMATICA + 8000 : 120000);
  A.agregar(intro);

  /* ---------- Sonido (siempre activo) ---------- */
  // Se crea con Web Audio: no hay archivos de sonido que descargar ($0).
  // El navegador solo deja sonar después de que la persona toca algo,
  // por eso arranca al tocar la puerta (o presionar una tecla).
  const Sonido = (function () {
    let ctx = null;
    let ambiente = null;

    function contexto() {
      const Clase = window.AudioContext || window.webkitAudioContext;
      if (!Clase) return null;
      if (!ctx) ctx = new Clase();
      if (ctx.state === "suspended") ctx.resume();
      return ctx;
    }

    // Ruido suave de calle: ruido "café" con filtro, muy bajito
    function iniciarAmbiente() {
      const c = contexto();
      if (!c || ambiente) return;
      const largo = c.sampleRate * 2;
      const buffer = c.createBuffer(1, largo, c.sampleRate);
      const datos = buffer.getChannelData(0);
      let ultimo = 0;
      for (let i = 0; i < largo; i++) {
        ultimo = (ultimo + 0.02 * (Math.random() * 2 - 1)) / 1.02;
        datos[i] = ultimo * 3.5;
      }
      const fuente = c.createBufferSource();
      fuente.buffer = buffer;
      fuente.loop = true;
      const filtro = c.createBiquadFilter();
      filtro.type = "lowpass";
      filtro.frequency.value = 700;
      const volumen = c.createGain();
      volumen.gain.setValueAtTime(0, c.currentTime);
      volumen.gain.linearRampToValueAtTime(0.05, c.currentTime + 1); // ✏️ volumen de la calle
      fuente.connect(filtro).connect(volumen).connect(c.destination);
      fuente.start();
      ambiente = { fuente, volumen };
    }

    function detenerAmbiente(segundos = 0.4) {
      if (!ambiente || !ctx) return;
      const { fuente, volumen } = ambiente;
      volumen.gain.cancelScheduledValues(ctx.currentTime);
      volumen.gain.setValueAtTime(volumen.gain.value, ctx.currentTime);
      volumen.gain.linearRampToValueAtTime(0, ctx.currentTime + segundos);
      fuente.stop(ctx.currentTime + segundos + 0.05);
      ambiente = null;
    }

    // Timbre de puerta "ding-dong": dos notas que se apagan solas
    function timbre() {
      const c = contexto();
      if (!c) return;
      const nota = (frecuencia, inicio) => {
        [1, 2.01].forEach((armonico, i) => {
          const osc = c.createOscillator();
          osc.type = "sine";
          osc.frequency.value = frecuencia * armonico;
          const vol = c.createGain();
          const t = c.currentTime + inicio;
          vol.gain.setValueAtTime(0.0001, t);
          vol.gain.exponentialRampToValueAtTime(i === 0 ? 0.16 : 0.04, t + 0.015); // ✏️ volumen del timbre
          vol.gain.exponentialRampToValueAtTime(0.0001, t + 1.4);
          osc.connect(vol).connect(c.destination);
          osc.start(t);
          osc.stop(t + 1.5);
        });
      };
      nota(659.25, 0);    // Mi
      nota(523.25, 0.32); // Do
    }

    return {
      alEntrar() {
        iniciarAmbiente();
        timbre();
      },
      terminar() { detenerAmbiente(0.6); }
    };
  })();

  /* ---------- La calle gira un poquito con el mouse ---------- */
  const giro = intro.querySelector(".intro-giro");
  const punteroFino = window.matchMedia("(hover: hover) and (pointer: fine)").matches;
  let pendiente = false;
  let gx = 0;
  let gy = 0;
  function moverMouse(e) {
    if (intro.classList.contains("entrando")) return;
    // -1 a 1 según dónde está el mouse en la pantalla
    gy = ((e.clientX / window.innerWidth) * 2 - 1) * GIRO_MAXIMO.y;
    gx = -((e.clientY / window.innerHeight) * 2 - 1) * GIRO_MAXIMO.x;
    if (pendiente) return;
    pendiente = true;
    requestAnimationFrame(() => {
      giro.style.setProperty("--giro-x", gx.toFixed(2) + "deg");
      giro.style.setProperty("--giro-y", gy.toFixed(2) + "deg");
      pendiente = false;
    });
  }
  if (punteroFino) intro.addEventListener("pointermove", moverMouse, { passive: true });

  /* ---------- Fases ---------- */
  let lista = false;        // la tienda ya apareció y la puerta espera
  let quiereEntrar = false; // tocaron antes de que la tienda terminara de aparecer
  let temporizador = null;

  function entrar() {
    if (intro.classList.contains("entrando")) return;
    if (!lista) { quiereEntrar = true; return; }
    clearTimeout(temporizador);
    Sonido.alEntrar();
    // La cámara se endereza y entra derecho por la puerta
    giro.style.setProperty("--giro-x", "0deg");
    giro.style.setProperty("--giro-y", "0deg");
    intro.classList.add("entrando");
  }

  function empezar() {
    intro.classList.add("reproduciendo");
  }

  // Empieza cuando la letra del letrero esté lista (máximo 600 ms de espera)
  const letraLista = document.fonts
    ? Promise.race([document.fonts.load('700 48px "Cormorant Garamond"'), new Promise((r) => setTimeout(r, 600))])
    : Promise.resolve();
  letraLista.then(empezar);

  intro.addEventListener("animationend", (e) => {
    // La cámara terminó de acercarse: la puerta espera que la toquen
    if (e.animationName === "intro-acercar") {
      lista = true;
      intro.classList.add("lista");
      if (quiereEntrar) entrar();
      else if (ESPERA_AUTOMATICA) temporizador = setTimeout(entrar, ESPERA_AUTOMATICA);
    }
    if (e.animationName === "intro-sale") {
      Sonido.terminar();
      intro.remove();
    }
  });
  // Cuando la luz empieza a desvanecerse, la página hace sus animaciones de entrada
  intro.addEventListener("animationstart", (e) => {
    if (e.animationName === "intro-sale") A.soltarTelon();
  });

  // Tocar en cualquier parte o presionar cualquier tecla = entrar (con sonido)
  intro.addEventListener("click", entrar);
  function tecla(e) {
    if (!intro.isConnected) return window.removeEventListener("keydown", tecla);
    entrar();
  }
  window.addEventListener("keydown", tecla);
})();
