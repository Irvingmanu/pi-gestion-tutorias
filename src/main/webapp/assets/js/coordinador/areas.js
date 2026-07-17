// Función para abrir el modal
function prepararEliminacion(id) {
    document.getElementById('deleteId').value = id;

    let modalEl = document.getElementById('modalEliminar');
    let modalObj = new bootstrap.Modal(modalEl);
    modalObj.show();
}