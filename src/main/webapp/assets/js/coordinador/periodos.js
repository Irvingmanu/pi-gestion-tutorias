function prepararEliminacionPeriodo(idPeriodo) {
    mostrarConfirmacion(
        'critica',
        '¿Eliminar periodo?',
        'El periodo dejará de estar disponible para nuevas asignaciones y registros de tutoría.',
        'Eliminar',
        function () {
            document.getElementById('inputEliminarPeriodo').value = idPeriodo;
            document.getElementById('formEliminarPeriodo').submit();
        }
    );
}

function prepararReactivacionPeriodo(idPeriodo) {
    mostrarConfirmacion(
        'advertencia',
        '¿Reactivar periodo?',
        'El periodo volverá a estar disponible para asignaciones y registros de tutoría.',
        'Reactivar',
        function () {
            document.getElementById('inputReactivarPeriodo').value = idPeriodo;
            document.getElementById('formReactivarPeriodo').submit();
        }
    );
}

function prepararEdicionPeriodo(boton) {
    document.getElementById('accionPeriodo').value = 'editar';
    document.getElementById('idPeriodoEdit').value = boton.dataset.id;
    document.getElementById('nombre').value = boton.dataset.nombre;
    document.getElementById('fechaInicio').value = boton.dataset.fechaInicio;
    document.getElementById('fechaFin').value = boton.dataset.fechaFin;
    document.getElementById('asistenciasGrupales').value = boton.dataset.objetivo;

    document.getElementById('tituloFormularioPeriodo').textContent = 'Editar periodo';
    document.getElementById('btnGuardar').textContent = 'Guardar Cambios';
    document.getElementById('btnCancelarEdicionPeriodo').classList.remove('d-none');

    const tabNuevo = document.getElementById('tab-nuevo-periodo-btn');
    if (tabNuevo && window.bootstrap) new bootstrap.Tab(tabNuevo).show();

    document.getElementById('asistenciasGrupales').dispatchEvent(new Event('input'));
}

function cancelarEdicionPeriodo() {
    document.getElementById('accionPeriodo').value = '';
    document.getElementById('idPeriodoEdit').value = '';
    document.getElementById('formGuardar').reset();

    document.getElementById('tituloFormularioPeriodo').textContent = 'Registrar nuevo periodo';
    document.getElementById('btnGuardar').textContent = 'Guardar';
    document.getElementById('btnCancelarEdicionPeriodo').classList.add('d-none');

    document.getElementById('asistenciasGrupales').dispatchEvent(new Event('input'));
}

function filtrarPeriodos() {
    const mostrarInactivos = document.getElementById('mostrarInactivos');
    const incluirInactivos = mostrarInactivos ? mostrarInactivos.checked : false;
    const filas = document.querySelectorAll('#tablaPeriodos tbody tr');

    filas.forEach(function (fila) {
        const activo = fila.dataset.activo !== 'N';
        fila.style.display = (activo || incluirInactivos) ? '' : 'none';
    });
}

document.addEventListener('DOMContentLoaded', function () {
    const mostrarInactivos = document.getElementById('mostrarInactivos');
    if (mostrarInactivos) mostrarInactivos.addEventListener('change', filtrarPeriodos);
    filtrarPeriodos();

    const form = document.getElementById('formGuardar');
    if (form) {
        const btnGuardar = document.getElementById('btnGuardar');
        const inputNombre = document.getElementById('nombre');
        const inputInicio = document.getElementById('fechaInicio');
        const inputFin = document.getElementById('fechaFin');
        const inputObjetivo = document.getElementById('asistenciasGrupales');

        function verificarFormulario() {
            let esValido = inputNombre.value.trim() !== '' && inputInicio.value !== '' && inputFin.value !== ''
                && inputObjetivo.value !== '' && Number(inputObjetivo.value) >= 0;

            if (inputInicio.value && inputFin.value && inputFin.value <= inputInicio.value) {
                esValido = false;
                inputFin.classList.add('is-invalid');
            } else if (inputFin.value) {
                inputFin.classList.remove('is-invalid');
            }

            btnGuardar.disabled = !esValido;
        }

        [inputNombre, inputInicio, inputFin, inputObjetivo].forEach(function (input) {
            input.addEventListener('input', verificarFormulario);
            input.addEventListener('change', verificarFormulario);
        });

        verificarFormulario();
    }

    // Toasts/alertas de exito y error via parametros en la URL
    const parametros = new URLSearchParams(window.location.search);
    const exito = parametros.get('exito');
    if (exito === 'guardado') {
        mostrarToast('exito', '¡Éxito!', 'El periodo escolar fue registrado correctamente');
    } else if (exito === 'eliminado') {
        mostrarToast('exito', '¡Éxito!', 'El periodo fue eliminado correctamente');
    } else if (exito === 'reactivado') {
        mostrarToast('exito', '¡Éxito!', 'El periodo fue reactivado correctamente');
    } else if (exito === 'editado') {
        mostrarToast('exito', '¡Éxito!', 'El periodo escolar fue actualizado correctamente');
    }

    const error = parametros.get('error');
    if (error) {
        switch (error) {
            case 'campos_incompletos':
                mostrarAlerta('error', 'Atención', 'Completa todos los campos del periodo.');
                break;
            case 'fechas_invalidas':
                mostrarAlerta('error', 'Error', 'Las fechas capturadas no son válidas.');
                break;
            case 'rango_invalido':
                mostrarAlerta('error', 'Rango inválido', 'La fecha de fin debe ser posterior a la fecha de inicio.');
                break;
            case 'nombre_duplicado':
                mostrarAlerta('error', 'Nombre repetido', 'Ya existe un periodo registrado con ese nombre.');
                break;
            case 'objetivo_invalido':
                mostrarAlerta('error', 'Objetivo inválido', 'El objetivo de tutorías grupales debe ser un número igual o mayor a 0.');
                break;
            case 'periodo_en_uso':
                mostrarAlerta('error', 'No se puede eliminar', 'Este periodo ya tiene asignaciones vinculadas.');
                break;
            case 'registro_fallido':
                mostrarAlerta('error', 'Error', 'No se pudo registrar el periodo.');
                break;
        }
    }

    if (exito || error) {
        window.history.replaceState(null, null, window.location.pathname);
    }
});