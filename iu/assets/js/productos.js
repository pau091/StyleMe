/*
 * CATÁLOGO DE PRODUCTOS con buscador y filtros
 *
 * El buscador usa la BÚSQUEDA SEMÁNTICA del backend (GET /api/busqueda): entiende lo que la persona
 * quiere decir, no solo las palabras exactas. "algo para que no me brille la cara" encuentra productos
 * para piel grasa aunque no digan "brillo". Si el servicio de IA no responde, busca por palabras.
 */

// Quita tildes y pasa a minúsculas, para que "camisón" y "camison" coincidan
function normalizar(texto) {
  return (texto ?? "").normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase();
}

cuandoHayaDatos(() => {
  const campoTexto = document.getElementById("filtro-texto");
  const campoCategoria = document.getElementById("filtro-categoria");
  const campoPrecio = document.getElementById("filtro-precio");
  const campoTienda = document.getElementById("filtro-tienda");
  const lista = document.getElementById("lista-productos");
  const contador = document.getElementById("contador");
  const modoBusqueda = document.getElementById("modo-busqueda");

  // Llenar la lista de tiendas
  campoTienda.innerHTML += TIENDAS.map(
    (t) => `<option value="${escaparHTML(t.id)}">${escaparHTML(t.nombre)}</option>`
  ).join("");

  // Valores que vienen en la URL (desde el buscador o las categorías del inicio)
  campoTexto.value = parametroURL("q") ?? "";
  campoCategoria.value = parametroURL("categoria") ?? "";

  let cantidadAnterior = 0;
  let consultaActual = 0; // si se escribe rápido, solo cuenta la última búsqueda
  let ultimaBusquedaRegistrada = "";

  // Búsqueda por palabras (la de siempre): nombre, marca o tienda
  function coincidePorPalabras(p, texto) {
    const tienda = buscarTienda(p.tiendaId);
    return normalizar(p.nombre).includes(texto) || normalizar(p.marca).includes(texto) || normalizar(tienda?.nombre).includes(texto);
  }

  // Devuelve los productos a mostrar y cómo se buscaron
  async function calcularResultado() {
    const texto = campoTexto.value.trim();
    const categoria = campoCategoria.value;
    const precioMaximo = Number(campoPrecio.value) || null;
    const tiendaId = campoTienda.value;

    const pasaFiltros = (p) =>
      (!categoria || p.categoria === categoria) &&
      // Si hay precio máximo, se ocultan los productos sin precio
      (!precioMaximo || (p.precio !== null && p.precio <= precioMaximo)) &&
      (!tiendaId || p.tiendaId === tiendaId);

    if (texto.length < 3) {
      const palabras = normalizar(texto);
      return { productos: PRODUCTOS.filter((p) => pasaFiltros(p) && (!palabras || coincidePorPalabras(p, palabras))), modo: "" };
    }

    try {
      const codigos = await StyleMe.buscarProductos(texto, { categoria, precioMaximo });
      const productos = codigos.map(buscarProducto).filter((p) => p && pasaFiltros(p)); // en orden de parecido
      return { productos, modo: "Resultados por significado (búsqueda inteligente), del más parecido al menos parecido." };
    } catch (error) {
      const palabras = normalizar(texto);
      return {
        productos: PRODUCTOS.filter((p) => pasaFiltros(p) && coincidePorPalabras(p, palabras)),
        modo: "La búsqueda inteligente no está disponible ahora: se buscó por palabras."
      };
    }
  }

  function pintar(productos) {
    // El número del contador cuenta desde el valor anterior hasta el nuevo
    const cantidad = productos.length;
    contador.innerHTML = `<span class="contador-numero">${cantidad}</span> producto${cantidad === 1 ? "" : "s"}`;
    window.StyleMeMovimiento?.animarNumero(contador.firstElementChild, cantidadAnterior, cantidad);
    cantidadAnterior = cantidad;
    lista.innerHTML = productos.length
      ? productos.map(tarjetaProducto).join("")
      : '<p class="vacio">No encontramos productos con esos filtros. Prueba con otras palabras.</p>';
  }

  async function mostrar(conTransicion = true) {
    const numero = ++consultaActual;
    const { productos, modo } = await calcularResultado();
    if (numero !== consultaActual) return; // llegó una búsqueda más nueva
    modoBusqueda.textContent = modo;
    if (conTransicion) pintarConTransicion(lista, () => pintar(productos));
    else pintar(productos);

    // Cada texto buscado se cuenta una vez, con cuántos resultados tuvo (indicador "búsquedas sin resultado")
    const texto = campoTexto.value.trim().toLowerCase();
    if (texto.length >= 3 && texto !== ultimaBusquedaRegistrada) {
      ultimaBusquedaRegistrada = texto;
      StyleMe.registrarEvento("BUSQUEDA", { textoBusqueda: texto, cantidadResultados: productos.length, categoria: campoCategoria.value });
    }
  }

  // Al escribir se espera un momento (no se llama a la IA con cada letra)
  let espera = null;
  campoTexto.addEventListener("input", () => {
    clearTimeout(espera);
    espera = setTimeout(mostrar, 450);
  });
  campoPrecio.addEventListener("input", () => mostrar());
  campoTienda.addEventListener("change", () => mostrar());
  campoCategoria.addEventListener("change", () => {
    if (campoCategoria.value) StyleMe.registrarEvento("FILTRAR_CATEGORIA", { categoria: campoCategoria.value });
    mostrar();
  });
  mostrar(false); // la primera vez entran en cascada y el contador cuenta desde 0
});
