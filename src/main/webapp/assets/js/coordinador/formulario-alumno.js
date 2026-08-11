// Logica dedicada a formulario-alumno.jsp: valida en tiempo real todos los
// campos requeridos del formulario y habilita/deshabilita el boton Guardar.

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('formGuardar');
    if (!form) return;

    const btnGuardar = document.getElementById('btnGuardar');
    const inputsRequeridos = form.querySelectorAll('input[required], select[required]');

    function verificarFormulario() {
        let esValido = true;
        inputsRequeridos.forEach(input => {
            if (!input.checkValidity()) {
                esValido = false;
            }
        });
        btnGuardar.disabled = !esValido;
    }

    inputsRequeridos.forEach(input => {
        input.addEventListener('input', function () {
            if (this.checkValidity()) {
                this.classList.remove('is-invalid');
            } else {
                this.classList.add('is-invalid');
            }
            verificarFormulario();
        });

        input.addEventListener('change', function () {
            if (this.checkValidity()) {
                this.classList.remove('is-invalid');
            } else {
                this.classList.add('is-invalid');
            }
            verificarFormulario();
        });

        input.addEventListener('blur', function () {
            if (!this.checkValidity()) {
                this.classList.add('is-invalid');
            }
            verificarFormulario();
        });
    });

    // Verificación inicial por si estamos en modo edición.
    verificarFormulario();
});