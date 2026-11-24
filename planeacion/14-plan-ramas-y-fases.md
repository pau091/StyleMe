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
| Generación de embeddings e indexación vectorial | 9 | 12,0 % | 3 · `feature/base-de-datos`, 6 · `feature/embeddings-busqueda` |
| Búsqueda semántica y recuperación vectorial | 9 | 12,0 % | 6 · `feature/embeddings-busqueda` |
| Implementación de arquitectura RAG | 9 | 12,0 % | 7 · `feature/rag-lia` |
| Automatización e integración de servicios de IA | 8 | 10,7 % | 10 · `feature/n8n-docker` |
| Calidad de respuestas, validación y pruebas de IA | 8 | 10,7 % | 8 · `test/evaluacion-ia` |
| Análisis de datos e indicadores | 8 | 10,7 % | 3 · `feature/base-de-datos`, 11 · `docs/entrega-final` |
| Integración funcional de la aplicación | 7 | 9,3 % | 9 · `feature/iu-api`, 10 · `feature/n8n-docker` |
| Documentación, control de versiones y sustentación | 7 | 9,3 % | Todas + 11 · `docs/entrega-final` |

> 🎯 **La IA vale el 57 %** (embeddings + búsqueda + RAG + automatización + pruebas). Por eso la **Fase 3** es la más importante.

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
4. Si la IA ayudó en esa rama, actualiza `AI_USAGE.md` en un commit de esa misma rama.
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
  | `style` | Formato o diseño visual sin cambiar la lógica |
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
1. **Compare & pull request** → **base: `develop`** ← **compare: `NOMBRE-DE-LA-RAMA`**.
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

**Paso 6 · Fin de FASE: `develop → main`**
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

> ⚠️ El PR #1 se unió a `main` en vez de `develop`. Se corrige poniendo `develop` al día con `main` (ver el chat del 2026-11-23).
> Desde ahora, en cada PR revisa que diga **base: `develop`**.

#### Rama 2 · `refactor/estructura-proyecto`

| # | Qué se hace | Commit |
|---|---|---|
| 0a ✅ | Modelo de datos | `docs(planeacion): agregar modelo de datos con pgvector` |
| 0b ✅ | Plan de ramas alineado con la rúbrica | `docs(planeacion): alinear plan de ramas con la rúbrica` |
| 0c ✅ | Guía de SDKMAN y decisiones D-049 a D-056 | `docs: agregar guía de sdkman y decisiones D-049 a D-056` |
| 0d ✅ | `planeacion/16-arquitectura-hexagonal.md` + plan 14 + D-057 + README | `docs(arquitectura): definir arquitectura hexagonal del backend` |
| 1 ✅ | Mover páginas HTML, `assets/` y `data/` a `frontend/` | `refactor(frontend): mover el sitio a frontend/` |
| 1b | Renombrar `frontend/` → `iu/` (nombres en español, D-058) | `refactor(iu): renombrar frontend a iu` |
| 2 | `planeacion/` → `docs/planeacion/`; crear `docs/diagramas/`, `docs/pruebas/`, `docs/evidencias/`, `docs/informe-datos/` | `docs: crear estructura de documentación` |
| 3 | `AI_USAGE.md` en la raíz con todo lo hecho con IA hasta hoy | `docs: agregar AI_USAGE.md` |
| 4 | `.env.ejemplo` → `.env.example`; carpetas `backend/`, `basedatos/`, `n8n/` con `LEEME.md`; `.gitignore` para Java y Docker | `chore: preparar estructura de backend, base de datos y n8n` |
| 5 | README con la nueva estructura | `docs: actualizar README con la estructura del proyecto` |

➡️ **Fin de Fase 0:** PR `develop → main` → `release: fase 0 - organización`.

---

### FASE 1 · Base de datos → `v0.2`

