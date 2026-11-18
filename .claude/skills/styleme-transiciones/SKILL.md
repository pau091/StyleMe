---
name: styleme-transiciones
description: Transiciones de StyleMe - View Transitions entre páginas (foto de tienda que viaja al perfil), cambios dentro de la página (filtros, pestañas, menú móvil, acordeones) y estados de carga. Usar al tocar la navegación entre páginas, los filtros de tiendas/productos/mapa, las pestañas del recomendador o el menú del encabezado.
---

# StyleMe · Transiciones

## Antes de empezar
- Lee `planeacion/11-contexto-visual.md` §2 y §4 y la skill `styleme-animaciones` (tokens de `movimiento.css`).
- StyleMe es multipágina (MPA, HTML estático). **No** conviertas el sitio en SPA ni agregues router.
- Español sencillo, sin git.

## 1. Entre páginas: View Transitions de documento cruzado
Funciona en Chrome/Edge 126+ y Safari 18.2+; en los demás navegadores la página cambia normal (mejora progresiva, cero riesgo).

En `movimiento.css`:
```css
/* Cambio de página suave entre todas las páginas de StyleMe */
@view-transition { navigation: auto; }

/* Encabezado y pie se quedan quietos: se ven "fijos" mientras cambia el contenido */
#encabezado { view-transition-name: encabezado; }
#pie { view-transition-name: pie; }

/* El contenido sale rápido y entra con seda */
::view-transition-old(root) { animation: salir-pagina 180ms var(--curva-vitrina) both; }
::view-transition-new(root) { animation: entrar-pagina 420ms var(--curva-seda) both; }
@keyframes salir-pagina { to { opacity: 0; transform: translateY(-8px); } }
@keyframes entrar-pagina { from { opacity: 0; transform: translateY(14px); } }

@media (prefers-reduced-motion: reduce) {
  ::view-transition-group(*), ::view-transition-old(*), ::view-transition-new(*) {
    animation-duration: 0.01ms !important;
  }
}
```

### La foto viajera (tarjeta → perfil de tienda)
El efecto firma: la foto de la tarjeta crece hasta la foto del perfil.

1. En `tarjetaTienda()` de `comun.js`, dale a la imagen un nombre único:
   ```js
   <div class="tarjeta-imagen" style="view-transition-name: foto-${tienda.id}">
   ```
   (Los `id` de tienda solo tienen letras y guiones, sirven como nombre.)
2. En `tienda.js`, al pintar el perfil: `<div class="perfil-foto" style="view-transition-name: foto-${tienda.id}">`.
3. Haz lo mismo con el nombre: `view-transition-name: nombre-${tienda.id}` en el `h3` de la tarjeta y el `h1` del perfil.
4. El perfil se pinta con JS: para que la transición espere a que exista, agrega en el `<head>` de `tienda.html`:
   ```html
   <link rel="expect" href="#perfil-listo" blocking="render">
   ```
   y en `tienda.js` pon `id="perfil-listo"` en el último elemento que pinta. Si no aparece en ~unos segundos el navegador sigue igual (no se cuelga).
5. Estilo del viaje: agrega también `view-transition-class: foto-tienda` a los dos elementos (tarjeta y perfil) y en CSS:
   ```css
   .tarjeta-imagen, .perfil-foto { view-transition-class: foto-tienda; }
   ::view-transition-group(*.foto-tienda) { animation-duration: 560ms; animation-timing-function: var(--curva-seda); }
   ```
   Así una sola regla sirve para las 10 tiendas (sin escribir una por cada `foto-bohor`, `foto-queen`...).

**Regla:** un `view-transition-name` no puede repetirse en la misma página. Si una tienda aparece dos veces (por ejemplo, en dos listas), solo la primera lleva nombre.

## 2. Dentro de la página: filtros y resultados
Al filtrar tiendas/productos, envuelve el re-pintado:
```js
function pintarConTransicion(pintar) {
  if (!document.startViewTransition || StyleMeMovimiento.menosMovimiento.matches) return pintar();
  document.startViewTransition(pintar);
}
// Uso en productos.js:  pintarConTransicion(() => { lista.innerHTML = ...; });
```
Si cada tarjeta lleva `view-transition-name` único, las que quedan **se deslizan** a su nuevo sitio y las nuevas aparecen. Ponle esta función en `comun.js` para reutilizarla.

Contador de resultados ("12 productos"): anima el número con `requestAnimationFrame` basado en tiempo (ver `styleme-rendimiento-120fps`), 400 ms.

## 3. Menú móvil
Hoy `.menu` cambia entre `display:none` y `flex` (salto brusco). Cambiar a:
```css
@media (max-width: 1079px) {
  .menu { display: flex; visibility: hidden; opacity: 0; transform: translateY(-8px);
          transition: opacity var(--duracion-corta) var(--curva-seda), transform var(--duracion-corta) var(--curva-seda), visibility 0s linear var(--duracion-corta);
          position: absolute; inset: 100% 0 auto; background: var(--color-superficie); padding: 8px 16px 16px; box-shadow: var(--sombra); }
  .menu.abierto { visibility: visible; opacity: 1; transform: none; transition-delay: 0s; }
  .menu.abierto li { animation: entrar-item var(--duracion-media) var(--curva-seda) both; }
  .menu.abierto li:nth-child(n) { animation-delay: calc((var(--i, 0)) * 40ms); }
}
```
(Pon `style="--i:${n}"` a cada `li` en `pintarEncabezado()`.) `visibility` mantiene el menú fuera del orden de tabulación cuando está cerrado. Cierra con `Escape` y devuelve el foco al botón.

Línea bajo el enlace activo del menú de escritorio: pseudo-elemento con `transform: scaleX(0 → 1)` y `transform-origin: left`, no `border-bottom` animado.

## 4. Pestañas del recomendador (skincare / capilar)
- Indicador deslizante bajo la pestaña activa (un `span` con `transform: translateX()` y `scaleX()`).
- El contenido cambia con `startViewTransition` o con un fundido cruzado de 280 ms.
- Mantén `aria-selected` / `aria-pressed` correctos.

## 5. Cargas
- Nunca pantalla de carga para páginas estáticas.
- Para esperas reales (MediaPipe, teselas del mapa): esqueleto con `@keyframes brillo` y texto de estado en `.estado` (ya existe, con `aria-live`).

## Checklist
- [ ] En Firefox (sin View Transitions) todo funciona igual, solo sin efecto.
- [ ] Ningún `view-transition-name` repetido en la página (la consola lo avisa).
- [ ] Reduced motion = cambios instantáneos.
- [ ] El botón Atrás del navegador también se ve bien.
