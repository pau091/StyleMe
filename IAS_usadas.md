# Uso de inteligencia artificial en StyleMe

> Este documento cumple la directriz del proyecto: registrar **las herramientas de IA utilizadas, su participación
> en el desarrollo, los componentes asistidos y las decisiones técnicas tomadas con su apoyo**.
> Se actualiza en cada rama donde se use IA (sección 6).
>
> ✏️ = dato que la autora debe completar o confirmar.

---

## 1. Herramientas utilizadas

| Herramienta | Proveedor | Para qué se usó | Tipo de uso |
|---|---|---|---|
| **Claude** (en Claude Code, extensión de VS Code) | Anthropic | Planeación, documentación, generación y revisión de código, reorganización del repositorio y tutoría técnica | Asistente de desarrollo |
| **ChatGPT** | OpenAI | ✏️ (ej.: consultas puntuales, generación de imágenes de Lía, textos…) | Asistente de desarrollo |
| **Gemini** (API) | Google | Cerebro de Lía: embeddings, búsqueda semántica y respuestas (RAG) | **Parte del producto**, no asistente de desarrollo |

> Gemini no ayudó a programar: es un servicio que **usa la aplicación** en tiempo de ejecución. Se lista porque
> es un componente de IA del sistema (ver la sección 4).

---

## 2. Participación en el desarrollo

### Etapa 1 · Sitio estático (2026-11-13 a 2026-11-22)
La IA tuvo una **participación alta**: a partir de las ideas y decisiones de la autora, generó buena parte de la
documentación y del código del sitio. La autora definió el concepto, el alcance, las tiendas, la identidad visual y
aprobó cada decisión; revisó y probó el resultado en el navegador.

### Etapa 2 · Entrega final (desde 2026-11-23)
Al recibir la directriz del proyecto, la IA pasa a **modo de tutoría técnica**:
- Da orientación conceptual, **ejemplos parciales** (moldes) y revisión de código.
- **No** genera soluciones completas ni ejecuta el proyecto de forma autónoma.
- La autora escribe el código de cada módulo siguiendo los moldes, y la IA lo revisa.
- La IA **no** hace commits ni usa GitHub: la autora crea las ramas, los commits y los pull requests.
- Excepción: tareas mecánicas de organización (mover archivos, corregir rutas, documentos de planeación)
  que la autora pidió y aprobó explícitamente.

---

## 3. Componentes asistidos

| Componente | Herramienta | Qué hizo la IA | Qué hizo la autora |
|---|---|---|---|
| Documentos de planeación (`docs/planeacion/01` a `16`) | Claude | Redactó los documentos a partir de las conversaciones y decisiones | Tomó las decisiones, revisó y aprobó el contenido |
| Páginas del sitio (`iu/*.html`, `iu/assets/js/*.js`) | Claude | Generó el código de las páginas, filtros, mapa, salida de compras, probador y recomendador | Definió las funciones, revisó y probó |
| Diseño, animaciones e intro (`iu/assets/css/*`, `movimiento.js`, `intro.js`) | Claude | Generó estilos, animaciones y la intro 3D siguiendo la guía visual | Definió la identidad visual ("Vitrina de Bucaramanga") y aprobó el resultado |
| Datos del catálogo (`iu/data/*.js`) | ✏️ | ✏️ | Eligió las tiendas reales; ✏️ |
| Lía: personaje y chat (`iu/assets/js/lia/`, `iu/assets/css/lia.css`) | Claude | Generó el personaje con globos de texto, emociones y el plan B por reglas | Definió la personalidad, el aspecto y las emociones |
| Lía: imágenes 2D (`iu/assets/lia/2d/`) y referencia 3D | ✏️ | ✏️ | ✏️ |
| Lía: función con RAG (`netlify/functions/lia.mjs`) | Claude | Generó la función: embeddings, similitud, instrucciones del modelo y streaming | Eligió Gemini (D-042), obtuvo la clave, probó |
| Reorganización del repositorio (rama `refactor/estructura-proyecto`) | Claude | Movió archivos, corrigió rutas, creó la estructura de `docs/` | Decidió la estructura y los nombres, hizo todos los commits |
| ✏️ (lo que se hizo con ChatGPT) | ChatGPT | ✏️ | ✏️ |

---

## 4. Decisiones técnicas tomadas con apoyo de IA

**Propuesta** = la sugirió la IA y la autora la aprobó · **Autora** = la decidió la autora y la IA ayudó a detallarla.

| Decisión | Origen | Registro |
|---|---|---|
| Usar Gemini como modelo de lenguaje y de embeddings para Lía | Propuesta | D-042 |
| Lía con RAG: solo recomienda productos reales del catálogo | Propuesta | P-009, D-008 |
| Estrategia de ramas `main` + `develop` + ramas por área, con pull requests | Propuesta | D-048 |
| Modelo de datos de 16 tablas, normalizado (ingredientes y etiquetas en tablas aparte) | Propuesta | D-050, D-055 |
| Reseñas y favoritos de tiendas **y** productos | Autora | D-051, D-052 |
| Guardar solo un resumen por conversación con Lía (privacidad y ahorro de tokens) | Autora (con propuesta de cómo hacerlo) | D-053 |
| PostgreSQL + pgvector con índice HNSW para la búsqueda vectorial | Propuesta | D-055 |
| Arquitectura hexagonal en el backend | Autora | D-057 |
| Nombres en español (`iu`, `dominio`, `aplicacion`, `infraestructura`) | Autora | D-058 |
| Conventional Commits | Autora | D-056 |
| Versiones de Java con SDKMAN | Autora | D-049 |

El detalle y el motivo de cada decisión están en [docs/planeacion/04-decisiones.md](docs/planeacion/04-decisiones.md).

---

## 5. Buenas prácticas aplicadas

- **Ninguna clave ni dato personal** se compartió con los asistentes. La clave de Gemini vive solo en `.env` (ignorado por Git).
- Todo lo generado con IA fue **revisado y probado** por la autora antes de cada commit.
- Las herramientas de IA **no forman parte del repositorio**: el proyecto funciona sin ellas (excepto Gemini, que es un componente del producto).
- Lía avisa a las personas que no compartan datos personales (Ley 1581 de 2012).

---

## 6. Registro por rama

| Rama | Herramienta | Uso de IA |
|---|---|---|
| `chore/limpieza-archivos` | Claude | Identificó archivos sin uso, separó Lía en 2D/3D y redactó el plan de ramas |
| `refactor/estructura-proyecto` | Claude | Modelo de datos, arquitectura hexagonal, mover el sitio a `iu/`, estructura de `docs/` y este documento |
| `feature/base-de-datos` | Claude | Generó el `docker-compose.yml` de PostgreSQL + pgvector (a pedido de la autora), configuró las variables locales y Live Server, y generó el diagrama entidad-relación (DBML y Mermaid) con su explicación de formas normales, a partir del modelo de datos acordado |
| *(siguientes ramas)* | | *(se completa al terminar cada rama)* |
