# 14 · Plan de ramas y fases de la entrega final

> Cómo se organiza el trabajo en Git para cumplir los requisitos de la entrega final (D-045) y la rúbrica (D-055):
> historial de commits progresivo, ramas para funcionalidades y correcciones, y merges con pull requests (PR).
>
> ⚠️ Las tecnologías marcadas 💡 son **sugerencias** hasta que se confirmen (PostgreSQL + pgvector, Java 21).
> ☕ Para instalar y cambiar la versión de Java se usa **SDKMAN** (ver la sección IMPORTANTE del README).
> 👤 La autora trabaja sola (D-049): todas las ramas y commits son suyos.
> 🔷 El backend sigue **arquitectura hexagonal** (D-057): cada módulo se construye de adentro hacia afuera (dominio → puertos → casos de uso → adaptadores). Ver [16](16-arquitectura-hexagonal.md).

---

## 1. Qué se evalúa y dónde se gana cada punto

| Criterio | Puntos | % | Ramas donde se gana |
|---|---|---|---|
| Dominio y conocimiento del código | 10 | 13,3 % | Todas (la autora escribe y explica su código) |
| Generación de embeddings e indexación vectorial | 9 | 12,0 % | 3 · `feature/base-de-datos`, 5 · `feature/embeddings-busqueda` |
| Búsqueda semántica y recuperación vectorial | 9 | 12,0 % | 5 · `feature/embeddings-busqueda` |
| Implementación de arquitectura RAG | 9 | 12,0 % | 6 · `feature/rag-lia` |
| Automatización e integración de servicios de IA | 8 | 10,7 % | 11 · `feature/n8n-docker` |
| Calidad de respuestas, validación y pruebas de IA | 8 | 10,7 % | 7 · `test/evaluacion-ia` |
| Análisis de datos e indicadores | 8 | 10,7 % | 8 · `feature/comunidad-analitica`, 14 · `docs/entrega-final` |
| Integración funcional de la aplicación | 7 | 9,3 % | 10 · `feature/iu-api`, 11 · `feature/n8n-docker` |
| Documentación, control de versiones y sustentación | 7 | 9,3 % | Todas + 14 · `docs/entrega-final` |

> 🎯 **La IA vale el 57 %** (embeddings + búsqueda + RAG + automatización + pruebas). Por eso la **Fase 3** es la más importante,
> y las fases 1 y 2 se recortaron a lo que la IA necesita para llegar antes a ella (D-059).

---

## 2. La idea general

```
main      ●────────────────●────────────────●──────────── ...   (solo versiones terminadas)
           \              /                /
develop     ●──●──●──●──●────●──●──●──●──●──────────────── ...   (aquí se junta todo)
               \    /         \      /
ramas de        ●──●           ●──●──●                           (aquí se trabaja)
trabajo   chore/limpieza...  feature/base-de-datos
```

| Rama | Para qué | Regla |
|---|---|---|
| `main` | Versión terminada de cada fase | **Nunca** se trabaja directo. Solo recibe PR desde `develop` al final de cada fase |
| `develop` | Junta el trabajo terminado de cada rama | **Nunca** se trabaja directo. Solo recibe PR de las ramas de trabajo |
| `feature/` `refactor/` `chore/` `docs/` `test/` | Una por área grande | Nace de `develop`, vuelve a `develop` con PR y se borra |
| `fix/...` | Corregir un error | Igual que las anteriores |

**Reglas de oro**
1. Una rama a la vez: no empieces la siguiente hasta hacer el merge de la anterior.
2. Commit **cada vez que termines una parte**, repartidos en varios días. Nunca todo el mismo día.
3. Mensajes con **Conventional Commits** (sección 3).
4. Si la IA ayudó en esa rama, actualiza `IAS_usadas.md` en un commit de esa misma rama.
5. Antes de cada PR, ensaya explicar en voz alta qué hace cada archivo que cambiaste (dominio del código = 13,3 %).

---

## 3. Conventional Commits (D-056)

**Forma:** `tipo(alcance): descripción`

