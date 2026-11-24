/*
 * ARRANQUE
 * --------
 * Archivo pequeño que se carga en el <head> de las 9 páginas, ANTES de que se vea la página.
 * Hace tres cosas:
 *   1. Avisa desde el principio que hay animaciones (clase .con-movimiento), para que
 *      el contenido no "aparezca y desaparezca" al cargar.
 *   2. PUERTAS ENTRE PÁGINAS: al pasar de una página a otra, dos puertas de vidrio
 *      se cierran, muestran "StyleMe" y se abren en la página nueva.
 *   3. TÍTULO DE LA PESTAÑA: cambia cuando la persona se va a otra pestaña.
 *
 * La intro 3D "Entrar a la tienda" está aparte, en intro.js (usa las utilidades de aquí).
 *
 * Mientras la intro o las puertas tapan la página, las animaciones de entrada
 * esperan (clase .esperando-telon) para que la persona sí las alcance a ver.
 */
(function () {
  const raiz = document.documentElement;
  const menosMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  /* ---------- 1. Animaciones activas desde el principio ---------- */
  if ("IntersectionObserver" in window) {
    raiz.classList.add("con-movimiento");
    // Seguro: si movimiento.js no carga, en 3 s se quita la clase y todo se ve normal
    setTimeout(() => {
      if (!window.StyleMeMovimiento) raiz.classList.remove("con-movimiento", "esperando-telon");
    }, 3000);
  }

  /* ---------- Utilidades (también las usa intro.js) ---------- */
  function leer(clave) {
    try { return sessionStorage.getItem(clave); } catch (e) { return null; }
  }
  function guardar(clave, valor) {
    try { sessionStorage.setItem(clave, valor); } catch (e) { /* navegación privada: no pasa nada */ }
  }

  // Tapa la página: las animaciones de entrada esperan hasta que el telón se empiece a abrir.
  // "maximo" = seguro por si algo falla (por ejemplo, la intro espera a que toquen la puerta).
  let seguroTelon = null;
  function esperarTelon(maximo = 6000) {
    raiz.classList.add("esperando-telon");
    clearTimeout(seguroTelon);
    seguroTelon = setTimeout(soltarTelon, maximo);
  }
  function soltarTelon() {
    if (!raiz.classList.contains("esperando-telon")) return;
    raiz.classList.remove("esperando-telon");
    document.dispatchEvent(new Event("styleme:telon-abierto"));
  }

  // Agrega un elemento a la página aunque el <body> todavía no exista
  function agregar(elemento) {
    (document.body || raiz).appendChild(elemento);
  }

  // Se lee ANTES de que las puertas lo borren: intro.js lo usa para saber
  // si la persona llegó desde otra página de StyleMe
  const vieneDeCambio = !!leer("styleme-paso");

  window.StyleMeArranque = { leer, guardar, esperarTelon, soltarTelon, agregar, menosMovimiento, vieneDeCambio };

  /* ---------- 2. Puertas entre páginas ---------- */
  // Funciona con View Transitions (Chrome, Edge y Safari recientes).
  // En otros navegadores la página cambia normal, sin puertas.
  //   - La página que se va guarda "styleme-paso" (lo hace movimiento.js):
  //       "foto"     → se tocó una tienda: la foto viaja al perfil (sin puertas)
  //       "cortina"  → cualquier otro cambio de página: puertas
  //   - La página que llega pone las puertas encima, ANTES de mostrarse.
  window.addEventListener("pagereveal", (evento) => {
    const paso = leer("styleme-paso");
    try { sessionStorage.removeItem("styleme-paso"); } catch (e) { /* nada */ }
    if (!evento.viewTransition || paso !== "cortina" || menosMovimiento) return;

    const hojas = ["izquierda", "derecha"].map((lado) => {
      const hoja = document.createElement("div");
      hoja.className = `puerta-pagina ${lado}`;
      hoja.setAttribute("aria-hidden", "true");
      hoja.innerHTML = `<span class="puerta-marca">${lado === "izquierda" ? "Style" : "Me"}</span>`;
      return hoja;
    });
    raiz.classList.add("con-puertas");
    esperarTelon();
    hojas.forEach(agregar);

    evento.viewTransition.finished.finally(() => raiz.classList.remove("con-puertas"));
    // Cuando las puertas empiezan a abrirse, la página nueva hace sus animaciones de entrada
    hojas[0].addEventListener("animationstart", (e) => {
      if (e.animationName === "puerta-pagina-abre-izq") soltarTelon();
    });
    hojas[0].addEventListener("animationend", (e) => {
      if (e.animationName === "puerta-pagina-abre-izq") hojas.forEach((h) => h.remove());
    });
  });

  /* ---------- 3. Título de la pestaña ---------- */
  // ✏️ Mensaje que se ve en la pestaña cuando la persona se va a otra
  const MENSAJE_PESTANA = "💄 ¡Vuelve! Te esperamos · StyleMe";
  let tituloGuardado = null;
  document.addEventListener("visibilitychange", () => {
    if (document.hidden) {
      tituloGuardado = document.title;
      document.title = MENSAJE_PESTANA;
    } else if (tituloGuardado) {
      document.title = tituloGuardado;
      tituloGuardado = null;
    }
  });
})();
