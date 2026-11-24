---
name: styleme-animaciones
description: Sistema de animación de StyleMe - tokens de movimiento, revelado al hacer scroll, cascadas (stagger), telón con clip-path, títulos línea por línea, scroll-driven animations y prefers-reduced-motion. Usar al agregar o cambiar cualquier animación de entrada, keyframes o efecto de scroll en StyleMe, o al crear frontend/assets/css/movimiento.css y frontend/assets/js/movimiento.js.
---

# StyleMe · Animaciones

## Antes de empezar
- Lee `planeacion/11-contexto-visual.md` §4 y la skill `styleme-identidad-visual`.
- Toda animación debe cumplir `styleme-rendimiento-120fps` (solo `transform`/`opacity`/`clip-path`).
- Código y comentarios en español sencillo. Sin frameworks ni librerías de animación. Sin git.

## 1. Archivos base (créalos si no existen)

### `frontend/assets/css/movimiento.css`
Se enlaza en el `<head>` de **todas** las páginas, justo después de `estilos.css`.

```css
/* MOVIMIENTO DE STYLEME: todo lo que se anima vive aquí */
:root {
  --curva-seda: cubic-bezier(0.22, 1, 0.36, 1);
  --curva-vitrina: cubic-bezier(0.65, 0, 0.35, 1);
  --curva-pulso: cubic-bezier(0.34, 1.56, 0.64, 1);
  --duracion-micro: 160ms;
  --duracion-corta: 280ms;
  --duracion-media: 520ms;
  --duracion-larga: 900ms;
  --escalon: 70ms;
}

/* Revelado al entrar en pantalla.
   Solo se oculta si movimiento.js cargó (clase .con-movimiento en <html>),
   así, si el JS falla, todo se sigue viendo. */
.con-movimiento [data-revelar] {
  opacity: 0;
  transform: translateY(24px);
  transition:
    opacity var(--duracion-media) var(--curva-seda),
    transform var(--duracion-media) var(--curva-seda);
  transition-delay: calc(var(--orden, 0) * var(--escalon));
}
.con-movimiento [data-revelar].visible { opacity: 1; transform: none; }

/* Telón: la foto se descubre de abajo hacia arriba */
.con-movimiento [data-revelar="telon"] {
  opacity: 1;
  transform: none;
  clip-path: inset(100% 0 0 0 round var(--radio));
  transition: clip-path var(--duracion-larga) var(--curva-vitrina);
  transition-delay: calc(var(--orden, 0) * 120ms);
}
.con-movimiento [data-revelar="telon"].visible { clip-path: inset(0 0 0 0 round var(--radio)); }
.con-movimiento [data-revelar="telon"] img {
  transform: scale(1.15);
  transition: transform calc(var(--duracion-larga) * 1.4) var(--curva-seda);
  transition-delay: inherit;
}
.con-movimiento [data-revelar="telon"].visible img { transform: scale(1); }

/* Título línea por línea: movimiento.js envuelve cada línea en .linea > span */
.titulo-lineas .linea { display: block; overflow: hidden; padding-bottom: 0.08em; }
.con-movimiento .titulo-lineas .linea > span {
  display: inline-block;
  transform: translateY(110%);
  transition: transform var(--duracion-larga) var(--curva-seda);
  transition-delay: calc(var(--orden, 0) * 110ms);
}
.con-movimiento .titulo-lineas.visible .linea > span { transform: none; }

/* Accesibilidad: sin desplazamientos para quien pidió menos movimiento */
@media (prefers-reduced-motion: reduce) {
  .con-movimiento [data-revelar],
  .con-movimiento [data-revelar="telon"],
  .con-movimiento .titulo-lineas .linea > span {
    transform: none !important;
    clip-path: none !important;
    transition: opacity 150ms linear !important;
    transition-delay: 0s !important;
  }
}
```

### `frontend/assets/js/movimiento.js`
Se carga **al final** de cada página, después del script de la página.

