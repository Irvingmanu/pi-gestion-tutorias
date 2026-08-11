// Logica dedicada a formulario-area.jsp: valida en tiempo real todos los
// formularios ".needs-validation" de la vista (modo "Nueva Área" y modo
// edición) y controla el estado del boton Guardar de cada uno.
//
// Soporta elementos dinamicos: los motivos que motivos.js agrega/quita del
// DOM no disparan 'input'/'focusout' por si solos, por eso se expone
// window.verificarFormularioArea para que motivos.js pueda pedir una
// revalidacion manual despues de mutar el DOM.

document.addEventListener('DOMContentLoaded', function () {
    const forms = document.querySelectorAll('.needs-validation');

    forms.forEach(form => {
        // Busca el botón de submit dentro del form, o si está fuera usando
        // el atributo form="id" (caso de "Guardar" en modo edición).
        let btnGuardar = form.querySelector('button[type="submit"]');
        if (!btnGuardar && form.id) {
            btnGuardar = document.querySelector('button[type="submit"][form="' + form.id + '"]');
        }

        function verificarFormulario() {
            if (!btnGuardar) {
                return;
            }

            let esValido = form.checkValidity();

            // "Nueva Área" no tiene un <input required> estatico para motivos
            // (son hidden inputs que agrega motivos.js), asi que se exige aparte.
            if (form.id === 'formNuevaArea') {
                const contenedorMotivos = document.getElementById('motivosContainer');
                const tieneMotivos = !!contenedorMotivos && contenedorMotivos.querySelectorAll('input[name="motivos[]"]').length > 0;
                esValido = esValido && tieneMotivos;
            }

            btnGuardar.disabled = !esValido;
        }

        // Delegación de eventos a nivel formulario para detectar inputs
        // dinámicos recién agregados.
        form.addEventListener('input', function (e) {
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'SELECT') {
                if (e.target.checkValidity()) {
                    e.target.classList.remove('is-invalid');
                    let feedback = e.target.closest('.motivo-row')?.querySelector('.invalid-feedback');
                    if (feedback) feedback.style.display = 'none';
                } else {
                    e.target.classList.add('is-invalid');
                    let feedback = e.target.closest('.motivo-row')?.querySelector('.invalid-feedback');
                    if (feedback) feedback.style.display = 'block';
                }
                verificarFormulario();
            }
        });

        form.addEventListener('focusout', function (e) {
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'SELECT') {
                if (!e.target.checkValidity()) {
                    e.target.classList.add('is-invalid');
                    let feedback = e.target.closest('.motivo-row')?.querySelector('.invalid-feedback');
                    if (feedback) feedback.style.display = 'block';
                }
                verificarFormulario();
            }
        });

        if (form.id === 'formNuevaArea') {
            window.verificarFormularioArea = verificarFormulario;
        }

        // Verificación inicial: si un campo ya viene invalido desde que carga
        // la pantalla (ej. un valor guardado en BD que ya no cumple el patron
        // actual), se marca de una vez en rojo.
        form.querySelectorAll('input, select').forEach(function (input) {
            if (!input.checkValidity()) {
                input.classList.add('is-invalid');
                let feedback = input.closest('.motivo-row')?.querySelector('.invalid-feedback');
                if (feedback) feedback.style.display = 'block';
            }
        });

        verificarFormulario();
    });

    // Muestra el mensaje de error (si el servidor lo mandó) usando el mismo
    // sistema de alertas que el resto del sitio.
    const mensajeError = document.body.getAttribute('data-mensaje-error');
    if (mensajeError) {
        mostrarAlerta('error', 'Error', mensajeError);
    }
});