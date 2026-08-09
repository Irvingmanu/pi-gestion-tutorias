// Archivo: webapp/assets/js/tutor/registro-grupal.js

document.addEventListener('DOMContentLoaded', function () {
    var selectGrupo = document.getElementById('grupoAsignado');
    var contenedorAsistencia = document.getElementById('contenedorAsistencia');
    var cuerpoTablaAsistencia = document.getElementById('cuerpoTablaAsistencia');

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

    // Verificamos que el form exista (en caso de que la vista entre al <c:when test="${empty asignaciones}">)
    if(formRegistroGrupal) {
        formRegistroGrupal.addEventListener('submit', function (e) {
            e.preventDefault();

            if (!formRegistroGrupal.checkValidity()) {
                formRegistroGrupal.reportValidity();
                return;
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
    }

    if (exito || errorUrl) {
        window.history.replaceState(null, null, window.location.pathname);
    }
});