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

    // ==========================================================================
    // VALIDACIÓN EN VIVO DEL MODAL "COMPLETAR SESIÓN" (mismo patrón que
    // registro-grupal.js / coordinador/tutor.js): marca is-invalid en cada
    // campo requerido del modal en tiempo real.
    // ==========================================================================
    var btnGuardarModal = formCompletarSesion.querySelector('button[type="submit"]');
    var inputsRequeridosModal = formCompletarSesion.querySelectorAll('input[required], textarea[required]');

    function verificarFormularioModal() {
        var esValido = true;
        inputsRequeridosModal.forEach(function (input) {
            // Los radios de asistencia se validan como grupo, no uno por uno
            if (input.type === 'radio') {
                return;
            }
            if (input.offsetParent === null) {
                // campo oculto/disabled por "Faltó": no bloquea el formulario
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

    // ==========================================================================
    // VALIDACIÓN EN VIVO DE "TUTORÍA ESPONTÁNEA" (mismo patrón que
    // assets/js/tutor/registro-grupal.js y assets/js/coordinador/tutor.js):
    // marca is-invalid en cada campo requerido y solo habilita "Guardar" cuando
    // todo el formulario es válido.
    // ==========================================================================
    if (formTutoriaEspontanea) {
        var btnGuardarEspontanea = document.getElementById('btnGuardarEspontanea');
        var inputsRequeridosEspontanea = formTutoriaEspontanea.querySelectorAll('input[required], select[required], textarea[required]');

        // ==========================================================================
        // SELECCIÓN EN CASCADA GRUPO -> ALUMNO, ahora con Select2 (jQuery) en vez del
        // datalist/autocomplete propio de antes: Select2 resuelve el buscador y su UI.
        // El <select id="alumnoBuscador" name="matricula" required> manda directo la
        // matrícula en el POST, ya no hace falta un <input type="hidden"> aparte.
        // ==========================================================================
        var selectGrupo = document.getElementById('grupoSelector');
        var feedbackAlumno = document.getElementById('alumnoEstado');
        // Guardado defensivo: si el CDN de jQuery/Select2 no cargó (ej. red bloqueada),
        // el resto del formulario (fecha, hora, temas, etc.) debe seguir funcionando.
        var $alumnoBuscador = (typeof jQuery !== 'undefined' && jQuery.fn.select2)
            ? jQuery('#alumnoBuscador')
            : null;

        if ($alumnoBuscador) {
            $alumnoBuscador.select2({
                theme: 'bootstrap-5',
                width: '100%'
            });
        }

        // Parche Bootstrap <-> Select2: el <select> real queda oculto (display:none)
        // tras el widget de Select2, así que Bootstrap no puede pintarle su borde rojo
        // ni activar ".is-invalid ~ .invalid-feedback" con solo ver el <select>. Estas
        // dos funciones se llaman a mano desde donde antes se marcaba is-invalid en los
        // demás campos: al perder foco/cerrarse sin elegir (select2:close, equivalente
        // al "blur" de un input normal) y al intentar enviar el formulario.
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

        // Vacía el <select> e inyecta una opción por alumno (o un placeholder si aún
        // no hay grupo/está cargando), y refresca la UI de Select2 con trigger('change').
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
                        // El tutor pudo haber cambiado de grupo mientras la peticion viajaba
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

                // Apenas el tutor elige un alumno válido, se limpia cualquier estado de
                // error que haya quedado forzado (por submit o por select2:close abajo).
                if (matricula) {
                    limpiarAlumnoInvalido();
                }

                verificarFormularioEspontanea();
            });

            // select2:close es el equivalente al "blur" de un input normal: se dispara
            // cuando el tutor cierra el desplegable (clic afuera, Escape, Tab) sin llegar
            // a elegir nada. Si en ese momento el <select> sigue sin valor, se marca
            // inválido igual que el resto de los campos requeridos al perder el foco.
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
            // #alumnoBuscador (Select2) ya notifica cambios via su propio listener
            // 'change' de arriba; el <select> real queda oculto tras el widget de
            // Select2, así que marcarle is-invalid aquí no tendría efecto visible.
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

        // #alumnoBuscador ahora es un <select required name="matricula"> real, así que
        // checkValidity() nativo ya refleja correctamente si se eligió un alumno.
        if (!formTutoriaEspontanea.checkValidity()) {
            inputsRequeridosEspontanea.forEach(function (input) {
                if (!input.checkValidity()) {
                    input.classList.add('is-invalid');
                }
            });

            // Parche Bootstrap <-> Select2: el loop de arriba salta #alumnoBuscador a
            // propósito (queda oculto tras el widget de Select2, marcarle is-invalid no
            // se ve por sí solo). marcarAlumnoInvalido() hace a mano lo mismo que ya
            // pasa con select2:close cuando el tutor sale del campo sin elegir nada.
            if ($alumnoBuscador && !$alumnoBuscador.val()) {
                marcarAlumnoInvalido();
            }

            verificarFormularioEspontanea();
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