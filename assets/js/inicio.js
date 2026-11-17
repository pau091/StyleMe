/* PÁGINA DE INICIO: categorías y tiendas por tipo */

document.addEventListener("DOMContentLoaded", () => {
  // Categorías: cada una lleva al catálogo ya filtrado
  document.getElementById("lista-categorias").innerHTML = Object.entries(CATEGORIAS)
    .map(
      ([clave, cat]) =>
        `<a class="categoria" href="productos.html?categoria=${clave}">${cat.nombre}</a>`
    )
    .join("");

  // Tiendas separadas por tipo
  document.getElementById("tiendas-ropa").innerHTML = TIENDAS.filter((t) => t.tipo === "ropa")
    .map(tarjetaTienda)
    .join("");

  document.getElementById("tiendas-maquillaje").innerHTML = TIENDAS.filter((t) => t.tipo === "maquillaje")
    .map(tarjetaTienda)
    .join("");
});
