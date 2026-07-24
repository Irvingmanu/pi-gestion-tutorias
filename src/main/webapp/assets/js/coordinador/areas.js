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

// Intercepta el submit del formulario principal del area (nueva o edicion)
function confirmarGuardarArea(evento) {
    evento.preventDefault();
    let formulario = evento.target;

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

// Elimina un motivo individual desde la tabla de la vista de edicion
function prepararEliminacionMotivo(idMotivo, idArea) {
    mostrarConfirmacion(
        'critica',
        '¿Eliminar motivo?',
        'Esta acción eliminará permanentemente este motivo de canalización.',
        'Eliminar',
        function () {
            document.getElementById('inputEliminarMotivoId').value = idMotivo;
            document.getElementById('inputEliminarMotivoIdArea').value = idArea;
            document.getElementById('formEliminarMotivo').submit();
        }
    );
}

// Alterna entre el texto del motivo y su form de edicion en linea (sin AJAX)
function toggleEditarMotivo(idMotivo) {
    let etiqueta = document.getElementById('lbl-motivo-' + idMotivo);
    let formulario = document.getElementById('form-edit-' + idMotivo);

    if (!etiqueta || !formulario) {
        return;
    }

    etiqueta.classList.toggle('d-none');
    formulario.classList.toggle('d-none');
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
        }

        window.history.replaceState(null, null, window.location.pathname);
    }
});
