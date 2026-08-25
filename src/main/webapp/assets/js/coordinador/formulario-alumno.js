
document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('formGuardar');
    if (!form) {
        return;
    }

    const camposTocados = new WeakSet();

    const selectAsignarGrupo = document.getElementById('asignarGrupo');
    const $asignarGrupo = (typeof jQuery !== 'undefined' && jQuery.fn.select2 && selectAsignarGrupo)
        ? jQuery(selectAsignarGrupo)
        : null;

    if ($asignarGrupo) {
        $asignarGrupo.select2({
            theme: 'bootstrap-5',
            width: '100%',
            placeholder: 'Escriba para buscar un grupo...'
        });
    }

    function marcarGrupoInvalido() {
        if (!$asignarGrupo) return;
        selectAsignarGrupo.classList.add('is-invalid');
        $asignarGrupo.next('.select2-container').find('.select2-selection')
            .css('border-color', 'var(--bs-form-invalid-border-color, #dc3545)');
    }

    function limpiarGrupoInvalido() {
        if (!$asignarGrupo) return;
        selectAsignarGrupo.classList.remove('is-invalid');
        $asignarGrupo.next('.select2-container').find('.select2-selection').css('border-color', '');
    }

    if ($asignarGrupo) {
        $asignarGrupo.on('select2:close', function () {
            camposTocados.add(selectAsignarGrupo);
            marcarValidez(selectAsignarGrupo);
            verificarFormulario();
        });
    }

    const inputMatricula = document.getElementById('matricula');
    const inputCorreo = document.getElementById('correo');
    const PLACEHOLDER_MATRICULA_AUTO = 'Se genera automáticamente al elegir el grupo';
    const PLACEHOLDER_CORREO_AUTO = 'Se genera automáticamente al elegir el grupo';
    const PLACEHOLDER_MATRICULA_MANUAL = 'Captura la matrícula ya existente del alumno';
    const PLACEHOLDER_CORREO_MANUAL = 'Captura el correo ya existente del alumno';

    function obtenerCredenciales(idGrupo) {
        const contextPath = document.body.dataset.contextPath || '';
        return fetch(contextPath + '/generarCredenciales?idGrupo=' + encodeURIComponent(idGrupo));
    }

    function alCambiarGrupo() {
        const idGrupo = selectAsignarGrupo.value;
        if (!idGrupo) return;

        limpiarGrupoInvalido();

        const opcionElegida = selectAsignarGrupo.options[selectAsignarGrupo.selectedIndex];
        const cuatri = opcionElegida ? opcionElegida.getAttribute('data-cuatri') : null;

        if (cuatri === '1') {
            inputMatricula.readOnly = true;
            inputCorreo.readOnly = true;
            inputMatricula.placeholder = PLACEHOLDER_MATRICULA_AUTO;
            inputCorreo.placeholder = PLACEHOLDER_CORREO_AUTO;

            obtenerCredenciales(idGrupo)
                .then(function (respuesta) {
                    if (!respuesta.ok) {
                        throw new Error('Respuesta no exitosa de /generarCredenciales');
                    }
                    return respuesta.json();
                })
                .then(function (datos) {
                    inputMatricula.value = datos.matricula;
                    inputMatricula.dispatchEvent(new Event('input'));

                    inputCorreo.value = inputMatricula.value.toLowerCase() + '@utez.edu.mx';
                    inputCorreo.dispatchEvent(new Event('input'));
                })
                .catch(function () {
                    mostrarAlerta('error', 'Error', 'No se pudo generar la matrícula y el correo para este grupo. Intenta de nuevo.');
                });
            return;
        }

        inputMatricula.readOnly = false;
        inputCorreo.readOnly = false;
        inputMatricula.value = '';
        inputCorreo.value = '';
        inputMatricula.placeholder = PLACEHOLDER_MATRICULA_MANUAL;
        inputCorreo.placeholder = PLACEHOLDER_CORREO_MANUAL;

        camposTocados.delete(inputMatricula);
        camposTocados.delete(inputCorreo);
        marcarValidez(inputMatricula);
        marcarValidez(inputCorreo);
        verificarFormulario();
    }

    if (selectAsignarGrupo && inputMatricula && inputCorreo) {
        if ($asignarGrupo) {
            $asignarGrupo.on('change', alCambiarGrupo);
        } else {
            selectAsignarGrupo.addEventListener('change', alCambiarGrupo);
        }
    }

    const btnGuardar = document.getElementById('btnGuardar');

    const inputsValidables = form.querySelectorAll('input, select');

    const REGEX_MATRICULA = /^[a-zA-Z0-9]{10}$/;
    const REGEX_CORREO = /^[a-zA-Z0-9._-]+@utez\.edu\.mx$/;

    const MENSAJE_CAMPO_OBLIGATORIO = 'Este campo es obligatorio.';

    function obtenerFeedback(input) {
        if (input.nextElementSibling && input.nextElementSibling.classList.contains('invalid-feedback')) {
            return input.nextElementSibling;
        }
        return input.parentElement?.querySelector('.invalid-feedback') || null;
    }

    function marcarValidez(input) {

        if (input === selectAsignarGrupo) {
            if (!camposTocados.has(input) || input.value) {
                limpiarGrupoInvalido();
            } else {
                marcarGrupoInvalido();
            }
            return;
        }

        let esValido = input.checkValidity();
        if (input === inputMatricula) {
            esValido = esValido && REGEX_MATRICULA.test(input.value);
        } else if (input === inputCorreo) {
            esValido = esValido && REGEX_CORREO.test(input.value);
        }

        const feedback = obtenerFeedback(input);

        if (!camposTocados.has(input) || esValido) {
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

    function verificarFormulario() {
        let esValido = true;
        inputsValidables.forEach(function (input) {
            if (input === selectAsignarGrupo) {
                if (!selectAsignarGrupo.value) esValido = false;
                return;
            }
            if (!input.checkValidity()) {
                esValido = false;
            }
        });

        if (!REGEX_MATRICULA.test(inputMatricula.value)) {
            esValido = false;
        }
        if (!REGEX_CORREO.test(inputCorreo.value)) {
            esValido = false;
        }

        btnGuardar.disabled = !esValido;
    }

    inputsValidables.forEach(function (input) {
        input.addEventListener('input', function () {
            camposTocados.add(this);
            marcarValidez(this);
            verificarFormulario();
        });

        input.addEventListener('change', function () {
            camposTocados.add(this);
            marcarValidez(this);
            verificarFormulario();
        });

        input.addEventListener('blur', function () {
            camposTocados.add(this);
            marcarValidez(this);
            verificarFormulario();
        });

        input.addEventListener('paste', function () {
            const inputPegado = this;
            setTimeout(function () {
                camposTocados.add(inputPegado);
                marcarValidez(inputPegado);
                verificarFormulario();
            }, 0);
        });

        input.addEventListener('animationstart', function (e) {
            if (e.animationName === 'onAutoFillStart') {
                camposTocados.add(this);
                marcarValidez(this);
                verificarFormulario();
            }
        });
    });

    function marcarSiTieneValor(input) {
        if (input.value) {
            camposTocados.add(input);
        }
        marcarValidez(input);
    }

    function marcarValidezFormularioCompleto() {
        inputsValidables.forEach(marcarSiTieneValor);
        verificarFormulario();
    }

    marcarValidezFormularioCompleto();

    window.addEventListener('load', marcarValidezFormularioCompleto);

    setInterval(marcarValidezFormularioCompleto, 500);

    const mensajeError = document.body.dataset.mensajeError;
    if (mensajeError) {
        mostrarAlerta('error', 'Error', mensajeError);
    }
});
