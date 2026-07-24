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
            'Esta acción es irreversible y perderás todos los datos del tutor.',
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
    let filas = document.querySelectorAll('#tablaTutores tr');
    let filasVisibles = 0;

    filas.forEach(function (fila) {
        if (fila.id === 'filaSinResultados') return;

        let nombre = fila.dataset.nombre || '';
        let coincide = nombre.includes(textoBuscar);

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

    if (buscarTutor) buscarTutor.addEventListener('input', filtrarTutores);
    if (btnAgregar) btnAgregar.addEventListener('click', agregarHorario);
});
