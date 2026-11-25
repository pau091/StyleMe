"""
GENERADOR DE LOS WORKFLOWS DE n8n
---------------------------------
Escribe los 3 workflows de StyleMe en n8n/workflows/*.json (el formato que importa n8n).
Se generan con este script para que los tres usen la misma forma de llamar al backend y a Gemini.

    python n8n/generar_workflows.py

Si cambias un workflow desde la pantalla de n8n, expórtalo (menú ··· → Download) y reemplaza su .json.
"""
import json
from pathlib import Path

CARPETA = Path(__file__).parent / "workflows"

# Datos que n8n lee de sus variables de entorno (docker-compose.yml, servicio n8n)
API = "{{ $env.STYLEME_API }}"
CABECERA_ADMIN = {"name": "X-Admin-Key", "value": "={{ $env.STYLEME_ADMIN_CLAVE }}"}
URL_GEMINI = ("=https://generativelanguage.googleapis.com/v1beta/models/"
              "{{ $env.GEMINI_MODELO || 'gemini-3.1-flash-lite' }}:generateContent")
CABECERA_GEMINI = {"name": "x-goog-api-key", "value": "={{ $env.GEMINI_API_KEY }}"}


def nodo(nombre, tipo, version, x, y, parametros, **extra):
    return {"parameters": parametros, "name": nombre, "type": tipo, "typeVersion": version,
            "position": [x, y], "id": nombre.lower().replace(" ", "-"), **extra}


def peticion(nombre, x, y, metodo, url, cabeceras, cuerpo=None, opciones=None, **extra):
    parametros = {"method": metodo, "url": url, "sendHeaders": True,
                  "headerParameters": {"parameters": cabeceras}, "options": opciones or {}}
    if cuerpo is not None:
        parametros.update({"sendBody": True, "specifyBody": "json", "jsonBody": cuerpo})
    return nodo(nombre, "n8n-nodes-base.httpRequest", 4.2, x, y, parametros, **extra)


# Gemini a veces responde 503 ("mucha demanda") o 429 (límite por minuto): se reintenta 3 veces,
# y si aun así falla, se sigue con las demás reseñas (la que falló queda PENDIENTE para la próxima revisión)
REINTENTOS_GEMINI = {"retryOnFail": True, "maxTries": 3, "waitBetweenTries": 5000, "onError": "continueRegularOutput"}


def codigo(nombre, x, y, js):
    return nodo(nombre, "n8n-nodes-base.code", 2, x, y, {"jsCode": js})


def cada(nombre, x, y, regla):
    return nodo(nombre, "n8n-nodes-base.scheduleTrigger", 1.2, x, y, {"rule": {"interval": [regla]}})


# Para ejecutar a mano (pruebas y sustentación): POST /webhook/<ruta> con la cabecera X-Admin-Key.
# Sin la clave correcta, el workflow no sigue.
VERIFICAR_CLAVE = """const recibida = $input.first().json.headers?.['x-admin-key'];
if (!recibida || recibida !== $env.STYLEME_ADMIN_CLAVE) return [];
return [{ json: { manual: true } }];
"""


def a_mano(ruta, x, y):
    """Webhook + verificación de la clave de administración."""
    return [nodo(f"A mano ({ruta})", "n8n-nodes-base.webhook", 2, x, y,
                 {"httpMethod": "POST", "path": ruta, "options": {}}, webhookId=ruta),
            codigo(f"Verificar clave ({ruta})", x + 220, y, VERIFICAR_CLAVE)]


def conectar(*pares):
    conexiones = {}
    for origen, destino in pares:
        conexiones.setdefault(origen, {"main": [[]]})["main"][0].append({"node": destino, "type": "main", "index": 0})
    return conexiones


