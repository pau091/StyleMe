/*
 * PANEL DE ADMINISTRACIÓN
 * -----------------------
 * Pide la clave de administración (la de STYLEME_ADMIN_CLAVE en el .env del backend), la guarda solo
 * mientras la pestaña esté abierta (sessionStorage) y la manda en la cabecera X-Admin-Key.
 *
 *   · Reseñas pendientes → Publicar / Rechazar (PATCH /api/admin/resenas/{id})
 *   · Indicadores        → GET /api/admin/indicadores (una tabla y un gráfico de barras por indicador)
 *   · Reindexar          → POST /api/admin/indexacion (embeddings de lo nuevo o modificado)
 */

/* ✏️ Qué columna se dibuja como barra en cada indicador (las demás se muestran en la tabla) */
const COLUMNA_GRAFICO = {
  conversion_tiendas: "tasa_conversion",
  calificacion_tiendas: "promedio_corazones",
  calificacion_productos: "promedio_corazones",
  productos_mas_vistos: "vistas",
  categorias: "vistas_productos",
  busquedas_sin_resultado: "veces",
  actividad_por_hora: "eventos",
  actividad_por_dia: "eventos",
  dispositivos: "porcentaje",
  zonas: "visitas",
  uso_lia: "conversaciones",
  productos_recomendados_lia: "recomendaciones",
  calidad_datos: "porcentaje"
};

// Columna que da nombre a cada barra (la primera de texto que exista)
const COLUMNAS_NOMBRE = ["nombre", "busqueda", "medida", "dispositivo", "zona", "tema", "categoria", "hora"];

// "tasa_conversion" → "Tasa conversion"
const titulo = (columna) => columna.charAt(0).toUpperCase() + columna.slice(1).replaceAll("_", " ");

function valorCelda(valor) {
  if (valor === null || valor === undefined) return "—";
  if (typeof valor === "boolean") return valor ? "Sí" : "No";
  if (typeof valor === "number") return valor.toLocaleString("es-CO");
  return escaparHTML(String(valor));
}

function graficoBarras(indicador) {
  const columna = COLUMNA_GRAFICO[indicador.codigo];
  const filas = indicador.filas.filter((f) => typeof f[columna] === "number").slice(0, 12);
  if (!columna || !filas.length) return "";
  const nombre = COLUMNAS_NOMBRE.find((c) => c in filas[0]);
  const maximo = Math.max(...filas.map((f) => f[columna]), 0) || 1;
  return `
    <figure class="grafico" aria-label="${escaparHTML(titulo(columna))}">
      ${filas.map((f) => `
        <div class="grafico-fila">
          <span class="grafico-nombre">${valorCelda(f[nombre])}</span>
          <span class="grafico-barra"><span style="width:${(f[columna] / maximo) * 100}%"></span></span>
          <span class="grafico-valor">${valorCelda(f[columna])}</span>
        </div>`).join("")}
      <figcaption class="texto-suave">${escaparHTML(titulo(columna))}</figcaption>
    </figure>`;
}

function tablaIndicador(indicador) {
  if (!indicador.filas.length) return '<p class="texto-suave">Sin datos todavía.</p>';
  const columnas = Object.keys(indicador.filas[0]);
  return `
    <div class="tabla-desplazable">
      <table>
        <thead><tr>${columnas.map((c) => `<th scope="col">${escaparHTML(titulo(c))}</th>`).join("")}</tr></thead>
        <tbody>${indicador.filas.map((f) => `<tr>${columnas.map((c) => `<td>${valorCelda(f[c])}</td>`).join("")}</tr>`).join("")}</tbody>
      </table>
    </div>`;
}

