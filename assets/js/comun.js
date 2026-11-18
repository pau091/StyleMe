/*
 * COMÚN
 * -----
 * Funciones que usan todas las páginas: encabezado, pie de página,
 * enlaces de WhatsApp y Google Maps, formato de precios y tarjetas.
 * Se carga DESPUÉS de data/tiendas.js y data/productos.js.
 */

const CATEGORIAS = {
  ropa: { nombre: "Ropa y moda" },
  maquillaje: { nombre: "Maquillaje" },
  skincare: { nombre: "Skincare" },
  capilar: { nombre: "Cuidado capilar" }
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

// Acepta "bohor.com.co" o "https://bohor.com.co" y siempre devuelve una dirección completa
function enlaceWeb(direccion) {
  if (!direccion) return "";
  return /^https?:\/\//.test(direccion) ? direccion : `https://${direccion}`;
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

// Muestra la foto, o un recuadro con el nombre de la categoría si todavía no hay foto
function imagenOMarcador(src, alt, categoria) {
  if (src) {
    return `<img src="${escaparHTML(src)}" alt="${escaparHTML(alt)}" loading="lazy">`;
  }
  const texto = CATEGORIAS[categoria]?.nombre ?? TIPOS_TIENDA[categoria] ?? "Sin foto";
  return `<div class="marcador-imagen" aria-hidden="true">${escaparHTML(texto)}</div>`;
}

function tarjetaTienda(tienda) {
  const cantidad = productosDeTienda(tienda.id).length;
  return `
    <a class="tarjeta" href="tienda.html?id=${encodeURIComponent(tienda.id)}">
      <div class="tarjeta-imagen">${imagenOMarcador(tienda.foto, tienda.nombre, tienda.tipo)}</div>
      <div class="tarjeta-cuerpo">
        <span class="etiqueta">${escaparHTML(TIPOS_TIENDA[tienda.tipo])}</span>
        <h3>${escaparHTML(tienda.nombre)}</h3>
        <p class="texto-suave">${escaparHTML(tienda.direccion)}</p>
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
        ${tienda ? `<a class="texto-suave" href="tienda.html?id=${encodeURIComponent(tienda.id)}">${escaparHTML(tienda.nombre)}</a>` : ""}
        ${botonProbador}
      </div>
    </article>`;
}

/* ---------- Íconos de línea (SVG) ---------- */

// Dibujos simples que reemplazan a los emojis. Toman el color del texto.
const ICONOS = {
  buscar: '<circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/>',
  ubicacion: '<path d="M12 21s-7-6.2-7-11.5a7 7 0 0 1 14 0C19 14.8 12 21 12 21z"/><circle cx="12" cy="9.5" r="2.5"/>',
  tienda: '<path d="M4 9h16l-1.5-5h-13L4 9z"/><path d="M5 9v11h14V9"/><path d="M10 20v-6h4v6"/>',
  chat: '<path d="M21 12a8 8 0 0 1-11.6 7.1L4 20l1-4.6A8 8 0 1 1 21 12z"/>',
  gancho: '<path d="M10 6.5a2 2 0 1 1 2 2v1.5"/><path d="M12 10 3.6 15.6A1.3 1.3 0 0 0 4.3 18h15.4a1.3 1.3 0 0 0 .7-2.4L12 10z"/>',
  labial: '<path d="M9 21h6v-7H9z"/><path d="M10 14V8.5L14 5v9"/>',
  frasco: '<path d="M10 3h4v3h-4z"/><path d="M9 6h6l1 3v10a2 2 0 0 1-2 2h-4a2 2 0 0 1-2-2V9z"/><path d="M8 13h8"/>',
  peine: '<path d="M4 7h16v4H4z"/><path d="M6 11v7M9 11v7M12 11v7M15 11v7M18 11v7"/>',
  ruta: '<circle cx="6" cy="18" r="2"/><circle cx="18" cy="6" r="2"/><path d="M8 18h6a3 3 0 0 0 0-6h-4a3 3 0 0 1 0-6h6"/>',
  destello: '<path d="M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8z"/><path d="M19 16v4M17 18h4"/>',
  candado: '<rect x="5" y="11" width="14" height="9" rx="2"/><path d="M8 11V8a4 4 0 0 1 8 0v3"/>'
};

function icono(nombre) {
  return `<svg class="icono" viewBox="0 0 24 24" aria-hidden="true">${ICONOS[nombre] ?? ""}</svg>`;
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

const LOGO = `
  <span class="logo-nombre">Style<span>Me</span></span>
  <span class="logo-lema">Bucaramanga</span>`;

function pintarEncabezado() {
  const contenedor = document.getElementById("encabezado");
  if (!contenedor) return;

  const paginaActual = window.location.pathname.split("/").pop() || "index.html";
  const enlaces = ENLACES_MENU.map(
    (e) =>
      `<li><a href="${e.href}" ${e.href === paginaActual ? 'aria-current="page"' : ""}>${e.texto}</a></li>`
  ).join("");

  contenedor.innerHTML = `
    <div class="anuncio">Tiendas locales de moda y belleza · Contacto directo con cada tienda</div>
    <div class="contenedor barra">
      <a class="logo" href="index.html" aria-label="StyleMe, ir al inicio">${LOGO}</a>
      <div class="acciones-encabezado">
        <a class="accion-icono" href="productos.html" aria-label="Buscar productos">${icono("buscar")}</a>
        <a class="accion-icono" href="mapa.html" aria-label="Ver el mapa de tiendas">${icono("ubicacion")}</a>
        <button class="boton-menu" aria-expanded="false" aria-controls="menu-principal">Menú</button>
      </div>
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
      <div class="pie-columnas">
        <div>
          <a class="logo" href="index.html">${LOGO}</a>
          <p>Las tiendas de moda y belleza de Bucaramanga en un solo lugar.</p>
        </div>
        <div>
          <h4>Explorar</h4>
          <ul>
            <li><a href="tiendas.html">Tiendas</a></li>
            <li><a href="productos.html">Productos</a></li>
            <li><a href="mapa.html">Mapa</a></li>
          </ul>
        </div>
        <div>
          <h4>Herramientas</h4>
          <ul>
            <li><a href="salida.html">Salida de compras</a></li>
            <li><a href="probador.html">Probador virtual</a></li>
            <li><a href="recomendador.html">Recomendador</a></li>
          </ul>
        </div>
        <div>
          <h4>¿Tienes una tienda?</h4>
          <p>Si quieres aparecer, corregir tu información o que la retiremos, <a href="acerca.html#contacto">escríbenos</a>.</p>
        </div>
      </div>
      <p class="pie-legal">
        © 2026 StyleMe · Proyecto académico. StyleMe no tiene acuerdos comerciales con las tiendas mostradas.
        La información puede cambiar: confírmala directamente con cada tienda.
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
    if (!t.whatsapp && !t.telefono) agregar("tiendas.js", t.id, "Falta WhatsApp o teléfono");
    if (!t.instagram && !t.web) agregar("tiendas.js", t.id, "Falta Instagram o página web");
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
    "color:#a35d6d;font-weight:bold"
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
