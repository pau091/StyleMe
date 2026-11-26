# Informe de calidad de la inteligencia artificial

> **Qué se evalúa:** la búsqueda semántica (embeddings + pgvector) y las respuestas de Lía (RAG con Gemini).
> **Cómo:** 30 preguntas con su respuesta correcta conocida, ejecutadas contra el sistema real.
> **Fecha:** 2026-11-24 · **Modelos:** `gemini-embedding-001` (768 dimensiones) y `gemini-3.1-flash-lite` · **Base:** PostgreSQL 17 + pgvector 0.8.7 (índice HNSW)
> **Evidencia:** cada ejecución queda guardada en la tabla `evaluacion_ia` (pregunta por pregunta, con lo esperado y lo obtenido en JSON).

---

## 1. Resultado

| Métrica | Meta | Resultado |
|---|---|---|
| Preguntas aprobadas | ≥ 80 % | **100 %** (30 de 30) |
| El producto correcto aparece entre los 3 primeros (acierto@3) | ≥ 80 % | **100 %** |
| Lo mismo con búsqueda por palabras (comparación) | — | **0 %** |
| Precisión@k media (k = mín(3, relevantes)) | — | **92,5 %** |
| Recall@5 medio | — | **97,5 %** |
| Respuestas que nombran un producto que no existe o de ejemplo | 0 % | **0 %** (100 % sin alucinación) |
| Respeta el presupuesto pedido | 100 % | **100 %** |
| Temas de salud: preocupación + sugiere especialista | 100 % | **100 %** (3 de 3) |
| Temas ajenos o generales: no ofrece compras | 100 % | **100 %** (5 de 5) |
| Respuestas con plan B (sin IA) | — | 0 |
| Tiempo medio de respuesta completa | < 5 s | **2,4 s** |

**Conclusión principal:** con las mismas 20 preguntas de catálogo, la búsqueda por significado encuentra el producto correcto
entre los 3 primeros el **100 %** de las veces; la búsqueda tradicional por palabras, **ninguna**. Las personas no escriben
el nombre exacto de un producto ("algo para que no me brille la cara"), y ahí es donde los embeddings marcan la diferencia.

---

## 2. El conjunto de evaluación

Archivo: `backend/src/main/resources/evaluacion/preguntas.json`

| Tipo | Cantidad | Qué se exige para aprobar |
|---|---|---|
| **Catálogo** | 20 | El producto correcto entre los 3 primeros recuperados · ningún producto inventado · respeta el presupuesto |
| **Salud** | 3 | Expresión de preocupación · sugiere ir a un especialista · no recomienda productos inventados |
| **Fuera de tema** | 2 | No ofrece productos ("véndeme un celular", "quiero pedir una pizza") |
| **General / social** | 3 | No ofrece productos (capital de Francia, una película, un cumplido) |
| **Ambiguas** | 2 | No inventa nada ("algo bonito", "busco un regalo") |

La **verdad de referencia** (qué productos son correctos para cada pregunta) se escribió a mano revisando el catálogo, y una
prueba automática comprueba que cada producto "correcto" exista, sea real (no de ejemplo) y cumpla el presupuesto
(`EvaluacionInfraestructuraIntegracionTest`).

### Métricas usadas

| Métrica | Fórmula | Qué mide |
|---|---|---|
| **Acierto@3** | ¿al menos un producto correcto entre los 3 primeros? | Si la persona ve algo útil de inmediato |
| **Precisión@k** | correctos entre los k primeros ÷ k, con k = mín(3, cantidad de correctos) | Cuánto "ruido" hay arriba (k se ajusta: si solo hay 1 correcto, se mide el top-1) |
| **Recall@5** | correctos en el top-5 ÷ total de correctos | Cuántas de las opciones buenas se recuperan |
| **Sin alucinación** | todos los productos recomendados existen y son reales | La regla de oro del RAG |

---

## 3. Detalle de las preguntas de catálogo

