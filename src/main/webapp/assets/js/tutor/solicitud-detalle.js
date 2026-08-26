/**
 * Controla la vista de detalle de una solicitud individual del tutor: el modal
 * de confirmación para aceptar/rechazar la solicitud, y el panel de
 * reprogramación con sus selects de fecha y hora poblados según la
 * disponibilidad del tutor y la duración de la solicitud.
 * @author Sebastian-CR7
 * @date 2026-08-09
 */
document.addEventListener('DOMContentLoaded', function () {

    var modalEl = document.getElementById('modalConfirmacion');
    if (modalEl) {
        var modalConfirmacion = new bootstrap.Modal(modalEl);

        var tituloEl = document.getElementById('confirmacionTitulo');
        var mensajeEl = document.getElementById('confirmacionMensaje');
        var iconoEl = document.getElementById('confirmacionIcono');
        var circuloEl = document.getElementById('confirmacionIconoCirculo');
        var btnAceptarModal = document.getElementById('btnConfirmacionAceptar');

        var accionPendiente = null;

        /**
         * Configura el título, mensaje, ícono y color del modal de confirmación
         * según la acción a confirmar, y lo muestra.
         * @param {string} accion la acción a confirmar ('aceptar' o 'rechazar')
         * @returns {void}
         */
        function abrirConfirmacion(accion) {
            accionPendiente = accion;

            circuloEl.classList.remove('confirmacion-icono--exito', 'confirmacion-icono--critica', 'confirmacion-icono--advertencia');
            btnAceptarModal.classList.remove('btn-confirmar-exito', 'btn-confirmar-critico', 'btn-confirmar-advertencia');

            if (accion === 'aceptar') {
                tituloEl.textContent = '¿Aceptar solicitud?';
                mensajeEl.textContent = 'El alumno será notificado de que su tutoría fue confirmada.';
                iconoEl.src = iconoEl.getAttribute('data-base-path') + 'exito.png';
                circuloEl.classList.add('confirmacion-icono--exito');
                btnAceptarModal.classList.add('btn-confirmar-exito');
            } else {
                tituloEl.textContent = '¿Negar solicitud?';
                mensajeEl.textContent = 'El alumno será notificado de que su solicitud fue rechazada.';
                iconoEl.src = iconoEl.getAttribute('data-base-path') + 'advertencia.png';
                circuloEl.classList.add('confirmacion-icono--advertencia');
                btnAceptarModal.classList.add('btn-confirmar-advertencia');
            }

            modalConfirmacion.show();
        }

        var btnAceptar = document.getElementById('btnAceptar');
        var btnNegar = document.getElementById('btnNegar');

        if(btnAceptar) btnAceptar.addEventListener('click', function () { abrirConfirmacion('aceptar'); });
        if(btnNegar) btnNegar.addEventListener('click', function () { abrirConfirmacion('rechazar'); });

        btnAceptarModal.addEventListener('click', function () {
            if (accionPendiente === 'aceptar') {
                document.getElementById('formAceptar').submit();
            } else if (accionPendiente === 'rechazar') {
                document.getElementById('formRechazar').submit();
            }
        });
    }

    var btnReprogramar = document.getElementById('btnReprogramar');
    var panelReprogramar = document.getElementById('panelReprogramar');

    if (btnReprogramar && panelReprogramar) {
        btnReprogramar.addEventListener('click', function () {
            panelReprogramar.classList.toggle('d-none');
        });
    }

    var selectDia = document.getElementById('nuevaFecha');
    var selectHora = document.getElementById('nuevaHora');

    if (selectDia && selectHora && window.DISPONIBILIDAD_REPROGRAMAR) {

        var disponibilidadReprogramar = window.DISPONIBILIDAD_REPROGRAMAR;
        var duracionSolicitud = window.DURACION_SOLICITUD;

        Object.keys(disponibilidadReprogramar).forEach(function (fecha) {
            var opcion = document.createElement('option');
            opcion.value = fecha;
            opcion.textContent = fecha;
            selectDia.appendChild(opcion);
        });

        /**
         * Suma una hora a una hora dada en formato "HH:MM".
         * @param {string} hora la hora de origen en formato "HH:MM"
         * @returns {string} la hora resultante una hora después, en formato "HH:MM"
         */
        function sumarUnaHora(hora) {
            var partes = hora.split(':');
            var horaSiguiente = parseInt(partes[0], 10) + 1;
            return String(horaSiguiente).padStart(2, '0') + ':' + partes[1];
        }

        selectDia.addEventListener('change', function () {
            selectHora.innerHTML = '';
            var opcionVacia = document.createElement('option');
            opcionVacia.value = '';
            opcionVacia.textContent = 'Seleccione hora';
            opcionVacia.disabled = true;
            opcionVacia.selected = true;
            selectHora.appendChild(opcionVacia);

            var horasDelDia = disponibilidadReprogramar[selectDia.value] || [];

            var horasValidas = horasDelDia.filter(function (hora) {
                if (duracionSolicitud !== 2) {
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
        });
    }
});