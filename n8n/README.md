# Automatizaciones con n8n

Workflows que integran StyleMe con servicios de IA. Se exportan desde n8n en formato JSON y se guardan aquí.

| Archivo | Qué hace | Cómo se conecta con el backend |
|---|---|---|
| `moderacion-resenas.json` | Cuando llega una reseña nueva, Gemini la clasifica y el estado pasa a PUBLICADA o RECHAZADA | El backend avisa por webhook (`NotificadorPuerto`) y n8n responde a `ModeracionWebhookControlador` |
| `reindexacion-embeddings.json` | Cuando se crea o edita un producto, se recalcula su embedding | n8n llama al endpoint de reindexación del backend |
| `reporte-semanal.json` | Cada semana consulta los indicadores y envía un resumen escrito por IA | n8n llama al endpoint de indicadores del backend |

Se construyen en la rama `feature/n8n-docker`.

## Importar un workflow
En n8n: **Workflows → Import from File** → elegir el `.json`.
