# 09 · Plan de entrega: StyleMe versión sencilla

> **Objetivo:** tener una página web funcionando y publicada para la entrega académica de esta semana.
> **Meta:** terminar entre el 16 y el 17 de noviembre de 2026.
> **Condiciones:** una sola persona (programadora junior), costo $0, solo HTML, CSS y JavaScript.
>
> Todo lo que no aparece aquí está en [10 - Proyecto completo](10-proyecto-completo.md).

## 1. La idea en una frase

Una página web donde cualquier persona de Bucaramanga, cuando se pregunte *"¿y ahora qué hacemos?"*, encuentra **tiendas locales de ropa y maquillaje**, arma una **salida de compras**, se **prueba la ropa virtualmente** y les **escribe por WhatsApp** o pide **cómo llegar**.

## 2. Qué incluye ✅

| # | Página | Qué hace | Estado |
|---|---|---|---|
| 1 | **Inicio** (`index.html`) | Buscador, 4 categorías, tiendas de ropa y de maquillaje, accesos a la salida de compras y al probador | ✅ Programada |
| 2 | **Tiendas** (`tiendas.html`) | Lista de las 10 tiendas con búsqueda y filtro por tipo | ✅ Programada |
| 3 | **Perfil de tienda** (`tienda.html`) | Datos, mini mapa, productos y botones de **WhatsApp**, **Cómo llegar** e **Instagram** | ✅ Programada |
| 4 | **Productos** (`productos.html`) | Catálogo con buscador y filtros por categoría, precio y tienda | ✅ Programada |
| 5 | **Mapa** (`mapa.html`) | Todas las tiendas en el mapa, con filtro por tipo | ✅ Programada |
| 6 | **Salida de compras** (`salida.html`) | Responde a "¿y ahora qué hacemos?": arma un recorrido por 2–5 tiendas cercanas, lo dibuja en el mapa y lo abre en Google Maps | ✅ Programada |
| 7 | **Probador virtual** (`probador.html`) | **Solo ropa.** IA en el navegador (MediaPipe) que detecta hombros y caderas y coloca la prenda sobre la foto. Solo mayores de 18 años; la foto no sale del dispositivo | ✅ Programada |
| 8 | **Acerca de** (`acerca.html`) | Qué es StyleMe, privacidad, aviso académico y contacto | ✅ Programada |
| 9 | **Recomendador** (`recomendador.html`) | Cuestionario de piel o de cabello que arma una **rutina por pasos** con productos del catálogo. Funciona con reglas, sin diagnósticos, y sugiere ir al dermatólogo si la persona marca una condición | ✅ Programada |
| 10 | **Lía** | Asesora con Gemini que responde preguntas abiertas, recomienda productos del catálogo y gesticula mientras conversa. | Programada. Pendiente de verificar con la clave de Gemini en Netlify. |

## 3. Qué NO incluye (queda para después) 🔮

Cuentas e inicio de sesión, registro de comerciantes, panel de administrador, reseñas con corazones, favoritos guardados en servidor, tienda 3D con recorrido por pasillos, probador virtual con IA generativa, probador de maquillaje, comparación avanzada y pagos.

## 4. Tecnología ✅

| Necesidad | Herramienta | Costo |
|---|---|---|
| Estructura de las páginas | HTML | $0 |
| Diseño | CSS (diseño pensado primero para celular) | $0 |
| Buscador, filtros y recomendador | JavaScript | $0 |
| Datos de tiendas y productos | Archivos `.json` | $0 |
| Mapa | Leaflet + OpenStreetMap | $0, sin tarjeta |
| IA del probador | MediaPipe Pose Landmarker (corre en el navegador) | $0 |
| WhatsApp | Enlace `https://wa.me/57NUMERO?text=...` | $0 |
| Cómo llegar | Enlace a Google Maps con la dirección | $0 |
| Publicar la página | GitHub Pages o Netlify | $0 |

## 5. Estructura de carpetas ✅

