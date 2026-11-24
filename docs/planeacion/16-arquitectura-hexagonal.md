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
| R1 | **Las dependencias apuntan hacia adentro** | `infraestructura` → `aplicacion` → `dominio`. Nunca al revés |
| R2 | **El dominio es Java puro** | En `dominio` no hay `@Entity`, `@Service`, `@Autowired`, ni imports de Spring, JPA, Jackson o Gemini |
| R3 | **La aplicación tampoco depende del framework** | En `aplicacion` no hay anotaciones de Spring. Los casos de uso se registran como beans en `infraestructura/configuracion` |
| R4 | **Puertos de entrada = interfaces de casos de uso** | Un caso de uso por acción: `BuscarSemanticoCasoUso`, `ChatearConLiaCasoUso`… Los controladores **solo** llaman a estas interfaces |
| R5 | **Puertos de salida = interfaces que define el centro** | `TiendaRepositorioPuerto`, `EmbeddingPuerto`, `AlmacenVectorialPuerto`… Se declaran en `aplicacion` y se **implementan** en `infraestructura` |
| R6 | **Cada capa tiene su propio modelo** | Dominio (`Tienda`) ≠ entidad JPA (`TiendaEntidad`) ≠ DTO de la API (`TiendaRespuesta`). Se convierten con **mappers** en los adaptadores |
| R7 | **Los adaptadores no se hablan entre sí** | El controlador REST no usa un repositorio JPA directo; pasa siempre por un caso de uso |
| R8 | **Las reglas de negocio viven en el dominio** | "Corazones entre 1 y 5", "una reseña es de una tienda o de un producto", "Lía no puede recomendar productos que no existen" |
| R9 | **Errores del dominio = excepciones del dominio** | `TiendaNoEncontradaException` nace en el dominio; el adaptador REST la traduce a HTTP 404 |
| R10 | **Transacciones y seguridad son infraestructura** | `@Transactional`, Spring Security y JWT viven en `infraestructura` (en la configuración o en los adaptadores) |

**Comprobación automática:** una prueba con **ArchUnit** (`ArquitecturaHexagonalTest`) falla si alguien rompe R1, R2, R3 o R7.
Así las reglas no dependen de la memoria: las vigila una prueba (y suma en "pruebas" y "dominio del código").

---

## 3. Estructura del repositorio

```
StyleMe/
├── iu/                 La página (HTML, CSS, JS, Lía 2D/3D). Es un CLIENTE del backend
├── backend/                  Spring Boot con arquitectura hexagonal (sección 4)
├── basedatos/                Scripts SQL: esquema, índices, datos iniciales, vistas
├── n8n/                      Workflows exportados en JSON
├── docs/
│   ├── planeacion/           Estos documentos
│   ├── diagramas/            Entidad-relación y arquitectura
│   ├── pruebas/              Documento de pruebas e informe de calidad de IA
│   ├── evidencias/           Capturas de Swagger y colección de Postman
│   └── informe-datos/        Informe de análisis de datos
├── docker-compose.yml
├── .env.example
├── IAS_usadas.md
└── README.md
```

> La arquitectura hexagonal es del **backend**. La iu, n8n y la base de datos son "el mundo de afuera":
> se conectan al hexágono por medio de adaptadores.

---

## 4. Estructura del backend

Paquete base: `co.styleme`. Primero las **capas** del hexágono; dentro de cada una, los **módulos**
(`catalogo`, `comunidad`, `analitica`, `ia`, `usuario`).

**Nombres en español (D-058):** capas `dominio`, `aplicacion`, `infraestructura`; puertos en `puerto/entrada` y `puerto/salida`;
casos de uso en `casodeuso`; adaptadores en `adaptador/entrada` y `adaptador/salida`.
Las clases usan sufijos en español: `...CasoUso` (puerto de entrada), `...Puerto` (puerto de salida), `...Servicio` (caso de uso),
`...Adaptador`, `...Controlador`, `...Entidad`, `...Mapeador`, `...Solicitud` / `...Respuesta` (DTO).
Se mantienen en inglés solo las convenciones de Java y JUnit: `...Exception` y `...Test`.

