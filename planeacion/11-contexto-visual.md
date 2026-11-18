# 11 · Contexto del proyecto y dirección visual

> **Para qué sirve este documento:** reunir en un solo lugar *qué es StyleMe* y *cómo debe verse y moverse*. Es la fuente que leen las skills de `.claude/skills/styleme-*` antes de tocar el diseño. Si algo cambia aquí, cambia en todas.
>
> Convención: ✅ confirmado por la autora · 💡 sugerencia (dirección visual propuesta) · ⏳ pendiente.

---

## 1. El proyecto en una página

**StyleMe** ✅ es una página web para **descubrir tiendas físicas de moda y belleza de Bucaramanga** (Santander, Colombia). No es una tienda virtual: no se compra nada en la página; la persona encuentra la tienda, le escribe por **WhatsApp** o pide **cómo llegar** (D-002, D-015).

- **Pregunta que responde:** *"¿Y ahora qué hacemos?"* → arma una **salida de compras** por tiendas cercanas (D-027).
- **Tres pilares:** descubrimiento · inteligencia artificial · comunidad (01-vision).
- **Público:** todas las edades (D-017). El probador virtual, solo mayores de 18.
- **Condiciones:** proyecto académico, una sola autora, programadora junior, **costo $0**, **HTML + CSS + JavaScript sin frameworks ni build** (D-013, D-020, D-021).
- **Datos:** 10 tiendas reales (5 de ropa, 5 de maquillaje) y productos de ejemplo, en `data/tiendas.js` y `data/productos.js` (D-022, D-025).
- **Principios que el diseño no puede romper:** la IA no diagnostica ni promete tallas; no se inventan precios; privacidad (la foto del probador no sale del celular); experiencia **intuitiva y accesible**; **no agregar cosas solo para que se vea más grande** (01-vision, principio 7).

### Páginas

| Página | Archivo | Qué hace | Momento visual estrella 💡 |
|---|---|---|---|
| Inicio | `index.html` + `inicio.js` | Portada con buscador, collage, ventajas, categorías, banner de salida, tiendas, herramientas | Portada que "abre el telón": título por líneas + collage que se revela como vitrina |
| Tiendas | `tiendas.html` + `tiendas.js` | Lista de las 10 tiendas con filtro | Tarjetas que entran en cascada y se reordenan con suavidad al filtrar |
| Perfil de tienda | `tienda.html` + `tienda.js` | Datos, mini mapa, productos, WhatsApp / Cómo llegar / Instagram | La foto de la tarjeta **viaja** hasta el perfil (View Transition) |
| Productos | `productos.html` + `productos.js` | Catálogo con buscador y filtros | Resultados que se reacomodan sin saltos; contador que cuenta |
| Mapa | `mapa.html` + `mapa.js` | Todas las tiendas en Leaflet | Marcadores que "caen" uno por uno; pin que late al pasar el mouse |
| Salida de compras | `salida.html` + `salida.js` | Recorrido de 2–5 tiendas cercanas | La ruta se **dibuja** sobre el mapa parada por parada |
| Probador | `probador.html` + `probador.js` | IA en el navegador (MediaPipe) pone la prenda sobre tu foto | Barrido de "escaneo" mientras la IA trabaja; la prenda aparece con un fundido |
| Recomendador | `recomendador.html` + `recomendador.js` | Cuestionario → rutina por pasos | Pasos de la rutina que aparecen como una lista numerada que se va llenando |
| Acerca de | `acerca.html` | Qué es, privacidad, aviso, contacto | Lectura tranquila: solo revelado suave de secciones |

Todas las páginas comparten: encabezado y pie pintados por `assets/js/comun.js`, la hoja `assets/css/estilos.css` y las funciones `tarjetaTienda()`, `tarjetaProducto()` e `icono()`.

---

## 2. Concepto visual: **"Vitrina de Bucaramanga"** 💡

StyleMe debe sentirse como **caminar por una calle de boutiques en Cabecera un sábado por la tarde**: cálido, elegante, local y sin afán. Una revista de moda editorial, pero cercana.

Tres palabras que guían toda decisión:

1. **Seda** — todo se mueve suave, continuo, con desaceleración larga. Nada rebota exagerado, nada aparece de golpe.
2. **Vitrina** — las fotos se *revelan* como cuando se corre una cortina o se enciende la luz de un escaparate. Las tarjetas son vitrinas: al acercarte, la foto se acerca a ti.
3. **Cercanía** — tono cálido, crema y rosa empolvado, letras de revista; el movimiento acompaña, no presume.

