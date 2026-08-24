<link href="<%= request.getContextPath() %>/assets/css/cargando.css" rel="stylesheet">

<!-- Overlay global de "cargando": lo activan/desactivan cargando.js (interceptando
     cualquier <form>.submit() y window.fetch de la pagina, ver ese archivo) sin que
     cada boton/formulario tenga que llamar nada a mano. -->
<div id="overlayCargando" class="overlay-cargando" aria-hidden="true">
    <div class="spinner-cargando" role="status" aria-label="Cargando"></div>
</div>
