# Repaso técnico del backend de StyleMe

> Guía de estudio: cómo está construido el backend, cómo funcionan los embeddings, la búsqueda semántica, el RAG de Lía,
> la base de datos y Swagger. Sirve para preparar la sustentación.
> Estado: hasta la rama 7 (`test/evaluacion-ia`).

---

## 1. Cómo se conecta todo

```
 Página (iu/)  ──HTTP/JSON──▶  Backend Spring Boot (puerto 8090)  ──▶  PostgreSQL + pgvector (Docker, 5432)
 Live Server 5500              arquitectura hexagonal                    16 tablas · índice vectorial HNSW
                                        │
                                        └──HTTPS──▶  Gemini (Google): embeddings + respuestas de Lía
```

- La **página** solo muestra datos.
- El **backend** tiene la lógica, la inteligencia artificial y guarda en secreto la clave de Gemini.
- La **base de datos** guarda los datos y los vectores (embeddings).

---

## 2. Lenguajes y tecnologías

| Lenguaje / tecnología | Dónde | Para qué |
|---|---|---|
| **Java 21** | `backend/` | Todo el backend |
| **Spring Boot 4.2** | `backend/` | Servidor web, conexión a la base, configuración |
| **SQL (PostgreSQL 17)** | `basedatos/*.sql` | Tablas, índices, datos y búsquedas vectoriales |
| **pgvector 0.8.7** | extensión de PostgreSQL | Guardar embeddings y buscar por similitud |
| **YAML** | `docker-compose.yml`, `application.yml` | Configuración de Docker y del backend |
| **XML** | `pom.xml` | Dependencias de Maven |
| **JSON** | API, preguntas de evaluación | Formato de intercambio de datos |
| **Markdown** | guías de conocimiento, instrucciones de Lía, documentos | Texto que lee la IA y documentación |
| **HTML, CSS, JavaScript** | `iu/` | La página (ya conectada con Lía en el backend) |

---

## 3. La base de datos

**Motor:** PostgreSQL 17 con **pgvector**, en **Docker**. Con `docker compose up -d` se enciende, y la primera vez ejecuta
solos los scripts de `basedatos/`, en orden alfabético:

| Script | Qué hace |
|---|---|
| `01-esquema.sql` | Crea las **16 tablas** con llaves, relaciones y reglas `CHECK` (corazones de 1 a 5, precio mayor que 0…) |
| `02-indices.sql` | **23 índices**, entre ellos el **HNSW**: el índice que hace rápida la búsqueda por similitud |
| `03-datos-iniciales.sql` | El catálogo real: 10 tiendas, 49 productos, 183 ingredientes y 36 etiquetas |
| `04-datos-prueba.sql` | Datos **de prueba** para el análisis (marcados como tales): 80 usuarios, unos 11.500 eventos de 900 sesiones, 310 reseñas, 350 favoritos y 240 conversaciones con Lía. Usa `setseed` para que salgan siempre los mismos números |
| `05-vistas-indicadores.sql` | **13 vistas** (consultas guardadas con nombre, `v_...`) que calculan los indicadores: conversión a WhatsApp por tienda, calificaciones, búsquedas sin resultado, horas y días de más uso, dispositivos, uso de Lía, calidad de los datos… |

> Si la base ya existía antes de la rama `feature/comunidad-analitica`, `04` y `05` se aplican a mano
> (los comandos están en [basedatos/README.md](../basedatos/README.md)).

**Las 16 tablas, en 4 grupos:**

| Grupo | Tablas |
|---|---|
| Catálogo | `tienda`, `producto`, `categoria`, `zona`, `ingrediente`, `producto_ingrediente`, `etiqueta`, `producto_etiqueta` |
| Comunidad | `usuario`, `resena`, `favorito` |
| Análisis | `evento`, `conversacion_lia`, `lia_recomendacion` |
| IA | `fragmento_conocimiento` (textos con su vector), `evaluacion_ia` (resultados de las pruebas de IA) |

**Normalizada hasta la 4FN.** Ejemplo: los ingredientes no se guardan como una lista dentro de un campo, sino en su propia
tabla (1FN); así se puede consultar *"¿qué productos tienen niacinamida?"*. Explicación completa:
[diagramas/entidad-relacion.md](diagramas/entidad-relacion.md).

**Reglas que protegen los datos.** Ejemplo: una reseña es de una tienda **o** de un producto, nunca de los dos:
`CHECK ((tienda_id IS NULL) <> (producto_id IS NULL))`. Se probaron 8 datos inválidos y la base los rechazó todos.

