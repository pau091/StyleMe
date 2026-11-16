/*
 * RECOMENDADOR DE SKINCARE Y CUIDADO CAPILAR
 * ------------------------------------------
 * Funciona con REGLAS, no inventa nada: solo sugiere productos que están en data/productos.js.
 *
 * Cada producto recibe puntos:
 *   +3 si es para tu tipo exacto de piel o cabello (+1 si es "para todas/todos")
 *   +2 por cada objetivo que coincide
 *   +1 si dice "libre de" algo que quieres evitar
 * Se descarta si no es para tu tipo o si supera tu presupuesto.
 * Luego se arma la rutina: el producto con más puntos en cada paso.
 */

const PASOS = {
  skincare: [
    { clave: "limpiador", nombre: "Limpiar", cuando: "Mañana y noche" },
    { clave: "tonico", nombre: "Tonificar", cuando: "Opcional, después de limpiar" },
    { clave: "serum", nombre: "Tratar (sérum)", cuando: "Mañana o noche" },
    { clave: "hidratante", nombre: "Hidratar", cuando: "Mañana y noche" },
    { clave: "protector", nombre: "Proteger del sol", cuando: "Todas las mañanas, incluso si está nublado" }
  ],
  capilar: [
    { clave: "shampoo", nombre: "Lavar", cuando: "Según tu cuero cabelludo" },
    { clave: "acondicionador", nombre: "Acondicionar", cuando: "Después de cada lavado, de medios a puntas" },
    { clave: "tratamiento", nombre: "Tratamiento", cuando: "1 o 2 veces por semana" },
    { clave: "peinar", nombre: "Peinar o definir", cuando: "Con el cabello húmedo" }
  ]
};

const NOMBRES_OBJETIVOS = {
  hidratar: "hidratar", grasa: "controlar la grasa", imperfecciones: "imperfecciones", manchas: "manchas",
  proteger: "protección solar", antiedad: "líneas de expresión", frizz: "frizz", reparar: "reparar daño",
  rizos: "definir rizos", fortalecer: "fortalecer"
};

// Lee las respuestas de un formulario y las devuelve como objeto
function leerRespuestas(formulario) {
  const datos = new FormData(formulario);
  return {
    tipoPiel: datos.get("tipoPiel"),
    tipoCabello: datos.get("tipoCabello"),
    cuero: datos.get("cuero"),
    objetivos: datos.getAll("objetivos"),
    evitar: datos.getAll("evitar"),
    presupuesto: Number(datos.get("presupuesto")) || null,
    condicion: datos.get("condicion") === "on"
  };
}

// Devuelve { producto, puntos, razones, avisos } o null si no aplica
function evaluar(producto, r, categoria) {
  const razones = [];
  const avisos = [];
  let puntos = 0;

  // ¿Es para su tipo?
  const tipos = categoria === "skincare" ? producto.tipoPiel ?? [] : producto.tipoCabello ?? [];
  const tipoPersona = categoria === "skincare" ? r.tipoPiel : r.tipoCabello;
  const comodin = categoria === "skincare" ? "todas" : "todos";
  if (tipos.includes(tipoPersona)) {
    puntos += 3;
    razones.push(categoria === "skincare" ? `para piel ${tipoPersona}` : `para cabello ${tipoPersona}`);
  } else if (tipos.includes(comodin)) {
    puntos += 1;
    razones.push(categoria === "skincare" ? "para todo tipo de piel" : "para todo tipo de cabello");
  } else {
    return null;
  }

  // Cuero cabelludo (solo capilar)
  if (categoria === "capilar") {
    const cuero = producto.cuero ?? [];
    if (cuero.includes(r.cuero)) {
      puntos += 2;
      razones.push(`cuero cabelludo ${r.cuero}`);
    } else if (!cuero.includes("todos")) {
      return null;
    }
  }

  // Objetivos
  (producto.objetivos ?? []).forEach((o) => {
    if (r.objetivos.includes(o)) {
      puntos += 2;
      razones.push(NOMBRES_OBJETIVOS[o] ?? o);
    }
  });

  // Ingredientes a evitar
  r.evitar.forEach((ingrediente) => {
    if ((producto.libreDe ?? []).includes(ingrediente)) {
      puntos += 1;
      razones.push(`sin ${ingrediente}`);
    } else {
      avisos.push(`Confirma con la tienda si contiene ${ingrediente}.`);
    }
  });

  // Presupuesto (si el producto no tiene precio, se muestra igual)
  if (r.presupuesto && producto.precio !== null && producto.precio > r.presupuesto) return null;
  if (producto.precio === null) avisos.push("Precio por confirmar.");

  return { producto, puntos, razones, avisos };
}

