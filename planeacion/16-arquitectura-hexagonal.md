# 16 · Arquitectura hexagonal del backend

> Decisión D-057 (2026-11-24): el backend de StyleMe sigue la **arquitectura hexagonal** (puertos y adaptadores)
> con todas sus reglas. Este documento dice **dónde va cada archivo** y **qué regla cumple**.
>
> 💡 Tecnologías: Java 21 · Spring Boot · Maven · PostgreSQL + pgvector · Gemini · n8n.

---

## 1. La idea en un dibujo

```
                    ADAPTADORES DE ENTRADA (quién llama a StyleMe)
          ┌────────────────┬────────────────┬────────────────┐
          │ REST (página)  │ Webhook (n8n)  │ Tareas program.│
          └───────┬────────┴───────┬────────┴───────┬────────┘
                  ▼                ▼                ▼
          ┌──────────── PUERTOS DE ENTRADA (casos de uso) ────────────┐
          │                                                           │
          │        APLICACIÓN: orquesta los casos de uso              │
          │     ┌───────────────────────────────────────────┐         │
          │     │   DOMINIO: Tienda, Producto, Reseña,      │         │
          │     │   Fragmento, Conversación... y sus reglas │         │
          │     │   (Java puro, sin Spring, sin base datos) │         │
          │     └───────────────────────────────────────────┘         │
          │                                                           │
          └──────────── PUERTOS DE SALIDA (interfaces) ───────────────┘
                  ▼                ▼                ▼               ▼
          ┌──────────────┬────────────────┬───────────────┬──────────────┐
          │ PostgreSQL   │ pgvector       │ Gemini        │ n8n          │
          │ (JPA)        │ (búsqueda      │ (embeddings y │ (avisos y    │
          │              │  vectorial)    │  respuestas)  │  automatiz.) │
          └──────────────┴────────────────┴───────────────┴──────────────┘
                    ADAPTADORES DE SALIDA (a quién llama StyleMe)
```

**En una frase:** el centro (dominio + aplicación) no sabe que existen Spring, PostgreSQL, Gemini ni n8n.
Solo conoce **interfaces** (puertos). Los adaptadores son los "enchufes" que conectan esas interfaces con el mundo real.

**Por qué conviene en StyleMe:** si mañana se cambia Gemini por otro modelo, o pgvector por otra base vectorial,
solo se escribe un adaptador nuevo; los casos de uso y las reglas no se tocan. Eso es fácil de defender en la sustentación.

---

## 2. Las reglas (y cómo se comprueban)

| # | Regla | Qué significa en la práctica |
|---|---|---|
| R1 | **Las dependencias apuntan hacia adentro** | `infrastructure` → `application` → `domain`. Nunca al revés |
| R2 | **El dominio es Java puro** | En `domain` no hay `@Entity`, `@Service`, `@Autowired`, ni imports de Spring, JPA, Jackson o Gemini |
| R3 | **La aplicación tampoco depende del framework** | En `application` no hay anotaciones de Spring. Los casos de uso se registran como beans en `infrastructure/config` |
| R4 | **Puertos de entrada = interfaces de casos de uso** | Un caso de uso por acción: `BuscarSemanticoUseCase`, `ChatearConLiaUseCase`… Los controladores **solo** llaman a estas interfaces |
| R5 | **Puertos de salida = interfaces que define el centro** | `TiendaRepositoryPort`, `EmbeddingPort`, `VectorStorePort`… Se declaran en `application` y se **implementan** en `infrastructure` |
| R6 | **Cada capa tiene su propio modelo** | Dominio (`Tienda`) ≠ entidad JPA (`TiendaEntity`) ≠ DTO de la API (`TiendaResponse`). Se convierten con **mappers** en los adaptadores |
| R7 | **Los adaptadores no se hablan entre sí** | El controlador REST no usa un repositorio JPA directo; pasa siempre por un caso de uso |
| R8 | **Las reglas de negocio viven en el dominio** | "Corazones entre 1 y 5", "una reseña es de una tienda o de un producto", "Lía no puede recomendar productos que no existen" |
| R9 | **Errores del dominio = excepciones del dominio** | `TiendaNoEncontradaException` nace en el dominio; el adaptador REST la traduce a HTTP 404 |
| R10 | **Transacciones y seguridad son infraestructura** | `@Transactional`, Spring Security y JWT viven en `infrastructure` (en la configuración o en los adaptadores) |

