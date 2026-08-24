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
    const MENSAJE_DURACION_INVALIDA = 'El periodo escolar debe durar entre 3 y 4 meses (91 a 123 días).';

    // Modelo estricto de 3 cuatrimestres: un periodo solo puede arrancar en uno de estos
    // meses (1=Enero, 5=Mayo, 9=Septiembre). NOMBRES_INICIO da el nombre que le corresponde
    // a cada uno para el autocompletado de "Nombre del periodo" (ver
    // actualizarNombreAutomatico() mas abajo); debe coincidir exactamente con
    // PeriodoEscolarServlet#NOMBRES_MES_INICIO en el backend.
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

    // Candado de mes (Fecha de inicio) + rango/duracion (Fecha de fin): se integran al
    // checkValidity() nativo via setCustomValidity, en vez de un classList suelto, para que
    // entren al mismo flujo de marcarValidez() de abajo. dataset.motivoInvalido distingue,
    // para fechaFin, si el problema es el ORDEN (fin <= inicio) o la DURACION (fuera de
    // 91-123 dias) — cada uno tiene su propio mensaje (ver marcarValidez).
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

    // Autocompleta "Nombre del periodo" a partir de la Fecha de inicio (readonly, el
    // coordinador ya no lo captura a mano). Solo SOBRESCRIBE el valor cuando el mes elegido
    // es valido: si esta vacio o (en edicion) trae un mes fuera de regla de un periodo
    // creado ANTES de esta validacion, se deja el nombre tal cual esta (nunca se borra solo
    // que se este viendo un valor invalido en fecha de inicio).
    function actualizarNombreAutomatico() {
        if (inputInicio.value && mesValido(inputInicio.value)) {
            const [anio, mes] = inputInicio.value.split('-');
            inputNombre.value = NOMBRES_INICIO[parseInt(mes, 10)] + ' ' + anio;
        }
    }

    // Marca (o desmarca) un input individual como invalido: borde rojo +
    // mensaje visible. Si esta vacio (valueMissing) usa data-msg-requerido;
    // para el rango/duracion de fechaFin usa el mensaje que corresponda segun
    // dataset.motivoInvalido; para cualquier otro tipo de invalidez (pattern,
    // o el candado de mes de fechaInicio) se usa el mensaje que ya trae el
    // HTML en el .invalid-feedback (guardado la primera vez para no perderlo).
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

    // Expuesta para que prepararEdicionPeriodo() y cancelarEdicionPeriodo()
    // puedan pedir una revalidacion manual tras llenar/limpiar el form por JS.
    window.verificarFormularioPeriodo = verificarFormulario;

    inputsValidables.forEach(function (input) {
        input.addEventListener('input', verificarFormulario);
        input.addEventListener('change', verificarFormulario);
    });

    // Candado del mes de inicio: si el coordinador elige (via el selector nativo de fecha)
    // un mes que no sea Enero/Mayo/Septiembre, el campo se limpia solo de inmediato. Va
    // aparte del listener generico de arriba (que solo marca en rojo sin tocar el valor)
    // porque este SI debe modificar el <input>, y solo cuando el cambio viene de una
    // eleccion activa del coordinador — nunca al precargar el formulario en modo edicion
    // (ahi actualizarValidezFechas ya se encarga de mostrarlo en rojo sin borrar nada).
    inputInicio.addEventListener('change', function () {
        if (this.value && !mesValido(this.value)) {
            this.value = '';
            verificarFormulario();
        }
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