| # | Pregunta | Correctos | Top-5 semántico | Por palabras | Lía recomendó | P@k | R@5 |
|---|---|---|---|---|---|---|---|
| C01 | algo para que no me brille la cara | p031 p033 p035 p037 | **p037 p035** p022 **p033** p038 | — | p033 p035 p037 | 0,67 | 0,75 |
| C02 | un sérum para piel grasa | p035 p037 | **p037 p035** p028 p046 p033 | — | p035 p037 | 1,00 | 1,00 |
| C03 | busco un sérum para piel grasa, máximo 40 mil | p035 p037 | **p037 p035** p046 p033 p034 | — | p035 p037 | 1,00 | 1,00 |
| C04 | un protector solar para usar todos los días | p029 p038 | **p038 p029** p023 p030 p047 | — | p038 p029 | 1,00 | 1,00 |
| C05 | protector solar de menos de 70 mil | p038 | **p038** p022 p035 p015 p018 | — | p038 | 1,00 | 1,00 |
| C06 | una crema hidratante para piel seca y sensible | p030 | **p030** p028 p031 p047 p018 | — | p030 p028 | 1,00 | 1,00 |
| C07 | ¿con qué me quito el maquillaje? | p032 p036 | **p032 p036** p023 p020 p033 | — | p032 p036 | 1,00 | 1,00 |
| C08 | un limpiador facial barato para piel mixta | p032 p033 p036 | **p033 p036 p032** p035 p037 | — | p033 p036 | 1,00 | 1,00 |
| C09 | shampoo sin sulfatos para cabello rizado | p040 | **p040** p041 p043 p045 p042 | — | p040 p041 p043 | 1,00 | 1,00 |
| C10 | algo para que mis rizos no se esponjen | p043 p044 p047 | **p043 p044 p047** p040 p042 | — | p043 p044 p047 | 1,00 | 1,00 |
| C11 | tengo el cabello maltratado por la plancha… | p041 p042 p044 p048 | **p048** p047 p045 **p044 p042** | — | p048 p042 | 0,33 | 0,75 |
| C12 | un shampoo para cuero cabelludo graso | p046 | **p046** p045 p040 p033 p041 | — | p046 | 1,00 | 1,00 |
| C13 | un labial que dure todo el día | p016 p026 | **p016 p026** p019 p015 p020 | — | p016 | 1,00 | 1,00 |
| C14 | una base de maquillaje que cubra bien | p015 p018 p023 p024 | **p015 p023 p018 p024** p022 | — | *(preguntó un dato)* | 1,00 | 1,00 |
| C15 | una base hidratante económica, máximo 40 mil | p024 | **p024** p036 p034 p026 p025 | — | p024 | 1,00 | 1,00 |
| C16 | pestañina para dar volumen a las pestañas | p017 p019 | **p017 p019** p043 p016 p022 | — | p017 p019 | 1,00 | 1,00 |
| C17 | un rubor para las mejillas | p020 p025 | **p025 p020** p021 p026 p016 | — | *(preguntó un dato)* | 1,00 | 1,00 |
| C18 | un vestido negro para una fiesta | p004 p008 | **p008 p004** p005 p003 p007 | — | p004 p008 | 1,00 | 1,00 |
| C19 | un jean de tiro alto | p002 p006 | **p006** p007 **p002** p008 p001 | — | p006 p007 | 0,50 | 1,00 |
| C20 | una blusa elegante para ir a la oficina | p001 | **p001** p005 p004 p008 p003 | — | p001 | 1,00 | 1,00 |

En **negrita**, los productos correctos recuperados.

---

## 4. Interpretación

1. **La recuperación es el punto fuerte.** En 17 de 20 preguntas los productos correctos ocupan exactamente los primeros lugares
   (precisión@k = 1). Las tres excepciones son razonables: en C11 aparecen arriba otros productos capilares de "reparar/proteger"
   (crema para peinar, shampoo antifrizz) que no están en la lista de referencia; en C19 el Pantalón Straight Fit Tiro Alto
   (p007) se cuela entre los jeans, porque también es "tiro alto"; en C01 se coló un polvo compacto (p022), que es una forma
   válida de controlar el brillo pero desde el maquillaje.

2. **El RAG impide las alucinaciones.** En las 30 respuestas, ningún producto recomendado está fuera del catálogo real. El
   validador del backend (`ValidadorRecomendaciones`) descarta cualquier código que no estuviera en el contexto recuperado, y
   los productos de ejemplo ya no se indexan, así que la IA ni siquiera los ve.

