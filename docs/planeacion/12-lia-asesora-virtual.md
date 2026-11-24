# 12 · Lía, la asesora virtual de StyleMe

> **Qué es este documento:** la planeación de Lía, la asesora con inteligencia artificial de StyleMe: quién es,
> cómo se ve, cómo conversa, cómo funciona por dentro y cómo se demuestra que la IA funciona.
>
> Convención: ✅ confirmado por la autora · 💡 sugerencia, pendiente de aprobar · ⏳ pendiente de decidir.
> Creado: 2026-11-20 · Actualizado: 2026-11-24 (Lía queda en **2D**; el 3D se canceló, D-062).

---

## 1. La idea en un párrafo

En todas las páginas de StyleMe aparece **Lía**, una asesora virtual con forma de **personaje 2D** dentro de un arco
de vitrina. Lía conversa con globos de texto y entiende lo que la persona necesita: *"tengo una boda el sábado, piel grasa,
máximo 150.000"*. Responde con **productos reales** de las tiendas de Bucaramanga, los muestra en tarjetas y ofrece
acciones: ver la tienda, escribirle por WhatsApp, probarse la prenda o armar la salida de compras.

| Técnica de IA | Para qué la usa Lía |
|---|---|
| **Modelo de lenguaje (LLM)** | Entender lo que escribe la persona y conversar con naturalidad |
| **Embeddings + búsqueda semántica** | Encontrar en el catálogo lo que de verdad sirve, por significado y no solo por palabras |
| **RAG** (recuperar → aumentar → generar) | Responder solo con información real de la base de datos, sin inventar productos |
| **Visión por computador** (MediaPipe) | Probador virtual de ropa (ya existe, corre en el navegador) |

Estado: ✅ Lía es un bot funcional (D-035), es un personaje y no una ventana de chat (D-044), y queda en 2D (D-062).

---

## 2. Quién es Lía (personalidad)

| Rasgo | Cómo se nota |
|---|---|
| **Cercana** | Habla de "tú", usa frases cortas |
| **Elegante** | Vocabulario cuidado, sin exageraciones, máximo un emoji por mensaje (o ninguno) |
| **Local** | Conoce Bucaramanga: "en Cabecera", "cerca del Parque San Pío". Nada de modismos forzados |
| **Honesta** | Si algo no está en el catálogo, lo dice. Si no sabe el precio, dice que hay que confirmarlo con la tienda |
| **Prudente** | No diagnostica la piel ni el cabello, no promete tallas ni resultados. Ante una condición (acné severo, dermatitis, caída fuerte del cabello) recomienda ir al dermatólogo |

**Cómo saluda (ejemplo):**
> "¡Hola! Soy Lía, tu asesora en StyleMe. Te ayudo a encontrar ropa, maquillaje y cuidado para tu piel y tu cabello en tiendas de Bucaramanga. ¿Qué estás buscando hoy?"

Lía **siempre** se presenta como una IA. Nunca dice que es una persona.

---

## 3. Cómo se ve Lía ✅

Lía es un **retrato 2D** de estilo limpio y elegante (D-034): cabello largo, liso y rubio miel con raya al medio,
ojos azules, pestañas largas, pecas suaves, aretes plateados y suéter de cuello tortuga negro.

### Expresiones (archivos en `iu/assets/lia/`)
| Expresión | Archivo | Cuándo aparece |
|---|---|---|
| Neutra amable | `principal.webp` | En reposo |
| Felicidad | `feliz.webp` | Saluda, encuentra algo, la persona agradece |
| Duda | `confundida.webp` | No entendió o le falta un dato (aparece un globo con "?") |
| Asombro | `sorprendida.webp` | Algo inesperado o muy positivo |

### Movimientos (en `iu/assets/lia/lia-2d.css`)
- **Hablar:** la boca se mueve mientras llega el texto.
- **Parpadeo** automático.
- **Mirada:** el retrato se inclina un poco hacia el cursor.
- **Gestos de cabeza:** asiente cuando está feliz, se inclina cuando duda o piensa.
- Todo respeta `prefers-reduced-motion` y anima solo `transform` y `opacity` (fluido a 120 fps).

---

## 4. La pantalla ✅ (D-044: Lía es un personaje, no un chat)

- Lía aparece abajo a la derecha, en su **arco de vitrina**. Cerrada se ve pequeña y saluda con un globo corto.
- Al abrirla, habla con **globos de texto** que salen de ella. La persona responde **ahí mismo**: toca una respuesta
  rápida o escribe en el campo bajo el globo.
- **Celular:** Lía pequeña abajo y el globo encima, a lo ancho.
- "Conversación" muestra lo que se ha hablado durante la visita.
- Los productos que recomienda aparecen como **tarjetas** dentro del globo, con acciones (ver tienda, WhatsApp, probador, salida).

