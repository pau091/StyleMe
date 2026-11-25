/*
 * SALIDA DE COMPRAS
 * -----------------
 * Arma un recorrido corto por tiendas cercanas entre sí.
 *
 * Cómo funciona (algoritmo del "vecino más cercano"):
 *   1. Se toman las tiendas que coinciden con lo que eligió la persona.
 *   2. Desde el punto de partida se va siempre a la tienda más cercana que falte.
 *   3. Si no hay ubicación de la persona, se prueba empezar en cada tienda
 *      y se elige el recorrido más corto.
 */

const VELOCIDAD_CAMINANDO_KMH = 4.5;
const FACTOR_CALLES = 1.3; // las calles no son línea recta: se suma un 30 %

// Distancia en km entre dos puntos {lat, lng} (fórmula de Haversine)
function distanciaKm(a, b) {
  const R = 6371;
  const rad = (g) => (g * Math.PI) / 180;
  const dLat = rad(b.lat - a.lat);
  const dLng = rad(b.lng - a.lng);
  const h = Math.sin(dLat / 2) ** 2 + Math.cos(rad(a.lat)) * Math.cos(rad(b.lat)) * Math.sin(dLng / 2) ** 2;
  return 2 * R * Math.asin(Math.sqrt(h));
}

// Desde "origen", visita siempre la tienda más cercana hasta completar "cantidad"
function rutaVecinoMasCercano(origen, tiendas, cantidad) {
  const pendientes = [...tiendas];
  const ruta = [];
  let actual = origen;
  let total = 0;

  while (ruta.length < cantidad && pendientes.length) {
    let indiceMasCercana = 0;
    pendientes.forEach((t, i) => {
      if (distanciaKm(actual, t) < distanciaKm(actual, pendientes[indiceMasCercana])) indiceMasCercana = i;
    });
    const siguiente = pendientes.splice(indiceMasCercana, 1)[0];
    total += distanciaKm(actual, siguiente);
    ruta.push(siguiente);
    actual = siguiente;
  }
  return { ruta, total };
}

function mejorRuta(tiendas, cantidad, ubicacionPersona) {
  if (ubicacionPersona) return rutaVecinoMasCercano(ubicacionPersona, tiendas, cantidad);

  // Sin ubicación: probar empezando en cada tienda y quedarse con la más corta
  let mejor = null;
  tiendas.forEach((inicio) => {
    const resto = tiendas.filter((t) => t !== inicio);
    const intento = rutaVecinoMasCercano(inicio, resto, cantidad - 1);
    intento.ruta.unshift(inicio);
    if (!mejor || intento.total < mejor.total) mejor = intento;
  });
  return mejor;
}

function enlaceRutaGoogleMaps(ruta, ubicacionPersona) {
  const punto = (p) => `${p.lat},${p.lng}`;
  const parametros = new URLSearchParams({ api: "1", travelmode: "walking" });
  const paradas = [...ruta];
  if (ubicacionPersona) parametros.set("origin", punto(ubicacionPersona));
  else parametros.set("origin", punto(paradas.shift()));
  parametros.set("destination", punto(paradas.pop() ?? ruta[0]));
  if (paradas.length) parametros.set("waypoints", paradas.map(punto).join("|"));
  return `https://www.google.com/maps/dir/?${parametros.toString()}`;
}

// Espera a que un elemento se vea en pantalla (al menos un 40 %)
function cuandoSeVea(elemento) {
  return new Promise((resolver) => {
    if (!("IntersectionObserver" in window)) return resolver();
    const observador = new IntersectionObserver(
      ([entrada]) => {
        if (!entrada.isIntersecting) return;
        observador.disconnect();
        resolver();
      },
      { threshold: 0.4 }
    );
    observador.observe(elemento);
  });
}