- **tipo** (obligatorio):

  | Tipo | Cuándo |
  |---|---|
  | `feat` | Funcionalidad nueva |
  | `fix` | Corrección de un error |
  | `docs` | Solo documentación |
  | `refactor` | Reorganizar código sin cambiar lo que hace |
  | `test` | Agregar o cambiar pruebas |
  | `chore` | Mantenimiento (borrar archivos, configuración) |
  | `build` | Docker, Maven, dependencias |
  | `style` | Formato o diseño visual sin cambiar la lógica (rama `style/iu-visual`) |
  | `perf` | Mejora de rendimiento |

- **alcance** (opcional, entre paréntesis): la parte del proyecto →
  capas del hexágono: `dominio`, `aplicacion`, `persistencia`, `api`, `seguridad`, `arquitectura` ·
  temas: `db`, `ia`, `rag`, `lia`, `iu`, `n8n`, `docker`, `planeacion`…
- **descripción:** en minúscula, en infinitivo ("agregar", "corregir"), sin punto final, máximo ~70 caracteres.

**Ejemplos:** `feat(api): agregar endpoint de tiendas` · `fix(rag): corregir umbral de similitud` · `docs: actualizar README`

---

## 4. Configuración inicial (UNA sola vez)

```
git checkout main
git pull origin main
git checkout -b develop
git push -u origin develop
```

**Proteger las ramas (recomendado):** GitHub → **Settings → Branches → Add branch ruleset** (o *Add rule*) →
rama `main` → **Require a pull request before merging**. Repetir con `develop`.

---

## 5. Receta para CADA rama

**Paso 1 · Partir de `develop` actualizado**
```
git checkout develop
git pull origin develop
git checkout -b NOMBRE-DE-LA-RAMA
```

**Paso 2 · Commits por partes**
```
git status
git add ARCHIVOS-O-CARPETAS
git commit -m "tipo(alcance): descripción"
```

**Paso 3 · Subir la rama** (la primera vez con `-u`; después basta `git push`)
```
git push -u origin NOMBRE-DE-LA-RAMA
```

**Paso 4 · Pull request en GitHub**
1. **Compare & pull request**. GitHub propone **`base: main`** porque `main` es la rama principal (D-061).
   ⚠️ **Antes de crear el PR**, en el primer menú de la izquierda cambia **`base: main` → `base: develop`**.
   Debe quedar: **base: `develop`** ← **compare: `NOMBRE-DE-LA-RAMA`**.
   Comprobación: arriba del PR debe decir *"wants to merge N commits into **develop**"*. Si dice `main`, toca **Edit** junto al título y cambia la base.
2. Título en formato Conventional Commits (ej.: `feat(db): base de datos con pgvector`). Descripción:
   ```
   ## Qué hace
   - ...
   ## Cómo probarlo
   - ...
   ## Uso de IA
   - (qué se hizo con ayuda de IA o "No se usó IA")
   ```
3. **Merge pull request → Create a merge commit** (no "Squash") → **Delete branch**.

**Paso 5 · Limpiar en tu computador**
```
git checkout develop
git pull origin develop
git branch -d NOMBRE-DE-LA-RAMA
```

**Paso 6 · Fin de FASE: `develop → main`** (el único PR que se deja con `base: main`)
PR **base: `main`** ← **compare: `develop`**, título `release: fase N - nombre`. Merge con *Create a merge commit* (**no** borres `develop`).
(Opcional) *Releases → Draft a new release* → etiqueta `v0.N`.

---

## 6. Las fases y qué sube cada rama

### FASE 0 · Organización → `v0.1`

#### Rama 1 · `chore/limpieza-archivos` ✅ HECHA (PR #1, 2026-11-23)

| # | Commit (con gitmoji, antes de D-056) |
|---|---|
| 1 | `chore: :fire: eliminar imágenes sin uso` |
| 2 | `refactor: :truck: separar Lía en carpetas 2d y 3d` |
| 3 | `docs: :memo: borrar plan de entrega y agregar plan de ramas` |

> ⚠️ Los PR #1 y #2 se unieron a `main` en vez de `develop`. Se corrigió poniendo `develop` al día con `main`
> (`git checkout develop` → `git merge origin/main` → `git push origin develop`). Por eso la Fase 0 llegó a `main` sin PR de release.
> Desde ahora, en cada PR revisa que diga **base: `develop`**.

#### Rama 2 · `refactor/estructura-proyecto`