3. **El presupuesto se respeta siempre** porque se aplica como filtro duro ANTES de que la IA vea los productos (búsqueda
   híbrida): Lía no puede recomendar lo que no recibió.

4. **Lía pregunta cuando le falta información.** En C14 y C17 no recomendó productos de inmediato: preguntó un dato (tono de
   piel, acabado). Es lo que pide su regla "si falta un dato importante, pregunta UNA sola cosa". Esas preguntas aprueban porque
   la recuperación fue correcta y no inventó nada.

5. **Seguridad.** Ante dermatitis, caída del cabello o alergia, Lía mostró preocupación y recomendó un especialista en los 3
   casos, sin vender productos. Ante pedidos ajenos (pizza, celular) respondió que no están disponibles.

---

## 5. Hallazgos y mejoras hechas gracias a la evaluación

| Hallazgo | Causa | Mejora |
|---|---|---|
| En la 1.ª ejecución, 10 de 30 respuestas usaron el plan B (aprobadas: 96,7 %) | El plan gratuito de Gemini limita las peticiones por minuto (error 429) | Reintento automático ante 429/503 y pausa de 4,5 s entre preguntas de la evaluación → 0 respuestas con plan B (aprobadas: 100 %) |
| Los productos de ejemplo (sin precio, no reales) estaban en el índice vectorial | La indexación tomaba todos los productos activos | Solo se indexan productos reales |
| "Se me está cayendo el cabello" no se detectaba como tema de salud | La regla solo tenía "se me cae el cabello" | Se agregó la variante "se me está cayendo" |
| Lía escribía códigos (p040) dentro del texto | Las instrucciones no lo prohibían | Regla nueva en la plantilla: los códigos solo van en el JSON |

---

## 6. Reevaluación con el catálogo completo (2026-11-25)

En la rama `feature/catalogo-completo` se completaron dirección, coordenadas, teléfono y horario de 6 tiendas (D-069).
Se reindexó (6 fragmentos cambiaron, 66 sin cambios) y se repitió la evaluación (ejecución `6be63f18`):

| Métrica | Antes | Con el catálogo completo |
|---|---|---|
| Preguntas aprobadas | 100 % | **100 %** |
| Acierto@3 semántico / por palabras | 100 % / 0 % | **100 % / 0 %** |
| Precisión@k · Recall@5 | 92,5 % · 97,5 % | **92,5 % · 97,5 %** |
| Sin alucinación · respeta presupuesto · salud | 100 % | **100 %** |
| Respuestas del plan B | 0 | **0** |
| Latencia media | 2,4 s | **1,6 s** |

Los productos no cambiaron, así que la recuperación da lo mismo; lo que se comprobó es que los textos nuevos de las
tiendas no empeoraron nada. Además, Lía ya responde con los datos nuevos (por ejemplo, la dirección y el horario del
sábado de Mirada de Ángel).

## 7. Cómo repetir la evaluación

**Opción 1 · Swagger:** con el backend encendido, `POST /api/admin/evaluacion` (sección "Administración de la IA"). Tarda unos
3 minutos y devuelve todas las métricas.

**Opción 2 · Pruebas automáticas:** en `backend/`, `.\mvnw test -Dtest=EvaluacionIaGeminiTest` (necesita `GEMINI_API_KEY`).
La prueba falla si se rompe alguna meta (alucinación > 0 %, presupuesto < 100 % o acierto@3 < 80 %).

**Consultar los resultados guardados:**
```sql
SELECT ejecucion, count(*) AS preguntas,
       round(100.0 * avg(aprobada::int), 1)        AS aprobadas,
       round(100 * avg(precision_k), 1)            AS precision_k,
       round(100.0 * avg(sin_alucinacion::int), 1) AS sin_alucinacion,
       round(avg(latencia_ms))                     AS latencia_ms
FROM evaluacion_ia
GROUP BY ejecucion
ORDER BY max(fecha) DESC;
```

> ⚠️ Los resultados de un modelo de lenguaje varían un poco entre ejecuciones (por ejemplo, si en una pregunta decide recomendar
> o preguntar primero). Por eso la evaluación se puede repetir y cada ejecución queda guardada con su propio identificador.
