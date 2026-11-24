# 12 · Lía, la asesora virtual de StyleMe

> **Qué es este documento:** la planeación completa de Lía, la asesora con inteligencia artificial que guía a cada persona dentro de una tienda 3D de StyleMe. Explica cómo se ve, cómo piensa, cómo conversa, con qué tecnología se construye y en qué orden.
>
> Convención: ✅ confirmado por la autora · 💡 sugerencia, pendiente de aprobar · ⏳ pendiente de decidir.
> Fecha: 2026-11-20.

---

## 1. La idea en un párrafo

Después de la intro (la calle, la tienda y las puertas que se abren), la persona **entra a una tienda 3D de StyleMe**. Ahí la recibe **Lía**, una asesora virtual en 3D. Lía conversa por texto o por voz y entiende lo que la persona necesita: *"tengo una boda el sábado, piel grasa, máximo 150.000"*. Responde con **productos reales** de las tiendas de Bucaramanga, **camina con la cámara** hasta el pasillo correcto, muestra los productos en los estantes y al final arma la **salida de compras** con la ruta para llegar.

**Por qué esto responde a lo que pide el profesor:** StyleMe deja de ser "una página de maquillaje". Pasa a ser una **experiencia guiada por IA** que usa tres técnicas reales:

| Técnica de IA | Para qué la usa Lía |
|---|---|
| **Modelo de lenguaje (LLM)** | Entender lo que escribe o dice la persona y conversar con naturalidad |
| **Embeddings + RAG** (búsqueda por significado) | Encontrar en el catálogo los productos que de verdad sirven, sin inventar ninguno |
| **Visión por computador** (MediaPipe, ya existe) | Probador virtual. A futuro, también tono de piel |

Estado: ✅ la autora confirmó la experiencia 3D, a Lía y que el bot debe ser funcional (D-032 a D-035).

---

## 2. Quién es Lía (personalidad)

| Rasgo | Cómo se nota |
|---|---|
| **Cercana** | Habla de "tú", saluda por el nombre si la persona se lo dice, usa frases cortas |
| **Elegante** | Vocabulario cuidado, sin exageraciones, máximo un emoji por mensaje (o ninguno) |
| **Local** | Conoce Bucaramanga: "en Cabecera", "cerca del Parque San Pío". Nada de modismos forzados |
| **Honesta** | Si algo no está en el catálogo, lo dice. Si no sabe el precio, dice que hay que confirmarlo con la tienda |
| **Prudente** | No diagnostica la piel ni el cabello, no promete tallas ni resultados. Ante una condición (acné severo, dermatitis, caída fuerte del cabello) recomienda ir al dermatólogo |

**Cómo saluda (ejemplo):**
> "¡Hola! Soy Lía, tu asesora en StyleMe. Te ayudo a encontrar ropa, maquillaje y cuidado para tu piel y tu cabello en tiendas de Bucaramanga. ¿Qué estás buscando hoy?"

Lía **siempre** se presenta como una IA. Nunca dice que es una persona.

---

## 3. Cómo se ve Lía (ficha de diseño del personaje)

> Referencia de rasgos: la imagen que compartió la autora, un rostro estilizado tipo muñeca de colección. Lía es un **personaje propio**: toma esos rasgos como inspiración, no copia la imagen.

### Rasgos
| Parte | Diseño |
|---|---|
| **Estilo general** | Estilizado tipo "muñeca de porcelana": piel lisa, proporciones suaves, cabeza un poco más grande que lo real. Limpio y elegante, **no** caricatura infantil ni hiperrealista (lo hiperrealista se ve inquietante en 3D) |
| **Cabello** | Largo, liso y brillante, **rubio miel**, con **raya en la mitad** y caída recta detrás de los hombros |
| **Ojos** | Grandes, **azules**, mirada tranquila y algo intensa, **pestañas largas y definidas** |
| **Cejas** | Rectas y suaves, castaño claro |
| **Piel** | Clara y cálida, con **pecas muy suaves** en nariz y mejillas y **rubor rosado** |
| **Labios** | Llenos, tono **nude rosado** |
| **Accesorios** | **Varios aretes plateados** pequeños en las orejas (aros y piercings) |
| **Ropa** | **Suéter de cuello tortuga negro**, de punto con brillo sutil. Un detalle de marca: un pequeño broche **rosa empolvado** (`#a35d6d`) en forma de "S" o de arco de vitrina |