| # | Qué se hace | Commit |
|---|---|---|
| 0a ✅ | Modelo de datos | `docs(planeacion): agregar modelo de datos con pgvector` |
| 0b ✅ | Plan de ramas alineado con la rúbrica | `docs(planeacion): alinear plan de ramas con la rúbrica` |
| 0c ✅ | Guía de SDKMAN y decisiones D-049 a D-056 | `docs: agregar guía de sdkman y decisiones D-049 a D-056` |
| 0d ✅ | Arquitectura hexagonal + plan 14 + D-057 + README | `docs(arquitectura): definir arquitectura hexagonal del backend` |
| 1 ✅ | Mover páginas HTML, `assets/` y `data/` a `frontend/` | `refactor(frontend): mover el sitio a frontend/` |
| 1b ✅ | Renombrar `frontend/` → `iu/` (nombres en español, D-058) | `refactor(iu): renombrar frontend a iu` |
| 2 ✅ | `planeacion/` → `docs/planeacion/` + carpetas de entregables | `docs: crear estructura de documentación` |
| 3 ✅ | `IAS_usadas.md` en la raíz: registro del uso de IA (D-060) | `docs: agregar registro de uso de ia` |
| 3b ✅ | Reordenar las fases para llegar antes a la IA (D-059) | `docs(planeacion): priorizar la fase de inteligencia artificial` |
| 4 ✅ | `backend/` con las capas `dominio`, `aplicacion`, `infraestructura` (con `package-info.java`); `basedatos/`, `n8n/`; `.env.ejemplo` → `.env.example`; `.gitignore` para Java | `chore: preparar estructura de backend, base de datos y n8n` |
| 5 ✅ | README con la nueva estructura | `docs: actualizar README con la estructura del proyecto` |

➡️ **Fin de Fase 0:** PR `develop → main` → `release: fase 0 - organización`.

---

### FASE 1 · Base de datos vectorial → `v0.2`

#### Rama 3 · `feature/base-de-datos`
*Criterios: embeddings e indexación vectorial (base). Solo lo que la IA necesita; los datos de análisis van en la rama 8.*

| # | Qué se hace | Commit |
|---|---|---|
| 1 | `docker-compose.yml` con **solo** PostgreSQL + pgvector (imagen `pgvector/pgvector`); los scripts de `basedatos/` se ejecutan solos al crear la base | `build(docker): agregar postgres con pgvector` |
| 2 | Diagrama entidad-relación en `docs/diagramas/` (DBML + PNG) | `docs(db): agregar diagrama entidad-relación` |
| 3 | `basedatos/01-esquema.sql`: extensión **pgvector**, las 16 tablas, llaves y `CHECK` | `feat(db): crear esquema con pgvector` |
| 4 | `basedatos/02-indices.sql`: índices, incluido el índice vectorial **HNSW** de `fragmento_conocimiento` | `feat(db): agregar índices e índice vectorial hnsw` |
| 5 | `basedatos/03-datos-iniciales.sql`: 10 tiendas y 49 productos reales (con ingredientes y etiquetas) | `feat(db): cargar datos iniciales del catálogo` |

✅ **Terminada cuando:** `docker compose up` crea la base con las 16 tablas y el catálogo, sin errores.

---

### FASE 2 · Backend base → `v0.3`

#### Rama 4 · `feature/backend-base`
*Solo el catálogo: es lo que la IA convierte en embeddings. Orden hexagonal: primero el centro, después los adaptadores.*

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Spring Initializr en `backend/` (Web, JPA, PostgreSQL, Validation) + `.sdkmanrc` | `build(backend): crear proyecto spring boot` |
| 2 | `ArquitecturaHexagonalTest` con ArchUnit (vigila las reglas R1, R2, R3 y R7) | `test(arquitectura): agregar reglas hexagonales con archunit` |
| 3 | **Dominio** del catálogo: Tienda, Producto, Categoria, Zona, Ingrediente, Etiqueta + excepciones + pruebas | `feat(dominio): agregar modelo del catálogo` |
| 4 | **Puertos** y **casos de uso** del catálogo + pruebas con puertos falsos | `feat(aplicacion): agregar casos de uso del catálogo` |
| 5 | **Adaptador de salida** JPA: entidades, repositorios Spring Data, mapeadores | `feat(persistencia): agregar adaptador jpa del catálogo` |
| 6 | **Adaptador de entrada** REST: controladores, DTO, `ManejadorErrores`, `ConfiguracionBeans` | `feat(api): agregar endpoints de tiendas y productos` |
| 7 | Swagger con springdoc-openapi | `docs(api): agregar documentación swagger` |

