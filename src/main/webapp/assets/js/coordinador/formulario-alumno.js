// Logica de coordinador/formulario-alumno.jsp: filtro OPCIONAL de Academia sobre
// #carreraSelect (oculta opciones en cliente, sin fetch ni bloquear el select), la
// cascada Carrera -> Cuatrimestre (segun el NIVEL de la carrera, en memoria),
// validacion en tiempo real del formulario, y el toast de error que antes vivia
// inline en el JSP.

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
    });

    // Carga inicial en modo edición: si ya hay una carrera preseleccionada (JSTL),
    // se puebla el cuatrimestre de una vez con el valor que ya tenía el alumno.
    if (selectCarrera.value) {
        const opcionInicial = selectCarrera.options[selectCarrera.selectedIndex];
        const nivelInicial = opcionInicial ? opcionInicial.getAttribute('data-nivel') : null;
        poblarCuatrimestres(nivelInicial, cuatrimestreActual);
    }

    // ==========================================================================
    // VALIDACIÓN EN VIVO DEL FORMULARIO
    // ==========================================================================
    const btnGuardar = document.getElementById('btnGuardar');
    // Ojo: "input, select" a proposito, no "input[required], select[required]". Apellido
    // materno es opcional (sin required) pero SI tiene pattern (letras y espacios); si
    // solo se enganchan los campos required, escribir un caracter invalido ahi nunca
    // marca is-invalid ni bloquea Guardar, aunque el patron lo rechace.
    const inputsValidables = form.querySelectorAll('input, select');

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

    // Verificación inicial por si estamos en modo edición
    verificarFormulario();

    // ==========================================================================
    // TOAST DE ERROR (mensajeError resuelto server-side, expuesto via data-attribute)
    // ==========================================================================
    const mensajeError = document.body.dataset.mensajeError;
    if (mensajeError) {
        mostrarAlerta('error', 'Error', mensajeError);
    }
});