### Expresiones que necesita
- **Neutra amable** (en reposo) y **sonrisa** (saludo, cuando encuentra algo).
- **Escuchando**: inclina un poco la cabeza.
- **Pensando**: mira hacia un lado y arriba.
- **Hablando**: boca que se mueve con las vocales (lip sync).
- **Parpadeo** automático cada 3 a 6 segundos y **mirada que sigue la cámara**.

### Gestos y animaciones del cuerpo
Reposo (respira y se balancea suave) · saludar con la mano · señalar un pasillo · caminar · mostrar un producto (mano abierta) · celebrar suave (cuando la persona arma su salida).

### Cómo se crea el modelo 3D 💡
| Opción | Qué es | Pros | Contras |
|---|---|---|---|
| **A. VRoid Studio → archivo VRM** (recomendada) | Programa **gratuito** para crear personajes 3D sin saber modelar, con controles deslizantes para cara, cabello y ropa | Gratis. El modelo es tuyo. El VRM ya trae **esqueleto, expresiones (feliz, parpadeo) y bocas (A, I, U, E, O)** listas para hablar | El estilo base es tipo anime: hay que ajustar proporciones y texturas para lograr el estilo "muñeca elegante" |
| B. Blender (gratis) desde cero | Modelar todo a mano | Control total del estilo | Muy difícil y lento para una persona que empieza |
| C. Generadores de 3D con IA | Crean un modelo 3D desde una imagen | Rápido para probar | El modelo sale **sin esqueleto ni expresiones**: no puede hablar ni moverse bien |
| D. Ilustración 2D animada | Lía dibujada por capas | Liviano | No es 3D (no cumple D-032) |

**Plan:** crear a Lía en **VRoid Studio**, exportarla en **VRM** y mostrarla en la página con **Three.js + three-vrm** (librerías gratuitas). Peso objetivo del modelo: **menos de 8 MB**, con texturas comprimidas.

---

## 4. El recorrido de la persona (experiencia 3D)

```
Intro (calle → puertas → luz)
   │
   ▼
Tienda 3D: recepción ── Lía saluda ── "¿Qué buscas hoy?"
   │                         │
   │        la persona escribe o habla
   │                         ▼
   │             Lía entiende y busca en el catálogo
   │                         ▼
   ├── Pasillo ROPA ─────────┐
   ├── Pasillo MAQUILLAJE ───┤  la cámara camina con Lía
   ├── Pasillo SKINCARE ─────┤  hasta el pasillo correcto
   └── Pasillo CAPILAR ──────┘
                             ▼
       Los productos aparecen en los estantes (tarjetas 3D)
                             ▼
   Tocar un producto → se acerca → Pruébatelo · Parecidos · WhatsApp · Cómo llegar
                             ▼
   "¿Te armo la salida de compras?" → la cámara sale a un mapa 3D de Cabecera
   y se dibuja la ruta por las tiendas
```

### La tienda 3D
- **Recepción** en el centro, con el mostrador y el letrero de StyleMe. Lía espera ahí.
- **Cuatro pasillos**, uno por categoría, cada uno con su color de acento y su letrero. Usan las mismas fotos de categorías de la página.
- **Estantes** donde aparecen las tarjetas de producto (foto, nombre, precio y tienda), en 3D y con luz.
- Luz cálida de boutique, piso claro y los arcos de vitrina de la intro, para que todo se sienta continuo.

### La pantalla ✅ (D-044: Lía es un personaje, no un chat)
- Lía aparece como **personaje** y habla con **globos de texto** que salen de ella. La persona le responde **ahí mismo**: toca una respuesta rápida o escribe en el campo bajo el globo.
- **Computador:** Lía en su arco de vitrina, abajo a la derecha (en 3D, dentro de la tienda). El globo sale a su izquierda, a la altura de su cara.
- **Celular:** Lía pequeña abajo y el globo encima, a lo ancho.
- "Conversación" muestra lo que se ha hablado. No hay ventana de chat.
- Hoy (antes del 3D) Lía es su retrato dentro del arco (`assets/js/lia/lia.js`): respira, se mueve al hablar y hace gestos según su emoción.
- Siempre visibles: botón 🎤 (hablar), botón 🔊 (que Lía hable en voz alta), "Ver como página" (modo ligero) y "Salir de la tienda".

### Modo ligero (obligatorio) ✅ recomendado
Para celulares lentos, "reducir movimiento" activado, o si el 3D no carga: **la misma conversación con Lía**, en una página normal, con Lía como imagen y los productos en tarjetas. Nadie se queda sin poder usar StyleMe.

---

