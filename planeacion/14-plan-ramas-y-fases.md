# 14 · Plan de ramas y fases de la entrega final

> Cómo se organiza el trabajo en Git para cumplir los requisitos de la entrega final (D-045):
> historial de commits progresivo, ramas para funcionalidades y correcciones, y merges con pull requests (PR).
>
> ⚠️ Las tecnologías marcadas 💡 son **sugerencias** hasta que se confirmen (PostgreSQL, Java 21).

---

## 1. La idea general

```
main      ●────────────────●────────────────●──────────── ...   (solo versiones terminadas)
           \              /                /
develop     ●──●──●──●──●────●──●──●──●──●──────────────── ...   (aquí se junta todo)
               \    /         \      /
ramas de        ●──●           ●──●──●                           (aquí se trabaja)
trabajo    chore/limpieza    feature/base-de-datos
```

| Rama | Para qué | Regla |
|---|---|---|
| `main` | Versión terminada de cada fase | **Nunca** se trabaja directo. Solo recibe PR desde `develop` al final de cada fase |
| `develop` | Junta el trabajo terminado de cada rama | **Nunca** se trabaja directo. Solo recibe PR de las ramas de trabajo |
| `feature/...` `refactor/...` `chore/...` `docs/...` | Una por área grande del proyecto | Nace de `develop`, se une a `develop` con PR y se borra |
| `fix/...` | Corregir un error que apareció | Igual que las anteriores |

**Reglas de oro**
1. Una rama a la vez: no empieces la siguiente hasta hacer el merge de la anterior.
2. Haz commit **cada vez que termines una parte** (varias veces por semana), no todo al final.
3. Mensajes de commit con el formato que ya usas: `tipo: :emoji: qué cambió`.
4. Si la IA ayudó en esa rama, actualiza `AI_USAGE.md` en un commit de esa misma rama.

---

## 2. Configuración inicial (se hace UNA sola vez)

```
git checkout main
git pull origin main
git checkout -b develop
git push -u origin develop
```

Con esto `develop` ya existe en GitHub.

**Proteger las ramas (recomendado):** en GitHub → **Settings → Branches → Add branch ruleset** (o *Add rule*):
- Nombre de la rama: `main` → marcar **Require a pull request before merging**.
- Repetir con `develop`.

Así GitHub no deja subir cambios directo a esas ramas, solo por PR.

---

## 3. Receta para CADA rama (se repite igual siempre)

**Paso 1 · Partir de `develop` actualizado**
```
git checkout develop
git pull origin develop
git checkout -b NOMBRE-DE-LA-RAMA
```

**Paso 2 · Trabajar y hacer commits por partes** (los de cada rama están en la sección 4)
```
git status                         ← ver qué cambió
git add ARCHIVOS-O-CARPETAS        ← solo lo de esa parte
git commit -m "tipo: :emoji: mensaje"
```

**Paso 3 · Subir la rama a GitHub** (la primera vez con `-u`; después basta `git push`)
```
git push -u origin NOMBRE-DE-LA-RAMA
```
Puedes subir varias veces mientras trabajas; así queda copia en GitHub.

**Paso 4 · Abrir el pull request** en GitHub
1. Botón **Compare & pull request** (o pestaña *Pull requests → New pull request*).
2. **base: `develop`** ← **compare: `NOMBRE-DE-LA-RAMA`**.
3. Título y descripción con esta plantilla:
   ```
   ## Qué hace
   - ...
   ## Cómo probarlo
   - ...
   ## Uso de IA
   - (qué se hizo con ayuda de IA o "No se usó IA")
   ```
4. **Merge pull request → Create a merge commit** (no "Squash": se perderían los commits).
5. **Delete branch**.

**Paso 5 · Limpiar en tu computador**
```
git checkout develop
git pull origin develop
git branch -d NOMBRE-DE-LA-RAMA
```

**Paso 6 · Al terminar una FASE: llevar `develop` a `main`**
1. En GitHub: *New pull request* → **base: `main`** ← **compare: `develop`**.
2. Título: `Fase N: nombre de la fase`. Merge con *Create a merge commit* (**no** borres `develop`).
3. (Opcional) Marcar la versión: *Releases → Draft a new release* → etiqueta `v0.N` sobre `main`.

---

## 4. Las fases y qué sube cada rama

### FASE 0 · Organización → al terminar: `develop → main` (`v0.1`)

#### Rama 1 · `chore/limpieza-archivos`
**Objetivo:** quitar lo que no se usa y separar Lía 2D / 3D. *(Ya está hecho, falta subirlo.)*