➡️ **Fin de Fase 2:** `release: fase 2 - backend base`.

---

### FASE 3 · Inteligencia artificial (la más importante) → `v0.4`

#### Rama 5 · `feature/embeddings-busqueda`
*Criterios: embeddings e indexación vectorial (9) + búsqueda semántica y recuperación vectorial (9).*

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Guías de conocimiento escritas por la autora (skincare, cuidado capilar, estilo) en `backend/src/main/resources/conocimiento/guias/` | `docs(ia): agregar guías de conocimiento` |
| 2 | Dominio: `FragmentoConocimiento`, `Embedding`, `FragmentadorConocimiento` (chunking de tiendas, productos y guías) + pruebas | `feat(dominio): agregar fragmentos de conocimiento y chunking` |
| 3 | Puertos `EmbeddingPuerto`, `AlmacenVectorialPuerto` + `IndexarConocimientoServicio` (recalcula solo si cambió el hash) | `feat(ia): agregar caso de uso de indexación` |
| 4 | `GeminiEmbeddingAdaptador` (clave en variable de entorno) | `feat(ia): agregar adaptador de embeddings de gemini` |
| 5 | `PgVectorAdaptador`: guardar vectores y buscar con `<=>` sobre el índice HNSW | `feat(ia): agregar adaptador pgvector` |
| 6 | `BuscarSemanticoServicio`: top-k + umbral + filtros (búsqueda **híbrida**) | `feat(ia): agregar búsqueda semántica híbrida` |
| 7 | Adaptadores de entrada: `BusquedaControlador`, `AdminIndexacionControlador`, `ReindexacionProgramador` | `feat(api): agregar endpoints de búsqueda y reindexación` |
| 8 | Pruebas con Testcontainers (pgvector real): "crema para piel grasa" trae productos de piel grasa | `test(ia): agregar pruebas de búsqueda semántica` |

#### Rama 6 · `feature/rag-lia`
*Criterio: arquitectura RAG (9).*

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Dominio: `ConversacionLia`, `RespuestaLia`, `ValidadorRecomendaciones` (anti-alucinación), `ResumidorConversacion` + pruebas | `feat(dominio): agregar validación de respuestas de lía` |
| 2 | Puerto `ModeloLenguajePuerto` + `ChatearConLiaServicio`: recuperar → aumentar → generar → validar, con plan B | `feat(rag): agregar caso de uso del flujo rag` |
| 3 | Instrucciones de Lía como plantilla en `resources/prompts/` | `feat(rag): agregar plantilla de instrucciones de lía` |
| 4 | `GeminiChatAdaptador` con respuesta por partes (streaming) | `feat(ia): agregar adaptador de chat de gemini` |
| 5 | Guardar resumen y métricas de la conversación (adaptador JPA) | `feat(lia): registrar métricas de conversación` |
| 6 | `LiaControlador` (`POST /api/lia/chat`) | `feat(api): agregar endpoint de chat de lía` |
| 7 | La iu de Lía usa el backend | `refactor(iu): conectar lía con el backend` |
| 8 | Borrar `netlify/functions/lia.mjs` | `chore: eliminar función de netlify` |

#### Rama 7 · `test/evaluacion-ia`
*Criterio: calidad de respuestas, validación y pruebas de IA (8).*

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Conjunto de **30 preguntas** con lo esperado (catálogo, fuera de tema, salud, ambiguas) | `test(ia): agregar conjunto de preguntas de evaluación` |
| 2 | Prueba automática de recuperación: **precisión@k** y **recall@k** | `test(ia): medir precisión de la recuperación` |
| 3 | Prueba de respuestas: sin productos inventados, rechaza lo ajeno, no da consejos médicos | `test(ia): validar respuestas de lía` |
| 4 | `EvaluarIaCasoUso` + adaptador que guarda cada ejecución en `evaluacion_ia` | `feat(ia): registrar resultados de evaluación` |
| 5 | Informe de calidad de IA en `docs/pruebas/` | `docs(pruebas): agregar informe de calidad de ia` |

➡️ **Fin de Fase 3:** `release: fase 3 - inteligencia artificial`.

