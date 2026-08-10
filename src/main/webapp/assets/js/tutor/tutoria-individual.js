document.addEventListener('DOMContentLoaded', function () {
    var modalEl = document.getElementById('modalCompletarSesion');
    var modal = new bootstrap.Modal(modalEl);
    var formCompletarSesion = document.getElementById('formCompletarSesion');
    var modalTemasTratados = document.getElementById('modalTemasTratados');
    var modalAcuerdos = document.getElementById('modalAcuerdos');
    var radioFalto = document.getElementById('modalAsistenciaFalta');
    var selectsMotivo = formCompletarSesion.querySelectorAll('select[name="idMotivo"]');
    var TEXTO_FALTA_TEMAS = 'El alumno no se presentó a la sesión.';
    var TEXTO_FALTA_ACUERDOS = 'N/A - El alumno no se presentó a la sesión.';

    var formTutoriaEspontanea = document.getElementById('formTutoriaEspontanea');
    var inputFechaEspontanea = document.getElementById('fecha');
    var inputHoraEspontanea = document.getElementById('hora');
    var HORA_MIN = '07:00';
    var HORA_MAX = '21:00';

    // Restringe el datepicker para que no se puedan elegir fechas futuras
    if (inputFechaEspontanea) {
        var hoy = new Date();
        var yyyy = hoy.getFullYear();
        var mm = String(hoy.getMonth() + 1).padStart(2, '0');
        var dd = String(hoy.getDate()).padStart(2, '0');
        inputFechaEspontanea.setAttribute('max', yyyy + '-' + mm + '-' + dd);
    }

    // Si el alumno falto no tiene caso obligar al tutor a redactar temas/acuerdos,
    // ni permitirle registrar una canalizacion basada en una sesion que no ocurrio.
    // Textareas van con readonly (no disabled) para que su valor SI viaje en el POST;
    // los selects de "Vinculo Directo" si van con disabled porque son opcionales
    // (el backend ya trata idMotivo vacio/ausente como "Ninguno").
    function actualizarCamposPorAsistencia() {
        if (radioFalto.checked) {
            modalTemasTratados.required = false;
            modalAcuerdos.required = false;
            modalTemasTratados.value = TEXTO_FALTA_TEMAS;
            modalAcuerdos.value = TEXTO_FALTA_ACUERDOS;
            modalTemasTratados.readOnly = true;
            modalAcuerdos.readOnly = true;

            selectsMotivo.forEach(function (select) {
                select.value = '';
                select.disabled = true;
            });
        } else {
            modalTemasTratados.required = true;
            modalAcuerdos.required = true;
            modalTemasTratados.readOnly = false;
            modalAcuerdos.readOnly = false;
            if (modalTemasTratados.value === TEXTO_FALTA_TEMAS) {
                modalTemasTratados.value = '';
            }
            if (modalAcuerdos.value === TEXTO_FALTA_ACUERDOS) {
                modalAcuerdos.value = '';
            }

            selectsMotivo.forEach(function (select) {
                select.disabled = false;
            });
        }
    }

    document.querySelectorAll('input[name="estatusAsistencia"]').forEach(function (radio) {
        radio.addEventListener('change', actualizarCamposPorAsistencia);
    });

    document.querySelectorAll('.btn-completar-sesion').forEach(function (btn) {
        btn.addEventListener('click', function () {
            formCompletarSesion.reset();
            actualizarCamposPorAsistencia();
            document.getElementById('modalIdSesion').value = btn.dataset.idSesion;
            document.getElementById('modalAlumnoInfo').textContent =
                btn.dataset.alumno + ' (' + btn.dataset.matricula + ') - ' + btn.dataset.fecha + ' ' + btn.dataset.hora;
            modal.show();
        });
    });

    function confirmarCierreModalCompletar() {
        mostrarConfirmacion(
            'critica',
            '¿Estás seguro de salir?',
            'Se perderán los datos que no hayas guardado.',
            'Sí, salir',
            function () {
                bootstrap.Modal.getInstance(modalEl).hide();
            }
        );
    }

    document.getElementById('btnCerrarModalCompletar').addEventListener('click', confirmarCierreModalCompletar);
    document.getElementById('btnCancelarModalCompletar').addEventListener('click', confirmarCierreModalCompletar);

    formCompletarSesion.addEventListener('submit', function (e) {
        e.preventDefault();

        if (!formCompletarSesion.checkValidity()) {
            formCompletarSesion.reportValidity();
            return;
        }

        mostrarConfirmacion(
            'advertencia',
            '¿Completar sesión?',
            'Estás a punto de registrar los temas, acuerdos y canalizaciones.',
            'Sí, guardar',
            function () {
                formCompletarSesion.submit();
            }
        );
    });

    formTutoriaEspontanea.addEventListener('submit', function (e) {
        e.preventDefault();

        if (!formTutoriaEspontanea.checkValidity()) {
            formTutoriaEspontanea.reportValidity();
            return;
        }

        // Validación de fecha: no se permiten fechas futuras
        if (inputFechaEspontanea && inputFechaEspontanea.value) {
            var fechaSeleccionada = new Date(inputFechaEspontanea.value + 'T00:00:00');
            var fechaHoy = new Date();
            fechaHoy.setHours(0, 0, 0, 0);
            if (fechaSeleccionada.getTime() > fechaHoy.getTime()) {
                mostrarAlerta('advertencia', 'Fecha inválida', 'No se pueden registrar tutorías con fecha futura.');
                return;
            }
        }

        // Validación de horario permitido (7:00 AM - 9:00 PM)
        if (inputHoraEspontanea && inputHoraEspontanea.value) {
            if (inputHoraEspontanea.value < HORA_MIN || inputHoraEspontanea.value > HORA_MAX) {
                mostrarAlerta('advertencia', 'Horario no permitido', 'Las tutorías solo pueden agendarse entre las 7:00 AM y las 9:00 PM.');
                return;
            }
        }

        mostrarConfirmacion(
            'advertencia',
            '¿Registrar tutoría?',
            'Estás a punto de registrar la tutoría espontánea con los datos ingresados.',
            'Sí, guardar',
            function () {
                formTutoriaEspontanea.submit();
            }
        );
    });

    var parametros = new URLSearchParams(window.location.search);
    var exito = parametros.get('exito');
    var errorUrl = parametros.get('error');

    if (exito === 'completada') {
        mostrarToast('exito', '¡Éxito!', 'La sesión fue completada correctamente');
    } else if (exito === 'tutoria_guardada') {
        mostrarToast('exito', 'Guardado', 'Tutoría espontánea registrada correctamente.');
    } else if (errorUrl === 'matricula_invalida') {
        mostrarAlerta('advertencia', 'Formato incorrecto', 'La matrícula debe tener exactamente 10 caracteres.');
    } else if (errorUrl === 'matricula_no_existe') {
        mostrarAlerta('error', 'Matrícula no encontrada', 'El alumno no está registrado en el sistema. Verifica el dato.');
    } else if (exito === 'tutoria_guardada') {
    mostrarToast('exito', 'Guardado', 'Tutoría espontánea registrada correctamente. Puedes consultarla en la pestaña "Historial".');
}

    if (exito || errorUrl) {
        window.history.replaceState(null, null, window.location.pathname);
    }
});