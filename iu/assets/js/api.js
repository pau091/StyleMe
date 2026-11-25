/*
 * API · CONEXIÓN CON EL BACKEND
 * -----------------------------
 * La página ya no tiene los datos guardados en archivos: todo sale del backend (Spring Boot + PostgreSQL).
 * Este archivo se carga ANTES de comun.js en todas las páginas y hace cinco cosas:
 *
 *   1. CATÁLOGO: trae tiendas y productos (GET /api/tiendas y /api/productos) y los deja en
 *      TIENDAS y PRODUCTOS con la forma que ya usaban las páginas. Guarda una copia por 5 minutos
 *      en la pestaña (sessionStorage) para que cambiar de página sea instantáneo.
 *   2. SESIÓN ANÓNIMA: un identificador al azar por navegador (D-066). Sirve para reseñas,
 *      favoritos y eventos sin pedir registro ni datos personales.
 *   3. EVENTOS: anota lo que pasa (ver tienda, clic en WhatsApp, búsquedas...) y lo manda en lotes
 *      a POST /api/eventos. Con eso se calculan los indicadores del informe de datos.
 *   4. FAVORITOS, RESEÑAS Y BÚSQUEDA SEMÁNTICA: funciones pequeñas para llamar a la API.
 *   5. ADMINISTRACIÓN: llamadas con la clave de administración (cabecera X-Admin-Key).
 *
 * Cada página pinta su contenido con cuandoHayaDatos(() => { ... }).
 */

// Las páginas leen estas dos listas (antes venían de data/tiendas.js y data/productos.js)
let TIENDAS = [];
let PRODUCTOS = [];