---

### FASE 4 · Comunidad, seguridad e integración → `v0.5`

#### Rama 8 · `feature/comunidad-analitica` ✅ HECHA (PR #11, 2026-11-25)
*Criterio: análisis de datos e indicadores (8).*

| # | Qué se hace | Commit |
|---|---|---|
*Reseñas y favoritos anónimos por sesión (D-066).*

| # | Qué se hace | Commit |
|---|---|---|
| 1 | `basedatos/05-vistas-indicadores.sql`: 13 vistas de indicadores | `feat(db): agregar vistas de indicadores` |
| 2 | `basedatos/04-datos-prueba.sql`: usuarios, reseñas, favoritos, eventos y conversaciones de prueba (marcados) | `feat(db): agregar datos de prueba para el análisis` |
| 3 | Dominio: reseña (corazones 1–5, moderación), favorito, objetivo "una de dos", evento, indicador | `feat(dominio): agregar reglas de reseñas, favoritos y eventos` |
| 4 | Puertos y casos de uso de reseñas, moderación, favoritos, eventos e indicadores | `feat(aplicacion): agregar casos de uso de comunidad y analítica` |
| 5 | Adaptadores de persistencia (SQL con JdbcTemplate) + pruebas contra la base | `feat(persistencia): agregar adaptadores de comunidad y analítica` |
| 6 | Endpoints REST `/api/resenas`, `/api/favoritos`, `/api/eventos`, `/api/admin/resenas`, `/api/admin/indicadores` + pruebas HTTP | `feat(api): agregar endpoints de comunidad y analítica` |
| 7 | Documentación (D-066, READMEs, plan, IAS) | `docs: documentar comunidad y analítica` |

#### Rama 9 · `feature/seguridad-admin`
*Cambia de JWT completo a una clave de administración (D-066): quien visita no necesita cuenta.*

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Filtro que exige la cabecera `X-Admin-Key` (valor en el `.env`) en `/api/admin/*` | `feat(seguridad): proteger endpoints de administración` |
| 2 | Swagger con el botón "Authorize" para la clave | `docs(api): documentar la clave de administración` |
| 3 | Pruebas: sin clave 401, con clave 200 | `test(seguridad): agregar pruebas de la clave de administración` |
| 4 | Documentación (README, repaso, IAS) | `docs: documentar la clave de administración` |

#### Rama 10 · `feature/iu-api`
*Criterio: integración funcional (7).*

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Módulo que llama a la API | `feat(iu): agregar cliente de la api` |
| 2 | Tiendas, perfil y productos desde la API | `feat(iu): cargar tiendas y productos desde la api` |
| 3 | El buscador de productos usa la **búsqueda semántica** | `feat(iu): usar búsqueda semántica en el catálogo` |
| 4 | Identificador anónimo de sesión (D-066) | `feat(iu): agregar sesión anónima` |
| 5 | Reseñas y favoritos | `feat(iu): agregar reseñas y favoritos` |
| 6 | Registro de eventos | `feat(iu): registrar eventos de uso` |
| 7 | Panel de admin con gráficos de indicadores | `feat(iu): agregar panel de indicadores` |
| 8 | Borrar `iu/data/*.js` | `chore(iu): eliminar datos locales` |

#### Rama 11 · `feature/n8n-docker`
*Criterios: automatización e integración de servicios de IA (8), integración funcional.*

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Dockerfiles del backend y de la iu (nginx) | `build(docker): agregar dockerfiles` |
| 2 | `docker-compose.yml` completo: pgvector, backend, iu, n8n | `build(docker): completar docker compose` |
| 3 | `.env.example` con todas las variables | `chore: completar variables de entorno` |
| 4 | Adaptadores n8n: `N8nNotificadorAdaptador` (salida) y `ModeracionWebhookControlador` (entrada) | `feat(n8n): agregar adaptadores de n8n` |
| 5 | n8n: **moderación de reseñas con Gemini** → cambia el estado a PUBLICADA/RECHAZADA | `feat(n8n): agregar moderación de reseñas con ia` |
| 6 | n8n: **reindexación programada** de embeddings (solo recalcula lo que cambió; D-066) | `feat(n8n): agregar reindexación automática de embeddings` |
| 7 | n8n: **reporte semanal** de indicadores con resumen escrito por IA | `feat(n8n): agregar reporte semanal con ia` |
| 8 | Exportar los workflows a `n8n/*.json` | `chore(n8n): exportar workflows` |
| 9 ✅ | Borrar `netlify.toml` (se adelantó a la rama 7, D-065) | `chore: eliminar configuración de netlify` |

