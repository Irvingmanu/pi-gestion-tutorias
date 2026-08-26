/**
 * Gestiona los motivos de canalización de un área de apoyo: agregarlos en el
 * formulario (localmente o vía API), editarlos y eliminarlos en línea con
 * confirmación, tanto en el formulario de creación como en el de edición del área.
 * @author Irvingmanu
 * @date 2026-07-23
 */
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

    var listaMotivos = document.getElementById('listaMotivosArea');
    if (listaMotivos) {
        listaMotivos.querySelectorAll('form[id^="form-edit-"]').forEach(function (form) {
            form.addEventListener('submit', function (evento) {
                evento.preventDefault();
                guardarEdicionMotivo(form);
            });
        });

        var formAgregarMotivoEdicion = document.getElementById('formAgregarMotivo');
        if (formAgregarMotivoEdicion) {
            formAgregarMotivoEdicion.addEventListener('submit', function (evento) {
                evento.preventDefault();
                crearMotivoAsync(formAgregarMotivoEdicion);
            });
        }
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

/**
 * Agrega localmente una fila de motivo al formulario de creación de área
 * (sin llamar al servidor), validando que no esté vacío, que cumpla el
 * patrón permitido y que no esté repetido en la lista actual.
 * @returns {void}
 */
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

/**
 * Crea un nuevo motivo de canalización para un área ya existente mediante
 * una petición POST al servidor, y agrega la fila correspondiente a la lista
 * si la operación fue exitosa.
 * @param {HTMLFormElement} form - el formulario con los campos idArea y nuevoMotivo
 * @returns {void}
 */
function crearMotivoAsync(form) {
    var idArea = form.querySelector('input[name="idArea"]').value;
    var input = form.querySelector('input[name="nuevoMotivo"]');
    var nombreMotivo = input.value.trim();

    if (!nombreMotivo) {
        mostrarAlerta('advertencia', 'Campo vacío', 'Escribe un motivo antes de agregarlo.');
        input.focus();
        return;
    }

    if (!new RegExp(REGEX_MOTIVO).test(nombreMotivo)) {
        mostrarAlerta('error', 'Motivo inválido', 'Solo se permiten letras, números, espacios y . , ( ) / -');
        input.focus();
        return;
    }

    var contextPath = document.body.dataset.contextPath || '';

    fetch(contextPath + '/areas-apoyo', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ idArea: idArea, nombreMotivo: nombreMotivo })
    })
        .then(function (resp) { return resp.json(); })
        .then(function (data) {
            if (data.exito) {
                agregarFilaMotivo(data.idMotivo, idArea, data.nombreMotivo);
                input.value = '';
                input.focus();
            } else {
                mostrarAlerta('error', 'No se pudo agregar', data.mensaje === 'area_bloqueada'
                    ? 'El área ya cuenta con alumnos canalizados.' : 'Verifica el motivo e intenta de nuevo.');
            }
        })
        .catch(function () {
            mostrarAlerta('error', 'Error de conexión', 'No se pudo contactar al servidor.');
        });
}

/**
 * Construye y agrega a la lista la fila de un motivo existente, con su
 * etiqueta de solo lectura, su formulario de edición inline oculto y los
 * botones de editar/eliminar.
 * @param {number|string} idMotivo - el id del motivo
 * @param {number|string} idArea - el id del área de apoyo a la que pertenece
 * @param {string} nombreMotivo - el nombre del motivo a mostrar
 * @returns {void}
 */
