# 15 · Modelo de datos (base de datos de StyleMe)

> Diseño de las tablas acordado el 2026-11-23 (D-050 a D-055). Es la guía para dibujar el
> **diagrama entidad-relación** y escribir los scripts SQL de la rama `feature/base-de-datos`.
>
> 💡 Motor sugerido: **PostgreSQL + pgvector** (guarda embeddings, los indexa con HNSW y busca por similitud; además tiene `JSONB` y buenas funciones de análisis).

---

## 1. Las 16 tablas

| # | Tabla | Grupo | De dónde sale |
|---|---|---|---|
| 1 | `categoria` | Catálogo | Ropa, maquillaje, skincare, capilar (D-004) |
| 2 | `zona` | Catálogo | Campo `zona` de `data/tiendas.js` |
| 3 | `usuario` | Comunidad | **Nueva** (roles comprador, comerciante, admin) |
| 4 | `tienda` | Catálogo | `data/tiendas.js` |
| 5 | `producto` | Catálogo | `data/productos.js` |
| 6 | `ingrediente` | Catálogo | Listas `ingredientes` de los productos |
| 7 | `producto_ingrediente` | Catálogo | Une productos con ingredientes |
| 8 | `etiqueta` | Catálogo | Listas `tipoPiel`, `tipoCabello`, `cuero`, `objetivos`, `libreDe` y el campo `paso` |
| 9 | `producto_etiqueta` | Catálogo | Une productos con etiquetas |
| 10 | `resena` | Comunidad | **Nueva**: reseñas de tiendas **y** de productos (D-051) |
| 11 | `favorito` | Comunidad | **Nueva**: favoritos de tiendas **y** de productos (D-052) |
| 12 | `evento` | Análisis | **Nueva**: cada acción en la página (para el informe) |
| 13 | `conversacion_lia` | Análisis | **Nueva**: tema de cada conversación con Lía (D-053) |
| 14 | `lia_recomendacion` | Análisis | **Nueva**: qué productos recomendó Lía en cada conversación |
| 15 | `fragmento_conocimiento` | IA (RAG) | **Nueva**: textos de tiendas, productos y guías con su **embedding** e índice vectorial (D-055) |
| 16 | `evaluacion_ia` | IA (pruebas) | **Nueva**: resultados de las pruebas automáticas de Lía y de la búsqueda (D-055) |

**Quedaron fuera (no son necesarias):**
- `salida` / `salida_tienda`: la salida de compras se registra como un `evento` de tipo `ARMAR_SALIDA`, con las tiendas en `detalle`.
- `mensaje_lia`: no se guarda cada mensaje, solo un resumen por conversación (D-053).

---

## 2. Relaciones

```
zona ──1───N── tienda ──1───N── producto ──N───1── categoria
                 │  N              │
categoria ──1────┘ (principal)     ├──N───M── ingrediente   (por producto_ingrediente)
                 │                 └──N───M── etiqueta      (por producto_etiqueta)
usuario ──1───N── tienda  (comerciante dueño, opcional)

usuario ──1───N── resena ──N───1── tienda   ┐ una reseña es de UNA tienda
                         └─N───1── producto ┘ o de UN producto, nunca de los dos
usuario ──1───N── favorito ─ (igual que resena)

evento ──N───1── usuario / tienda / producto / categoria   (todas opcionales)
conversacion_lia ──1───N── lia_recomendacion ──N───1── producto

fragmento_conocimiento ──N───1── tienda / producto   (o ninguno, si es una guía)
evaluacion_ia  (independiente: guarda cada ejecución de las pruebas)
```

**Leer así:** "una zona tiene muchas tiendas", "una tienda tiene muchos productos",
"un producto tiene muchos ingredientes y un ingrediente está en muchos productos".

---

## 3. Campos de cada tabla

**Convenciones**
- **PK** = llave primaria · **FK** = llave foránea (apunta a otra tabla) · **NN** = obligatorio · **U** = único.
- Los `id` son números automáticos (`BIGINT GENERATED ALWAYS AS IDENTITY` o `SMALLINT` en tablas pequeñas).
- Los valores de lista fija (roles, estados, tipos) se guardan como texto con una regla `CHECK`.
  En Java se leen como un `enum`.
- "No se sabe" se guarda como `NULL`, nunca como el texto "Por confirmar".