## 5. Cómo funciona por dentro (arquitectura)

```
┌──────────────────────── NAVEGADOR (página estática, $0) ────────────────────────┐
│  Three.js + three-vrm → tienda 3D y Lía (expresiones, gestos, lip sync)         │
│  Chat de Lía (texto) + voz del navegador (escuchar y hablar)                    │
│  Ejecuta las ACCIONES que manda Lía: ir a un pasillo, mostrar productos, etc.   │
└───────────────────────────────┬─────────────────────────────────────────────────┘
                                │  mensaje de la persona + historia corta
                                ▼
┌──────────────── FUNCIÓN EN UN SERVIDOR GRATUITO (Netlify o Cloudflare) ─────────┐
│  1. Guarda la clave secreta de la IA (NUNCA va en la página)                    │
│  2. Busca en el catálogo con EMBEDDINGS los productos que sirven (RAG)          │
│  3. Le pide al MODELO DE LENGUAJE la respuesta, con reglas + esos productos     │
│  4. Devuelve la respuesta POCO A POCO (streaming) + las acciones para el 3D     │
│  5. Limita cuántos mensajes puede mandar cada persona (para no gastar de más)   │
└───────────────────────────────┬─────────────────────────────────────────────────┘
                                ▼
                    Proveedor del modelo de lenguaje (API)
```

### 5.1 Las piezas

| Pieza | Herramienta propuesta 💡 | Costo |
|---|---|---|
| 3D en la página | **Three.js** + **@pixiv/three-vrm** | $0 |
| Modelo de Lía | **VRoid Studio** → VRM | $0 |
| Animaciones (caminar, saludar) | Animaciones gratuitas compatibles con VRM (formato VRMA) | $0 |
| Servidor pequeño | **Netlify Functions** o **Cloudflare Workers** (plan gratuito) | $0 con límites |
| Modelo de lenguaje | ⏳ Un proveedor con plan gratuito (por ejemplo, Gemini). Se deja cambiable | $0 con límites |
| Embeddings | Un modelo de embeddings **multilingüe** (que entienda español) | $0 |
| Voz | **Web Speech API** del navegador (escuchar y hablar). En Edge hay voces naturales de Colombia | $0 |
| Datos | Los mismos `data/tiendas.js` y `data/productos.js` | $0 |

> ⚠️ Antes de elegir proveedor hay que revisar sus condiciones: límites del plan gratuito y **si usan las conversaciones para entrenar**, por la Ley 1581 de protección de datos.

### 5.2 RAG: cómo Lía encuentra productos sin inventarlos
1. **Una sola vez** (y cada vez que cambie el catálogo), un script convierte cada producto en un **embedding**. El texto que se convierte incluye nombre, marca, categoría, descripción, para quién es, objetivos y libre de. Se guarda en `data/embeddings.json`.
2. Cuando la persona escribe, su mensaje también se convierte en embedding.
3. Se comparan los números y se toman los **8 productos más parecidos**. Antes se aplican los filtros duros: categoría, presupuesto y tipo de piel.
4. Esos 8 productos, **y solo esos**, se le pasan al modelo de lenguaje para que responda.
5. Regla de oro: **Lía solo puede nombrar productos que estén en esa lista**. Si el modelo nombra uno que no está, la función lo detecta y lo quita.

Con estos mismos embeddings se construyen también el **buscador inteligente** de Productos y **"Productos parecidos"** en cada producto.

### 5.3 Herramientas de Lía (lo que puede "hacer")
El modelo de lenguaje no solo escribe: puede pedir **acciones**. La página las ejecuta en el 3D.

| Herramienta | Qué hace | Ejemplo de uso |
|---|---|---|
| `buscar_productos` | Busca en el catálogo (RAG) con filtros | "labial rojo que dure, máximo 70.000" |
| `info_tienda` | Dirección, horario, WhatsApp y cómo llegar | "¿dónde queda Pandora?" |
| `ir_a_pasillo` | Lía y la cámara caminan al pasillo | Después de buscar skincare |
| `mostrar_productos` | Pone los productos en los estantes y en el chat | Los 3 mejores resultados |
| `abrir_probador` | Abre el probador con una prenda | "¿cómo me quedaría?" |
| `armar_salida` | Arma el recorrido por tiendas | "¿me armas el plan?" |
| `contactar_tienda` | Prepara el mensaje de WhatsApp | "quiero preguntar si lo tienen" |

