/**
 * Muestra modal de confirmación antes de cancelar el formulario.
 */
function confirmarCancelacion() {
    let boton = document.getElementById('btnCancelarFormulario');
    let urlDestino = boton ? boton.dataset.urlCancelar : '/';

    mostrarConfirmacion(
        'advertencia',
        '¿Descartar cambios?',
        'Si sales ahora, perderás todos los datos que has ingresado.',
        'Sí, salir',
        function () {
            window.location.href = urlDestino;
        }
    );
}

/**
 * Prepara y confirma la eliminación del tutor.
 * @param {string|number} nomina
 */
function prepararEliminacion(nomina) {
    // Si la función de la alerta personalizada existe y funciona
    if (typeof mostrarConfirmacion === 'function') {
        mostrarConfirmacion(
            'critica',
            '¿Eliminar tutor?',
            'El tutor se dará de baja y no podrá acceder al sistema, pero se conservará su historial.',
            'Eliminar',
            function () {
                ejecutarSubmitEliminar(nomina);
            }
        );
    } else {
        if (confirm('¿Estás seguro de que deseas eliminar este tutor?')) {
            ejecutarSubmitEliminar(nomina);
        }
    }
}

function ejecutarSubmitEliminar(nomina) {
    const inputNomina = document.getElementById('inputEliminarNomina');
    const formEliminar = document.getElementById('formEliminarTutor');

    if (inputNomina && formEliminar) {
        inputNomina.value = nomina;
        formEliminar.submit();
    } else {
        console.error('No se encontró el formulario oculto formEliminarTutor o inputEliminarNomina');
    }
}

function prepararReactivacion(nomina) {
    mostrarConfirmacion(
        'advertencia',
        '¿Reactivar tutor?',
        'El tutor volverá a aparecer en los listados y podrá acceder al sistema nuevamente.',
        'Reactivar',
        function () {
            ejecutarSubmitReactivar(nomina);
        }
    );
}

function ejecutarSubmitReactivar(nomina) {
    const inputNomina = document.getElementById('inputReactivarNomina');
    const formReactivar = document.getElementById('formReactivarTutor');

    if (inputNomina && formReactivar) {
        inputNomina.value = nomina;
        formReactivar.submit();
    } else {
        console.error('No se encontró el formulario oculto formReactivarTutor o inputReactivarNomina');
    }
}
/**
 * Agrega un rango de horario dinámico a la lista de horarios del tutor.
 */
function agregarHorario() {
    const selectDia = document.getElementById('selectDia');
    const inputDesde = document.getElementById('horarioDesde');
    const inputHasta = document.getElementById('horarioHasta');
    const contenedor = document.getElementById('contenedorHorarios');

    if (!selectDia || !inputDesde || !inputHasta || !contenedor) return;

    const dia = selectDia.value;
    const desde = inputDesde.value;
    const hasta = inputHasta.value;

    if (!dia) {
        mostrarAlerta('error', 'Atención', 'Seleccione un día disponible.');
        return;
    }
    if (!desde || !hasta) {
        mostrarAlerta('error', 'Atención', 'Ingrese un horario de inicio y fin válido.');
        return;
    }
    if (desde >= hasta) {
        mostrarAlerta('error', 'Horario Inválido', 'La hora de fin debe ser posterior a la hora de inicio.');
        return;
    }

    const textoFormateado = `${dia}: ${desde} - ${hasta}`;
    const horariosExistentes = contenedor.querySelectorAll('input[name="horariosDispo"]');
    for (let input of horariosExistentes) {
        if (input.value === textoFormateado) {
            mostrarAlerta('error', 'Horario Repetido', 'Este horario ya ha sido agregado a la lista.');
            return;
        }
    }

    const itemDiv = document.createElement('div');
    itemDiv.className = 'd-flex align-items-center gap-2 mb-2 horario-item';

    itemDiv.innerHTML = `
        <input type="text" class="form-control form-control-figma fs-6" value="${textoFormateado}" readonly>
        <input type="hidden" name="horariosDispo" value="${textoFormateado}">
        <button type="button" class="btn-cancelar-figma btn-cancelar-figma-sm flex-shrink-0" onclick="eliminarHorario(this)" title="Eliminar Horario">-</button>
    `;

    contenedor.appendChild(itemDiv);
    selectDia.value = '';

    if (typeof window.actualizarEstadoGuardar === 'function') {
        window.actualizarEstadoGuardar();
    }
}