| # | Qué agregar | Commit |
|---|---|---|
| 1 | `git add "assets/img/Screenshot 2026-11-17 081556.png" assets/img/logos assets/img/lia/lia-retrato.jpg assets/js/inicio.js assets/css/estilos.css` | `chore: :fire: eliminar imágenes sin uso` |
| 2 | `git add assets/img/lia assets/lia assets/js/lia assets/css/lia.css *.html` | `refactor: :truck: separar Lía en carpetas 2d y 3d` |
| 3 | `git add planeacion README.md` | `docs: :memo: borrar plan de entrega y agregar plan de ramas` |

✅ **Terminada cuando:** `git status` no muestra nada pendiente y la página abre con Lía en 2D.

#### Rama 2 · `refactor/estructura-proyecto`
**Objetivo:** dejar el repositorio con la estructura de la entrega.

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Mover páginas HTML, `assets/` y `data/` a `frontend/`; corregir rutas | `refactor: :truck: mover el sitio a frontend/` |
| 2 | Mover `planeacion/` a `docs/planeacion/`; crear `docs/diagramas/`, `docs/pruebas/`, `docs/evidencias/`, `docs/informe-datos/` | `docs: :file_folder: crear estructura de documentación` |
| 3 | Crear `AI_USAGE.md` en la raíz con todo lo hecho con IA hasta hoy | `docs: :memo: agregar AI_USAGE.md` |
| 4 | Renombrar `.env.ejemplo` → `.env.example`; crear `backend/`, `database/`, `n8n/` con un `LEEME.md`; completar `.gitignore` (Java, Docker) | `chore: :construction: base para backend, base de datos y n8n` |
| 5 | Actualizar el README con la nueva estructura | `docs: :memo: actualizar README con la estructura` |

✅ **Terminada cuando:** la página sigue funcionando desde `frontend/` y existe `AI_USAGE.md`.
➡️ **Fin de la Fase 0:** PR `develop → main` "Fase 0: limpieza y organización".

---

### FASE 1 · Base de datos → al terminar: `develop → main` (`v0.2`)

#### Rama 3 · `feature/base-de-datos`
**Objetivo:** pasar los datos de `data/*.js` a SQL y preparar los datos para el informe.

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Diagrama entidad-relación (tablas: usuario, tienda, producto, reseña, favorito, evento, conversación de Lía) en `docs/diagramas/` | `docs: :card_file_box: diagrama entidad-relación` |
| 2 | `database/01-esquema.sql`: crear tablas, llaves y relaciones (💡 PostgreSQL) | `feat: :card_file_box: esquema de la base de datos` |
| 3 | `database/02-datos-iniciales.sql`: las 10 tiendas y los 49 productos reales | `feat: :seedling: datos iniciales de tiendas y productos` |
| 4 | `database/03-datos-prueba.sql`: usuarios, reseñas y eventos de prueba (marcados como tales) | `feat: :seedling: datos de prueba para el análisis` |
| 5 | `database/04-vistas-indicadores.sql`: primeras vistas/consultas de indicadores | `feat: :bar_chart: vistas para los indicadores` |

✅ **Terminada cuando:** los scripts corren de principio a fin sin errores en una base vacía.

---

### FASE 2 · Backend → al terminar: `develop → main` (`v0.3`)

#### Rama 4 · `feature/backend-api`
**Objetivo:** API REST en Spring Boot conectada a la base de datos.

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Proyecto creado con Spring Initializr en `backend/` (💡 Java 21, Web, JPA, PostgreSQL, Validation) | `feat: :tada: proyecto Spring Boot` |
| 2 | Entidades y repositorios | `feat: :sparkles: entidades y repositorios` |
| 3 | Tiendas y productos: servicios + controladores (con filtros) | `feat: :sparkles: API de tiendas y productos` |
| 4 | Reseñas y favoritos | `feat: :sparkles: API de reseñas y favoritos` |
| 5 | Registro de eventos (vistas, clics, búsquedas) | `feat: :sparkles: API de eventos` |
| 6 | Swagger con springdoc-openapi | `docs: :memo: documentación Swagger` |
| 7 | Pruebas unitarias (JUnit + Mockito) | `test: :white_check_mark: pruebas de servicios` |

#### Rama 5 · `feature/autenticacion`
**Objetivo:** registro, inicio de sesión y roles.

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Spring Security + registro e inicio de sesión | `feat: :lock: registro e inicio de sesión` |
| 2 | Token JWT | `feat: :closed_lock_with_key: autenticación con JWT` |
| 3 | Permisos por rol (comprador, comerciante, admin) | `feat: :lock: permisos por rol` |
| 4 | Pruebas de los endpoints protegidos | `test: :white_check_mark: pruebas de seguridad` |

---

### FASE 3 · Integración → al terminar: `develop → main` (`v0.4`)

