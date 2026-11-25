/* PERFIL DE TIENDA: datos, botones de contacto, favorito, mini mapa, productos y reseñas */

// El catálogo llega del backend (api.js). Si ya está guardado en la pestaña, se pinta casi al instante,
// así la foto alcanza a "viajar" desde la tarjeta (tienda.html espera a #perfil-listo antes de mostrarse).
cuandoHayaDatos(function pintarPerfil() {
  const contenedor = document.getElementById("perfil");
  const tienda = buscarTienda(parametroURL("id"));

  if (!tienda) {
    contenedor.innerHTML = `
      <p class="vacio" id="perfil-listo">No encontramos esta tienda.</p>
      <p style="text-align:center"><a class="boton" href="tiendas.html">Ver todas las tiendas</a></p>`;
    return;
  }

  document.title = `${tienda.nombre} · StyleMe`;
  StyleMe.registrarEvento("VER_TIENDA", { tienda: tienda.id });

  const mensaje = `Hola, los encontré en StyleMe. Quisiera más información sobre su tienda.`;
  const whatsapp = enlaceWhatsapp(tienda.whatsapp, mensaje);
  const instagram = enlaceInstagram(tienda.instagram);
  const web = enlaceWeb(tienda.web);
  const productos = productosDeTienda(tienda.id);

  contenedor.innerHTML = `
    <div class="perfil-cabecera" id="perfil-listo">
      <div class="perfil-foto" data-tienda="${escaparHTML(tienda.id)}" style="view-transition-name: foto-viajera">${imagenOMarcador(tienda.foto, tienda.nombre, tienda.tipo, true)}</div>
      <div data-cascada>
        <span class="etiqueta">${escaparHTML(TIPOS_TIENDA[tienda.tipo])}</span>
        <div class="tarjeta-titulo">
          <h1>${escaparHTML(tienda.nombre)}</h1>
          ${StyleMe.botonFavorito("TIENDA", tienda.id)}
        </div>
        <p class="calificacion" id="calificacion" aria-live="polite"></p>
        <p>${escaparHTML(tienda.descripcion)}</p>
        <ul class="datos">
          <li><strong>Dirección:</strong> ${escaparHTML(tienda.direccion)}, Bucaramanga</li>
          <li><strong>Horario:</strong> ${escaparHTML(tienda.horario)}</li>
        </ul>
        <div class="grupo-botones">
          ${whatsapp ? `<a class="boton boton-whatsapp" href="${whatsapp}" target="_blank" rel="noopener" ${eventoTienda(tienda)}>Escribir por WhatsApp</a>` : ""}
          ${!whatsapp && tienda.telefono ? `<a class="boton boton-whatsapp" href="tel:+57${escaparHTML(tienda.telefono)}">Llamar</a>` : ""}
          <a class="boton" href="${enlaceComoLlegar(tienda)}" target="_blank" rel="noopener" ${eventoTienda(tienda)}>Cómo llegar</a>
          ${instagram ? `<a class="boton boton-secundario" href="${instagram}" target="_blank" rel="noopener">Instagram</a>` : ""}
          ${web ? `<a class="boton boton-secundario" href="${escaparHTML(web)}" target="_blank" rel="noopener">Página web</a>` : ""}
        </div>
        ${tienda.verificado ? "" : '<p class="aviso">Información tomada de fuentes públicas. Confírmala con la tienda antes de ir.</p>'}
      </div>
    </div>

    ${tieneUbicacion(tienda) ? '<section data-revelar="fundido"><h2>Ubicación</h2><div id="mini-mapa" class="mapa mapa-pequeno"></div></section>' : ""}

    <section>
      <h2 data-revelar>Productos</h2>
      ${
        productos.length
          ? `<div class="cuadricula" data-cascada>${productos.map(tarjetaProducto).join("")}</div>`
          : '<p class="vacio">Esta tienda aún no tiene productos publicados.</p>'
      }
    </section>

    <section id="resenas" aria-labelledby="titulo-resenas">
      <h2 id="titulo-resenas" data-revelar>Reseñas</h2>
      <div id="lista-resenas"><p class="texto-suave">Cargando reseñas…</p></div>
      ${formularioResena()}
    </section>`;

  if (tieneUbicacion(tienda)) {
    const mapa = crearMapa("mini-mapa", [tienda.lat, tienda.lng], 16);
    const marcador = L.marker([tienda.lat, tienda.lng]).addTo(mapa).bindPopup(escaparHTML(tienda.nombre));
    // El pin "cae" cuando el mapa aparece en pantalla (animación en movimiento.css)
    marcador.getElement()?.classList.add("pin-cae");
  }

  cargarResenas(tienda);
  activarFormulario(tienda);
});

