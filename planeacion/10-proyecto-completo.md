# 10 · Proyecto completo: StyleMe (visión original)

> Esta es la plataforma que se quiere construir **después** de la entrega académica, para poder ofrecerla a las tiendas y monetizarla.
> ⚠️ Con los requisitos de la entrega final (D-045), buena parte de esta visión (roles, reseñas, favoritos, backend) **sí se va a construir**. Este documento se actualizará al organizar las fases.
>
> Detalle de cada parte: [01 Visión](01-vision.md) · [02 Alcance](02-alcance-y-fases.md) · [03 Roles](03-roles-y-permisos.md) · [05 Riesgos](05-riesgos.md) · [07 Historias de usuario](07-historias-mvp1.md) · [08 Validación](08-validacion.md)

## 1. La visión

StyleMe reúne las tiendas físicas de moda y belleza de Bucaramanga, desde grandes marcas hasta emprendimientos, para que la gente **descubra, compare, visualice y encuentre** productos y sepa a dónde ir. Se apoya en tres pilares:

1. **Descubrimiento:** catálogos reales, buscador, filtros, mapa y comparación.
2. **Inteligencia artificial:** probador virtual, recomendador de skincare y cuidado capilar, asesor de moda.
3. **Comunidad:** reseñas de 1 a 5 corazones, favoritos y perfiles.

## 2. Qué falta respecto a la versión sencilla

| Área | Versión sencilla (entrega) | Versión completa |
|---|---|---|
| Datos | Archivos JSON escritos a mano | Base de datos; cada tienda actualiza su catálogo |
| Usuarios | Sin cuentas | Comprador, comerciante y administrador |
| Tiendas | 10, cargadas por ti | Las tiendas se registran solas y el administrador las aprueba |
| Productos | Ejemplos "de referencia" | Catálogo real con precio, existencias y fecha de actualización |
| Recomendador | Reglas en JavaScript | Reglas + IA que explica y arma la rutina |
| Probador virtual | Simulación (opcional) | IA real, con consentimiento y borrado automático de fotos |
| Asesor de moda | — | IA que arma outfits solo con productos reales del catálogo |
| Comunidad | — | Reseñas con corazones, favoritos y moderación |
| Comparación | — | Comparar precio, marca, tienda y ubicación |

## 3. Qué hay que BUSCAR o investigar 🔍

| # | Tema | Por qué |
|---|---|---|
| B-01 | Disponibilidad del nombre **StyleMe** en la SIC y de dominios (`.co`, `.com.co`, `.com`) | Necesario antes de crear logo y marca (R-13) |
| B-02 | Backend gratuito o barato: Supabase, Firebase, etc. | Cuentas, base de datos y almacenamiento de fotos |
| B-03 | Modelos de probador virtual: APIs pagas o modelos abiertos | Calidad, costo por imagen y tiempos de espera (R-03) |
| B-04 | API de IA para el asesor y las rutinas | Costo por uso y forma de limitarlo solo al catálogo (R-09) |
| B-05 | Ley 1581 de 2012: política de privacidad, autorización de datos y registro ante la SIC si aplica | Obligatorio al manejar cuentas y fotos (R-04) |
| B-06 | Términos y condiciones, y autorización de uso de imágenes para comercios | Derechos de las fotos de producto (R-06) |
| B-07 | Cómo se verifica que un comercio es real: RUT, Cámara de Comercio | Evitar tiendas falsas |
| B-08 | Competencia en Colombia: directorios, marketplaces, Instagram Shopping, Google Maps | Saber qué hace diferente a StyleMe |
| B-09 | Cuánto pagaría una tienda y por qué (entrevistas, ver [08](08-validacion.md)) | Base del modelo de negocio |
| B-10 | Proveedor de mapas a escala: OpenStreetMap o Google Maps | Costo cuando haya más tráfico |

## 4. Qué hay que IMPLEMENTAR 🛠️

Ordenado por fases sugeridas (ver P-001 en [04 - Decisiones](04-decisiones.md)).

### Fase 1: plataforma real
- [ ] Base de datos y backend.
- [ ] Registro e inicio de sesión (comprador y comerciante).
- [ ] Registro de tiendas con estado `pendiente → verificado → suspendido`.
- [ ] Panel del comerciante: crear y editar productos desde el celular, marcar agotado.
- [ ] Panel del administrador: aprobar tiendas, gestionar categorías y usuarios.
- [ ] Favoritos de productos y tiendas.
- [ ] Fecha de "última actualización" en precio y disponibilidad.

### Fase 2: comunidad y primera IA
- [ ] Reseñas de 1 a 5 corazones: una por usuario y tienda, con botón de reporte y moderación.
- [ ] Recomendador de skincare y cuidado capilar con ficha estructurada de producto (tipo de piel o cabello, ingredientes).
- [ ] Rutinas personalizadas guardadas en la cuenta.

### Fase 3: probador virtual con IA
- [ ] Prueba de concepto: calidad, costo y tiempo.
- [ ] Consentimiento, solo mayores de 18, borrado automático de fotos, sin entrenar modelos con ellas.
- [ ] Límites de uso por cuenta y botón de reporte.

### Fase 4: valor agregado
- [ ] Asesor de moda con IA, por ejemplo "outfit para una cena, máximo $250.000", usando solo el catálogo real.
- [ ] Comparación de productos.
- [ ] Probador de maquillaje.
- [ ] Ampliación al área metropolitana (Floridablanca, Girón, Piedecuesta) y a otras ciudades.

## 5. Qué hay que MEJORAR 📈

- **Diseño y marca:** logo, paleta de colores, tipografía y accesibilidad para todas las edades (letra grande, buen contraste).
- **Datos:** pasar de productos "de referencia" a catálogos autorizados por cada tienda.
- **Buscador:** tolerancia a errores de escritura y sinónimos ("labial" = "pintalabios").
- **Velocidad:** imágenes optimizadas para celular.
- **Confianza:** sello de "tienda verificada" y política de privacidad clara.
- **Medición:** cuántas personas tocan "WhatsApp" o "Cómo llegar" por tienda. Ese dato sirve para venderles el servicio.

## 6. Monetización: ideas para investigar 💡

> Nada de esto está decidido. Son opciones para validar con las tiendas (B-09).

| Idea | Cómo funciona |
|---|---|
| Plan gratis + plan premium para tiendas | Gratis: perfil básico. Premium: más productos, estadísticas, aparecer destacada |
| Tiendas destacadas | Pago por aparecer primero en el inicio o en una categoría |
| Reporte de estadísticas | Cuántas personas vieron la tienda, le escribieron o pidieron cómo llegar |
| Pago por uso del probador | La tienda paga para que sus prendas tengan probador virtual |
| Campañas por temporada | Por ejemplo "Día de la Madre en Cabecera", con tiendas participantes |

**Primer paso sugerido:** al terminar la entrega, mostrarles la página a las 10 tiendas, preguntarles qué les sirve y pedir permiso para usar sus fotos y productos reales.
