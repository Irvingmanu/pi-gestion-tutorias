// Logica dedicada a asignacion.jsp: habilita/deshabilita el boton "Agregar"
// del formulario "Nueva Asignación" segun se llenen todos los selects
// requeridos, y marca en rojo el select que quede invalido.

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('formGuardar');
    if (!form) return;

    const btnGuardar = document.getElementById('btnGuardar');
    const selects = form.querySelectorAll('select[required]');

    function verificarFormulario() {
        let esValido = true;
        selects.forEach(select => {
            if (!select.value || select.value === "") {
                esValido = false;
            }
        });
        btnGuardar.disabled = !esValido;
    }

    selects.forEach(select => {
        select.addEventListener('change', function () {
            if (this.checkValidity()) {
                this.classList.remove('is-invalid');
            } else {
                this.classList.add('is-invalid');
            }
            verificarFormulario();
        });
    });

    verificarFormulario();
});