---

## 5. Cómo funciona por dentro

> Arquitectura del backend: [16 - Arquitectura hexagonal](16-arquitectura-hexagonal.md) ·
> Tablas: [15 - Modelo de datos](15-modelo-de-datos.md).

```
┌──────────────── iu/ (navegador) ────────────────┐
│  Lía: globos, expresiones, tarjetas y acciones  │
│  Plan B por reglas si el backend no responde    │
└───────────────────────┬─────────────────────────┘
                        │  POST /api/lia/chat (mensaje + historia corta)
                        ▼
┌──────────── backend/ (Spring Boot, hexagonal) ────────────┐
│  ChatearConLiaServicio (flujo RAG):                        │
│   1. EmbeddingPuerto       → embedding de la pregunta      │
│   2. AlmacenVectorialPuerto → 5 fragmentos más parecidos   │
│   3. Prompt = reglas + fragmentos + historial corto        │
│   4. ModeloLenguajePuerto  → respuesta por partes          │
│   5. ValidadorRecomendaciones → descarta lo inexistente    │
│   6. Guarda el resumen en conversacion_lia                 │
└──────┬──────────────────────┬──────────────────────────────┘
       ▼                      ▼
 PostgreSQL + pgvector     Gemini (embeddings + respuestas)
 (índice HNSW)
```

> **Hoy (temporal):** este flujo vive en `netlify/functions/lia.mjs` con los vectores en memoria.
> En la Fase 3 (ramas `feature/embeddings-busqueda` y `feature/rag-lia`) pasa al backend con pgvector.

### 5.1 RAG: cómo Lía encuentra información sin inventarla
1. **Indexación (una vez, y cuando algo cambia):** tiendas, productos y guías se convierten en **fragmentos de texto**;
   cada fragmento se convierte en un **embedding** y se guarda en `fragmento_conocimiento`. Un hash evita recalcular
   lo que no cambió.
2. **Recuperar:** el mensaje de la persona también se convierte en embedding y pgvector trae los fragmentos más parecidos
   (distancia coseno sobre el índice HNSW), después de aplicar filtros duros: categoría, presupuesto, tipo de piel.
3. **Aumentar:** solo esos fragmentos se le pasan al modelo de lenguaje, junto con las reglas.
4. **Generar y validar:** el modelo responde; el backend revisa que cada producto nombrado **exista** y quita el que no.

Con los mismos embeddings funciona el **buscador semántico** del catálogo (`GET /api/busqueda?q=`).

### 5.2 Acciones de Lía
El modelo de lenguaje no solo escribe: devuelve **acciones** que la página muestra como botones.

| Acción | Qué hace |
|---|---|
| `ver_tienda` | Abre el perfil de la tienda |
| `contactar_tienda` | Prepara el mensaje de WhatsApp |
| `abrir_probador` | Abre el probador con una prenda |
| `armar_salida` | Arma el recorrido por tiendas |

La respuesta siempre trae: el **texto**, la **emoción**, los **productos** recomendados, las **acciones** y 2 o 3 **sugerencias**
de respuesta rápida.

### 5.3 Las reglas de Lía (instrucciones del sistema)
Se guardan como plantilla en `backend/src/main/resources/prompts/` y se envían en cada mensaje:
- Eres Lía, asesora virtual de StyleMe, una IA. Hablas en español, de "tú", en máximo 3 frases por turno.
- Solo recomiendas productos de la información que te damos. **Nunca** inventas productos, precios, existencias ni tiendas.
- Si falta el precio, dices "precio por confirmar con la tienda".
- No haces diagnósticos médicos ni dermatológicos. Ante condiciones de salud, sugieres un dermatólogo.
- No prometes tallas ni resultados exactos. El probador es una referencia.
- StyleMe no vende: la compra se hace en la tienda. Ofreces WhatsApp o "cómo llegar".
- Si te piden algo fuera de moda y belleza, lo dices con amabilidad y vuelves al tema.
- No pides datos personales: ni cédula, ni dirección, ni fotos.

---

## 6. Para que la conversación sea fluida

| Técnica | Qué logra |
|---|---|
| **Respuesta poco a poco (streaming)** | Lía empieza a "hablar" en 1–2 segundos, en vez de hacer esperar la respuesta completa |
| **Estado "pensando"** | Mientras llega la respuesta aparecen tres puntos y Lía inclina la cabeza |
| **Turnos cortos** | Máximo 3 frases |
| **Respuestas rápidas** | 2 o 3 sugerencias debajo de cada mensaje ("¿Y un protector?", "Ármame la salida") |
| **Una pregunta a la vez** | Si falta información (presupuesto, tipo de piel), pregunta **solo una cosa** |
| **Recomienda pronto** | Con 2 datos ya muestra algo; después afina |
| **Memoria de la visita** | Recuerda lo hablado durante la visita (`sessionStorage`) |
| **Plan B sin IA** | Si la IA no responde, Lía sigue con reglas y lo avisa con naturalidad |
| **Saber cerrar** | Al final ofrece armar la salida, escribirle a la tienda o seguir explorando |

