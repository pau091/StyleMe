/*
 * COMÚN
 * -----
 * Funciones que usan todas las páginas: encabezado, pie de página,
 * enlaces de WhatsApp y Google Maps, formato de precios y tarjetas.
 * Se carga DESPUÉS de data/tiendas.js y data/productos.js.
 */

const CATEGORIAS = {
  ropa: { nombre: "Ropa y moda", icono: "👗" },
  maquillaje: { nombre: "Maquillaje", icono: "💄" },
  skincare: { nombre: "Skincare", icono: "🧴" },
  capilar: { nombre: "Cuidado capilar", icono: "💇" }
};

const TIPOS_TIENDA = {
  ropa: "Ropa y moda",
  maquillaje: "Maquillaje y belleza"
};

// Centro aproximado de Bucaramanga, para el mapa
const CENTRO_BUCARAMANGA = [7.1193, -73.1227];

/* ---------- Utilidades ---------- */

// Evita que un texto con < o > se interprete como HTML
function escaparHTML(texto) {
  const div = document.createElement("div");
  div.textContent = texto ?? "";
  return div.innerHTML;
}

// Lee un parámetro de la URL. Ejemplo: tienda.html?id=bohor → parametroURL("id") = "bohor"
function parametroURL(nombre) {
  return new URLSearchParams(window.location.search).get(nombre);
}

function formatoPrecio(precio) {
  if (precio === null || precio === undefined) return "Precio por confirmar";
  return "$" + precio.toLocaleString("es-CO");
}

function buscarTienda(id) {
  return TIENDAS.find((t) => t.id === id);
}

function buscarProducto(id) {
  return PRODUCTOS.find((p) => p.id === id);
}

function productosDeTienda(tiendaId) {
  return PRODUCTOS.filter((p) => p.tiendaId === tiendaId);
}

function tieneUbicacion(tienda) {
  return typeof tienda.lat === "number" && typeof tienda.lng === "number";
}

/* ---------- Enlaces externos ---------- */

function enlaceWhatsapp(numero, mensaje) {
  if (!numero) return "";
  return `https://wa.me/${numero}?text=${encodeURIComponent(mensaje)}`;
}

function enlaceComoLlegar(tienda) {
  const destino = tieneUbicacion(tienda)
    ? `${tienda.lat},${tienda.lng}`
    : `${tienda.direccion}, Bucaramanga, Santander`;
  return `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(destino)}`;
}

function enlaceInstagram(usuario) {
  return usuario ? `https://www.instagram.com/${usuario}/` : "";
}

/* ---------- Mapas (solo en páginas que cargan Leaflet) ---------- */

function crearMapa(idElemento, centro = CENTRO_BUCARAMANGA, zoom = 14) {
  const mapa = L.map(idElemento).setView(centro, zoom);
  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
  }).addTo(mapa);
  return mapa;
}

// Texto del globito que aparece al tocar una tienda en el mapa
function popupTienda(tienda) {
  return `<strong>${escaparHTML(tienda.nombre)}</strong><br>
    ${escaparHTML(tienda.direccion)}<br>
    <a href="tienda.html?id=${encodeURIComponent(tienda.id)}">Ver tienda</a> ·
    <a href="${enlaceComoLlegar(tienda)}" target="_blank" rel="noopener">Cómo llegar</a>`;
}

/* ---------- Piezas visuales ---------- */

// Muestra la foto, o un recuadro con ícono si todavía no hay foto
function imagenOMarcador(src, alt, categoria) {
  if (src) {
    return `<img src="${escaparHTML(src)}" alt="${escaparHTML(alt)}" loading="lazy">`;
  }
  const icono = CATEGORIAS[categoria]?.icono ?? "🛍️";
  return `<div class="marcador-imagen" aria-hidden="true">${icono}</div>`;
}

function tarjetaTienda(tienda) {
  const cantidad = productosDeTienda(tienda.id).length;
  return `
    <a class="tarjeta" href="tienda.html?id=${encodeURIComponent(tienda.id)}">
      <div class="tarjeta-imagen">${imagenOMarcador(tienda.foto, tienda.nombre, tienda.tipo)}</div>
      <div class="tarjeta-cuerpo">
        <span class="etiqueta">${escaparHTML(TIPOS_TIENDA[tienda.tipo])}</span>
        <h3>${escaparHTML(tienda.nombre)}</h3>
        <p class="texto-suave">📍 ${escaparHTML(tienda.direccion)}</p>
        <p class="texto-suave">${cantidad} producto${cantidad === 1 ? "" : "s"}</p>
      </div>
    </a>`;
}

function tarjetaProducto(producto) {
  const tienda = buscarTienda(producto.tiendaId);
  const botonProbador =
    producto.categoria === "ropa"
      ? `<a class="boton boton-secundario boton-pequeno" href="probador.html?producto=${encodeURIComponent(producto.id)}">Probar virtualmente</a>`
      : "";
  return `
    <article class="tarjeta">
      <div class="tarjeta-imagen">
        ${imagenOMarcador(producto.foto, producto.nombre, producto.categoria)}
        ${producto.pendiente ? '<span class="sello">Ejemplo</span>' : ""}
      </div>
      <div class="tarjeta-cuerpo">
        <span class="etiqueta">${escaparHTML(CATEGORIAS[producto.categoria]?.nombre)}</span>
        <h3>${escaparHTML(producto.nombre)}</h3>
        <p class="precio">${formatoPrecio(producto.precio)}</p>
        ${tienda ? `<a class="texto-suave" href="tienda.html?id=${encodeURIComponent(tienda.id)}">🏬 ${escaparHTML(tienda.nombre)}</a>` : ""}
        ${botonProbador}
      </div>
    </article>`;
}

