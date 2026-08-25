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

    if (inputFechaEspontanea) {
        var hoy = new Date();
        var yyyy = hoy.getFullYear();
        var mm = String(hoy.getMonth() + 1).padStart(2, '0');
        var dd = String(hoy.getDate()).padStart(2, '0');
        inputFechaEspontanea.setAttribute('max', yyyy + '-' + mm + '-' + dd);
    }

    function actualizarCamposPorAsistencia() {
        if (radioFalto.checked) {
            modalTemasTratados.required = false;
            modalAcuerdos.required = false;
            modalTemasTratados.value = TEXTO_FALTA_TEMAS;
            modalAcuerdos.value = TEXTO_FALTA_ACUERDOS;
            modalTemasTratados.readOnly = true;
            modalAcuerdos.readOnly = true;
            modalTemasTratados.classList.remove('is-invalid');
            modalAcuerdos.classList.remove('is-invalid');

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
        verificarFormularioModal();
    }

    document.querySelectorAll('input[name="estatusAsistencia"]').forEach(function (radio) {
        radio.addEventListener('change', actualizarCamposPorAsistencia);
    });

    var btnGuardarModal = formCompletarSesion.querySelector('button[type="submit"]');
    var inputsRequeridosModal = formCompletarSesion.querySelectorAll('input[required], textarea[required]');

    function verificarFormularioModal() {
        var esValido = true;
        inputsRequeridosModal.forEach(function (input) {
            if (input.type === 'radio') {
                return;
            }
            if (input.offsetParent === null) {
                return;
            }
            if (!input.checkValidity()) {
                esValido = false;
            }
        });

        var asistenciaMarcada = formCompletarSesion.querySelector('input[name="estatusAsistencia"]:checked') !== null;
        if (!asistenciaMarcada) {
            esValido = false;
        }

        if (btnGuardarModal) {
            btnGuardarModal.disabled = !esValido;
        }
    }

    inputsRequeridosModal.forEach(function (input) {
        if (input.type === 'radio') {
            return;
        }
        input.addEventListener('input', function () {
            if (this.checkValidity()) {
                this.classList.remove('is-invalid');
            } else {
                this.classList.add('is-invalid');
            }
            verificarFormularioModal();
        });

        input.addEventListener('blur', function () {
            if (!this.checkValidity()) {
                this.classList.add('is-invalid');
            }
            verificarFormularioModal();
        });
    });

    document.querySelectorAll('.btn-completar-sesion').forEach(function (btn) {
        btn.addEventListener('click', function () {
            formCompletarSesion.reset();
            modalTemasTratados.classList.remove('is-invalid');
            modalAcuerdos.classList.remove('is-invalid');
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
            inputsRequeridosModal.forEach(function (input) {
                if (input.type === 'radio') {
                    return;
                }
                if (!input.checkValidity()) {
                    input.classList.add('is-invalid');
                }
            });
            verificarFormularioModal();
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

    if (formTutoriaEspontanea) {
        var btnGuardarEspontanea = document.getElementById('btnGuardarEspontanea');
        var inputsRequeridosEspontanea = formTutoriaEspontanea.querySelectorAll('input[required], select[required], textarea[required]');

        var selectGrupo = document.getElementById('grupoSelector');
        var feedbackAlumno = document.getElementById('alumnoEstado');
        var inputTemasEspontanea = document.getElementById('temasTratados');
        var inputAcuerdosEspontanea = document.getElementById('acuerdos');

        function actualizarCamposPorGrupoAlumno() {
            var hayGrupoYAlumno = !!(selectGrupo && selectGrupo.value)
                && !!($alumnoBuscador && $alumnoBuscador.val());

            if (inputTemasEspontanea) {
                inputTemasEspontanea.disabled = !hayGrupoYAlumno;
            }
            if (inputAcuerdosEspontanea) {
                inputAcuerdosEspontanea.disabled = !hayGrupoYAlumno;
            }
        }
        var $alumnoBuscador = (typeof jQuery !== 'undefined' && jQuery.fn.select2)
            ? jQuery('#alumnoBuscador')
            : null;

        if ($alumnoBuscador) {
            $alumnoBuscador.select2({
                theme: 'bootstrap-5',
                width: '100%'
            });
        }

        actualizarCamposPorGrupoAlumno();

        function marcarAlumnoInvalido() {
            if (!$alumnoBuscador) {
                return;
            }
            $alumnoBuscador[0].classList.add('is-invalid');
            var feedbackInvalido = document.getElementById('alumnoBuscadorInvalido');
            if (feedbackInvalido) {
                feedbackInvalido.style.display = 'block';
            }
            $alumnoBuscador.next('.select2-container').find('.select2-selection')
                .css('border-color', 'var(--bs-form-invalid-border-color, #dc3545)');
        }

        function limpiarAlumnoInvalido() {
            if (!$alumnoBuscador) {
                return;
            }
            $alumnoBuscador[0].classList.remove('is-invalid');
            var feedbackInvalido = document.getElementById('alumnoBuscadorInvalido');
            if (feedbackInvalido) {
                feedbackInvalido.style.display = '';
            }
            $alumnoBuscador.next('.select2-container').find('.select2-selection').css('border-color', '');
        }

        function fijarOpcionesAlumno(opciones, deshabilitado) {
            if (!$alumnoBuscador) {
                return;
            }
            $alumnoBuscador.empty();
            opciones.forEach(function (op) {
                $alumnoBuscador.append(new Option(op.texto, op.valor, false, false));
            });
            $alumnoBuscador.prop('disabled', deshabilitado);
            $alumnoBuscador.trigger('change');
        }

        if (selectGrupo) {
            selectGrupo.addEventListener('change', function () {
                if (feedbackAlumno) {
                    feedbackAlumno.textContent = '';
                    feedbackAlumno.className = 'form-text';
                }

                var idGrupo = selectGrupo.value;
                if (!idGrupo) {
                    fijarOpcionesAlumno([{ texto: 'Selecciona un grupo primero', valor: '' }], true);
                    return;
                }

                fijarOpcionesAlumno([{ texto: 'Cargando alumnos...', valor: '' }], true);

                fetch(formTutoriaEspontanea.action + '?accion=obtenerAlumnosPorGrupo&idGrupo=' + encodeURIComponent(idGrupo))
                    .then(function (resp) { return resp.json(); })
                    .then(function (alumnos) {
                        if (selectGrupo.value !== idGrupo) return;

                        var opciones = [{ texto: 'Selecciona un alumno', valor: '' }];
                        alumnos.forEach(function (a) {
                            opciones.push({ texto: a.nombres + ' ' + a.apellidos, valor: a.matricula });
                        });
                        fijarOpcionesAlumno(opciones, false);
                    })
                    .catch(function () {
                        fijarOpcionesAlumno([{ texto: 'No se pudo cargar la lista de alumnos', valor: '' }], true);
                    });
            });
        }

        if ($alumnoBuscador) {
            $alumnoBuscador.on('change', function () {
                var matricula = $alumnoBuscador.val();
                if (feedbackAlumno) {
                    if (matricula) {
                        feedbackAlumno.textContent = 'Alumno seleccionado (matrícula ' + matricula + ').';
                        feedbackAlumno.className = 'form-text text-success';
                    } else {
                        feedbackAlumno.textContent = '';
                        feedbackAlumno.className = 'form-text';
                    }
                }

                if (matricula) {
                    limpiarAlumnoInvalido();
                }

                actualizarCamposPorGrupoAlumno();
                verificarFormularioEspontanea();
            });

            $alumnoBuscador.on('select2:close', function () {
                if (!$alumnoBuscador.val()) {
                    marcarAlumnoInvalido();
                }
                verificarFormularioEspontanea();
            });
        }

        function verificarFormularioEspontanea() {
            var esValido = true;
            inputsRequeridosEspontanea.forEach(function (input) {
                if (!input.checkValidity()) {
                    esValido = false;
                }
            });
            if (btnGuardarEspontanea) {
                btnGuardarEspontanea.disabled = !esValido;
            }
        }

        inputsRequeridosEspontanea.forEach(function (input) {
            if (input.id === 'alumnoBuscador') {
                return;
            }

            input.addEventListener('input', function () {
                if (this.checkValidity()) {
                    this.classList.remove('is-invalid');
                } else {
                    this.classList.add('is-invalid');
                }
                verificarFormularioEspontanea();
            });

            input.addEventListener('blur', function () {
                if (!this.checkValidity()) {
                    this.classList.add('is-invalid');
                }
                verificarFormularioEspontanea();
            });
        });

        verificarFormularioEspontanea();
    }

    formTutoriaEspontanea.addEventListener('submit', function (e) {
        e.preventDefault();

        if (!formTutoriaEspontanea.checkValidity()) {
            inputsRequeridosEspontanea.forEach(function (input) {
                if (!input.checkValidity()) {
                    input.classList.add('is-invalid');
                }
            });

            if ($alumnoBuscador && !$alumnoBuscador.val()) {
                marcarAlumnoInvalido();
            }

            verificarFormularioEspontanea();
            return;
        }

        if (inputFechaEspontanea && inputFechaEspontanea.value) {
            var fechaSeleccionada = new Date(inputFechaEspontanea.value + 'T00:00:00');
            var fechaHoy = new Date();
            fechaHoy.setHours(0, 0, 0, 0);
            if (fechaSeleccionada.getTime() > fechaHoy.getTime()) {
                mostrarAlerta('advertencia', 'Fecha inválida', 'No se pueden registrar tutorías con fecha futura.');
                return;
            }
        }

        if (inputHoraEspontanea && inputHoraEspontanea.value) {
            if (inputHoraEspontanea.value < HORA_MIN || inputHoraEspontanea.value > HORA_MAX) {
                inputHoraEspontanea.classList.add('is-invalid');
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
    }

    if (exito || errorUrl) {
        window.history.replaceState(null, null, window.location.pathname);
    }
});
