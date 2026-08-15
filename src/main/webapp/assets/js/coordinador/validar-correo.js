// Validación en vivo del correo institucional (@utez.edu.mx).
// Se usa en formulario-tutor.jsp, formulario-alumno.jsp y formulario-area.jsp.
//
// A diferencia de la validación genérica (que solo marca is-invalid), esta
// muestra un mensaje específico y visible MIENTRAS el usuario escribe,
// sin esperar a que le dé clic en Guardar.

document.addEventListener('DOMContentLoaded', function () {
    const DOMINIO_VALIDO = '@utez.edu.mx';
    const inputsCorreo = document.querySelectorAll('input[type="email"]');

    inputsCorreo.forEach(function (input) {
        // Buscamos el div .invalid-feedback que ya existe justo despues del input
        // en el HTML, para reusarlo como mensaje en vivo.
        const feedback = input.parentElement.querySelector('.invalid-feedback')
            || input.nextElementSibling;

        function validarEnVivo() {
            const valor = input.value.trim().toLowerCase();

            // Campo vacío: no mostramos error todavía (se valida al enviar/blur).
            if (valor === '') {
                input.classList.remove('is-invalid');
                return;
            }

            const terminaCorrecto = valor.endsWith(DOMINIO_VALIDO);
            const formatoCompleto = /^[a-z0-9._-]+@utez\.edu\.mx$/.test(valor);

            if (!terminaCorrecto) {
                // Aún no ha llegado al dominio o lo está escribiendo mal.
                input.classList.add('is-invalid');
                if (feedback) {
                    feedback.textContent = 'El correo debe terminar en ' + DOMINIO_VALIDO;
                    feedback.style.display = 'block';
                }
            } else if (!formatoCompleto) {
                // Termina en el dominio correcto pero la parte de antes tiene caracteres inválidos.
                input.classList.add('is-invalid');
                if (feedback) {
                    feedback.textContent = 'Solo se permiten letras, números, puntos, guiones y guion bajo antes de ' + DOMINIO_VALIDO;
                    feedback.style.display = 'block';
                }
            } else {
                // Correo válido.
                input.classList.remove('is-invalid');
                if (feedback) {
                    feedback.style.display = 'none';
                }
            }
        }

        // Se ejecuta con cada tecla, no solo al salir del campo.
        input.addEventListener('input', validarEnVivo);
        input.addEventListener('blur', validarEnVivo);

        // Por si el campo ya viene con un valor precargado (modo edición).
        validarEnVivo();
    });
});