/* ---------- Encabezado y pie de página ---------- */

const ENLACES_MENU = [
  { href: "index.html", texto: "Inicio" },
  { href: "tiendas.html", texto: "Tiendas" },
  { href: "productos.html", texto: "Productos" },
  { href: "mapa.html", texto: "Mapa" },
  { href: "salida.html", texto: "Salida de compras" },
  { href: "probador.html", texto: "Probador" },
  { href: "recomendador.html", texto: "Recomendador" },
  { href: "acerca.html", texto: "Acerca de" }
];

function pintarEncabezado() {
  const contenedor = document.getElementById("encabezado");
  if (!contenedor) return;

  const paginaActual = window.location.pathname.split("/").pop() || "index.html";
  const enlaces = ENLACES_MENU.map(
    (e) =>
      `<li><a href="${e.href}" ${e.href === paginaActual ? 'aria-current="page"' : ""}>${e.texto}</a></li>`
  ).join("");

  contenedor.innerHTML = `
    <div class="contenedor barra">
      <a class="logo" href="index.html">Style<span>Me</span></a>
      <button class="boton-menu" aria-expanded="false" aria-controls="menu-principal">☰ Menú</button>
      <nav aria-label="Principal">
        <ul id="menu-principal" class="menu">${enlaces}</ul>
      </nav>
    </div>`;

  const boton = contenedor.querySelector(".boton-menu");
  const menu = contenedor.querySelector(".menu");
  boton.addEventListener("click", () => {
    const abierto = menu.classList.toggle("abierto");
    boton.setAttribute("aria-expanded", abierto);
  });
}

function pintarPie() {
  const contenedor = document.getElementById("pie");
  if (!contenedor) return;
  contenedor.innerHTML = `
    <div class="contenedor">
      <p><strong>StyleMe</strong>: tiendas de moda y belleza de Bucaramanga en un solo lugar.</p>
      <p class="texto-suave">
        Proyecto académico. StyleMe no tiene acuerdos comerciales con las tiendas mostradas.
        La información puede cambiar: confírmala directamente con cada tienda.
        ¿Eres una tienda y quieres corregir o retirar tu información? <a href="acerca.html#contacto">Escríbenos</a>.
      </p>
    </div>`;
}

/* ---------- Revisión de datos (solo en tu computador) ---------- */

// Muestra en la consola (F12) lo que falta completar en data/tiendas.js y data/productos.js.
// Solo se ejecuta con Live Server; en la página publicada no aparece.
function revisarDatos() {
  const enLocal = ["localhost", "127.0.0.1"].includes(window.location.hostname);
  if (!enLocal) return;

  const problemas = [];
  const agregar = (archivo, id, problema) => problemas.push({ archivo, id, problema });
  const idsTiendas = new Set();

  TIENDAS.forEach((t) => {
    if (idsTiendas.has(t.id)) agregar("tiendas.js", t.id, "❌ id repetido");
    idsTiendas.add(t.id);
    if (!tieneUbicacion(t)) agregar("tiendas.js", t.id, "Falta lat y lng (no sale en el mapa)");
    if (!t.whatsapp) agregar("tiendas.js", t.id, "Falta WhatsApp");
    if (!t.instagram) agregar("tiendas.js", t.id, "Falta Instagram");
    if (!t.horario || t.horario === "Por confirmar") agregar("tiendas.js", t.id, "Falta horario");
    if (!t.verificado) agregar("tiendas.js", t.id, "Sin verificar");
  });

  const idsProductos = new Set();
  PRODUCTOS.forEach((p) => {
    if (idsProductos.has(p.id)) agregar("productos.js", p.id, "❌ id repetido");
    idsProductos.add(p.id);
    if (!idsTiendas.has(p.tiendaId)) agregar("productos.js", p.id, `❌ La tienda "${p.tiendaId}" no existe`);
    if (!CATEGORIAS[p.categoria]) agregar("productos.js", p.id, `❌ Categoría "${p.categoria}" no válida`);
    if (p.pendiente) agregar("productos.js", p.id, "Es un ejemplo (pendiente: true)");
    if (!p.foto) agregar("productos.js", p.id, "Falta foto");
    if (p.precio === null) agregar("productos.js", p.id, "Falta precio");
    if (p.categoria === "ropa" && !["superior", "inferior", "completa"].includes(p.tipoPrenda))
      agregar("productos.js", p.id, "❌ Falta tipoPrenda (superior, inferior o completa)");
    if ((p.categoria === "skincare" || p.categoria === "capilar") && !p.paso)
      agregar("productos.js", p.id, "Falta paso (no entra en la rutina del recomendador)");
  });

  const graves = problemas.filter((p) => p.problema.startsWith("❌")).length;
  console.groupCollapsed(
    `%cStyleMe · revisión de datos: ${problemas.length} pendientes (${graves} errores)`,
    "color:#b03a6a;font-weight:bold"
  );
  console.table(problemas);
  console.groupEnd();
}

// Avisa en la consola si una foto no carga (ruta mal escrita o archivo inexistente)
window.addEventListener(
  "error",
  (evento) => {
    if (evento.target instanceof HTMLImageElement) {
      console.error(`🖼️ No se encontró la imagen: ${evento.target.getAttribute("src")}`);
    }
  },
  true
);

document.addEventListener("DOMContentLoaded", () => {
  pintarEncabezado();
  pintarPie();
  // Se espera un momento para que cada página pinte sus imágenes
  setTimeout(revisarDatos, 500);
});
