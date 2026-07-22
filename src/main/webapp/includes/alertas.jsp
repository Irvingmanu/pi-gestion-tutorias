<link href="<%= request.getContextPath() %>/assets/css/alertas.css" rel="stylesheet">

<div class="modal fade" id="modalAlerta" tabindex="-1" aria-labelledby="alertaTitulo" aria-hidden="true" data-bs-backdrop="static">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content alerta-card">
            <div class="modal-body alerta-body">

                <div class="alerta-icono-wrap">
                    <div class="alerta-icono" id="alertaIconoCirculo">
                        <img id="alertaIcono" src="" alt="" data-base-path="<%= request.getContextPath() %>/assets/img/alertas/">
                    </div>
                </div>

                <div class="alerta-texto">
                    <h2 class="alerta-titulo" id="alertaTitulo"></h2>
                    <p class="alerta-mensaje" id="alertaMensaje"></p>
                </div>

                <div class="alerta-botones" id="alertaBotones">
                    <button type="button" class="alerta-btn alerta-btn-secundario" id="alertaBtnCancelar" data-bs-dismiss="modal">Cancelar</button>
                    <button type="button" class="alerta-btn alerta-btn-exito" id="alertaBtnAceptar" data-bs-dismiss="modal">Aceptar</button>
                </div>

            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="modalConfirmacion" tabindex="-1" aria-labelledby="confirmacionTitulo" aria-hidden="true" data-bs-backdrop="static">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content alerta-card">
            <div class="modal-body alerta-body">

                <div class="confirmacion-icono-wrap">
                    <div class="confirmacion-icono" id="confirmacionIconoCirculo">
                        <img id="confirmacionIcono" src="" alt="" data-base-path="<%= request.getContextPath() %>/assets/img/alertas/">
                    </div>
                </div>

                <h2 class="confirmacion-titulo" id="confirmacionTitulo"></h2>
                <p class="confirmacion-mensaje" id="confirmacionMensaje"></p>

                <div class="confirmacion-botones">
                    <button type="button" class="btn-confirmar-cancelar" id="btnConfirmacionCancelar" data-bs-dismiss="modal">Cancelar</button>
                    <button type="button" class="btn-confirmar" id="btnConfirmacionAceptar">Aceptar</button>
                </div>

            </div>
        </div>
    </div>
</div>

<div class="toast-container position-fixed top-0 end-0 p-3">
    <div id="toastNotificacion" class="toast toast-alerta" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="toast-alerta-cuerpo">
            <div class="toast-alerta-icono" id="toastIconoCirculo">
                <img id="toastIcono" src="" alt="" data-base-path="<%= request.getContextPath() %>/assets/img/alertas/">
            </div>
            <div class="toast-alerta-contenido">
                <p class="toast-alerta-titulo" id="toastTitulo"></p>
                <p class="toast-alerta-mensaje" id="toastMensaje"></p>
            </div>
            <button type="button" class="btn-close toast-alerta-cerrar" data-bs-dismiss="toast" aria-label="Cerrar"></button>
        </div>
        <div class="toast-progress-bar" id="toastBarra"></div>
    </div>
</div>
