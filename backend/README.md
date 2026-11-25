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

## Capas

```
src/main/java/co/styleme/
├── dominio/                🟢 Modelo y reglas del negocio (Java puro)
│   ├── modelo/catalogo/        Tienda, Producto, Categoria, Zona, Etiqueta, Coordenadas, Contacto...
│   ├── servicio/
│   └── excepcion/              TiendaNoEncontradaException, DatoInvalidoException...
├── aplicacion/             🟡 Puertos y casos de uso (Java puro)
│   ├── puerto/entrada/         interfaces ...CasoUso   (lo que StyleMe permite hacer)
│   ├── puerto/salida/          interfaces ...Puerto    (lo que StyleMe necesita de afuera)
│   └── casodeuso/              implementaciones ...Servicio
└── infraestructura/        🔵 Adaptadores (aquí sí hay Spring)
    ├── adaptador/entrada/rest/     Controladores, DTO, ManejadorErrores
    ├── adaptador/salida/persistencia/  Entidades JPA, repositorios Spring Data, adaptadores
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
| Persistencia | `test/.../persistencia/` | Los adaptadores JPA contra los datos reales | Sí |
| API (punta a punta) | `test/.../rest/` | Peticiones HTTP reales: 200, 400, 404 y Swagger | Sí |

## Configuración (`src/main/resources/application.yml`)

- **Puerto 8090** (en este computador el 8080 y el 8081 los usan otros proyectos).
- La conexión a la base se arma con las variables del `.env`: nunca hay contraseñas en el código.
- `ddl-auto: validate`: Hibernate **comprueba** que las entidades coincidan con las tablas, pero nunca las cambia.
  Las tablas las manda [basedatos/01-esquema.sql](../basedatos/01-esquema.sql).