**Comprobación automática:** una prueba con **ArchUnit** (`ArquitecturaHexagonalTest`) falla si alguien rompe R1, R2, R3 o R7.
Así las reglas no dependen de la memoria: las vigila una prueba (y suma en "pruebas" y "dominio del código").

---

## 3. Estructura del repositorio

```
StyleMe/
├── frontend/                 La página (HTML, CSS, JS, Lía 2D/3D). Es un CLIENTE del backend
├── backend/                  Spring Boot con arquitectura hexagonal (sección 4)
├── database/                 Scripts SQL: esquema, índices, datos iniciales, vistas
├── n8n/                      Workflows exportados en JSON
├── docs/
│   ├── planeacion/           Estos documentos
│   ├── diagramas/            Entidad-relación y arquitectura
│   ├── pruebas/              Documento de pruebas e informe de calidad de IA
│   ├── evidencias/           Capturas de Swagger y colección de Postman
│   └── informe-datos/        Informe de análisis de datos
├── docker-compose.yml
├── .env.example
├── AI_USAGE.md
└── README.md
```

> La arquitectura hexagonal es del **backend**. El frontend, n8n y la base de datos son "el mundo de afuera":
> se conectan al hexágono por medio de adaptadores.

---

## 4. Estructura del backend

Paquete base: `co.styleme`. Primero las **capas** del hexágono; dentro de cada una, los **módulos**
(`catalogo`, `comunidad`, `analitica`, `ia`, `usuario`).

```
backend/
├── pom.xml
├── .sdkmanrc                              java=21.x.x-tem
└── src/
    ├── main/
    │   ├── java/co/styleme/
    │   │   ├── StyleMeApplication.java
    │   │   │
    │   │   ├── domain/                    🟢 CENTRO · Java puro
    │   │   │   ├── model/
    │   │   │   │   ├── catalogo/          Tienda, Producto, Categoria, Zona, Ingrediente, Etiqueta
    │   │   │   │   ├── comunidad/         Resena, Favorito, Corazones (valor 1–5), EstadoResena
    │   │   │   │   ├── usuario/           Usuario, Rol, Correo
    │   │   │   │   ├── analitica/         Evento, TipoEvento, Indicador
    │   │   │   │   └── ia/                FragmentoConocimiento, Embedding, ResultadoBusqueda,
    │   │   │   │                          ConversacionLia, RespuestaLia, EvaluacionIa
    │   │   │   ├── service/               Reglas que usan varios modelos:
    │   │   │   │                          ValidadorRecomendaciones (anti-alucinación),
    │   │   │   │                          FragmentadorConocimiento (chunking), ResumidorConversacion
    │   │   │   └── exception/             TiendaNoEncontradaException, ResenaDuplicadaException…
    │   │   │
    │   │   ├── application/               🟡 CASOS DE USO · Java puro
    │   │   │   ├── port/
    │   │   │   │   ├── in/                Interfaces de casos de uso (qué puede hacer StyleMe)
    │   │   │   │   │   ├── catalogo/      ConsultarTiendasUseCase, ConsultarProductosUseCase
    │   │   │   │   │   ├── comunidad/     CalificarUseCase, GestionarFavoritosUseCase, ModerarResenaUseCase
    │   │   │   │   │   ├── usuario/       RegistrarUsuarioUseCase, IniciarSesionUseCase
    │   │   │   │   │   ├── analitica/     RegistrarEventoUseCase, ConsultarIndicadoresUseCase
    │   │   │   │   │   └── ia/            IndexarConocimientoUseCase, BuscarSemanticoUseCase,
    │   │   │   │   │                      ChatearConLiaUseCase, EvaluarIaUseCase
    │   │   │   │   └── out/               Interfaces de lo que StyleMe NECESITA de afuera
    │   │   │   │       ├── persistencia/  TiendaRepositoryPort, ProductoRepositoryPort, ResenaRepositoryPort…
    │   │   │   │       ├── ia/            EmbeddingPort, VectorStorePort, ModeloLenguajePort
    │   │   │   │       ├── seguridad/     CifradorContrasenaPort, GeneradorTokenPort
    │   │   │   │       └── notificacion/  NotificadorPort
    │   │   │   └── usecase/               Implementaciones: ConsultarTiendasService, BuscarSemanticoService,
    │   │   │                              ChatearConLiaService (el flujo RAG), IndexarConocimientoService…
    │   │   │
    │   │   └── infrastructure/            🔵 ADAPTADORES · aquí sí hay Spring
    │   │       ├── adapter/
    │   │       │   ├── in/
    │   │       │   │   ├── rest/          Controladores + dto/ (Request, Response) + mapper/
    │   │       │   │   │                  + ManejadorErrores (excepción del dominio → HTTP)
    │   │       │   │   ├── webhook/       Lo que llama n8n (ej. resultado de moderación)
    │   │       │   │   └── scheduler/     Tareas programadas (ej. reindexar de noche)
    │   │       │   └── out/
    │   │       │       ├── persistencia/  entity/ (TiendaEntity…), repository/ (Spring Data),
    │   │       │       │                  mapper/, y los adaptadores que implementan *RepositoryPort
    │   │       │       ├── vector/        PgVectorAdapter (JdbcTemplate + operador <=>) → VectorStorePort
    │   │       │       ├── gemini/        GeminiEmbeddingAdapter → EmbeddingPort
    │   │       │       │                  GeminiChatAdapter → ModeloLenguajePort
    │   │       │       ├── seguridad/     BCryptCifradorAdapter, JwtGeneradorTokenAdapter
    │   │       │       └── n8n/           N8nNotificadorAdapter → NotificadorPort (llama webhooks)
    │   │       └── config/                BeanConfig (registra los casos de uso), SecurityConfig,
    │   │                                  OpenApiConfig (Swagger), CorsConfig, GeminiProperties
    │   └── resources/
    │       ├── application.yml            Lee todo de variables de entorno (nada secreto aquí)
    │       └── prompts/                   Instrucciones de Lía (texto), fuera del código
    └── test/java/co/styleme/
        ├── arquitectura/                  ArquitecturaHexagonalTest (ArchUnit: vigila las reglas)
        ├── domain/                        Pruebas del dominio (sin Spring, rapidísimas)
        ├── application/                   Pruebas de casos de uso con puertos falsos (Mockito)
        ├── infrastructure/                Pruebas de adaptadores (Testcontainers con pgvector)
        └── evaluacion/                    Evaluación de IA: 30 preguntas, precisión@k (rama test/evaluacion-ia)
```

