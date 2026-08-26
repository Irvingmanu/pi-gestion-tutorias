/**
 * Controla el modal de cambio de contraseña (verificación de la contraseña
 * actual seguida de captura y guardado de la nueva contraseña), incluyendo
 * validación en vivo y comunicación con el endpoint de cambio de contraseña.
 * @author J4IROXD
 * @date 2026-08-20
 */
let passwordActualVerificada = null;

/**
 * Obtiene (creando una única vez) la instancia del modal de Bootstrap para
 * cambiar la contraseña.
 * @returns {bootstrap.Modal|null} la instancia del modal, o null si el elemento no existe en el DOM
 */
function obtenerModalCambiarPassword() {
    const el = document.getElementById('modalCambiarPassword');
    if (!el) {
        console.error('No se encontró el elemento #modalCambiarPassword en el DOM.');
        return null;
    }

    return bootstrap.Modal.getOrCreateInstance(el);
}

/**
 * Marca un campo como inválido y muestra el mensaje de error correspondiente.
 * @param {HTMLElement} input - el campo a marcar como inválido
 * @param {HTMLElement} errorEl - el elemento donde se muestra el mensaje de error
 * @param {string} mensaje - el mensaje de error a mostrar
 * @returns {void}
 */
function marcarInvalido(input, errorEl, mensaje) {
    input.classList.add('is-invalid');
    if (errorEl && mensaje) errorEl.textContent = mensaje;
}

/**
 * Quita el estado visual de inválido de un campo.
 * @param {HTMLElement} input - el campo a limpiar
 * @returns {void}
 */
function limpiarInvalido(input) {
    input.classList.remove('is-invalid');
}

/**
 * Avanza la interfaz del modal del primer paso (verificar contraseña actual)
 * al segundo paso (capturar la nueva contraseña).
 * @returns {void}
 */
function irAPasoNuevaPassword() {
    document.getElementById('pasoVerificarPassword').classList.add('d-none');
    document.getElementById('pasoNuevaPassword').classList.remove('d-none');
    document.getElementById('btnVerificarPassword').classList.add('d-none');
    document.getElementById('btnGuardarNuevaPassword').classList.remove('d-none');
    document.getElementById('passwordNueva').focus();
}

/**
 * Reinicia el modal de cambio de contraseña a su estado inicial: vuelve al
 * primer paso, limpia los campos de contraseña y su estado de validación,
 * y olvida la contraseña actual verificada previamente.
 * @returns {void}
 */
function resetearModalCambiarPassword() {
    passwordActualVerificada = null;

    document.getElementById('pasoVerificarPassword').classList.remove('d-none');
    document.getElementById('pasoNuevaPassword').classList.add('d-none');
    document.getElementById('btnVerificarPassword').classList.remove('d-none');
    document.getElementById('btnGuardarNuevaPassword').classList.add('d-none');
    document.getElementById('btnGuardarNuevaPassword').disabled = true;

    ['passwordVerificar', 'passwordNueva', 'passwordConfirmar'].forEach(function (id) {
        const input = document.getElementById(id);
        input.value = '';
        limpiarInvalido(input);
    });
}

/**
 * Verifica si una contraseña cumple los requisitos mínimos de seguridad
 * (entre 8 y 64 caracteres, al menos una mayúscula y al menos un número).
 * @param {string} password - la contraseña a validar
 * @returns {boolean} true si la contraseña cumple los requisitos; false en caso contrario
 */
function cumpleRequisitosPassword(password) {
    return password.length >= 8 && password.length <= 64 && /[A-Z]/.test(password) && /[0-9]/.test(password);
}

/**
 * Valida en vivo los campos de nueva contraseña y confirmación (requisitos de
 * seguridad, que no sea igual a la contraseña actual, y que coincidan entre sí),
 * marcando los campos inválidos y habilitando/deshabilitando el botón de guardar.
 * @returns {boolean} true si ambos campos son válidos y están completos; false en caso contrario
 */
function validarPasoNuevaPassword() {
    const inputNueva = document.getElementById('passwordNueva');
    const inputConfirmar = document.getElementById('passwordConfirmar');
    const btnGuardar = document.getElementById('btnGuardarNuevaPassword');

    let esValido = true;

    limpiarInvalido(inputNueva);
    limpiarInvalido(inputConfirmar);

    if (inputNueva.value.length > 0 && !cumpleRequisitosPassword(inputNueva.value)) {
        marcarInvalido(
            inputNueva,
            document.getElementById('errorPasswordNueva'),
            'Debe tener entre 8 y 64 caracteres, una mayúscula y un número.'
        );
        esValido = false;
    } else if (inputNueva.value && passwordActualVerificada !== null && inputNueva.value === passwordActualVerificada) {
        marcarInvalido(inputNueva, document.getElementById('errorPasswordNueva'), 'La nueva contraseña no puede ser igual a la actual.');
        esValido = false;
    }

    if (inputConfirmar.value && inputConfirmar.value !== inputNueva.value) {
        marcarInvalido(inputConfirmar, document.getElementById('errorPasswordConfirmar'), 'Las contraseñas no coinciden.');
        esValido = false;
    }

    if (!inputNueva.value || !inputConfirmar.value) {
        esValido = false;
    }

    btnGuardar.disabled = !esValido;
    return esValido;
}

