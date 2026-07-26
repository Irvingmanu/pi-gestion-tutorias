function confirmarCancelacion() {
    let boton = document.getElementById('btnCancelarFormulario');
    let urlDestino = boton ? boton.dataset.urlCancelar : '/';

    mostrarConfirmacion(
        'advertencia',
        '¿Descartar cambios?',
        'Si sales ahora, perderás todos los datos que has ingresado.',
        'Sí, salir',
        function () {
            window.location.href = urlDestino;
        }
    );
}

function prepararEliminacion(matricula) {
    mostrarConfirmacion(
        'critica',
        '¿Eliminar alumno?',
        'El alumno se dará de baja y no podrá acceder al sistema, pero se conservará su historial.',
        'Eliminar',
        function () {
            document.getElementById('inputEliminarMatricula').value = matricula;
            document.getElementById('formEliminarAlumno').submit();
        }
    );
}

function prepararReactivacion(matricula) {
    mostrarConfirmacion(
        'advertencia',
        '¿Reactivar alumno?',
        'El alumno volverá a aparecer en los listados y podrá acceder al sistema nuevamente.',
        'Reactivar',
        function () {
            document.getElementById('inputReactivarMatricula').value = matricula;
            document.getElementById('formReactivarAlumno').submit();
        }
    );
}

// Filtrado en tiempo real de la tabla de alumnos
function filtrarAlumnos() {
    let inputBuscar = document.getElementById('buscarAlumno');
    let tabla = document.getElementById('tablaAlumnos');
    if (!inputBuscar || !tabla) return;

    let textoBuscar = inputBuscar.value.trim().toLowerCase();
    let carreraSeleccionada = document.getElementById('carrera').value;
    let grupoSeleccionado = document.getElementById('grupo').value;
    let cuatrimestreSeleccionado = document.getElementById('cuatrimestre').value;
    let mostrarInactivos = document.getElementById('mostrarInactivos');
    let incluirInactivos = mostrarInactivos ? mostrarInactivos.checked : false;

    let filas = document.querySelectorAll('#tablaAlumnos tr');
    let filasVisibles = 0;

    filas.forEach(function (fila) {
        if (fila.id === 'filaSinResultados') return;

        let nombre = fila.dataset.nombre || '';
        let carrera = fila.dataset.carrera || '';
        let cuatri = fila.dataset.cuatri || '';
        let grupo = fila.dataset.grupo || '';
        let activo = fila.dataset.activo !== 'N';

        let coincideNombre = nombre.includes(textoBuscar);
        let coincideCarrera = carreraSeleccionada === '' || carrera === carreraSeleccionada;
        let coincideGrupo = grupoSeleccionado === '' || grupo === grupoSeleccionado;
        let coincideCuatri = cuatrimestreSeleccionado === '' || cuatri === cuatrimestreSeleccionado;
        let coincideActivo = activo || incluirInactivos;

        let coincide = coincideNombre && coincideCarrera && coincideGrupo && coincideCuatri && coincideActivo;
        fila.style.display = coincide ? '' : 'none';
        if (coincide) filasVisibles++;
    });

    let filaSinResultados = document.getElementById('filaSinResultados');
    if (filaSinResultados) {
        filaSinResultados.style.display = filasVisibles === 0 ? '' : 'none';
    }
}

document.addEventListener('DOMContentLoaded', function () {
    let buscarAlumno = document.getElementById('buscarAlumno');
    let carrera = document.getElementById('carrera');
    let grupo = document.getElementById('grupo');
    let cuatrimestre = document.getElementById('cuatrimestre');
    let mostrarInactivos = document.getElementById('mostrarInactivos');

    if (buscarAlumno) buscarAlumno.addEventListener('input', filtrarAlumnos);
    if (carrera) carrera.addEventListener('change', filtrarAlumnos);
    if (grupo) grupo.addEventListener('change', filtrarAlumnos);
    if (cuatrimestre) cuatrimestre.addEventListener('change', filtrarAlumnos);
    if (mostrarInactivos) mostrarInactivos.addEventListener('change', filtrarAlumnos);

    filtrarAlumnos();
});

// Toasts/alertas de exito y error via parametros en la URL (?exito=, ?error=)
document.addEventListener('DOMContentLoaded', function () {
    const parametros = new URLSearchParams(window.location.search);
    const exito = parametros.get('exito');

    if (exito) {
        switch (exito) {
            case 'guardado':
                mostrarToast('exito', '¡Éxito!', 'El alumno fue guardado correctamente');
                break;
            case 'editado':
                mostrarToast('exito', '¡Éxito!', 'El alumno fue editado correctamente');
                break;
            case 'eliminado':
                mostrarToast('exito', '¡Éxito!', 'El alumno fue eliminado correctamente');
                break;
            case 'reactivado':
                mostrarToast('exito', '¡Éxito!', 'El alumno fue reactivado correctamente');
                break;
        }

        window.history.replaceState(null, null, window.location.pathname);
    }

    const error = parametros.get('error');

    if (error) {
        switch (error) {
            case 'matricula_duplicada':
                mostrarAlerta('error', 'Error', 'Esta matrícula ya está registrada en el sistema.');
                break;
            case 'correo_duplicado':
                mostrarAlerta('error', 'Error', 'Este correo ya está registrado en el sistema.');
                break;
            case 'correo':
                mostrarAlerta('error', 'Error', 'El correo debe terminar en @utez.edu.mx.');
                break;
            case 'alumno_en_uso':
                mostrarAlerta('error', 'No se puede eliminar', 'Este alumno ya tiene asistencias u otros registros vinculados en el sistema.');
                break;
            case 'reactivacion_fallida':
                mostrarAlerta('error', 'Error', 'No se pudo reactivar al alumno.');
                break;
        }

        window.history.replaceState(null, null, window.location.pathname);
    }
});