**Reseñas y favoritos sin registro (D-066).** La página manda un identificador anónimo de la sesión del navegador. Por cada
sesión el backend crea un usuario técnico (`<sesión>@sesion.styleme`, sin contraseña ni datos personales). Toda reseña
nueva queda **PENDIENTE** hasta que se modera (más adelante lo hará n8n con Gemini), y cada sesión puede reseñar una sola
vez cada tienda o producto (si lo intenta otra vez, la API responde **409**).

---

## 4. El backend: arquitectura hexagonal

Tres capas. Las dependencias **siempre apuntan hacia el centro**:

```
infraestructura  ──▶  aplicacion  ──▶  dominio
(Spring, base,        (puertos y       (modelo y reglas
 Gemini, REST)         casos de uso)     del negocio, Java puro)
```

| Capa | Qué tiene | Ejemplo |
|---|---|---|
| **Dominio** | Las reglas del negocio, sin frameworks | `Producto` no acepta precio ≤ 0 · `ValidadorRecomendaciones` descarta productos inventados |
| **Aplicación** | **Casos de uso** y **puertos** (interfaces) | `ChatearConLiaServicio` (el flujo RAG) · `EmbeddingPuerto` ("necesito embeddings", sin decir de quién) |
| **Infraestructura** | **Adaptadores**: cómo se habla con el mundo real | `GeminiEmbeddingAdaptador` implementa el puerto con Gemini · `PgVectorAdaptador` con pgvector |

**¿Por qué importa?** Si mañana se cambia Gemini por otra IA, solo se escribe un adaptador nuevo: el resto no se toca.
Una prueba con **ArchUnit** vigila estas reglas: si alguien usa Spring dentro del dominio, el proyecto deja de pasar las pruebas.

**Recorrido de una petición:**
```
GET /api/tiendas/bohor → TiendaControlador → ConsultarTiendasCasoUso → ConsultarTiendasServicio
→ TiendaRepositorioPuerto → TiendaPersistenciaAdaptador → PostgreSQL → (de vuelta) → JSON
```

Diseño completo: [planeacion/16-arquitectura-hexagonal.md](planeacion/16-arquitectura-hexagonal.md).

---

## 5. Embeddings

**Qué son.** Un embedding es una lista de **768 números** que representa el **significado** de un texto. Textos con significado
parecido tienen vectores que apuntan en direcciones parecidas: *"que no me brille la cara"* y *"sérum para piel grasa"*
quedan cerca aunque no compartan palabras.

**Cómo se generan** (`POST /api/admin/indexacion`):

1. **Fragmentación (chunking):** cada tienda, cada producto real y cada sección de las 3 guías se convierte en un texto
   (*"Producto: Sérum Centella. Tipo de piel: todas. Precio: $85.001…"*). Son **72 fragmentos**.
2. **Huella (hash SHA-256):** si el texto de un fragmento no cambió, **no se vuelve a pedir su embedding**: la segunda
   indexación gasta **0 tokens**.
3. **Gemini** (`gemini-embedding-001`) convierte cada texto en 768 números: `RETRIEVAL_DOCUMENT` para lo que se guarda,
   `RETRIEVAL_QUERY` para lo que busca la persona.
4. Los vectores se **normalizan** (largo 1) y se guardan en `fragmento_conocimiento.embedding`, de tipo `VECTOR(768)`.
5. Una tarea programada **reindexa sola cada noche a las 3 a. m.**

---

## 6. Búsqueda semántica (`GET /api/busqueda?q=...`)

```
"algo para que no me brille la cara"
   → Gemini lo convierte en embedding
   → pgvector:  ORDER BY embedding <=> vector_de_la_pregunta  LIMIT 5
                (<=> = distancia coseno; el índice HNSW evita compararlo contra todos)
   → los 5 fragmentos más parecidos, con su similitud (0 a 1)
```

- **Búsqueda híbrida:** primero filtros "duros" (categoría, presupuesto, tipo) y luego se ordena por significado.
- **Umbral:** los resultados con similitud muy baja se descartan.
- **Medido:** con embeddings, el producto correcto aparece entre los 3 primeros el **100 %** de las veces; por palabras, el **0 %**.

---

## 7. Lía con RAG (`POST /api/lia/chat`)

**RAG** (*Retrieval-Augmented Generation*): primero se busca información real y luego la IA responde **solo con eso**.

