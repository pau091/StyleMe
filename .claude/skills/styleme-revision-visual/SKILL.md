---
name: styleme-revision-visual
description: Revisión final de calidad visual, movimiento, accesibilidad y fluidez (120/144 fps) de StyleMe. Usar al terminar cualquier cambio visual o de animación en StyleMe, antes de que la autora haga commit, o cuando pida "revisa cómo quedó", "¿está fluido?", "¿está bien la animación?".
---

# StyleMe · Revisión visual

## Cómo hacer la revisión
1. Lee `planeacion/11-contexto-visual.md`.
2. Revisa los archivos que cambiaron (`git diff` **solo para leer**; nunca `git add`/`commit`/`push`).
3. Recorre la checklist. Para cada problema di: archivo y línea, qué pasa, cómo arreglarlo. Explica en español sencillo.
4. Si se puede abrir la página (Live Server o el navegador disponible), compruébalo en vivo a 360 px y a 1280 px de ancho.
5. Si se encontró algo que cambia una decisión del proyecto, propón anotarlo en `planeacion/04-decisiones.md` como P-xxx (sugerencia), no como confirmado.

## Checklist

### Identidad (skill `styleme-identidad-visual`)
- [ ] Solo tokens de `:root`; sin colores sueltos.
- [ ] Cormorant para títulos, Jost para texto; sin una tercera fuente.
- [ ] Encaja en "seda · vitrina · cercanía"; nada neón/tech/caricatura.
- [ ] Un solo momento estrella por página.

### Movimiento (skills `styleme-animaciones`, `styleme-transiciones`, `styleme-microinteracciones`)
- [ ] Usa las curvas y duraciones de `movimiento.css`, no números sueltos.
- [ ] Entradas ≤ 900 ms; microinteracciones ≤ 160 ms; distancias ≤ 32 px.
- [ ] Cascadas de máximo 8 escalones.
- [ ] Cada elemento se revela una sola vez.
- [ ] Si `movimiento.js` no carga, **todo el contenido se ve** (probar quitando el `<script>`).
- [ ] Hover dentro de `@media (hover: hover)`; efectos de cursor solo con `pointer: fine`.
- [ ] View Transitions: sin nombres repetidos; en Firefox la navegación sigue normal.

### Accesibilidad
- [ ] `prefers-reduced-motion: reduce` probado (DevTools → Rendering → "Emulate CSS prefers-reduced-motion"): sin desplazamientos, sin parallax, sin trazos; solo fundidos cortos.
- [ ] Nada parpadea más de 3 veces por segundo.
- [ ] Navegación con Tab: foco visible siempre, orden lógico, el menú cerrado no recibe foco.
- [ ] Contraste AA (también texto sobre fotos y sobre el encabezado translúcido).
- [ ] Zonas táctiles ≥ 44 px.
- [ ] Lo que cambia solo (resultados, estado del probador) se anuncia con `aria-live`.

### Fluidez 120/144 fps (skill `styleme-rendimiento-120fps`)
- [ ] Solo `transform`/`opacity` en elementos repetidos; `clip-path`/`filter` en pocos.
- [ ] Sin `addEventListener("scroll", …)` con lógica; listeners pasivos.
- [ ] Bucles JS basados en tiempo (`performance.now()`), sin `setInterval` para animar.
- [ ] `will-change` puntual.
- [ ] Imágenes con espacio reservado, lazy (menos la de portada), pesos razonables.
- [ ] DevTools Performance con CPU 4×: sin tareas largas ni cuadros perdidos en scroll y hover.
- [ ] Lighthouse móvil: CLS < 0.1.

### Funcionalidad intacta
- [ ] Buscador, filtros, mapa, salida, probador y recomendador siguen funcionando igual.
- [ ] Enlaces de WhatsApp / Cómo llegar / Instagram funcionan.
- [ ] Sin errores en la consola (F12), salvo la tabla de `revisarDatos()` que es intencional.
- [ ] Las 9 páginas cargan `movimiento.css` y `movimiento.js` en el orden correcto.

### Principios del proyecto
- [ ] Ningún texto nuevo promete talla exacta, diagnóstico ni precios inventados.
- [ ] Ningún efecto agregado "solo para que se vea más grande".

## Formato del informe
Breve, en español:
1. **Veredicto** en una línea (listo / casi listo / falta).
2. **Problemas** ordenados de más a menos grave, con `archivo:línea` y el arreglo.
3. **Lo que quedó bien** (1–3 puntos).
