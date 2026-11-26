/*
 * BUSCADOR CON IA DE LA PORTADA (idea 1)
 * --------------------------------------
 * Mientras la persona escribe, se piden resultados a la búsqueda semántica del backend
 * (GET /api/busqueda: embeddings + pgvector) y se muestran debajo del buscador.
 * Cada resultado dice si coincidió "por nombre" o "por significado": así se ve que la IA
 * entiende lo que la persona quiso decir aunque no use las mismas palabras del producto.
 *
 * Para no gastar la cuota de la IA:
 *   - espera a que la persona deje de escribir (ESPERA_MS) y a que haya al menos 3 letras;
 *   - recuerda las búsquedas ya hechas (no las repite).
 * Si el backend no responde, busca por nombre en el catálogo que ya tiene la página.
 */
(function () {
  // ✏️ Frases que se escriben solas, letra por letra, dentro del buscador
  const FRASES = [
    "¿Buscando una nueva experiencia?",
    "¿Productos capilares?",
    "¿Productos para el cuidado de tu piel?",
    "¿Make up?",
    "algo negro para una fiesta",
    "labial que dure todo el día"
  ];
  const TEXTO_FIJO = "Blusas, labiales, tiendas..."; // si la persona pidió menos movimiento

  // ✏️ Botones de "Prueba:" (búsquedas que muestran cómo la IA entiende el significado)
  const EJEMPLOS = [
    "algo elegante para un matrimonio",
    "algo para hidratar la piel seca",
    "look para salir de noche",
    "labial que dure todo el día",
    "shampoo para cabello rizado"
  ];
  const ESPERA_MS = 450;     // ✏️ cuánto esperar después de la última letra
  const MAXIMO_VISIBLES = 4; // ✏️ resultados que se muestran debajo del buscador

  const formulario = document.getElementById("buscador-ia");
  const campo = document.getElementById("busqueda");
  const panel = document.getElementById("sugerencias-busqueda");
  const anuncio = document.getElementById("estado-busqueda");
  const ejemplos = document.getElementById("ejemplos-busqueda");
  const menosMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  const recordadas = new Map(); // texto → códigos de producto
  let temporizador = null;
  let ultimaPedida = "";

  /* ---------- Botones de ejemplo ---------- */
  ejemplos.insertAdjacentHTML("beforeend", EJEMPLOS.slice(0, 3)
    .map((e) => `<button type="button" class="ejemplo-busqueda">${escaparHTML(e)}</button>`).join(""));
  ejemplos.addEventListener("click", (evento) => {
    const boton = evento.target.closest(".ejemplo-busqueda");
    if (!boton) return;
    detenerEscritura();
    campo.value = boton.textContent;
    campo.focus();
    buscar(campo.value);
  });

  /* ---------- El texto de ejemplo se escribe solo (si la persona no está escribiendo) ---------- */
  let escritura = null;
  function escribirEjemplos() {
    if (menosMovimiento || escritura) return; // quien pidió menos movimiento ve el texto fijo
    let ejemplo = 0, letra = 0, borrando = false;
    escritura = setInterval(() => {
      const texto = FRASES[ejemplo];
      letra += borrando ? -1 : 1;
      campo.placeholder = texto.slice(0, letra) + (borrando ? "" : "|");
      if (!borrando && letra === texto.length + 12) borrando = true; // pausa con el texto completo
      if (borrando && letra <= 0) {
        borrando = false;
        ejemplo = (ejemplo + 1) % FRASES.length;
      }
    }, 70);
  }
  function detenerEscritura() {
    clearInterval(escritura);
    escritura = null;
    campo.placeholder = TEXTO_FIJO;
  }
  // Mientras la persona está en el buscador, el texto se queda quieto; si sale sin escribir, vuelve a moverse
  campo.addEventListener("focus", detenerEscritura);
  campo.addEventListener("blur", () => { if (!campo.value) escribirEjemplos(); });
  escribirEjemplos();

  /* ---------- Búsqueda mientras se escribe ---------- */
  campo.addEventListener("input", () => {
    clearTimeout(temporizador);
    const texto = campo.value.trim();
    if (texto.length < 3) return cerrarPanel();
    temporizador = setTimeout(() => buscar(texto), ESPERA_MS);
  });

  async function buscar(texto) {
    texto = texto.trim();
    if (texto.length < 3) return;
    ultimaPedida = texto;
    abrirPanel(`<p class="sugerencias-estado"><span class="punto-pensando" aria-hidden="true"></span> Buscando por significado…</p>`);
    await cuandoHayaDatos(() => {});

    let codigos;
    let sinIA = false;
    try {
      codigos = recordadas.get(texto.toLowerCase()) ?? (await StyleMe.buscarProductos(texto));
      recordadas.set(texto.toLowerCase(), codigos);
    } catch (e) {
      codigos = porNombre(texto);
      sinIA = true;
    }
    if (texto !== ultimaPedida) return; // llegó tarde: la persona ya escribió otra cosa
    pintar(texto, codigos, sinIA);
  }

  // Respaldo sin backend: productos cuyo nombre o marca contiene alguna palabra buscada
  function porNombre(texto) {
    const palabras = normalizar(texto).split(/\s+/).filter((p) => p.length > 2);
    return PRODUCTOS.filter((p) => palabras.some((w) => normalizar(`${p.nombre} ${p.marca}`).includes(w))).map((p) => p.id);
  }

  function normalizar(texto) {
    return String(texto ?? "").toLowerCase().normalize("NFD").replace(/[̀-ͯ]/g, "");
  }

  function pintar(texto, codigos, sinIA) {
    const tiendas = TIENDAS.filter((t) => normalizar(t.nombre).includes(normalizar(texto))).slice(0, 2);
    const productos = codigos.map(buscarProducto).filter(Boolean).slice(0, MAXIMO_VISIBLES);
    if (!productos.length && !tiendas.length) {
      anuncio.textContent = "Sin resultados";
      return abrirPanel(`<p class="sugerencias-estado">No encontramos eso en las tiendas de StyleMe. Prueba con otras palabras.</p>`);
    }
    const palabras = normalizar(texto).split(/\s+/).filter((p) => p.length > 2);
    const filaTienda = (t) => `
      <li><a class="sugerencia" href="tienda.html?id=${encodeURIComponent(t.id)}">
        <span class="sugerencia-foto">${imagenOMarcador(t.foto, "", t.tipo)}</span>
        <span class="sugerencia-texto"><strong>${escaparHTML(t.nombre)}</strong><span>Tienda · ${escaparHTML(t.zona || t.direccion)}</span></span>
        <span class="sugerencia-tipo">Tienda</span>
      </a></li>`;
    const filaProducto = (p) => {
      const tienda = buscarTienda(p.tiendaId);
      const porNombre = palabras.some((w) => normalizar(p.nombre).includes(w));
      return `
      <li><a class="sugerencia" href="${tienda ? `tienda.html?id=${encodeURIComponent(tienda.id)}` : "productos.html"}" data-codigo="${escaparHTML(p.id)}">
        <span class="sugerencia-foto">${imagenOMarcador(p.foto, "", p.categoria)}</span>
        <span class="sugerencia-texto"><strong>${escaparHTML(p.nombre)}</strong><span>${escaparHTML(tienda?.nombre ?? "")} · ${formatoPrecio(p.precio)}</span></span>
        <span class="sugerencia-tipo ${porNombre || sinIA ? "" : "por-significado"}">${porNombre || sinIA ? "Por nombre" : "Por significado"}</span>
      </a></li>`;
    };
    abrirPanel(`
      <ul class="sugerencias-lista">${tiendas.map(filaTienda).join("")}${productos.map(filaProducto).join("")}</ul>
      <a class="sugerencias-todas" href="productos.html?q=${encodeURIComponent(texto)}">Ver todos los resultados →</a>
      ${sinIA ? '<p class="sugerencias-estado">La búsqueda con IA no está disponible: estos resultados son por nombre.</p>' : ""}`);
    anuncio.textContent = `${productos.length + tiendas.length} resultados`;
  }

  // Tocar un resultado cuenta como búsqueda para los indicadores (lo que se busca y lo que se encuentra)
  panel.addEventListener("click", (evento) => {
    if (!evento.target.closest("a")) return;
    StyleMe.registrarEvento("BUSQUEDA", {
      textoBusqueda: ultimaPedida.slice(0, 100),
      cantidadResultados: panel.querySelectorAll(".sugerencia").length,
      detalle: { origen: "portada" }
    });
  });

  /* ---------- Abrir y cerrar el panel ---------- */
  function abrirPanel(html) {
    panel.innerHTML = html;
    panel.hidden = false;
    campo.setAttribute("aria-expanded", "true");
  }
  function cerrarPanel() {
    panel.hidden = true;
    panel.innerHTML = "";
    campo.setAttribute("aria-expanded", "false");
  }
  campo.addEventListener("keydown", (evento) => {
    if (evento.key === "Escape") cerrarPanel();
    if (evento.key === "ArrowDown" && !panel.hidden) {
      evento.preventDefault();
      panel.querySelector("a")?.focus();
    }
  });
  panel.addEventListener("keydown", (evento) => {
    const enlaces = [...panel.querySelectorAll("a")];
    const actual = enlaces.indexOf(document.activeElement);
    if (evento.key === "ArrowDown") { evento.preventDefault(); enlaces[Math.min(actual + 1, enlaces.length - 1)]?.focus(); }
    if (evento.key === "ArrowUp") { evento.preventDefault(); (actual <= 0 ? campo : enlaces[actual - 1]).focus(); }
    if (evento.key === "Escape") { cerrarPanel(); campo.focus(); }
  });
  document.addEventListener("click", (evento) => {
    if (!formulario.contains(evento.target) && !ejemplos.contains(evento.target)) cerrarPanel();
  });
})();