```
1. RECUPERAR  pregunta → embedding → pgvector trae los 8 fragmentos más parecidos
              ("máximo 40 mil" se vuelve filtro: la IA nunca ve productos más caros)
2. AUMENTAR   instrucciones de Lía + tiendas + ese CONTEXTO + últimos mensajes
3. GENERAR    Gemini escribe; el texto llega a la página por partes (streaming NDJSON)
4. VALIDAR    si nombra un producto que no estaba en el contexto, se descarta
5. REGISTRAR  resumen anónimo: tema, tokens, tiempo, similitud (nunca el texto de la persona)
```

**Protecciones:**
- **Plan B** con reglas si Gemini falla.
- **Reintento automático** si Gemini está saturado (error 429 o 503).
- **Límite** de 15 mensajes por minuto por persona.
- **CORS:** solo la página de Live Server puede llamar a la API.
- **Clave de Gemini** solo en `.env`, nunca en el código.

**Evaluación con 30 preguntas** ([pruebas/informe-calidad-ia.md](pruebas/informe-calidad-ia.md)): 100 % aprobadas,
**0 productos inventados**, 100 % del presupuesto respetado, temas de salud bien manejados, 2,4 s de respuesta en promedio.

---

## 8. Swagger

**Qué es.** Una **página web generada sola** a partir del código que documenta todos los endpoints de la API y **permite
probarlos sin escribir código**. La genera la librería `springdoc-openapi` leyendo los controladores y sus anotaciones
(`@Operation`, `@Parameter`…).

**Cómo usarlo:**
1. Encender Docker y el backend (`.\mvnw spring-boot:run` en `backend/`).
2. Abrir **http://localhost:8090/swagger-ui.html**.
3. Elegir un endpoint → **Try it out** → llenar los campos → **Execute**.
4. Se ve la petición exacta, el código de respuesta (200, 400, 404, 503) y el JSON.

**Para qué se usa:**

| Uso | Ejemplo |
|---|---|
| **Entregable obligatorio** | La especificación pide *"evidencias de Swagger o Postman"*: capturas en `docs/evidencias/` |
| **Operar la IA** | `POST /api/admin/indexacion` (la primera vez) y `POST /api/admin/evaluacion` (medir la calidad) |
| **Probar a mano** | Ver qué responde `/api/busqueda` con distintas frases |
| **Contrato con la página y con n8n** | Saber exactamente qué recibe y qué devuelve cada endpoint |
| **Sustentación** | Mostrar la API funcionando en vivo |

También publica la especificación técnica en **`/v3/api-docs`** (formato OpenAPI, en JSON), que Postman puede importar para
crear la colección de pruebas.

**Endpoints actuales:**

| Grupo | Endpoints |
|---|---|
| Catálogo | `GET /api/categorias` · `/api/tiendas` · `/api/tiendas/{slug}` · `/api/tiendas/{slug}/productos` · `/api/productos` · `/api/productos/{codigo}` |
| IA | `GET /api/busqueda` · `POST /api/lia/chat` |
| Comunidad | `GET/POST /api/resenas` · `GET/POST/DELETE /api/favoritos` |
| Analítica | `POST /api/eventos` (lotes de hasta 50) |
| Administración 🔒 | `POST /api/admin/indexacion` · `POST /api/admin/evaluacion` · `GET /api/admin/resenas/pendientes` · `PATCH /api/admin/resenas/{id}` · `GET /api/admin/indicadores` |

**🔒 Clave de administración (D-066).** Las rutas `/api/admin/*` piden la cabecera `X-Admin-Key` con el valor de
`STYLEME_ADMIN_CLAVE` del `.env`; sin ella responden **401**. En Swagger se escribe una vez con el botón **Authorize**.
La revisa un **filtro** (`FiltroClaveAdmin`), que corre antes de los controladores. Se eligió una clave en vez de cuentas con
contraseña porque quien visita StyleMe no necesita registrarse: solo la administradora (y n8n) usan esas rutas.

---

## 9. Pruebas (174 en verde)

| Tipo | Qué comprueba |
|---|---|
| **Arquitectura** (ArchUnit) | Las reglas hexagonales |
| **Dominio** | Reglas del negocio sin base ni IA: precio, coordenadas, validador anti-alucinación, presupuesto |
| **Casos de uso** | La lógica con puertos **falsos** (Mockito): el RAG completo sin Gemini |
| **Adaptadores** | Gemini simulado; pgvector y JPA contra la base real |
| **API** | Peticiones HTTP reales: 200, 201, 400, 401, 404, 409, CORS, streaming |
| **IA real** | Indexación, búsqueda, RAG y evaluación completa con Gemini de verdad |

