document.addEventListener('DOMContentLoaded', function () {

    // ==========================================
    // 1. LÓGICA DE CONFIRMACIÓN (ACEPTAR/NEGAR)
    // ==========================================
    var modalEl = document.getElementById('modalConfirmacion');
    if (modalEl) {
        var modalConfirmacion = new bootstrap.Modal(modalEl);

        var tituloEl = document.getElementById('confirmacionTitulo');
        var mensajeEl = document.getElementById('confirmacionMensaje');
        var iconoEl = document.getElementById('confirmacionIcono');
        var circuloEl = document.getElementById('confirmacionIconoCirculo');
        var btnAceptarModal = document.getElementById('btnConfirmacionAceptar');

        var accionPendiente = null;

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

    // Mostrar/Ocultar panel de reprogramar
    var btnReprogramar = document.getElementById('btnReprogramar');
    var panelReprogramar = document.getElementById('panelReprogramar');

    if (btnReprogramar && panelReprogramar) {
        btnReprogramar.addEventListener('click', function () {
            panelReprogramar.classList.toggle('d-none');
        });
    }

    // ==========================================
    // 2. LÓGICA DE REPROGRAMACIÓN (FECHAS/HORAS)
    // ==========================================
    var selectDia = document.getElementById('nuevaFecha');
    var selectHora = document.getElementById('nuevaHora');

    // Validamos que los selects existan y que las variables globales hayan sido declaradas
    if (selectDia && selectHora && window.DISPONIBILIDAD_REPROGRAMAR) {

        // Consumimos las variables globales que inyectamos desde el JSP
        var disponibilidadReprogramar = window.DISPONIBILIDAD_REPROGRAMAR;
        var duracionSolicitud = window.DURACION_SOLICITUD;

        Object.keys(disponibilidadReprogramar).forEach(function (fecha) {
            var opcion = document.createElement('option');
            opcion.value = fecha;
            opcion.textContent = fecha;
            selectDia.appendChild(opcion);
        });

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