#### Rama 3 · `feature/base-de-datos`
*Criterios: embeddings e indexación vectorial (base), análisis de datos.*

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Diagrama entidad-relación en `docs/diagramas/` (DBML + PNG) | `docs(db): agregar diagrama entidad-relación` |
| 2 | `basedatos/01-esquema.sql`: extensión **pgvector**, 16 tablas, llaves, `CHECK` | `feat(db): crear esquema con pgvector` |
| 3 | Índices, incluido el índice vectorial **HNSW** de `fragmento_conocimiento` | `feat(db): agregar índices e índice vectorial hnsw` |
| 4 | `basedatos/02-datos-iniciales.sql`: 10 tiendas y 49 productos reales | `feat(db): cargar datos iniciales del catálogo` |
| 5 | `basedatos/03-datos-prueba.sql`: usuarios, reseñas, favoritos, eventos de prueba (marcados) | `feat(db): agregar datos de prueba para el análisis` |
| 6 | `basedatos/04-vistas-indicadores.sql`: vistas de los indicadores | `feat(db): agregar vistas de indicadores` |

✅ **Terminada cuando:** los scripts corren sin errores en una base vacía (con Docker: imagen `pgvector/pgvector`).

---

### FASE 2 · Backend → `v0.3`

#### Rama 4 · `feature/backend-api`
*Orden hexagonal: primero el centro, después los adaptadores.*

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Spring Initializr en `backend/` (Web, JPA, PostgreSQL, Validation) + `.sdkmanrc` + paquetes `dominio/`, `aplicacion/`, `infraestructura/` | `build(backend): crear proyecto spring boot con estructura hexagonal` |
| 2 | `ArquitecturaHexagonalTest` con ArchUnit (vigila las reglas R1, R2, R3 y R7) | `test(arquitectura): agregar reglas hexagonales con archunit` |
| 3 | **Dominio** del catálogo: Tienda, Producto, Categoria, Zona, Ingrediente, Etiqueta + excepciones + pruebas | `feat(dominio): agregar modelo del catálogo` |
| 4 | **Puertos** (in/out) y **casos de uso** del catálogo + pruebas con puertos falsos | `feat(aplicacion): agregar casos de uso del catálogo` |
| 5 | **Adaptador de salida** JPA: entidades, repositorios Spring Data, mappers | `feat(persistencia): agregar adaptador jpa del catálogo` |
| 6 | **Adaptador de entrada** REST: controladores, DTO, `ManejadorErrores`, `ConfiguracionBeans` | `feat(api): agregar endpoints de tiendas y productos` |
| 7 | Reseñas y favoritos: dominio (corazones 1–5, una de dos) + casos de uso | `feat(dominio): agregar reglas de reseñas y favoritos` |
| 8 | Reseñas y favoritos: adaptadores JPA y REST | `feat(api): agregar endpoints de reseñas y favoritos` |
| 9 | Analítica: eventos e indicadores (dominio → casos de uso → adaptadores que leen las vistas SQL) | `feat(analitica): agregar eventos e indicadores` |
| 10 | Swagger con springdoc-openapi | `docs(api): agregar documentación swagger` |

#### Rama 5 · `feature/autenticacion`

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Dominio Usuario, Rol, Correo + puertos `CifradorContrasenaPuerto`, `GeneradorTokenPuerto` + casos de uso de registro e inicio de sesión | `feat(usuario): agregar registro e inicio de sesión` |
| 2 | Adaptadores `BCryptCifradorAdaptador` y `JwtGeneradorTokenAdaptador` | `feat(seguridad): agregar adaptadores bcrypt y jwt` |
| 3 | `ConfiguracionSeguridad` + filtro JWT + permisos por rol | `feat(seguridad): proteger endpoints por rol` |
| 4 | Pruebas de endpoints protegidos | `test(seguridad): agregar pruebas de seguridad` |

➡️ **Fin de Fase 2:** `release: fase 2 - backend`.

---

### FASE 3 · Inteligencia artificial (la más importante) → `v0.4`

#### Rama 6 · `feature/embeddings-busqueda`
*Criterios: embeddings e indexación vectorial (9) + búsqueda semántica y recuperación vectorial (9).*

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Dominio: `FragmentoConocimiento`, `Embedding`, `FragmentadorConocimiento` (chunking de tiendas, productos y guías) + pruebas | `feat(dominio): agregar fragmentos de conocimiento y chunking` |
| 2 | Puertos `EmbeddingPuerto`, `AlmacenVectorialPuerto` + `IndexarConocimientoServicio` (recalcula solo si cambió el hash) | `feat(ia): agregar caso de uso de indexación` |
| 3 | `GeminiEmbeddingAdaptador` (clave en variable de entorno) | `feat(ia): agregar adaptador de embeddings de gemini` |
| 4 | `PgVectorAdaptador`: guardar vectores y buscar con `<=>` sobre el índice HNSW | `feat(ia): agregar adaptador pgvector` |
| 5 | `BuscarSemanticoServicio`: top-k + umbral + filtros (búsqueda **híbrida**) | `feat(ia): agregar búsqueda semántica híbrida` |
| 6 | Adaptadores de entrada: `BusquedaControlador`, `AdminIndexacionControlador`, `ReindexacionProgramador` | `feat(api): agregar endpoints de búsqueda y reindexación` |
| 7 | Pruebas con Testcontainers (pgvector real): "crema para piel grasa" trae productos de piel grasa | `test(ia): agregar pruebas de búsqueda semántica` |

