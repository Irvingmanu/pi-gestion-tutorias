/**
 * Habilita el botón de guardar del formulario de asignación de tutor solo
 * cuando todos los selects obligatorios tienen un valor seleccionado.
 * @author J4IROXD
 * @date 2026-08-10
 */
document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('formGuardar');
    if (!form) return;

    const btnGuardar = document.getElementById('btnGuardar');
    const selects = form.querySelectorAll('select[required]');

    /**
     * Verifica que todos los selects obligatorios del formulario tengan un
     * valor seleccionado y habilita/deshabilita el botón de guardar.
     * @returns {void}
     */
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