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

function obtenerModalAlerta() {
    if (!alertaModalInstancia) {
        alertaModalInstancia = new bootstrap.Modal(document.getElementById('modalAlerta'));
    }
    return alertaModalInstancia;
}

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
