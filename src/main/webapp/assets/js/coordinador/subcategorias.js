// Logica dinamica compartida por nueva-area.jsp y formulario-area.jsp para
// la seccion "Sub-Categorias": el boton '+' del primer input agrega filas,
// los botones '-' (propios o inyectados via JSTL) eliminan su fila.

document.addEventListener('DOMContentLoaded', function () {
    var container = document.getElementById('subcategoriasContainer');
    if (!container) {
        return;
    }

    container.addEventListener('click', function (evento) {
        if (evento.target.classList.contains('btn-eliminar-subcategoria')) {
            evento.target.closest('.subcategoria-row').remove();
        }
    });
});

function agregarSubcategoria() {
    var container = document.getElementById('subcategoriasContainer');
    var inputs = container.querySelectorAll('input[name="motivos[]"]');
    var ultimoInput = inputs[inputs.length - 1];

    if (!ultimoInput.value.trim()) {
        alert('Llena el campo actual antes de agregar otro');
        ultimoInput.focus();
        return;
    }

    var fila = document.createElement('div');
    fila.className = 'd-flex gap-2 mb-3 subcategoria-row';
    fila.innerHTML =
        '<input type="text" name="motivos[]" class="form-control form-control-figma w-100 fs-6" placeholder="Escribe la subcategoria" required>' +
        '<button type="button" class="btn-cancelar-figma btn-cancelar-figma-sm flex-shrink-0 btn-eliminar-subcategoria">-</button>';

    container.appendChild(fila);
    fila.querySelector('input').focus();
}
