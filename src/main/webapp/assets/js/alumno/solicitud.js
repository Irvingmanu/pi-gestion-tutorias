// Logica de la vista de Nueva Solicitud (alumno/solicitud.jsp).
// Depende de dos variables globales que el JSP define ANTES de cargar
// este script: window.PUEDE_ENVIAR (boolean) y window.DISPONIBILIDAD
// (objeto {"2026-08-10": ["13:00","14:00"], ...}), calculado en
// SolicitudServlet.construirDisponibilidadJson.

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

    var selectDia = document.getElementById('fechaPropuesta');
    var selectDuracion = document.getElementById('duracionPropuesta');
    var selectHora = document.getElementById('horaPropuesta');

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

    // Al elegir el Día, se habilita Duración (sus opciones 1/2 horas ya
    // están fijas en el HTML, aquí solo se activa el select)
    selectDia.addEventListener('change', function () {
        selectDuracion.disabled = false;
        selectDuracion.querySelector('option[value=""]').textContent = 'Seleccione duración';
        selectDuracion.value = '';

        reiniciarSelect(selectHora, 'Seleccione la duración primero');
        selectHora.disabled = true;
    });

    // Al elegir la Duración, se calcula y llena la Hora de inicio
    selectDuracion.addEventListener('change', function () {
        reiniciarSelect(selectHora, 'Seleccione hora');

        var fecha = selectDia.value;
        var duracion = parseInt(selectDuracion.value, 10);
        var horasDelDia = disponibilidad[fecha] || [];

        var horasValidas = horasDelDia.filter(function (hora) {
            if (duracion === 1) {
                return true;
            }
            // Duración de 2 horas: solo si el bloque inmediatamente
            // siguiente también está disponible ese mismo día.
            return horasDelDia.indexOf(sumarUnaHora(hora)) !== -1;
        });

        horasValidas.forEach(function (hora) {
            var opcion = document.createElement('option');
            opcion.value = hora;
            opcion.textContent = hora;
            selectHora.appendChild(opcion);
        });

        selectHora.disabled = horasValidas.length === 0;
    });
});