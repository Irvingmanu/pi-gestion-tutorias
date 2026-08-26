/**
 * Configura la validación en vivo (HTML5 + reglas propias) de un formulario
 * con la clase "needs-validation", habilitando/deshabilitando su botón de
 * guardar y revalidándolo periódicamente para cubrir el autocompletado del navegador.
 * @author J4IROXD
 * @date 2026-08-10
 * @param {HTMLFormElement} form - el formulario a validar en vivo
 * @returns {void}
 */
function configurarValidacionFormulario(form) {
    let btnGuardar = form.querySelector('button[type="submit"]');
    if (!btnGuardar && form.id) {
        btnGuardar = document.querySelector('button[type="submit"][form="' + form.id + '"]');
    }

    /**
     * Verifica la validez completa del formulario (incluyendo, para el
     * formulario de nueva área, que exista al menos un motivo capturado) y
     * habilita/deshabilita el botón de guardar en consecuencia.
     * @returns {void}
     */
    function verificarFormulario() {
        if (!btnGuardar) {
            return;
        }

        let esValido = form.checkValidity();

        if (form.id === 'formNuevaArea') {
            const contenedorMotivos = document.getElementById('motivosContainer');
            const tieneMotivos = !!contenedorMotivos && contenedorMotivos.querySelectorAll('input[name="motivos[]"]').length > 0;
            esValido = esValido && tieneMotivos;

            const feedbackMotivoRequerido = document.getElementById('feedbackMotivoRequerido');
            if (feedbackMotivoRequerido) {
                feedbackMotivoRequerido.style.display = tieneMotivos ? 'none' : 'block';
            }
        }

        btnGuardar.disabled = !esValido;
    }

    const camposTocados = new WeakSet();

    const MENSAJE_CAMPO_OBLIGATORIO = 'Este campo es obligatorio.';

    /**
     * Obtiene el elemento de retroalimentación de validación asociado a un
     * campo, buscándolo como hermano siguiente o dentro de su fila de motivo.
     * @param {HTMLElement} input - el campo del formulario
     * @returns {HTMLElement|null} el elemento de retroalimentación encontrado, o null si no existe
     */
    function obtenerFeedback(input) {
        if (input.nextElementSibling && input.nextElementSibling.classList.contains('invalid-feedback')) {
            return input.nextElementSibling;
        }
        const motivoRow = input.closest('.motivo-row');
        if (motivoRow) {
            const fb = motivoRow.querySelector('.invalid-feedback');
            if (fb) return fb;
        }
        return input.parentElement?.querySelector('.invalid-feedback') || null;
    }

    /**
     * Marca visualmente un campo como válido o inválido según su estado de
     * validación HTML5, mostrando el mensaje de error correspondiente solo
     * si el campo ya fue tocado.
     * @param {HTMLElement} input - el campo del formulario a validar visualmente
     * @returns {void}
     */
    function marcarValidez(input) {
        const feedback = obtenerFeedback(input);

        if (!camposTocados.has(input) || input.checkValidity()) {
            input.classList.remove('is-invalid');
            if (feedback) feedback.style.display = 'none';
            return;
        }

        input.classList.add('is-invalid');
        if (!feedback) {
            return;
        }

        if (feedback.dataset.msgPatron === undefined) {
            feedback.dataset.msgPatron = feedback.textContent.trim();
        }

        feedback.textContent = input.validity.valueMissing
            ? (input.dataset.msgRequerido || MENSAJE_CAMPO_OBLIGATORIO)
            : feedback.dataset.msgPatron;

        feedback.style.display = 'block';
    }

    form.addEventListener('input', function (e) {
        if (e.target.tagName === 'INPUT' || e.target.tagName === 'SELECT') {
            camposTocados.add(e.target);
            marcarValidez(e.target);
            verificarFormulario();
        }
    });

    form.addEventListener('focusout', function (e) {
        if (e.target.tagName === 'INPUT' || e.target.tagName === 'SELECT') {
            camposTocados.add(e.target);
            marcarValidez(e.target);
            verificarFormulario();
        }
    });

    form.addEventListener('change', function (e) {
        if (e.target.tagName === 'INPUT' || e.target.tagName === 'SELECT') {
            camposTocados.add(e.target);
            marcarValidez(e.target);
            verificarFormulario();
        }
    });

    form.addEventListener('animationstart', function (e) {
        if (e.animationName === 'onAutoFillStart' &&
            (e.target.tagName === 'INPUT' || e.target.tagName === 'SELECT')) {
            camposTocados.add(e.target);
            marcarValidez(e.target);
            verificarFormulario();
        }
    });

    if (form.id === 'formNuevaArea') {
        window.verificarFormularioArea = verificarFormulario;
    }

    /**
     * Marca un campo como tocado si ya tiene un valor (útil para detectar
     * campos autocompletados por el navegador) y aplica su validación visual.
     * @param {HTMLElement} input - el campo a evaluar
     * @returns {void}
     */
    function marcarSiTieneValor(input) {
        if (input.value) {
            camposTocados.add(input);
        }
        marcarValidez(input);
    }

    /**
     * Revalida visualmente todos los campos del formulario y actualiza el
     * estado del botón de guardar; se ejecuta al cargar y periódicamente.
     * @returns {void}
     */
    function marcarValidezFormularioCompleto() {
        form.querySelectorAll('input, select').forEach(marcarSiTieneValor);
        verificarFormulario();
    }

    marcarValidezFormularioCompleto();

    setInterval(marcarValidezFormularioCompleto, 500);
}
window.configurarValidacionFormulario = configurarValidacionFormulario;

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.needs-validation').forEach(configurarValidacionFormulario);

    const mensajeError = document.body.getAttribute('data-mensaje-error');
    if (mensajeError) {
        mostrarAlerta('error', 'Error', mensajeError);
    }
});