### Lo que Lía va descubriendo en la conversación

| Dato | Ejemplo | ¿Obligatorio? |
|---|---|---|
| Qué busca (categoría) | maquillaje | Sí (lo deduce casi siempre) |
| Para qué / ocasión | boda, diario, trabajo | Útil |
| Presupuesto | máximo 150.000 | Útil |
| Tipo de piel o de cabello | piel grasa, cabello rizado | Para skincare y capilar |
| Preferencias | sin fragancia, vegano, sin sulfatos | Opcional |
| Zona | Cabecera, Sur | Para la salida de compras |

### Ejemplo de conversación

> **Lía:** ¡Hola! Soy Lía, tu asesora en StyleMe. ¿Qué estás buscando hoy?
> **Persona:** algo para que no me brille la cara, tengo la piel grasa
> **Lía:** Te entiendo, la piel grasa necesita control sin resecar. ¿Buscas algo para tu rutina diaria o un maquillaje que dure?
> *[respuestas rápidas: Rutina diaria · Maquillaje · Las dos]*
> **Persona:** rutina diaria, máximo 50 mil
> **Lía:** Mira estos: el **Suero Niacinamida + Zinc** ($32.000) ayuda a regular la grasa, y el **Jabón Facial Detox** ($16.000) con ácido salicílico. Los dos están en **Mirada de Ángel**.
> *[tarjetas de producto · respuestas rápidas: ¿Y un protector solar? · ¿Dónde queda? · Ármame la salida]*

---

## 7. Seguridad, privacidad y ética

| Riesgo | Cómo se evita |
|---|---|
| Alguien roba la clave de la IA | La clave vive **solo** en el servidor (variables de entorno), nunca en la página ni en GitHub |
| Gastar todo el cupo gratis | Límite de mensajes por persona y por minuto; mensajes de máximo 500 caracteres; embeddings guardados (no se recalculan) |
| Lía inventa productos o precios | RAG + regla de oro + validación automática de los productos que nombra |
| "Prompt injection" | Reglas firmes; el texto de la persona nunca reemplaza las reglas; Lía vuelve al tema |
| Datos personales | No se piden. De cada conversación solo se guarda un **resumen sin el texto** (D-053). Aviso visible: "Lía es una IA; no compartas datos personales" |
| Temas de salud | Lía no diagnostica; sugiere dermatólogo |
| Menores de edad | El probador sigue siendo solo para mayores de 18 (R-04) |
| Accesibilidad | Todo lo que Lía dice está escrito; se puede usar con teclado; respeta "reducir movimiento" |

---

## 8. Cómo demostrar que la IA funciona (para la sustentación)

Un **conjunto de prueba de 30 preguntas** con la respuesta esperada (rama `test/evaluacion-ia`). Ejemplos:
"shampoo sin sulfatos para cabello rizado" → debe aparecer Cantu. "labial de menos de 50.000" → no debe aparecer ninguno más caro.

| Medida | Meta |
|---|---|
| El producto correcto aparece entre los 3 primeros (precisión@3) | ≥ 80 % |
| Respuestas que nombran un producto que **no existe** | 0 % |
| Respeta el presupuesto | 100 % |
| Ante temas de salud, recomienda al dermatólogo | 100 % |
| Tiempo hasta la primera palabra de Lía | < 2 s |

Se compara **con y sin embeddings** (buscador de palabras frente a búsqueda por significado). Así se demuestra con números
que la IA mejora la experiencia. Los resultados se guardan en la tabla `evaluacion_ia`.

---

## 9. Decisiones pendientes ⏳

| # | Pregunta | Recomendación |
|---|---|---|
| 1 | ¿Lía habla en voz alta? | Solo si la persona lo activa, como el sonido de la intro (P-014) |

---

## 10. Riesgos principales

| Riesgo | Plan |
|---|---|
| El plan gratuito de la IA cambia o se acaba | Puerto `ModeloLenguajePuerto`: cambiar de proveedor es escribir otro adaptador + plan B por reglas |
| Respuestas raras de la IA | Reglas firmes, conjunto de prueba, validación de productos nombrados |
| Datos de tiendas desactualizados | Lía siempre invita a confirmar con la tienda |
