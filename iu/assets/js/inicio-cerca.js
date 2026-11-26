/*
 * ABIERTAS AHORA Y CERCA DE TI (idea 3)
 * -------------------------------------
 * - Con el horario de cada tienda (estadoHorario, en comun.js) se ve cuáles están abiertas ahora.
 * - Un mini mapa (Leaflet + OpenStreetMap) con las tiendas: verde si está abierta.
 *   Leaflet se descarga solo cuando la sección se acerca a la pantalla (la portada abre más rápido).
 * - "Ver las más cercanas a mí": pide la ubicación (solo con permiso), mide la distancia a cada
 *   tienda y las ordena. La ubicación NO se envía ni se guarda.
 */
(function () {
  const LEAFLET = "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/";
  const MAXIMO_LISTA = 5; // ✏️ tiendas que se muestran en la lista

  const seccion = document.getElementById("seccion-cerca");
  const contenedorMapa = document.getElementById("mini-mapa");
  const lista = document.getElementById("lista-cerca");
  const resumen = document.getElementById("resumen-abiertas");
  const botonUbicacion = document.getElementById("usar-ubicacion");
  const estadoUbicacion = document.getElementById("estado-ubicacion");

  let ubicacion = null; // { lat, lng } solo en memoria
  let mapa = null;
  let marcadorPersona = null;

  botonUbicacion.insertAdjacentHTML("afterbegin", icono("ubicacion"));
  if (!navigator.geolocation) botonUbicacion.hidden = true;

  /* ---------- Lista ---------- */
  function distanciaKm(a, b) {
    const rad = (g) => (g * Math.PI) / 180;
    const dLat = rad(b.lat - a.lat), dLng = rad(b.lng - a.lng);
    const h = Math.sin(dLat / 2) ** 2 + Math.cos(rad(a.lat)) * Math.cos(rad(b.lat)) * Math.sin(dLng / 2) ** 2;
    return 6371 * 2 * Math.asin(Math.sqrt(h));
  }

  function formatoDistancia(km) {
    return km < 1 ? `a ${Math.round(km * 1000 / 10) * 10} m` : `a ${km.toLocaleString("es-CO", { maximumFractionDigits: 1 })} km`;
  }

  // Abiertas primero; si hay ubicación, de la más cercana a la más lejana
  function ordenadas() {
    return TIENDAS.map((t) => ({
      tienda: t,
      estado: estadoHorario(t.horario),
      km: ubicacion && tieneUbicacion(t) ? distanciaKm(ubicacion, t) : null
    })).sort((a, b) => {
      if (ubicacion && a.km !== null && b.km !== null) return a.km - b.km;
      return (b.estado?.abierta ? 1 : 0) - (a.estado?.abierta ? 1 : 0);
    });
  }

  function pintarLista() {
    const filas = ordenadas();
    const abiertas = filas.filter((f) => f.estado?.abierta).length;
    resumen.innerHTML = abiertas
      ? `<strong>${abiertas} de ${TIENDAS.length} tiendas</strong> están abiertas en este momento.`
      : "En este momento todas las tiendas están cerradas. Mira a qué hora abren:";
    lista.innerHTML = filas.slice(0, MAXIMO_LISTA).map(({ tienda, estado, km }) => `
      <li>
        <a class="fila-cerca" href="tienda.html?id=${encodeURIComponent(tienda.id)}">
          <span class="fila-cerca-foto">${imagenOMarcador(tienda.foto, "", tienda.tipo)}</span>
          <span class="fila-cerca-texto">
            <strong>${escaparHTML(tienda.nombre)}</strong>
            ${estado ? `<span class="estado-horario ${estado.abierta ? "abierta" : "cerrada"}">${escaparHTML(estado.texto)}</span>` : '<span class="texto-suave">Horario por confirmar</span>'}
          </span>
          ${km !== null ? `<span class="fila-cerca-distancia">${formatoDistancia(km)}</span>` : ""}
        </a>
      </li>`).join("");
  }

  /* ---------- Mini mapa ---------- */
  function cargarLeaflet() {
    if (window.L) return Promise.resolve();
    const hoja = document.createElement("link");
    hoja.rel = "stylesheet";
    hoja.href = LEAFLET + "leaflet.min.css";
    document.head.append(hoja);
    return new Promise((listo, error) => {
      const script = document.createElement("script");
      script.src = LEAFLET + "leaflet.min.js";
      script.onload = listo;
      script.onerror = error;
      document.head.append(script);
    });
  }

  function pin(abierta) {
    return L.divIcon({
      className: "",
      html: `<span class="pin-mini ${abierta ? "abierta" : ""}" aria-hidden="true"></span>`,
      iconSize: [18, 18],
      iconAnchor: [9, 9]
    });
  }

  async function pintarMapa() {
    try {
      await cargarLeaflet();
    } catch (e) {
      contenedorMapa.innerHTML = '<p class="texto-suave mini-mapa-error">No se pudo cargar el mapa. <a href="mapa.html">Abrir el mapa</a></p>';
      return;
    }
    const conUbicacion = TIENDAS.filter(tieneUbicacion);
    mapa = crearMapa("mini-mapa", CENTRO_BUCARAMANGA, 13);
    mapa.scrollWheelZoom.disable(); // que la rueda del mouse siga bajando la página
    conUbicacion.forEach((t) => {
      L.marker([t.lat, t.lng], { icon: pin(estadoHorario(t.horario)?.abierta), title: t.nombre, alt: t.nombre })
        .addTo(mapa)
        .bindPopup(popupTienda(t));
    });
    if (conUbicacion.length) mapa.fitBounds(conUbicacion.map((t) => [t.lat, t.lng]), { padding: [24, 24] });
  }

  /* ---------- Ubicación ---------- */
  botonUbicacion.addEventListener("click", () => {
    botonUbicacion.disabled = true;
    estadoUbicacion.textContent = "Buscando tu ubicación…";
    navigator.geolocation.getCurrentPosition(
      (posicion) => {
        ubicacion = { lat: posicion.coords.latitude, lng: posicion.coords.longitude };
        estadoUbicacion.textContent = "Ordenadas de la más cercana a la más lejana. Tu ubicación no se envía ni se guarda.";
        botonUbicacion.disabled = false;
        pintarLista();
        if (mapa) {
          marcadorPersona?.remove();
          marcadorPersona = L.circleMarker([ubicacion.lat, ubicacion.lng], { radius: 8, color: "#4f2c3a", fillColor: "#a35d6d", fillOpacity: 0.9 })
            .addTo(mapa).bindTooltip("Estás aquí");
          const cercanas = ordenadas().filter((f) => f.km !== null).slice(0, 3).map((f) => [f.tienda.lat, f.tienda.lng]);
          mapa.fitBounds([[ubicacion.lat, ubicacion.lng], ...cercanas], { padding: [24, 24], maxZoom: 15 });
        }
      },
      (error) => {
        botonUbicacion.disabled = false;
        estadoUbicacion.textContent = error.code === error.PERMISSION_DENIED
          ? "No diste permiso para usar tu ubicación. Puedes ver todas las tiendas en el mapa."
          : "No pudimos saber dónde estás. Inténtalo de nuevo o mira el mapa completo.";
      },
      { enableHighAccuracy: false, timeout: 10000, maximumAge: 300000 }
    );
  });

  /* ---------- Arranque ---------- */
  cuandoHayaDatos(() => {
    pintarLista();
    // Cada minuto se recalcula (una tienda puede abrir o cerrar mientras la página está abierta)
    setInterval(pintarLista, 60_000);
    // El mapa se carga cuando la sección está por aparecer
    const vigia = new IntersectionObserver((entradas) => {
      if (!entradas.some((e) => e.isIntersecting)) return;
      vigia.disconnect();
      pintarMapa();
    }, { rootMargin: "300px 0px" });
    vigia.observe(seccion);
  });
})();