document.addEventListener("DOMContentLoaded", () => {
  const formulario = document.getElementById("formulario-clave");
  const campo = document.getElementById("campo-clave");
  const estadoClave = document.getElementById("estado-clave");
  const panel = document.getElementById("panel");
  const estado = document.getElementById("estado-panel");

  async function entrar() {
    estadoClave.textContent = "Comprobando…";
    try {
      await Promise.all([cargarPendientes(), cargarIndicadores()]);
      formulario.hidden = true;
      panel.hidden = false;
      estadoClave.textContent = "";
    } catch (error) {
      if (error.estado === 401 || error.estado === 503) StyleMe.claveAdmin.borrar();
      formulario.hidden = false;
      panel.hidden = true;
      estadoClave.textContent = error.estado === 401 ? "La clave no es correcta." : error.message;
    }
  }

  formulario.addEventListener("submit", (evento) => {
    evento.preventDefault();
    StyleMe.claveAdmin.guardar(campo.value.trim());
    campo.value = "";
    entrar();
  });

  document.getElementById("boton-salir").addEventListener("click", () => {
    StyleMe.claveAdmin.borrar();
    panel.hidden = true;
    formulario.hidden = false;
  });

  document.getElementById("boton-reindexar").addEventListener("click", async (evento) => {
    const boton = evento.currentTarget;
    boton.disabled = true;
    estado.textContent = "Reindexando… (puede tardar un minuto si hay muchos cambios)";
    try {
      const r = await StyleMe.pedirAdmin("/api/admin/indexacion", { metodo: "POST" });
      estado.textContent = `Listo: ${r.total} fragmentos (${r.nuevos} nuevos, ${r.cambiados} cambiados, ${r.eliminados} eliminados) en ${(r.duracionMs / 1000).toFixed(1)} s.`;
    } catch (error) {
      estado.textContent = error.message;
    } finally {
      boton.disabled = false;
    }
  });

  /* ---------- Reseñas pendientes ---------- */
  async function cargarPendientes() {
    await StyleMe.datos.catch(() => {}); // para mostrar el nombre de la tienda o el producto
    const pendientes = await StyleMe.pedirAdmin("/api/admin/resenas/pendientes?limite=50");
    const contenedor = document.getElementById("pendientes");
    contenedor.innerHTML = pendientes.length
      ? `<ul class="lista-resenas">${pendientes.map((r) => {
          const sobre = r.tienda ? buscarTienda(r.tienda)?.nombre ?? r.tienda : buscarProducto(r.producto)?.nombre ?? r.producto;
          return `
            <li class="resena" data-id="${r.id}">
              <p><strong>${escaparHTML(r.autor)}</strong> · ${"♥".repeat(r.corazones)} · sobre <em>${escaparHTML(sobre)}</em>
                 <span class="texto-suave">(${new Date(r.fecha).toLocaleString("es-CO")})</span></p>
              ${r.comentario ? `<p>${escaparHTML(r.comentario)}</p>` : '<p class="texto-suave">Sin comentario</p>'}
              <div class="grupo-botones">
                <button class="boton boton-pequeno" type="button" data-moderar="PUBLICADA">Publicar</button>
                <button class="boton boton-secundario boton-pequeno" type="button" data-moderar="RECHAZADA">Rechazar</button>
              </div>
            </li>`;
        }).join("")}</ul>`
      : '<p class="texto-suave">No hay reseñas pendientes.</p>';
  }

  document.getElementById("pendientes").addEventListener("click", async (evento) => {
    const boton = evento.target.closest("button[data-moderar]");
    if (!boton) return;
    const item = boton.closest("[data-id]");
    item.querySelectorAll("button").forEach((b) => (b.disabled = true));
    try {
      await StyleMe.pedirAdmin(`/api/admin/resenas/${item.dataset.id}`, { metodo: "PATCH", cuerpo: { estado: boton.dataset.moderar } });
      item.remove();
      if (!document.querySelector("#pendientes [data-id]")) document.getElementById("pendientes").innerHTML = '<p class="texto-suave">No hay reseñas pendientes.</p>';
    } catch (error) {
      estado.textContent = error.message;
      item.querySelectorAll("button").forEach((b) => (b.disabled = false));
    }
  });

  /* ---------- Indicadores ---------- */
  async function cargarIndicadores() {
    const indicadores = await StyleMe.pedirAdmin("/api/admin/indicadores");
    document.getElementById("indice-indicadores").innerHTML = indicadores
      .map((i) => `<a href="#indicador-${i.codigo}">${escaparHTML(i.descripcion)}</a>`).join("");
    document.getElementById("indicadores").innerHTML = indicadores.map((i) => `
      <article class="indicador" id="indicador-${i.codigo}">
        <h3>${escaparHTML(i.descripcion)} <code>${i.codigo}</code></h3>
        ${graficoBarras(i)}
        ${tablaIndicador(i)}
      </article>`).join("");
  }

  // Si la clave ya estaba guardada en esta pestaña, se entra directo
  if (StyleMe.claveAdmin.leer()) entrar();
});
