# Backend de StyleMe

API en **Spring Boot 4.2** (Java 21) con **arquitectura hexagonal** (puertos y adaptadores).
Diseño completo: [docs/planeacion/16-arquitectura-hexagonal.md](../docs/planeacion/16-arquitectura-hexagonal.md).

## Ejecutar

1. Encender la base de datos (en la raíz del proyecto): `docker compose up -d`
2. Tener el archivo `.env` en la raíz (copia de `.env.example`). El backend lee de ahí la contraseña de la base.
3. En la carpeta `backend/`:

```
.\mvnw spring-boot:run        # PowerShell (Windows)
./mvnw spring-boot:run        # Git Bash, Linux o Mac
```

El backend queda en **http://localhost:8090** (se cambia con `SERVER_PORT` en el `.env`). Se detiene con `Ctrl+C`.

## Documentación de la API (Swagger)

Con el backend encendido:

- **Swagger UI (interactivo):** http://localhost:8090/swagger-ui.html → cada endpoint tiene un botón *"Try it out"* para probarlo.
- **Especificación OpenAPI (JSON):** http://localhost:8090/v3/api-docs

## Clave de administración

Las rutas **`/api/admin/*`** (indexación, evaluación, moderación de reseñas e indicadores) exigen la cabecera
**`X-Admin-Key`** con el valor de `STYLEME_ADMIN_CLAVE` del `.env`. El resto de la API es pública.

| Situación | Respuesta |
|---|---|
| Sin cabecera o con una clave equivocada | **401** |
| `STYLEME_ADMIN_CLAVE` vacía | **503** (la administración queda bloqueada) |
| Clave correcta | La respuesta normal del endpoint |

En **Swagger**: botón **Authorize** (arriba a la derecha) → pegar la clave → *Authorize*. Desde ese momento Swagger la
manda sola en las rutas de administración. La revisa `FiltroClaveAdmin` (un filtro que corre antes de los controladores).

## Endpoints del catálogo

| Método | Dirección | Qué devuelve |
|---|---|---|
| GET | `/api/categorias` | Las 4 categorías |
| GET | `/api/tiendas?categoria=ropa&zona=Cabecera` | Tiendas activas (filtros opcionales) |
| GET | `/api/tiendas/{slug}` | Una tienda (ej. `/api/tiendas/bohor`) · 404 si no existe |
| GET | `/api/tiendas/{slug}/productos` | Productos de una tienda · 404 si la tienda no existe |
| GET | `/api/productos?categoria=skincare&tienda=pandora-makeup&precioMaximo=50000&q=serum` | Productos (filtros opcionales; `q` ignora tildes y mayúsculas) |
| GET | `/api/productos/{codigo}` | Un producto (ej. `/api/productos/p028`) · 404 si no existe |

Los errores siempre tienen la misma forma:
```json
{ "estado": 404, "error": "Not Found", "mensaje": "No existe una tienda con el identificador 'x'", "ruta": "/api/tiendas/x", "fecha": "..." }
```

## Inteligencia artificial: embeddings y búsqueda semántica

| Método | Dirección | Qué hace |
|---|---|---|
| POST | `/api/admin/indexacion` | Genera los embeddings (Gemini) de tiendas, productos y guías y los guarda en pgvector. Solo recalcula lo nuevo o modificado (compara el hash SHA-256 del texto) |
| GET | `/api/busqueda?q=algo para que no me brille la cara&categoria=skincare&precioMaximo=60000` | Búsqueda **semántica** (por significado) e **híbrida** (filtros + similitud). Parámetros opcionales: `tipo` (tienda, producto, guia), `limite` (1–20), `similitudMinima` (0–1) |

**Primera vez:** con el backend encendido, ejecutar una vez `POST /api/admin/indexacion` (desde Swagger) para construir el índice.
Después se reindexa solo cada noche a las 3:00 a. m. (`ReindexacionProgramador`).

