# StyleMe

Página web para descubrir tiendas físicas de moda y belleza de Bucaramanga (Santander, Colombia): dónde quedan, cómo contactarlas, cómo armar una salida de compras y un probador virtual con inteligencia artificial.

> Proyecto académico. Versión actual: **versión sencilla para la entrega** (ver [plan de entrega](planeacion/09-plan-entrega.md)).

## Cómo abrir la página en tu computador

La página usa un módulo de JavaScript (el probador), así que **no funciona con doble clic** sobre el archivo. Hay que abrirla con un servidor local:

1. En VS Code, instala la extensión **Live Server** (de Ritwick Dey).
2. Abre `index.html`.
3. Clic derecho → **Open with Live Server**.

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

### Convención usada en los documentos

- ✅ **Confirmado:** decidido por la autora del proyecto.
- 💡 **Sugerencia:** propuesta del asesor, pendiente de aprobación.
- ⏳ **Pendiente:** aún no decidido.
- 🔮 **Futuro:** fuera del alcance inicial.
