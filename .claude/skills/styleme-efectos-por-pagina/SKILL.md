---
name: styleme-efectos-por-pagina
description: Recetas del "momento estrella" de cada página de StyleMe - portada que abre el telón (index), cascada y filtros (tiendas, productos), foto viajera (tienda), pines que caen (mapa), ruta que se dibuja (salida), escaneo de IA (probador), rutina que se llena (recomendador). Usar al mejorar visualmente una página concreta de StyleMe.
---

# StyleMe · Efectos firma por página

## Antes de empezar
- Lee `planeacion/11-contexto-visual.md` (tabla de páginas en §1 y catálogo de efectos en §4.3).
- Asegúrate de que existan `frontend/assets/css/movimiento.css` y `frontend/assets/js/movimiento.js` (skill `styleme-animaciones`) y que estén enlazados en la página.
- Lee el HTML **y** el JS de la página antes de tocarla: casi todo se pinta con `innerHTML` desde `frontend/assets/js/<pagina>.js`.
- **Un solo momento estrella por página.** El resto: revelados suaves y microinteracciones. Principio 7: no agregar cosas solo para que se vea más grande.
- Español sencillo, comentarios con ✏️ donde la autora pueda cambiar algo, sin git.

## Inicio (`index.html`, `inicio.js`) — "Se abre la vitrina"
1. `h1` → `class="titulo-lineas"` con un `<br>`: `VAMOS DE<br><em>compras!</em>` (itálica de Cormorant en la segunda línea).
2. Etiqueta, párrafo, buscador y botones: `data-revelar` con `--orden` 2, 3, 4, 5 (aparecen después del título).
3. Collage: en `inicio.js` cada `.collage-foto` lleva `data-revelar="telon" style="--orden:${i}"`. La primera foto con `fetchpriority="high"`.
4. Fondo de la portada: dos manchas difusas (`radial-gradient` de `--color-marca-suave` y `--color-beige`) en un `::before` que se desplaza muy lento con `@keyframes` (30 s, `transform` solamente). Con reduced motion, quietas.
5. Botón "Planear salida de compras": `data-magnetico`.
6. Ventajas: `data-cascada`; el ícono se dibuja (`stroke-dasharray`/`dashoffset` de 60 → 0, 700 ms) al ser visible.
7. Categorías: `data-cascada` + microinteracción de círculo.
8. Banner promo: foto con deriva scroll-driven (`styleme-animaciones` §3) y texto con `data-revelar`.
9. Cuadrículas de tiendas y herramientas: `data-cascada`.

## Tiendas (`tiendas.html`, `tiendas.js`) — "Vitrinas en fila"
- `#lista-tiendas` con `data-cascada`.
- Al filtrar/buscar: `pintarConTransicion()` (skill `styleme-transiciones` §2) y cada tarjeta con `view-transition-name: foto-<id>` → las que quedan se deslizan.
- Estado vacío (`.vacio`): aparece con fundido + ícono de búsqueda que se balancea una vez.

## Perfil de tienda (`tienda.html`, `tienda.js`) — "La foto que viaja"
- Foto viajera tarjeta → perfil (skill `styleme-transiciones` §1).
- Botones WhatsApp / Cómo llegar / Instagram: entran en cascada después de la foto; WhatsApp con un brillo que lo cruza una sola vez (pseudo-elemento con gradiente, `translateX(-100% → 100%)`, 900 ms, retraso 600 ms).
- Mini mapa: el pin cae (`caer-pin`) cuando el mapa termina de cargar (`mapa.whenReady`).
- Productos de la tienda: `data-cascada`.

## Productos (`productos.html`, `productos.js`) — "El catálogo vivo"
- Filtros con transición (`pintarConTransicion`).
- `#contador`: número animado con `animarNumero()` (skill `styleme-rendimiento-120fps` §2).
- Sello "Ejemplo": aparece con `scale(0.8 → 1)` un poco después de la tarjeta.
- Botón "Probar virtualmente": ícono de gancho que se mece al hover (`rotate(-10deg → 10deg)` una vez).

