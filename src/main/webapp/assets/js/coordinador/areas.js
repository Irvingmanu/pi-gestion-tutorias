/**
 * Gestiona la vista de "Áreas de apoyo" del coordinador: confirmaciones para
 * cancelar el formulario, guardar un área y eliminarla, además de mostrar
 * los mensajes de éxito/error de la operación según el parámetro de la URL.
 * @author Irvingmanu
 * @date 2026-07-17
 */
/**
 * Pide confirmación antes de descartar los cambios del formulario de área
 * de apoyo y, si se confirma, redirige a la URL de cancelación.
 * @returns {void}
 */
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
 * Intercepta el envío del formulario de área de apoyo: exige al menos un
 * motivo de canalización capturado y, si hay, pide confirmación antes de
 * enviar el formulario al servidor.
 * @param {Event} evento - el evento de envío del formulario
 * @returns {boolean} false siempre, para prevenir el envío nativo del formulario
 */
function confirmarGuardarArea(evento) {
    evento.preventDefault();
    let formulario = evento.target;

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

/**
 * Pide confirmación crítica antes de eliminar un área de apoyo y, si se
 * confirma, envía el formulario oculto de eliminación con el id indicado.
 * @param {number|string} idArea - el id del área de apoyo a eliminar
 * @returns {void}
 */
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
            case 'area_bloqueada':
                mostrarAlerta('error', 'Área bloqueada', 'El nombre del área y sus motivos están bloqueados porque ya cuenta con alumnos canalizados.');
                break;
        }

        window.history.replaceState(null, null, window.location.pathname);
    }
});