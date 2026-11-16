/* LISTA DE TIENDAS con búsqueda por nombre o barrio y filtro por tipo */

document.addEventListener("DOMContentLoaded", () => {
  const campoTexto = document.getElementById("filtro-texto");
  const campoTipo = document.getElementById("filtro-tipo");
  const lista = document.getElementById("lista-tiendas");

  // Permite llegar ya filtrado: tiendas.html?tipo=ropa
  campoTipo.value = parametroURL("tipo") ?? "";

  function mostrar() {
    const texto = campoTexto.value.trim().toLowerCase();
    const tipo = campoTipo.value;

    const resultado = TIENDAS.filter((t) => {
      const coincideTipo = !tipo || t.tipo === tipo;
      const coincideTexto =
        !texto ||
        t.nombre.toLowerCase().includes(texto) ||
        t.direccion.toLowerCase().includes(texto) ||
        t.zona.toLowerCase().includes(texto);
      return coincideTipo && coincideTexto;
    });

    lista.innerHTML = resultado.length
      ? resultado.map(tarjetaTienda).join("")
      : '<p class="vacio">No encontramos tiendas con esos filtros.</p>';
  }

  campoTexto.addEventListener("input", mostrar);
  campoTipo.addEventListener("change", mostrar);
  mostrar();
});