## Mapa (`mapa.html`, `mapa.js`) — "Las tiendas aparecen en la ciudad"
- Marcadores como `L.divIcon` con clase propia; cada uno con `animation: caer-pin 500ms var(--curva-seda) both; animation-delay: calc(var(--i) * 60ms)`.
- Hover/toque en el marcador: anillo que late (`::after` con `scale(1 → 1.8)` + `opacity 0.5 → 0`, 1.2 s, 2 veces).
- Popup de Leaflet: fundido + subir 6 px (sobrescribe `.leaflet-popup` con `transition` de `opacity`/`transform`).
- Al filtrar por tipo, los marcadores que se van se desvanecen antes de quitarlos (escucha `animationend`).

## Salida de compras (`salida.html`, `salida.js`) — "La ruta se dibuja"
- Al generar el recorrido:
  1. Los marcadores numerados (`.marcador-numero`) aparecen en orden (retraso de 350 ms entre cada uno).
  2. La línea entre paradas se dibuja: en el `L.polyline`, usa `className: "ruta-linea"` y en CSS `stroke-dasharray: var(--largo); stroke-dashoffset: var(--largo)` → `0`. Calcula `--largo` con `path.getTotalLength()` del elemento SVG del polyline.
  3. La lista `.ruta li` entra en cascada sincronizada con los marcadores.
  4. `map.flyToBounds(..., { duration: 1 })` antes de empezar a dibujar.
- Reduced motion: todo aparece de una vez, sin trazo.

## Probador (`probador.html`, `probador.js`) — "La IA te mira (y no guarda nada)"
- Mientras MediaPipe carga o procesa: sobre `.lienzo-contenedor`, una franja de luz rosa que baja y sube (`@keyframes escanear`, `transform: translateY`, 1.6 s, alterno) + `.estado` con puntos animados. Solo CSS.
- Al detectar la pose: 4 puntos pequeños (hombros y caderas) aparecen con `scale(0 → 1)` en su posición y luego se desvanecen; la prenda aparece con fundido de 400 ms.
- Al mover los deslizadores: la prenda responde en tiempo real (redibujo en `requestAnimationFrame`, uno por cambio).
- Aviso de privacidad con ícono de candado que se "cierra" una vez (traslación del arco 2 px).
- Recuerda: nada de textos que prometan talla o resultado exacto.

## Recomendador (`recomendador.html`, `recomendador.js`) — "Tu rutina, paso a paso"
- Pestañas skincare/capilar con indicador deslizante (`styleme-transiciones` §4).
- Cada `fieldset` del cuestionario: `data-revelar` en cascada.
- Al generar la rutina: cada paso (`.recomendacion`) entra con 120 ms entre uno y otro, el número del paso hace `latido` suave; la sección hace scroll suave hasta el resultado (`scrollIntoView({ behavior: "smooth" })` solo sin reduced motion) y el foco va al título del resultado.
- Aviso de dermatólogo: fondo `--color-aviso-fondo`, entra con fundido, **sin** efectos llamativos (es un tema serio).

## Acerca de (`acerca.html`) — "Lectura tranquila"
- Solo `data-revelar` en cada `section` y `titulo-lineas` en el `h1`. Nada más.

## Encabezado y pie (en `comun.js`, todas las páginas)
- Encabezado sticky: al bajar más de 80 px, se compacta (`.barra` reduce altura con `transform: scale` del logo, no cambiando `min-height`) y gana fondo semitransparente con `backdrop-filter: blur(10px)`. Detecta con un `IntersectionObserver` sobre un elemento centinela de 1 px al inicio del `main`.
- Logo: la palabra "Me" hace un brillo sutil al cargar la página (una vez).
- Pie: columnas con `data-cascada`.

## Al terminar una página
Pasa la skill `styleme-revision-visual`.
