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
        'Esta acción es irreversible y perderás todos los datos del alumno.',
        'Eliminar',
        function () {
            document.getElementById('inputEliminarMatricula').value = matricula;
            document.getElementById('formEliminarAlumno').submit();
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

    let filas = document.querySelectorAll('#tablaAlumnos tr');
    let filasVisibles = 0;

    filas.forEach(function (fila) {
        if (fila.id === 'filaSinResultados') return;

        let nombre = fila.dataset.nombre || '';
        let carrera = fila.dataset.carrera || '';
        let cuatri = fila.dataset.cuatri || '';
        let grupo = fila.dataset.grupo || '';

        let coincideNombre = nombre.includes(textoBuscar);
        let coincideCarrera = carreraSeleccionada === '' || carrera === carreraSeleccionada;
        let coincideGrupo = grupoSeleccionado === '' || grupo === grupoSeleccionado;
        let coincideCuatri = cuatrimestreSeleccionado === '' || cuatri === cuatrimestreSeleccionado;

        let coincide = coincideNombre && coincideCarrera && coincideGrupo && coincideCuatri;
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

    if (buscarAlumno) buscarAlumno.addEventListener('input', filtrarAlumnos);
    if (carrera) carrera.addEventListener('change', filtrarAlumnos);
    if (grupo) grupo.addEventListener('change', filtrarAlumnos);
    if (cuatrimestre) cuatrimestre.addEventListener('change', filtrarAlumnos);
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
        }

        window.history.replaceState(null, null, window.location.pathname);
    }
});