### 1 · `categoria`
| Campo | Tipo | Reglas |
|---|---|---|
| id | SMALLINT | PK |
| slug | VARCHAR(20) | NN, U · `ropa`, `maquillaje`, `skincare`, `capilar` |
| nombre | VARCHAR(40) | NN · "Cuidado capilar" |

### 2 · `zona`
| Campo | Tipo | Reglas |
|---|---|---|
| id | SMALLINT | PK |
| nombre | VARCHAR(60) | NN, U · "Cabecera", "Sur", "La Aurora" |

### 3 · `usuario`
| Campo | Tipo | Reglas |
|---|---|---|
| id | BIGINT | PK |
| nombre | VARCHAR(80) | NN |
| correo | VARCHAR(120) | NN, U |
| contrasena_hash | VARCHAR(100) | NN · **nunca** la contraseña real (se cifra con BCrypt) |
| rol | VARCHAR(15) | NN · CHECK: `COMPRADOR`, `COMERCIANTE`, `ADMIN` · por defecto `COMPRADOR` |
| activo | BOOLEAN | NN · por defecto `true` |
| fecha_registro | TIMESTAMP | NN · por defecto la fecha actual |

> Ley 1581: solo datos mínimos. Sin teléfono, dirección ni fecha de nacimiento.

### 4 · `tienda`
| Campo | Tipo | Reglas |
|---|---|---|
| id | BIGINT | PK |
| slug | VARCHAR(60) | NN, U · `"bohor"` (mantiene los enlaces `tienda.html?id=bohor`) |
| nombre | VARCHAR(100) | NN |
| descripcion | VARCHAR(500) | |
| categoria_principal_id | SMALLINT | NN, FK → categoria (antes `tipo`) |
| zona_id | SMALLINT | FK → zona · NULL si no se sabe |
| direccion | VARCHAR(150) | |
| lat, lng | NUMERIC(9,6) | NULL si no se sabe (entonces no sale en el mapa) |
| horario | VARCHAR(120) | NULL si no se sabe |
| whatsapp | VARCHAR(15) | solo números, con 57 |
| telefono | VARCHAR(15) | solo si no tiene WhatsApp |
| instagram | VARCHAR(60) | usuario sin @ |
| web | VARCHAR(150) | sin https:// |
| foto | VARCHAR(255) | ruta o enlace |
| verificado | BOOLEAN | NN · por defecto `false` |
| activa | BOOLEAN | NN · por defecto `true` (ocultar sin borrar) |
| comerciante_id | BIGINT | FK → usuario · NULL mientras ninguna tienda tenga dueño registrado |
| fecha_registro | TIMESTAMP | NN |

### 5 · `producto`
| Campo | Tipo | Reglas |
|---|---|---|
| id | BIGINT | PK |
| codigo | VARCHAR(10) | NN, U · `"p028"` (las fotos locales se llaman así) |
| tienda_id | BIGINT | NN, FK → tienda |
| categoria_id | SMALLINT | NN, FK → categoria |
| nombre | VARCHAR(150) | NN |
| marca | VARCHAR(80) | |
| precio | INTEGER | CHECK > 0 · NULL si no se sabe (pesos sin decimales) |
| descripcion | VARCHAR(500) | |
| foto | VARCHAR(500) | |
| fuente | VARCHAR(500) | enlace al producto en la tienda |
| fuente_ingredientes | VARCHAR(500) | antes `ingredientesFuente` |
| tipo_prenda | VARCHAR(10) | CHECK: `superior`, `inferior`, `completa` · solo ropa |
| foto_probador | VARCHAR(255) | solo ropa con PNG sin fondo |
| es_ejemplo | BOOLEAN | NN · antes `pendiente` (true = producto de ejemplo, no real) |
| activo | BOOLEAN | NN · por defecto `true` |
| fecha_registro | TIMESTAMP | NN |

### 6 · `ingrediente`
| Campo | Tipo | Reglas |
|---|---|---|
| id | BIGINT | PK |
| nombre | VARCHAR(120) | NN, U · nombre INCI: "Niacinamide" |

### 7 · `producto_ingrediente`
| Campo | Tipo | Reglas |
|---|---|---|
| producto_id | BIGINT | FK → producto · parte de la PK |
| ingrediente_id | BIGINT | FK → ingrediente · parte de la PK |
| orden | SMALLINT | NN · posición en la lista (1 = el que más tiene) |