function tarjetaRecomendacion(item) {
  const p = item.producto;
  const tienda = buscarTienda(p.tiendaId);
  const mensaje = `Hola, vi en StyleMe el producto "${p.nombre}". ¿Lo tienen disponible?`;
  const whatsapp = tienda ? enlaceWhatsapp(tienda.whatsapp, mensaje) : "";
  return `
    <div class="recomendacion">
      <div class="recomendacion-imagen">${imagenOMarcador(p.foto, p.nombre, p.categoria)}</div>
      <div>
        <strong>${escaparHTML(p.nombre)}</strong> ${p.pendiente ? '<span class="sello sello-linea">Ejemplo</span>' : ""}
        <p class="precio">${formatoPrecio(p.precio)}</p>
        ${tienda ? `<p class="texto-suave">🏬 <a href="tienda.html?id=${encodeURIComponent(tienda.id)}">${escaparHTML(tienda.nombre)}</a></p>` : ""}
        <p class="texto-suave">✔ ${escaparHTML(item.razones.join(" · "))}</p>
        ${item.avisos.map((a) => `<p class="texto-suave">⚠️ ${escaparHTML(a)}</p>`).join("")}
        ${whatsapp ? `<a class="boton boton-whatsapp boton-pequeno" href="${whatsapp}" target="_blank" rel="noopener">💬 Preguntar</a>` : ""}
      </div>
    </div>`;
}

function mostrarResultado(formulario, categoria) {
  const r = leerRespuestas(formulario);
  const resultado = document.getElementById("resultado");

  const evaluados = PRODUCTOS.filter((p) => p.categoria === categoria)
    .map((p) => evaluar(p, r, categoria))
    .filter(Boolean)
    .sort((a, b) => b.puntos - a.puntos);

  // Rutina: el mejor producto de cada paso
  const usados = new Set();
  const rutina = PASOS[categoria].map((paso) => {
    const mejor = evaluados.find((e) => e.producto.paso === paso.clave);
    if (mejor) usados.add(mejor.producto.id);
    return { paso, mejor };
  });
  const otros = evaluados.filter((e) => !usados.has(e.producto.id));

  const avisoMedico = r.condicion
    ? `<p class="aviso">🩺 Por lo que nos cuentas, te recomendamos <strong>consultar a un dermatólogo</strong> antes de usar productos nuevos.
       Las sugerencias de abajo son solo de referencia.</p>`
    : "";

  resultado.innerHTML = `
    <h2>Tu rutina sugerida</h2>
    ${avisoMedico}
    <ol class="ruta">
      ${rutina
        .map(
          ({ paso, mejor }, i) => `
        <li>
          <span class="numero">${i + 1}</span>
          <div style="flex:1">
            <strong>${paso.nombre}</strong>
            <p class="texto-suave">🕒 ${paso.cuando}</p>
            ${mejor ? tarjetaRecomendacion(mejor) : '<p class="texto-suave">Aún no tenemos un producto para este paso en las tiendas registradas.</p>'}
          </div>
        </li>`
        )
        .join("")}
    </ol>
    ${otros.length ? `<h2>Otras opciones para ti</h2>${otros.map(tarjetaRecomendacion).join("")}` : ""}
    ${evaluados.length ? "" : '<p class="vacio">No encontramos productos con esas respuestas. Prueba sin presupuesto o con otras opciones.</p>'}`;

  resultado.scrollIntoView({ behavior: "smooth", block: "start" });
}

document.addEventListener("DOMContentLoaded", () => {
  const pestanas = document.querySelectorAll('[role="tab"]');
  const panelPiel = document.getElementById("panel-piel");
  const panelCabello = document.getElementById("panel-cabello");

  // Cambiar entre "Mi piel" y "Mi cabello"
  pestanas.forEach((pestana) => {
    pestana.addEventListener("click", () => {
      pestanas.forEach((p) => {
        const activa = p === pestana;
        p.setAttribute("aria-selected", activa);
        p.classList.toggle("boton-secundario", !activa);
        document.getElementById(p.dataset.panel).hidden = !activa;
      });
      document.getElementById("resultado").innerHTML = "";
    });
  });

  // Abrir directamente en cabello: recomendador.html?tema=cabello
  if (parametroURL("tema") === "cabello") pestanas[1].click();

  panelPiel.addEventListener("submit", (e) => {
    e.preventDefault();
    mostrarResultado(panelPiel, "skincare");
  });
  panelCabello.addEventListener("submit", (e) => {
    e.preventDefault();
    mostrarResultado(panelCabello, "capilar");
  });
});