/**
 * Confirma y elimina el renglón de horario seleccionado.
 * @param {HTMLElement} btn
 */
function eliminarHorario(btn) {
    mostrarConfirmacion(
        'advertencia',
        '¿Eliminar horario?',
        '¿Estás seguro de que deseas quitar este horario de la lista?',
        'Sí, eliminar',
        function () {
            const item = btn.closest('.horario-item');
            if (item) {
                item.remove();
            }

            if (typeof window.actualizarEstadoGuardar === 'function') {
                window.actualizarEstadoGuardar();
            }
        }
    );
}

/**
 * Ajusta un <input type="time"> de horario a los limites 07:00-21:00, y evita que
 * "Hasta" quede antes (o igual) que "Desde". Usado por formulario-tutor.jsp.
 * @param {HTMLInputElement} input
 */
function validarLimitesHora(input) {
    const minHora = '07:00';
    const maxHora = '21:00';
    if (input.value) {
        if (input.value < minHora) { input.value = minHora; }
        else if (input.value > maxHora) { input.value = maxHora; }
    }

    // "Hasta" nunca puede quedar antes (ni igual) que "Desde": se revisa aqui sin importar
    // cual de los dos inputs disparo el cambio, para que tambien se corrija si el usuario
    // edita "Hasta" directamente a una hora anterior a la ya elegida en "Desde".
    const inputDesde = document.getElementById('horarioDesde');
    const inputHasta = document.getElementById('horarioHasta');
    if (inputDesde && inputHasta && inputDesde.value) {
        inputHasta.min = inputDesde.value;
        if (inputHasta.value && inputHasta.value <= inputDesde.value) {
            inputHasta.value = inputDesde.value;
        }
    }
}

// ==========================================================================
// VALIDACIÓN EN VIVO DE formulario-tutor.jsp. Este archivo tambien se carga en
// gestion-tutores.jsp (listado), que no tiene #formGuardar; el guard de abajo evita
// que este bloque intente enganchar listeners a elementos que no existen ahi.
// ==========================================================================
document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('formGuardar');
    if (!form) {
        return;
    }

    const btnGuardar = document.getElementById('btnGuardar');
    const contenedorHorarios = document.getElementById('contenedorHorarios');
    const inputCorreo = document.getElementById('correo');
    // Ojo: "input, select" a proposito, no "input[required], select[required]". Apellido
    // materno es opcional (sin required) pero SI tiene pattern (letras y espacios); si
    // solo se enganchan los campos required, escribir un caracter invalido ahi nunca
    // marca is-invalid ni bloquea Guardar, aunque el patron lo rechace.
    const inputsValidables = form.querySelectorAll('input, select');

    function tieneHorarios() {
        return contenedorHorarios.querySelectorAll('input[name="horariosDispo"]').length > 0;
    }

    function verificarFormulario() {
        let esValido = true;
        inputsValidables.forEach(function (input) {
            if (!input.checkValidity()) {
                esValido = false;
            }
        });

        if (!tieneHorarios()) {
            esValido = false;
        }

        btnGuardar.disabled = !esValido;
        return esValido;
    }

    inputsValidables.forEach(function (input) {
        input.addEventListener('input', function () {
            if (this.checkValidity()) {
                this.classList.remove('is-invalid');
            } else {
                this.classList.add('is-invalid');
            }
            verificarFormulario();
        });

        input.addEventListener('blur', function () {
            if (!this.checkValidity()) {
                this.classList.add('is-invalid');
            }
            verificarFormulario();
        });
    });

    // Al enviar el formulario, avisar con las alertas ya definidas el motivo exacto
    // por el que no se puede guardar (en vez de solo dejar el boton deshabilitado).
    form.addEventListener('submit', function (evento) {
        if (inputCorreo && !inputCorreo.checkValidity()) {
            evento.preventDefault();
            inputCorreo.classList.add('is-invalid');
            mostrarAlerta('error', 'Correo inválido', 'El correo debe ser un correo institucional válido terminado en @utez.edu.mx.');
            return;
        }

        if (!tieneHorarios()) {
            evento.preventDefault();
            mostrarAlerta('error', 'Horario requerido', 'Debes agregar al menos un horario de atención antes de guardar.');
            return;
        }

        if (!form.checkValidity()) {
            evento.preventDefault();
            mostrarAlerta('error', 'Formulario incompleto', 'Verifica que todos los campos estén completos y correctos.');
        }
    });

    // Expuesta para que agregarHorario()/eliminarHorario() (arriba) puedan re-evaluar
    // el boton al agregar/quitar horarios.
    window.actualizarEstadoGuardar = verificarFormulario;

    verificarFormulario();

    // Toast de error (mensajeError resuelto server-side en TutoresServlet, expuesto
    // via data-attribute; ausente en gestion-tutores.jsp, asi que no hace falta guard aparte).
    const mensajeError = document.body.dataset.mensajeError;
    if (mensajeError) {
        mostrarAlerta('error', 'Error', mensajeError);
    }
});