Se ejecutan con `.\mvnw test` en `backend/` (con Docker encendido).

---

## 10. Integración: página, Docker y n8n

**La página lee todo del backend.** `iu/assets/js/api.js` trae tiendas y productos (`GET /api/tiendas`, `/api/productos`)
y los guarda 5 minutos en la pestaña. Cada página se pinta con `cuandoHayaDatos(() => …)`. Ya no existen `iu/data/*.js`.

| Qué hace la página | Endpoint |
|---|---|
| Buscador de productos (por significado) | `GET /api/busqueda` · muestra resultados solo si el mejor supera 0,64 (D-067) |
| Favoritos (♥) y página Mis favoritos | `GET/POST/DELETE /api/favoritos` |
| Reseñas en el perfil de cada tienda | `GET/POST /api/resenas` |
| Eventos (ver tienda, WhatsApp, búsquedas…) en lotes | `POST /api/eventos` |
| Panel `admin.html`: moderación e indicadores | `/api/admin/*` con la clave |

**Docker (`docker compose up -d --build`)** levanta 4 servicios:

| Servicio | Qué es | Puerto |
|---|---|---|
| `basedatos` | PostgreSQL + pgvector | 5432 |
| `backend` | Spring Boot (imagen de 2 etapas: compila con Maven y corre solo con Java) | 8090 |
| `iu` | nginx: sirve la página y reenvía `/api/` al backend (misma dirección → sin CORS) | 8085 |
| `n8n` | automatizaciones con IA | 5678 |

**n8n · 3 workflows con Gemini** ([n8n/README.md](../n8n/README.md)):
1. **Moderación de reseñas:** el backend avisa por webhook (`NotificadorPuerto` → `N8nNotificadorAdaptador`), n8n trae
   las pendientes, Gemini decide PUBLICADA o RECHAZADA, n8n responde con `PATCH /api/admin/resenas/{id}`. También corre
   cada 30 min. Prueba real: una crítica respetuosa se publicó; una con insultos y un teléfono se rechazó.
2. **Reindexación nocturna:** 3:00 a. m. → `POST /api/admin/indexacion` (solo recalcula lo que cambió).
3. **Reporte semanal:** lunes 8:00 → lee los indicadores → Gemini escribe hallazgos y recomendaciones → archivo `.md`
   (ejemplo real en [docs/evidencias/reporte-semanal-ia-ejemplo.md](evidencias/reporte-semanal-ia-ejemplo.md)).

---

## 11. Preguntas probables en la sustentación

| Pregunta | Respuesta corta |
|---|---|
| ¿Qué es un embedding? | Una lista de 768 números que representa el significado de un texto |
| ¿Para qué sirve el índice HNSW? | Para encontrar los vectores más parecidos sin compararlos todos (un grafo de "vecinos") |
| ¿Qué es RAG? | Buscar primero información real y luego generar la respuesta solo con esa información |
| ¿Cómo evitan que Lía invente productos? | Solo recibe productos recuperados de la base, y el backend descarta cualquier código que no estuviera en el contexto |
| ¿Por qué arquitectura hexagonal? | Porque el negocio no depende de la tecnología: cambiar Gemini o la base es cambiar un adaptador |
| ¿Cómo demuestran que la IA funciona? | 30 preguntas con respuesta conocida: 100 % de acierto con embeddings frente a 0 % por palabras, 0 alucinaciones |
| ¿Qué pasa si Gemini se cae? | Reintenta; si sigue fallando, responde el plan B con reglas y productos reales |
| ¿Para qué sirve n8n si el backend ya hace todo? | Para automatizar tareas que pasan solas (moderar, reindexar, reportar) sin escribir más código en el backend; se ven y se cambian en la pantalla de n8n |
| ¿Qué pasa si n8n está apagado cuando llega una reseña? | La reseña queda guardada como PENDIENTE; la revisión de cada 30 minutos la modera después |
| ¿Por qué nginx? | Sirve la página y reenvía `/api/` al backend: página y API quedan en la misma dirección y el navegador no necesita CORS |
| ¿Cómo ahorran tokens? | Embeddings guardados (hash), historial corto y solo 8 fragmentos de contexto por pregunta |
