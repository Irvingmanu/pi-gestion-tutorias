/**
 * Controla las vistas de Gestión de Tutores y el formulario de tutor del coordinador:
 * confirmación de cancelación/baja/reactivación, gestión dinámica de horarios de
 * atención, generación automática del correo institucional, validación en vivo del
 * formulario, filtrado de la tabla de tutores, carga masiva por Excel con su modal,
 * y los mensajes de éxito/error tras el submit.
 * @author Irvingmanu
 * @date 2026-07-23
 */

/**
 * Solicita confirmación antes de descartar los cambios del formulario y redirige
 * a la URL de cancelación indicada en el botón.
 * @returns {void}
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
 * Solicita confirmación (o fallback con confirm nativo) antes de dar de baja a un tutor.
 * @param {string} nomina la nómina del tutor a eliminar
 * @returns {void}
 */
function prepararEliminacion(nomina) {
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

/**
 * Coloca la nómina en el formulario oculto de eliminación y lo envía.
 * @param {string} nomina la nómina del tutor a eliminar
 * @returns {void}
 */
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

/**
 * Solicita confirmación antes de reactivar a un tutor dado de baja.
 * @param {string} nomina la nómina del tutor a reactivar
 * @returns {void}
 */
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

/**
 * Coloca la nómina en el formulario oculto de reactivación y lo envía.
 * @param {string} nomina la nómina del tutor a reactivar
 * @returns {void}
 */
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
 * Valida y agrega un nuevo horario de atención (día, hora de inicio y fin) a la
 * lista de horarios del formulario de tutor, evitando duplicados.
 * @returns {void}
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
 * Solicita confirmación y, si se acepta, quita un horario de la lista del formulario de tutor.
 * @param {HTMLElement} btn el botón "eliminar" del horario, contenido dentro del elemento .horario-item
 * @returns {void}
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
 * Restringe la hora capturada al rango permitido (07:00–21:00) y ajusta la hora
 * de fin para que no sea anterior a la hora de inicio seleccionada.
 * @param {HTMLInputElement} input el campo de hora (inicio o fin) que disparó la validación
 * @returns {void}
 */
function validarLimitesHora(input) {
    const minHora = '07:00';
    const maxHora = '21:00';
    if (input.value) {
        if (input.value < minHora) { input.value = minHora; }
        else if (input.value > maxHora) { input.value = maxHora; }
    }

    const inputDesde = document.getElementById('horarioDesde');
    const inputHasta = document.getElementById('horarioHasta');
    if (inputDesde && inputHasta && inputDesde.value) {
        inputHasta.min = inputDesde.value;
        if (inputHasta.value && inputHasta.value <= inputDesde.value) {
            inputHasta.value = inputDesde.value;
        }
    }
}

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('formGuardar');
    if (!form) {
        return;
    }

    const btnGuardar = document.getElementById('btnGuardar');
    const contenedorHorarios = document.getElementById('contenedorHorarios');
    const feedbackHorario = document.getElementById('feedbackHorarioRequerido');
    const inputCorreo = document.getElementById('correo');
    const inputsValidables = form.querySelectorAll('input, select');

    /**
     * Indica si el formulario tiene al menos un horario de atención agregado.
     * @returns {boolean} true si hay al menos un horario en la lista
     */
    function tieneHorarios() {
        return contenedorHorarios.querySelectorAll('input[name="horariosDispo"]').length > 0;
    }

    /**
     * Revalida todos los campos del formulario de tutor, incluyendo que exista al
     * menos un horario agregado, y habilita/deshabilita el botón de guardar.
     * @returns {boolean} true si el formulario completo es válido
     */
    function verificarFormulario() {
        let esValido = true;
        inputsValidables.forEach(function (input) {
            if (!input.checkValidity()) {
                esValido = false;
            }
        });

        const hayHorarios = tieneHorarios();
        if (!hayHorarios) {
            esValido = false;
        }
        if (feedbackHorario) {
            feedbackHorario.style.display = hayHorarios ? 'none' : 'block';
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

    window.actualizarEstadoGuardar = verificarFormulario;

    verificarFormulario();

    const mensajeError = document.body.dataset.mensajeError;
    if (mensajeError) {
        mostrarAlerta('error', 'Error', mensajeError);
    }
});

document.addEventListener('DOMContentLoaded', function () {
    const inputNombres = document.getElementById('nombres');
    const inputApellidoPaterno = document.getElementById('apellidoPaterno');
    const inputCorreo = document.getElementById('correo');
    if (!inputNombres || !inputApellidoPaterno || !inputCorreo) return;

    const DOMINIO_CORREO = '@utez.edu.mx';
    let ultimoCorreoGenerado = inputCorreo.value || '';

    /**
     * Extrae la primera palabra de un texto, la pasa a minúsculas y le quita
     * acentos y cualquier carácter que no sea letra a-z.
     * @param {string} texto el texto del cual extraer la primera palabra
     * @returns {string} la primera palabra normalizada, o cadena vacía si no hay texto
     */
    function primeraPalabraSinAcentos(texto) {
        const primera = (texto || '').trim().split(/\s+/)[0] || '';
        return primera.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().replace(/[^a-z]/g, '');
    }

    /**
     * Genera y coloca el correo institucional automáticamente a partir del primer
     * nombre y primer apellido paterno, mientras el usuario no lo haya editado manualmente.
     * @returns {void}
     */
    function actualizarCorreoAutomatico() {
        if (inputCorreo.value !== ultimoCorreoGenerado) return;

        const primerNombre = primeraPalabraSinAcentos(inputNombres.value);
        const primerApellido = primeraPalabraSinAcentos(inputApellidoPaterno.value);

        ultimoCorreoGenerado = (primerNombre && primerApellido) ? primerNombre + primerApellido + DOMINIO_CORREO : '';
        inputCorreo.value = ultimoCorreoGenerado;
        inputCorreo.dispatchEvent(new Event('input', { bubbles: true }));
    }

    inputNombres.addEventListener('input', actualizarCorreoAutomatico);
    inputApellidoPaterno.addEventListener('input', actualizarCorreoAutomatico);
});

/**
 * Filtra las filas de la tabla de tutores por nombre buscado, estado activo/inactivo
 * y academia seleccionada, mostrando/ocultando filas y el mensaje de "sin resultados".
 * @returns {void}
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

document.addEventListener('DOMContentLoaded', function () {
    let inputCargaArchivo = document.getElementById('cargaMasivaTutoresArchivo');
    let formCargaMasiva = document.getElementById('formCargaMasivaTutores');
    let modalCargaMasivaEl = document.getElementById('modalCargaMasivaTutores');
    if (!formCargaMasiva || !inputCargaArchivo || !modalCargaMasivaEl) return;

    /**
     * Indica si el usuario ya seleccionó un archivo en el input de carga masiva de tutores.
     * @returns {boolean} true si hay al menos un archivo seleccionado
     */
    function cargaMasivaTieneCambios() {
        return inputCargaArchivo.files && inputCargaArchivo.files.length > 0;
    }

    /**
     * Cierra el modal de carga masiva de tutores usando la instancia de Bootstrap.
     * @returns {void}
     */
    function cerrarModalCargaMasiva() {
        let instancia = bootstrap.Modal.getInstance(modalCargaMasivaEl);
        if (instancia) instancia.hide();
    }

    /**
     * Cierra el modal de carga masiva directamente si no hay archivo seleccionado,
     * o solicita confirmación antes de cerrarlo si ya se seleccionó uno.
     * @returns {void}
     */
    function intentarCerrarModalCargaMasiva() {
        if (!cargaMasivaTieneCambios()) {
            cerrarModalCargaMasiva();
            return;
        }
        mostrarConfirmacion(
            'advertencia',
            '¿Descartar carga masiva?',
            'Si cierras ahora, perderás el archivo seleccionado.',
            'Sí, salir',
            cerrarModalCargaMasiva
        );
    }

    modalCargaMasivaEl.addEventListener('show.bs.modal', function () {
        formCargaMasiva.reset();
    });

    let btnCancelarCargaMasiva = document.getElementById('btnCancelarCargaMasivaTutores');
    let btnCerrarCargaMasiva = document.getElementById('btnCerrarCargaMasivaTutores');
    if (btnCancelarCargaMasiva) btnCancelarCargaMasiva.addEventListener('click', intentarCerrarModalCargaMasiva);
    if (btnCerrarCargaMasiva) btnCerrarCargaMasiva.addEventListener('click', intentarCerrarModalCargaMasiva);
});

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
            case 'carga_masiva_tutores': {
                if (window.filasInvalidasTutoresExcel && window.filasInvalidasTutoresExcel.length > 0) break;
                let insertados = parametros.get('insertados') || '0';
                mostrarToast('exito', '¡Éxito!', 'Se registraron ' + insertados + ' tutor(es) correctamente.');
                break;
            }
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
            case 'tutor_con_pendientes':
                mostrarAlerta('error', 'No se puede eliminar', 'No se puede eliminar al tutor porque tiene grupos asignados, solicitudes o sesiones pendientes.');
                break;
            case 'archivo_vacio':
                mostrarAlerta('error', 'Error', 'Selecciona un archivo Excel (.xlsx o .xls) antes de subir.');
                break;
            case 'archivo_muy_grande':
                mostrarAlerta('error', 'Error', 'El archivo es demasiado grande. El límite es de 5 MB.');
                break;
            case 'archivo_invalido':
                if (!window.filasInvalidasTutoresExcel || window.filasInvalidasTutoresExcel.length === 0) {
                    mostrarAlerta('error', 'Error', 'No se pudo leer el archivo, o ninguna fila tenía datos válidos. Verifica el formato de las columnas y vuelve a intentar.');
                }
                break;
            case 'carga_fallida':
                mostrarAlerta('error', 'Error', 'No se pudo guardar la carga masiva por un error interno. Intenta de nuevo.');
                break;
        }

        window.history.replaceState(null, null, window.location.pathname);
    }

    if (window.filasInvalidasTutoresExcel && window.filasInvalidasTutoresExcel.length > 0) {
        let insertados = parseInt(parametros.get('insertados') || '0', 10);
        let listaFilas = window.filasInvalidasTutoresExcel.join('\n');

        if (insertados > 0) {
            mostrarAlerta(
                'advertencia',
                'Carga parcial',
                'Se registraron ' + insertados + ' tutor(es), pero se omitieron estas filas:\n' + listaFilas
            );
        } else {
            mostrarAlerta(
                'error',
                'No se registró ningún tutor',
                'Ninguna fila del archivo pasó la validación:\n' + listaFilas
            );
        }
    }
});