**Lo que NO es StyleMe:** neón, glitch, cyberpunk, rebotes de caricatura, parallax mareador, cursores gigantes que tapan el contenido, scroll secuestrado ("smooth scroll" falso), pantallas de carga largas.

---

## 3. Sistema visual actual (lo que ya existe en `estilos.css`)

### Colores (`:root`)

| Token | Valor | Uso |
|---|---|---|
| `--color-marca` | `#a35d6d` | Rosa empolvado: botones, acentos, líneas |
| `--color-marca-oscuro` | `#7d4253` | Hover, enlaces |
| `--color-marca-suave` | `#f6e9ea` | Fondos suaves, círculos |
| `--color-ciruela` | `#4f2c3a` | Pie de página, paneles oscuros |
| `--color-beige` | `#efe3d6` | Fondo de imágenes |
| `--color-texto` / `--color-texto-suave` | `#2f2427` / `#6e5d61` | Texto |
| `--color-fondo` / `--color-superficie` | `#fcf8f5` / `#fff` | Crema general / tarjetas |
| `--color-borde` | `#eadcd6` | Bordes |
| `--color-exito` | `#3f7a5a` | WhatsApp |

### Tipografía
- **Títulos:** Cormorant Garamond 600/700 — serif de revista. Los títulos de sección van en MAYÚSCULAS con `letter-spacing` amplio.
- **Texto:** Jost 400/500/600 — limpia, geométrica.
- **Etiquetas pequeñas:** Jost 600, mayúsculas, `letter-spacing: 0.08–0.14em`.

### Forma
- Radios: `--radio: 10px`, `--radio-grande: 18px`; botones `6px`.
- Sombra única y suave: `--sombra: 0 4px 18px rgba(79,44,58,.07)`.
- Ancho máximo `1180px`, gutter `16px`. **Mobile first**; cortes en `720px` y `1080px`.
- Íconos de línea (SVG, `stroke-width 1.5`), sin emojis en la interfaz.

### Movimiento actual
Casi nulo: `transition: 0.2s` en botones, tarjetas (`translateY(-2px)`) y círculos de categoría. **Aquí está la oportunidad.**

---

## 4. Sistema de movimiento propuesto 💡

### 4.1 Tokens (van en `:root`, archivo `assets/css/movimiento.css`)

```css
--curva-seda:     cubic-bezier(0.22, 1, 0.36, 1);   /* salida larga y suave: entradas, reveals */
--curva-vitrina:  cubic-bezier(0.65, 0, 0.35, 1);   /* entra y sale suave: cortinas, cambios de estado */
--curva-pulso:    cubic-bezier(0.34, 1.56, 0.64, 1);/* rebote mínimo: SOLO corazones y confirmaciones */
--duracion-micro: 160ms;   /* hover, foco, presionar */
--duracion-corta: 280ms;   /* menús, pestañas, filtros */
--duracion-media: 520ms;   /* tarjetas que entran */
--duracion-larga: 900ms;   /* portada, revelado de fotos */
--escalon:        70ms;    /* retraso entre elementos de una lista (stagger) */
```

### 4.2 Reglas de movimiento
1. **Se anima solo `transform`, `opacity`** (y `clip-path` con moderación). Nunca `width`, `height`, `top`, `left`, `margin`, `box-shadow` directamente. → así se mantiene a **120 / 144 fps**.
2. **Entrar despacio, salir rápido:** las salidas duran ~60 % de las entradas.
3. **Distancias cortas:** las entradas viajan 16–32 px, no 100 px.
4. **Cascada (stagger) máxima de ~8 elementos**; del 9.º en adelante entran juntos.
5. **Cada cosa se anima una vez** al entrar en pantalla, no cada vez que se hace scroll.
6. **`prefers-reduced-motion: reduce`** → sin desplazamientos ni parallax; se conservan fundidos cortos (≤150 ms). Obligatorio.
7. **El contenido nunca depende de la animación:** si el JS falla, todo se ve (las clases de "oculto" solo se aplican cuando `movimiento.js` cargó).

### 4.3 Catálogo de efectos firma

