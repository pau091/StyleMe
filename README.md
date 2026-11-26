# StyleMe

Plataforma para descubrir tiendas físicas de moda y belleza de Bucaramanga (Santander, Colombia): dónde quedan,
cómo contactarlas, cómo armar una salida de compras, un probador virtual y **Lía**, una asesora con inteligencia
artificial que recomienda productos reales del catálogo usando **embeddings, búsqueda semántica y RAG**.

> Proyecto académico · Estado: **Fase 0 completa** (organización). En construcción: base de datos vectorial,
> backend y módulos de IA. Plan completo en [docs/planeacion/14-plan-ramas-y-fases.md](docs/planeacion/14-plan-ramas-y-fases.md).

---

## Arquitectura

```
   iu/ (página web)                           n8n (automatizaciones)
        │  REST                                      │  webhooks
        ▼                                            ▼
 ┌──────────────────────── backend/ (Spring Boot) ────────────────────────┐
 │  infraestructura  →  aplicacion  →  dominio      (arquitectura hexagonal) │
 └───────────┬──────────────────────┬───────────────────────┬─────────────┘
             ▼                      ▼                       ▼
   PostgreSQL + pgvector     Gemini (embeddings      n8n (avisos y
   (datos + índice HNSW)      y respuestas de Lía)     reportes con IA)
```

- **Arquitectura hexagonal** (puertos y adaptadores): [docs/planeacion/16-arquitectura-hexagonal.md](docs/planeacion/16-arquitectura-hexagonal.md)
- **Modelo de datos** (16 tablas, con búsqueda vectorial): ver la sección siguiente

---

## Modelo de datos (diagrama entidad-relación)

**16 tablas** en PostgreSQL + pgvector, organizadas en 4 grupos y **normalizadas hasta la cuarta forma normal (4FN)**.

```mermaid
erDiagram
    categoria ||--o{ tienda : "categoría principal"
    zona |o--o{ tienda : "ubica"
    usuario |o--o{ tienda : "comerciante"
    tienda ||--o{ producto : "vende"
    categoria ||--o{ producto : "clasifica"
    producto ||--o{ producto_ingrediente : "contiene"
    ingrediente ||--o{ producto_ingrediente : "aparece en"
    producto ||--o{ producto_etiqueta : "se describe con"
    etiqueta ||--o{ producto_etiqueta : "describe"
    usuario ||--o{ resena : "escribe"
    tienda |o--o{ resena : "recibe"
    producto |o--o{ resena : "recibe"
    usuario ||--o{ favorito : "guarda"
    tienda |o--o{ favorito : "es guardada"
    producto |o--o{ favorito : "es guardado"
    usuario |o--o{ evento : "genera"
    tienda |o--o{ evento : "aparece en"
    producto |o--o{ evento : "aparece en"
    categoria |o--o{ evento : "aparece en"
    usuario |o--o{ conversacion_lia : "conversa"
    categoria |o--o{ conversacion_lia : "tema"
    conversacion_lia ||--o{ lia_recomendacion : "recomienda"
    producto ||--o{ lia_recomendacion : "es recomendado"
    tienda |o--o{ fragmento_conocimiento : "se describe en"
    producto |o--o{ fragmento_conocimiento : "se describe en"
    evaluacion_ia {
        bigint id PK
        varchar pregunta
        numeric precision_k
        boolean sin_alucinacion
    }
    fragmento_conocimiento {
        bigint id PK
        text texto
        char hash_texto
        vector embedding "768 dim, indice HNSW"
    }
```

| Grupo | Tablas | Para qué |
|---|---|---|
| **Catálogo** | `categoria`, `zona`, `tienda`, `producto`, `ingrediente`, `producto_ingrediente`, `etiqueta`, `producto_etiqueta` | Tiendas y productos reales, con ingredientes y características |
| **Comunidad** | `usuario`, `resena`, `favorito` | Usuarios con roles, reseñas de 1 a 5 corazones y favoritos (de tiendas y productos) |
| **Análisis** | `evento`, `conversacion_lia`, `lia_recomendacion` | Lo que pasa en la página y con Lía, para el informe de indicadores |
| **IA (RAG)** | `fragmento_conocimiento`, `evaluacion_ia` | Textos con su **embedding** (`vector(768)`, índice **HNSW**) y resultados de las pruebas de IA |

**Normalización:**
- **1FN:** sin listas en un campo (los ingredientes van en `producto_ingrediente`).
- **2FN:** en las tablas con llave compuesta, cada dato depende de la llave completa (`orden` depende del producto **y** del ingrediente).
- **3FN:** sin dependencias transitivas (`tienda` guarda `zona_id`, no el nombre de la zona).
- **4FN:** ingredientes y etiquetas son hechos independientes, por eso van en tablas separadas.

