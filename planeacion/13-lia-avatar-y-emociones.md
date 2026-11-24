# 13 · Lía: avatar 3D, movimientos y emociones

> Continúa [12 - Lía, asesora virtual](12-lia-asesora-virtual.md). Aquí está **cómo se crea Lía en VRoid Studio**, **cómo se mueve con fluidez** y **cómo muestra emociones**.
> Convención: ✅ confirmado · 💡 sugerencia · ⏳ pendiente. Fecha: 2026-11-20.

**Referencia oficial del avatar** ✅ (D-036):
- `assets/lia/3d/lia-referencia.png`: original, para diseñar en VRoid.
- `assets/lia/2d/`: Lía en 2D (cuatro expresiones en `.webp` + `lia-2d.css`). Es la que se usa mientras no exista `lia.vrm`.

---

## 1. Ficha del avatar (según la imagen de referencia)

| Parte | Cómo es | Color aproximado |
|---|---|---|
| Cabello | Muy largo, liso y brillante, **raya en la mitad**, cae recto por delante de los hombros | Rubio miel `#C9A675`, sombra `#9C7B54`, brillo `#E6CFA4` |
| Cara | Ovalada y suave, mentón pequeño, mejillas llenas, estilo muñeca de porcelana | Piel `#F2D9C9`, sombra `#DDB7A3` |
| Ojos | Grandes, algo rasgados hacia afuera, mirada serena con **párpado un poco caído** | Iris azul `#4C78C4`, borde `#2B4A86` |
| Pestañas | **Muy largas y abundantes**, arriba y abajo | Negro `#1E1A1C` |
| Cejas | Rectas, suaves, finas | Castaño claro `#A98463` |
| Piel (detalles) | **Pecas muy suaves** en la nariz y las mejillas, **rubor rosado** | Pecas `#C99A84`, rubor `#E8A4A0` |
| Labios | Llenos, brillo suave, **sonrisa muy leve** | Nude rosado `#D99A8F` |
| Orejas | **Varios aros plateados** en cada oreja (3 a 4) | Plata `#D8D8DC` |
| Ropa | **Suéter negro de cuello tortuga**, punto grueso con **brillo plateado** | `#1C1A1D` con puntos `#8E8E96` |
| Broche | **Flor de loto / tulipán rosa** en el pecho, a la izquierda | Rosa `#E4B9B4`, borde `#C99590` |

---

## 2. Crear a Lía en VRoid Studio (paso a paso)

> **Quién hace qué:** VRoid Studio es un programa de escritorio que se maneja con el mouse y controles deslizantes, así que **lo usa la autora**. Esta guía dice qué tocar y qué valores usar. Los nombres de los menús pueden variar un poco según la versión de VRoid.
>
> **Expectativa honesta:** VRoid tiene un estilo de dibujo animado (sombreado "MToon"). Lía se va a ver **estilizada y elegante, no fotorrealista** como la imagen. Eso es bueno en 3D: lo hiperrealista se ve inquietante cuando se mueve. El parecido se logra con los colores, el cabello, las pestañas, las pecas y la ropa.

### 2.1 Preparación
1. Descargar **VRoid Studio** (gratis) de su página oficial e instalarlo.
2. **Nuevo modelo → femenino**.
3. Guardar seguido (Archivo → Guardar) como `lia.vroid`, en una carpeta **fuera** del proyecto web, por ejemplo `Documentos/StyleMe-modelos/`.

### 2.2 Cara (pestaña "Cara")
| Ajuste | Qué hacer |
|---|---|
| Forma de la cara | Redonda-ovalada, mentón pequeño y suave, mejillas un poco llenas |
| Ojos: tamaño | Grandes (70–80 % del control) |
| Ojos: forma | Esquina exterior un poco hacia arriba. **Párpado superior un poco caído** para la mirada serena de la referencia |
| Iris | Color azul `#4C78C4`, borde más oscuro, brillo pequeño |
| Pestañas | Las más **largas y densas** disponibles. Activar también las **pestañas de abajo** |
| Delineado | Muy fino o ninguno (las pestañas hacen el trabajo) |
| Cejas | Rectas y finas, color `#A98463`, poco grosor |
| Nariz | Pequeña y fina |
| Boca | Pequeña pero llena. En la **textura** pintar los labios nude rosado con brillo |
| Mejillas | En la textura de la piel: **rubor** `#E8A4A0` difuminado, **pecas** con pincel pequeño y opacidad baja (20–30 %) |

