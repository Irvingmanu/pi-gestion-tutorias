// Archivo: webapp/assets/js/tutor/registro-grupal.js

document.addEventListener('DOMContentLoaded', function () {
    var selectGrupo = document.getElementById('grupoAsignado');
    var contenedorAsistencia = document.getElementById('contenedorAsistencia');
    var cuerpoTablaAsistencia = document.getElementById('cuerpoTablaAsistencia');
    var inputFecha = document.getElementById('fecha');
    var inputHora = document.getElementById('hora');
    var HORA_MIN = '07:00';
    var HORA_MAX = '21:00';

    // La fecha nunca puede ser futura: bloqueamos el datepicker desde HOY
    if (inputFecha) {
        var hoy = new Date();
        var yyyy = hoy.getFullYear();
        var mm = String(hoy.getMonth() + 1).padStart(2, '0');
        var dd = String(hoy.getDate()).padStart(2, '0');
        var hoyStr = yyyy + '-' + mm + '-' + dd;

        inputFecha.setAttribute('max', hoyStr);
    }

    function mostrarFilaMensaje(texto, claseTexto) {
        cuerpoTablaAsistencia.innerHTML = '';
        var fila = document.createElement('tr');
        var celda = document.createElement('td');
        celda.colSpan = 3;
        celda.className = 'text-center py-3 ' + claseTexto;
        celda.textContent = texto;
        fila.appendChild(celda);
        cuerpoTablaAsistencia.appendChild(fila);
    }

    function pintarAlumnos(alumnos) {
        cuerpoTablaAsistencia.innerHTML = '';

        if (!alumnos.length) {
            mostrarFilaMensaje('No hay alumnos registrados en este grupo.', 'text-muted');
            return;
        }

        alumnos.forEach(function (alumno) {
            var fila = document.createElement('tr');

            var tdMatricula = document.createElement('td');
            tdMatricula.textContent = alumno.matricula;

            var tdNombre = document.createElement('td');
            tdNombre.textContent = alumno.nombres + ' ' + alumno.apellidos;

            var tdAsistencia = document.createElement('td');
            tdAsistencia.className = 'text-center';
            var checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.name = 'asistentes';
            checkbox.value = alumno.matricula;
            tdAsistencia.appendChild(checkbox);

            fila.appendChild(tdMatricula);
            fila.appendChild(tdNombre);
            fila.appendChild(tdAsistencia);
            cuerpoTablaAsistencia.appendChild(fila);
        });
    }

    function cargarAsistencia() {
        if (!selectGrupo) {
            return;
        }

        var valor = selectGrupo.value;

        if (!valor) {
            contenedorAsistencia.style.display = 'none';
            cuerpoTablaAsistencia.innerHTML = '';
            return;
        }

        var partes = valor.split('|');
        var idCarrera = partes[0];
        var idCuatrimestre = partes[1];
        var idLetra = partes[2];

        contenedorAsistencia.style.display = 'block';
        mostrarFilaMensaje('Cargando alumnos...', 'text-muted');

        // Utilizamos APP_CONTEXT definido en el JSP en lugar del EL tag
        var url = APP_CONTEXT + '/tutoria-grupal?accion=obtenerAlumnos'
            + '&idCarrera=' + encodeURIComponent(idCarrera)
            + '&idCuatrimestre=' + encodeURIComponent(idCuatrimestre)
            + '&idLetra=' + encodeURIComponent(idLetra);

        fetch(url)
            .then(function (resp) { return resp.json(); })
            .then(pintarAlumnos)
            .catch(function () {
                mostrarFilaMensaje('Ocurrió un error al cargar la lista de alumnos.', 'text-danger');
            });
    }

    if (selectGrupo) {
        selectGrupo.addEventListener('change', cargarAsistencia);

        if (selectGrupo.value) {
            cargarAsistencia();
        }
    }

    var formRegistroGrupal = document.getElementById('formRegistroGrupal');

    // ==========================================================================
    // VALIDACIÓN EN VIVO (mismo patrón que assets/js/coordinador/tutor.js):
    // marca is-invalid en cada campo requerido y solo habilita "Guardar" cuando
    // todo el formulario es válido.
    // ==========================================================================
    var inputsRequeridos;
    var btnGuardarGrupal;
    var verificarFormularioGrupal = function () {};

    if (formRegistroGrupal) {
        btnGuardarGrupal = document.getElementById('btnGuardarGrupal');
        inputsRequeridos = formRegistroGrupal.querySelectorAll('input[required], select[required], textarea[required]');

        verificarFormularioGrupal = function () {
            var esValido = true;
            inputsRequeridos.forEach(function (input) {
                if (!input.checkValidity()) {
                    esValido = false;
                }
            });
            if (btnGuardarGrupal) {
                btnGuardarGrupal.disabled = !esValido;
            }
        };

        inputsRequeridos.forEach(function (input) {
            input.addEventListener('input', function () {
                if (this.checkValidity()) {
                    this.classList.remove('is-invalid');
                } else {
                    this.classList.add('is-invalid');
                }
                verificarFormularioGrupal();
            });

            input.addEventListener('change', function () {
                if (this.checkValidity()) {
                    this.classList.remove('is-invalid');
                } else {
                    this.classList.add('is-invalid');
                }
                verificarFormularioGrupal();
            });

            input.addEventListener('blur', function () {
                if (!this.checkValidity()) {
                    this.classList.add('is-invalid');
                }
                verificarFormularioGrupal();
            });
        });

        verificarFormularioGrupal();
    }

    // Verificamos que el form exista (en caso de que la vista entre al <c:when test="${empty asignaciones}">)
    if (formRegistroGrupal) {
        formRegistroGrupal.addEventListener('submit', function (e) {
            e.preventDefault();

            if (!formRegistroGrupal.checkValidity()) {
                // Pintamos is-invalid en todo lo que falte antes de bloquear el envío
                inputsRequeridos.forEach(function (input) {
                    if (!input.checkValidity()) {
                        input.classList.add('is-invalid');
                    }
                });
                verificarFormularioGrupal();
                return;
            }

            // Validación de fecha: no se permiten fechas futuras
            if (inputFecha && inputFecha.value) {
                var fechaSeleccionada = new Date(inputFecha.value + 'T00:00:00');
                var fechaHoy = new Date();
                fechaHoy.setHours(0, 0, 0, 0);

                if (fechaSeleccionada.getTime() > fechaHoy.getTime()) {
                    mostrarAlerta('advertencia', 'Fecha inválida', 'No se pueden registrar tutorías con fecha futura.');
                    return;
                }
            }

            // Validación de horario permitido (7:00 am - 9:00 pm)
            if (inputHora && inputHora.value) {
                if (inputHora.value < HORA_MIN || inputHora.value > HORA_MAX) {
                    inputHora.classList.add('is-invalid');
                    mostrarAlerta('advertencia', 'Horario no permitido', 'Las tutorías solo pueden agendarse entre las 7:00 am y las 9:00 pm.');
                    return;
                }
            }

            mostrarConfirmacion(
                'advertencia',
                '¿Registrar tutoría grupal?',
                'Estás a punto de registrar la sesión y la asistencia del grupo.',
                'Sí, guardar',
                function () {
                    formRegistroGrupal.submit();
                }
            );
        });
    }

    var parametros = new URLSearchParams(window.location.search);
    var exito = parametros.get('exito');
    var errorUrl = parametros.get('error');

    if (exito === 'grupal_guardada') {
        mostrarToast('exito', 'Guardado', 'La tutoría grupal fue registrada correctamente.');
    } else if (errorUrl === 'campos_incompletos') {
        mostrarAlerta('advertencia', 'Faltan datos', 'Completa todos los campos obligatorios.');
    } else if (errorUrl === 'datos_invalidos') {
        mostrarAlerta('advertencia', 'Datos inválidos', 'Revisa el grupo y la fecha capturados.');
    } else if (errorUrl === 'grupo_no_asignado') {
        mostrarAlerta('error', 'Grupo no válido', 'Ese grupo no está asignado a tu cuenta de tutor.');
    } else if (errorUrl === 'tutor_no_encontrado') {
        mostrarAlerta('error', 'Error', 'No se encontró el perfil de tutor asociado a tu cuenta.');
    } else if (errorUrl === 'guardado_fallido') {
        mostrarAlerta('error', 'Error', 'Ocurrió un error al guardar el registro. Intenta de nuevo.');
    } else if (errorUrl === 'fecha_futura') {
        mostrarAlerta('advertencia', 'Fecha inválida', 'No se pueden registrar tutorías con fecha futura.');
    } else if (errorUrl === 'horario_no_permitido') {
        mostrarAlerta('advertencia', 'Horario no permitido', 'Las tutorías solo pueden agendarse entre las 7:00 am y las 9:00 pm.');
    }

    if (exito || errorUrl) {
        window.history.replaceState(null, null, window.location.pathname);
    }
});
