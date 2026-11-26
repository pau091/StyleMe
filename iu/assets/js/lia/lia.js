/*
 * LÍA · EL PERSONAJE
 * ------------------
 * Lía aparece en todas las páginas como un PERSONAJE (abajo a la derecha), no como un chat:
 *   - Habla con GLOBOS DE TEXTO que salen de ella.
 *   - La persona le responde AHÍ MISMO: tocando una respuesta rápida o escribiendo
 *     en el campo que está debajo del globo.
 *   - Cerrada, Lía se queda pequeña en su arco; si nadie le ha hablado, saluda con un globo corto.
 *   - Respira, se mueve al hablar y hace gestos según su emoción.
 *   - La conversación se recuerda durante la visita (aunque cambies de página).
 *     "Conversación" muestra lo que se ha hablado.
 *
 * Lía es un retrato 2D con expresiones (assets/lia/). Avisa sus cambios con estos eventos:
 *   "lia:emocion"  → { emocion, intensidad }
 *   "lia:pensando" → true / false
 *   "lia:hablando" → true / false
 *
 * Cómo responde: manda el mensaje al backend (POST /api/lia/chat: RAG con pgvector + Gemini).
 * El texto llega poco a poco. Si el backend no responde, usa el plan B (plan-b.js) y lo avisa.
 *
 * Necesita, antes de este archivo: api.js (catálogo y dirección del backend), comun.js y lia/plan-b.js.
 */
