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
          // Si es el contenedor de unas fotos con telón, se descubren ellas
          if (telonesPorContenedor.has(el)) {
            telonesPorContenedor.get(el).forEach((foto) => foto.classList.add("visible"));
            telonesPorContenedor.delete(el);
            observador.unobserve(el);
            return;
          }
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

    // Las fotos con telón empiezan 100 % recortadas y el navegador las considera
    // "invisibles"; por eso se vigila a su contenedor, que sí tiene tamaño.
    const telonesPorContenedor = new Map();
    function prepararTelon(foto) {
      if (foto.dataset.telonListo) return;
      foto.dataset.telonListo = "1";
      const contenedor = foto.parentElement;
      if (!telonesPorContenedor.has(contenedor)) {
        telonesPorContenedor.set(contenedor, []);
        observador.observe(contenedor);
      }
      telonesPorContenedor.get(contenedor).push(foto);
    }

    // Los hijos de una cuadrícula con data-cascada se revelan en orden
    // data-cascada="rapido" hace que los hijos entren con la versión rápida (herramientas)
    function prepararCascada(contenedor) {
      const tipo = contenedor.dataset.cascada || "";
      for (const hijo of contenedor.children) {
        if (!hijo.hasAttribute("data-revelar")) hijo.setAttribute("data-revelar", tipo);
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

    /* ---------- Botón magnético (solo con mouse) ---------- */
    // El botón se inclina unos px hacia el cursor. Se usa en 1–2 botones por página.
    const MAX_IMAN = 8; // ✏️ cuántos px se puede mover como máximo

    function prepararMagnetico(boton) {
      if (boton.dataset.magneticoListo) return;
      boton.dataset.magneticoListo = "1";
      let caja = null;
      let pendiente = false;
      let x = 0;
      let y = 0;
      const limitar = (n) => Math.max(-MAX_IMAN, Math.min(MAX_IMAN, n));

      boton.addEventListener("pointerenter", () => {
        caja = boton.getBoundingClientRect(); // se mide una sola vez al entrar
      });
      boton.addEventListener(
        "pointermove",
        (evento) => {
          if (!caja || !punteroFino.matches || menosMovimiento.matches) return;
          x = limitar((evento.clientX - caja.left - caja.width / 2) * 0.25);
          y = limitar((evento.clientY - caja.top - caja.height / 2) * 0.35);
          if (pendiente) return; // como máximo un cambio por cuadro
          pendiente = true;
          requestAnimationFrame(() => {
            boton.style.translate = `${x}px ${y}px`;
            pendiente = false;
          });
        },
        { passive: true }
      );
      boton.addEventListener("pointerleave", () => {
        caja = null;
        boton.style.translate = "";
      });
    }

    // Busca dentro de "raiz" todo lo que hay que animar y lo prepara
    function activar(raiz = document) {
      if (raiz.nodeType !== 1 && raiz !== document) return;
      raiz.querySelectorAll("[data-cascada]").forEach(prepararCascada);
      raiz.querySelectorAll(".titulo-lineas").forEach(prepararTitulo);
      raiz.querySelectorAll("[data-magnetico]").forEach(prepararMagnetico);
      raiz.querySelectorAll('[data-revelar="telon"]:not(.visible)').forEach(prepararTelon);
      raiz.querySelectorAll('[data-revelar]:not(.visible):not([data-revelar="telon"])').forEach((el) => observador.observe(el));
      // El propio elemento también puede tener data-revelar
      if (raiz !== document && raiz.matches('[data-revelar]:not(.visible):not([data-revelar="telon"])')) observador.observe(raiz);
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

    /* ---------- La foto que viaja: tarjeta de tienda ⇄ perfil ---------- */
    // El perfil (tienda.html) siempre tiene una foto llamada "foto-viajera".
    // En las listas, solo la tarjeta que se tocó recibe ese nombre justo antes de
    // cambiar de página; así el navegador sabe qué foto tiene que hacer crecer.
    const CLAVE_VUELTA = "styleme-foto-viajera";
    let fotoTocada = null;

    // Recuerda qué tarjeta de tienda se tocó
    document.addEventListener(
      "click",
      (evento) => {
        const tarjeta = evento.target.closest?.("a.tarjeta");
        // Ctrl/Cmd/Shift + clic abre otra pestaña: aquí no hay viaje
        const otraPestana = evento.ctrlKey || evento.metaKey || evento.shiftKey || evento.button !== 0;
        fotoTocada = tarjeta && !otraPestana ? tarjeta.querySelector(".tarjeta-imagen[data-tienda]") : null;
      },
      true
    );

    function quitarNombresDeFotos() {
      document.querySelectorAll(".tarjeta-imagen[data-tienda]").forEach((foto) => {
        foto.style.viewTransitionName = "";
      });
    }

    // Al SALIR de una página
    window.addEventListener("pageswap", (evento) => {
      if (!evento.viewTransition) return;
      // Ida (lista → perfil): nombramos la foto de la tarjeta tocada
      if (fotoTocada) {
        quitarNombresDeFotos();
        fotoTocada.style.viewTransitionName = "foto-viajera";
      }
      // Vuelta (perfil → lista): recordamos de qué tienda veníamos
      const fotoPerfil = document.querySelector(".perfil-foto[data-tienda]");
      try {
        if (fotoPerfil) sessionStorage.setItem(CLAVE_VUELTA, fotoPerfil.dataset.tienda);
      } catch (e) { /* sin almacenamiento: solo no habrá viaje de vuelta */ }
    });

    // Al LLEGAR a una página
    window.addEventListener("pageshow", () => { fotoTocada = null; });
    window.addEventListener("pagereveal", (evento) => {
      let idTienda = null;
      try {
        idTienda = sessionStorage.getItem(CLAVE_VUELTA);
        sessionStorage.removeItem(CLAVE_VUELTA);
      } catch (e) { /* nada */ }

      quitarNombresDeFotos();
      if (!evento.viewTransition || !idTienda) return;

      const foto = document.querySelector(`.tarjeta-imagen[data-tienda="${CSS.escape(idTienda)}"]`);
      if (!foto) return;
      foto.style.viewTransitionName = "foto-viajera";

      // Su tarjeta se muestra ya (sin cascada) para que la foto tenga dónde aterrizar
      const tarjeta = foto.closest("[data-revelar]");
      if (tarjeta) {
        tarjeta.classList.add("visible", "sin-transicion");
        observador.unobserve(tarjeta);
      }
      evento.viewTransition.finished.finally(() => {
        foto.style.viewTransitionName = "";
        tarjeta?.classList.remove("sin-transicion");
      });
    });

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
