/*
 * LÍA · PLAN B (sin inteligencia artificial)
 * ------------------------------------------
 * Si el cerebro de Lía (la función de Netlify con Gemini) no responde:
 *   - sin internet,
 *   - se acabó el cupo gratis de Gemini,
 *   - o estás probando con Live Server (que no ejecuta funciones),
 * Lía sigue ayudando con REGLAS sencillas: busca palabras del mensaje en el catálogo.
 * Responde con el mismo formato que la IA, así el chat no nota la diferencia.
 * Lía avisa que está en "modo básico" para ser honesta.
 */
(function () {
  // Quita tildes y pasa a minúsculas
  const limpiar = (t) => (t ?? "").normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase();

  // ✏️ Palabras que indican cada categoría
  const PALABRAS_CATEGORIA = {
    ropa: ["ropa", "blusa", "camisa", "jean", "pantalon", "falda", "vestido", "body", "outfit", "prenda", "vestir"],
    maquillaje: ["maquillaje", "base", "labial", "pestanina", "mascara", "rubor", "iluminador", "polvo", "sombra", "maquillar"],
    skincare: ["piel", "skincare", "cara", "rostro", "serum", "suero", "protector", "bloqueador", "hidratante", "limpiador", "tonico", "acne", "brillo", "grasa", "manchas"],
    capilar: ["cabello", "pelo", "shampoo", "champu", "acondicionador", "rizos", "crespo", "frizz", "keratina", "capilar", "peinar"]
  };
  const PALABRAS_SALUD = /ardor|arde|alergi|sangr|herida|dermatitis|infecci|hongo|ronch|quemadura|psoriasis|rosacea|se me cae el (pelo|cabello)/;
  const SALUDOS = /^(hola|buenas|buenos dias|buenas tardes|buenas noches|hey|holi)\b/;
  const PALABRAS_VACIAS = new Set(["para", "que", "una", "uno", "los", "las", "con", "por", "del", "algo", "quiero", "busco", "necesito", "tengo", "mas", "muy", "como", "este", "esta", "me", "mi", "de", "la", "el", "en", "y", "un", "lo", "se", "es"]);

  function leerPresupuesto(t) {
    const m = t.match(/(?:maximo|hasta|menos de|no mas de|presupuesto(?: de)?)\s*\$?\s*(\d{1,3}(?:[.\s]\d{3})+|\d+)\s*(mil|k)?/);
    if (!m) return null;
    let n = Number(m[1].replace(/[.\s]/g, ""));
    if (m[2] || n < 1000) n *= 1000;
    return n;
  }

  function textoProducto(p) {
    return limpiar([p.nombre, p.marca, p.categoria, p.descripcion, ...(p.objetivos || []), ...(p.tipoPiel || []), ...(p.tipoCabello || [])].join(" "));
  }

  function responder(mensaje, historia = []) {
    const t = limpiar(mensaje);
    const sugerenciasBase = ["Busco maquillaje", "Rutina para mi piel", "Algo para mi cabello"];

    if (PALABRAS_SALUD.test(t)) {
      return {
        texto: "Lo que me cuentas merece atención. No puedo hacer diagnósticos: te recomiendo consultar a un dermatólogo antes de usar productos nuevos. Si quieres, te muestro opciones suaves para piel sensible.",
        emocion: "preocupacion", intensidad: 0.7, productos: [], acciones: [],
        sugerencias: ["Opciones para piel sensible", "Ver tiendas"]
      };
    }
    const sobreLia = /\b(tu (outfit|diseno|estilo|ropa|look|sueter)|como te ves)\b/.test(t);
    if (sobreLia && /\b(horrible|feo|fea|no me gusta|odio|espantoso)\b/.test(t)) {
      return {
        texto: "Entiendo, mi estilo no es para todo el mundo. ¿Qué cambiarías del diseño?",
        emocion: "neutra", intensidad: 0.6, productos: [], acciones: [], sugerencias: []
      };
    }
    if (sobreLia && /\b(me encanta|me gusta|lindo|bonito|hermoso|genial)\b/.test(t)) {
      return {
        texto: "¡Gracias! Mi parte favorita es el broche rosa que llevo en el suéter.",
        emocion: "felicidad", intensidad: 0.8, productos: [], acciones: [], sugerencias: []
      };
    }
    if (SALUDOS.test(t) && t.split(/\s+/).length <= 3) {
      return {
        texto: "¡Hola! Soy Lía, tu asesora en StyleMe. Te ayudo a encontrar ropa, maquillaje y cuidado para tu piel y tu cabello en tiendas de Bucaramanga. ¿Qué estás buscando hoy?",
        emocion: "felicidad", intensidad: 0.7, productos: [], acciones: [], sugerencias: sugerenciasBase
      };
    }
    if (/\b(adivina que|tengo una sorpresa|no te vas a creer|no lo vas a creer)\b/.test(t)) {
      return {
        texto: "¡Qué sorpresa! Cuéntame qué pasó.",
        emocion: "asombro", intensidad: 0.85, productos: [], acciones: [], sugerencias: []
      };
    }
    // "¿Dónde queda?": la tienda del último producto que mostró Lía
    if (/donde queda|direccion|como llego|ubicacion|donde esta/.test(t)) {
      const ultimo = [...historia].reverse().find((h) => h.rol === "lia" && h.productos?.length);
      const tienda = ultimo && buscarTienda(buscarProducto(ultimo.productos[0])?.tiendaId);
      if (tienda) {
        return {
          texto: `${tienda.nombre} queda en ${tienda.direccion}${tienda.zona && tienda.zona !== "Por confirmar" ? `, en ${tienda.zona}` : ""}. Antes de ir, confirma el horario con la tienda.`,
          emocion: "felicidad", intensidad: 0.5, productos: [],
          acciones: [{ tipo: "ver_tienda", tienda: tienda.id }, { tipo: "contactar_tienda", tienda: tienda.id }],
          sugerencias: ["Ármame la salida", "Busco otra cosa"]
        };
      }
    }
    if (/salida|recorrido|ruta|ir a las tiendas/.test(t)) {
      return {
        texto: "¡Me encanta la idea! Te armo un recorrido por tiendas cercanas entre sí en la sección Salida de compras.",
        emocion: "felicidad", intensidad: 0.6, productos: [], acciones: [{ tipo: "armar_salida" }], sugerencias: ["Busco maquillaje"]
      };
    }

    // Categoría: la del mensaje. Si el mensaje no dice ninguna, la del mensaje anterior
    // (para entender continuaciones como "¿algo más económico?").
    const buscarCategoria = (texto) =>
      Object.keys(PALABRAS_CATEGORIA).find((c) => PALABRAS_CATEGORIA[c].some((p) => texto.includes(p)));
    const anterior = limpiar(historia.filter((h) => h.rol === "persona").slice(-1).map((h) => h.texto).join(" "));
    const categoriaPropia = buscarCategoria(t);
    const categoria = categoriaPropia || buscarCategoria(anterior);
    // El presupuesto del mensaje anterior solo vale si se sigue hablando de lo mismo
    const presupuesto = leerPresupuesto(t) || (!categoriaPropia ? leerPresupuesto(anterior) : null);
    const palabras = t.split(/[^a-z0-9ñ]+/).filter((p) => p.length > 2 && !PALABRAS_VACIAS.has(p));
    const masEconomico = /economic|barat|menos plata|mas bajo/.test(t);
    const continuacion = masEconomico || /otra opcion|otro|otra|mas opciones|algo mas/.test(t);
    // Productos que Lía ya mostró (para no repetirlos al pedir "otra opción")
    const yaMostrados = new Set(historia.filter((h) => h.rol === "lia").flatMap((h) => h.productos || []));

    const resultados = PRODUCTOS
      .filter((p) => !p.pendiente)
      .filter((p) => !categoria || p.categoria === categoria)
      .filter((p) => !presupuesto || p.precio === null || p.precio <= presupuesto)
      .filter((p) => !continuacion || !yaMostrados.has(p.id))
      .map((p) => {
        const texto = textoProducto(p);
        return { p, puntos: palabras.reduce((s, w) => s + (texto.includes(w) ? 1 : 0), 0) };
      })
      // Si la categoría salió del mensaje anterior, solo valen coincidencias reales
      // (así "un perfume" no muestra la ropa de antes), salvo que sea una continuación.
      .filter((r) => r.puntos > 0 || categoriaPropia || continuacion)
      .sort((a, b) => (masEconomico ? (a.p.precio ?? Infinity) - (b.p.precio ?? Infinity) : b.puntos - a.puntos))
      .slice(0, 3)
      .map((r) => r.p);

    if (!categoria && !resultados.length) {
      return {
        texto: "Cuéntame un poco más: ¿buscas ropa, maquillaje, algo para tu piel o para tu cabello?",
        emocion: "duda", intensidad: 0.6, productos: [], acciones: [],
        sugerencias: ["Ropa", "Maquillaje", "Skincare", "Cuidado capilar"].slice(0, 3)
      };
    }
    if (!resultados.length) {
      return {
        texto: "Lo siento, no encontré algo así en las tiendas registradas. ¿Probamos con otro presupuesto u otra búsqueda?",
        emocion: "tristeza", intensidad: 0.5, productos: [], acciones: [],
        sugerencias: sugerenciasBase
      };
    }
    const tienda = buscarTienda(resultados[0].tiendaId);
    const nombres = resultados.map((p) => `${p.nombre} (${formatoPrecio(p.precio)})`).join(", ");
    return {
      texto: `Mira estas opciones: ${nombres}. La primera la encuentras en ${tienda?.nombre ?? "una tienda local"}.`,
      emocion: "felicidad", intensidad: 0.6,
      productos: resultados.map((p) => p.id),
      acciones: [{ tipo: "ir_a_pasillo", pasillo: resultados[0].categoria }],
      sugerencias: ["¿Algo más económico?", "¿Dónde queda la tienda?", "Ármame la salida"]
    };
  }

  window.LiaPlanB = { responder };
})();