(function () {
  if (typeof PRODUCTOS === "undefined" || typeof TIENDAS === "undefined") return;

  /* ---------- ✏️ Ajustes ---------- */
  // ✏️ Dirección del backend. Se puede cambiar sin tocar este archivo definiendo window.STYLEME_API antes de cargarlo.
  const URL_API = window.STYLEME_API || "http://localhost:8090";
  const URL_CEREBRO = `${URL_API}/api/lia/chat`;
  const SALUDO = {
    texto: "¡Hola! Soy Lía, tu asesora en StyleMe. Te ayudo a encontrar ropa, maquillaje y cuidado para tu piel y tu cabello en tiendas de Bucaramanga. ¿Qué estás buscando hoy?",
    emocion: "felicidad",
    sugerencias: ["Busco maquillaje", "Rutina para mi piel", "Algo para mi cabello"]
  };
  const INVITACION = "¡Hola! Soy Lía. ¿Te ayudo a encontrar algo?"; // globo corto cuando está cerrada
  const ESPERA_INVITACION = 2500; // ms después de cargar la página
  const NOMBRE_PASILLO = { ropa: "ropa y moda", maquillaje: "maquillaje", skincare: "skincare", capilar: "cuidado capilar" };
  const CLAVE_HISTORIA = "styleme-lia-historia";
  const CLAVE_ABIERTA = "styleme-lia-abierta";
  const CLAVE_INVITO = "styleme-lia-invito";

  /* ---------- Memoria de la visita ---------- */
  const leerSesion = (clave) => { try { return sessionStorage.getItem(clave); } catch (e) { return null; } };
  const guardarSesion = (clave, valor) => {
    try { valor === null ? sessionStorage.removeItem(clave) : sessionStorage.setItem(clave, valor); } catch (e) { /* nada */ }
  };
  let historia = (() => { try { return JSON.parse(leerSesion(CLAVE_HISTORIA)) || []; } catch (e) { return []; } })();
  // [{ rol: "persona" | "lia", texto, emocion, productos, acciones, sugerencias, basico }]
  const guardarHistoria = () => guardarSesion(CLAVE_HISTORIA, JSON.stringify(historia.slice(-30)));
  // Identificador ANÓNIMO de la conversación: el backend guarda un resumen (sin el texto) para el informe de datos
  const CLAVE_SESION = "styleme-lia-sesion";
  const nuevaSesion = () => {
    const id = (crypto.randomUUID ? crypto.randomUUID() : String(Date.now()) + Math.random().toString(16).slice(2)).slice(0, 36);
    guardarSesion(CLAVE_SESION, id);
    return id;
  };
  let sesionId = leerSesion(CLAVE_SESION) || nuevaSesion();
  let ocupada = false;

  /* ---------- Armar el personaje ---------- */
  const lia = document.createElement("div");
  lia.className = "lia";
  lia.dataset.estado = "cerrada";      // cerrada | abierta
  lia.dataset.emocion = "neutra";      // felicidad | preocupacion | tristeza | duda | asombro | neutra
  lia.dataset.actividad = "quieta";    // quieta | pensando | hablando
  lia.innerHTML = `
    <div class="lia-escena" id="lia-escena" hidden>
      <div class="lia-globo">
        <div class="lia-globo-barra">
          <button type="button" class="lia-mini lia-ver-historial" aria-expanded="false">Conversación</button>
          <button type="button" class="lia-mini lia-nueva" aria-label="Empezar una conversación nueva" title="Nueva conversación">↺</button>
          <button type="button" class="lia-mini lia-cerrar" aria-label="Cerrar a Lía" title="Cerrar">✕</button>
        </div>
        <div class="lia-historial" hidden></div>
        <p class="lia-tu" hidden></p>
        <p class="lia-dice" aria-live="polite"></p>
        <div class="lia-extras"></div>
      </div>
      <div class="lia-respuestas" aria-label="Respuestas rápidas"></div>
      <form class="lia-responder">
        <label class="visualmente-oculto" for="lia-entrada">Respóndele a Lía</label>
        <input id="lia-entrada" type="text" maxlength="500" autocomplete="off" placeholder="Pregúntale a Lía…">
        <button type="submit" aria-label="Enviar">➤</button>
      </form>
      <p class="lia-aviso">Lía es una IA y puede equivocarse. No compartas datos personales.</p>
    </div>
    <p class="lia-invitacion" hidden></p>
    <button type="button" class="lia-figura" aria-controls="lia-escena" aria-expanded="false" aria-label="Lía, tu asesora virtual. Toca para hablar con ella">
      <span class="lia-cuerpo">
        <span class="lia-arco">
          <span class="lia-retrato" aria-hidden="true">
            <span class="lia-retrato-cabeza">
              <span class="lia-retrato-pose lia-pose-principal"><span class="lia-retrato-boca"></span></span>
              <span class="lia-retrato-pose lia-pose-feliz"><span class="lia-retrato-boca"></span></span>
              <span class="lia-retrato-pose lia-pose-duda"><span class="lia-retrato-boca"></span></span>
              <span class="lia-retrato-pose lia-pose-asombro"><span class="lia-retrato-boca"></span></span>
              <span class="lia-retrato-parpadeo"></span>
            </span>
          </span>
          <span class="lia-interrogacion" aria-hidden="true">?</span>
        </span>
      </span>
      <span class="lia-placa">Lía <span>· asesora IA</span></span>
    </button>`;

  const $ = (selector) => lia.querySelector(selector);
  const escena = $(".lia-escena");
  const figura = $(".lia-figura");
  const dice = $(".lia-dice");
  const tu = $(".lia-tu");
  const extras = $(".lia-extras");
  const respuestas = $(".lia-respuestas");
  const entrada = $("#lia-entrada");
  const historial = $(".lia-historial");
  const botonHistorial = $(".lia-ver-historial");
  const invitacion = $(".lia-invitacion");

  /* ---------- Estado del personaje ---------- */
  let finHabla;
  let finReaccion;

  function ponerEmocion(emocion = "neutra", intensidad = 0.6) {
    clearTimeout(finReaccion);
    lia.dataset.emocion = emocion;
    lia.style.setProperty("--lia-intensidad", Math.max(0, Math.min(1, Number(intensidad) || 0)));
    document.dispatchEvent(new CustomEvent("lia:emocion", { detail: { emocion, intensidad } }));
    // La tristeza dura poco: Lía vuelve a la expresión amable
  }
  function ponerActividad(actividad) {
    lia.dataset.actividad = actividad;
    document.dispatchEvent(new CustomEvent("lia:pensando", { detail: actividad === "pensando" }));
    document.dispatchEvent(new CustomEvent("lia:hablando", { detail: actividad === "hablando" }));
  }

  /* ---------- Piezas del globo ---------- */
  const sinMarkdown = (t) => t.replace(/\*\*(.+?)\*\*/g, "$1").replace(/^\s*[-*]\s+/gm, "• ");

  function tarjetaMini(id) {
    const p = buscarProducto(id);
    if (!p) return "";
    const tienda = buscarTienda(p.tiendaId);
    return `
      <a class="lia-producto" href="tienda.html?id=${encodeURIComponent(p.tiendaId)}">
        <span class="lia-producto-foto">${imagenOMarcador(p.foto, p.nombre, p.categoria)}</span>
        <span class="lia-producto-datos">
          <strong>${escaparHTML(p.nombre)}</strong>
          <span class="precio">${formatoPrecio(p.precio)}</span>
          ${tienda ? `<span class="lia-producto-tienda">${icono("tienda")} En ${escaparHTML(tienda.nombre)}${tienda.zona ? ` · ${escaparHTML(tienda.zona)}` : ""}</span>` : ""}
        </span>
      </a>`;
  }

  function botonAccion(a) {
    const tienda = a.tienda && buscarTienda(a.tienda);
    const producto = a.producto && buscarProducto(a.producto);
    switch (a.tipo) {
      case "ver_tienda":
        return tienda ? `<a class="lia-accion" href="tienda.html?id=${encodeURIComponent(tienda.id)}">Ver ${escaparHTML(tienda.nombre)}</a>` : "";
      case "contactar_tienda": {
        if (!tienda) return "";
        const texto = `Hola, vi en StyleMe ${producto ? `el producto "${producto.nombre}"` : "su tienda"}. ¿Me pueden dar más información?`;
        const enlace = enlaceWhatsapp(tienda.whatsapp, texto) || enlaceInstagram(tienda.instagram) || enlaceWeb(tienda.web);
        return enlace
          ? `<a class="lia-accion lia-accion-whatsapp" href="${enlace}" target="_blank" rel="noopener" ${eventoTienda(tienda)}>Escribir a ${escaparHTML(tienda.nombre)}</a>`
          : `<a class="lia-accion" href="tienda.html?id=${encodeURIComponent(tienda.id)}">Contacto de ${escaparHTML(tienda.nombre)}</a>`;
      }
      case "abrir_probador":
        return producto ? `<a class="lia-accion" href="probador.html?producto=${encodeURIComponent(producto.id)}">Probar virtualmente</a>` : "";
      case "armar_salida":
        return `<a class="lia-accion" href="salida.html">Armar salida de compras</a>`;
      case "ir_a_pasillo":
        // En 2D lleva al catálogo de esa categoría (en la Fase 3 moverá la cámara al pasillo)
        return a.pasillo ? `<a class="lia-accion" href="productos.html?categoria=${a.pasillo}">Ver todo en ${NOMBRE_PASILLO[a.pasillo]}</a>` : "";
      default:
        return "";
    }
  }

  function pintarExtras(datos) {
    const productos = (datos.productos || []).map(tarjetaMini).join("");
    const vistas = new Set();
    const acciones = (datos.acciones || [])
      .map(botonAccion)
      .filter((html) => html && !vistas.has(html) && vistas.add(html))
      .join("");
    extras.innerHTML =
      (productos ? `<p class="lia-fuente">Del catálogo de StyleMe: cada producto dice en qué tienda está</p><div class="lia-productos">${productos}</div>` : "") +
      (acciones ? `<div class="lia-acciones">${acciones}</div>` : "") +
      (datos.basico ? `<p class="lia-modo">Modo básico (sin IA por ahora)</p>` : "");
  }

  function pintarRespuestas(lista = []) {
    respuestas.innerHTML = lista
      .map((s) => `<button type="button" class="lia-respuesta">${escaparHTML(s)}</button>`)
      .join("");
  }

  // Lo que se ha hablado (se abre con el botón "Conversación")
  function pintarHistorial() {
    const anteriores = historia.slice(0, -1);
    historial.innerHTML = anteriores.length
      ? anteriores
          .map((m) => `<p class="lia-linea lia-linea-${m.rol}"><strong>${m.rol === "lia" ? "Lía" : "Tú"}:</strong> ${escaparHTML(m.texto)}</p>`)
          .join("")
      : `<p class="lia-linea texto-suave">Aquí aparecerá lo que hablen.</p>`;
    historial.scrollTop = historial.scrollHeight;
  }

  // Muestra en el globo la última respuesta de Lía (al abrir o al cambiar de página)
  function pintarUltima() {
    const ultimaLia = [...historia].reverse().find((m) => m.rol === "lia") || { ...SALUDO };
    const indice = historia.lastIndexOf(ultimaLia);
    const antes = indice > 0 ? historia[indice - 1] : null;
    tu.hidden = !(antes && antes.rol === "persona");
    if (antes) tu.textContent = `Tú: ${antes.texto}`;
    dice.textContent = ultimaLia.texto;
    pintarExtras(ultimaLia);
    pintarRespuestas(ultimaLia.sugerencias);
    ponerEmocion("neutra");
    if (!historial.hidden) pintarHistorial();
  }

  /* ---------- Conversar ---------- */
  async function preguntarAlCerebro(mensaje, alTexto) {
    const respuesta = await fetch(URL_CEREBRO, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ mensaje, sesionId, historia: historia.slice(-31, -1).map((m) => ({ rol: m.rol, texto: m.texto })) })
    });
    const tipo = respuesta.headers.get("Content-Type") || "";
    if (!respuesta.ok || !tipo.includes("ndjson") || !respuesta.body) throw new Error("sin cerebro");

    const lector = respuesta.body.getReader();
    const decodificar = new TextDecoder();
    let pendiente = "";
    let final = null;
    while (true) {
      const { value, done } = await lector.read();
      if (done) break;
      pendiente += decodificar.decode(value, { stream: true });
      const lineas = pendiente.split("\n");
      pendiente = lineas.pop();
      for (const linea of lineas) {
        if (!linea.trim()) continue;
        const evento = JSON.parse(linea);
        if (evento.tipo === "texto") alTexto(evento.delta);
        else if (evento.tipo === "fin") final = evento;
        else if (evento.tipo === "error") throw new Error(evento.mensaje);
      }
    }
    if (!final) throw new Error("respuesta incompleta");
    return final;
  }

  async function enviar(texto) {
    texto = texto.trim().slice(0, 500);
    if (!texto || ocupada) return;
    ocupada = true;
    clearTimeout(finHabla);
    entrada.value = "";
    pintarRespuestas([]);
    extras.innerHTML = "";

    historia.push({ rol: "persona", texto });
    guardarHistoria();
    tu.hidden = false;
    tu.textContent = `Tú: ${texto}`;

    // Lía piensa: tres puntos en el globo
    dice.innerHTML = `<span class="lia-puntos" aria-label="Lía está pensando"><i></i><i></i><i></i></span>`;
    ponerEmocion("neutra");
    ponerActividad("pensando");

    let escrito = "";
    let datos;
    try {
      datos = await preguntarAlCerebro(texto, (delta) => {
        if (!escrito) ponerActividad("hablando");
        escrito += delta;
        dice.textContent = sinMarkdown(escrito.trimStart());
      });
      datos.texto = sinMarkdown(escrito.trim());
    } catch (error) {
      if (escrito.trim()) {
        datos = { texto: sinMarkdown(escrito.trim()), emocion: "neutra", productos: [], acciones: [], sugerencias: [] };
      } else {
        // Plan B: responde con reglas y avisa que está en modo básico
        ponerActividad("hablando");
        await new Promise((r) => setTimeout(r, 450));
        datos = { ...LiaPlanB.responder(texto, historia.slice(0, -1)), basico: true };
      }
    }

    dice.textContent = datos.texto;
    ponerEmocion(datos.emocion, datos.intensidad);
    pintarExtras(datos);
    pintarRespuestas(datos.sugerencias);
    // Sigue "hablando" un momento, y luego se queda tranquila
    const restante = 1400;
    if (datos.emocion === "duda") finReaccion = setTimeout(() => ponerEmocion("neutra"), 2200);
    finHabla = setTimeout(() => {
      ponerActividad("quieta");
      if (datos.emocion !== "duda" && lia.dataset.emocion !== "neutra") finReaccion = setTimeout(() => ponerEmocion("neutra"), 1000);
    }, restante);

    historia.push({
      rol: "lia",
      texto: datos.texto,
      emocion: datos.emocion,
      intensidad: datos.intensidad,
      productos: datos.productos,
      acciones: datos.acciones,
      sugerencias: datos.sugerencias,
      basico: !!datos.basico
    });
    guardarHistoria();
    if (!historial.hidden) pintarHistorial();
    ocupada = false;
    entrada.focus({ preventScroll: true });
  }

  /* ---------- Abrir y cerrar ---------- */
  function abrir(enfocar = true) {
    ocultarInvitacion();
    guardarSesion(CLAVE_INVITO, "1");
    escena.hidden = false;
    lia.dataset.estado = "abierta";
    figura.setAttribute("aria-expanded", "true");
    guardarSesion(CLAVE_ABIERTA, "1");
    if (enfocar) window.StyleMe?.registrarEvento("ABRIR_LIA"); // solo cuando la persona la abre (no al cambiar de página)
    pintarUltima();
    if (enfocar) entrada.focus({ preventScroll: true });
  }
  function cerrar() {
    escena.hidden = true;
    lia.dataset.estado = "cerrada";
    figura.setAttribute("aria-expanded", "false");
    guardarSesion(CLAVE_ABIERTA, null);
    ponerEmocion("neutra");
    figura.focus({ preventScroll: true });
  }

  /* ---------- Invitación: Lía saluda sola una vez por visita ---------- */
  let temporizadorInvitacion = null;
  function mostrarInvitacion() {
    if (lia.dataset.estado === "abierta" || leerSesion(CLAVE_INVITO)) return;
    guardarSesion(CLAVE_INVITO, "1");
    invitacion.textContent = INVITACION;
    invitacion.hidden = false;
    ponerEmocion("felicidad", 0.5);
    finReaccion = setTimeout(() => ponerEmocion("neutra"), 2000);
    temporizadorInvitacion = setTimeout(ocultarInvitacion, 7000);
  }
  function ocultarInvitacion() {
    clearTimeout(temporizadorInvitacion);
    invitacion.hidden = true;
  }
  function programarInvitacion() {
    // Si la intro o las puertas están tapando la página, espera a que se abran
    if (document.documentElement.classList.contains("esperando-telon")) {
      document.addEventListener("styleme:telon-abierto", () => setTimeout(mostrarInvitacion, ESPERA_INVITACION), { once: true });
    } else {
      setTimeout(mostrarInvitacion, ESPERA_INVITACION);
    }
  }

  /* ---------- Eventos ---------- */
  figura.addEventListener("click", () => (lia.dataset.estado === "abierta" ? cerrar() : abrir()));
  invitacion.addEventListener("click", () => abrir());
  $(".lia-cerrar").addEventListener("click", cerrar);
  $(".lia-nueva").addEventListener("click", () => {
    if (ocupada) return;
    clearTimeout(finHabla);
    ponerActividad("quieta");
    historia = [];
    guardarHistoria();
    sesionId = nuevaSesion(); // una conversación nueva es otra fila en el informe
    historial.hidden = true;
    botonHistorial.setAttribute("aria-expanded", "false");
    pintarUltima();
    entrada.focus({ preventScroll: true });
  });
  botonHistorial.addEventListener("click", () => {
    historial.hidden = !historial.hidden;
    botonHistorial.setAttribute("aria-expanded", String(!historial.hidden));
    if (!historial.hidden) pintarHistorial();
  });
  lia.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && lia.dataset.estado === "abierta") cerrar();
  });
  $(".lia-responder").addEventListener("submit", (e) => {
    e.preventDefault();
    enviar(entrada.value);
  });
  respuestas.addEventListener("click", (e) => {
    const boton = e.target.closest(".lia-respuesta");
    if (boton) enviar(boton.textContent);
  });
  // Cualquier enlace a "#lia" la abre
  document.addEventListener("click", (e) => {
    const enlace = e.target.closest?.('a[href="#lia"]');
    if (!enlace) return;
    e.preventDefault();
    abrir();
  });

  /* ---------- El retrato sigue al mouse ---------- */
  // La foto se desplaza un poquito hacia donde está el cursor, como si mirara.
  let pendienteMirada = false;
  window.addEventListener("pointermove", (e) => {
    if (pendienteMirada) return;
    pendienteMirada = true;
    requestAnimationFrame(() => {
      const caja = figura.getBoundingClientRect();
      const x = Math.max(-1, Math.min(1, (e.clientX - (caja.left + caja.width / 2)) / (window.innerWidth / 2)));
      const y = Math.max(-1, Math.min(1, (e.clientY - (caja.top + caja.height * 0.3)) / (window.innerHeight / 2)));
      lia.style.setProperty("--mirar-x", x.toFixed(3));
      lia.style.setProperty("--mirar-y", y.toFixed(3));
      pendienteMirada = false;
    });
  }, { passive: true });

  document.body.append(lia);
  if (leerSesion(CLAVE_ABIERTA)) abrir(false);
  else programarInvitacion();

  window.Lia = { abrir, cerrar, enviar };
})();