---

## 5. Puertos y adaptadores de StyleMe

| Puerto (interfaz, en `application`) | Tipo | Adaptador (clase, en `infrastructure`) | Conecta con |
|---|---|---|---|
| `ConsultarTiendasUseCase`, `ConsultarProductosUseCase` | Entrada | `TiendaController`, `ProductoController` | Página web |
| `CalificarUseCase`, `GestionarFavoritosUseCase` | Entrada | `ResenaController`, `FavoritoController` | Página web |
| `ModerarResenaUseCase` | Entrada | `ModeracionWebhookController` | n8n |
| `RegistrarEventoUseCase`, `ConsultarIndicadoresUseCase` | Entrada | `EventoController`, `IndicadorController` | Página web, panel admin, n8n |
| `BuscarSemanticoUseCase` | Entrada | `BusquedaController` | Página web |
| `ChatearConLiaUseCase` | Entrada | `LiaController` | Lía (página) |
| `IndexarConocimientoUseCase` | Entrada | `AdminIndexacionController`, `ReindexacionScheduler` | Admin, n8n, tarea nocturna |
| `TiendaRepositoryPort`, `ProductoRepositoryPort`… | Salida | `TiendaPersistenceAdapter`… (JPA) | PostgreSQL |
| `VectorStorePort` | Salida | `PgVectorAdapter` | pgvector (índice HNSW) |
| `EmbeddingPort` | Salida | `GeminiEmbeddingAdapter` | Gemini (embeddings) |
| `ModeloLenguajePort` | Salida | `GeminiChatAdapter` | Gemini (respuestas) |
| `NotificadorPort` | Salida | `N8nNotificadorAdapter` | n8n (webhooks) |
| `CifradorContrasenaPort`, `GeneradorTokenPort` | Salida | `BCryptCifradorAdapter`, `JwtGeneradorTokenAdapter` | Spring Security |

### El RAG dentro del hexágono (`ChatearConLiaService`)
```
LiaController ──▶ ChatearConLiaUseCase
                     │ 1. EmbeddingPort.generar(pregunta)            → GeminiEmbeddingAdapter
                     │ 2. VectorStorePort.buscarParecidos(vector, 5)  → PgVectorAdapter (HNSW)
                     │ 3. arma el prompt: reglas + fragmentos + historial corto
                     │ 4. ModeloLenguajePort.responder(prompt)        → GeminiChatAdapter
                     │ 5. ValidadorRecomendaciones (DOMINIO) descarta productos inexistentes
                     │ 6. guarda el resumen (ConversacionRepositoryPort)
                     ▼
                 RespuestaLia
```
Si `ModeloLenguajePort` falla, el caso de uso responde con el **plan B** (reglas) y marca la conversación como `PLAN_B`.