#### Rama 7 · `feature/rag-lia`
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

#### Rama 8 · `test/evaluacion-ia`
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

### FASE 4 · Integración y automatización → `v0.5`

#### Rama 9 · `feature/iu-api`
*Criterio: integración funcional (7).*

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Módulo que llama a la API | `feat(iu): agregar cliente de la api` |
| 2 | Tiendas, perfil y productos desde la API | `feat(iu): cargar tiendas y productos desde la api` |
| 3 | El buscador de productos usa la **búsqueda semántica** | `feat(iu): usar búsqueda semántica en el catálogo` |
| 4 | Inicio de sesión y registro | `feat(iu): agregar inicio de sesión` |
| 5 | Reseñas y favoritos | `feat(iu): agregar reseñas y favoritos` |
| 6 | Registro de eventos | `feat(iu): registrar eventos de uso` |
| 7 | Panel de admin con gráficos de indicadores | `feat(iu): agregar panel de indicadores` |
| 8 | Borrar `iu/data/*.js` | `chore(iu): eliminar datos locales` |

#### Rama 10 · `feature/n8n-docker`
*Criterios: automatización e integración de servicios de IA (8), integración funcional.*

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Dockerfiles del backend y de la iu (nginx) | `build(docker): agregar dockerfiles` |
| 2 | `docker-compose.yml`: pgvector, backend, iu, n8n | `build(docker): agregar docker compose` |
| 3 | `.env.example` con todas las variables | `chore: completar variables de entorno` |
| 4 | Adaptadores n8n: `N8nNotificadorAdaptador` (salida) y `ModeracionWebhookControlador` (entrada) | `feat(n8n): agregar adaptadores de n8n` |
| 4b | n8n: **moderación de reseñas con Gemini** → cambia el estado a PUBLICADA/RECHAZADA | `feat(n8n): agregar moderación de reseñas con ia` |
| 5 | n8n: producto creado o editado → **reindexar su embedding** | `feat(n8n): agregar reindexación automática de embeddings` |
| 6 | n8n: **reporte semanal** de indicadores con resumen escrito por IA | `feat(n8n): agregar reporte semanal con ia` |
| 7 | Exportar los workflows a `n8n/*.json` | `chore(n8n): exportar workflows` |
| 8 | Borrar `netlify.toml` | `chore: eliminar configuración de netlify` |

✅ **Terminada cuando:** `docker compose up` levanta todo en un computador limpio.

---

### FASE 5 · Entrega → `v1.0`

#### Rama 11 · `docs/entrega-final`

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Diagrama de arquitectura (con el flujo RAG) | `docs: agregar diagrama de arquitectura` |
| 2 | Colección de Postman + capturas de Swagger | `docs: agregar evidencias de la api` |
| 3 | Documento de pruebas | `docs(pruebas): agregar documento de pruebas` |
| 4 | Informe de análisis de datos (indicadores, SQL, interpretación, ≥3 conclusiones) | `docs: agregar informe de análisis de datos` |
| 5 | README final | `docs: completar README` |
| 6 | `AI_USAGE.md` final | `docs: completar AI_USAGE.md` |
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
| 1 · Base de datos | `feature/base-de-datos` | v0.2 |
| 2 · Backend | `feature/backend-api`, `feature/autenticacion` | v0.3 |
| 3 · Inteligencia artificial | `feature/embeddings-busqueda`, `feature/rag-lia`, `test/evaluacion-ia` | v0.4 |
| 4 · Integración y automatización | `feature/iu-api`, `feature/n8n-docker` | v0.5 |
| 5 · Entrega | `docs/entrega-final` | v1.0 |

**Total:** 2 permanentes + 11 de trabajo + las `fix/...` que salgan.