### 8 · `etiqueta`
| Campo | Tipo | Reglas |
|---|---|---|
| id | SMALLINT | PK |
| grupo | VARCHAR(15) | NN · CHECK: `paso`, `tipo_piel`, `tipo_cabello`, `cuero`, `objetivo`, `libre_de` |
| valor | VARCHAR(30) | NN · "grasa", "rizado", "hidratar", "fragancia", "serum" |
| | | U (grupo, valor): no se repite la misma etiqueta |

### 9 · `producto_etiqueta`
| Campo | Tipo | Reglas |
|---|---|---|
| producto_id | BIGINT | FK → producto · parte de la PK |
| etiqueta_id | SMALLINT | FK → etiqueta · parte de la PK |

### 10 · `resena`
| Campo | Tipo | Reglas |
|---|---|---|
| id | BIGINT | PK |
| usuario_id | BIGINT | NN, FK → usuario |
| tienda_id | BIGINT | FK → tienda · se llena si la reseña es de una tienda |
| producto_id | BIGINT | FK → producto · se llena si la reseña es de un producto |
| corazones | SMALLINT | NN · CHECK entre 1 y 5 |
| comentario | VARCHAR(500) | |
| estado | VARCHAR(10) | NN · CHECK: `PENDIENTE`, `PUBLICADA`, `RECHAZADA` · por defecto `PENDIENTE` (lo cambia n8n) |
| fecha | TIMESTAMP | NN |

**Reglas especiales**
- `CHECK ((tienda_id IS NULL) <> (producto_id IS NULL))` → exactamente una de las dos.
- `UNIQUE (usuario_id, tienda_id)` y `UNIQUE (usuario_id, producto_id)` → una reseña por persona y por tienda/producto.
  (En PostgreSQL los `NULL` no chocan entre sí, así que estas dos reglas funcionan sin trucos.)

### 11 · `favorito`
| Campo | Tipo | Reglas |
|---|---|---|
| id | BIGINT | PK |
| usuario_id | BIGINT | NN, FK → usuario |
| tienda_id | BIGINT | FK → tienda |
| producto_id | BIGINT | FK → producto |
| fecha | TIMESTAMP | NN |

Mismas reglas especiales que `resena` (uno de los dos, sin repetir).

### 12 · `evento`
| Campo | Tipo | Reglas |
|---|---|---|
| id | BIGINT | PK |
| tipo | VARCHAR(25) | NN · CHECK: `VER_TIENDA`, `VER_PRODUCTO`, `CLIC_WHATSAPP`, `CLIC_COMO_LLEGAR`, `BUSQUEDA`, `FILTRAR_CATEGORIA`, `USAR_PROBADOR`, `USAR_RECOMENDADOR`, `ARMAR_SALIDA`, `ABRIR_LIA` |
| sesion_id | VARCHAR(36) | NN · código anónimo de la visita |
| usuario_id | BIGINT | FK → usuario · NULL si no inició sesión |
| tienda_id | BIGINT | FK → tienda |
| producto_id | BIGINT | FK → producto |
| categoria_id | SMALLINT | FK → categoria |
| texto_busqueda | VARCHAR(100) | solo en `BUSQUEDA` |
| cantidad_resultados | INTEGER | solo en `BUSQUEDA` (0 = no encontró nada) |
| dispositivo | VARCHAR(10) | CHECK: `celular`, `computador` |
| detalle | JSONB | datos extra: tipo de piel elegido, tiendas de la salida… |
| fecha_hora | TIMESTAMP | NN |

> Índice sugerido: `(tipo, fecha_hora)`, porque casi todas las consultas del informe filtran por eso.

### 13 · `conversacion_lia`
Un registro por conversación (**no** por mensaje). No se guarda lo que escribe la persona.

| Campo | Tipo | Reglas |
|---|---|---|
| id | BIGINT | PK |
| sesion_id | VARCHAR(36) | NN |
| usuario_id | BIGINT | FK → usuario · NULL si es anónima |
| tema | VARCHAR(12) | NN · CHECK: `catalogo`, `general`, `social`, `aclaracion` · la intención que más se repitió |
| categoria_id | SMALLINT | FK → categoria · pasillo del que más se habló (NULL si no fue de compras) |
| sentimiento_final | VARCHAR(10) | CHECK: `positivo`, `negativo`, `neutral` |
| cantidad_mensajes | SMALLINT | NN |
| modo | VARCHAR(6) | NN · CHECK: `IA`, `PLAN_B`, `MIXTO` (si la IA falló y entró el plan B) |
| similitud_promedio | NUMERIC(4,3) | qué tan parecido era el contexto recuperado (0 a 1) |
| latencia_promedio_ms | INTEGER | cuánto tardó Lía en responder, en promedio |
| tokens_entrada | INTEGER | tokens enviados a Gemini (Gemini los informa en cada respuesta) |
| tokens_salida | INTEGER | tokens que respondió Gemini |
| inicio | TIMESTAMP | NN |
| fin | TIMESTAMP | |

