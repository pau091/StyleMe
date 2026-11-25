# Base de datos de StyleMe

**PostgreSQL + pgvector.** Diseño de las 16 tablas: [docs/planeacion/15-modelo-de-datos.md](../docs/planeacion/15-modelo-de-datos.md).

## Scripts

Se ejecutan **en orden alfabético** y **automáticamente** la primera vez que Docker crea la base
(se montan en `/docker-entrypoint-initdb.d/`).

| Archivo | Contenido | Rama |
|---|---|---|
| `01-esquema.sql` | Extensión `vector`, las 16 tablas, llaves y reglas `CHECK` | `feature/base-de-datos` |
| `02-indices.sql` | Índices de consulta y el índice vectorial **HNSW** de `fragmento_conocimiento` | `feature/base-de-datos` |
| `03-datos-iniciales.sql` | Catálogo real: 10 tiendas y 49 productos, con ingredientes y etiquetas | `feature/base-de-datos` |
| `04-datos-prueba.sql` | Usuarios, reseñas, favoritos y eventos **de prueba** (marcados como tales) | `feature/comunidad-analitica` |
| `05-vistas-indicadores.sql` | Vistas con los indicadores del informe de análisis de datos | `feature/comunidad-analitica` |

> Los embeddings **no** van en estos scripts: los calcula el backend con Gemini y los guarda en `fragmento_conocimiento`.

## Aplicar `04` y `05` a una base que ya existe (sin borrar nada)

Si la base se creó antes de la rama `feature/comunidad-analitica`, estos dos scripts no corrieron solos. Desde la raíz del proyecto, en PowerShell:
```
docker exec styleme-basedatos psql -U styleme -d styleme -f /docker-entrypoint-initdb.d/04-datos-prueba.sql
docker exec styleme-basedatos psql -U styleme -d styleme -f /docker-entrypoint-initdb.d/05-vistas-indicadores.sql
```
Los datos de prueba están marcados (`@prueba.styleme`, sesiones `prueba-N`, `detalle = {"prueba": true}`) y el final de `04-datos-prueba.sql` explica cómo borrarlos.

## Volver a crear la base desde cero

Los scripts solo corren cuando la base está vacía. Para repetirlos:
```
docker compose down -v
docker compose up -d
```
(`-v` borra el volumen con los datos.)