#### Rama 6 · `feature/frontend-api`
**Objetivo:** que la página use la API en vez de `data/*.js`.

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Módulo que hace las llamadas a la API | `feat: :sparkles: cliente de la API en el frontend` |
| 2 | Tiendas, perfil y productos leen de la API | `feat: :sparkles: tiendas y productos desde la API` |
| 3 | Páginas de inicio de sesión y registro | `feat: :sparkles: inicio de sesión en el frontend` |
| 4 | Reseñas y favoritos en la página | `feat: :sparkles: reseñas y favoritos` |
| 5 | Registro de eventos desde la página | `feat: :chart_with_upwards_trend: registrar eventos de uso` |
| 6 | Panel de admin con gráficos de indicadores | `feat: :bar_chart: panel de indicadores` |
| 7 | Borrar `frontend/data/*.js` (ya reemplazado por SQL) | `chore: :fire: eliminar datos locales` |

#### Rama 7 · `feature/lia-backend`
**Objetivo:** pasar el cerebro de Lía de Netlify a Spring Boot.

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Endpoint `/api/lia/chat` que llama a Gemini (clave en variable de entorno) | `feat: :robot: Lía en el backend` |
| 2 | Lía busca solo productos reales del catálogo (base de datos) | `feat: :robot: Lía consulta el catálogo` |
| 3 | Guardar conversaciones (sin datos personales) para el análisis | `feat: :card_file_box: registro de conversaciones de Lía` |
| 4 | El frontend usa el nuevo endpoint; se mantiene el plan B | `refactor: :recycle: Lía usa el backend` |
| 5 | Borrar `netlify/functions/lia.mjs` | `chore: :fire: eliminar función de Netlify` |

---

### FASE 4 · Automatización e infraestructura → al terminar: `develop → main` (`v0.5`)

#### Rama 8 · `feature/n8n-docker`

| # | Qué se hace | Commit |
|---|---|---|
| 1 | `Dockerfile` del backend y del frontend (nginx) | `build: :whale: Dockerfiles` |
| 2 | `docker-compose.yml`: base de datos, backend, frontend, n8n | `build: :whale: Docker Compose` |
| 3 | `.env.example` con todas las variables | `chore: :wrench: variables de entorno` |
| 4 | Workflow n8n: aviso de reseña nueva → exportado a `n8n/` | `feat: :zap: workflow de reseñas` |
| 5 | Workflow n8n: moderación de reseñas con IA | `feat: :zap: workflow de moderación` |
| 6 | Workflow n8n: reporte semanal de indicadores | `feat: :zap: workflow de reporte` |
| 7 | Borrar `netlify.toml` si ya no se publica en Netlify | `chore: :fire: eliminar configuración de Netlify` |

✅ **Terminada cuando:** `docker compose up` levanta todo en un computador limpio.

---

### FASE 5 · Entrega → al terminar: `develop → main` (`v1.0`)

#### Rama 9 · `docs/entrega-final`

| # | Qué se hace | Commit |
|---|---|---|
| 1 | Diagrama de arquitectura en `docs/diagramas/` | `docs: :memo: diagrama de arquitectura` |
| 2 | Colección de Postman + capturas de Swagger en `docs/evidencias/` | `docs: :camera_flash: evidencias de la API` |
| 3 | Documento de pruebas en `docs/pruebas/` | `docs: :white_check_mark: documento de pruebas` |
| 4 | Informe de análisis de datos (indicadores, consultas SQL, interpretación, ≥3 conclusiones) en `docs/informe-datos/` | `docs: :bar_chart: informe de análisis de datos` |
| 5 | README final (cómo instalar, correr y probar) | `docs: :memo: README final` |
| 6 | `AI_USAGE.md` final | `docs: :memo: AI_USAGE.md final` |
| 7 | Presentación final | `docs: :memo: presentación final` |

---

### Ramas `fix/...` (cuando aparezca un error)

En cualquier momento, con la misma receta de la sección 3. Ejemplos: `fix/rutas-frontend`, `fix/filtro-productos`, `fix/cors-api`.
Commit: `fix: :bug: qué se corrigió`. Lo ideal es que haya al menos 2 o 3 en todo el proyecto.

---

## 5. Resumen

| Fase | Ramas | Versión en `main` |
|---|---|---|
| 0 · Organización | `chore/limpieza-archivos`, `refactor/estructura-proyecto` | v0.1 |
| 1 · Base de datos | `feature/base-de-datos` | v0.2 |
| 2 · Backend | `feature/backend-api`, `feature/autenticacion` | v0.3 |
| 3 · Integración | `feature/frontend-api`, `feature/lia-backend` | v0.4 |
| 4 · Automatización | `feature/n8n-docker` | v0.5 |
| 5 · Entrega | `docs/entrega-final` | v1.0 |

**Total:** 2 permanentes + 9 de trabajo + las `fix/...` que salgan.

> 👥 Si el equipo tiene más integrantes, cada rama se asigna a una persona y esa persona hace los commits desde **su** cuenta. Se actualiza esta tabla con una columna "Responsable".