---

## 6. Qué pasa con los archivos que ya existen

| Hoy | Destino | Cuándo (rama) |
|---|---|---|
| `*.html`, `assets/`, `data/` | `frontend/` | 2 · `refactor/estructura-proyecto` |
| `planeacion/` | `docs/planeacion/` | 2 |
| `.env.ejemplo` | `.env.example` | 2 |
| `data/*.js` | `database/02-datos-iniciales.sql` (y luego se borra) | 3 y 9 |
| `netlify/functions/lia.mjs` → reglas de Lía | `backend/src/main/resources/prompts/` | 7 · `feature/rag-lia` |
| `lia.mjs` → llamada a embeddings | `GeminiEmbeddingAdapter` | 6 · `feature/embeddings-busqueda` |
| `lia.mjs` → similitud coseno en memoria | `PgVectorAdapter` (la hace la base de datos con HNSW) | 6 |
| `lia.mjs` → validación de productos y emoción | `ValidadorRecomendaciones` (dominio) | 7 |
| `lia.mjs` → llamada a Gemini con streaming | `GeminiChatAdapter` | 7 |
| `frontend/assets/js/lia/plan-b.js` | Se queda (respaldo si el backend no responde) | — |
| `netlify/`, `netlify.toml` | Se borran | 7 y 10 |

---

## 7. Ejemplo parcial: el módulo de tiendas (molde para los demás)

> Modo tutoría: este es **el molde de una sola tienda**. Productos, reseñas, favoritos, etc. los escribe la autora
> siguiendo este patrón, y después se revisan.

**Dominio** (`domain/model/catalogo/Tienda.java`) · sin anotaciones
```java
public record Tienda(Long id, String slug, String nombre, Categoria categoriaPrincipal,
                     Zona zona, Coordenadas coordenadas, boolean verificada) {
    public Tienda {
        if (slug == null || slug.isBlank()) throw new IllegalArgumentException("La tienda necesita slug");
    }
    public boolean apareceEnMapa() { return coordenadas != null; }   // regla del negocio
}
```

**Puerto de salida** (`application/port/out/persistencia/TiendaRepositoryPort.java`)
```java
public interface TiendaRepositoryPort {
    List<Tienda> buscarActivas(String categoriaSlug);
    Optional<Tienda> buscarPorSlug(String slug);
}
```

**Puerto de entrada** (`application/port/in/catalogo/ConsultarTiendasUseCase.java`)
```java
public interface ConsultarTiendasUseCase {
    List<Tienda> listar(String categoriaSlug);
    Tienda obtener(String slug);   // lanza TiendaNoEncontradaException
}
```

**Caso de uso** (`application/usecase/ConsultarTiendasService.java`) · sin `@Service`
```java
public class ConsultarTiendasService implements ConsultarTiendasUseCase {
    private final TiendaRepositoryPort repositorio;
    public ConsultarTiendasService(TiendaRepositoryPort repositorio) { this.repositorio = repositorio; }

    @Override public Tienda obtener(String slug) {
        return repositorio.buscarPorSlug(slug).orElseThrow(() -> new TiendaNoEncontradaException(slug));
    }
    // listar(...) → completar
}
```

**Registro del bean** (`infrastructure/config/BeanConfig.java`)
```java
@Configuration
public class BeanConfig {
    @Bean
    ConsultarTiendasUseCase consultarTiendas(TiendaRepositoryPort repositorio) {
        return new ConsultarTiendasService(repositorio);
    }
}
```

**Lo que falta en este molde (lo completa la autora):**
- `TiendaEntity` + `TiendaJpaRepository` + `TiendaPersistenceMapper` + `TiendaPersistenceAdapter implements TiendaRepositoryPort`.
- `TiendaController` (usa `ConsultarTiendasUseCase`) + `TiendaResponse` + `TiendaRestMapper`.
- Traducir `TiendaNoEncontradaException` a 404 en `ManejadorErrores`.

---

## 8. Orden recomendado para construir cada módulo

```
1. Dominio (modelo + reglas)  →  2. Prueba del dominio
3. Puertos (in y out)         →  4. Caso de uso + prueba con puertos falsos
5. Adaptador de salida        →  6. Adaptador de entrada (REST)
7. Registrar en BeanConfig    →  8. Probar en Swagger / Postman
```
De adentro hacia afuera: así el centro nunca depende de los adaptadores.
