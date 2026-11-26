# Automatizaciones con n8n

Tres workflows que conectan StyleMe con IA (Gemini). Están en `workflows/` y n8n los **importa y activa solo**
cada vez que se enciende con `docker compose up` (ver el servicio `n8n` en `docker-compose.yml`).

| Archivo | Cuándo corre | Qué hace |
|---|---|---|
| `moderacion-resenas.json` | Cuando el backend avisa de una reseña nueva (webhook `resena-nueva`) **y** cada 30 minutos | Trae las reseñas PENDIENTES, Gemini decide si cada una se publica o se rechaza, y n8n le manda la decisión al backend |
| `reindexacion-embeddings.json` | Cada noche a las 3:00 | Pide al backend que recalcule los embeddings de lo nuevo o modificado (si nada cambió, no gasta llamadas a Gemini) |
| `reporte-semanal.json` | Los lunes a las 8:00 | Lee los 13 indicadores, Gemini escribe un reporte con hallazgos, recomendaciones y alertas, y lo guarda en `reportes/reporte-AAAA-MM-DD.md` |

## Cómo se conecta con el backend

```
Persona escribe una reseña
   └─▶ backend: la guarda PENDIENTE y avisa a n8n (NotificadorPuerto → N8nNotificadorAdaptador)
         └─▶ n8n: GET  /api/admin/resenas/pendientes        (con X-Admin-Key)
             n8n: Gemini → {"decision": "PUBLICADA" | "RECHAZADA", "motivo": "..."}
             n8n: PATCH /api/admin/resenas/{id}              (con X-Admin-Key)
```

- El backend solo manda el **id** de la reseña (nada personal). n8n pide los datos con la clave de administración.
- Si n8n está apagado o Gemini falla, la reseña **no se pierde**: queda PENDIENTE y la revisión de cada 30 minutos la recoge.
- Gemini se llama de a una reseña cada 4 s y con 3 reintentos (el plan gratuito a veces responde 503 o 429).
- Regla de moderación: se **rechaza** solo lo que tenga insultos, ataques, datos personales, publicidad o no hable de la
  tienda. Las **críticas negativas respetuosas se publican** (son útiles para otras personas).

## Ejecutarlos a mano (pruebas y sustentación)

La reindexación y el reporte también tienen un webhook protegido con la clave de administración:

```powershell
$clave = "la-clave-de-STYLEME_ADMIN_CLAVE"
Invoke-RestMethod -Method Post -Uri http://localhost:5678/webhook/reporte-semanal -Headers @{ "X-Admin-Key" = $clave }
Invoke-RestMethod -Method Post -Uri http://localhost:5678/webhook/reindexar      -Headers @{ "X-Admin-Key" = $clave }
```
Sin la clave correcta el workflow se detiene en el nodo *Verificar clave*.

## Ver lo que pasó
Abrir **http://localhost:5678** (la primera vez n8n pide crear una cuenta local) → cada workflow → pestaña **Executions**.

## Cambiar un workflow
Los `.json` se generan con `python3 n8n/generar_workflows.py` (en Windows: `python n8n/generar_workflows.py`) (así los tres llaman igual al backend y a Gemini).
Después de cambiar el script: `python3 n8n/generar_workflows.py` (Windows: `python …`) y `docker compose restart n8n`.
Si prefieres editarlo en la pantalla de n8n, expórtalo (menú ··· → *Download*) y reemplaza su `.json`.
