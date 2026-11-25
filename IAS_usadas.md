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
| Lía: imágenes de las expresiones (`iu/assets/lia/`) | ✏️ | ✏️ | ✏️ |
| Lía: función con RAG (`netlify/functions/lia.mjs`, borrada en `feature/rag-lia`) | Claude | Generó la función: embeddings, similitud, instrucciones del modelo y streaming | Eligió Gemini (D-042), obtuvo la clave, probó |
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
| `feature/base-de-datos` | Claude | Generó el `docker-compose.yml` de PostgreSQL + pgvector (a pedido de la autora), configuró las variables locales y Live Server, y generó el diagrama entidad-relación (DBML y Mermaid) con su explicación de formas normales, a partir del modelo de datos acordado, quitó el código y la documentación de Lía 3D (a pedido de la autora) escribió `01-esquema.sql` completo (16 tablas) , `02-indices.sql` (23 índices, incluido el HNSW) y `03-datos-iniciales.sql` (generado con un script a partir de `iu/data/*.js`) a pedido de la autora, y los probó en una base real |
| `feature/backend-base` | Claude | La autora generó el proyecto con Spring Initializr; la IA terminó de copiarlo, movió la clase principal a `co.styleme`, escribió `application.yml` (puerto 8090, conexión desde `.env`, `ddl-auto: validate`) comprobó la conexión con la base, escribió la prueba ArchUnit de las reglas hexagonales (verificada con una clase que rompe las reglas a propósito) y, a pedido de la autora, el módulo de catálogo completo: dominio, puertos, casos de uso, adaptador JPA, API REST y Swagger, con 49 pruebas |
| `feature/embeddings-busqueda` | Claude | A pedido de la autora: redactó las 3 guías de conocimiento (revisión pendiente de la autora), el dominio de IA (embeddings, fragmentos, chunking), la indexación con hashes, el adaptador de Gemini, el adaptador pgvector (HNSW, búsqueda híbrida) y la búsqueda semántica, con 86 pruebas (5 con Gemini real) |
| `feature/rag-lia` | Claude | A pedido de la autora: llevó el RAG de Lía al backend (dominio, validador anti-alucinación, plan B, caso de uso RAG, plantilla de instrucciones, adaptador de chat de Gemini con streaming, resumen de conversaciones, endpoint NDJSON, CORS), conectó la página y borró la función de Netlify; 127 pruebas (incluidas pruebas con Gemini real) |
| `test/evaluacion-ia` | Claude | A pedido de la autora: armó el conjunto de 30 preguntas con su verdad de referencia, el dominio y el caso de uso de evaluación (precisión@k, recall@5, alucinación, presupuesto, salud), el endpoint y la persistencia en evaluacion_ia; ejecutó la evaluación con Gemini real, corrigió lo que encontró (reintento ante 429, productos de ejemplo en el índice, regla de salud) y redactó el informe de calidad de IA; borró netlify.toml |
| `feature/comunidad-analitica` | Claude | A pedido de la autora: escribió las 13 vistas de indicadores y el script de datos de prueba (marcados), el dominio y los casos de uso de reseñas, moderación, favoritos, eventos e indicadores, los adaptadores SQL, los endpoints REST y sus pruebas (unitarias, contra la base y HTTP). La autora decidió que reseñas y favoritos fueran anónimos por sesión y cambiar la autenticación por una clave de administración (D-066) |
| `feature/seguridad-admin` | Claude | A pedido de la autora: escribió el filtro que exige la clave de administración en `/api/admin/*`, el botón Authorize de Swagger, la variable en `.env.example` y las pruebas (sin clave, clave equivocada, clave correcta, rutas públicas, CORS) |
| `feature/iu-api` | Claude | A pedido de la autora: escribió `api.js` (catálogo desde el backend, sesión anónima, eventos, favoritos, reseñas, búsqueda semántica, administración), adaptó las páginas, creó Mis favoritos y el panel de administración, borró `iu/data/*.js`; probó todo con un navegador automatizado, encontró y corrigió el error de CORS con la clave, y midió el corte de similitud del buscador |
| `feature/n8n-docker` | Claude | A pedido de la autora: escribió los Dockerfiles (backend y nginx), el `docker-compose.yml` completo, el aviso del backend a n8n (`NotificadorPuerto`) y los 3 workflows de n8n con Gemini (moderación, reindexación, reporte semanal) con su generador; los probó de punta a punta en Docker (una crítica respetuosa se publicó y una con insultos y teléfono se rechazó) |
| *(siguientes ramas)* | | *(se completa al terminar cada rama)* |
