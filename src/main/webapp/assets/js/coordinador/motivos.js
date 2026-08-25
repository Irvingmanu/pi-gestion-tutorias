
var REGEX_MOTIVO = '^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s.,()/-]+$';

document.addEventListener('DOMContentLoaded', function () {
    var inputNuevoMotivo = document.getElementById('nuevoMotivoInput');
    var feedbackNuevoMotivo = document.getElementById('feedbackNuevoMotivo');
    if (inputNuevoMotivo && feedbackNuevoMotivo) {
        inputNuevoMotivo.addEventListener('input', function () {
            inputNuevoMotivo.classList.remove('is-invalid');
            feedbackNuevoMotivo.style.display = 'none';
        });
    }

    var inputAgregarMotivo = document.getElementById('inputAgregarMotivo');
    var feedbackAgregarMotivo = document.getElementById('feedbackAgregarMotivo');
    if (inputAgregarMotivo && feedbackAgregarMotivo) {
        inputAgregarMotivo.addEventListener('input', function () {
            inputAgregarMotivo.classList.remove('is-invalid');
            feedbackAgregarMotivo.style.display = 'none';
        });
    }

    var container = document.getElementById('motivosContainer');
    if (!container) {
        return;
    }

    container.addEventListener('click', function (evento) {
        if (!evento.target.classList.contains('btn-eliminar-motivo')) {
            return;
        }

        var fila = evento.target.closest('.motivo-row');

        mostrarConfirmacion(
            'advertencia',
            '¿Eliminar motivo?',
            '¿Estás seguro de que deseas quitar este motivo de la lista?',
            'Sí, eliminar',
            function () {
                if (fila) {
                    fila.remove();
                }

                if (typeof window.verificarFormularioArea === 'function') {
                    window.verificarFormularioArea();
                }
            }
        );
    });
});

function agregarMotivo() {
    var inputNuevo = document.getElementById('nuevoMotivoInput');
    var container = document.getElementById('motivosContainer');
    var feedback = document.getElementById('feedbackNuevoMotivo');
    if (!inputNuevo || !container) {
        return;
    }

    var valor = inputNuevo.value.trim();

    if (!valor) {
        inputNuevo.classList.remove('is-invalid');
        if (feedback) feedback.style.display = 'none';
        mostrarAlerta('advertencia', 'Campo vacío', 'Escribe un motivo antes de agregarlo.');
        inputNuevo.focus();
        return;
    }

    if (!new RegExp(REGEX_MOTIVO).test(valor)) {
        inputNuevo.classList.add('is-invalid');
        if (feedback) feedback.style.display = 'block';
        inputNuevo.focus();
        return;
    }

    inputNuevo.classList.remove('is-invalid');
    if (feedback) feedback.style.display = 'none';

    var existentes = container.querySelectorAll('input[name="motivos[]"]');
    for (var i = 0; i < existentes.length; i++) {
        if (existentes[i].value.trim().toLowerCase() === valor.toLowerCase()) {
            mostrarAlerta('error', 'Motivo repetido', 'Este motivo ya ha sido agregado a la lista.');
            return;
        }
    }

    var fila = document.createElement('div');
    fila.className = 'd-flex align-items-center gap-2 motivo-row';
    fila.innerHTML =
        '<input type="text" name="motivos[]" class="form-control form-control-figma fs-6" ' +
        'pattern="' + REGEX_MOTIVO + '" title="Solo se permiten letras, números, espacios y . , ( ) / -" required>' +
        '<button type="button" class="btn-cancelar-figma btn-cancelar-figma-sm flex-shrink-0 btn-eliminar-motivo" title="Eliminar motivo">-</button>';

    fila.querySelector('input[name="motivos[]"]').value = valor;

    container.appendChild(fila);

    inputNuevo.value = '';
    inputNuevo.focus();

    if (typeof window.verificarFormularioArea === 'function') {
        window.verificarFormularioArea();
    }
}

function validarAgregarMotivo(evento) {
    var input = evento.target.querySelector('input[name="nuevoMotivo"]');
    if (!input) {
        return true;
    }

    var feedback = document.getElementById('feedbackAgregarMotivo');
    var valor = input.value.trim();

    if (!valor) {
        evento.preventDefault();
        input.classList.remove('is-invalid');
        if (feedback) feedback.style.display = 'none';
        mostrarAlerta('advertencia', 'Campo vacío', 'Escribe un motivo antes de agregarlo.');
        input.focus();
        return false;
    }

    if (!input.checkValidity()) {
        evento.preventDefault();
        input.classList.add('is-invalid');
        if (feedback) feedback.style.display = 'block';
        input.focus();
        return false;
    }

    input.classList.remove('is-invalid');
    if (feedback) feedback.style.display = 'none';

    return true;
}

function toggleEditarMotivo(idMotivo) {
    var lbl = document.getElementById('lbl-motivo-' + idMotivo);
    var form = document.getElementById('form-edit-' + idMotivo);
    if (!lbl || !form) return;

    var estaEditando = !form.classList.contains('d-none');
    if (estaEditando) {
        form.classList.add('d-none');
        lbl.classList.remove('d-none');
    } else {
        form.classList.remove('d-none');
        lbl.classList.add('d-none');
        var input = form.querySelector('input[name="nombreMotivo"]');
        if (input) input.focus();
    }
}

function prepararEliminacionMotivo(idMotivo, idArea) {
    mostrarConfirmacion(
        'critica',
        '¿Eliminar motivo?',
        'Este motivo de canalización se eliminará permanentemente.',
        'Eliminar',
        function () {
            document.getElementById('inputEliminarMotivoIdArea').value = idArea;
            document.getElementById('inputEliminarMotivoId').value = idMotivo;
            document.getElementById('formEliminarMotivo').submit();
        }
    );
}