✅ **Terminada cuando:** `docker compose up` levanta todo en un computador limpio.

➡️ **Fin de Fase 4:** `release: fase 4 - comunidad, seguridad e integración`.

---

### FASE 5 · Catálogo completo y parte visual → `v0.6`

> Empieza **solo cuando toda la lógica que pide el profesor esté terminada** (fases 1 a 4) (D-063).

#### Rama 12 · `feature/catalogo-completo`
*Antes de lo visual: registrar todas las tiendas con la información completa (D-064).*

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Completar las tiendas: descripción, dirección, horario, coordenadas, WhatsApp, Instagram, web | `feat(db): completar información de contacto de las tiendas` |
| 2 | Completar los productos: descripción, precio, ingredientes, etiquetas y fotos | `feat(db): completar información de los productos` |
| 3 | Marcar como verificados los datos confirmados con cada tienda | `feat(db): marcar datos verificados del catálogo` |
| 4 | Reindexar los embeddings con el catálogo nuevo y volver a correr la evaluación de IA | `test(ia): reevaluar la ia con el catálogo completo` |

#### Rama 13 · `style/iu-visual`
*Rama dedicada **solo** a la parte visual de la página (D-063). Se planea en detalle cuando empiece.*

| Tema | Idea |
|---|---|
| Cambios visuales de la iu | Los que la autora defina al llegar a esta fase |
| **Probador con IA** 💡 | La persona toma o sube una foto de **cuerpo completo**; la IA la adapta como si estuviera **dentro de la tienda** probándose la ropa, con aspecto **humanoide**, y con **4 poses predeterminadas básicas** para apreciar cómo quedan los outfits (P-017). Requiere decidir el modelo de IA de imágenes, la privacidad de las fotos y el aviso para mayores de 18 |

➡️ **Fin de Fase 5:** `release: fase 5 - catálogo y visual`.

---

### FASE 6 · Entrega → `v1.0`

#### Rama 14 · `docs/entrega-final`

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Diagrama de arquitectura (con el flujo RAG) | `docs: agregar diagrama de arquitectura` |
| 2 | Colección de Postman + capturas de Swagger | `docs: agregar evidencias de la api` |
| 3 | Documento de pruebas | `docs(pruebas): agregar documento de pruebas` |
| 4 | Informe de análisis de datos (indicadores, SQL, interpretación, ≥3 conclusiones) | `docs: agregar informe de análisis de datos` |
| 5 | README final | `docs: completar README` |
| 6 | `IAS_usadas.md` final | `docs: completar registro de uso de ia` |
| 7 | Guía de sustentación (qué explicar de cada parte) | `docs: agregar guía de sustentación` |
| 8 | Presentación final | `docs: agregar presentación final` |

---

### Ramas `fix/...`

Cuando aparezca un error, con la receta de la sección 5. Ej.: `fix/rutas-iu` → `fix(iu): corregir rutas de imágenes`.
Lo ideal: al menos 2 o 3 en todo el proyecto.

---

## 7. Resumen

| Fase | Ramas | Versión |
|---|---|---|
| 0 · Organización | `chore/limpieza-archivos` ✅, `refactor/estructura-proyecto` | v0.1 |
| 1 · Base de datos vectorial | `feature/base-de-datos` | v0.2 |
| 2 · Backend base | `feature/backend-base` | v0.3 |
| 3 · Inteligencia artificial | `feature/embeddings-busqueda`, `feature/rag-lia`, `test/evaluacion-ia` | v0.4 |
| 4 · Comunidad, seguridad e integración | `feature/comunidad-analitica`, `feature/seguridad-admin`, `feature/iu-api`, `feature/n8n-docker` | v0.5 |
| 5 · Catálogo completo y visual | `feature/catalogo-completo`, `style/iu-visual` | v0.6 |
| 6 · Entrega | `docs/entrega-final` | v1.0 |

**Total:** 2 permanentes + 14 de trabajo + las `fix/...` que salgan.
