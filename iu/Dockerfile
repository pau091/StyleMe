# IMAGEN DE LA PÁGINA (iu) DE STYLEME
# -----------------------------------
# nginx sirve los archivos de la página y reenvía /api/... al backend (ver nginx.conf).
# Así la página y la API comparten dirección: el navegador no necesita permisos CORS.

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY . /usr/share/nginx/html
# Los archivos de Docker no son parte de la página: no se publican
RUN rm -f /usr/share/nginx/html/Dockerfile /usr/share/nginx/html/nginx.conf /usr/share/nginx/html/.dockerignore
EXPOSE 80