| Efecto | Dónde | Cómo se siente |
|---|---|---|
| **Telón** (revelado con `clip-path: inset()`) | Fotos del collage, banner, perfil | La foto se descubre de abajo hacia arriba, como subir una persiana |
| **Línea por línea** | `h1` de cada página | Cada línea sube desde una máscara invisible |
| **Cascada de vitrinas** | Cuadrículas de tiendas y productos | Las tarjetas suben 24 px y aparecen una tras otra |
| **Acercamiento de vitrina** | Hover de tarjetas | La foto hace `scale(1.06)` dentro de su marco; la tarjeta sube 4 px y aparece una sombra (por opacidad de un pseudo-elemento) |
| **Cinta de anuncio** | Barra `.anuncio` | Texto que corre lento en bucle (marquee CSS), se pausa al pasar el mouse |
| **Foto viajera** | Tarjeta de tienda → perfil | View Transition entre páginas: la foto se expande hasta su lugar en el perfil |
| **Ruta dibujada** | Salida de compras | Los números aparecen en orden y la línea se traza entre ellos |
| **Escaneo** | Probador | Una franja de luz rosa recorre la foto mientras MediaPipe trabaja |
| **Latido** | Corazones (futuras reseñas/favoritos, D-006) | Escala 1 → 1.25 → 1 con `--curva-pulso` |
| **Botón magnético** (solo mouse) | CTA principales | El botón se inclina unos px hacia el cursor |

---

## 5. Rendimiento: 120 fps / 144 Hz 💡

- Presupuesto por cuadro: **8,3 ms a 120 Hz** y **6,9 ms a 144 Hz**. Las animaciones de CSS (`transition`, `@keyframes`, View Transitions, scroll-driven) se adaptan solas a la frecuencia de la pantalla.
- En JavaScript, todo lo que se mueva usa `requestAnimationFrame` y **el tiempo real** (`ahora - antes`), nunca "sumar 1 px por cuadro" (a 144 Hz iría 2,4 veces más rápido que a 60 Hz).
- `IntersectionObserver` en lugar de escuchar `scroll`. Escuchas de `scroll`/`pointermove`/`touchmove` con `{ passive: true }`.
- Imágenes con `width`/`height` (o `aspect-ratio`) para no causar saltos (CLS), `loading="lazy"` y `decoding="async"` excepto la foto principal de la portada (`fetchpriority="high"`).
- `will-change` solo durante la animación, nunca en decenas de elementos.
- Nada de bibliotecas pesadas de animación: CSS + JS propio pequeño. ($0 y entendible para una junior.)

---

## 6. Accesibilidad que el movimiento debe respetar ✅ (principio 5)

- `prefers-reduced-motion` en todos los efectos.
- Nada parpadea más de 3 veces por segundo.
- El foco del teclado (`:focus-visible`) siempre visible, también durante animaciones.
- Efectos de cursor (magnético, inclinación) **solo** con `@media (hover: hover) and (pointer: fine)`.
- Contraste de texto AA sobre todos los fondos (incluido el texto sobre fotos).
- Botones de al menos 44–48 px de alto (ya existe).

---

## 7. Archivos que se agregan para el movimiento 💡

```
assets/
├── css/
│   ├── estilos.css       (existente: colores, layout, componentes)
│   └── movimiento.css    (NUEVO: tokens de movimiento, keyframes, reveals, view transitions, reduced motion)
└── js/
    ├── comun.js          (existente)
    └── movimiento.js     (NUEVO: observador de revelado, cascada, magnético, utilidades)
```

Orden de carga en cada página: `estilos.css` → `movimiento.css` en el `<head>`; `comun.js` → script de la página → `movimiento.js` al final.

---

## 8. Cómo trabajar con las skills

| Skill | Cuándo usarla |
|---|---|
| `styleme-identidad-visual` | Antes de cualquier cambio visual: colores, tipografía, espaciado, tono |
| `styleme-animaciones` | Entradas al hacer scroll, cascadas, portada, keyframes |
| `styleme-transiciones` | Cambio entre páginas (View Transitions), menú, pestañas, filtros |
| `styleme-microinteracciones` | Hover, presionar, botones, tarjetas, corazones, cursor |
| `styleme-rendimiento-120fps` | Cualquier animación con JS, imágenes, scroll, mapas, probador |
| `styleme-efectos-por-pagina` | Recetas concretas del "momento estrella" de cada página |
| `styleme-revision-visual` | Al terminar: checklist de calidad, accesibilidad y fluidez |

**Orden recomendado para pulir una página:** identidad → animaciones → microinteracciones → efecto de la página → transiciones → rendimiento → revisión.
