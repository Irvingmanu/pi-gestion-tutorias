/**
 * Provee las funciones globales de alertas, confirmaciones y notificaciones toast
 * basadas en modales de Bootstrap, usadas en todo el sistema para retroalimentar
 * al usuario (éxito, error, advertencia) y para confirmar acciones destructivas.
 * @author Irvingmanu
 * @date 2026-07-21
 */
const ALERTA_RUTAS_ICONO = {
    exito: 'exito.png',
    error: 'error.png',
    advertencia: 'advertencia.png'
};

const ALERTA_CLASES_ICONO = {
    exito: 'alerta-icono--exito',
    error: 'alerta-icono--error',
    advertencia: 'alerta-icono--advertencia'
};

const ALERTA_CLASES_BOTON = {
    exito: 'alerta-btn-exito',
    error: 'alerta-btn-error',
    advertencia: 'alerta-btn-advertencia'
};

let alertaModalInstancia = null;

/**
 * Obtiene (creando una única vez) la instancia del modal de Bootstrap usado
 * para mostrar alertas simples.
 * @returns {bootstrap.Modal} la instancia del modal de alerta
 */
function obtenerModalAlerta() {
    if (!alertaModalInstancia) {
        alertaModalInstancia = new bootstrap.Modal(document.getElementById('modalAlerta'));
    }
    return alertaModalInstancia;
}

/**
 * Aplica el ícono, color de círculo y color de botón correspondientes al tipo
 * de alerta (éxito, error o advertencia) sobre los elementos del modal de alerta.
 * @param {string} tipo - el tipo de alerta ("exito", "error" o "advertencia")
 * @returns {void}
 */
function aplicarTipoAlerta(tipo) {
    let tipoValido = ALERTA_CLASES_ICONO[tipo] ? tipo : 'exito';

    let icono = document.getElementById('alertaIcono');
    let circulo = document.getElementById('alertaIconoCirculo');
    let btnAceptar = document.getElementById('alertaBtnAceptar');

    icono.src = icono.dataset.basePath + ALERTA_RUTAS_ICONO[tipoValido];
    icono.alt = tipoValido;

    circulo.classList.remove('alerta-icono--exito', 'alerta-icono--error', 'alerta-icono--advertencia');
    circulo.classList.add(ALERTA_CLASES_ICONO[tipoValido]);

    btnAceptar.classList.remove('alerta-btn-exito', 'alerta-btn-error', 'alerta-btn-advertencia');
    btnAceptar.classList.add(ALERTA_CLASES_BOTON[tipoValido]);
}

/**
 * Muestra el modal de alerta simple (solo botón "Aceptar") con el título,
 * mensaje y tipo indicados.
 * @param {string} tipo - el tipo de alerta ("exito", "error" o "advertencia")
 * @param {string} titulo - el título mostrado en el modal
 * @param {string} mensaje - el mensaje descriptivo mostrado en el modal
 * @returns {void}
 */
function mostrarAlerta(tipo, titulo, mensaje) {
    document.getElementById('alertaTitulo').innerText = titulo;
    document.getElementById('alertaMensaje').innerText = mensaje;

    aplicarTipoAlerta(tipo);

    document.getElementById('alertaBtnCancelar').style.display = 'none';
    document.getElementById('alertaBtnAceptar').innerText = 'Aceptar';

    obtenerModalAlerta().show();
}

const CONFIRMACION_RUTAS_ICONO = {
    critica: 'error.png',
    advertencia: 'advertencia.png'
};

/**
 * Aplica el ícono y color de círculo correspondientes al tipo de confirmación
 * (crítica o advertencia) sobre los elementos del modal de confirmación.
 * @param {string} tipo - el tipo de confirmación ("critica" o "advertencia")
 * @returns {string} el tipo validado realmente aplicado ("critica" por defecto si no coincide)
 */
function aplicarTipoConfirmacion(tipo) {
    let tipoValido = CONFIRMACION_RUTAS_ICONO[tipo] ? tipo : 'critica';

    let icono = document.getElementById('confirmacionIcono');
    let circulo = document.getElementById('confirmacionIconoCirculo');

    icono.src = icono.dataset.basePath + CONFIRMACION_RUTAS_ICONO[tipoValido];
    icono.alt = tipoValido;

    circulo.classList.remove('confirmacion-icono--critica', 'confirmacion-icono--advertencia');
    circulo.classList.add('confirmacion-icono--' + tipoValido);

    return tipoValido;
}

/**
 * Muestra el modal de confirmación (con botón "Aceptar" personalizado) y ejecuta
 * el callback indicado solo si el usuario confirma la acción. Reemplaza el botón
 * de aceptar por un clon para eliminar cualquier listener de una invocación previa.
 * @param {string} tipo - el tipo de confirmación ("critica" o "advertencia")
 * @param {string} titulo - el título mostrado en el modal
 * @param {string} mensaje - el mensaje descriptivo mostrado en el modal
 * @param {string} textoBotonAceptar - el texto a mostrar en el botón de confirmar
 * @param {Function} callbackAccion - la función a ejecutar cuando el usuario confirma
 * @returns {void}
 */