### 2.3 Cabello (pestaña "Cabello")
1. Empezar con un **peinado largo y liso** de los que trae.
2. **Flequillo:** quitarlo. Hacer una **raya en la mitad**: dos grupos de mechones que salen del centro de la cabeza hacia cada lado, pegados a la cabeza.
3. **Largo:** hasta la mitad del pecho, recto, con las puntas parejas. Dejar la mayoría **por delante de los hombros**, como en la imagen.
4. **Color:** base `#C9A675`, sombra `#9C7B54`, brillo `#E6CFA4`. Subir el **brillo especular** (que se vea sedoso).
5. **Física del cabello (muy importante):** dejar activado el movimiento del cabello. Con ese ajuste el pelo se mece solo cuando Lía se mueve, y es una de las cosas que más la hacen ver "viva".

### 2.4 Cuerpo y ropa
| Ajuste | Qué hacer |
|---|---|
| Cuerpo | Proporciones esbeltas y naturales. Cabeza un poquito más grande que lo real (estilo muñeca), sin exagerar |
| Ropa | Una **camiseta o suéter de manga larga con cuello alto**. Color `#1C1A1D` |
| Brillo del suéter | En la textura de la ropa: capa de **puntitos claros** `#8E8E96` (pincel de ruido) para el brillo del tejido |
| Tejido | Si se puede, pintar **líneas verticales suaves** (canalé) un tono más claro |
| Broche | Pintarlo en la textura del pecho (lado izquierdo): flor de 3 pétalos rosa `#E4B9B4` con borde `#C99590`. *Alternativa: hacerlo como objeto 3D en la página (ver 2.6)* |
| Parte de abajo | Pantalón o falda negra simple (casi no se verá) |
| Zapatos | Negros sencillos |

### 2.5 Exportar
1. **Exportar → VRM**, versión **VRM 1.0**.
2. Datos del modelo: nombre "Lía", autora (tu nombre). En la licencia, elegir que **solo tú** puedes usarla.
3. **Reducir peso:**
   - Texturas a **1024 px** (2048 solo para la cara).
   - Activar "reducir polígonos" o "combinar materiales", si está disponible.
   - **Meta: menos de 8 MB.**
4. Guardar como `lia.vrm` y copiarlo en el proyecto: `assets/lia/3d/lia.vrm`.
5. Antes de exportar, revisar en la vista previa de **expresiones** que tenga alegría, tristeza, sorpresa, enojo, parpadeo y las bocas A, I, U, E, O.

### 2.6 Aretes y broche (se agregan en la página)
VRoid no tiene buenos accesorios pequeños. Los **aros plateados** (3 o 4 por oreja) y, si se quiere, el **broche en 3D** se crean con código en Three.js y se "pegan" al hueso de la cabeza y del pecho. Así se mueven con Lía y brillan con la luz de la tienda.

---

## 3. Movimientos fluidos (que nunca se vea "congelada")

Lía siempre tiene **capas de movimiento** que se suman. Todas se calculan con el **tiempo real**, así se ven igual de suaves a 60, 120 o 144 Hz.