function obtenerUbicacion() {
  return new Promise((resolver, rechazar) => {
    if (!navigator.geolocation) return rechazar(new Error("sin soporte"));
    navigator.geolocation.getCurrentPosition(
      (pos) => resolver({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
      rechazar,
      { timeout: 10000 }
    );
  });
}

cuandoHayaDatos(() => {
  const formulario = document.getElementById("formulario-salida");
  const campoZona = document.getElementById("zona");
  const resultado = document.getElementById("resultado");
  const contenedorMapa = document.getElementById("mapa");
  let mapa = null;
  let capa = null;
  let salidaActual = 0; // si se arma otra salida, la animación anterior se cancela

  const tiendasUbicadas = TIENDAS.filter(tieneUbicacion);

  // Zonas disponibles (sin repetir)
  const zonas = [...new Set(tiendasUbicadas.map((t) => t.zona))].filter((z) => z && z !== "Por confirmar");
  campoZona.innerHTML += zonas.map((z) => `<option value="${escaparHTML(z)}">${escaparHTML(z)}</option>`).join("");

  formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const tipos = [...formulario.querySelectorAll('input[name="tipo"]:checked')].map((c) => c.value);
    const zona = campoZona.value;
    const cantidad = Number(document.getElementById("paradas").value);
    const desdeUbicacion = document.getElementById("inicio").value === "ubicacion";

    if (!tipos.length) {
      resultado.innerHTML = '<p class="aviso">Elige al menos un tipo de tienda.</p>';
      return;
    }

    const candidatas = tiendasUbicadas.filter((t) => tipos.includes(t.tipo) && (!zona || t.zona === zona));

    if (candidatas.length < 2) {
      resultado.innerHTML = `<p class="aviso">No hay suficientes tiendas ubicadas en el mapa con esos filtros.
        Prueba con "Cualquier zona" o con ambos tipos de tienda.</p>`;
      contenedorMapa.hidden = true;
      return;
    }

    let ubicacionPersona = null;
    let notaUbicacion = "";
    if (desdeUbicacion) {
      resultado.innerHTML = '<p class="estado">Buscando tu ubicación…</p>';
      try {
        ubicacionPersona = await obtenerUbicacion();
      } catch {
        notaUbicacion = '<p class="aviso">No pudimos obtener tu ubicación. Armamos la salida desde la primera tienda.</p>';
      }
    }

    const { ruta, total } = mejorRuta(candidatas, cantidad, ubicacionPersona);
    // "detalle" lleva solo textos cortos y nada personal (no se manda la ubicación)
    StyleMe.registrarEvento("ARMAR_SALIDA", { detalle: { paradas: String(ruta.length), zona: zona || "cualquiera", tipos: tipos.join(",") } });
    const kmReales = total * FACTOR_CALLES;
    const minutos = Math.round((kmReales / VELOCIDAD_CAMINANDO_KMH) * 60);

    resultado.innerHTML = `
      ${notaUbicacion}
      <h2 data-revelar>Tu salida de compras</h2>
      <p data-revelar style="--orden:1">${ruta.length} tiendas · aprox. <strong>${kmReales.toFixed(1)} km</strong> y
         <strong>${minutos} min</strong> caminando (sin contar el tiempo dentro de cada tienda).</p>
      <ol class="ruta" data-cascada>
        ${ruta
          .map(
            (t, i) => `
          <li>
            <span class="numero">${i + 1}</span>
            <div>
              <strong>${escaparHTML(t.nombre)}</strong>
              <span class="etiqueta">· ${escaparHTML(TIPOS_TIENDA[t.tipo])}</span>
              <p class="texto-suave">${escaparHTML(t.direccion)} · ${escaparHTML(t.horario)}</p>
              <a href="tienda.html?id=${encodeURIComponent(t.id)}">Ver tienda</a>
            </div>
          </li>`
          )
          .join("")}
      </ol>
      <div class="grupo-botones">
        <a class="boton" href="${enlaceRutaGoogleMaps(ruta, ubicacionPersona)}" target="_blank" rel="noopener">Abrir recorrido en Google Maps</a>      </div>
      <p class="texto-suave">Consejo: confirma el horario de cada tienda antes de salir.</p>`;

    // Dibujar el recorrido en el mapa
    contenedorMapa.hidden = false;
    if (!mapa) {
      mapa = crearMapa("mapa");
      capa = L.layerGroup().addTo(mapa);
    }
    capa.clearLayers();

    const puntos = ruta.map((t) => [t.lat, t.lng]);
    if (ubicacionPersona) puntos.unshift([ubicacionPersona.lat, ubicacionPersona.lng]);
    const desfase = ubicacionPersona ? 1 : 0; // si sales desde tu ubicación, ese es el punto 0

    // Dibuja el recorrido. --i es el orden de aparición de cada parada y cada tramo
    // (el ritmo se cambia con --paso-ruta en movimiento.css)
    function dibujarRuta() {
      if (ubicacionPersona) {
        L.circleMarker([ubicacionPersona.lat, ubicacionPersona.lng], { radius: 8 }).addTo(capa).bindPopup("Tú");
      }
      ruta.forEach((t, i) => {
        const icono = L.divIcon({
          className: "",
          html: `<div class="marcador-numero" style="--i:${i + desfase}">${i + 1}</div>`,
          iconSize: [32, 32],
          iconAnchor: [16, 16]
        });
        L.marker([t.lat, t.lng], { icon: icono }).addTo(capa).bindPopup(popupTienda(t));
      });
      // La línea va por tramos: cada tramo aparece justo antes de la parada a la que llega
      for (let k = 1; k < puntos.length; k++) {
        const tramo = L.polyline([puntos[k - 1], puntos[k]], {
          color: "#a35d6d",
          weight: 4,
          dashArray: "6 8",
          className: "ruta-tramo"
        }).addTo(capa);
        tramo.getElement()?.style.setProperty("--i", k - 1);
      }
    }

    mapa.invalidateSize();
    const vista = { padding: [40, 40], maxZoom: 17 };
    const menosMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    if (menosMovimiento) {
      mapa.fitBounds(puntos, vista);
      dibujarRuta();
      return;
    }

    // Con movimiento: cuando el mapa se ve en pantalla, vuela hasta el recorrido
    // y al llegar empieza a dibujar las paradas
    const numero = ++salidaActual;
    await cuandoSeVea(contenedorMapa);
    if (numero !== salidaActual) return; // ya se pidió otra salida

    let empezo = false;
    const empezar = () => {
      if (empezo || numero !== salidaActual) return;
      empezo = true;
      dibujarRuta();
    };
    mapa.once("moveend", empezar);
    setTimeout(empezar, 1300); // por si el mapa ya estaba en ese lugar y no se mueve
    mapa.flyToBounds(puntos, { ...vista, duration: 1 });
  });
});
