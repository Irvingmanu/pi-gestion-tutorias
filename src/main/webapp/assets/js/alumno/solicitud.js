
document.addEventListener('DOMContentLoaded', function () {
    var puedeEnviar = window.PUEDE_ENVIAR === true;
    var form = document.getElementById('formSolicitud');

    form.addEventListener('submit', function (evento) {
        if (!puedeEnviar) {
            evento.preventDefault();

            mostrarAlerta(
                'advertencia',
                'No puedes enviar tu solicitud',
                'Todavía no tienes un tutor asignado o tu tutor no tiene horarios de atención registrados. Consulta con el coordinador.'
            );
        }
    });
});

document.addEventListener('DOMContentLoaded', function () {
    var disponibilidad = window.DISPONIBILIDAD || {};
    var puedeEnviar = window.PUEDE_ENVIAR === true;

    var form = document.getElementById('formSolicitud');
    var btnEnviar = document.getElementById('btnEnviarSolicitud');
    var selectDia = document.getElementById('fechaPropuesta');
    var selectDuracion = document.getElementById('duracionPropuesta');
    var selectHora = document.getElementById('horaPropuesta');
    if (!form || !btnEnviar || !selectDia || !selectDuracion || !selectHora) return;

    var camposTocados = new WeakSet();
    var inputsValidables = form.querySelectorAll('input, select, textarea');
    var MENSAJE_CAMPO_OBLIGATORIO = 'Este campo es obligatorio.';

    function obtenerFeedback(input) {
        if (input.nextElementSibling && input.nextElementSibling.classList.contains('invalid-feedback')) {
            return input.nextElementSibling;
        }
        return input.parentElement ? input.parentElement.querySelector('.invalid-feedback') : null;
    }

    function marcarValidez(input) {
        var feedback = obtenerFeedback(input);
        var esValido = input.checkValidity();

        if (!camposTocados.has(input) || esValido) {
            input.classList.remove('is-invalid');
            if (feedback) feedback.style.display = 'none';
            return;
        }

        input.classList.add('is-invalid');
        if (!feedback) return;

        if (feedback.dataset.msgPatron === undefined) {
            feedback.dataset.msgPatron = feedback.textContent.trim();
        }

        feedback.textContent = input.validity.valueMissing
            ? (input.dataset.msgRequerido || MENSAJE_CAMPO_OBLIGATORIO)
            : feedback.dataset.msgPatron;

        feedback.style.display = 'block';
    }

    function verificarFormulario() {
        var esValido = true;
        inputsValidables.forEach(function (input) {

            if (!input.checkValidity()) {
                esValido = false;
            }
        });
        btnEnviar.disabled = !esValido || !puedeEnviar;
    }

    inputsValidables.forEach(function (input) {
        ['input', 'change', 'blur'].forEach(function (evento) {
            input.addEventListener(evento, function () {
                camposTocados.add(this);
                marcarValidez(this);
                verificarFormulario();
            });
        });
    });

    Object.keys(disponibilidad).forEach(function (fecha) {
        var opcion = document.createElement('option');
        opcion.value = fecha;
        opcion.textContent = fecha;
        selectDia.appendChild(opcion);
    });

    function reiniciarSelect(select, textoPlaceholder) {
        select.innerHTML = '';
        var opcionVacia = document.createElement('option');
        opcionVacia.value = '';
        opcionVacia.textContent = textoPlaceholder;
        opcionVacia.disabled = true;
        opcionVacia.selected = true;
        select.appendChild(opcionVacia);
    }

    function sumarUnaHora(hora) {

        var partes = hora.split(':');
        var horaSiguiente = parseInt(partes[0], 10) + 1;
        return String(horaSiguiente).padStart(2, '0') + ':' + partes[1];
    }

    selectDia.addEventListener('change', function () {
        selectDuracion.disabled = false;
        selectDuracion.querySelector('option[value=""]').textContent = 'Seleccione duración';
        selectDuracion.value = '';

        reiniciarSelect(selectHora, 'Seleccione la duración primero');
        selectHora.disabled = true;

        marcarValidez(selectHora);
        verificarFormulario();
    });

    selectDuracion.addEventListener('change', function () {
        reiniciarSelect(selectHora, 'Seleccione hora');

        var fecha = selectDia.value;
        var duracion = parseInt(selectDuracion.value, 10);
        var horasDelDia = disponibilidad[fecha] || [];

        var horasValidas = horasDelDia.filter(function (hora) {
            if (duracion === 1) {
                return true;
            }

            return horasDelDia.indexOf(sumarUnaHora(hora)) !== -1;
        });

        horasValidas.forEach(function (hora) {
            var opcion = document.createElement('option');
            opcion.value = hora;
            opcion.textContent = hora;
            selectHora.appendChild(opcion);
        });

        selectHora.disabled = horasValidas.length === 0;

        marcarValidez(selectHora);
        verificarFormulario();
    });

    verificarFormulario();
});