| Capa | Qué hace | Cómo |
|---|---|---|
| **Respiración** | El pecho y los hombros suben y bajan muy poco | Movimiento suave y continuo de los huesos del pecho (ciclo de ~4 s) |
| **Balanceo** | El cuerpo se mece un poquito, como al estar de pie | Mismo método, más lento (~7 s) y desfasado |
| **Parpadeo** | Cada 3–6 s (al azar), a veces doble | Expresión `blink`, 120 ms para cerrar y 160 ms para abrir |
| **Mirada** | Sigue a la cámara con suavidad, y cada tanto mira un punto cercano y vuelve | `lookAt` de VRM con suavizado |
| **Cabello** | Se mece solo con la física | Huesos de resorte que trae VRoid |
| **Gestos** | Saludar, señalar, mano en la barbilla, caminar, mostrar | Animaciones VRMA con **fundido de 0,4 s** entre una y otra (nunca cortes bruscos) |
| **Expresión** | La emoción del momento (sección 4) | Mezcla de formas de la cara con transición de 300 ms |
| **Habla** | La boca se mueve con las vocales | Lip sync (sección 5) |

**Reglas de fluidez:**
- Nunca saltar de una expresión a otra: siempre una **transición suave** (300 ms para la cara, 400 ms para el cuerpo).
- Las emociones fuertes duran poco y vuelven a la **neutra amable**.
- Si la pestaña está oculta o Lía sale de la pantalla, se pausa todo.

---

## 4. Emociones ✅ (preocupación, felicidad, tristeza y duda)

Cada emoción es una **combinación** de cara, cabeza, cuerpo y mirada. VRoid trae formas de cara separadas para **cejas, ojos y boca**: alegría, tristeza, sorpresa, etc. Mezclándolas se crean emociones que VRoid no trae listas, como la preocupación y la duda.

### 4.1 Las emociones de Lía

| Emoción | Cara | Cabeza y cuerpo | Mirada | Cuándo la usa |
|---|---|---|---|---|
| 😊 **Felicidad** | Ojos sonrientes (60 %), cejas alegres (40 %), boca sonriendo (60 %) | Cabeza un poco arriba (3°), hombros relajados. Gesto: manos juntas o saludo | A la cámara | Saludo · encontró productos · la persona arma su salida · despedida |
| 😟 **Preocupación** | Cejas tristes (60 %), ojos un poco tristes (25 %), boca pequeña y cerrada (30 %) | Cabeza adelante (4°) e inclinada (6°). Gesto: mano en el pecho | A la cámara, atenta | La persona habla de **irritación, alergia, piel sensible, caída de cabello** · Lía recomienda ir al dermatólogo |
| 😔 **Tristeza** | Cejas tristes (50 %), ojos tristes (40 %), boca triste (30 %) | Cabeza baja (6°), hombros caen un poco. Corta: **máximo 2 s** y vuelve a la amable | Abajo y luego a la cámara | **No encontró** productos · la tienda no tiene datos · "lo siento, eso no lo tenemos" |
| 🤔 **Duda** | Cejas de sorpresa suave (30 %), un ojo un poco entrecerrado (25 %), boca en "U" pequeña (25 %) | Cabeza **inclinada a un lado (10°)**. Gesto: **mano en la barbilla** | Arriba y a un lado | Necesita aclarar algo ("¿para el día o para la noche?") · el mensaje es ambiguo |

### 4.2 Estados de apoyo (siempre presentes)
| Estado | Cara | Cuerpo | Cuándo |
|---|---|---|---|
| 🙂 **Neutra amable** (reposo) | Sonrisa muy leve (15 %), ojos relajados | Respiración y balanceo | Por defecto |
| 👂 **Escuchando** | Cejas un poco arriba (15 %) | Cabeza inclinada (5°) y asiente suave cada tanto | Mientras la persona escribe o habla |
| 💭 **Pensando** | Boca cerrada, cejas suaves | Mirada arriba y a un lado | Mientras llega la respuesta de la IA |
| 🗣️ **Hablando** | La emoción del momento **+** boca con vocales | Pequeños gestos con las manos | Mientras Lía responde |

> Los porcentajes son un punto de partida. Se ajustan mirando a Lía en pantalla. Quedan en un solo archivo de configuración (`EMOCIONES` en el código) marcado con ✏️ para cambiarlos fácil.

