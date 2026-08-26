/**
 * Gestiona la vista de "Asignación de tutores" del coordinador: muestra los
 * mensajes de éxito/error de la operación, filtra los selects de tutor y
 * grupo según la academia elegida en el formulario, filtra la tabla de
 * asignaciones por academia, y confirma la eliminación de una asignación.
 * @author 20253ds074-art
 * @date 2026-08-06
 */
document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    const exito = urlParams.get('exito');
    const error = urlParams.get('error');

    if (exito === 'true') {
        mostrarAlerta('exito', '¡Asignación Exitosa!', 'El tutor ha sido asignado correctamente al grupo.');
    } else if (exito === 'eliminado') {
        mostrarAlerta('exito', '¡Asignación Eliminada!', 'El tutor ya no está asignado a ese grupo y cuatrimestre.');
    } else if (error === 'grupo_asignado') {
        mostrarAlerta('error', 'Grupo ya asignado', 'Este grupo ya tiene un tutor asignado en ese cuatrimestre.');
    } else if (error === 'academia_no_coincide') {
        mostrarAlerta('error', 'Academia no coincide', 'Solo puedes asignar al tutor a grupos que pertenezcan a su misma academia.');
    } else if (error === 'cuatrimestre_no_permitido') {
        mostrarAlerta('error', 'Cuatrimestre no permitido', 'Los grupos de 6° y 10° cuatrimestre no pueden tener tutor asignado, excepto en la carrera Terapia Física.');
    } else if (error === 'grupo_sin_alumnos') {
        mostrarAlerta('error', 'Grupo sin alumnos', 'Este grupo no tiene alumnos activos, así que no se le puede asignar un tutor.');
    } else if (error === 'true') {
        mostrarAlerta('error', 'Error en la Asignación', 'Esta asignación ya existe en la base de datos.');
    } else if (error === 'grupo_asignado') {
        mostrarAlerta('error', 'Grupo ya asignado', 'Este grupo ya tiene un tutor asignado en ese cuatrimestre y periodo escolar.');
    } else if (error === 'asignacion_con_pendientes') {
        mostrarAlerta('error', 'No se puede remover', 'No se puede remover al tutor de este grupo porque aún tiene sesiones o solicitudes pendientes con estos alumnos.');
    }

    if (exito || error) {
        window.history.replaceState(null, null, window.location.pathname);
    }
});

document.addEventListener('DOMContentLoaded', function () {
    const selectAcademia = document.getElementById('academiaFormulario');
    const selectTutor = document.getElementById('tutor');
    const selectGrupo = document.getElementById('grupo');
    if (!selectAcademia || !selectTutor || !selectGrupo) {
        return;
    }

    /**
     * Muestra/oculta y habilita/deshabilita las opciones de un select según
     * si pertenecen a la academia indicada, actualizando el texto del
     * placeholder según haya o no opciones disponibles.
     * @param {HTMLSelectElement} select - el select a filtrar (tutor o grupo)
     * @param {string} idAcademia - el id de la academia seleccionada (cadena vacía si ninguna)
     * @param {string} textoSinAcademia - el texto del placeholder cuando no hay academia elegida
     * @param {string} textoConOpciones - el texto del placeholder cuando hay opciones disponibles
     * @param {string} textoSinOpciones - el texto del placeholder cuando no hay opciones disponibles
     * @returns {void}
     */
    function filtrarPorAcademia(select, idAcademia, textoSinAcademia, textoConOpciones, textoSinOpciones) {
        let huboOpcionesVisibles = false;

        Array.prototype.forEach.call(select.options, function (opcion) {
            if (!opcion.value) {
                return;
            }

            const coincide = idAcademia !== '' && opcion.getAttribute('data-academia-id') === idAcademia;
            opcion.hidden = !coincide;
            opcion.disabled = !coincide;
            if (coincide) {
                huboOpcionesVisibles = true;
            }
        });

        select.value = '';
        select.disabled = idAcademia === '' || !huboOpcionesVisibles;

        const placeholder = select.querySelector('option[value=""]');
        if (placeholder) {
            placeholder.textContent = idAcademia === ''
                ? textoSinAcademia
                : (huboOpcionesVisibles ? textoConOpciones : textoSinOpciones);
        }

        select.dispatchEvent(new Event('change'));
    }

    selectAcademia.addEventListener('change', function () {
        const idAcademia = selectAcademia.value;
        filtrarPorAcademia(selectTutor, idAcademia, 'Seleccione primero la academia', 'Seleccione el tutor', 'Esta academia no tiene tutores disponibles');
        filtrarPorAcademia(selectGrupo, idAcademia, 'Seleccione primero la academia', 'Seleccione el grupo', 'Esta academia no tiene grupos disponibles');
    });
});

document.addEventListener('DOMContentLoaded', function () {
    const selectFiltroTabla = document.getElementById('filtroAcademiaTabla');
    const tabla = document.getElementById('tablaAsignaciones');
    if (!selectFiltroTabla || !tabla) {
        return;
    }

    selectFiltroTabla.addEventListener('change', function () {
        const idAcademia = selectFiltroTabla.value;
        let filasVisibles = 0;

        tabla.querySelectorAll('tbody tr').forEach(function (fila) {
            if (fila.id === 'filaSinResultados') {
                return;
            }

            const coincide = idAcademia === '' || fila.getAttribute('data-academia-id') === idAcademia;
            fila.style.display = coincide ? '' : 'none';
            if (coincide) {
                filasVisibles++;
            }
        });

        const filaSinResultados = document.getElementById('filaSinResultados');
        if (filaSinResultados) {
            filaSinResultados.style.display = filasVisibles === 0 ? '' : 'none';
        }
    });
});

/**
 * Pide confirmación crítica antes de eliminar una asignación de tutor y, si
 * se confirma, envía el formulario oculto de eliminación con el id indicado.
 * @param {number|string} idAsignacion - el id de la asignación a eliminar
 * @returns {void}
 */
function prepararEliminacionAsignacion(idAsignacion) {
    mostrarConfirmacion(
        'critica',
        '¿Eliminar asignación?',
        'El tutor dejará de estar asignado a este grupo y cuatrimestre.',
        'Eliminar',
        function () {
            document.getElementById('inputEliminarAsignacion').value = idAsignacion;
            document.getElementById('formEliminarAsignacion').submit();
        }
    );
}