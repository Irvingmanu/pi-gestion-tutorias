// Modal "Cambiar contraseña", reutilizable para Coordinador/Tutor/Alumno: dos pasos,
// primero verifica la contraseña actual contra el servidor y luego permite capturar/
// confirmar la nueva. Al guardar con éxito, el modal se cierra solo.
// Requiere que la vista defina antes:
//   const CONTEXT_PATH = "<context-path>";
//   const ENDPOINT_CAMBIAR_PASSWORD = "<context-path>/<servlet-del-rol>";
// y que ya esten cargados alertas.js y bootstrap.js.

let passwordActualVerificada = null;

function obtenerModalCambiarPassword() {
    const el = document.getElementById('modalCambiarPassword');
    if (!el) {
        console.error('No se encontró el elemento #modalCambiarPassword en el DOM.');
        return null;
    }
    // getOrCreateInstance reutiliza la instancia que Bootstrap ya creó al abrir
    // el modal (por data-bs-toggle="modal"). Si aquí hiciéramos "new bootstrap.Modal(el)"
    // se crearía una instancia nueva con _isShown = false, y su hide() no haría nada.
    return bootstrap.Modal.getOrCreateInstance(el);
}

function marcarInvalido(input, errorEl, mensaje) {
    input.classList.add('is-invalid');
    if (errorEl && mensaje) errorEl.textContent = mensaje;
}

function limpiarInvalido(input) {
    input.classList.remove('is-invalid');
}

function irAPasoNuevaPassword() {
    document.getElementById('pasoVerificarPassword').classList.add('d-none');
    document.getElementById('pasoNuevaPassword').classList.remove('d-none');
    document.getElementById('btnVerificarPassword').classList.add('d-none');
    document.getElementById('btnGuardarNuevaPassword').classList.remove('d-none');
    document.getElementById('passwordNueva').focus();
}

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
            'Debe tener al menos 8 caracteres, una mayúscula y un número.'
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

/** Al menos 8 caracteres, una mayúscula y un número (debe coincidir con CambioPasswordUtil.java). */
function cumpleRequisitosPassword(password) {
    return password.length >= 8 && /[A-Z]/.test(password) && /[0-9]/.test(password);
}

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
                // Cierra el modal automáticamente al guardar con éxito.
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