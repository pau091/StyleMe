# 07 · Historias de usuario: MVP 1 (borrador)

> ⏳ **Borrador.** Se basa en tu propuesta de MVP 1 (registro de usuarios y tiendas, catálogos, categorías, buscador y mapa). Las historias marcadas 💡 dependen de aprobar la reorganización P-001.
>
> **Formato:** *Como [rol], quiero [acción] para [beneficio].* Los **criterios de aceptación** indican cuándo la función se considera terminada.

## Comprador / visitante

### HU-01 · Registrarse e iniciar sesión
Como comprador, quiero crear una cuenta e iniciar sesión para usar las funciones personales.
- [ ] Registro con nombre, correo y contraseña.
- [ ] Aceptación de términos y política de privacidad.
- [ ] Inicio y cierre de sesión.
- [ ] Recuperación de contraseña.

### HU-02 · Explorar por categorías
Como visitante, quiero navegar por las cuatro categorías para descubrir productos sin buscar algo específico.
- [ ] La página principal muestra las 4 categorías.
- [ ] Cada categoría lista productos de diferentes tiendas.

### HU-03 · Buscar productos y tiendas
Como visitante, quiero escribir lo que busco para encontrar productos o tiendas rápidamente.
- [ ] Busca por nombre de producto, marca y tienda.
- [ ] Muestra un mensaje útil si no hay resultados.

### HU-04 · Filtrar resultados 💡
Como visitante, quiero filtrar por precio, talla, color, estilo y marca para encontrar opciones dentro de mi presupuesto.
- [ ] Los filtros cambian según la categoría (las tallas solo aplican a ropa).
- [ ] Se pueden combinar varios filtros.

### HU-05 · Ver un producto
Como visitante, quiero ver el detalle de un producto para decidir si me interesa.
- [ ] Fotos, nombre, precio, marca, tallas o variantes, descripción y tienda.
- [ ] Fecha de última actualización del precio 💡.
- [ ] Enlace al perfil de la tienda.

### HU-06 · Ver el perfil de una tienda
Como visitante, quiero ver la información de una tienda para saber dónde está y qué vende.
- [ ] Nombre, descripción, dirección, mini mapa, contacto, horarios, fotos y catálogo.

### HU-07 · Ver tiendas en el mapa
Como visitante, quiero ver las tiendas en un mapa de Bucaramanga para encontrar las más cercanas.
- [ ] Muestra un marcador por cada tienda verificada.
- [ ] Al tocar un marcador se ve un resumen con un enlace al perfil.
- [ ] Filtro por categoría.
- [ ] (Opcional) Centrar el mapa en mi ubicación, previo permiso.

### HU-08 · Guardar favoritos 💡
Como comprador, quiero guardar productos y tiendas para volver a verlos después.
- [ ] Botón de favorito en el producto y en la tienda.
- [ ] Sección "Mis favoritos".

## Comerciante

### HU-09 · Registrar mi establecimiento
Como comerciante, quiero registrar mi tienda para aparecer en StyleMe.
- [ ] Formulario: nombre comercial, descripción, dirección, ubicación en el mapa, contacto, horarios, fotos y categorías.
- [ ] La tienda queda en estado `pendiente` hasta que el administrador la apruebe.
- [ ] Autorización para usar las imágenes de los productos.

### HU-10 · Administrar mi catálogo
Como comerciante, quiero agregar, editar y eliminar productos para mantener mi catálogo al día.
- [ ] Crear un producto con foto, nombre, precio, categoría, marca y variantes (talla, color).
- [ ] Marcar como disponible o agotado.
- [ ] Se puede hacer cómodamente desde el celular 💡.

### HU-11 · Editar la información de mi negocio
Como comerciante, quiero actualizar horarios, contacto y fotos para que la información sea correcta.

## Administrador

### HU-12 · Aprobar comercios
Como administrador, quiero revisar las tiendas registradas para evitar perfiles falsos.
- [ ] Lista de tiendas pendientes.
- [ ] Aprobar, rechazar o suspender.

### HU-13 · Gestionar categorías y usuarios
Como administrador, quiero gestionar categorías y usuarios para mantener el orden de la plataforma.