### 14 · `lia_recomendacion`
| Campo | Tipo | Reglas |
|---|---|---|
| conversacion_id | BIGINT | FK → conversacion_lia · parte de la PK |
| producto_id | BIGINT | FK → producto · parte de la PK |
| veces | SMALLINT | NN · cuántas veces lo recomendó en esa conversación |

### 15 · `fragmento_conocimiento` (el corazón del RAG)
Cada fila es un **trozo de texto** que Lía puede consultar, con su embedding.
Ejemplo: *"BOHOR · ropa · Carrera 36 #53-32, Cabecera · Lun–Sáb 10:00–19:00"* o
*"Sérum Centella SKIN1004 · skincare · piel: todas · objetivo: hidratar · $85.001"*.

| Campo | Tipo | Reglas |
|---|---|---|
| id | BIGINT | PK |
| tipo_fuente | VARCHAR(10) | NN · CHECK: `tienda`, `producto`, `guia` |
| tienda_id | BIGINT | FK → tienda · si el fragmento habla de una tienda |
| producto_id | BIGINT | FK → producto · si habla de un producto |
| titulo | VARCHAR(150) | NN |
| texto | TEXT | NN · lo que se convierte en embedding y se le pasa a Gemini como contexto |
| hash_texto | CHAR(64) | NN · huella del texto: si no cambió, **no** se recalcula el embedding (ahorra tokens) |
| embedding | VECTOR(768) | NN · tipo de pgvector; 768 números = el "significado" del texto |
| modelo_embedding | VARCHAR(40) | NN · ej. `gemini-embedding-001` (si se cambia el modelo, se sabe qué recalcular) |
| actualizado | TIMESTAMP | NN |

**Índice vectorial:** `CREATE INDEX ... USING hnsw (embedding vector_cosine_ops)`.
HNSW organiza los vectores como un grafo para encontrar los más parecidos **sin compararlos todos**.
La búsqueda usa el operador `<=>` (distancia coseno): `ORDER BY embedding <=> :vector_pregunta LIMIT 5`.

> Las **guías** son textos cortos escritos por la autora (rutina básica de skincare, cómo elegir shampoo según el cabello…).
> Le dan a Lía conocimiento que no está en el catálogo y hacen el RAG más completo.

### 16 · `evaluacion_ia`
Una fila por pregunta en cada ejecución de las pruebas (rama `test/evaluacion-ia`).

| Campo | Tipo | Reglas |
|---|---|---|
| id | BIGINT | PK |
| ejecucion | VARCHAR(36) | NN · código de la ejecución (todas las preguntas de una corrida comparten el mismo) |
| pregunta | VARCHAR(300) | NN |
| tipo_pregunta | VARCHAR(12) | NN · CHECK: `catalogo`, `fuera_tema`, `salud`, `ambigua`, `general` |
| esperado | JSONB | NN · productos o comportamiento esperado (ej. `{"productos":["p028"],"debe_rechazar":false}`) |
| obtenido | JSONB | NN · lo que devolvió el sistema |
| precision_k | NUMERIC(4,3) | de los k resultados, qué fracción era correcta |
| recall_k | NUMERIC(4,3) | de los esperados, qué fracción se encontró |
| sin_alucinacion | BOOLEAN | NN · `true` si no inventó productos |
| aprobada | BOOLEAN | NN |
| latencia_ms | INTEGER | |
| fecha | TIMESTAMP | NN |

---

## 4. Cómo pasan los datos de `data/*.js` a las tablas