```
backend/
├── pom.xml
├── .sdkmanrc                              java=21.x.x-tem
└── src/
    ├── main/
    │   ├── java/co/styleme/
    │   │   ├── StyleMeApplication.java
    │   │   │
    │   │   ├── dominio/                   🟢 CENTRO · Java puro
    │   │   │   ├── modelo/
    │   │   │   │   ├── catalogo/          Tienda, Producto, Categoria, Zona, Ingrediente, Etiqueta
    │   │   │   │   ├── comunidad/         Resena, Favorito, Corazones (valor 1–5), EstadoResena
    │   │   │   │   ├── usuario/           Usuario, Rol, Correo
    │   │   │   │   ├── analitica/         Evento, TipoEvento, Indicador
    │   │   │   │   └── ia/                FragmentoConocimiento, Embedding, ResultadoBusqueda,
    │   │   │   │                          ConversacionLia, RespuestaLia, EvaluacionIa
    │   │   │   ├── servicio/              Reglas que usan varios modelos:
    │   │   │   │                          ValidadorRecomendaciones (anti-alucinación),
    │   │   │   │                          FragmentadorConocimiento (chunking), ResumidorConversacion
    │   │   │   └── excepcion/             TiendaNoEncontradaException, ResenaDuplicadaException…
    │   │   │
    │   │   ├── aplicacion/                🟡 CASOS DE USO · Java puro
    │   │   │   ├── puerto/
    │   │   │   │   ├── entrada/           Interfaces de casos de uso (qué puede hacer StyleMe)
    │   │   │   │   │   ├── catalogo/      ConsultarTiendasCasoUso, ConsultarProductosCasoUso
    │   │   │   │   │   ├── comunidad/     CalificarCasoUso, GestionarFavoritosCasoUso, ModerarResenaCasoUso
    │   │   │   │   │   ├── usuario/       RegistrarUsuarioCasoUso, IniciarSesionCasoUso
    │   │   │   │   │   ├── analitica/     RegistrarEventoCasoUso, ConsultarIndicadoresCasoUso
    │   │   │   │   │   └── ia/            IndexarConocimientoCasoUso, BuscarSemanticoCasoUso,
    │   │   │   │   │                      ChatearConLiaCasoUso, EvaluarIaCasoUso
    │   │   │   │   └── salida/            Interfaces de lo que StyleMe NECESITA de afuera
    │   │   │   │       ├── persistencia/  TiendaRepositorioPuerto, ProductoRepositorioPuerto, ResenaRepositorioPuerto…
    │   │   │   │       ├── ia/            EmbeddingPuerto, AlmacenVectorialPuerto, ModeloLenguajePuerto
    │   │   │   │       ├── seguridad/     CifradorContrasenaPuerto, GeneradorTokenPuerto
    │   │   │   │       └── notificacion/  NotificadorPuerto
    │   │   │   └── casodeuso/             Implementaciones: ConsultarTiendasServicio, BuscarSemanticoServicio,
    │   │   │                              ChatearConLiaServicio (el flujo RAG), IndexarConocimientoServicio…
    │   │   │
    │   │   └── infraestructura/           🔵 ADAPTADORES · aquí sí hay Spring
    │   │       ├── adaptador/
    │   │       │   ├── entrada/
    │   │       │   │   ├── rest/          Controladores + dto/ (Solicitud, Respuesta) + mapeador/
    │   │       │   │   │                  + ManejadorErrores (excepción del dominio → HTTP)
    │   │       │   │   ├── webhook/       Lo que llama n8n (ej. resultado de moderación)
    │   │       │   │   └── programador/   Tareas programadas (ej. reindexar de noche)
    │   │       │   └── salida/
    │   │       │       ├── persistencia/  entidad/ (TiendaEntidad…), repositorio/ (Spring Data),
    │   │       │       │                  mapeador/, y los adaptadores que implementan *RepositorioPuerto
    │   │       │       ├── vector/        PgVectorAdaptador (JdbcTemplate + operador <=>) → AlmacenVectorialPuerto
    │   │       │       ├── gemini/        GeminiEmbeddingAdaptador → EmbeddingPuerto
    │   │       │       │                  GeminiChatAdaptador → ModeloLenguajePuerto
    │   │       │       ├── seguridad/     BCryptCifradorAdaptador, JwtGeneradorTokenAdaptador
    │   │       │       └── n8n/           N8nNotificadorAdaptador → NotificadorPuerto (llama webhooks)
    │   │       └── configuracion/         ConfiguracionBeans (registra los casos de uso), ConfiguracionSeguridad,
    │   │                                  ConfiguracionOpenApi (Swagger), ConfiguracionCors, PropiedadesGemini
    │   └── resources/
    │       ├── application.yml            Lee todo de variables de entorno (nada secreto aquí)
    │       └── prompts/                   Instrucciones de Lía (texto), fuera del código
    └── test/java/co/styleme/
        ├── arquitectura/                  ArquitecturaHexagonalTest (ArchUnit: vigila las reglas)
        ├── dominio/                       Pruebas del dominio (sin Spring, rapidísimas)
        ├── aplicacion/                    Pruebas de casos de uso con puertos falsos (Mockito)
        ├── infraestructura/               Pruebas de adaptadores (Testcontainers con pgvector)
        └── evaluacion/                    Evaluación de IA: 30 preguntas, precisión@k (rama test/evaluacion-ia)
```