La respuesta de la función siempre trae dos partes:
```json
{
  "texto": "Para piel grasa te recomiendo este sérum con niacinamida. Lo tienen en Mirada de Ángel...",
  "acciones": [
    { "tipo": "ir_a_pasillo", "pasillo": "skincare" },
    { "tipo": "mostrar_productos", "ids": ["p035", "p031", "p037"] }
  ],
  "sugerencias": ["¿Y un protector solar?", "¿Dónde queda la tienda?", "Ármame la salida"]
}
```

### 5.4 Las reglas de Lía (instrucciones del sistema)
Se escriben una vez y se le envían al modelo en cada mensaje:
- Eres Lía, asesora virtual de StyleMe, una IA. Hablas en español, de "tú", en máximo 3 frases por turno.
- Solo recomiendas productos de la lista que te damos. **Nunca** inventas productos, precios, existencias ni tiendas.
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
| **Respuesta poco a poco (streaming)** | Lía empieza a "hablar" en menos de 1–2 segundos, en vez de hacer esperar la respuesta completa |
| **Estado "pensando"** | Mientras llega la respuesta, Lía mira hacia un lado y aparecen tres puntos. La espera se siente natural |
| **Turnos cortos** | Máximo 3 frases. La conversación se siente como hablar con alguien, no como leer un folleto |
| **Botones de respuesta rápida** | 2 o 3 sugerencias debajo de cada mensaje ("¿Y un protector?", "Ármame la salida") |
| **Una pregunta a la vez** | Si falta información (presupuesto, tipo de piel), Lía pregunta **solo una cosa** y mejor si ofrece opciones |
| **Recomienda pronto** | Con 2 datos ya muestra algo. Después afina ("¿Lo quieres más económico?") |
| **Memoria de la visita** | Recuerda lo que se habló (en `sessionStorage`) y lo que ya mostró, para no repetir |
| **Interrumpir** | Si la persona escribe o habla mientras Lía habla, Lía se calla y escucha |
| **Plan B sin IA** | Si la IA no responde (sin internet o sin cupo), Lía sigue funcionando con el **recomendador por reglas** que ya existe y lo avisa con naturalidad |
| **Saber cerrar** | Al final ofrece armar la salida, escribirle a la tienda o seguir explorando |

### Lo que Lía va descubriendo en la conversación
Lía no hace un formulario. Va completando estos datos **solo si hacen falta**:

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
> *[chips: Rutina diaria · Maquillaje · Las dos]*
> **Persona:** rutina diaria, máximo 50 mil
> **Lía:** *(camina con la cámara al pasillo de skincare)* Mira estos: el **Suero Niacinamida + Zinc de Trendy** ($32.000) ayuda a regular la grasa, y el **Jabón Facial Detox** ($16.000) con ácido salicílico. Los dos están en **Mirada de Ángel**.
> *[tarjetas 3D en el estante · chips: ¿Y un protector solar? · ¿Dónde queda? · Ármame la salida]*
> **Persona:** ármame la salida
> **Lía:** Listo. Te armé un recorrido corto por Cabecera. *(la cámara sale al mapa 3D y se dibuja la ruta)* ¿Quieres que le escriba a la tienda para confirmar si los tienen?

---

## 7. Seguridad, privacidad y ética

| Riesgo | Cómo se evita |
|---|---|
| Alguien roba la clave de la IA | La clave vive **solo** en el servidor (variables de entorno), nunca en la página ni en GitHub |
| Gastar todo el cupo gratis | Límite de mensajes por persona y por minuto; mensajes de máximo 500 caracteres |
| Lía inventa productos o precios | RAG + regla de oro + revisión automática de los productos que nombra |
| "Prompt injection" (alguien intenta que Lía diga otra cosa) | Reglas firmes; el texto de la persona nunca reemplaza las reglas; Lía vuelve al tema |
| Datos personales | No se piden ni se guardan. Las conversaciones no se guardan en ningún servidor de StyleMe. Aviso visible: "Lía es una IA; no compartas datos personales" |
| Temas de salud | Lía no diagnostica; sugiere dermatólogo (como el recomendador actual) |
| Menores de edad | El probador sigue siendo solo para mayores de 18 (R-04) |
| Accesibilidad | Todo lo que Lía dice también está escrito; el 3D tiene modo ligero; se puede usar con teclado |

---

## 8. Rendimiento