function agregarFilaMotivo(idMotivo, idArea, nombreMotivo) {
    var lista = document.getElementById('listaMotivosArea');
    if (!lista) {
        return;
    }

    var vacio = lista.querySelector('.text-muted');
    if (vacio) {
        vacio.remove();
    }

    var contextPath = document.body.dataset.contextPath || '';

    var fila = document.createElement('div');
    fila.className = 'd-flex align-items-center gap-2 motivo-item';
    fila.innerHTML =
        '<span id="lbl-motivo-' + idMotivo + '" class="flex-grow-1"></span>' +
        '<form id="form-edit-' + idMotivo + '" class="d-none d-flex gap-2 flex-grow-1 needs-validation" novalidate>' +
            '<input type="hidden" name="idArea" value="' + idArea + '">' +
            '<input type="hidden" name="idMotivo" value="' + idMotivo + '">' +
            '<div class="w-100 position-relative">' +
                '<input type="text" name="nombreMotivo" class="form-control form-control-figma w-100 fs-6" ' +
                'pattern="' + REGEX_MOTIVO + '" title="Solo se permiten letras, números, espacios y . , ( ) / -" required>' +
            '</div>' +
            '<button type="submit" class="btn-figma btn-figma-sm flex-shrink-0" title="Guardar motivo" disabled>' +
                '<img src="' + contextPath + '/assets/img/coordinador/check.png" width="16" alt="Guardar">' +
            '</button>' +
        '</form>' +
        '<div class="d-flex gap-2 flex-shrink-0">' +
            '<button type="button" class="btn-figma btn-figma-sm" title="Editar motivo" onclick="toggleEditarMotivo(' + idMotivo + ')">' +
                '<img src="' + contextPath + '/assets/img/coordinador/editar.png" width="16" alt="Editar">' +
            '</button>' +
            '<button type="button" class="btn-cancelar-figma btn-cancelar-figma-sm" title="Eliminar motivo" onclick="prepararEliminacionMotivo(' + idMotivo + ', ' + idArea + ')">' +
                '<img src="' + contextPath + '/assets/img/coordinador/eliminar.png" width="16" alt="Eliminar">' +
            '</button>' +
        '</div>';

    fila.querySelector('#lbl-motivo-' + idMotivo).textContent = nombreMotivo;

    lista.appendChild(fila);

    var nuevoForm = fila.querySelector('#form-edit-' + idMotivo);
    nuevoForm.addEventListener('submit', function (evento) {
        evento.preventDefault();
        guardarEdicionMotivo(nuevoForm);
    });

    if (typeof window.configurarValidacionFormulario === 'function') {
        window.configurarValidacionFormulario(nuevoForm);
    }
}

/**
 * Alterna entre mostrar la etiqueta de solo lectura o el formulario de
 * edición inline de un motivo existente.
 * @param {number|string} idMotivo - el id del motivo a alternar
 * @returns {void}
 */
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

/**
 * Guarda la edición de un motivo existente mediante una petición PUT al
 * servidor, actualizando la etiqueta y volviendo al modo lectura si la
 * operación fue exitosa.
 * @param {HTMLFormElement} form - el formulario de edición con idMotivo, idArea y nombreMotivo
 * @returns {void}
 */
function guardarEdicionMotivo(form) {
    var idMotivo = form.querySelector('input[name="idMotivo"]').value;
    var idArea = form.querySelector('input[name="idArea"]').value;
    var input = form.querySelector('input[name="nombreMotivo"]');
    var nombreMotivo = input.value.trim();

    if (!new RegExp(REGEX_MOTIVO).test(nombreMotivo)) {
        mostrarAlerta('error', 'Motivo inválido', 'Solo se permiten letras, números, espacios y . , ( ) / -');
        return;
    }

    var contextPath = document.body.dataset.contextPath || '';

    fetch(contextPath + '/areas-apoyo', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ idArea: idArea, idMotivo: idMotivo, nombreMotivo: nombreMotivo })
    })
        .then(function (resp) { return resp.json(); })
        .then(function (data) {
            if (data.exito) {
                document.getElementById('lbl-motivo-' + idMotivo).textContent = nombreMotivo;
                toggleEditarMotivo(idMotivo);
            } else {
                mostrarAlerta('error', 'No se pudo actualizar', data.mensaje === 'area_bloqueada'
                    ? 'El área ya cuenta con alumnos canalizados.' : 'Verifica el motivo e intenta de nuevo.');
            }
        })
        .catch(function () {
            mostrarAlerta('error', 'Error de conexión', 'No se pudo contactar al servidor.');
        });
}

/**
 * Pide confirmación crítica antes de eliminar un motivo de canalización y,
 * si se confirma, lo elimina mediante una petición DELETE al servidor,
 * quitando su fila de la lista si la operación fue exitosa.
 * @param {number|string} idMotivo - el id del motivo a eliminar
 * @param {number|string} idArea - el id del área de apoyo a la que pertenece
 * @returns {void}
 */
function prepararEliminacionMotivo(idMotivo, idArea) {
    mostrarConfirmacion(
        'critica',
        '¿Eliminar motivo?',
        'Este motivo de canalización se eliminará permanentemente.',
        'Eliminar',
        function () {
            var contextPath = document.body.dataset.contextPath || '';

            fetch(contextPath + '/areas-apoyo', {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ idArea: idArea, idMotivo: idMotivo })
            })
                .then(function (resp) { return resp.json(); })
                .then(function (data) {
                    if (data.exito) {
                        var etiqueta = document.getElementById('lbl-motivo-' + idMotivo);
                        var fila = etiqueta ? etiqueta.closest('.motivo-item') : null;
                        if (fila) fila.remove();
                    } else {
                        mostrarAlerta('error', 'No se pudo eliminar', data.mensaje === 'area_bloqueada'
                            ? 'El área ya cuenta con alumnos canalizados.' : 'Este motivo está en uso.');
                    }
                })
                .catch(function () {
                    mostrarAlerta('error', 'Error de conexión', 'No se pudo contactar al servidor.');
                });
        }
    );
}
