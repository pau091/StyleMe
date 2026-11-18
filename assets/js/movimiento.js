/*
 * MOVIMIENTO
 * ----------
 * Da vida a la página sin cambiar cómo funciona:
 *   - Revela con suavidad lo que tenga data-revelar cuando entra en pantalla (una sola vez).
 *   - Las cuadrículas con data-cascada muestran a sus hijos uno tras otro.
 *   - Los títulos con class="titulo-lineas" suben línea por línea.
 *   - También funciona con lo que las páginas pintan después con innerHTML
 *     (tarjetas, resultados de filtros), gracias al MutationObserver.
 *
 * Se carga AL FINAL de cada página, después del script de la página.
 * Si este archivo falla o no carga, todo el contenido se sigue viendo
 * (las clases que esconden cosas solo funcionan con .con-movimiento en <html>).
 */
(function () {
  const raizHTML = document.documentElement;

  // Navegadores muy viejos: no animamos nada y todo se ve normal
  if (!("IntersectionObserver" in window) || !("MutationObserver" in window)) return;

  const menosMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)");
  const punteroFino = window.matchMedia("(hover: hover) and (pointer: fine)");

  const MAX_ESCALONES = 8; // ✏️ en una cascada, del 9.º en adelante entran juntos

  try {
    raizHTML.classList.add("con-movimiento");

    /* ---------- Revelado al entrar en pantalla ---------- */

    const observador = new IntersectionObserver(
      (entradas) => {
        // Las tarjetas de una cascada que aparecen juntas se ordenan entre ellas:
        // así la primera visible no espera a las que están más arriba (ya vistas).
        let enEstaTanda = 0;
        entradas.forEach((entrada) => {
          if (!entrada.isIntersecting) return;
          const el = entrada.target;
          if (el.hasAttribute("data-en-cascada")) {
            el.style.setProperty("--orden", Math.min(enEstaTanda, MAX_ESCALONES));
            enEstaTanda++;
          }
          el.classList.add("visible");
          observador.unobserve(el); // cada cosa se anima una sola vez
        });
      },
      { rootMargin: "0px 0px -8% 0px", threshold: 0.12 }
    );

    // Los hijos de una cuadrícula con data-cascada se revelan en orden
    function prepararCascada(contenedor) {
      for (const hijo of contenedor.children) {
        if (!hijo.hasAttribute("data-revelar")) hijo.setAttribute("data-revelar", "");
        hijo.setAttribute("data-en-cascada", "");
      }
    }

    // Parte un título en líneas (cada <br> = una línea nueva)
    function prepararTitulo(titulo) {
      if (titulo.dataset.listo) return;
      titulo.dataset.listo = "1";
      const partes = titulo.innerHTML.split(/<br\s*\/?>/i);
      titulo.innerHTML = partes
        .map((parte, i) => `<span class="linea"><span style="--orden:${i}">${parte.trim()}</span></span>`)
        .join("");
      observador.observe(titulo);
    }

    // Busca dentro de "raiz" todo lo que hay que animar y lo prepara
    function activar(raiz = document) {
      if (raiz.nodeType !== 1 && raiz !== document) return;
      raiz.querySelectorAll("[data-cascada]").forEach(prepararCascada);
      raiz.querySelectorAll(".titulo-lineas").forEach(prepararTitulo);
      raiz.querySelectorAll("[data-revelar]:not(.visible)").forEach((el) => observador.observe(el));
      // El propio elemento también puede tener data-revelar
      if (raiz !== document && raiz.matches("[data-revelar]:not(.visible)")) observador.observe(raiz);
    }

    /* ---------- Lo que se pinta después (innerHTML de cada página) ---------- */

    new MutationObserver((cambios) => {
      // Se juntan los contenedores que cambiaron para revisarlos una sola vez
      const porRevisar = new Set();
      cambios.forEach((cambio) => {
        if (cambio.addedNodes.length === 0) return;
        const padre = cambio.target;
        const cascada = padre.closest?.("[data-cascada]");
        if (cascada) prepararCascada(cascada);
        porRevisar.add(padre);
      });
      porRevisar.forEach((padre) => activar(padre));
    }).observe(document.body, { childList: true, subtree: true });

    /* ---------- Encabezado que se compacta al bajar ---------- */
    // Un "centinela" invisible arriba de la página: cuando sale de la pantalla
    // (la persona bajó más de 120 px), el encabezado gana sombra y el logo se achica.
    // Usa IntersectionObserver, no el evento scroll, para no gastar en cada cuadro.
    function vigilarEncabezado() {
      const encabezado = document.getElementById("encabezado");
      if (!encabezado) return;
      const centinela = document.createElement("div");
      centinela.setAttribute("aria-hidden", "true");
      centinela.style.cssText =
        "position:absolute;top:0;left:0;width:1px;height:120px;pointer-events:none;visibility:hidden;";
      document.body.prepend(centinela);
      new IntersectionObserver(([entrada]) => {
        encabezado.classList.toggle("encabezado-compacto", !entrada.isIntersecting);
      }).observe(centinela);
    }
    vigilarEncabezado();

    /* ---------- Brillo del logo: solo la primera página de cada visita ---------- */
    try {
      if (sessionStorage.getItem("styleme-brillo")) raizHTML.classList.add("brillo-visto");
      else sessionStorage.setItem("styleme-brillo", "1");
    } catch (e) {
      // Navegación privada sin almacenamiento: el brillo simplemente sale siempre
    }

    // En iPhone, :active (botón que se hunde) solo funciona si hay una escucha de toque
    document.addEventListener("touchstart", () => {}, { passive: true });

    /* ---------- Utilidades para las otras partes del proyecto ---------- */

    // Cuenta un número de "desde" a "hasta" (ej.: "12 productos").
    // Basado en el tiempo real: dura lo mismo a 60, 120 o 144 Hz.
    function animarNumero(elemento, desde, hasta, duracion = 400) {
      if (menosMovimiento.matches || desde === hasta) {
        elemento.textContent = hasta;
        return;
      }
      const inicio = performance.now();
      const suave = (t) => 1 - Math.pow(1 - t, 3); // frena al final
      function cuadro(ahora) {
        const t = Math.min((ahora - inicio) / duracion, 1);
        elemento.textContent = Math.round(desde + (hasta - desde) * suave(t));
        if (t < 1) requestAnimationFrame(cuadro);
      }
      requestAnimationFrame(cuadro);
    }

    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", () => activar());
    } else {
      activar();
    }

    // Disponible para las otras partes del proyecto: window.StyleMeMovimiento.animarNumero(...)
    window.StyleMeMovimiento = { activar, animarNumero, menosMovimiento, punteroFino };
  } catch (error) {
    // Si algo falla, quitamos la clase para que todo se vea sin animación
    raizHTML.classList.remove("con-movimiento");
    console.error("StyleMe · movimiento.js no pudo iniciar:", error);
  }
})();
