// Logica de coordinador/formulario-alumno.jsp: filtro OPCIONAL de Academia sobre
// #carreraSelect (oculta opciones en cliente, sin fetch ni bloquear el select), la
// cascada Carrera -> Cuatrimestre (segun el NIVEL de la carrera, en memoria),
// validacion en tiempo real del formulario (borde rojo + mensaje, igual que
// formulario-area.js) y el toast de error que antes vivia inline en el JSP.
//
// Tambien soporta autocompletado del navegador: Chrome/Edge rellenan campos
// (ej. un correo ya usado antes) sin disparar 'input' de forma confiable, asi
// que el marcado visual (borde rojo + mensaje) no aparecia aunque el valor
// fuera invalido. Se cubre con el evento 'change', la deteccion via CSS
// animation-name ('onAutoFillStart', ver el <style> de formulario-alumno.jsp),
// revalidacion en 'load' y un polling de respaldo. Misma estrategia que
// formulario-area.js.

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('formGuardar');
    if (!form) {
        return;
    }

    const selectAcademiaFiltro = document.getElementById('academiaFiltro');
    const selectCarrera = document.getElementById('carreraSelect');
    const selectCuatrimestre = document.getElementById('cuatrimestre');
    const cuatrimestreActual = selectCuatrimestre.dataset.cuatrimestreActual;

    // ==========================================================================
    // FILTRO OPCIONAL: Academia -> Carrera. #carreraSelect ya viene con TODAS las
    // carreras del sistema renderizadas (JSTL) y habilitado desde el inicio; este
    // filtro solo oculta (display:none) las <option> cuyo data-academia-id no
    // coincida, en el cliente, sin ir al servidor ni deshabilitar nada.
    // ==========================================================================
    function aplicarFiltroAcademia() {
        const idAcademia = selectAcademiaFiltro.value;
        let opcionSeleccionadaSigueVisible = false;

        Array.prototype.forEach.call(selectCarrera.options, function (opcion) {
            if (!opcion.value) {
                return; // el placeholder "Seleccione la carrera" siempre se conserva
            }

            const coincide = !idAcademia || opcion.getAttribute('data-academia-id') === idAcademia;
            opcion.style.display = coincide ? '' : 'none';
            if (coincide && opcion.selected) {
                opcionSeleccionadaSigueVisible = true;
            }
        });

        // Si la carrera que estaba elegida ya no pertenece a la academia filtrada,
        // se limpia la seleccion: no tiene sentido dejar elegida una opcion oculta.
        if (selectCarrera.value && !opcionSeleccionadaSigueVisible) {
            selectCarrera.value = '';
            selectCarrera.dispatchEvent(new Event('change'));
        }
    }

    selectAcademiaFiltro.addEventListener('change', aplicarFiltroAcademia);
    // Aplica el filtro apenas carga la pantalla, respetando la academia que ya
    // pudiera venir preseleccionada en modo edición.
    aplicarFiltroAcademia();

    // ==========================================================================
    // CASCADA: Carrera -> Cuatrimestre (en memoria, segun el NIVEL de la carrera
    // elegida). TSU -> cuatrimestres 1 a 6. Ingeniería (ING) -> cuatrimestres 7 a 10.
    // ==========================================================================
    function poblarCuatrimestres(nivel, valorPreseleccionado) {
        selectCuatrimestre.innerHTML = '';

        if (!nivel) {
            selectCuatrimestre.disabled = true;
            const opcionVacia = document.createElement('option');
            opcionVacia.value = '';
            opcionVacia.textContent = 'Seleccione primero la carrera';
            opcionVacia.selected = true;
            selectCuatrimestre.appendChild(opcionVacia);
            return;
        }

        const desde = nivel === 'TSU' ? 1 : 7;
        const hasta = nivel === 'TSU' ? 6 : 10;

        const opcionVacia = document.createElement('option');
        opcionVacia.value = '';
        opcionVacia.textContent = 'Seleccione el cuatrimestre';
        selectCuatrimestre.appendChild(opcionVacia);

        let huboSeleccion = false;
        for (let numero = desde; numero <= hasta; numero++) {
            const opcion = document.createElement('option');
            opcion.value = String(numero);
            opcion.textContent = numero + '°';
            if (valorPreseleccionado && String(numero) === String(valorPreseleccionado)) {
                opcion.selected = true;
                huboSeleccion = true;
            }
            selectCuatrimestre.appendChild(opcion);
        }
        if (!huboSeleccion) {
            opcionVacia.selected = true;
        }

        selectCuatrimestre.disabled = false;
    }

    selectCarrera.addEventListener('change', function () {
        const opcionElegida = selectCarrera.options[selectCarrera.selectedIndex];
        const nivel = opcionElegida ? opcionElegida.getAttribute('data-nivel') : null;
        poblarCuatrimestres(nivel, null);
        selectCuatrimestre.dispatchEvent(new Event('change'));
        // La cascada reconstruye #cuatrimestre (innerHTML = '') y eso no dispara
        // 'input'/'change' en el propio #cuatrimestre para el marcado visual;
        // se revalida a mano tras reconstruirlo.
        marcarValidez(selectCuatrimestre);
        verificarFormulario();
    });

    // Carga inicial en modo edición: si ya hay una carrera preseleccionada (JSTL),
    // se puebla el cuatrimestre de una vez con el valor que ya tenía el alumno.
    if (selectCarrera.value) {
        const opcionInicial = selectCarrera.options[selectCarrera.selectedIndex];
        const nivelInicial = opcionInicial ? opcionInicial.getAttribute('data-nivel') : null;
        poblarCuatrimestres(nivelInicial, cuatrimestreActual);
    }

    // ==========================================================================
    // VALIDACIÓN EN VIVO DEL FORMULARIO (misma logica que formulario-area.js:
    // marca borde rojo + muestra el <div class="invalid-feedback"> de cada
    // campo, tanto al escribir como desde que carga la pantalla).
    // ==========================================================================
    const btnGuardar = document.getElementById('btnGuardar');
    // Ojo: "input, select" a proposito, no "input[required], select[required]". Apellido
    // materno es opcional (sin required) pero SI tiene pattern (letras y espacios); si
    // solo se enganchan los campos required, escribir un caracter invalido ahi nunca
    // marca is-invalid ni bloquea Guardar, aunque el patron lo rechace.
    const inputsValidables = form.querySelectorAll('input, select');

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

    // Marca (o desmarca) un input individual como invalido: borde rojo + mensaje
    // visible, igual que en formulario-area.jsp. Si el campo es obligatorio y
    // esta vacio se muestra data-msg-requerido (o el generico si no lo tiene);
    // para cualquier otro tipo de invalidez (pattern, formato) se muestra el
    // mensaje original que ya trae el HTML en el .invalid-feedback.
    function marcarValidez(input) {
        const feedback = obtenerFeedback(input);

        if (input.checkValidity()) {
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
            if (!input.checkValidity()) {
                esValido = false;
            }
        });
        btnGuardar.disabled = !esValido;
    }

    inputsValidables.forEach(function (input) {
        input.addEventListener('input', function () {
            marcarValidez(this);
            verificarFormulario();
        });

        input.addEventListener('change', function () {
            marcarValidez(this);
            verificarFormulario();
        });

        input.addEventListener('blur', function () {
            marcarValidez(this);
            verificarFormulario();
        });

        // Cubre el caso de pegar un valor con Ctrl+V (ej. pegar un correo
        // @gmail.com en vez de escribirlo). El evento 'paste' se dispara ANTES
        // de que el valor pegado quede escrito en el input, por eso se valida
        // un tick despues con setTimeout(..., 0).
        input.addEventListener('paste', function () {
            const inputPegado = this;
            setTimeout(function () {
                marcarValidez(inputPegado);
                verificarFormulario();
            }, 0);
        });

        // Deteccion de autofill en Chrome/Edge: estos navegadores no disparan
        // 'input' al autocompletar un campo (ej. un correo que el navegador ya
        // tenia guardado de una carga anterior), pero si aplican una animacion
        // CSS que se puede "escuchar" (ver @keyframes onAutoFillStart y la
        // regla input:-webkit-autofill en el <style> del JSP). En cuanto el
        // navegador rellena el campo, esto lo revalida al instante. Misma
        // tecnica que en formulario-area.js.
        input.addEventListener('animationstart', function (e) {
            if (e.animationName === 'onAutoFillStart') {
                marcarValidez(this);
                verificarFormulario();
            }
        });
    });

    // Revisa todos los campos del form y marca los invalidos. Se usa en la
    // verificacion inicial, en 'load' y en el polling de abajo.
    function marcarValidezFormularioCompleto() {
        inputsValidables.forEach(marcarValidez);
        verificarFormulario();
    }

    // Verificación inicial: marca de una vez todos los campos invalidos (ej. al
    // cargar la pantalla vacia en modo "Nuevo Alumno"), igual que en el de áreas.
    marcarValidezFormularioCompleto();

    // Revalidacion tras el evento 'load' de la ventana: cubre el caso en que el
    // autocompletado del navegador rellena campos DESPUES de DOMContentLoaded,
    // sin disparar 'input' ni 'change' (pasa seguido en Chrome).
    window.addEventListener('load', marcarValidezFormularioCompleto);

    // Revision periodica (polling), igual que en formulario-area.js: cubre el
    // caso de scripts o extensiones (ej. herramientas de "form filler" para
    // pruebas) que asignan el valor de un input directamente por JS
    // (input.value = "..."). Esa asignacion NO dispara 'input', 'change' ni la
    // animacion de autofill, asi que ningun listener se entera. Revisar cada
    // 500ms es barato para un formulario de este tamaño y garantiza que el
    // marcado visual siempre refleje el valor real.
    setInterval(marcarValidezFormularioCompleto, 500);

    // ==========================================================================
    // TOAST DE ERROR (mensajeError resuelto server-side, expuesto via data-attribute)
    // ==========================================================================
    const mensajeError = document.body.dataset.mensajeError;
    if (mensajeError) {
        mostrarAlerta('error', 'Error', mensajeError);
    }
});