def guardar(archivo, identificador, nombre, nodos, conexiones):
    flujo = {"id": identificador, "name": nombre, "nodes": nodos, "connections": conexiones,
             "settings": {"executionOrder": "v1", "timezone": "America/Bogota"}, "active": True,
             "pinData": {}, "tags": []}
    (CARPETA / archivo).write_text(json.dumps(flujo, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("✔", archivo)


# =====================================================================
# 1. MODERACIÓN DE RESEÑAS CON IA
#    El backend avisa por webhook cuando llega una reseña (y además se revisa cada 30 min por si n8n
#    estaba apagado). Gemini decide si se publica o se rechaza; n8n le dice la decisión al backend.
# =====================================================================
INSTRUCCIONES_MODERACION = (
    "Eres moderadora de reseñas de StyleMe, una página de tiendas de moda y belleza de Bucaramanga. "
    "Decide si esta reseña se PUBLICA o se RECHAZA.\\n"
    "RECHAZA solo si tiene: insultos o groserías, ataques a personas, datos personales (teléfonos, correos, "
    "direcciones de personas, nombres completos de empleados), publicidad o enlaces, o no habla de la tienda o el producto.\\n"
    "PUBLICA las críticas negativas si son respetuosas: son útiles para otras personas. "
    "Una reseña solo con corazones y sin comentario se PUBLICA.\\n"
    "Responde SOLO con JSON: {\\\"decision\\\": \\\"PUBLICADA\\\" o \\\"RECHAZADA\\\", \\\"motivo\\\": \\\"frase corta\\\"}"
)
cuerpo_moderacion = (
    "={{ JSON.stringify({ contents: [{ parts: [{ text: \"" + INSTRUCCIONES_MODERACION + "\\n\\nReseña: \" + "
    "$json.corazones + \" de 5 corazones. Comentario: \" + ($json.comentario || '(sin comentario)') }] }], "
    "generationConfig: { responseMimeType: 'application/json', temperature: 0 } }) }}"
)
leer_decision = """// Une cada reseña con la respuesta de Gemini (vienen en el mismo orden).
// Si la IA responde algo raro, la reseña se deja PENDIENTE (no se publica ni se rechaza a ciegas).
const resenas = $('Traer pendientes').all();
const salida = [];
$input.all().forEach((respuesta, i) => {
  const resena = resenas[i]?.json;
  if (!resena?.id) return;
  try {
    const texto = respuesta.json.candidates[0].content.parts[0].text;
    const { decision, motivo } = JSON.parse(texto);
    if (decision === 'PUBLICADA' || decision === 'RECHAZADA') {
      salida.push({ json: { id: resena.id, decision, motivo: String(motivo ?? '').slice(0, 200) } });
    }
  } catch (e) {
    // respuesta no válida: se intenta de nuevo en la próxima revisión
  }
});
return salida;
"""
guardar(
    "moderacion-resenas.json", "StyleMeModeracion1", "StyleMe · Moderación de reseñas con IA",
    [
        nodo("Reseña nueva", "n8n-nodes-base.webhook", 2, 0, 0,
             {"httpMethod": "POST", "path": "resena-nueva", "options": {}}, webhookId="resena-nueva"),
        cada("Cada 30 minutos", 0, 200, {"field": "minutes", "minutesInterval": 30}),
        peticion("Traer pendientes", 240, 100, "GET", f"={API}/api/admin/resenas/pendientes?limite=50", [CABECERA_ADMIN],
                 alwaysOutputData=False),
        peticion("Preguntar a Gemini", 480, 100, "POST", URL_GEMINI, [CABECERA_GEMINI], cuerpo_moderacion,
                 # Una reseña cada 4 s: respeta el límite por minuto del plan gratuito de Gemini
                 {"batching": {"batch": {"batchSize": 1, "batchInterval": 4000}}, "timeout": 60000}, **REINTENTOS_GEMINI),
        codigo("Leer decisión", 720, 100, leer_decision),
        peticion("Publicar o rechazar", 960, 100, "PATCH", f"={API}/api/admin/resenas/{{{{ $json.id }}}}", [CABECERA_ADMIN],
                 "={{ JSON.stringify({ estado: $json.decision }) }}",
                 # Si otra ejecución ya la moderó, el backend responde 400: se sigue con las demás
                 onError="continueRegularOutput"),
    ],
    conectar(("Reseña nueva", "Traer pendientes"), ("Cada 30 minutos", "Traer pendientes"),
             ("Traer pendientes", "Preguntar a Gemini"), ("Preguntar a Gemini", "Leer decisión"),
             ("Leer decisión", "Publicar o rechazar")),
)

# =====================================================================
# 2. REINDEXACIÓN DE EMBEDDINGS
#    Cada noche a las 3:00 le pide al backend que recalcule los embeddings de lo nuevo o modificado
#    (el backend compara hashes: si nada cambió, no gasta llamadas a Gemini).
# =====================================================================
guardar(
    "reindexacion-embeddings.json", "StyleMeReindexar01", "StyleMe · Reindexación nocturna de embeddings",
    [
        cada("Cada noche 3:00", 0, 0, {"field": "cronExpression", "expression": "0 3 * * *"}),
        *a_mano("reindexar", 0, 200),
        peticion("Reindexar", 480, 0, "POST", f"={API}/api/admin/indexacion", [CABECERA_ADMIN],
                 opciones={"timeout": 300000}),
    ],
    conectar(("Cada noche 3:00", "Reindexar"), ("A mano (reindexar)", "Verificar clave (reindexar)"),
             ("Verificar clave (reindexar)", "Reindexar")),
)

# =====================================================================
# 3. REPORTE SEMANAL CON IA
#    Los lunes a las 8:00 lee los indicadores, le pide a Gemini un resumen con hallazgos y
#    recomendaciones, y lo guarda como n8n/reportes/reporte-AAAA-MM-DD.md
# =====================================================================
resumir_indicadores = """// Los 13 indicadores en texto corto (solo las primeras filas de cada uno): menos tokens para la IA
const lineas = $input.all().map(({ json: ind }) => {
  const filas = (ind.filas || []).slice(0, 6).map((f) => Object.entries(f).map(([k, v]) => `${k}=${v}`).join(', '));
  return `## ${ind.descripcion} (${ind.codigo})\\n${filas.join('\\n') || 'sin datos'}`;
});
return [{ json: { indicadores: lineas.join('\\n\\n'), fecha: $now.toFormat('yyyy-MM-dd') } }];
"""
INSTRUCCIONES_REPORTE = (
    "Eres analista de datos de StyleMe (tiendas de moda y belleza de Bucaramanga). Con estos indicadores escribe, "
    "en español y en Markdown, un reporte semanal corto para la administradora: 1) tres hallazgos con su número, "
    "2) tres recomendaciones concretas, 3) una alerta si algo se ve mal (por ejemplo, búsquedas sin resultado o "
    "reseñas negativas). No inventes datos que no estén aquí. Algunos datos son de prueba. "
    "No escribas título, saludo ni fecha (ya los pone el sistema): empieza directo con '## 1. Hallazgos'."
)
cuerpo_reporte = (
    "={{ JSON.stringify({ contents: [{ parts: [{ text: \"" + INSTRUCCIONES_REPORTE + "\\nFecha de hoy: \" + $json.fecha + \"\\n\\n\" + $json.indicadores }] }], "
    "generationConfig: { temperature: 0.3 } }) }}"
)
armar_reporte = """const texto = $input.first().json.candidates?.[0]?.content?.parts?.[0]?.text ?? 'La IA no respondió esta semana.';
const fecha = $('Resumir indicadores').first().json.fecha;
const contenido = `# Reporte semanal de StyleMe · ${fecha}\\n\\n_Escrito por IA (Gemini) a partir de los indicadores de /api/admin/indicadores._\\n\\n${texto}\\n`;
return [{ json: { contenido, archivo: `/home/node/reportes/reporte-${fecha}.md` } }];
"""
guardar(
    "reporte-semanal.json", "StyleMeReporte0001", "StyleMe · Reporte semanal con IA",
    [
        cada("Lunes 8:00", 0, 0, {"field": "cronExpression", "expression": "0 8 * * 1"}),
        *a_mano("reporte-semanal", 0, 200),
        peticion("Traer indicadores", 480, 0, "GET", f"={API}/api/admin/indicadores", [CABECERA_ADMIN]),
        codigo("Resumir indicadores", 720, 0, resumir_indicadores),
        peticion("Escribir con Gemini", 960, 0, "POST", URL_GEMINI, [CABECERA_GEMINI], cuerpo_reporte, {"timeout": 60000},
                 **{k: v for k, v in REINTENTOS_GEMINI.items() if k != "onError"}),
        codigo("Armar reporte", 1200, 0, armar_reporte),
        nodo("Convertir a archivo", "n8n-nodes-base.convertToFile", 1.1, 1440, 0,
             {"operation": "toText", "sourceProperty": "contenido", "options": {"fileName": "={{ $json.archivo }}"}}),
        nodo("Guardar reporte", "n8n-nodes-base.readWriteFile", 1, 1680, 0,
             {"operation": "write", "fileName": "={{ $('Armar reporte').first().json.archivo }}", "options": {}}),
    ],
    conectar(("Lunes 8:00", "Traer indicadores"), ("A mano (reporte-semanal)", "Verificar clave (reporte-semanal)"),
             ("Verificar clave (reporte-semanal)", "Traer indicadores"), ("Traer indicadores", "Resumir indicadores"),
             ("Resumir indicadores", "Escribir con Gemini"), ("Escribir con Gemini", "Armar reporte"),
             ("Armar reporte", "Convertir a archivo"), ("Convertir a archivo", "Guardar reporte")),
)