(function () {
  /* ---------- Dirección del backend ---------- */
  // Con Live Server (puerto 5500) el backend está en el puerto 8090 del mismo computador.
  // Servida por nginx (Docker), la página y la API comparten dirección: /api/... (rama feature/n8n-docker).
  // ✏️ Se puede cambiar sin tocar este archivo definiendo window.STYLEME_API antes de cargarlo.
  function direccionPorDefecto() {
    const { protocol, hostname, port } = window.location;
    if (protocol === "file:" || port === "5500") return `http://${hostname || "localhost"}:8090`;
    return "";
  }
  const API = window.STYLEME_API ?? direccionPorDefecto();
  window.STYLEME_API = API; // Lía (lia.js) usa la misma dirección

  const MINUTOS_COPIA = 5;
  const CLAVE_CATALOGO = "styleme-catalogo";
  const CLAVE_SESION = "styleme-sesion";
  const CLAVE_ADMIN = "styleme-clave-admin";
  const CLAVE_FAVORITOS = "styleme-favoritos";

  // Si el catálogo y los favoritos ya están guardados en la pestaña, las páginas se pintan al instante
  // (sin esperar al backend). Así la foto de la tienda alcanza a "viajar" de la tarjeta al perfil.
  let catalogoEnMemoria = false;
  let favoritosEnMemoria = false;

  /* ---------- Almacenamiento seguro (navegación privada puede bloquearlo) ---------- */
  const almacen = {
    leer(tipo, clave) { try { return window[tipo].getItem(clave); } catch (e) { return null; } },
    guardar(tipo, clave, valor) { try { window[tipo].setItem(clave, valor); } catch (e) { /* no pasa nada */ } },
    borrar(tipo, clave) { try { window[tipo].removeItem(clave); } catch (e) { /* no pasa nada */ } }
  };

  /* ---------- Peticiones ---------- */
  class ErrorApi extends Error {
    constructor(estado, mensaje) {
      super(mensaje);
      this.estado = estado;
    }
  }

  // Hace la petición y devuelve el JSON. Si la API responde un error, lanza ErrorApi con su mensaje.
  async function pedir(ruta, { metodo = "GET", cuerpo, cabeceras = {} } = {}) {
    let respuesta;
    try {
      respuesta = await fetch(API + ruta, {
        method: metodo,
        headers: cuerpo === undefined ? cabeceras : { "Content-Type": "application/json", ...cabeceras },
        body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo)
      });
    } catch (e) {
      throw new ErrorApi(0, "No pudimos conectar con StyleMe. Revisa tu conexión o inténtalo en un momento.");
    }
    if (respuesta.status === 204) return null;
    const datos = await respuesta.json().catch(() => null);
    if (!respuesta.ok) throw new ErrorApi(respuesta.status, datos?.mensaje ?? "Algo salió mal. Inténtalo de nuevo.");
    return datos;
  }

  /* ---------- 1. Catálogo ---------- */
  // Del JSON de la API a la forma que usan las páginas
  function aTienda(t) {
    return {
      id: t.slug,
      nombre: t.nombre,
      tipo: t.categoria?.slug, // "ropa" o "maquillaje"
      descripcion: t.descripcion ?? "",
      direccion: t.direccion ?? "Dirección por confirmar",
      zona: t.zona ?? "",
      lat: t.coordenadas ? Number(t.coordenadas.latitud) : null,
      lng: t.coordenadas ? Number(t.coordenadas.longitud) : null,
      horario: t.horario ?? "Por confirmar",
      whatsapp: t.contacto?.whatsapp ?? "",
      telefono: t.contacto?.telefono ?? "",
      instagram: t.contacto?.instagram ?? "",
      web: t.contacto?.web ?? "",
      foto: t.foto ?? "",
      verificado: t.verificada
    };
  }

  function aProducto(p) {
    const e = p.etiquetas ?? {};
    return {
      id: p.codigo,
      tiendaId: p.tienda?.slug,
      nombre: p.nombre,
      marca: p.marca ?? "",
      categoria: p.categoria?.slug,
      precio: p.precio ?? null,
      descripcion: p.descripcion ?? "",
      ingredientes: p.ingredientes ?? [],
      foto: p.foto ?? "",
      fuente: p.fuente ?? "",
      pendiente: p.esEjemplo,
      tipoPrenda: p.tipoPrenda ?? null,
      fotoProbador: p.disponibleEnProbador ? p.fotoProbador : null,
      // Etiquetas del recomendador (en la base son filas de producto_etiqueta)
      paso: e.paso?.[0] ?? null,
      tipoPiel: e.tipo_piel ?? [],
      tipoCabello: e.tipo_cabello ?? [],
      cuero: e.cuero ?? [],
      objetivos: e.objetivo ?? [],
      libreDe: e.libre_de ?? []
    };
  }

  function usarCatalogo(catalogo) {
    TIENDAS = catalogo.tiendas;
    PRODUCTOS = catalogo.productos;
  }

  async function cargarCatalogo() {
    const copia = JSON.parse(almacen.leer("sessionStorage", CLAVE_CATALOGO) || "null");
    if (copia && Date.now() - copia.fecha < MINUTOS_COPIA * 60_000) {
      usarCatalogo(copia);
      catalogoEnMemoria = true;
      return;
    }
    const [tiendas, productos] = await Promise.all([pedir("/api/tiendas"), pedir("/api/productos")]);
    const catalogo = { fecha: Date.now(), tiendas: tiendas.map(aTienda), productos: productos.map(aProducto) };
    usarCatalogo(catalogo);
    almacen.guardar("sessionStorage", CLAVE_CATALOGO, JSON.stringify(catalogo));
  }

  /* ---------- 2. Sesión anónima ---------- */
  function nuevoIdentificador() {
    if (window.crypto?.randomUUID) return crypto.randomUUID();
    return "s-" + Date.now().toString(36) + "-" + Math.random().toString(36).slice(2, 12);
  }
  let sesionId = almacen.leer("localStorage", CLAVE_SESION);
  if (!sesionId) {
    sesionId = nuevoIdentificador();
    almacen.guardar("localStorage", CLAVE_SESION, sesionId);
  }
  const dispositivo = window.matchMedia("(pointer: coarse)").matches || window.innerWidth < 768 ? "celular" : "computador";

  /* ---------- 3. Eventos ---------- */
  // Se juntan y se mandan cada pocos segundos (o al salir de la página), no uno por uno.
  const MAXIMO_LOTE = 50; // el backend acepta hasta 50 por envío
  let cola = [];
  let temporizador = null;

  function registrarEvento(tipo, datos = {}) {
    const evento = { tipo, sesionId, dispositivo };
    // Solo se mandan los campos con valor (el backend rechaza datos vacíos en "detalle")
    Object.entries(datos).forEach(([clave, valor]) => {
      if (valor !== null && valor !== undefined && valor !== "") evento[clave] = valor;
    });
    cola.push(evento);
    if (cola.length >= 20) enviarEventos();
    else if (!temporizador) temporizador = setTimeout(enviarEventos, 4000);
  }

  function enviarEventos() {
    clearTimeout(temporizador);
    temporizador = null;
    while (cola.length) {
      const lote = cola.splice(0, MAXIMO_LOTE);
      // keepalive: el envío termina aunque la persona ya se haya ido a otra página
      fetch(API + "/api/eventos", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(lote),
        keepalive: true
      }).catch(() => { /* si el backend no está, los eventos se pierden: no afecta a la persona */ });
    }
  }
  document.addEventListener("visibilitychange", () => { if (document.hidden) enviarEventos(); });
  window.addEventListener("pagehide", enviarEventos);

  // Clics en WhatsApp y "Cómo llegar": cualquier enlace con data-evento-tienda="slug" se cuenta solo
  document.addEventListener("click", (evento) => {
    const enlace = evento.target.closest?.("a[data-evento-tienda]");
    if (!enlace) return;
    const href = enlace.getAttribute("href") ?? "";
    const tipo = href.includes("wa.me/") ? "CLIC_WHATSAPP" : href.includes("google.com/maps") ? "CLIC_COMO_LLEGAR" : null;
    if (tipo) registrarEvento(tipo, { tienda: enlace.dataset.eventoTienda });
  });

  /* ---------- 4. Favoritos ---------- */
  // Se guardan como "TIENDA:bohor" o "PRODUCTO:p001" para buscarlos rápido
  const favoritos = new Set();
  const claveFavorito = (tipo, referencia) => `${tipo}:${referencia}`;
  const objetivo = (tipo, referencia) => (tipo === "TIENDA" ? { tienda: referencia } : { producto: referencia });

  const guardarCopiaFavoritos = () => almacen.guardar("sessionStorage", CLAVE_FAVORITOS, JSON.stringify([...favoritos]));
  const copiaFavoritos = JSON.parse(almacen.leer("sessionStorage", CLAVE_FAVORITOS) || "null");
  if (Array.isArray(copiaFavoritos)) {
    copiaFavoritos.forEach((clave) => favoritos.add(clave));
    favoritosEnMemoria = true;
  }

  async function cargarFavoritos() {
    try {
      const lista = await pedir(`/api/favoritos?sesionId=${encodeURIComponent(sesionId)}`);
      favoritos.clear();
      lista.forEach((f) => favoritos.add(claveFavorito(f.tipo, f.referencia)));
      guardarCopiaFavoritos();
      return lista;
    } catch (e) {
      return []; // sin favoritos no se rompe nada
    }
  }

  function esFavorito(tipo, referencia) {
    return favoritos.has(claveFavorito(tipo, referencia));
  }

  // Agrega o quita; devuelve true si quedó como favorito
  async function alternarFavorito(tipo, referencia) {
    const clave = claveFavorito(tipo, referencia);
    if (favoritos.has(clave)) {
      await pedir(`/api/favoritos?sesionId=${encodeURIComponent(sesionId)}&${tipo === "TIENDA" ? "tienda" : "producto"}=${encodeURIComponent(referencia)}`, { metodo: "DELETE" });
      favoritos.delete(clave);
      guardarCopiaFavoritos();
      return false;
    }
    await pedir("/api/favoritos", { metodo: "POST", cuerpo: { sesionId, ...objetivo(tipo, referencia) } });
    favoritos.add(clave);
    guardarCopiaFavoritos();
    return true;
  }

  // Botones de corazón: <button data-favorito="PRODUCTO:p001">. Funciona en cualquier página.
  document.addEventListener("click", async (evento) => {
    const boton = evento.target.closest?.("button[data-favorito]");
    if (!boton) return;
    evento.preventDefault();
    const [tipo, referencia] = boton.dataset.favorito.split(":");
    boton.disabled = true;
    try {
      const guardado = await alternarFavorito(tipo, referencia);
      document.querySelectorAll(`button[data-favorito="${CSS.escape(boton.dataset.favorito)}"]`).forEach((b) => pintarBotonFavorito(b, guardado));
      document.dispatchEvent(new CustomEvent("styleme:favoritos", { detail: { tipo, referencia, guardado } }));
    } catch (e) {
      boton.title = e.message;
    } finally {
      boton.disabled = false;
    }
  });

  function pintarBotonFavorito(boton, guardado) {
    boton.setAttribute("aria-pressed", guardado);
    boton.setAttribute("aria-label", guardado ? "Quitar de favoritos" : "Guardar en favoritos");
    boton.textContent = guardado ? "♥" : "♡";
  }

  // HTML del botón (lo usan las tarjetas y el perfil de la tienda)
  function botonFavorito(tipo, referencia) {
    const guardado = esFavorito(tipo, referencia);
    return `<button type="button" class="boton-favorito" data-favorito="${tipo}:${escaparTexto(referencia)}"
      aria-pressed="${guardado}" aria-label="${guardado ? "Quitar de favoritos" : "Guardar en favoritos"}">${guardado ? "♥" : "♡"}</button>`;
  }

  function escaparTexto(texto) {
    return String(texto ?? "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[c]);
  }

  /* ---------- 4. Reseñas ---------- */
  function resenas(tipo, referencia) {
    const parametro = tipo === "TIENDA" ? "tienda" : "producto";
    return pedir(`/api/resenas?${parametro}=${encodeURIComponent(referencia)}`);
  }

  function escribirResena(tipo, referencia, { autor, corazones, comentario }) {
    return pedir("/api/resenas", { metodo: "POST", cuerpo: { sesionId, autor, corazones, comentario, ...objetivo(tipo, referencia) } });
  }

  /* ---------- 4. Búsqueda semántica ---------- */
  // ✏️ Cuándo un resultado "sí tiene que ver". Se midió con búsquedas reales (similitud de 0 a 1):
  //   lo que StyleMe sí tiene → el mejor resultado da 0,66 o más ("vestido negro" 0,74 · "jean" 0,68)
  //   lo que no tiene         → el mejor da 0,63 o menos ("zapatos" 0,62 · "bolsos" 0,62 · "perfume" 0,60)
  const SIMILITUD_MEJOR_MINIMA = 0.64; // si el mejor resultado no llega aquí, no hay resultados
  const DISTANCIA_AL_MEJOR = 0.1;      // se muestran los que estén cerca del mejor
  const SIMILITUD_PISO = 0.58;

  // Devuelve los códigos de producto ordenados del más parecido al menos parecido (sin repetir)
  async function buscarProductos(texto, { categoria, precioMaximo } = {}) {
    const parametros = new URLSearchParams({ q: texto, tipo: "producto", limite: "20", similitudMinima: String(SIMILITUD_PISO) });
    if (categoria) parametros.set("categoria", categoria);
    if (precioMaximo) parametros.set("precioMaximo", precioMaximo);
    const resultados = await pedir(`/api/busqueda?${parametros}`);
    const mejor = resultados[0]?.similitud ?? 0;
    if (mejor < SIMILITUD_MEJOR_MINIMA) return []; // StyleMe no tiene eso (cuenta como "búsqueda sin resultado")
    const corte = Math.max(SIMILITUD_PISO, mejor - DISTANCIA_AL_MEJOR);
    return [...new Set(resultados.filter((r) => r.similitud >= corte).map((r) => r.codigoProducto).filter(Boolean))];
  }

  /* ---------- 5. Administración ---------- */
  const claveAdmin = {
    leer: () => almacen.leer("sessionStorage", CLAVE_ADMIN),
    guardar: (clave) => almacen.guardar("sessionStorage", CLAVE_ADMIN, clave),
    borrar: () => almacen.borrar("sessionStorage", CLAVE_ADMIN)
  };

  function pedirAdmin(ruta, opciones = {}) {
    return pedir(ruta, { ...opciones, cabeceras: { "X-Admin-Key": claveAdmin.leer() ?? "" } });
  }

  /* ---------- Arranque ---------- */
  const datos = cargarCatalogo();
  const favoritosCargados = datos.then(cargarFavoritos, () => []);
  const domListo = new Promise((listo) => {
    if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", listo, { once: true });
    else listo();
  });

  window.StyleMe = {
    api: API,
    sesionId,
    dispositivo,
    datos,
    ErrorApi,
    pedir,
    registrarEvento,
    enviarEventos,
    cargarFavoritos,
    esFavorito,
    alternarFavorito,
    botonFavorito,
    resenas,
    escribirResena,
    buscarProductos,
    claveAdmin,
    pedirAdmin
  };

  // Si el backend no responde, se avisa arriba del contenido (una sola vez)
  function avisarSinConexion(error) {
    console.error("StyleMe: no se pudo cargar el catálogo", error);
    if (document.getElementById("aviso-conexion")) return;
    const aviso = document.createElement("p");
    aviso.id = "aviso-conexion";
    aviso.className = "aviso";
    aviso.setAttribute("role", "alert");
    aviso.textContent = "No pudimos cargar las tiendas en este momento. Revisa tu conexión y recarga la página.";
    (document.querySelector("main") ?? document.body).prepend(aviso);
  }

  // Ejecuta "pintar" cuando ya están el catálogo, los favoritos y la página
  // (Los scripts de cada página van al final del <body>: cuando se ejecutan, sus elementos ya existen.)
  window.cuandoHayaDatos = function (pintar) {
    if (catalogoEnMemoria && favoritosEnMemoria) {
      pintar();
      return Promise.resolve();
    }
    return Promise.all([datos, favoritosCargados, domListo]).then(() => pintar(), avisarSinConexion);
  };
})();
