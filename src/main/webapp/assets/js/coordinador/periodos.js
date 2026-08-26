/**
 * Controla la vista de Gestión de Periodos del coordinador: confirmación de baja
 * y reactivación de periodos escolares, precarga del formulario en modo edición,
 * filtrado de la tabla por estado activo/inactivo, y validación en vivo del
 * formulario de alta/edición (mes de inicio permitido, rango y duración de fechas,
 * nombre automático) junto con los mensajes de éxito/error tras el submit.
 * @author 20253ds074-art
 * @date 2026-08-10
 */

/**
 * Solicita confirmación antes de dar de baja (eliminar lógicamente) un periodo escolar
 * y, si se confirma, envía el formulario oculto de eliminación.
 * @param {number|string} idPeriodo el id del periodo a eliminar
 * @returns {void}
 */
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

/**
 * Solicita confirmación antes de reactivar un periodo escolar dado de baja
 * y, si se confirma, envía el formulario oculto de reactivación.
 * @param {number|string} idPeriodo el id del periodo a reactivar
 * @returns {void}
 */
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

/**
 * Precarga el formulario de periodo en modo edición con los datos del periodo
 * seleccionado, tomados de los atributos data-* del botón, y cambia a la pestaña del formulario.
 * @param {HTMLElement} boton el botón "editar" que disparó la acción, con los datos del periodo en su dataset
 * @returns {void}
 */
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

/**
 * Cancela el modo edición del formulario de periodo, limpiándolo y devolviéndolo
 * a su estado de alta de un nuevo periodo.
 * @returns {void}
 */
function cancelarEdicionPeriodo() {
    document.getElementById('accionPeriodo').value = '';
    document.getElementById('idPeriodoEdit').value = '';
    document.getElementById('formGuardar').reset();

    document.getElementById('tituloFormularioPeriodo').textContent = 'Registrar nuevo periodo';
    document.getElementById('btnGuardar').textContent = 'Guardar';
    document.getElementById('btnCancelarEdicionPeriodo').classList.add('d-none');

    if (window.verificarFormularioPeriodo) window.verificarFormularioPeriodo();
}

/**
 * Muestra u oculta las filas de periodos inactivos en la tabla según el estado
 * del checkbox "mostrar inactivos".
 * @returns {void}
 */
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

    /**
     * Determina si el mes de una fecha ISO corresponde a uno de los meses de inicio
     * de cuatrimestre permitidos (enero, mayo o septiembre).
     * @param {string} fechaISO fecha en formato ISO (yyyy-MM-dd)
     * @returns {boolean} true si el mes es válido como inicio de periodo
     */
    function mesValido(fechaISO) {
        return MESES_PERMITIDOS.includes(parseInt(fechaISO.split('-')[1], 10));
    }

    /**
     * Ubica el elemento de retroalimentación de validación (.invalid-feedback) asociado a un input.
     * @param {HTMLInputElement} input el campo del cual buscar su feedback
     * @returns {HTMLElement|null} el elemento de feedback encontrado, o null si no existe
     */
    function obtenerFeedback(input) {
        if (input.nextElementSibling && input.nextElementSibling.classList.contains('invalid-feedback')) {
            return input.nextElementSibling;
        }
        return input.parentElement?.querySelector('.invalid-feedback') || null;
    }

    /**
     * Valida las fechas de inicio y fin del periodo: mes de inicio permitido, que el fin
     * sea posterior al inicio y que la duración esté entre 91 y 123 días, marcando la
     * validez personalizada (setCustomValidity) de cada input según corresponda.
     * @returns {void}
     */
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

    /**
     * Genera automáticamente el nombre del periodo (ej. "Enero - Abril 2027") a partir
     * de la fecha de inicio capturada, cuando su mes es válido.
     * @returns {void}
     */
    function actualizarNombreAutomatico() {
        if (inputInicio.value && mesValido(inputInicio.value)) {
            const [anio, mes] = inputInicio.value.split('-');
            inputNombre.value = NOMBRES_INICIO[parseInt(mes, 10)] + ' ' + anio;
        }
    }

    /**
     * Aplica el estilo visual de validación (is-invalid) y el mensaje de error
     * correspondiente al feedback de un campo del formulario según su estado de validez.
     * @param {HTMLInputElement} input el campo a marcar
     * @returns {void}
     */
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

    /**
     * Revalida por completo el formulario de periodo (fechas, nombre automático y
     * campos obligatorios) y habilita o deshabilita el botón de guardar según el resultado.
     * @returns {void}
     */
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