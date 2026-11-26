/*
 * LO MÁS BUSCADO Y RESEÑAS RECIENTES (ideas 4 y 5)
 * ------------------------------------------------
 * - GET /api/tendencias: lo que más buscan, las tiendas y los productos más vistos de la semana.
 *   Son totales anónimos (cuántas personas, nunca quién). Una búsqueda solo aparece si la hicieron
 *   3 personas o más. Si la semana no tiene datos, se muestra el último mes; si tampoco, la sección no sale.
 * - GET /api/resenas/recientes: las últimas reseñas publicadas. Todas pasaron por la moderación
 *   con IA (n8n) antes de publicarse.
 */
(function () {
  const seccionTendencias = document.getElementById("seccion-tendencias");
  const seccionResenas = document.getElementById("seccion-resenas");

  /* ---------- Lo más buscado ---------- */
  async function pintarTendencias() {
    let datos = null;
    try {
      datos = await StyleMe.pedir("/api/tendencias?dias=7");
      if (vacia(datos)) datos = await StyleMe.pedir("/api/tendencias?dias=30");
    } catch (e) {
      return; // sin backend no hay tendencias: la sección sigue oculta
    }
    if (vacia(datos)) return;

    const periodo = datos.dias === 7 ? "esta semana" : "este mes";
    document.getElementById("titulo-tendencias").textContent = `Lo más buscado ${periodo}`;

    // Búsquedas: cada una lleva al catálogo con esa búsqueda
    document.getElementById("busquedas-populares").innerHTML = datos.busquedas.map((b, i) => `
      <li><a class="busqueda-popular" href="productos.html?q=${encodeURIComponent(b.referencia)}">
        <span class="puesto">${i + 1}</span>
        <span class="busqueda-texto">${escaparHTML(b.referencia)}</span>
        <span class="texto-suave">${b.personas} personas</span>
      </a></li>`).join("");

    // Tiendas: barra proporcional a la más visitada
    const maximo = Math.max(...datos.tiendas.map((t) => t.personas), 1);
    document.getElementById("tiendas-populares").innerHTML = datos.tiendas
      .map((t) => ({ ...t, tienda: buscarTienda(t.referencia) }))
      .filter((t) => t.tienda)
      .map((t) => `
        <li><a class="tienda-popular" href="tienda.html?id=${encodeURIComponent(t.tienda.id)}">
          <span class="tienda-popular-nombre">${escaparHTML(t.tienda.nombre)}</span>
          <span class="barra-visitas" aria-hidden="true"><span style="--valor:${(t.personas / maximo).toFixed(3)}"></span></span>
          <span class="texto-suave">${t.personas} visitas</span>
        </a></li>`).join("");

    // Productos más vistos, en carrusel
    const productos = datos.productos.map((p) => ({ ...p, producto: buscarProducto(p.referencia) })).filter((p) => p.producto);
    const carrusel = document.getElementById("productos-populares");
    carrusel.innerHTML = productos.map(({ producto, personas }) => {
      const tienda = buscarTienda(producto.tiendaId);
      return `
        <a class="mini-producto" href="${tienda ? `tienda.html?id=${encodeURIComponent(tienda.id)}` : "productos.html"}">
          <span class="mini-producto-foto">${imagenOMarcador(producto.foto, producto.nombre, producto.categoria)}</span>
          <strong>${escaparHTML(producto.nombre)}</strong>
          <span class="texto-suave">${escaparHTML(tienda?.nombre ?? "")}</span>
          <span class="precio">${formatoPrecio(producto.precio)}</span>
          <span class="mini-producto-vistas">${personas} personas lo vieron</span>
        </a>`;
    }).join("");
    carrusel.closest(".tendencias-productos").hidden = !productos.length;

    document.getElementById("nota-tendencias").textContent =
      `Calculado con la actividad anónima de StyleMe en los últimos ${datos.dias} días: se cuentan personas, nunca quién.`;
    seccionTendencias.hidden = false;
    activarCarrusel(carrusel);
  }

  function vacia(datos) {
    return !datos || (!datos.busquedas.length && !datos.tiendas.length && !datos.productos.length);
  }

  /* ---------- Reseñas recientes ---------- */
  async function pintarResenas() {
    let resenas;
    try {
      resenas = await StyleMe.pedir("/api/resenas/recientes?limite=8");
    } catch (e) {
      return;
    }
    const tarjetas = resenas.map(tarjetaResena).filter(Boolean);
    if (!tarjetas.length) return;
    const lista = document.getElementById("lista-resenas");
    lista.innerHTML = tarjetas.join("");
    seccionResenas.hidden = false;
    activarCarrusel(lista);
  }

  function tarjetaResena(r) {
    // ¿De qué es la reseña? De una tienda o de un producto (que lleva a su tienda)
    const producto = r.producto ? buscarProducto(r.producto) : null;
    const tienda = buscarTienda(r.tienda ?? producto?.tiendaId);
    if (!tienda) return "";
    const sobre = producto ? `${producto.nombre} · ${tienda.nombre}` : tienda.nombre;
    const corazones = "♥".repeat(r.corazones) + "♡".repeat(5 - r.corazones);
    return `
      <article class="tarjeta-resena">
        <p class="corazones" aria-label="${r.corazones} de 5 corazones">${corazones}</p>
        <blockquote>${escaparHTML(r.comentario)}</blockquote>
        <p class="resena-autor"><strong>${escaparHTML(r.autor)}</strong> · ${hace(r.fecha)}</p>
        <a class="resena-sobre" href="tienda.html?id=${encodeURIComponent(tienda.id)}">${escaparHTML(sobre)}</a>
        <span class="sello-moderada">${icono("candado")} Revisada antes de publicarse</span>
      </article>`;
  }

  // "hace 3 días", "hace 2 horas"...
  function hace(fecha) {
    const segundos = (Date.now() - new Date(fecha).getTime()) / 1000;
    const formato = new Intl.RelativeTimeFormat("es", { numeric: "auto" });
    const unidades = [["year", 31536000], ["month", 2592000], ["week", 604800], ["day", 86400], ["hour", 3600], ["minute", 60]];
    for (const [unidad, valor] of unidades) {
      if (Math.abs(segundos) >= valor) return formato.format(-Math.round(segundos / valor), unidad);
    }
    return "hace un momento";
  }

  cuandoHayaDatos(() => {
    pintarTendencias();
    pintarResenas();
  });
})();
