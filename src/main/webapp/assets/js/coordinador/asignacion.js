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
    } else if (error === 'true') {
        mostrarAlerta('error', 'Error en la Asignación', 'Esta asignación ya existe en la base de datos.');
    } else if (error === 'grupo_asignado') {
    mostrarAlerta('error', 'Grupo ya asignado', 'Este grupo ya tiene un tutor asignado en ese cuatrimestre y periodo escolar.');
    }

    if (exito || error) {
        window.history.replaceState(null, null, window.location.pathname);
    }
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