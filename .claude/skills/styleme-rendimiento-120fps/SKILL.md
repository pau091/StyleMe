---
name: styleme-rendimiento-120fps
description: Rendimiento y fluidez de StyleMe a 120 fps / 144 Hz - presupuesto por cuadro, propiedades que se pueden animar, requestAnimationFrame basado en tiempo, IntersectionObserver, imágenes sin saltos (CLS), will-change, content-visibility, Leaflet y MediaPipe sin trabas. Usar siempre que se escriba JS que mueva cosas, se agreguen imágenes, efectos de scroll o cursor, o cuando algo se sienta lento o entrecortado.
---

# StyleMe · Rendimiento a 120 / 144 fps

## Antes de empezar
- Contexto: `planeacion/11-contexto-visual.md` §5. Sitio estático, sin build, $0.
- Español sencillo, sin git.

## Presupuesto
| Pantalla | ms por cuadro |
|---|---|
| 60 Hz | 16,7 |
| 120 Hz | 8,3 |
| 144 Hz | 6,9 |

Todo el trabajo de JS + estilos + pintado de un cuadro debe caber ahí. La forma de lograrlo: **que el navegador anime en el compositor (GPU) y el JS no haga casi nada por cuadro.**

## 1. Qué se puede animar
- ✅ `transform`, `opacity` → compositor, fluido a cualquier Hz.
- ⚠️ `clip-path`, `filter` → pueden ir por el hilo principal; úsalos en pocos elementos (fotos de portada), por poco tiempo, y nunca en listas largas.
- ❌ `width`, `height`, `top/left`, `margin`, `padding`, `border-width`, `box-shadow`, `background-position` en elementos repetidos, `font-size`. Causan *layout* o *repaint* cada cuadro.
- Sombra animada → pseudo-elemento con la sombra fija y animar su `opacity` (ver `styleme-microinteracciones`).
- `backdrop-filter: blur()` → solo en el encabezado sticky si se quiere efecto vidrio, con radio ≤ 12px. Nunca en tarjetas.

## 2. JavaScript que se mueve: basado en tiempo
```js
// ✅ Igual de rápido a 60, 120 o 144 Hz
function animarNumero(elemento, desde, hasta, duracion = 400) {
  const inicio = performance.now();
  const suave = (t) => 1 - Math.pow(1 - t, 3); // curva de salida
  function cuadro(ahora) {
    const t = Math.min((ahora - inicio) / duracion, 1);
    elemento.textContent = Math.round(desde + (hasta - desde) * suave(t));
    if (t < 1) requestAnimationFrame(cuadro);
  }
  requestAnimationFrame(cuadro);
}
// ❌ NUNCA: x += 2 en cada cuadro (a 144 Hz va 2,4 veces más rápido), ni setInterval(…, 16)
```
- Para seguir el cursor con suavidad usa interpolación dependiente del tiempo: `actual += (meta - actual) * (1 - Math.exp(-dt / 80))`, con `dt` en ms.
- Agrupa lecturas (`getBoundingClientRect`) y escrituras (`style.transform`) por separado; nunca leer-escribir-leer en un bucle (layout thrashing).
- Una sola escritura por cuadro: guarda el último evento y aplícalo en `requestAnimationFrame` (patrón "pendiente", ver botón magnético).
- Escribe en variables CSS (`el.style.setProperty("--rx", …)`) o en `transform`; no cambies clases que alteren el layout en cada cuadro.

## 3. Eventos
- `scroll`, `touchmove`, `pointermove`, `wheel` → `{ passive: true }`.
- Saber si algo está visible → `IntersectionObserver`, nunca calcular posiciones en `scroll`.
- Efectos ligados al scroll → CSS `animation-timeline` (ver `styleme-animaciones` §3).
- **No** "smooth scroll" por JS (Lenis, etc.): el scroll nativo ya va a 120/144 Hz y es accesible. Solo `html { scroll-behavior: smooth; }` dentro de `prefers-reduced-motion: no-preference`.

## 4. Imágenes (la mayor causa de tirones)
- Siempre reservar espacio: `aspect-ratio` en el contenedor (ya existe en `.tarjeta-imagen` y `.perfil-foto`) o `width`/`height` en la etiqueta.
- `loading="lazy"` + `decoding="async"` en todas, **excepto** la foto grande de la portada: `fetchpriority="high"` y sin lazy.
- Peso: fotos ≤ 200 KB, en `.webp` cuando se pueda, ~2× el tamaño en que se muestran (no 4000 px para un cuadro de 300 px).
- Antes de un telón o un zoom, que la imagen esté decodificada: `await img.decode()` antes de agregar `.visible` si se nota un parpadeo.

## 5. Capas y `will-change`
- `will-change: transform` solo en lo que se va a animar **ya** (agrégalo al empezar, quítalo al terminar) o en 1–3 elementos permanentes (cinta del anuncio, botón magnético).
- Muchas capas = mucha memoria de GPU = tirones en celulares baratos.

## 6. Contenido largo
- Secciones bajo el pliegue de páginas largas: `content-visibility: auto; contain-intrinsic-size: auto 600px;`. No en secciones con `view-transition-name` ni con animaciones de entrada que deban medirse.
- Cuadrículas: `contain: layout paint` en `.tarjeta`.

## 7. Partes pesadas del proyecto
- **Leaflet (mapa, tienda, salida):** no animes el contenedor del mapa con `transform` mientras se arrastra. Marcadores animados con CSS en su `divIcon`, no recreándolos. `map.flyTo()` con `duration` ≤ 1.2 s. Llama `map.invalidateSize()` después de que termine una transición que cambie su tamaño.
- **MediaPipe (probador):** el modelo se carga solo cuando la persona sube una foto o entra al probador, no en el inicio. Mientras procesa, solo animaciones CSS (el barrido de escaneo); no ejecutes otros bucles de JS. Dibuja en el `canvas` una vez por resultado, no en un bucle continuo.
- **Fuentes:** ya usan `display=swap`. No agregues más pesos de los que se usan.

## 8. Cómo medir
1. Chrome DevTools → **Performance** → grabar haciendo scroll y hover. Buscar cuadros rojos y tareas largas (> 50 ms).
2. **Rendering** → activar "Frame Rendering Stats" (FPS) y "Paint flashing" (lo verde que parpadea en scroll = repintado que sobra).
3. **CPU throttling 4×** para simular un celular normal: debe seguir sin tirones.
4. Lighthouse móvil: CLS < 0.1, LCP < 2.5 s.
5. Probar en un monitor de 120/144 Hz si hay uno disponible: las animaciones deben durar lo mismo que a 60 Hz.

## Checklist
- [ ] Solo `transform`/`opacity` en lo repetido.
- [ ] Todo bucle JS usa `performance.now()` / el `timestamp` de rAF.
- [ ] Listeners pasivos; nada de lógica en `scroll`.
- [ ] Imágenes con espacio reservado y lazy.
- [ ] Sin tareas largas al cargar en DevTools.