Diagrama completo con todos los campos y reglas: [docs/diagramas/entidad-relacion.dbml](docs/diagramas/entidad-relacion.dbml) ·
Explicación detallada: [docs/diagramas/entidad-relacion.md](docs/diagramas/entidad-relacion.md) ·
Diseño de cada campo: [docs/planeacion/15-modelo-de-datos.md](docs/planeacion/15-modelo-de-datos.md)

---

## Estructura del proyecto

```
StyleMe/
├── iu/                     Interfaz de usuario: la página web
│   ├── *.html              Inicio, tiendas, productos, mapa, salida, probador, recomendador, favoritos, acerca y admin
│   ├── Dockerfile          Imagen con nginx (sirve la página y reenvía /api/ al backend)
│   └── assets/
│       ├── css/            Estilos, animaciones e intro
│       ├── js/             api.js (conexión con el backend), comun.js, un archivo por página; lia/ (personaje y plan B)
│       ├── lia/            Lía: retrato 2D con sus expresiones
│       └── img/            Fotos de tiendas, categorías y decoración
│
├── backend/                API Spring Boot con arquitectura hexagonal
│   └── src/main/java/co/styleme/
│       ├── dominio/        Modelo y reglas del negocio (Java puro)
│       ├── aplicacion/     Puertos y casos de uso (Java puro)
│       └── infraestructura/ Adaptadores: REST, PostgreSQL, pgvector, Gemini, n8n
│
├── basedatos/              Scripts SQL: esquema, índices (HNSW), datos iniciales y vistas
├── n8n/                    Workflows de n8n (moderación con IA, reindexación, reporte semanal)
│
├── docs/
│   ├── planeacion/         Visión, decisiones, plan de ramas, modelo de datos y arquitectura
│   ├── diagramas/          Entidad-relación y arquitectura
│   ├── pruebas/            Documento de pruebas e informe de calidad de la IA
│   ├── evidencias/         Swagger y Postman
│   └── informe-datos/      Informe de análisis de datos
│
├── .env.example            Variables de entorno (copiar como .env)
├── IAS_usadas.md           Registro del uso de inteligencia artificial (AI_USAGE)
└── README.md
```

Cada carpeta tiene su propio `README.md` con el detalle.

---

## Tecnologías

