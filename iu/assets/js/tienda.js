/* PERFIL DE TIENDA: datos, botones de contacto, mini mapa y productos */

// Se pinta apenas carga este archivo (no espera a DOMContentLoaded): así la foto ya existe
// cuando el navegador hace la transición desde la tarjeta ("foto que viaja").
// Este script está al final del <body>, por eso #perfil ya existe.
(function pintarPerfil() {
  const contenedor = document.getElementById("perfil");
  const tienda = buscarTienda(parametroURL("id"));

  if (!tienda) {
    contenedor.innerHTML = `
      <p class="vacio">No encontramos esta tienda.</p>
      <p style="text-align:center"><a class="boton" href="tiendas.html">Ver todas las tiendas</a></p>`;
    return;
  }

  document.title = `${tienda.nombre} · StyleMe`;

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
        <h1>${escaparHTML(tienda.nombre)}</h1>
        <p>${escaparHTML(tienda.descripcion)}</p>
        <ul class="datos">
          <li><strong>Dirección:</strong> ${escaparHTML(tienda.direccion)}, Bucaramanga</li>
          <li><strong>Horario:</strong> ${escaparHTML(tienda.horario)}</li>
        </ul>
        <div class="grupo-botones">
          ${whatsapp ? `<a class="boton boton-whatsapp" href="${whatsapp}" target="_blank" rel="noopener">Escribir por WhatsApp</a>` : ""}
          ${!whatsapp && tienda.telefono ? `<a class="boton boton-whatsapp" href="tel:+57${escaparHTML(tienda.telefono)}">Llamar</a>` : ""}
          <a class="boton" href="${enlaceComoLlegar(tienda)}" target="_blank" rel="noopener">Cómo llegar</a>
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
    </section>`;

  if (tieneUbicacion(tienda)) {
    const mapa = crearMapa("mini-mapa", [tienda.lat, tienda.lng], 16);
    const marcador = L.marker([tienda.lat, tienda.lng]).addTo(mapa).bindPopup(escaparHTML(tienda.nombre));
    // El pin "cae" cuando el mapa aparece en pantalla (animación en movimiento.css)
    marcador.getElement()?.classList.add("pin-cae");
  }
})();
