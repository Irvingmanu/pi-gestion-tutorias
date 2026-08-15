// Logica de la vista de Nueva Solicitud (alumno/solicitud.jsp).
// Depende de dos variables globales que el JSP define ANTES de cargar
// este script: window.PUEDE_ENVIAR (boolean) y window.DISPONIBILIDAD
// (objeto {"2026-08-10": ["13:00","14:00"], ...}), calculado en
// SolicitudServlet.construirDisponibilidadJson.

document.addEventListener('DOMContentLoaded', function () {
    var puedeEnviar = window.PUEDE_ENVIAR === true;
    var form = document.getElementById('formSolicitud');

    /**
     * Intercepta el evento de envío del formulario.
     * Si el usuario no tiene permitido enviar, detiene el evento y muestra una alerta.
     */
    form.addEventListener('submit', function (evento) {
        if (!puedeEnviar) {
            evento.preventDefault();
            // Asegúrate de que la función mostrarAlerta esté definida globalmente en otro script
            mostrarAlerta(
                'advertencia',
                'No puedes enviar tu solicitud',
                'Todavía no tienes un tutor asignado o tu tutor no tiene horarios de atención registrados. Consulta con el coordinador.'
            );
        }
    });
});

// BLOQUE 2: Lógica en cascada de los selectores (Día -> Duración -> Hora)
document.addEventListener('DOMContentLoaded', function () {
    var disponibilidad = window.DISPONIBILIDAD || {};

    var selectDia = document.getElementById('fechaPropuesta');
    var selectDuracion = document.getElementById('duracionPropuesta');
    var selectHora = document.getElementById('horaPropuesta');
    // 1. Inicialización: Poblar el selector de Días con las claves del objeto DISPONIBILIDAD

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
        // padStart asegura que las horas < 10 mantengan el formato de 2 dígitos (ej. "09:00")
        var partes = hora.split(':');
        var horaSiguiente = parseInt(partes[0], 10) + 1;
        return String(horaSiguiente).padStart(2, '0') + ':' + partes[1];
    }

    // 2. Evento: Cambio en el selector de Día
    // Al elegir el Día, se habilita el selector de Duración. Las opciones de 1/2 horas
    // ya deben estar fijas en el HTML. Se reinicia y deshabilita la Hora.
    selectDia.addEventListener('change', function () {
        selectDuracion.disabled = false;
        selectDuracion.querySelector('option[value=""]').textContent = 'Seleccione duración';
        selectDuracion.value = '';

        reiniciarSelect(selectHora, 'Seleccione la duración primero');
        selectHora.disabled = true;
    });

    // 3. Evento: Cambio en el selector de Duración
    // Al elegir la Duración, se calcula y llena la Hora de inicio basándose en la disponibilidad.
    selectDuracion.addEventListener('change', function () {
        reiniciarSelect(selectHora, 'Seleccione hora');

        var fecha = selectDia.value;
        var duracion = parseInt(selectDuracion.value, 10);
        var horasDelDia = disponibilidad[fecha] || [];

// Filtramos las horas disponibles dependiendo de la duración requerida
        var horasValidas = horasDelDia.filter(function (hora) {
            if (duracion === 1) {
                return true;// Para 1 hora, cualquier bloque disponible es válido
            }
            // Duración de 2 horas: solo si el bloque inmediatamente
            // siguiente también está disponible ese mismo día.
            return horasDelDia.indexOf(sumarUnaHora(hora)) !== -1;
        });
        // Poblar el selector de Horas con los resultados válidos
        horasValidas.forEach(function (hora) {
            var opcion = document.createElement('option');
            opcion.value = hora;
            opcion.textContent = hora;
            selectHora.appendChild(opcion);
        });
        // Habilitar el selector de Hora solo si hay opciones disponibles tras el filtro
        selectHora.disabled = horasValidas.length === 0;
    });
});