| Parte | Herramienta |
|---|---|
| Interfaz de usuario | HTML, CSS y JavaScript |
| Backend | Java 21 · Spring Boot · Maven · arquitectura hexagonal |
| Base de datos | PostgreSQL + **pgvector** (índice vectorial HNSW) |
| IA de Lía | [Gemini API](https://ai.google.dev/): embeddings (`gemini-embedding-001`) y modelo de lenguaje, con RAG |
| Automatización | [n8n](https://n8n.io/) |
| Contenedores | Docker y Docker Compose |
| Pruebas | JUnit, Mockito, ArchUnit, Testcontainers, Postman |
| Documentación de la API | Swagger (springdoc-openapi) |
| Mapas | [Leaflet](https://leafletjs.com/) + [OpenStreetMap](https://www.openstreetmap.org/) |
| Probador virtual | [MediaPipe](https://ai.google.dev/edge/mediapipe/solutions/guide) Pose Landmarker e Image Segmenter (corren en el navegador, también con la cámara) + probador con IA opcional (OpenAI o Gemini) |

---

## Requisitos para trabajar en el proyecto

StyleMe funciona igual en **Windows, Mac (Intel o Apple M1/M2/M3) y Linux**: todo corre en Docker
(las imágenes tienen versión para procesadores Intel/AMD y ARM) y la página se usa desde cualquier navegador
moderno en computador, tableta o celular.

| Herramienta | Versión | Para qué | Windows | Mac | Linux |
|---|---|---|---|---|---|
| Docker | reciente | Base de datos, backend, página y n8n | [Docker Desktop](https://www.docker.com/products/docker-desktop/) | [Docker Desktop](https://www.docker.com/products/docker-desktop/) (elige Apple chip o Intel) | Docker Engine + plugin `docker compose` ([guía](https://docs.docker.com/engine/install/)) |
| Java (Temurin) | 21 | Solo para **programar** el backend | SDKMAN en Git Bash (abajo) | SDKMAN en la Terminal | SDKMAN en la terminal |
| Git | reciente | Clonar y subir cambios | [Git for Windows](https://git-scm.com/download/win) (trae Git Bash) | `xcode-select --install` | `sudo apt install git` (o el gestor de tu distribución) |
| Python | 3.10 o superior | Opcional: regenerar los workflows de n8n o servir la página sin VS Code | [python.org](https://www.python.org/downloads/) | Ya viene (`python3`) | Ya viene (`python3`) |
| VS Code | reciente | Editor (opcional) | Extensiones: Extension Pack for Java, Spring Boot Extension Pack, Docker, Live Server | igual | igual |

> 🐳 Para **correr** el proyecto solo hace falta Docker: la imagen del backend trae su propio Java.

---

## Java 21 con SDKMAN (solo para programar el backend)

Si ya tienes Java 21 y la variable `JAVA_HOME` apunta a él, no necesitas nada más.
[SDKMAN](https://sdkman.io/) instala Java y permite **cambiar de versión**.

### 1. Instalar SDKMAN (una sola vez)
- **Mac y Linux:** en la Terminal ejecuta `curl -s "https://get.sdkman.io" | bash` y abre una terminal nueva.
- **Windows:** SDKMAN no funciona en PowerShell, se usa en **Git Bash**. Antes, descarga `zip` y `bzip2` de
  [GnuWin32](https://gnuwin32.sourceforge.net/packages/zip.htm) y copia `zip.exe` y `bzip2.dll` en
  `C:\Program Files\Git\usr\bin`. Luego, en Git Bash: `curl -s "https://get.sdkman.io" | bash`.

Comprueba con `sdk version`.

### 2. Instalar y cambiar la versión de Java
```bash
sdk list java                  # busca una "21.x.x-tem" (Temurin)
sdk install java 21.x.x-tem    # cambia 21.x.x por la versión exacta
sdk default java 21.x.x-tem    # versión por defecto
cd backend && sdk env          # usa la versión de backend/.sdkmanrc
java -version                  # confirmar
```

### 3. Solo en Windows: que VS Code y PowerShell vean ese Java
SDKMAN solo cambia la versión dentro de Git Bash. Crea la variable de entorno **`JAVA_HOME`**:
*Inicio → "Editar las variables de entorno del sistema" → Variables de entorno → Nueva (usuario)*,
con el valor `C:\Users\TU_USUARIO\.sdkman\candidates\java\21.x.x-tem`; agrega `%JAVA_HOME%\bin` al `Path` y reinicia VS Code.
En Mac y Linux SDKMAN configura `JAVA_HOME` solo.

---

## Cómo ejecutar

### 1. Variables de entorno
Copia `.env.example` con el nombre **`.env`** y llena los valores (contraseña de la base de datos, clave de Gemini y
clave de administración):

| Mac y Linux | Windows (PowerShell) |
|---|---|
| `cp .env.example .env` | `Copy-Item .env.example .env` |

El archivo `.env` está en `.gitignore`: nunca se sube a GitHub.
La clave de Gemini se saca gratis en [Google AI Studio](https://aistudio.google.com/apikey).
⚠️ Es secreta: **nunca** la pegues en un archivo `.js`, `.html` o `.java`.

### 2. Opción A · Todo con Docker (como se entrega)
El mismo comando en los tres sistemas:
```
docker compose up -d --build    # base de datos, backend, página y n8n
```
| Servicio | Dirección |
|---|---|
| Página | http://localhost:8085 (panel: `/admin.html`) |
| Swagger | http://localhost:8090/swagger-ui.html |
| n8n (automatizaciones con IA) | http://localhost:5678 |

La primera vez tarda unos minutos (construye las imágenes). Si la base es nueva, construye el índice de la IA
desde el panel (botón *Reindexar embeddings*) o desde Swagger. Detalle de n8n: [n8n/README.md](n8n/README.md).

**Solo en Linux:**
- Si `docker` pide `sudo`, agrega tu usuario al grupo con `sudo usermod -aG docker $USER` y vuelve a iniciar sesión.
- n8n escribe el reporte semanal en `n8n/reportes/` con el usuario 1000 del contenedor. Si tu usuario no es el 1000
  (lo ves con `id -u`), dale permiso a la carpeta: `sudo chown -R 1000:1000 n8n/reportes`.

### 2. Opción B · Para programar (backend en tu computador)
```
docker compose up -d basedatos  # en la raíz: solo PostgreSQL + pgvector
cd backend
```
| Mac y Linux | Windows (PowerShell) |
|---|---|
| `./mvnw spring-boot:run` | `.\mvnw spring-boot:run` |

El backend queda en http://localhost:8090. Si en Mac o Linux sale *"permission denied"*, dale permiso al script una
sola vez: `chmod +x mvnw`. La primera vez, construye el índice de la IA con `POST /api/admin/indexacion` desde Swagger
(http://localhost:8090/swagger-ui.html, botón **Authorize** con la clave `STYLEME_ADMIN_CLAVE`).

### 3. La página con un servidor local (cuando programas)
Con la Opción A la página ya está en http://localhost:8085. Si programas la página, ábrela con un servidor local en el
puerto **5500** (el backend acepta peticiones desde ahí). Usa módulos de JavaScript, así que **no funciona con doble clic**:

| Con VS Code (cualquier sistema) | Sin VS Code · Mac y Linux | Sin VS Code · Windows |
|---|---|---|
| **Go Live** (barra de abajo) o clic derecho en `iu/index.html` → *Open with Live Server* | `python3 -m http.server 5500 --directory iu` | `python -m http.server 5500 --directory iu` |

Queda en `http://127.0.0.1:5500/` (panel: `/admin.html`, pide la clave).

Si la IA (Gemini) no responde, Lía contesta en **modo básico** (reglas, sin IA) y el buscador busca por palabras.

### 4. Desde un celular o una tableta de tu red
Con la Opción A, abre en el celular `http://IP-DE-TU-COMPUTADOR:8085`. La IP la ves con `ipconfig` (Windows),
`ipconfig getifaddr en0` (Mac) o `hostname -I` (Linux). Todo funciona salvo la **cámara** del probador: los navegadores
solo la permiten en `localhost` o con HTTPS. Desde el celular se puede **subir una foto**.

Detalle: [basedatos/README.md](basedatos/README.md) · [backend/README.md](backend/README.md).

> **Privacidad:** Lía avisa que no se compartan datos personales (Ley 1581 de 2012). De cada conversación solo se guarda un resumen sin el texto de la persona.

---

## Cómo se trabaja con Git

- Ramas permanentes: `main` (versiones terminadas) y `develop` (integración).
- Una rama de trabajo por área (`feature/`, `fix/`, `docs/`, `refactor/`, `test/`, `chore/`), unida a `develop` con pull request.
- Mensajes con **Conventional Commits**: `tipo(alcance): descripción`.

Detalle y orden de las ramas: [docs/planeacion/14-plan-ramas-y-fases.md](docs/planeacion/14-plan-ramas-y-fases.md).

---

## Documentación

| Documento | Contenido |
|---|---|
| [01 - Visión](docs/planeacion/01-vision.md) | Qué es StyleMe, qué problemas busca resolver y para quién |
| [02 - Alcance y fases](docs/planeacion/02-alcance-y-fases.md) | Funciones confirmadas, opcionales y futuras; etapas (MVP) |
| [03 - Roles y permisos](docs/planeacion/03-roles-y-permisos.md) | Qué puede hacer cada tipo de usuario |
| [04 - Registro de decisiones](docs/planeacion/04-decisiones.md) | Decisiones tomadas, con fecha y motivo |
| [05 - Riesgos](docs/planeacion/05-riesgos.md) | Riesgos identificados y cómo mitigarlos |
| [06 - Pendientes y preguntas](docs/planeacion/06-pendientes.md) | Decisiones abiertas y preguntas por responder |
| [07 - Historias de usuario MVP 1](docs/planeacion/07-historias-mvp1.md) | Lo que debe poder hacer cada usuario en la primera versión |
| [08 - Plan de validación](docs/planeacion/08-validacion.md) | Encuesta y entrevistas para comprobar los problemas planteados |
| [10 - Proyecto completo](docs/planeacion/10-proyecto-completo.md) | Visión original: qué buscar, implementar y mejorar después |
| [11 - Contexto visual](docs/planeacion/11-contexto-visual.md) | Guía de diseño: dirección visual, animaciones y fluidez |
| [12 - Lía, asesora virtual](docs/planeacion/12-lia-asesora-virtual.md) | Lía: personaje 2D, expresiones, RAG con embeddings, conversación y cómo se evalúa |
| [14 - Plan de ramas y fases](docs/planeacion/14-plan-ramas-y-fases.md) | ⭐ Ramas, commits y orden de trabajo de la entrega final |
| [15 - Modelo de datos](docs/planeacion/15-modelo-de-datos.md) | ⭐ Las 16 tablas (con pgvector para el RAG), relaciones e indicadores |
| [16 - Arquitectura hexagonal](docs/planeacion/16-arquitectura-hexagonal.md) | ⭐ Dominio, puertos, casos de uso y adaptadores del backend |

Entregables (diagramas, pruebas, evidencias, informe de datos): [docs/](docs/).
Uso de inteligencia artificial en el desarrollo: [IAS_usadas.md](IAS_usadas.md).

### Convención usada en los documentos

- ✅ **Confirmado:** decidido por la autora del proyecto.
- 💡 **Sugerencia:** propuesta pendiente de aprobación.
- ⏳ **Pendiente:** aún no decidido.
- 🔮 **Futuro:** fuera del alcance inicial.
