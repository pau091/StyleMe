# Pruebas

| Archivo | Contenido | Se crea en |
|---|---|---|
| `documento-de-pruebas.md` | Casos de prueba: caso, entrada, resultado esperado, resultado obtenido y evidencia (backend, API, iu y n8n) | Rama `docs/entrega-final` |
| `informe-calidad-ia.md` | Evaluación de Lía y de la búsqueda semántica: 30 preguntas, precisión@k, recall@k, respuestas sin productos inventados y tiempo de respuesta | Rama `test/evaluacion-ia` |

Las pruebas automáticas (JUnit, Mockito, ArchUnit, Testcontainers) viven en `backend/src/test/`.
Este documento resume sus resultados.
