// Logica dinamica usada solo en el modo "Nueva Área" de formulario-area.jsp
// para la seccion "Motivos de Canalización": el boton '+' del primer input
// agrega filas, los botones '-' inyectados eliminan su propia fila.
// El modo edicion maneja sus motivos con forms individuales (alta/baja), sin JS.

document.addEventListener('DOMContentLoaded', function () {
    var container = document.getElementById('motivosContainer');
    if (!container) {
        return;
    }

    container.addEventListener('click', function (evento) {
        if (evento.target.classList.contains('btn-eliminar-motivo')) {
            evento.target.closest('.motivo-row').remove();
        }
    });
});

function agregarMotivo() {
    var container = document.getElementById('motivosContainer');
    var inputs = container.querySelectorAll('input[name="motivos[]"]');
    var ultimoInput = inputs[inputs.length - 1];

    if (!ultimoInput.value.trim()) {
        mostrarAlerta('advertencia', 'Campo vacío', 'Por favor, llena el campo actual antes de agregar otro motivo.');
        ultimoInput.focus();
        return;
    }

    var fila = document.createElement('div');
    fila.className = 'd-flex gap-2 mb-3 motivo-row';
    fila.innerHTML =
        '<input type="text" name="motivos[]" class="form-control form-control-figma w-100 fs-6" placeholder="Escribe el motivo de atención" ' +
        'pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s./]+$" title="Solo se permiten letras, espacios, puntos y diagonales" required>' +
        '<button type="button" class="btn-cancelar-figma btn-cancelar-figma-sm flex-shrink-0 btn-eliminar-motivo">-</button>';

    container.appendChild(fila);
    fila.querySelector('input').focus();
}
