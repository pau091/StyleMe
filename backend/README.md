# Backend de StyleMe

API en **Spring Boot** con **arquitectura hexagonal** (puertos y adaptadores).
Diseño completo: [docs/planeacion/16-arquitectura-hexagonal.md](../docs/planeacion/16-arquitectura-hexagonal.md).

## Capas

```
src/main/java/co/styleme/
├── dominio/             🟢 Modelo y reglas del negocio (Java puro)
│   ├── modelo/
│   ├── servicio/
│   └── excepcion/
├── aplicacion/          🟡 Puertos y casos de uso (Java puro)
│   ├── puerto/entrada/      interfaces ...CasoUso
│   ├── puerto/salida/       interfaces ...Puerto
│   └── casodeuso/           implementaciones ...Servicio
└── infraestructura/     🔵 Adaptadores (Spring)
    ├── adaptador/entrada/   REST, webhooks de n8n, tareas programadas
    ├── adaptador/salida/    PostgreSQL, pgvector, Gemini, seguridad, n8n
    └── configuracion/       beans, seguridad, Swagger, CORS
```

Cada carpeta tiene un `package-info.java` que explica qué va ahí y qué regla cumple.

**Dependencias:** `infraestructura` → `aplicacion` → `dominio`. Nunca al revés.
Una prueba con ArchUnit (`ArquitecturaHexagonalTest`) lo comprueba automáticamente.

## Crear el proyecto (rama `feature/backend-base`)

En [start.spring.io](https://start.spring.io/):

| Campo | Valor |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | la versión estable que aparezca por defecto |
| Group | `co.styleme` |
| Artifact | `backend` |
| **Package name** | **`co.styleme`** ⚠️ (Initializr propone `co.styleme.backend`: hay que cambiarlo para que coincida con estas carpetas) |
| Java | 21 |
| Dependencias | Spring Web, Spring Data JPA, PostgreSQL Driver, Validation |

Descomprimir **dentro de esta carpeta** `backend/`: se agregan `pom.xml`, `mvnw` y la clase principal, sin borrar las capas.

## Ejecutar

```
cd backend
./mvnw spring-boot:run
```
Necesita la base de datos encendida (`docker compose up` en la raíz) y las variables de `.env`.