### 4.3 Cómo sabe Lía qué emoción poner
1. **La IA la elige.** Cada respuesta del modelo trae un campo `"emocion"` con uno de estos valores: `felicidad`, `preocupacion`, `tristeza`, `duda` o `neutra`, y una intensidad de 0 a 1.
   ```json
   { "texto": "Uy, si tu piel se irrita con facilidad, mejor consulta a un dermatólogo...",
     "emocion": "preocupacion", "intensidad": 0.7, "acciones": [], "sugerencias": [] }
   ```
2. **Reglas que corrigen a la IA** (por seguridad):
   - Si la búsqueda no encontró productos → `tristeza`.
   - Si el mensaje tiene palabras de salud (ardor, alergia, sangra, caída, dermatitis…) → `preocupacion`.
   - Si Lía hace una pregunta para aclarar → `duda`.
3. **Duración:** la emoción aparece al empezar a hablar y se mantiene mientras habla. Después vuelve poco a poco a la neutra amable. La tristeza dura máximo 2 s.

---

## 5. Lip sync (la boca se mueve al hablar)

| Modo | Cómo funciona | Calidad |
|---|---|---|
| **Texto (sin voz)** | Mientras el texto aparece letra por letra, la boca hace la forma de cada **vocal** (A, E, I, O, U) | Buena y liviana |
| **Voz del navegador** | El navegador avisa cada palabra que dice. Con eso se mueven las vocales de esa palabra | Buena |
| **Voz de un servicio externo** (futuro) | Se analiza el sonido real y la boca se abre según el volumen | Muy buena |

Mientras habla, la boca de la emoción se reduce a la mitad para no "pelear" con las vocales.

---

## 6. Archivos (Fase 2) ✅ programado el 2026-11-20, falta el modelo

```
assets/
├── lia/
│   ├── 2d/                  ← LÍA EN 2D (la que se usa hoy)
│   │   ├── principal.webp, feliz.webp, confundida.webp, sorprendida.webp
│   │   └── lia-2d.css       ← poses, boca que habla y parpadeo
│   └── 3d/                  ← LÍA EN 3D (se activa sola si existe lia.vrm)
│       ├── lia.vrm          ← el modelo exportado de VRoid (LO PONE LA AUTORA)
│       ├── lia-3d.js        ← Three.js + three-vrm: respiración, balanceo, parpadeo, mirada,
│       │                      ✏️ tabla EMOCIONES, lip sync con vocales, física del cabello
│       ├── lia-3d.css       ← lienzo 3D; oculta el retrato 2D
│       ├── lia-referencia.png ← imagen oficial del avatar
│       └── LEEME.txt
├── css/lia.css              ← lo común: arco, globo de texto, respuestas
└── js/lia/
    ├── lia.js               ← el personaje: globos, respuestas, emociones; carga el 3D si existe el modelo
    └── plan-b.js            ← respuestas sin IA
```

- Las librerías 3D se cargan con un `importmap` en cada página (Three.js 0.169.0 y three-vrm 3.0.0).
- **Funcionamiento:** apenas exista `assets/lia/3d/lia.vrm`, Lía aparece en 3D sola. Si no existe o algo falla, se queda el retrato.
- **Pendiente para más adelante:** gestos con los brazos (saludar, mano en la barbilla) con animaciones VRMA. Hoy los gestos son de cabeza, cuerpo y cara.

---

## 7. Orden de trabajo sugerido 💡

| Semana | Modelo 3D (VRoid) | Desarrollo |
|---|---|---|
| **1** | Crea a Lía en VRoid con esta guía y exporta `lia.vrm` | **Fase 1:** el cerebro de Lía (chat 2D funcional con RAG en Netlify), usando el retrato 2D (`assets/lia/2d/`) como avatar |
| **2** | Revisa las emociones y da su opinión | **Fase 2:** Lía en 3D con movimientos fluidos, las 4 emociones y lip sync |
| **3+** | Pruebas con personas | **Fase 3:** la tienda 3D guiada |

Mientras se hace el modelo 3D, el cerebro ya funciona con el retrato.