// ==========================================================================
// AUTOCOMPLETADO DE CORREO INSTITUCIONAL en formulario-tutor.jsp: mientras el
// coordinador escribe Nombres/Apellido paterno, el correo se arma solo como
// primerNombre + primerApellidoPaterno + "@utez.edu.mx" (sin acentos ni espacios).
// Mismo criterio que TutoresServlet#generarCorreoInstitucional (carga masiva por Excel).
// ==========================================================================
document.addEventListener('DOMContentLoaded', function () {
    const inputNombres = document.getElementById('nombres');
    const inputApellidoPaterno = document.getElementById('apellidoPaterno');
    const inputCorreo = document.getElementById('correo');
    if (!inputNombres || !inputApellidoPaterno || !inputCorreo) return;

    const DOMINIO_CORREO = '@utez.edu.mx';
    // Recuerda el ultimo valor que generamos nosotros: si el correo actual ya no
    // coincide con el, significa que el usuario lo edito a mano y dejamos de tocarlo
    // (asi no le pisamos un correo distinto que haya escrito el, ej. en modo edicion).
    let ultimoCorreoGenerado = inputCorreo.value || '';

    function primeraPalabraSinAcentos(texto) {
        const primera = (texto || '').trim().split(/\s+/)[0] || '';
        return primera.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().replace(/[^a-z]/g, '');
    }

    function actualizarCorreoAutomatico() {
        if (inputCorreo.value !== ultimoCorreoGenerado) return;

        const primerNombre = primeraPalabraSinAcentos(inputNombres.value);
        const primerApellido = primeraPalabraSinAcentos(inputApellidoPaterno.value);

        ultimoCorreoGenerado = (primerNombre && primerApellido) ? primerNombre + primerApellido + DOMINIO_CORREO : '';
        inputCorreo.value = ultimoCorreoGenerado;
        // Dispara "input" para que la validacion en vivo (mas arriba en este archivo y
        // en validar-correo.js) reevalue el campo y el boton Guardar.
        inputCorreo.dispatchEvent(new Event('input', { bubbles: true }));
    }

    inputNombres.addEventListener('input', actualizarCorreoAutomatico);
    inputApellidoPaterno.addEventListener('input', actualizarCorreoAutomatico);
});

/**
 * Filtrado en tiempo real de la tabla de tutores.
 */
