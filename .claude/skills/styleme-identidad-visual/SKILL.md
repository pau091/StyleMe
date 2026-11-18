---
name: styleme-identidad-visual
description: Identidad visual de StyleMe (concepto "Vitrina de Bucaramanga"): paleta, tipografía, espaciado, forma, íconos y tono. Usar ANTES de cualquier cambio visual en las páginas de StyleMe (HTML/CSS/JS), al crear componentes nuevos o al decidir cómo debe verse algo.
---

# StyleMe · Identidad visual

## Antes de empezar
1. Lee `planeacion/11-contexto-visual.md` (secciones 1–3). Es la fuente de verdad.
2. Lee `assets/css/estilos.css` (los tokens están en `:root`).
3. Responde y comenta el código **en español sencillo**: la autora es programadora junior. Nombres de clases, variables y funciones en español (`tarjeta`, `pintarEncabezado`, `--color-marca`), como el resto del proyecto.
4. **No hagas commits ni uses git.** La autora hace todo el git.

## Concepto: "Vitrina de Bucaramanga"
Seda (movimiento suave y continuo) · Vitrina (las fotos se revelan como un escaparate) · Cercanía (cálido, local, de revista). Si una idea se ve "tech", "gamer" o ruidosa, no va.

## Reglas
### Color
- Usa **siempre** las variables de `:root`; nunca pegues un hex nuevo en un componente. Si hace falta un color, agrégalo como token con comentario.
- Proporción aproximada: 70 % crema/blanco, 20 % rosa empolvado y beige, 10 % ciruela (acentos oscuros, pie, paneles de contraste).
- Degradados permitidos: los de la casa (`--color-marca-suave → --color-beige`, `--color-marca → --color-ciruela`). Ángulos 120–160°.
- Texto sobre foto: siempre con velo (`linear-gradient` ciruela 0 → 60 %) para contraste AA.

### Tipografía
- Títulos: `var(--fuente-titulos)` (Cormorant Garamond). Se permite *itálica* para una palabra de énfasis en `h1` (ej.: "VAMOS DE *compras*"). Si usas itálica, agrega `ital,wght@1,600` a la URL de Google Fonts de las páginas que la usen.
- Texto: `var(--fuente)` (Jost). Etiquetas: 600, mayúsculas, `letter-spacing` 0.08–0.14em, tamaño 0.7–0.85rem.
- Tamaños fluidos para títulos grandes: `font-size: clamp(2.2rem, 1.4rem + 3.5vw, 4rem)`.
- No más de 2 familias. No negritas en Cormorant por debajo de 1.1rem.

### Espacio y forma
- Escala de espacio: 4, 8, 12, 16, 24, 32, 48, 64, 96 px. Secciones separadas 48 px (móvil) a 96 px (escritorio).
- Radios: `--radio` (10px) tarjetas, `--radio-grande` (18px) bloques grandes, 6px botones, 50 % círculos, 99px píldoras.
- Una sola sombra (`--sombra`). Para "elevación" en hover, aumenta su opacidad con un pseudo-elemento (ver `styleme-microinteracciones`), no inventes otra.
- Mobile first. Cortes: 720px y 1080px. Sin scroll horizontal nunca.

### Íconos e imágenes
- Íconos: solo los de `ICONOS` en `assets/js/comun.js` (línea, `stroke-width 1.5`). Para uno nuevo, agrégalo ahí con el mismo estilo (viewBox 24, sin relleno). Sin emojis en la interfaz.
- Fotos: `object-fit: cover`; logos PNG con `contain` (ya existe la regla). Fondo `--color-beige` mientras cargan.
- Decoración: `assets/img/decoracion/`.

### Tono de los textos
Cercano, en "tú", frases cortas, local ("Bucaramanga", "Cabecera"). Nada de promesas que la IA no cumple (no "tu talla perfecta", no "diagnóstico").

## Checklist rápida
- [ ] ¿Usa solo tokens de `:root`?
- [ ] ¿Se ve bien a 360 px de ancho?
- [ ] ¿Contraste AA?
- [ ] ¿Cabe en el concepto "seda / vitrina / cercanía"?
- [ ] ¿Comentarios en español explicando qué se cambia y dónde (✏️ como en el resto del código)?
