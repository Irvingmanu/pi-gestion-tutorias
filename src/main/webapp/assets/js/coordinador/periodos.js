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

    if (window.verificarFormularioPeriodo) window.verificarFormularioPeriodo();
}

function cancelarEdicionPeriodo() {
    document.getElementById('accionPeriodo').value = '';
    document.getElementById('idPeriodoEdit').value = '';
    document.getElementById('formGuardar').reset();

    document.getElementById('tituloFormularioPeriodo').textContent = 'Registrar nuevo periodo';
    document.getElementById('btnGuardar').textContent = 'Guardar';
    document.getElementById('btnCancelarEdicionPeriodo').classList.add('d-none');

    if (window.verificarFormularioPeriodo) window.verificarFormularioPeriodo();
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
    if (!form) {
        return;
    }

    const btnGuardar = document.getElementById('btnGuardar');
    const inputNombre = document.getElementById('nombre');
    const inputInicio = document.getElementById('fechaInicio');
    const inputFin = document.getElementById('fechaFin');
    const inputObjetivo = document.getElementById('asistenciasGrupales');
    const inputsValidables = [inputNombre, inputInicio, inputFin, inputObjetivo];

    const MENSAJE_CAMPO_OBLIGATORIO = 'Este campo es obligatorio.';
    const MENSAJE_DURACION_INVALIDA = 'El periodo escolar debe durar entre 3 y 4 meses (91 a 123 días).';

    const MESES_PERMITIDOS = [1, 5, 9];
    const NOMBRES_INICIO = {1: 'Enero - Abril', 5: 'Mayo - Agosto', 9: 'Septiembre - Diciembre'};

    function mesValido(fechaISO) {
        return MESES_PERMITIDOS.includes(parseInt(fechaISO.split('-')[1], 10));
    }

    function obtenerFeedback(input) {
        if (input.nextElementSibling && input.nextElementSibling.classList.contains('invalid-feedback')) {
            return input.nextElementSibling;
        }
        return input.parentElement?.querySelector('.invalid-feedback') || null;
    }

    function actualizarValidezFechas() {
        if (inputInicio.value && !mesValido(inputInicio.value)) {
            inputInicio.setCustomValidity('mes_invalido');
        } else {
            inputInicio.setCustomValidity('');
        }

        inputFin.dataset.motivoInvalido = '';
        if (inputInicio.value && inputFin.value && inputFin.value <= inputInicio.value) {
            inputFin.setCustomValidity('rango_invalido');
            inputFin.dataset.motivoInvalido = 'rango';
        } else if (inputInicio.value && inputFin.value) {
            const msPorDia = 24 * 60 * 60 * 1000;
            const dias = Math.round((new Date(inputFin.value + 'T00:00:00') - new Date(inputInicio.value + 'T00:00:00')) / msPorDia);
            if (dias <= 90 || dias > 123) {
                inputFin.setCustomValidity('duracion_invalida');
                inputFin.dataset.motivoInvalido = 'duracion';
            } else {
                inputFin.setCustomValidity('');
            }
        } else {
            inputFin.setCustomValidity('');
        }
    }

    function actualizarNombreAutomatico() {
        if (inputInicio.value && mesValido(inputInicio.value)) {
            const [anio, mes] = inputInicio.value.split('-');
            inputNombre.value = NOMBRES_INICIO[parseInt(mes, 10)] + ' ' + anio;
        }
    }

    function marcarValidez(input) {
        const feedback = obtenerFeedback(input);

        if (input.checkValidity()) {
            input.classList.remove('is-invalid');
            if (feedback) feedback.style.display = 'none';
            return;
        }

        input.classList.add('is-invalid');
        if (!feedback) {
            return;
        }

        if (feedback.dataset.msgPatron === undefined) {
            feedback.dataset.msgPatron = feedback.textContent.trim();
        }

        let mensaje;
        if (input.validity.valueMissing) {
            mensaje = input.dataset.msgRequerido || MENSAJE_CAMPO_OBLIGATORIO;
        } else if (input === inputFin && input.dataset.motivoInvalido === 'duracion') {
            mensaje = MENSAJE_DURACION_INVALIDA;
        } else {
            mensaje = feedback.dataset.msgPatron;
        }

        feedback.textContent = mensaje;
        feedback.style.display = 'block';
    }

    function verificarFormulario() {
        actualizarValidezFechas();
        actualizarNombreAutomatico();

        let esValido = true;
        inputsValidables.forEach(function (input) {
            marcarValidez(input);
            if (!input.checkValidity()) {
                esValido = false;
            }
        });

        btnGuardar.disabled = !esValido;
    }

    window.verificarFormularioPeriodo = verificarFormulario;

    inputsValidables.forEach(function (input) {
        input.addEventListener('input', verificarFormulario);
        input.addEventListener('change', verificarFormulario);
    });

    inputInicio.addEventListener('change', function () {
        if (this.value && !mesValido(this.value)) {
            this.value = '';
            verificarFormulario();
        }
    });

    verificarFormulario();

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
            case 'mes_invalido':
                mostrarAlerta('error', 'Mes de inicio inválido', 'Los cuatrimestres solo pueden iniciar en Enero, Mayo o Septiembre.');
                break;
            case 'duracion_invalida':
                mostrarAlerta('error', 'Duración inválida', 'El periodo escolar debe durar entre 3 y 4 meses (91 a 123 días).');
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