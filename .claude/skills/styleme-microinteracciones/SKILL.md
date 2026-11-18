---
name: styleme-microinteracciones
description: Microinteracciones de StyleMe - hover y presión de botones, acercamiento de vitrina en tarjetas, botón magnético, corazones con latido, foco visible, cinta del anuncio, enlaces subrayados animados. Usar al pulir botones, tarjetas, enlaces, íconos, formularios o cualquier respuesta visual al mouse, al dedo o al teclado en StyleMe.
---

# StyleMe · Microinteracciones

## Antes de empezar
- Lee `styleme-identidad-visual` y los tokens de `styleme-animaciones`.
- Español sencillo, sin librerías, sin git.
- **Regla de oro:** respuesta en ≤ 160 ms (`--duracion-micro`). Lo que la persona toca debe reaccionar al instante.

## 1. Botones (`.boton`)
```css
.boton {
  position: relative; overflow: hidden; isolation: isolate;
  transition: transform var(--duracion-micro) var(--curva-seda), color var(--duracion-corta), border-color var(--duracion-corta);
}
/* Relleno que sube desde abajo en hover (en lugar de cambiar el fondo de golpe) */
.boton::before {
  content: ""; position: absolute; inset: 0; z-index: -1;
  background: var(--color-marca-oscuro);
  transform: scaleY(0); transform-origin: bottom;
  transition: transform var(--duracion-corta) var(--curva-seda);
}
@media (hover: hover) { .boton:hover::before { transform: scaleY(1); } }
.boton:active { transform: scale(0.97); }
```
Adapta el color del `::before` a cada variante (`.boton-secundario` → `--color-marca-suave`, `.boton-whatsapp` → `#2f5f45`, `.boton-claro` → `--color-marca-suave`) y quita el `background` de su `:hover` actual.

## 2. Tarjetas: "acercamiento de vitrina"
```css
.tarjeta { position: relative; transition: transform var(--duracion-corta) var(--curva-seda); }
/* Sombra por opacidad (barata) en vez de animar box-shadow (cara) */
.tarjeta::after {
  content: ""; position: absolute; inset: 0; border-radius: inherit; pointer-events: none;
  box-shadow: 0 14px 34px rgba(79, 44, 58, 0.14); opacity: 0;
  transition: opacity var(--duracion-corta) var(--curva-seda);
}
.tarjeta-imagen { overflow: hidden; }
.tarjeta-imagen img { transition: transform 700ms var(--curva-seda); }
@media (hover: hover) {
  .tarjeta:hover { transform: translateY(-4px); }
  .tarjeta:hover::after { opacity: 1; }
  .tarjeta:hover .tarjeta-imagen img { transform: scale(1.06); }
}
.tarjeta:focus-visible { transform: translateY(-4px); }
```
Los logos PNG (`object-fit: contain`) usan `scale(1.03)`.

### Inclinación 3D (opcional, solo escritorio, solo tarjetas de tienda)
Máximo 4°. Con `pointermove` pasivo, calcula y escribe en un `requestAnimationFrame` las variables `--rx`/`--ry`; el CSS hace `transform: perspective(800px) rotateX(var(--rx)) rotateY(var(--ry))`. Al salir, vuelve a 0 con transición. Solo dentro de `matchMedia("(hover: hover) and (pointer: fine)")` y sin reduced motion.

## 3. Botón magnético (CTA de portada y banner)
```js
// En movimiento.js. Solo mouse fino y sin reduced motion.
const punteroFino = matchMedia("(hover: hover) and (pointer: fine)");
document.querySelectorAll("[data-magnetico]").forEach((el) => {
  if (!punteroFino.matches || StyleMeMovimiento.menosMovimiento.matches) return;
  let pendiente = false, x = 0, y = 0;
  el.addEventListener("pointermove", (e) => {
    const r = el.getBoundingClientRect();
    x = (e.clientX - r.left - r.width / 2) * 0.25;
    y = (e.clientY - r.top - r.height / 2) * 0.35;
    if (pendiente) return;
    pendiente = true;
    requestAnimationFrame(() => { el.style.transform = `translate(${x}px, ${y}px)`; pendiente = false; });
  }, { passive: true });
  el.addEventListener("pointerleave", () => { el.style.transform = ""; });
});
```
CSS: `[data-magnetico] { transition: transform 400ms var(--curva-seda); }`. Desplazamiento máximo ~8 px. Solo 2–3 botones por página.

## 4. Enlaces ("Ver todas", pie de página)
Subrayado que crece: `background: linear-gradient(currentColor 0 0) 0 100% / 0 1px no-repeat; transition: background-size var(--duracion-corta) var(--curva-seda);` y en hover `background-size: 100% 1px`. La flecha de "Ver todas" se mueve 4 px con `transform`.

## 5. Categorías en círculo
Hover: el círculo sube 4 px, el ícono gira −8° y crece 1.08, y un anillo `::after` con `--color-marca` aparece con `scale(0.9 → 1)` + opacidad.

## 6. Corazones (calificación de 1 a 5, D-006 · futuro)
Al marcar: `animation: latido 450ms var(--curva-pulso)`, relleno de `--color-marca`, y 6 puntitos que salen en círculo (pseudo-elementos o `span` con `transform`) y se desvanecen en 400 ms. `aria-pressed` correcto.

## 7. Cinta del anuncio (`.anuncio`)
Duplica el texto dentro de un `div` y anímalo con `@keyframes cinta` (lineal, 40 s, infinito). `animation-play-state: paused` en hover y con reduced motion se queda quieto mostrando el texto centrado. `aria-hidden="true"` en la copia duplicada.

## 8. Formularios
- Buscador: al enfocar, el borde pasa a `--color-marca` y aparece un halo `box-shadow: 0 0 0 4px var(--color-marca-suave)` (transición de 160 ms; aquí `box-shadow` está bien porque es un solo elemento).
- Casillas del recomendador: la marca se "dibuja" (`stroke-dashoffset`) si se personalizan.
- Rango del probador: el pulgar crece 1.15 al arrastrar.

## 9. Foco del teclado
`:focus-visible` siempre visible. Nunca `outline: none` sin reemplazo. El foco no debe "temblar" durante animaciones (no animar `outline`).

## Checklist
- [ ] Todo lo de hover está dentro de `@media (hover: hover)` (en celular no se queda "pegado").
- [ ] `:active` da respuesta al toque en celular.
- [ ] Nada de animar `box-shadow`, `width`, `height` en elementos repetidos.
- [ ] Reduced motion respetado.