---

## 5. Puertos y adaptadores de StyleMe

| Puerto (interfaz, en `aplicacion`) | Tipo | Adaptador (clase, en `infraestructura`) | Conecta con |
|---|---|---|---|
| `ConsultarTiendasCasoUso`, `ConsultarProductosCasoUso` | Entrada | `TiendaControlador`, `ProductoControlador` | Página web |
| `CalificarCasoUso`, `GestionarFavoritosCasoUso` | Entrada | `ResenaControlador`, `FavoritoControlador` | Página web |
| `ModerarResenaCasoUso` | Entrada | `ModeracionWebhookControlador` | n8n |
| `RegistrarEventoCasoUso`, `ConsultarIndicadoresCasoUso` | Entrada | `EventoControlador`, `IndicadorControlador` | Página web, panel admin, n8n |
| `BuscarSemanticoCasoUso` | Entrada | `BusquedaControlador` | Página web |
| `ChatearConLiaCasoUso` | Entrada | `LiaControlador` | Lía (página) |
| `IndexarConocimientoCasoUso` | Entrada | `AdminIndexacionControlador`, `ReindexacionProgramador` | Admin, n8n, tarea nocturna |
| `TiendaRepositorioPuerto`, `ProductoRepositorioPuerto`… | Salida | `TiendaPersistenciaAdaptador`… (JPA) | PostgreSQL |
| `AlmacenVectorialPuerto` | Salida | `PgVectorAdaptador` | pgvector (índice HNSW) |
| `EmbeddingPuerto` | Salida | `GeminiEmbeddingAdaptador` | Gemini (embeddings) |
| `ModeloLenguajePuerto` | Salida | `GeminiChatAdaptador` | Gemini (respuestas) |
| `NotificadorPuerto` | Salida | `N8nNotificadorAdaptador` | n8n (webhooks) |
| `CifradorContrasenaPuerto`, `GeneradorTokenPuerto` | Salida | `BCryptCifradorAdaptador`, `JwtGeneradorTokenAdaptador` | Spring Security |

### El RAG dentro del hexágono (`ChatearConLiaServicio`)
```
LiaControlador ──▶ ChatearConLiaCasoUso
                     │ 1. EmbeddingPuerto.generar(pregunta)            → GeminiEmbeddingAdaptador
                     │ 2. AlmacenVectorialPuerto.buscarParecidos(vector, 5)  → PgVectorAdaptador (HNSW)
                     │ 3. arma el prompt: reglas + fragmentos + historial corto
                     │ 4. ModeloLenguajePuerto.responder(prompt)        → GeminiChatAdaptador
                     │ 5. ValidadorRecomendaciones (DOMINIO) descarta productos inexistentes
                     │ 6. guarda el resumen (ConversacionRepositorioPuerto)
                     ▼
                 RespuestaLia
```
Si `ModeloLenguajePuerto` falla, el caso de uso responde con el **plan B** (reglas) y marca la conversación como `PLAN_B`.

---

## 6. Qué pasa con los archivos que ya existen

