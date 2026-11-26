/*
 * PROBADOR CON IA (P-017)
 * -----------------------
 * La persona sube una foto de cuerpo completo; el backend le pide a un modelo de imágenes (OpenAI o Gemini)
 * que la muestre DENTRO DE LA TIENDA con la prenda puesta, en 4 poses. Las poses se piden una por una
 * y se muestran a medida que llegan.
 *
 * Si el backend no tiene el probador con IA activado (o el servicio falla), esta sección se oculta y
 * queda el probador rápido de siempre (probador.js), donde la foto no sale del navegador.
 *
 * Privacidad: antes de enviarla, la foto se achica y se vuelve a dibujar en un lienzo. Eso borra sus datos
 * ocultos (EXIF: ubicación GPS, modelo del celular...). El backend no la guarda.
 */
(function () {
  const seccion = document.getElementById("probador-ia");
  const tituloRapido = document.getElementById("titulo-probador-rapido");
  const formulario = document.getElementById("formulario-ia");
  const selector = document.getElementById("prenda-ia");
  const consentimiento = document.getElementById("consentimiento-ia");
  const campoFoto = document.getElementById("foto-ia");
  const boton = document.getElementById("boton-ia");
  const botonBorrar = document.getElementById("borrar-ia");
  const estado = document.getElementById("estado-ia");
  const contenedorPoses = document.getElementById("poses-ia");
  const infoPrenda = document.getElementById("info-prenda-ia");

  const LADO_MAXIMO = 1536; // px: suficiente para el modelo y la foto sube rápido
  let poses = [];
  let urlsGeneradas = [];
  let enCurso = false;

  // Muestra el probador con IA solo si el backend lo tiene activado
  async function iniciar() {
    let datos;
    try {
      datos = await StyleMe.pedir("/api/probador/estado");
    } catch (e) {
      return; // backend apagado: solo el probador rápido
    }
    if (!datos.disponible) return;
    await StyleMe.datos.catch(() => {});

    const prendas = PRODUCTOS.filter((p) => p.categoria === "ropa" && /^https:\/\//.test(p.foto ?? ""));
    if (!prendas.length) return;

    poses = datos.poses;
    selector.innerHTML = prendas.map((p) => {
      const tienda = buscarTienda(p.tiendaId);
      return `<option value="${escaparHTML(p.id)}">${escaparHTML(p.nombre)} · ${escaparHTML(tienda?.nombre)}</option>`;
    }).join("");
    const pedida = parametroURL("producto");
    if (pedida && prendas.some((p) => p.id === pedida)) selector.value = pedida;

    document.getElementById("texto-consentimiento-ia").textContent =
      `2. Soy mayor de 18 años, la foto es mía (o tengo permiso) y acepto que se envíe a ${datos.proveedor} para crear las imágenes.`;
    seccion.hidden = false;
    tituloRapido.hidden = false;
    pintarPrenda();
  }

  function pintarPrenda() {
    const producto = buscarProducto(selector.value);
    const tienda = producto && buscarTienda(producto.tiendaId);
    if (!producto || !tienda) return;
    const whatsapp = enlaceWhatsapp(tienda.whatsapp, `Hola, vi en StyleMe la prenda "${producto.nombre}". ¿La tienen disponible?`);
    infoPrenda.innerHTML = `
      <div class="prenda-elegida">
        ${imagenOMarcador(producto.foto, producto.nombre, "ropa")}
        <div>
          <strong>${escaparHTML(producto.nombre)}</strong>
          <p class="precio">${formatoPrecio(producto.precio)}</p>
          <p class="texto-suave">${escaparHTML(tienda.nombre)} · ${escaparHTML(tienda.direccion)}</p>
          <div class="grupo-botones">
            ${whatsapp ? `<a class="boton boton-whatsapp boton-pequeno" href="${whatsapp}" target="_blank" rel="noopener" ${eventoTienda(tienda)}>Preguntar por WhatsApp</a>` : ""}
            <a class="boton boton-secundario boton-pequeno" href="tienda.html?id=${encodeURIComponent(tienda.id)}">Ver tienda</a>
          </div>
        </div>
      </div>`;
  }

  function actualizarBotones() {
    campoFoto.disabled = !consentimiento.checked || enCurso;
    boton.disabled = !consentimiento.checked || !campoFoto.files[0] || enCurso;
  }

  // Achica la foto y la vuelve JPG (sin datos ocultos)
  function prepararFoto(archivo) {
    return new Promise((listo, error) => {
      const url = URL.createObjectURL(archivo);
      const imagen = new Image();
      imagen.onload = () => {
        const factor = Math.min(1, LADO_MAXIMO / Math.max(imagen.naturalWidth, imagen.naturalHeight));
        const lienzo = document.createElement("canvas");
        lienzo.width = Math.round(imagen.naturalWidth * factor);
        lienzo.height = Math.round(imagen.naturalHeight * factor);
        lienzo.getContext("2d").drawImage(imagen, 0, 0, lienzo.width, lienzo.height);
        URL.revokeObjectURL(url);
        lienzo.toBlob((blob) => (blob ? listo(blob) : error(new Error("foto"))), "image/jpeg", 0.9);
      };
      imagen.onerror = () => {
        URL.revokeObjectURL(url);
        error(new Error("No pudimos abrir esa imagen. Prueba con una foto JPG o PNG."));
      };
      imagen.src = url;
    });
  }

  // Foto de la tienda (si hay) para que la IA use su ambiente de fondo
  async function fotoDeLaTienda(producto) {
    const tienda = buscarTienda(producto.tiendaId);
    if (!tienda?.foto || !/\.(jpe?g|png|webp)$/i.test(tienda.foto)) return null;
    try {
      const respuesta = await fetch(tienda.foto);
      return respuesta.ok ? await respuesta.blob() : null;
    } catch (e) {
      return null;
    }
  }

  async function generarPose(foto, fondo, producto, pose) {
    const datos = new FormData();
    datos.append("foto", foto, "persona.jpg");
    datos.append("producto", producto.id);
    datos.append("pose", pose.codigo);
    if (fondo) datos.append("fondo", fondo, "tienda." + (fondo.type.split("/")[1] || "jpg"));
    const respuesta = await fetch(StyleMe.api + "/api/probador", { method: "POST", body: datos });
    if (!respuesta.ok) {
      const error = await respuesta.json().catch(() => ({}));
      throw Object.assign(new Error(error.mensaje ?? "No se pudo crear la imagen"), { estado: respuesta.status });
    }
    return URL.createObjectURL(await respuesta.blob());
  }

  function borrarResultados() {
    urlsGeneradas.forEach((u) => URL.revokeObjectURL(u));
    urlsGeneradas = [];
    contenedorPoses.innerHTML = "";
    botonBorrar.hidden = true;
  }

  // Si el servicio de IA no está disponible, se oculta esta sección y queda el probador rápido
  function pasarAlProbadorRapido(mensaje) {
    seccion.hidden = true;
    tituloRapido.hidden = false;
    tituloRapido.insertAdjacentHTML("afterend", `<p class="aviso" role="alert">${escaparHTML(mensaje)}</p>`);
    tituloRapido.scrollIntoView({ behavior: "smooth", block: "start" });
  }

  formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const producto = buscarProducto(selector.value);
    if (!producto || !campoFoto.files[0] || !consentimiento.checked) return;

    enCurso = true;
    actualizarBotones();
    borrarResultados();
    estado.textContent = "Preparando tu foto…";
    StyleMe.registrarEvento("USAR_PROBADOR", { producto: producto.id, detalle: { modo: "ia" } });

    // Un recuadro por pose: se llena cuando llega su imagen
    contenedorPoses.innerHTML = poses.map((p) => `
      <figure class="pose-ia" data-pose="${p.codigo}">
        <div class="pose-ia-imagen cargando" aria-hidden="true"></div>
        <figcaption>${escaparHTML(p.nombre)}</figcaption>
      </figure>`).join("");

    try {
      const [foto, fondo] = await Promise.all([prepararFoto(campoFoto.files[0]), fotoDeLaTienda(producto)]);
      let creadas = 0;
      for (const [i, pose] of poses.entries()) {
        estado.textContent = `Creando la pose ${i + 1} de ${poses.length} (${pose.nombre})… puede tardar unos segundos.`;
        const figura = contenedorPoses.querySelector(`[data-pose="${pose.codigo}"] .pose-ia-imagen`);
        try {
          const url = await generarPose(foto, fondo, producto, pose);
          urlsGeneradas.push(url);
          figura.classList.remove("cargando");
          figura.innerHTML = `<img src="${url}" alt="Tú con ${escaparHTML(producto.nombre)}, ${escaparHTML(pose.nombre)}">`;
          creadas++;
        } catch (error) {
          if (error.estado === 503 && creadas === 0) {
            borrarResultados();
            pasarAlProbadorRapido("El probador con IA no está disponible en este momento. Usa el probador rápido.");
            return;
          }
          figura.classList.remove("cargando");
          figura.innerHTML = `<p class="texto-suave">${escaparHTML(error.message)}</p>`;
          if (error.estado === 429) break; // demasiadas pruebas: no seguir pidiendo
        }
      }
      estado.textContent = creadas
        ? `Listo: ${creadas} de ${poses.length} poses. Las imágenes solo están en tu navegador.`
        : "No se pudo crear ninguna imagen. Prueba con otra foto.";
      botonBorrar.hidden = !creadas;
    } catch (error) {
      estado.textContent = error.message;
    } finally {
      enCurso = false;
      actualizarBotones();
    }
  });

  selector.addEventListener("change", () => {
    borrarResultados();
    pintarPrenda();
  });
  consentimiento.addEventListener("change", () => {
    if (!consentimiento.checked) {
      campoFoto.value = "";
      borrarResultados();
    }
    actualizarBotones();
  });
  campoFoto.addEventListener("change", actualizarBotones);
  botonBorrar.addEventListener("click", () => {
    borrarResultados();
    campoFoto.value = "";
    actualizarBotones();
    estado.textContent = "Tus imágenes fueron borradas.";
  });

  cuandoHayaDatos(iniciar);
})();