**Cómo funciona:**
```
INDEXAR:  tienda / producto / sección de guía → FragmentadorConocimiento (chunking)
          → ¿cambió el hash? → Gemini (RETRIEVAL_DOCUMENT, 768 dim.) → pgvector (índice HNSW)

BUSCAR:   consulta → Gemini (RETRIEVAL_QUERY) → pgvector: filtros + ORDER BY embedding <=> consulta
          → top-k con similitud ≥ umbral
```
Las guías de conocimiento están en `src/main/resources/conocimiento/guias/*.md`.
Si Gemini no está disponible (sin clave o sin cupo), la API responde **503** y el resto del backend sigue funcionando.

## Lía: asesora con RAG

`POST /api/lia/chat` con `{"mensaje": "...", "historia": [{"rol": "persona|lia", "texto": "..."}], "sesionId": "opcional"}`.
Responde **por partes** en NDJSON (una línea JSON por evento): `{"tipo":"texto","delta":"..."}` mientras habla y
`{"tipo":"fin","emocion":"...","productos":[...],"acciones":[...],"sugerencias":[...],"basico":false}` al final.

```
RECUPERAR  pregunta (+ 2 mensajes anteriores) → embedding → pgvector: 8 fragmentos más parecidos (similitud ≥ 0.55,
           presupuesto como filtro duro: "máximo 50 mil" → precio ≤ 50.000)
AUMENTAR   plantilla resources/prompts/lia-instrucciones.md + tiendas + CONTEXTO recuperado + historial corto
GENERAR    Gemini (streamGenerateContent) → el texto se reenvía a la página mientras llega
VALIDAR    ValidadorRecomendaciones: descarta productos que no estaban en el contexto (anti-alucinación)
REGISTRAR  resumen en conversacion_lia (tema, modo, similitud, latencia, tokens) — nunca el texto de la persona
```
Si Gemini no responde, contesta el **plan B** (reglas + productos reales) y la página muestra "modo básico".
Límite: 15 mensajes por minuto por persona. CORS: solo Live Server (`STYLEME_CORS_ORIGENES` para cambiarlo).

## Reseñas, favoritos y analítica

Sin registro: la página manda un `sesionId` anónimo del navegador (D-066).

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/api/resenas?tienda=bohor` (o `?producto=p049`) | Promedio de corazones, cantidad y reseñas **publicadas** |
| POST | `/api/resenas` | `{"sesionId","autor","tienda" o "producto","corazones":1-5,"comentario"}` → 201, queda **PENDIENTE** · 404 si no existe · 409 si esa sesión ya la reseñó · 429 si son demasiadas |
| GET | `/api/favoritos?sesionId=...` | Favoritos de la sesión |
| POST | `/api/favoritos` | `{"sesionId","tienda" o "producto"}` → 204 (si ya estaba, no se duplica) |
| DELETE | `/api/favoritos?sesionId=...&tienda=bohor` | Quita un favorito → 204 |
| POST | `/api/eventos` | Lote de hasta 50 eventos (`VER_TIENDA`, `CLIC_WHATSAPP`, `BUSQUEDA`...) → 202. Si uno es inválido no se guarda ninguno |
| GET | `/api/admin/resenas/pendientes?limite=20` | Reseñas por moderar (las más antiguas primero) |
| PATCH | `/api/admin/resenas/{id}` | `{"estado":"PUBLICADA"}` o `RECHAZADA` (solo desde PENDIENTE) |
| GET | `/api/admin/indicadores` y `/api/admin/indicadores/{codigo}` | Los 13 indicadores del informe de datos (vistas `v_*` de `basedatos/05-vistas-indicadores.sql`) |

## Evaluación de la IA

`POST /api/admin/evaluacion` ejecuta las 30 preguntas de `src/main/resources/evaluacion/preguntas.json` y devuelve:
acierto@3 (semántico vs. palabras), precisión@k, recall@5, % sin alucinación, % presupuesto, salud y temas ajenos.
Cada ejecución queda en la tabla `evaluacion_ia`. Resultados: [docs/pruebas/informe-calidad-ia.md](../docs/pruebas/informe-calidad-ia.md).

## Capas

```
src/main/java/co/styleme/
├── dominio/                🟢 Modelo y reglas del negocio (Java puro)
│   ├── modelo/catalogo/        Tienda, Producto, Categoria, Zona, Etiqueta, Coordenadas, Contacto...
│   ├── modelo/ia/              Embedding, FragmentoConocimiento, Guia, FiltroBusqueda, ResultadoBusqueda...
│   ├── modelo/lia/             PreguntaLia, RespuestaLia, ConversacionLia, Prompt...
│   ├── servicio/               FragmentadorConocimiento (chunking), ValidadorRecomendaciones (anti-alucinación),
│   │                           LectorRespuestaLia (streaming), LectorPresupuesto, PlanBLia
│   └── excepcion/              TiendaNoEncontradaException, DatoInvalidoException...
├── aplicacion/             🟡 Puertos y casos de uso (Java puro)
│   ├── puerto/entrada/         interfaces ...CasoUso   (lo que StyleMe permite hacer)
│   ├── puerto/salida/          interfaces ...Puerto    (lo que StyleMe necesita de afuera)
│   └── casodeuso/              implementaciones ...Servicio
└── infraestructura/        🔵 Adaptadores (aquí sí hay Spring)
    ├── adaptador/entrada/rest/     Controladores, DTO, ManejadorErrores
    ├── adaptador/entrada/programador/  Reindexación nocturna
    ├── adaptador/salida/persistencia/  Entidades JPA, repositorios Spring Data, adaptadores
    ├── adaptador/salida/gemini/    Embeddings con la API de Gemini
    ├── adaptador/salida/vector/    pgvector: guardar y buscar vectores (índice HNSW)
    ├── adaptador/salida/conocimiento/  Lectura de las guías Markdown
    └── configuracion/              ConfiguracionBeans (enchufa los casos de uso), Swagger
