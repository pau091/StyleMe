/* CATÁLOGO DE PRODUCTOS con buscador y filtros */

// Quita tildes y pasa a minúsculas, para que "camisón" y "camison" coincidan
function normalizar(texto) {
  return (texto ?? "").normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase();
}

document.addEventListener("DOMContentLoaded", () => {
  const campoTexto = document.getElementById("filtro-texto");
  const campoCategoria = document.getElementById("filtro-categoria");
  const campoPrecio = document.getElementById("filtro-precio");
  const campoTienda = document.getElementById("filtro-tienda");
  const lista = document.getElementById("lista-productos");
  const contador = document.getElementById("contador");

  // Llenar la lista de tiendas
  campoTienda.innerHTML += TIENDAS.map(
    (t) => `<option value="${escaparHTML(t.id)}">${escaparHTML(t.nombre)}</option>`
  ).join("");

  // Valores que vienen en la URL (desde el buscador o las categorías del inicio)
  campoTexto.value = parametroURL("q") ?? "";
  campoCategoria.value = parametroURL("categoria") ?? "";

  let cantidadAnterior = 0;

  function mostrar() {
    const texto = normalizar(campoTexto.value.trim());
    const categoria = campoCategoria.value;
    const precioMaximo = Number(campoPrecio.value) || null;
    const tiendaId = campoTienda.value;

    const resultado = PRODUCTOS.filter((p) => {
      const tienda = buscarTienda(p.tiendaId);
      const coincideTexto =
        !texto ||
        normalizar(p.nombre).includes(texto) ||
        normalizar(tienda?.nombre).includes(texto);
      const coincideCategoria = !categoria || p.categoria === categoria;
      // Si hay precio máximo, se ocultan los productos sin precio
      const coincidePrecio = !precioMaximo || (p.precio !== null && p.precio <= precioMaximo);
      const coincideTienda = !tiendaId || p.tiendaId === tiendaId;
      return coincideTexto && coincideCategoria && coincidePrecio && coincideTienda;
    });

    // El número del contador cuenta desde el valor anterior hasta el nuevo
    const cantidad = resultado.length;
    contador.innerHTML = `<span class="contador-numero">${cantidad}</span> producto${cantidad === 1 ? "" : "s"}`;
    window.StyleMeMovimiento?.animarNumero(contador.firstElementChild, cantidadAnterior, cantidad);
    cantidadAnterior = cantidad;
    lista.innerHTML = resultado.length
      ? resultado.map(tarjetaProducto).join("")
      : '<p class="vacio">No encontramos productos con esos filtros. Prueba con otra palabra.</p>';
  }

  // Al filtrar, las tarjetas se reacomodan con suavidad (pintarConTransicion está en comun.js)
  const mostrarConTransicion = () => pintarConTransicion(lista, mostrar);
  [campoTexto, campoPrecio].forEach((c) => c.addEventListener("input", mostrarConTransicion));
  [campoCategoria, campoTienda].forEach((c) => c.addEventListener("change", mostrarConTransicion));
  mostrar(); // la primera vez entran en cascada y el contador cuenta desde 0
});