function filtrarTutores() {
    let inputBuscar = document.getElementById('buscarTutor');
    let tabla = document.getElementById('tablaTutores');
    if (!inputBuscar || !tabla) return;

    let textoBuscar = inputBuscar.value.trim().toLowerCase();
    let mostrarInactivos = document.getElementById('mostrarInactivos');
    let incluirInactivos = mostrarInactivos ? mostrarInactivos.checked : false;
    let selectAcademia = document.getElementById('academiaFiltroTutores');
    let academiaSeleccionada = selectAcademia ? selectAcademia.value : '';

    let filas = document.querySelectorAll('#tablaTutores tr');
    let filasVisibles = 0;

    filas.forEach(function (fila) {
        if (fila.id === 'filaSinResultados') return;

        let nombre = fila.dataset.nombre || '';
        let activo = fila.dataset.activo !== 'N';
        let academia = fila.dataset.academia || '';

        let coincideNombre = nombre.includes(textoBuscar);
        let coincideActivo = activo || incluirInactivos;
        let coincideAcademia = academiaSeleccionada === '' || academia === academiaSeleccionada;

        let coincide = coincideNombre && coincideActivo && coincideAcademia;
        fila.style.display = coincide ? '' : 'none';
        if (coincide) filasVisibles++;
    });

    let filaSinResultados = document.getElementById('filaSinResultados');
    if (filaSinResultados) {
        filaSinResultados.style.display = filasVisibles === 0 ? '' : 'none';
    }
}

document.addEventListener('DOMContentLoaded', function () {
    let buscarTutor = document.getElementById('buscarTutor');
    let btnAgregar = document.getElementById('btnAgregarHorario');
    let mostrarInactivos = document.getElementById('mostrarInactivos');
    let academiaFiltroTutores = document.getElementById('academiaFiltroTutores');

    if (buscarTutor) buscarTutor.addEventListener('input', filtrarTutores);
    if (btnAgregar) btnAgregar.addEventListener('click', agregarHorario);
    if (mostrarInactivos) mostrarInactivos.addEventListener('change', filtrarTutores);
    if (academiaFiltroTutores) academiaFiltroTutores.addEventListener('change', filtrarTutores);

    filtrarTutores();
});

// Toasts/alertas de exito y error via parametros en la URL (?exito=, ?error=)
document.addEventListener('DOMContentLoaded', function () {
    const parametros = new URLSearchParams(window.location.search);
    const exito = parametros.get('exito');

    if (exito) {
        switch (exito) {
            case 'eliminado':
                mostrarToast('exito', '¡Éxito!', 'El tutor fue eliminado correctamente');
                break;
            case 'reactivado':
                mostrarToast('exito', '¡Éxito!', 'El tutor fue reactivado correctamente');
                break;
            case 'guardado':
                mostrarToast('exito', '¡Éxito!', 'El tutor fue registrado correctamente');
                break;
            case 'actualizado':
                mostrarToast('exito', '¡Éxito!', 'El tutor fue actualizado correctamente');
                break;
        }

        window.history.replaceState(null, null, window.location.pathname);
    }

    const error = parametros.get('error');

    if (error) {
        switch (error) {
            case 'tutor_en_uso':
                mostrarAlerta('error', 'No se puede eliminar', 'Este tutor ya tiene asignaciones o sesiones vinculadas en el sistema.');
                break;
            case 'tutor_no_encontrado':
                mostrarAlerta('error', 'Error', 'No se encontró el tutor indicado.');
                break;
            case 'reactivacion_fallida':
                mostrarAlerta('error', 'Error', 'No se pudo reactivar al tutor.');
                break;
            case 'tutor_periodo_activo':
                mostrarAlerta('error', 'No se puede eliminar', 'Este tutor tiene un grupo asignado dentro de un periodo escolar activo. Debes esperar a que el periodo finalice o reasignar el grupo antes de eliminarlo.');
                break;
        }

        window.history.replaceState(null, null, window.location.pathname);
    }
});