function mostrarConfirmacion(tipo, titulo, mensaje, textoBotonAceptar, callbackAccion) {
    document.getElementById('confirmacionTitulo').innerText = titulo;
    document.getElementById('confirmacionMensaje').innerText = mensaje;

    let tipoValido = aplicarTipoConfirmacion(tipo);

    let btnAceptarViejo = document.getElementById('btnConfirmacionAceptar');
    let btnAceptar = btnAceptarViejo.cloneNode(true);
    btnAceptarViejo.parentNode.replaceChild(btnAceptar, btnAceptarViejo);

    btnAceptar.innerText = textoBotonAceptar;
    btnAceptar.classList.remove('btn-confirmar-critico', 'btn-confirmar-advertencia');
    btnAceptar.classList.add(tipoValido === 'advertencia' ? 'btn-confirmar-advertencia' : 'btn-confirmar-critico');

    btnAceptar.addEventListener('click', function () {
        if (typeof callbackAccion === 'function') {
            callbackAccion();
        }
        bootstrap.Modal.getInstance(document.getElementById('modalConfirmacion')).hide();
    });

    new bootstrap.Modal(document.getElementById('modalConfirmacion')).show();
}

document.getElementById('modalConfirmacion').addEventListener('shown.bs.modal', function () {
    let backdrops = document.querySelectorAll('.modal-backdrop.show');
    let backdropConfirmacion = backdrops[backdrops.length - 1];
    if (backdropConfirmacion) {
        backdropConfirmacion.classList.add('confirmacion-backdrop');
    }
});

const TOAST_CLASES_ICONO = {
    exito: 'toast-alerta-icono--exito',
    error: 'toast-alerta-icono--error',
    advertencia: 'toast-alerta-icono--advertencia'
};

const TOAST_CLASES_BARRA = {
    exito: 'toast-progress-bar--exito',
    error: 'toast-progress-bar--error',
    advertencia: 'toast-progress-bar--advertencia'
};

/**
 * Muestra una notificación toast temporal (4 segundos) con el ícono, color de
 * barra de progreso, título y mensaje correspondientes al tipo indicado.
 * @param {string} tipo - el tipo de notificación ("exito", "error" o "advertencia")
 * @param {string} titulo - el título mostrado en el toast
 * @param {string} mensaje - el mensaje descriptivo mostrado en el toast
 * @returns {void}
 */
function mostrarToast(tipo, titulo, mensaje) {
    let tipoValido = ALERTA_RUTAS_ICONO[tipo] ? tipo : 'exito';

    document.getElementById('toastTitulo').innerText = titulo;
    document.getElementById('toastMensaje').innerText = mensaje;

    let icono = document.getElementById('toastIcono');
    icono.src = icono.dataset.basePath + ALERTA_RUTAS_ICONO[tipoValido];
    icono.alt = tipoValido;

    let circulo = document.getElementById('toastIconoCirculo');
    circulo.classList.remove('toast-alerta-icono--exito', 'toast-alerta-icono--error', 'toast-alerta-icono--advertencia');
    circulo.classList.add(TOAST_CLASES_ICONO[tipoValido]);

    let barra = document.getElementById('toastBarra');
    barra.classList.remove('toast-progress-bar--error', 'toast-progress-bar--advertencia');
    if (tipoValido !== 'exito') {
        barra.classList.add(TOAST_CLASES_BARRA[tipoValido]);
    }

    barra.style.animation = 'none';
    void barra.offsetWidth;
    barra.style.animation = '';

    const toastElement = document.getElementById('toastNotificacion');
    const toast = new bootstrap.Toast(toastElement, { delay: 4000 });
    toast.show();
}
document.addEventListener('DOMContentLoaded', function () {
    const parametros = new URLSearchParams(window.location.search);
    const exito = parametros.get('exito');

    if (exito === 'asistencia_guardada') {
        mostrarAlerta('exito', 'Éxito', 'Se registró exitosamente');
        document.getElementById('alertaBtnAceptar').innerText = 'OK';
        window.history.replaceState(null, null, window.location.pathname);
    }

    const formularioGuardar = document.getElementById('formGuardar');

    if (formularioGuardar) {
        formularioGuardar.addEventListener('submit', function (e) {
            if (this.dataset.confirmado === 'true') {
                return;
            }

            e.preventDefault();

            mostrarConfirmacion(
                'advertencia',
                '¿Deseas guardar?',
                'Estás a punto de registrar los datos ingresados en el sistema.',
                'Sí, guardar',
                () => {
                    formularioGuardar.dataset.confirmado = 'true';
                    formularioGuardar.submit();
                }
            );
        });
    }
});