Ver el [README](../README.md#estructura-del-proyecto). En resumen, las páginas `.html` van en la raíz porque GitHub Pages lo necesita. Los datos van en `data/`, el diseño y los scripts en `assets/`, y esta documentación en `planeacion/`.

> Los datos están en archivos `.js` y no en `.json` porque así se pueden editar y cargar sin trucos extra. Cuando exista una base de datos, se reemplazan.

## 6. Tiendas (10: 5 de ropa y 5 de maquillaje) ⏳ por verificar

> ⚠️ Estos datos salen de directorios y redes sociales en internet. **Pueden estar desactualizados.** Antes de publicar, confirma en Google Maps o en sus redes la dirección, el horario y el WhatsApp.
> **Coordenadas:** en Google Maps, clic derecho sobre el local y luego clic en los números para copiarlos. Mientras falten, la tienda no aparece en el mapa ni en la salida de compras.

| # | Tienda | Tipo | Dirección (por verificar) | Contacto encontrado |
|---|---|---|---|---|
| 1 | Samylu Boutique | Ropa | Carrera 37 #42-75, Cabecera del Llano | — |
| 2 | BOHOR | Ropa | Carrera 36 #53-32 | — |
| 3 | Queen | Ropa (segunda mano) | Carrera 35 #52-146, Cabecera del Llano ✅ | Tel. fijo 607 657 1340 |
| 4 | Henar's y Eleganzza | Ropa (tallas grandes y pequeñas) | Carrera 22 #45-13, La Concordia | — |
| 5 | Mattelsa | Ropa (marca nacional) | Carrera 31 #32-45 | — |
| 6 | Pandora Makeup | Maquillaje | Carrera 33 #46-59, Cabecera | WhatsApp 321 327 3357 |
| 7 | Guacamaya Colors | Maquillaje y skincare | Carrera 36 #51-120, Cabecera | guacamayacolors.com |
| 8 | Gogó Makeup | Maquillaje | Carrera 35 #52-103, Cabecera | gogomakeup.com.co |
| 9 | So Beauty Maquillaje | Maquillaje | Carrera 36 #52-28, local 1, Cabecera | Instagram @sobeautymaquillajebga |
| 10 | Mirada de Ángel | Maquillaje y cuidado capilar | Carrera 23 #35-14, local 1, Edificio Zentri | 322 220 9159 |

**Cambio:** HairClinic Store (cuidado capilar) salió para cumplir 5 de ropa y 5 de maquillaje. Entró So Beauty.
**Cambio 2:** Sea & Seams salió porque no aparece en Google Maps. Entró Queen.
**Suplentes:** HairClinic Store, Sea & Seams (sin ubicación), Color Club (Cra 35 #52-84), Cosméticos Cabecera (Cra 33 #48-51).

## 7. Datos que tienes que completar

Archivo `data/tiendas.js`: `lat`, `lng`, `horario`, `whatsapp`, `instagram` y `foto`. Cuando lo verifiques, pon `verificado: true` y desaparece el aviso amarillo.

Archivo `data/productos.js`: hay 20 productos **de ejemplo** (2 por tienda). Reemplázalos por productos del Instagram de cada tienda:
- `nombre`, `precio` (número sin puntos), `foto` (en `assets/img/productos/`) y `fuente` (enlace a la publicación).
- Pon `pendiente: false`. Así desaparece el sello "Ejemplo".
- **Ropa:** `tipoPrenda` debe ser `"superior"`, `"inferior"` o `"completa"`. Para el probador, la foto ideal es un **PNG sin fondo**. Puedes quitar el fondo gratis con herramientas en línea. Si la foto tiene fondo blanco, el probador tiene la opción "Quitar el fondo blanco".

## 8. Plan de trabajo

| Bloque | Tarea | Estado |
|---|---|---|
| Hoy | Estructura, diseño y todas las páginas | ✅ Hecho (falta probar en el navegador) |
| Hoy | Abrir con Live Server y revisar cada página | ⏳ Tú |
| Hoy | Completar coordenadas, horarios y WhatsApp de las 10 tiendas | ⏳ Tú |
| Mañana | Productos reales del Instagram (fotos, precios, tipo de prenda) | ⏳ Tú |
| Mañana | Probar el probador con 3–4 prendas y fotos diferentes | ⏳ Tú + asesor |
| Mañana | Cambiar el correo de contacto en `acerca.html` | ⏳ Tú |
| Mañana | Revisar en celular y publicar en Netlify | ⏳ Tú |

## 9. Lista final antes de entregar

- [ ] Datos de las 10 tiendas verificados (dirección, WhatsApp y horario).
- [ ] Aviso visible: *"Proyecto académico. StyleMe no tiene acuerdos comerciales con las tiendas mostradas. La información puede cambiar; confírmala con cada tienda."*
- [ ] Productos con `fuente` (enlace al Instagram de origen).
- [ ] El probador funciona con al menos 3 prendas reales.
- [ ] Correo de contacto real en `acerca.html`.
- [ ] Probado en celular y en computador.
- [ ] Página publicada y enlace funcionando.
- [ ] Opción para que una tienda pida que la retiren o la corrijan (correo o formulario).

## Fuentes de los datos de tiendas

- [Samylu Boutique: PlanetaColombia](https://www.planetacolombia.com/samylu-boutique-F1209C1071AD447)
- [Tiendas de ropa en Bucaramanga: PlanetaColombia](https://www.planetacolombia.com/bucaramanga/tiendas+de+ropa)
- [Ropa de segunda mano en Bucaramanga: A Fashion List](https://afashionlist.com/moda/hay-uso-en-el-desuso-tiendas-fisicas-de-ropa-de-segunda-mano-en-bucaramanga-y-su-area-metropolitana/)
- [Henar's y Eleganzza: UbicaYa](https://www.ubicaya.com/bucaramanga/ropa-para-dama/sarom-boutique/)
- [Pandora Maquillaje](https://pandoramaquillaje.com/)
- [Guacamaya Colors](https://guacamayacolors.com/)
- [Tiendas de maquillaje en Bucaramanga: Dancefree](https://www.dancefree.com.co/diario/tiendas-de-maquillaje-y-cosmeticos-en-bucaramanga)
- [Productos de belleza en Bucaramanga: ForBucaramangaLovers](https://www.forbucaramangalovers.com/es/mejores/tiendas-para-comprar-productos-belleza-bucaramanga)
- [HairClinic Store: Facebook](https://www.facebook.com/hairclinicstore/)
- [Mirada de Ángel](https://miradadeangel.com.co/)
- [Productos para el cabello en Bucaramanga: PlanetaColombia](https://www.planetacolombia.com/bucaramanga/productos+para+el+cabello)