| Hoy en `data/*.js` | En la base de datos |
|---|---|
| `tienda.id: "bohor"` | `tienda.slug = 'bohor'` (y un `id` numérico nuevo) |
| `tienda.tipo: "ropa"` | `tienda.categoria_principal_id` → id de `ropa` |
| `tienda.zona: "Por confirmar"` | `tienda.zona_id = NULL` |
| `horario: "Por confirmar"` | `horario = NULL` |
| `producto.id: "p028"` | `producto.codigo = 'p028'` |
| `producto.tiendaId: "pandora-makeup"` | `producto.tienda_id` → id de esa tienda |
| `pendiente: true` | `es_ejemplo = true` |
| `ingredientes: ["Water", "Glycerin"]` | 2 filas en `producto_ingrediente` (orden 1 y 2) |
| `tipoPiel: ["mixta", "grasa"]` | 2 filas en `producto_etiqueta` (grupo `tipo_piel`) |
| `paso: "serum"` | 1 fila en `producto_etiqueta` (grupo `paso`) |

> En esta rama se pasan **los mismos** 10 tiendas y 49 productos de hoy. El catálogo se completa después, en la rama `feature/catalogo-completo` (D-064).

---

## 5. Lía sin gastar tokens de más (D-053)

1. **El tema sale gratis.** Gemini ya devuelve `intencion`, `sentimiento` y `productos` en el JSON de cada respuesta.
   El backend solo los **cuenta** durante la conversación y, al terminar, guarda el resumen en `conversacion_lia`.
   No se hace ninguna llamada extra a la IA.
2. **Embeddings guardados.** La antigua función de Netlify (ya borrada) calculaba los embeddings de los 49 productos cada vez que "despertaba".
   Con `fragmento_conocimiento` se calculan **una sola vez**; `hash_texto` avisa si el texto cambió y solo entonces se recalcula.
   Una pregunta a Lía gasta 1 embedding (el de la pregunta) + 1 respuesta de Gemini.
3. **Historial corto.** A Gemini se le envían solo los últimos mensajes de la conversación, no toda la historia (la función actual ya lo hace con `MAX_TURNOS`).
4. **Contexto justo.** Solo se pasan a Gemini los 5 fragmentos más parecidos que superen un umbral de similitud, no el catálogo entero.

### Flujo RAG completo
```
Pregunta → embedding de la pregunta → pgvector (HNSW) trae los 5 fragmentos más parecidos
        → prompt = reglas + fragmentos + historial corto → Gemini responde
        → se validan los productos (deben existir) → respuesta + resumen en conversacion_lia
```

---

## 6. Indicadores que permite este modelo (para el informe)

| Indicador | Tablas |
|---|---|
| Tiendas y productos mejor calificados | `resena` |
| Conversión: visitas a una tienda → clics en WhatsApp | `evento` |
| Búsquedas sin resultados (lo que la gente busca y no hay) | `evento` |
| Categorías y productos más vistos y más guardados en favoritos | `evento`, `favorito`, `categoria` |
| Precio promedio, mínimo y máximo por categoría y tienda | `producto` |
| Actividad por zona de la ciudad | `evento`, `tienda`, `zona` |
| Horas y días con más actividad; celular vs. computador | `evento` |
| Temas de conversación con Lía y % de veces que falló la IA | `conversacion_lia` |
| Productos que más recomienda Lía | `lia_recomendacion` |
| Ingredientes más comunes en skincare y capilar | `producto_ingrediente` |
| Calidad de la IA: precisión@k, % sin alucinaciones, latencia | `evaluacion_ia` |
| Costo de la IA: tokens por conversación; similitud promedio del contexto | `conversacion_lia` |
| Calidad de los datos: % de tiendas verificadas y de productos reales | `tienda`, `producto` |

---

## 7. Ejemplo parcial para el diagrama (dbdiagram.io)

En [dbdiagram.io](https://dbdiagram.io/) el diagrama se escribe con un lenguaje sencillo (DBML).
Aquí van **dos tablas** como molde; las otras 14 se escriben igual siguiendo la sección 3.

```
Table categoria {
  id smallint [pk]
  slug varchar(20) [not null, unique]
  nombre varchar(40) [not null]
}

Table tienda {
  id bigint [pk]
  slug varchar(60) [not null, unique]
  nombre varchar(100) [not null]
  categoria_principal_id smallint [not null, ref: > categoria.id]
  // ... sigue con los demás campos de la sección 3
}
```

`ref: > categoria.id` significa "muchas tiendas apuntan a una categoría".
Al terminar: **Export → PNG** y guardar en `docs/diagramas/entidad-relacion.png` (y el texto DBML en `docs/diagramas/entidad-relacion.dbml`).
