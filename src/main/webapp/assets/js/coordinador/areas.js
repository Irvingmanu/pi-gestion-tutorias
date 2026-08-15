// Confirmacion para el boton 'Cancelar' de formulario-area.jsp (nueva y edicion)
function confirmarCancelacionArea() {
    let boton = document.getElementById('btnCancelarFormularioArea');
    let urlDestino = boton ? boton.dataset.urlCancelar : '/';

    mostrarConfirmacion(
        'advertencia',
        '¿Descartar cambios?',
        'Si sales ahora, perderás los datos que has ingresado.',
        'Sí, salir',
        function () {
            window.location.href = urlDestino;
        }
    );
}

/**
 *
 * @param evento
 * @returns {boolean}
 *
 */

// Intercepta el submit del formulario principal del area (nueva o edicion)
function confirmarGuardarArea(evento) {
    evento.preventDefault();
    let formulario = evento.target;

    // Solo aplica a "Nueva Área": ahi los motivos son hidden inputs agregados por JS,
    // asi que el navegador no los puede exigir por si solo via required.
    let contenedorMotivos = document.getElementById('motivosContainer');
    if (contenedorMotivos && contenedorMotivos.querySelectorAll('input[name="motivos[]"]').length === 0) {
        mostrarAlerta('error', 'Motivo requerido', 'Agrega al menos un motivo de canalización antes de guardar.');
        return false;
    }

    mostrarConfirmacion(
        'advertencia',
        '¿Guardar área?',
        'Se guardarán los datos capturados para esta área de apoyo.',
        'Sí, guardar',
        function () {
            formulario.submit();
        }
    );

    return false;
}

// Elimina un area completa (con sus motivos) desde la tabla/tarjetas principales
function prepararEliminacionArea(idArea) {
    mostrarConfirmacion(
        'critica',
        '¿Eliminar área de apoyo?',
        'Esta acción es irreversible y también se eliminarán sus motivos de canalización.',
        'Eliminar',
        function () {
            document.getElementById('inputEliminarIdArea').value = idArea;
            document.getElementById('formEliminarArea').submit();
        }
    );
}

// Toasts/alertas de exito y error via parametros en la URL (?exito=, ?error=)
document.addEventListener('DOMContentLoaded', function () {
    const parametros = new URLSearchParams(window.location.search);
    const exito = parametros.get('exito');

    if (exito) {
        switch (exito) {
            case 'guardado':
                mostrarToast('exito', '¡Éxito!', 'Los datos se guardaron correctamente');
                break;
            case 'editado':
                mostrarToast('exito', '¡Éxito!', 'Los datos se actualizaron correctamente');
                break;
            case 'eliminado':
                mostrarToast('exito', '¡Éxito!', 'El registro fue eliminado correctamente');
                break;
        }

        window.history.replaceState(null, null, window.location.pathname);
    }

    const error = parametros.get('error');

    if (error) {
        switch (error) {
            case 'formato_invalido':
                mostrarAlerta('error', 'Error', 'Verifica los datos. El formato de uno o más campos es incorrecto.');
                break;
            case 'nombre_duplicado':
                mostrarAlerta('error', 'Error', 'Ya existe un área de apoyo registrada con ese nombre.');
                break;
            case 'area_en_uso':
                mostrarAlerta('error', 'No se puede eliminar', 'Esta área no puede ser eliminada porque ya tiene información o alumnos vinculados en el sistema.');
                break;
            case 'motivo_en_uso':
                mostrarAlerta('error', 'No se puede eliminar', 'Este motivo no puede eliminarse porque ya hay alumnos canalizados con él.');
                break;
        }

        window.history.replaceState(null, null, window.location.pathname);
    }
});