/* ---------- Reseñas ---------- */

// ♥♥♥♡♡ para 3 de 5 (el texto completo queda para los lectores de pantalla)
function corazones(cantidad) {
  const llenos = Math.round(cantidad);
  return `<span aria-hidden="true">${"♥".repeat(llenos)}${"♡".repeat(5 - llenos)}</span>`;
}

function fechaCorta(texto) {
  return new Date(texto).toLocaleDateString("es-CO", { day: "numeric", month: "short", year: "numeric" });
}

async function cargarResenas(tienda) {
  const lista = document.getElementById("lista-resenas");
  const calificacion = document.getElementById("calificacion");
  try {
    const datos = await StyleMe.resenas("TIENDA", tienda.id);
    calificacion.innerHTML = datos.cantidad
      ? `${corazones(datos.promedio)} <strong>${datos.promedio.toLocaleString("es-CO")}</strong>
         <span class="texto-suave">(${datos.cantidad} reseña${datos.cantidad === 1 ? "" : "s"})</span>
         <span class="solo-lectores">${datos.promedio} de 5 corazones</span>`
      : '<span class="texto-suave">Aún sin reseñas</span>';
    lista.innerHTML = datos.resenas.length
      ? `<ul class="lista-resenas" data-cascada>${datos.resenas.map((r) => `
          <li class="resena">
            <p>${corazones(r.corazones)} <span class="solo-lectores">${r.corazones} de 5</span>
               <strong>${escaparHTML(r.autor)}</strong> · <span class="texto-suave">${fechaCorta(r.fecha)}</span></p>
            ${r.comentario ? `<p>${escaparHTML(r.comentario)}</p>` : ""}
          </li>`).join("")}</ul>`
      : '<p class="texto-suave">Sé la primera persona en dejar una reseña.</p>';
  } catch (error) {
    calificacion.textContent = "";
    lista.innerHTML = `<p class="texto-suave">${escaparHTML(error.message)}</p>`;
  }
}

function formularioResena() {
  const opciones = [5, 4, 3, 2, 1]
    .map((n) => `<label class="opcion-corazon"><input type="radio" name="corazones" value="${n}" required> ${"♥".repeat(n)} <span class="solo-lectores">${n} de 5</span></label>`)
    .join("");
  return `
    <form id="formulario-resena" class="formulario-resena" novalidate>
      <h3>Deja tu reseña</h3>
      <fieldset>
        <legend>¿Cuántos corazones le das?</legend>
        <div class="opciones-corazones">${opciones}</div>
      </fieldset>
      <label for="resena-autor">Tu nombre (opcional)</label>
      <input id="resena-autor" name="autor" maxlength="80" autocomplete="given-name" placeholder="Anónimo">
      <label for="resena-comentario">Comentario (opcional)</label>
      <textarea id="resena-comentario" name="comentario" maxlength="500" rows="3"></textarea>
      <button class="boton" type="submit">Enviar reseña</button>
      <p class="texto-suave" id="estado-resena" role="status"></p>
    </form>`;
}

function activarFormulario(tienda) {
  const formulario = document.getElementById("formulario-resena");
  const estado = document.getElementById("estado-resena");
  formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const datos = new FormData(formulario);
    if (!datos.get("corazones")) {
      estado.textContent = "Elige cuántos corazones le das.";
      return;
    }
    const boton = formulario.querySelector("button");
    boton.disabled = true;
    estado.textContent = "Enviando…";
    try {
      await StyleMe.escribirResena("TIENDA", tienda.id, {
        autor: datos.get("autor").trim() || null,
        corazones: Number(datos.get("corazones")),
        comentario: datos.get("comentario").trim() || null
      });
      formulario.reset();
      estado.textContent = "¡Gracias! Tu reseña se publicará cuando la revisemos.";
    } catch (error) {
      // 409: esta sesión ya reseñó la tienda · 429: demasiadas seguidas · 400: datos inválidos
      estado.textContent = error.message;
    } finally {
      boton.disabled = false;
    }
  });
}
