// Logica de la vista de Asignacion de Tutores (coordinador/asignacion.jsp):
// toasts/alertas via parametros en la URL (?exito=, ?error=) y confirmacion
// de eliminacion de una asignacion existente.

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

// Filtro OBLIGATORIO en cascada: Academia -> Tutor + Grupo. Un tutor solo puede
// asignarse a grupos de su misma academia, asi que en vez de derivar el filtro del
// Tutor elegido (como antes), ahora la Academia es la que manda arriba y filtra a
// los dos por igual. Tutor y Grupo empiezan disabled; todas sus opciones ya vienen
// renderizadas con data-academia-id (ver asignacion.jsp), asi que esto no pide nada
// al servidor, solo oculta/deshabilita <option> en el cliente.
document.addEventListener('DOMContentLoaded', function () {
    const selectAcademia = document.getElementById('academiaFormulario');
    const selectTutor = document.getElementById('tutor');
    const selectGrupo = document.getElementById('grupo');
    if (!selectAcademia || !selectTutor || !selectGrupo) {
        return;
    }

    // Reutilizable para Tutor y Grupo: oculta las <option> cuyo data-academia-id no
    // coincida con la academia elegida, limpia la seleccion previa (evita datos
    // cruzados entre academias) y deshabilita el <select> si no hay academia o si
    // esa academia no tiene opciones disponibles.
    function filtrarPorAcademia(select, idAcademia, textoSinAcademia, textoConOpciones, textoSinOpciones) {
        let huboOpcionesVisibles = false;

        Array.prototype.forEach.call(select.options, function (opcion) {
            if (!opcion.value) {
                return; // el placeholder siempre se conserva tal cual
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

// Filtro OPCIONAL de la tabla "Asignaciones Actuales": oculta filas cuyo
// data-academia-id no coincida con la academia elegida en #filtroAcademiaTabla.
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