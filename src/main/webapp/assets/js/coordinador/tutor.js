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
function eliminarHorario(boton) {
    Swal.fire({
        title: '¿Eliminar horario?',
        text: '¿Estás seguro de que deseas quitar este horario de la lista?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#00897b', // Verde de tu tema
        cancelButtonColor: '#dc3545',  // Rojo de eliminar
        confirmButtonText: 'Sí, eliminar',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            // Elimina la fila del horario
            const elemento = boton.closest('.horario-item') || boton.parentElement;
            elemento.remove();

            // Si tienes una función para revalidar el formulario después de eliminar, la llamas aquí
            if (typeof verificarFormulario === 'function') {
                verificarFormulario();
            }
        }
    });
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
}

/**
 * Elimina el renglón de horario seleccionado.
 * @param {HTMLElement} btn
 */
function eliminarHorario(btn) {
    const item = btn.closest('.horario-item');
    if (item) {
        item.remove();
    }
}

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

    let filas = document.querySelectorAll('#tablaTutores tr');
    let filasVisibles = 0;

    filas.forEach(function (fila) {
        if (fila.id === 'filaSinResultados') return;

        let nombre = fila.dataset.nombre || '';
        let activo = fila.dataset.activo !== 'N';

        let coincideNombre = nombre.includes(textoBuscar);
        let coincideActivo = activo || incluirInactivos;

        let coincide = coincideNombre && coincideActivo;
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

    if (buscarTutor) buscarTutor.addEventListener('input', filtrarTutores);
    if (btnAgregar) btnAgregar.addEventListener('click', agregarHorario);
    if (mostrarInactivos) mostrarInactivos.addEventListener('change', filtrarTutores);

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
        }

        window.history.replaceState(null, null, window.location.pathname);
    }
    // Limita el <input type="time"> del horario de atencion a 08:00-20:00 en tiempo real
    function validarLimitesHora(input) {
        const minHora = '08:00';
        const maxHora = '20:00';
        if (input.value) {
            if (input.value < minHora) {
                input.value = minHora;
            } else if (input.value > maxHora) {
                input.value = maxHora;
            }
        }
    }

// Validacion en vivo del formulario de tutor: marca is-invalid en cada
// campo requerido y solo habilita "Guardar" cuando todo el formulario es valido.
    document.addEventListener('DOMContentLoaded', function () {
        const form = document.getElementById('formGuardar');
        if (!form) return;

        const btnGuardar = document.getElementById('btnGuardar');
        const inputsRequeridos = form.querySelectorAll('input[required], select[required]');

        function verificarFormulario() {
            let esValido = true;
            inputsRequeridos.forEach(function (input) {
                if (!input.checkValidity()) {
                    esValido = false;
                }
            });
            btnGuardar.disabled = !esValido;
        }

        inputsRequeridos.forEach(function (input) {
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

        verificarFormulario();
    });
});