| Hoy | Destino | Cuándo (rama) |
|---|---|---|
| `*.html`, `assets/`, `data/` | `iu/` (antes `frontend/`) | 2 · `refactor/estructura-proyecto` |
| `planeacion/` | `docs/planeacion/` | 2 |
| `.env.ejemplo` | `.env.example` | 2 |
| `data/*.js` | `basedatos/03-datos-iniciales.sql` (y luego se borra) | 3 y 10 |
| `netlify/functions/lia.mjs` → reglas de Lía | `backend/src/main/resources/prompts/` | 6 · `feature/rag-lia` |
| `lia.mjs` → llamada a embeddings | `GeminiEmbeddingAdaptador` | 5 · `feature/embeddings-busqueda` |
| `lia.mjs` → similitud coseno en memoria | `PgVectorAdaptador` (la hace la base de datos con HNSW) | 5 |
| `lia.mjs` → validación de productos y emoción | `ValidadorRecomendaciones` (dominio) | 6 |
| `lia.mjs` → llamada a Gemini con streaming | `GeminiChatAdaptador` | 6 |
| `iu/assets/js/lia/plan-b.js` | Se queda (respaldo si el backend no responde) | — |
| `netlify/`, `netlify.toml` | Se borran | 6 y 11 |

---

## 7. Ejemplo parcial: el módulo de tiendas (molde para los demás)

> Modo tutoría: este es **el molde de una sola tienda**. Productos, reseñas, favoritos, etc. los escribe la autora
> siguiendo este patrón, y después se revisan.

**Dominio** (`dominio/modelo/catalogo/Tienda.java`) · sin anotaciones
```java
public record Tienda(Long id, String slug, String nombre, Categoria categoriaPrincipal,
                     Zona zona, Coordenadas coordenadas, boolean verificada) {
    public Tienda {
        if (slug == null || slug.isBlank()) throw new IllegalArgumentException("La tienda necesita slug");
    }
    public boolean apareceEnMapa() { return coordenadas != null; }   // regla del negocio
}
```

**Puerto de salida** (`aplicacion/puerto/salida/persistencia/TiendaRepositorioPuerto.java`)
```java
public interface TiendaRepositorioPuerto {
    List<Tienda> buscarActivas(String categoriaSlug);
    Optional<Tienda> buscarPorSlug(String slug);
}
```

**Puerto de entrada** (`aplicacion/puerto/entrada/catalogo/ConsultarTiendasCasoUso.java`)
```java
public interface ConsultarTiendasCasoUso {
    List<Tienda> listar(String categoriaSlug);
    Tienda obtener(String slug);   // lanza TiendaNoEncontradaException
}
```

**Caso de uso** (`aplicacion/casodeuso/ConsultarTiendasServicio.java`) · sin `@Service`
```java
public class ConsultarTiendasServicio implements ConsultarTiendasCasoUso {
    private final TiendaRepositorioPuerto repositorio;
    public ConsultarTiendasServicio(TiendaRepositorioPuerto repositorio) { this.repositorio = repositorio; }

    @Override public Tienda obtener(String slug) {
        return repositorio.buscarPorSlug(slug).orElseThrow(() -> new TiendaNoEncontradaException(slug));
    }
    // listar(...) → completar
}
```

**Registro del bean** (`infraestructura/configuracion/ConfiguracionBeans.java`)
```java
@Configuration
public class ConfiguracionBeans {
    @Bean
    ConsultarTiendasCasoUso consultarTiendas(TiendaRepositorioPuerto repositorio) {
        return new ConsultarTiendasServicio(repositorio);
    }
}
```

**Lo que falta en este molde (lo completa la autora):**
- `TiendaEntidad` + `TiendaJpaRepositorio` + `TiendaPersistenciaMapeador` + `TiendaPersistenciaAdaptador implements TiendaRepositorioPuerto`.
- `TiendaControlador` (usa `ConsultarTiendasCasoUso`) + `TiendaRespuesta` + `TiendaRestMapeador`.
- Traducir `TiendaNoEncontradaException` a 404 en `ManejadorErrores`.

---

## 8. Orden recomendado para construir cada módulo

```
1. Dominio (modelo + reglas)  →  2. Prueba del dominio
3. Puertos (in y out)         →  4. Caso de uso + prueba con puertos falsos
5. Adaptador de salida        →  6. Adaptador de entrada (REST)
7. Registrar en ConfiguracionBeans    →  8. Probar en Swagger / Postman
```
De adentro hacia afuera: así el centro nunca depende de los adaptadores.