```js
/*
 * MOVIMIENTO
 * ----------
 * Revela con suavidad lo que tenga data-revelar cuando entra en pantalla.
 * Funciona también con lo que las páginas pintan después con innerHTML
 * (tarjetas, resultados de filtros), gracias al MutationObserver.
 */
(function () {
  const raizHTML = document.documentElement;
  raizHTML.classList.add("con-movimiento");

  const menosMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)");

  const observador = new IntersectionObserver(
    (entradas) => {
      entradas.forEach((entrada) => {
        if (!entrada.isIntersecting) return;
        entrada.target.classList.add("visible");
        observador.unobserve(entrada.target); // cada cosa se anima una sola vez
      });
    },
    { rootMargin: "0px 0px -10% 0px", threshold: 0.12 }
  );

  // Las cuadrículas marcadas con data-cascada revelan a sus hijos en orden
  function prepararCascada(contenedor) {
    [...contenedor.children].forEach((hijo, i) => {
      if (!hijo.hasAttribute("data-revelar")) hijo.setAttribute("data-revelar", "");
      hijo.style.setProperty("--orden", Math.min(i, 8)); // máximo 8 escalones
    });
  }

  // Parte un título en líneas (cada <br> o el texto entero = una línea)
  function prepararTitulo(titulo) {
    if (titulo.dataset.listo) return;
    titulo.dataset.listo = "1";
    const partes = titulo.innerHTML.split(/<br\s*\/?>/i);
    titulo.innerHTML = partes
      .map((p, i) => `<span class="linea"><span style="--orden:${i}">${p.trim()}</span></span>`)
      .join("");
    observador.observe(titulo);
  }

  function activar(raiz = document) {
    raiz.querySelectorAll("[data-cascada]").forEach(prepararCascada);
    raiz.querySelectorAll(".titulo-lineas").forEach(prepararTitulo);
    raiz.querySelectorAll("[data-revelar]:not(.visible)").forEach((el) => observador.observe(el));
  }

  // Vigila lo que se agrega después (tarjetas pintadas por JS)
  new MutationObserver((cambios) => {
    cambios.forEach((c) => c.addedNodes.forEach((n) => {
      if (n.nodeType !== 1) return;
      const contenedor = n.parentElement?.closest("[data-cascada]");
      if (contenedor) prepararCascada(contenedor);
      activar(n.parentElement || document);
    }));
  }).observe(document.body, { childList: true, subtree: true });

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", () => activar());
  } else {
    activar();
  }

  // Disponible para las otras partes del proyecto
  window.StyleMeMovimiento = { activar, menosMovimiento };
})();
```

## 2. Cómo usarlo en el HTML
- Bloque que aparece: `<section data-revelar>`
- Foto con telón: `<div class="collage-foto" data-revelar="telon" style="--orden:1">`
- Cuadrícula en cascada: `<div class="cuadricula" id="tiendas-ropa" data-cascada>`
- Título por líneas: `<h1 class="titulo-lineas">VAMOS DE<br>COMPRAS!</h1>`

**Elementos que se re-pintan con filtros** (`#lista-tiendas`, `#lista-productos`): al re-pintar, los hijos nuevos vuelven a entrar en cascada automáticamente. Si el filtro debe sentirse instantáneo, usa la receta de `styleme-transiciones` (View Transition del mismo documento) en lugar de la cascada.

## 3. Animaciones ligadas al scroll (mejora progresiva)
Para efectos que siguen al dedo (barra de progreso de lectura, foto del banner que se desplaza unos px), usa **CSS scroll-driven animations**, que corren en el hilo del compositor:

```css
@supports (animation-timeline: view()) {
  @media (prefers-reduced-motion: no-preference) {
    .promo-imagenes img {
      animation: deriva-suave linear both;
      animation-timeline: view();
      animation-range: entry 0% exit 100%;
    }
    @keyframes deriva-suave { from { transform: scale(1.12) translateY(-4%); } to { transform: scale(1.12) translateY(4%); } }
  }
}
```
- Recorridos máximos ±6 %. Sin `@supports`, simplemente no pasa nada (está bien).
- **Nunca** reimplementes esto con `addEventListener("scroll")`.

## 4. Keyframes de la casa
Defínelos en `movimiento.css` y reutilízalos:
- `@keyframes latido` — escala 1 → 1.25 → 0.95 → 1 (corazones), `--curva-pulso`, 450ms.
- `@keyframes cinta` — `translateX(0) → translateX(-50%)` lineal, 40s, infinito (anuncio).
- `@keyframes brillo` — gradiente que cruza (placeholder de carga / escaneo del probador).
- `@keyframes caer-pin` — `translateY(-20px) opacity 0 → 0` (marcadores del mapa).

## Reglas
- Distancias 16–32 px; entradas ≤ 900 ms; nada infinito salvo la cinta y los indicadores de carga.
- No animes lo que está arriba del pliegue en páginas de herramienta (probador, recomendador) más de 400 ms: la persona vino a usar algo.
- Siempre bloque `prefers-reduced-motion`.
- Revisa con `styleme-revision-visual` al terminar.
