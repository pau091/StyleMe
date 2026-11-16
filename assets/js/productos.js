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

    contador.textContent = `${resultado.length} producto${resultado.length === 1 ? "" : "s"}`;
    lista.innerHTML = resultado.length
      ? resultado.map(tarjetaProducto).join("")
      : '<p class="vacio">No encontramos productos con esos filtros. Prueba con otra palabra.</p>';
  }

  [campoTexto, campoPrecio].forEach((c) => c.addEventListener("input", mostrar));
  [campoCategoria, campoTienda].forEach((c) => c.addEventListener("change", mostrar));
  mostrar();
});