/**
 * Envía la contraseña actual capturada al endpoint de cambio de contraseña
 * para verificarla; si es correcta, avanza al paso de nueva contraseña,
 * y si no, marca el campo como inválido con el mensaje correspondiente.
 * @returns {void}
 */
function verificarPasswordActual() {
    const inputVerificar = document.getElementById('passwordVerificar');
    limpiarInvalido(inputVerificar);

    if (!inputVerificar.value) {
        marcarInvalido(inputVerificar, document.getElementById('errorPasswordVerificar'), 'Ingresa tu contraseña actual.');
        return;
    }

    const btnVerificar = document.getElementById('btnVerificarPassword');
    btnVerificar.disabled = true;

    const params = new URLSearchParams();
    params.append('accion', 'verificarPassword');
    params.append('passwordActual', inputVerificar.value);

    fetch(ENDPOINT_CAMBIAR_PASSWORD, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
        .then(function (resp) { return resp.json(); })
        .then(function (data) {
            if (data.exito) {
                passwordActualVerificada = inputVerificar.value;
                irAPasoNuevaPassword();
            } else {
                marcarInvalido(inputVerificar, document.getElementById('errorPasswordVerificar'), 'Contraseña incorrecta.');
            }
        })
        .catch(function (err) {
            console.error('Error al verificar la contraseña:', err);
            mostrarToast('error', 'Error', 'Ocurrió un error al verificar tu contraseña.');
        })
        .finally(function () {
            btnVerificar.disabled = false;
        });
}

/**
 * Valida el paso de nueva contraseña y, si es válido, envía la contraseña
 * actual verificada junto con la nueva contraseña y su confirmación al
 * endpoint de cambio de contraseña, mostrando el resultado (éxito o error
 * por campo) mediante el modal y notificaciones toast.
 * @returns {void}
 */
function guardarNuevaPassword() {
    if (!validarPasoNuevaPassword()) return;

    const btnGuardar = document.getElementById('btnGuardarNuevaPassword');
    btnGuardar.disabled = true;

    const params = new URLSearchParams();
    params.append('accion', 'cambiarPassword');
    params.append('passwordActual', passwordActualVerificada);
    params.append('passwordNueva', document.getElementById('passwordNueva').value);
    params.append('passwordConfirmar', document.getElementById('passwordConfirmar').value);

    fetch(ENDPOINT_CAMBIAR_PASSWORD, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
        .then(function (resp) { return resp.json(); })
        .then(function (data) {
            if (data.exito) {

                const modal = obtenerModalCambiarPassword();
                if (modal) modal.hide();
                mostrarToast('exito', 'Contraseña actualizada', data.mensaje);
            } else {
                if (data.campo === 'actual') {
                    resetearModalCambiarPassword();
                    marcarInvalido(document.getElementById('passwordVerificar'), document.getElementById('errorPasswordVerificar'), data.mensaje);
                } else if (data.campo === 'confirmar') {
                    marcarInvalido(document.getElementById('passwordConfirmar'), document.getElementById('errorPasswordConfirmar'), data.mensaje);
                } else if (data.campo === 'nueva') {
                    marcarInvalido(document.getElementById('passwordNueva'), document.getElementById('errorPasswordNueva'), data.mensaje);
                } else {
                    mostrarToast('error', 'Error', data.mensaje || 'No se pudo actualizar la contraseña.');
                }
            }
        })
        .catch(function (err) {
            console.error('Error al cambiar la contraseña:', err);
            mostrarToast('error', 'Error', 'Ocurrió un error al actualizar tu contraseña.');
        })
        .finally(function () {
            btnGuardar.disabled = false;
        });
}

document.addEventListener('DOMContentLoaded', function () {
    const btnVerificar = document.getElementById('btnVerificarPassword');
    const btnGuardar = document.getElementById('btnGuardarNuevaPassword');
    const modalEl = document.getElementById('modalCambiarPassword');

    if (btnVerificar) btnVerificar.addEventListener('click', verificarPasswordActual);
    if (btnGuardar) btnGuardar.addEventListener('click', guardarNuevaPassword);

    ['passwordNueva', 'passwordConfirmar'].forEach(function (id) {
        const input = document.getElementById(id);
        if (input) input.addEventListener('input', validarPasoNuevaPassword);
    });

    if (modalEl) {
        modalEl.addEventListener('hidden.bs.modal', resetearModalCambiarPassword);
    }
});