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

    conUbicacion.forEach((t, i) => {
      const marcador = L.marker([t.lat, t.lng]).addTo(capaMarcadores).bindPopup(popupTienda(t));
      // El pin "cae" con un pequeño retraso según su orden (animación en movimiento.css)
      const pin = marcador.getElement();
      if (pin) {
        pin.classList.add("pin-entra");
        pin.style.setProperty("--i", i);
      }
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

  // Al filtrar: los pines actuales se desvanecen y luego caen los nuevos
  function cambiarTipo() {
    const pinesActuales = document.querySelectorAll("#mapa .pin-entra");
    const menosMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    if (menosMovimiento || !pinesActuales.length) return mostrar();
    pinesActuales.forEach((pin) => pin.classList.add("pin-sale"));
    setTimeout(mostrar, 200); // ✏️ lo que tardan en desvanecerse
  }

  campoTipo.addEventListener("change", cambiarTipo);
  mostrar();
});
