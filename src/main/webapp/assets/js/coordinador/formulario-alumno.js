// Logica de coordinador/formulario-alumno.jsp: generacion de Matricula/Correo (via
// GenerarCredencialesServlet) al elegir "Asignar a Grupo", y validacion en tiempo real del
// formulario (borde rojo + mensaje, igual que formulario-area.js). El marcado es
// "tradicional": un campo vacio u invalido NO se marca en rojo apenas carga la pantalla,
// solo despues de que el usuario lo toca (escribe, pega, sale de el) o el navegador lo
// autocompleta (ver camposTocados mas abajo); el boton Guardar si evalua la validez real
// del formulario completo desde el inicio. Tambien incluye soporte para
// autocompletado del navegador: Chrome/Edge rellenan campos (ej. un correo ya usado antes)
// sin disparar 'input' de forma confiable, asi que el marcado visual no aparecia aunque el
// valor fuera invalido. Se cubre con el evento 'change', la deteccion via CSS
// animation-name ('onAutoFillStart', ver el <style> de formulario-alumno.jsp), revalidacion
// en 'load' y un polling de respaldo. Misma estrategia que formulario-area.js. Tambien
// incluye el toast de error que antes vivia inline en el JSP.

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('formGuardar');
    if (!form) {
        return;
    }

    // Campos que el usuario ya toco (escribio, pego, salio de el, o el navegador
    // autocompleto): el marcado en rojo (borde + mensaje) solo aparece para campos que
    // estan en este set, nunca para uno que el usuario aun no ha tocado. Evita que un
    // "Nuevo Alumno" recien abierto se vea todo en rojo antes de que el usuario haga algo.
    const camposTocados = new WeakSet();

    // ==========================================================================
    // SEARCHABLE SELECT (Select2) para "Asignar a Grupo": la lista de grupos crece sin
    // tope (Carrera x Cuatrimestre x Letra), y un <select> nativo deja de ser usable
    // pasados ~50. Mismo patron defensivo que #alumnoBuscador en
    // tutor/tutoria-individual.js: si el CDN de jQuery/Select2 no cargo, el <select>
    // nativo sigue funcionando (con el placeholder ya escrito en su primer <option>).
    // El tema 'bootstrap-5' es el que global.css estiliza con los bordes redondeados
    // de form-control-figma (.select2-container--bootstrap-5 .select2-selection).
    // ==========================================================================
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

    // Parche Bootstrap <-> Select2: el <select> real queda oculto (display:none) tras el
    // widget de Select2, asi que Bootstrap no puede pintarle su borde rojo con solo ver el
    // <select> oculto. Estas dos funciones son el "marcarValidez" de #asignarGrupo (ver mas
    // abajo, se invocan desde ahi en vez de tocar is-invalid/.invalid-feedback a mano):
    // se llaman al cerrar el desplegable sin elegir nada (select2:close, equivalente al
    // "blur" de un input normal) y al elegir un grupo valido. Mismo patron que
    // #alumnoBuscador en tutor/tutoria-individual.js.
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

    // ==========================================================================
    // GENERACION DE CREDENCIALES al elegir "Asignar a Grupo", con desbloqueo inteligente
    // segun el data-cuatri de la opcion elegida (Opcion B):
    //   - Cuatrimestre 1 (alumno de nuevo ingreso): Matricula/Correo quedan readonly y
    //     se generan solos via /generarCredenciales.
    //   - Cuatrimestre > 1 (alumno rezagado, ya trae matricula de una generacion previa):
    //     se quita el readonly, se limpian los dos campos y NO se dispara el fetch; el
    //     coordinador captura a mano la matricula y el correo YA EXISTENTES del alumno.
    // Solo dispara en modo alta (no hay listener automatico al cargar), asi que en
    // edicion se conservan los valores ya guardados del alumno hasta que el usuario
    // cambie el grupo a mano.
    // OJO: si Select2 esta activo, el 'change' NO siempre llega a un
    // addEventListener('change', ...) nativo sobre el <select> (mismo motivo por el que
    // #alumnoBuscador en tutor/tutoria-individual.js usa $alumnoBuscador.on('change', ...)
    // en vez de addEventListener); por eso aqui tambien se engancha con jQuery cuando
    // Select2 esta disponible, y solo se cae a addEventListener nativo si no lo esta.
    // ==========================================================================
    const inputMatricula = document.getElementById('matricula');
    const inputCorreo = document.getElementById('correo');
    const PLACEHOLDER_MATRICULA_AUTO = 'Se genera automáticamente al elegir el grupo';
    const PLACEHOLDER_CORREO_AUTO = 'Se genera automáticamente al elegir el grupo';
    const PLACEHOLDER_MATRICULA_MANUAL = 'Captura la matrícula ya existente del alumno';
    const PLACEHOLDER_CORREO_MANUAL = 'Captura el correo ya existente del alumno';

    // GET /generarCredenciales?idGrupo=X (GenerarCredencialesServlet): genera la Matricula
    // en el servidor (Anio+Periodo del grupo+Sigla de carrera+Contador). contextPath sale
    // de <body data-context-path> (mismo patron que data-mensaje-error mas abajo) porque
    // este archivo es un JS estatico, sin acceso a EL/JSTL.
    function obtenerCredenciales(idGrupo) {
        const contextPath = document.body.dataset.contextPath || '';
        return fetch(contextPath + '/generarCredenciales?idGrupo=' + encodeURIComponent(idGrupo));
    }

    function alCambiarGrupo() {
        const idGrupo = selectAsignarGrupo.value;
        if (!idGrupo) return;

        // Apenas se elige un grupo valido, se limpia cualquier estado de error que haya
        // quedado forzado por select2:close.
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
                    inputMatricula.dispatchEvent(new Event('input')); // marca/desmarca is-invalid + forzado a mayusculas ya existente

                    inputCorreo.value = inputMatricula.value.toLowerCase() + '@utez.edu.mx';
                    inputCorreo.dispatchEvent(new Event('input'));
                })
                .catch(function () {
                    mostrarAlerta('error', 'Error', 'No se pudo generar la matrícula y el correo para este grupo. Intenta de nuevo.');
                });
            return;
        }

        // Cuatrimestre > 1 (o data-cuatri ausente/invalido, por seguridad): no se generan
        // credenciales nuevas. El alumno rezagado ya tiene matricula/correo de cuando
        // ingreso; se capturan a mano en vez de arriesgar un duplicado o un dato erroneo.
        inputMatricula.readOnly = false;
        inputCorreo.readOnly = false;
        inputMatricula.value = '';
        inputCorreo.value = '';
        inputMatricula.placeholder = PLACEHOLDER_MATRICULA_MANUAL;
        inputCorreo.placeholder = PLACEHOLDER_CORREO_MANUAL;
        // Se vacian para captura manual: no se marcan como "tocados" (se quita cualquier
        // marca previa sin encenderla de nuevo), para que no aparezcan en rojo hasta que
        // el coordinador realmente escriba algo en ellos.
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

    // ==========================================================================
    // VALIDACIÓN EN VIVO DEL FORMULARIO (misma logica que formulario-area.js: marca
    // borde rojo + muestra el <div class="invalid-feedback"> de cada campo, tanto al
    // escribir como desde que carga la pantalla).
    // ==========================================================================
    const btnGuardar = document.getElementById('btnGuardar');
    // Ojo: "input, select" a proposito, no "input[required], select[required]". Apellido
    // materno es opcional (sin required) pero SI tiene pattern (letras y espacios); si
    // solo se enganchan los campos required, escribir un caracter invalido ahi nunca
    // marca is-invalid ni bloquea Guardar, aunque el patron lo rechace. Esto tambien
    // incluye #asignarGrupo (su marcado visual se resuelve aparte, ver marcarValidez).
    const inputsValidables = form.querySelectorAll('input, select');

    // Matricula/Correo quedan readonly cuando el grupo elegido es de 1er cuatrimestre
    // (se generan solos via obtenerCredenciales), y por spec un <input readonly> no es
    // "mutable": el navegador NO evalua "required" ni "pattern" sobre ellos
    // (checkValidity() los da por validos aunque esten vacios o con un valor viejo). Si el
    // fetch llegara a fallar sin dejar rastro visible, el marcado visual no lo reflejaria
    // y Guardar se habilitaria con Matricula/Correo en blanco. Se revisan aparte con el
    // mismo regex que el servidor, integrado directo en marcarValidez/verificarFormulario.
    const REGEX_MATRICULA = /^[a-zA-Z0-9]{10}$/;
    const REGEX_CORREO = /^[a-zA-Z0-9._-]+@utez\.edu\.mx$/;

    // Mensaje generico de respaldo, solo se usa si un input required NO tiene
    // su propio atributo data-msg-requerido en el HTML.
    const MENSAJE_CAMPO_OBLIGATORIO = 'Este campo es obligatorio.';

    // Encuentra el <div class="invalid-feedback"> asociado a un input: en este
    // formulario siempre es el hermano siguiente directo del input/select.
    function obtenerFeedback(input) {
        if (input.nextElementSibling && input.nextElementSibling.classList.contains('invalid-feedback')) {
            return input.nextElementSibling;
        }
        return input.parentElement?.querySelector('.invalid-feedback') || null;
    }

    // Marca (o desmarca) un input individual como invalido: borde rojo + mensaje visible,
    // igual que en formulario-area.jsp. Si el campo es obligatorio y esta vacio se muestra
    // data-msg-requerido (o el generico si no lo tiene); para cualquier otro tipo de
    // invalidez (pattern, formato) se muestra el mensaje original que ya trae el HTML en
    // el .invalid-feedback.
    function marcarValidez(input) {
        // #asignarGrupo lo oculta Select2 (display:none): su marcado visual no pasa por
        // is-invalid/.invalid-feedback sino por el borde del widget (ver
        // marcarGrupoInvalido/limpiarGrupoInvalido mas arriba). Igual que los demas
        // campos, solo se marca en rojo si el usuario ya lo toco (ver camposTocados).
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

        // Solo se muestra el borde rojo/mensaje si el campo ya fue tocado por el usuario:
        // uno invalido pero aun no tocado se trata visualmente como si estuviera bien.
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

        // Cubre el caso de pegar un valor con Ctrl+V (ej. pegar un correo @gmail.com en
        // vez de escribirlo). El evento 'paste' se dispara ANTES de que el valor pegado
        // quede escrito en el input, por eso se valida un tick despues con
        // setTimeout(..., 0).
        input.addEventListener('paste', function () {
            const inputPegado = this;
            setTimeout(function () {
                camposTocados.add(inputPegado);
                marcarValidez(inputPegado);
                verificarFormulario();
            }, 0);
        });

        // Deteccion de autofill en Chrome/Edge: estos navegadores no disparan 'input' al
        // autocompletar un campo (ej. un correo que el navegador ya tenia guardado de una
        // carga anterior), pero si aplican una animacion CSS que se puede "escuchar" (ver
        // @keyframes onAutoFillStart y la regla input:-webkit-autofill en el <style> del
        // JSP). En cuanto el navegador rellena el campo, esto lo revalida al instante.
        // Misma tecnica que en formulario-area.js.
        input.addEventListener('animationstart', function (e) {
            if (e.animationName === 'onAutoFillStart') {
                camposTocados.add(this);
                marcarValidez(this);
                verificarFormulario();
            }
        });
    });

    // Marca como "tocado" (ver camposTocados) cualquier campo que YA traiga un valor: es
    // la señal de que algo lo lleno (modo edicion con datos guardados, autofill del
    // navegador, o un script/extension que asigno el value directamente por JS sin
    // disparar eventos). Un campo vacio nunca se marca aqui, para no adelantarse a que el
    // usuario lo toque.
    function marcarSiTieneValor(input) {
        if (input.value) {
            camposTocados.add(input);
        }
        marcarValidez(input);
    }

    // Revisa todos los campos del form. Se usa en la verificacion inicial, en 'load' y en
    // el polling de abajo.
    function marcarValidezFormularioCompleto() {
        inputsValidables.forEach(marcarSiTieneValor);
        verificarFormulario();
    }

    // Verificación inicial: en modo edición, si el alumno ya trae datos guardados
    // invalidos (ej. un valor que ya no cumple el patron actual) se marcan de una vez; en
    // modo "Nuevo Alumno" (todo vacio) no se marca nada todavia, hasta que el coordinador
    // toque un campo.
    marcarValidezFormularioCompleto();

    // Revalidacion tras el evento 'load' de la ventana: cubre el caso en que el
    // autocompletado del navegador rellena campos DESPUES de DOMContentLoaded, sin
    // disparar 'input' ni 'change' (pasa seguido en Chrome).
    window.addEventListener('load', marcarValidezFormularioCompleto);

    // Revision periodica (polling), igual que en formulario-area.js: cubre el caso de
    // scripts o extensiones (ej. herramientas de "form filler" para pruebas) que asignan
    // el valor de un input directamente por JS (input.value = "..."). Esa asignacion NO
    // dispara 'input', 'change' ni la animacion de autofill, asi que ningun listener se
    // entera. Revisar cada 500ms es barato para un formulario de este tamaño y garantiza
    // que el marcado visual siempre refleje el valor real, sin marcar como "tocado" un
    // campo que sigue vacio.
    setInterval(marcarValidezFormularioCompleto, 500);

    // ==========================================================================
    // TOAST DE ERROR (mensajeError resuelto server-side, expuesto via data-attribute)
    // ==========================================================================
    const mensajeError = document.body.dataset.mensajeError;
    if (mensajeError) {
        mostrarAlerta('error', 'Error', mensajeError);
    }
});
