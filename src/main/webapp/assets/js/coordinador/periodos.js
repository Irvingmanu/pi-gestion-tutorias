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

    // Los campos se llenaron por JS directo (input.value = ...), lo que no
    // dispara 'input'/'change'; se revalida todo el formulario a mano para
    // que el marcado visual (rojo/verde) y el estado del boton Guardar
    // reflejen los valores que se acaban de cargar.
    if (window.verificarFormularioPeriodo) window.verificarFormularioPeriodo();
}

function cancelarEdicionPeriodo() {
    document.getElementById('accionPeriodo').value = '';
    document.getElementById('idPeriodoEdit').value = '';
    document.getElementById('formGuardar').reset();

    document.getElementById('tituloFormularioPeriodo').textContent = 'Registrar nuevo periodo';
    document.getElementById('btnGuardar').textContent = 'Guardar';
    document.getElementById('btnCancelarEdicionPeriodo').classList.add('d-none');

    // form.reset() tampoco dispara 'input'/'change' en todos los navegadores
    // de forma confiable; se revalida a mano para limpiar el marcado rojo.
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

    // ==========================================================================
    // VALIDACIÓN EN VIVO (misma logica que formulario-area.js / formulario-alumno.js):
    // marca borde rojo + muestra el <div class="invalid-feedback"> de cada campo.
    // ==========================================================================
    const MENSAJE_CAMPO_OBLIGATORIO = 'Este campo es obligatorio.';

    function obtenerFeedback(input) {
        if (input.nextElementSibling && input.nextElementSibling.classList.contains('invalid-feedback')) {
            return input.nextElementSibling;
        }
        return input.parentElement?.querySelector('.invalid-feedback') || null;
    }

    // Fecha fin invalida (anterior o igual a fecha inicio): se integra al
    // checkValidity() nativo via setCustomValidity, en vez de un classList
    // suelto, para que entre al mismo flujo de marcarValidez() de abajo.
    function actualizarValidezRango() {
        if (inputInicio.value && inputFin.value && inputFin.value <= inputInicio.value) {
            inputFin.setCustomValidity('rango_invalido');
        } else {
            inputFin.setCustomValidity('');
        }
    }

    // Marca (o desmarca) un input individual como invalido: borde rojo +
    // mensaje visible. Si esta vacio (valueMissing) usa data-msg-requerido;
    // para cualquier otro tipo de invalidez (pattern, o el rango de fechas
    // via setCustomValidity) se usa el mensaje que ya trae el HTML en el
    // .invalid-feedback (guardado la primera vez para no perderlo).
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

        feedback.textContent = input.validity.valueMissing
            ? (input.dataset.msgRequerido || MENSAJE_CAMPO_OBLIGATORIO)
            : feedback.dataset.msgPatron;

        feedback.style.display = 'block';
    }

    function verificarFormulario() {
        actualizarValidezRango();

        let esValido = true;
        inputsValidables.forEach(function (input) {
            marcarValidez(input);
            if (!input.checkValidity()) {
                esValido = false;
            }
        });

        btnGuardar.disabled = !esValido;
    }

    // Expuesta para que prepararEdicionPeriodo() y cancelarEdicionPeriodo()
    // puedan pedir una revalidacion manual tras llenar/limpiar el form por JS.
    window.verificarFormularioPeriodo = verificarFormulario;

    inputsValidables.forEach(function (input) {
        input.addEventListener('input', verificarFormulario);
        input.addEventListener('change', verificarFormulario);
    });

    // Verificación inicial (ej. al cargar la pantalla, tab "Nuevo Periodo" vacío).
    verificarFormulario();

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