- El 3D **se descarga mientras corre la intro**, así que la espera queda escondida.
- Metas: Lía < 8 MB, tienda < 10 MB, texturas comprimidas. Las fotos de producto en los estantes son las versiones pequeñas.
- Meta de fluidez: 60 fps en celulares normales y 120 fps en computadores. Si el celular no puede, se bajan sombras y resolución, o se ofrece el modo ligero.
- Lía solo se anima cuando se ve en pantalla. Cuando la pestaña está oculta, todo se pausa.

---

## 9. Plan por fases

Cada fase termina con algo **que funciona** y se puede mostrar.

| Fase | Qué se construye | Resultado para mostrar |
|---|---|---|
| **0 · Preparación** | Decidir los pendientes (sección 11). Crear cuentas gratuitas (servidor e IA). Mejorar el catálogo (descripciones, "para quién es"). Script de embeddings | El catálogo listo para la IA |
| **1 · El cerebro de Lía (sin 3D)** | Función en el servidor + RAG + reglas + streaming + chat 2D + plan B con reglas. "Productos parecidos" y buscador inteligente con embeddings | **Lía ya conversa y recomienda bien**, en una página normal. ✅ *Chat, función, RAG y plan B programados el 2026-11-20 (falta probar con la clave real). Pendientes de esta fase: "productos parecidos" y buscador inteligente* |
| **2 · El cuerpo de Lía** | Modelo en VRoid → VRM. Escena 3D simple: Lía con reposo, parpadeo, mirada, expresiones y boca que se mueve al hablar | Lía en 3D conversando |
| **3 · La tienda 3D** | Recepción + 4 pasillos + estantes. Las acciones de Lía mueven la cámara y ponen productos. Unión con la intro | **La experiencia guiada completa** |
| **4 · Voz y salida 3D** | Hablar con Lía (micrófono), que Lía hable. Mapa 3D de Cabecera con la ruta. Animaciones de caminar y señalar | La experiencia "wow" |
| **5 · Pruebas y ajustes** | Pruebas con personas reales, evaluación de la IA (sección 10), rendimiento en celulares, accesibilidad, documento para el profesor | Proyecto pulido y sustentado |

**Por qué el cerebro va antes que el 3D:** si el 3D falla, Lía igual funciona. Si se hiciera al revés, habría una muñeca bonita que no sabe recomendar.

---

## 10. Cómo demostrar que la IA funciona (para la sustentación)

Un **conjunto de prueba de 30 preguntas** con la respuesta esperada. Ejemplos: "shampoo sin sulfatos para cabello rizado" → debe aparecer Cantu. "labial de menos de 50.000" → no debe aparecer ninguno más caro.

| Medida | Meta |
|---|---|
| El producto correcto aparece entre los 3 primeros | ≥ 80 % |
| Respuestas que nombran un producto que **no existe** | 0 % |
| Respeta el presupuesto | 100 % |
| Ante temas de salud, recomienda al dermatólogo | 100 % |
| Tiempo hasta la primera palabra de Lía | < 2 s |

Se compara **con y sin embeddings** (buscador de palabras vs. búsqueda por significado). Así se demuestra con números que la IA mejora la experiencia.

---

## 11. Decisiones pendientes ⏳

| # | Pregunta | Recomendación |
|---|---|---|
| 1 | ¿Qué proveedor de modelo de lenguaje? | Uno con plan gratuito y condiciones de privacidad claras. Dejar el código preparado para cambiarlo |
| 2 | ~~¿Netlify o Cloudflare?~~ | ✅ **Netlify** (D-038) |
| 3 | ~~¿El modelo 3D en VRoid?~~ | ✅ **VRoid Studio** (D-039). Guía en [13](13-lia-avatar-y-emociones.md) |
| 4 | ¿Lía habla en voz alta desde el inicio o solo si la persona lo activa? | Solo si la activa, como el sonido de la intro |
| 5 | ~~¿Fecha de entrega final?~~ | ✅ 1 semana o más, sin fecha exacta (D-041) |
| 6 | ~~¿Ampliar el catálogo?~~ | ✅ Por ahora no se toca (D-040) |

---

## 12. Riesgos principales

| Riesgo | Plan |
|---|---|
| El plan gratuito de la IA cambia o se acaba | Código cambiable de proveedor + plan B por reglas |
| El 3D es pesado en celulares | Modo ligero + metas de peso + descargar durante la intro |
| Hacer el modelo de Lía toma más de lo esperado | Fase 1 primero: Lía funciona en 2D mientras se termina el 3D |
| Respuestas raras de la IA | Reglas firmes, conjunto de prueba, revisión de productos nombrados |
| Datos de tiendas desactualizados | Lía siempre invita a confirmar con la tienda (como hoy) |
