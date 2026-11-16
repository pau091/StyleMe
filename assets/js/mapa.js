/* MAPA DE TIENDAS con filtro por tipo */

document.addEventListener("DOMContentLoaded", () => {
  const mapa = crearMapa("mapa");
  const capaMarcadores = L.layerGroup().addTo(mapa);
  const campoTipo = document.getElementById("filtro-tipo");
  const avisoSinUbicacion = document.getElementById("sin-ubicacion");

  function mostrar() {
    capaMarcadores.clearLayers();
    const tipo = campoTipo.value;
    const tiendas = TIENDAS.filter((t) => !tipo || t.tipo === tipo);
    const conUbicacion = tiendas.filter(tieneUbicacion);

    conUbicacion.forEach((t) => {
      L.marker([t.lat, t.lng]).addTo(capaMarcadores).bindPopup(popupTienda(t));
    });

    // Ajusta el zoom para que se vean todas las tiendas
    if (conUbicacion.length) {
      mapa.fitBounds(conUbicacion.map((t) => [t.lat, t.lng]), { padding: [40, 40], maxZoom: 16 });
    }

    const faltan = tiendas.length - conUbicacion.length;
    avisoSinUbicacion.textContent = faltan
      ? `${faltan} tienda${faltan === 1 ? "" : "s"} aún sin ubicación en el mapa.`
      : "";
  }

  campoTipo.addEventListener("change", mostrar);
  mostrar();
});