```

**Dependencias:** `infraestructura` → `aplicacion` → `dominio`. Nunca al revés.
Cada carpeta tiene un `package-info.java` que explica qué va ahí y qué regla cumple.

### Recorrido de una petición
```
GET /api/tiendas/bohor
  → TiendaControlador            (adaptador de entrada: recibe HTTP)
  → ConsultarTiendasCasoUso      (puerto de entrada)
  → ConsultarTiendasServicio     (caso de uso)
  → TiendaRepositorioPuerto      (puerto de salida)
  → TiendaPersistenciaAdaptador  (adaptador de salida: JPA → PostgreSQL)
  ← Tienda (dominio) → TiendaRespuesta (DTO) → JSON
```

## Pruebas

```
.\mvnw test
```

| Tipo | Dónde | Qué comprueba | Necesita la base |
|---|---|---|---|
| Arquitectura (ArchUnit) | `test/.../arquitectura/` | Las 8 reglas hexagonales | No |
| Dominio | `test/.../dominio/` | Reglas del negocio (precio > 0, tienda sin coordenadas no sale en el mapa...) | No |
| Casos de uso | `test/.../aplicacion/` | Lógica de la aplicación con puertos falsos (Mockito) | No |
| Persistencia | `test/.../persistencia/` | Los adaptadores JPA y SQL (reseñas, favoritos, eventos, indicadores) contra los datos reales; se deshacen al terminar | Sí |
| API (punta a punta) | `test/.../rest/` | Peticiones HTTP reales: 200, 201, 202, 204, 400, 404, 409 y Swagger | Sí |
| Adaptador Gemini | `test/.../gemini/` | La forma de las peticiones a Gemini (con un servidor simulado) | No |
| pgvector | `test/.../vector/` | Guardar vectores, búsqueda por similitud, filtros híbridos y umbral | Sí |
| IA real | `test/.../ia/` | Indexación, búsqueda y **RAG de Lía con Gemini real** (productos reales, presupuesto, salud, NDJSON). Solo corre si hay `GEMINI_API_KEY` | Sí + clave |

## Configuración (`src/main/resources/application.yml`)

- **Puerto 8090** (en este computador el 8080 y el 8081 los usan otros proyectos).
- La conexión a la base se arma con las variables del `.env`: nunca hay contraseñas en el código.
- `ddl-auto: validate`: Hibernate **comprueba** que las entidades coincidan con las tablas, pero nunca las cambia.
  Las tablas las manda [basedatos/01-esquema.sql](../basedatos/01-esquema.sql).
