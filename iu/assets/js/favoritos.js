/* MIS FAVORITOS: tiendas y productos guardados por esta sesión (GET /api/favoritos) */

cuandoHayaDatos(() => {
  const listaTiendas = document.getElementById("favoritos-tiendas");
  const listaProductos = document.getElementById("favoritos-productos");

  function pintar() {
    // Se usa el catálogo para mostrar cada favorito con su tarjeta completa
    const tiendas = TIENDAS.filter((t) => StyleMe.esFavorito("TIENDA", t.id));
    const productos = PRODUCTOS.filter((p) => StyleMe.esFavorito("PRODUCTO", p.id));

    listaTiendas.innerHTML = tiendas.length
      ? tiendas.map(tarjetaTienda).join("")
      : '<p class="vacio">Aún no tienes tiendas favoritas. <a href="tiendas.html">Ver tiendas</a></p>';
    listaProductos.innerHTML = productos.length
      ? productos.map(tarjetaProducto).join("")
      : '<p class="vacio">Aún no tienes productos favoritos. <a href="productos.html">Ver productos</a></p>';
  }

  // Si se quita un favorito desde esta página, la tarjeta desaparece
  document.addEventListener("styleme:favoritos", () => pintarConTransicion(listaProductos, pintar));
  pintar();
});
