# StyleMe

Página web para descubrir tiendas físicas de moda y belleza de Bucaramanga (Santander, Colombia): dónde quedan, cómo contactarlas, cómo armar una salida de compras y un probador virtual con inteligencia artificial.

> Proyecto académico. Versión actual: **versión sencilla para la entrega** (ver [plan de entrega](planeacion/09-plan-entrega.md)).

## Cómo abrir la página en tu computador

La página usa un módulo de JavaScript (el probador), así que **no funciona con doble clic** sobre el archivo. Hay que abrirla con un servidor local:

1. En VS Code, instala la extensión **Live Server** (de Ritwick Dey).
2. Abre `index.html`.
3. Clic derecho → **Open with Live Server**.

## Cómo activar a Lía (la asesora con IA)

Lía funciona en dos modos:
- **Con IA (Gemini):** responde preguntas abiertas y recomienda productos del catálogo cuando le pides ayuda para comprar. Necesita la función de Netlify.
- **Modo básico (sin IA):** si la función no está disponible (por ejemplo, con Live Server), Lía responde con reglas sencillas y lo avisa.

### 1. Sacar la clave de Gemini (gratis)
1. Entra a [Google AI Studio](https://aistudio.google.com/apikey) con una cuenta de Google y crea una **API key**.
2. ⚠️ La clave es secreta: **nunca** la pegues en un archivo `.js` ni en un `.html`, y nunca la subas a GitHub.

### 2. Probar en tu computador
1. Copia el archivo `.env.ejemplo`, llama a la copia **`.env`** y pega tu clave en `GEMINI_API_KEY=`. El archivo `.env` está en `.gitignore`, así que no se sube a GitHub.
2. En la terminal de VS Code (una sola vez): `npm install -g netlify-cli`
3. En la carpeta del proyecto: `netlify dev`
4. Abre la dirección que aparece (normalmente `http://localhost:8888`) y toca **"Habla con Lía"**.

> Con Live Server la página funciona, pero Lía queda en **modo básico**, porque Live Server no ejecuta funciones.

### 3. Publicar en Netlify
1. En [Netlify](https://www.netlify.com/), crea un sitio nuevo conectado a tu repositorio de GitHub. No necesita "build": el archivo `netlify.toml` ya lo configura.
2. En **Site configuration → Environment variables**, agrega `GEMINI_API_KEY` con tu clave.
3. Publica. Lía usará la IA en `https://tu-sitio.netlify.app`.

> **Privacidad:** en el plan gratuito, Google puede usar las conversaciones para mejorar sus productos. Por eso el chat avisa: *"No compartas datos personales"* (Ley 1581 de 2012).

## Estructura del proyecto

```
StyleMe/
├── index.html          Inicio
├── tiendas.html        Lista de tiendas
├── tienda.html         Perfil de una tienda (tienda.html?id=...)
├── productos.html      Catálogo con buscador y filtros
├── mapa.html           Mapa de todas las tiendas
├── salida.html         Planear una salida de compras
├── probador.html       Probador virtual (solo ropa)
├── recomendador.html   Rutina sugerida de skincare o cuidado capilar
├── acerca.html         Qué es StyleMe, privacidad y contacto
│
├── data/               ← AQUÍ SE EDITAN LOS DATOS
│   ├── tiendas.js      Las 10 tiendas
│   └── productos.js    Los productos
│
├── assets/
│   ├── css/estilos.css Diseño (los colores se cambian arriba, en :root)
│   ├── js/             Un archivo por página + comun.js (compartido)
│   └── img/            Fotos de tiendas y productos
│
└── planeacion/         Documentos de planeación (no son parte de la página)
```

## Tecnología

| Parte | Herramienta | Costo |
|---|---|---|
| Página | HTML, CSS y JavaScript | $0 |
| Mapas | [Leaflet](https://leafletjs.com/) + [OpenStreetMap](https://www.openstreetmap.org/) | $0 |
| IA del probador | [MediaPipe Pose Landmarker](https://ai.google.dev/edge/mediapipe/solutions/vision/pose_landmarker) (corre en el navegador) | $0 |
| IA de Lía | [Gemini API](https://ai.google.dev/) (modelo de lenguaje + embeddings para RAG) | $0 (plan gratuito) |
| Servidor de Lía | [Netlify Functions](https://docs.netlify.com/functions/overview/) (`netlify/functions/lia.mjs`) | $0 (plan gratuito) |
| Contacto | Enlaces de WhatsApp (`wa.me`) y Google Maps | $0 |
| Publicación | GitHub Pages o Netlify | $0 |

## Documentación de planeación

| Documento | Contenido |
|---|---|
| [01 - Visión](planeacion/01-vision.md) | Qué es StyleMe, qué problemas busca resolver y para quién |
| [02 - Alcance y fases](planeacion/02-alcance-y-fases.md) | Funciones confirmadas, opcionales y futuras; etapas (MVP) |
| [03 - Roles y permisos](planeacion/03-roles-y-permisos.md) | Qué puede hacer cada tipo de usuario |
| [04 - Registro de decisiones](planeacion/04-decisiones.md) | Decisiones tomadas, con fecha y motivo |
| [05 - Riesgos](planeacion/05-riesgos.md) | Riesgos identificados y cómo mitigarlos |
| [06 - Pendientes y preguntas](planeacion/06-pendientes.md) | Decisiones abiertas y preguntas por responder |
| [07 - Historias de usuario MVP 1](planeacion/07-historias-mvp1.md) | Borrador de lo que debe poder hacer cada usuario en la primera versión |
| [08 - Plan de validación](planeacion/08-validacion.md) | Encuesta y entrevistas para comprobar los problemas planteados |
| [09 - Plan de entrega](planeacion/09-plan-entrega.md) | ⭐ **Versión sencilla** que se construye para la entrega de esta semana |
| [10 - Proyecto completo](planeacion/10-proyecto-completo.md) | Visión original: qué buscar, implementar y mejorar después |
| [11 - Contexto visual](planeacion/11-contexto-visual.md) | Resumen del proyecto + dirección visual, animaciones y fluidez (lo usan las skills de `.claude/skills/`) |
| [12 - Lía, asesora virtual](planeacion/12-lia-asesora-virtual.md) | ⭐ Planeación de Lía: personaje 3D, IA (RAG + embeddings), conversación, tienda 3D y fases |
| [13 - Lía: avatar y emociones](planeacion/13-lia-avatar-y-emociones.md) | Guía para crear a Lía en VRoid Studio, movimientos fluidos, emociones y lip sync |

### Convención usada en los documentos

- ✅ **Confirmado:** decidido por la autora del proyecto.
- 💡 **Sugerencia:** propuesta del asesor, pendiente de aprobación.
- ⏳ **Pendiente:** aún no decidido.
- 🔮 **Futuro:** fuera del alcance inicial.
