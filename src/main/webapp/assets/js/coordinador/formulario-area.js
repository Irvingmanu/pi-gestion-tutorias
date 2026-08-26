
function configurarValidacionFormulario(form) {
    let btnGuardar = form.querySelector('button[type="submit"]');
    if (!btnGuardar && form.id) {
        btnGuardar = document.querySelector('button[type="submit"][form="' + form.id + '"]');
    }

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

    function marcarSiTieneValor(input) {
        if (input.value) {
            camposTocados.add(input);
        }
        marcarValidez(input);
    }

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
