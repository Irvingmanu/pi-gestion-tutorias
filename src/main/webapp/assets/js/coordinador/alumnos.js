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

// ==================== AGRUPACION DE LA TABLA POR CARRERA + CUATRIMESTRE + GRUPO ====================
// El JSP renderiza UNA sola tabla oculta (#tablaOriginalAlumnos) con todos los alumnos.
// Aqui la leemos una sola vez, y cada vez que cambia un filtro agrupamos las filas que
// coinciden y pintamos una tabla independiente (con scroll) por cada combinacion de
// Carrera + Cuatrimestre + Grupo, sin volver a pedir nada al servidor.

let filasAlumnosOriginales = [];

// Toma las filas de la tabla oculta del JSP y pinta el primer grupo, respetando
// el filtro de "Mostrar alumnos dados de baja" (desmarcado por defecto).
function inicializarAgrupacionAlumnos() {
    let tbodyOriginal = document.getElementById('tablaAlumnosOriginal');
    let contenedorGrupos = document.getElementById('contenedorGruposAlumnos');
    if (!contenedorGrupos) return;

    if (!tbodyOriginal) {
        // El JSP no encontro alumnos en BD (listaAlumnos vacia)
        contenedorGrupos.innerHTML = '<div class="alert alert-info text-center">No hay alumnos registrados todavía.</div>';
        return;
    }

    filasAlumnosOriginales = Array.from(tbodyOriginal.querySelectorAll('tr'));
    filtrarAlumnos();
}

// Filtrado en tiempo real: ningun filtro es obligatorio
function filtrarAlumnos() {
    let inputBuscar = document.getElementById('buscarAlumno');
    if (!inputBuscar) return;

    let textoBuscar = inputBuscar.value.trim().toLowerCase();
    let carreraSeleccionada = document.getElementById('carrera').value;
    let grupoSeleccionado = document.getElementById('grupo').value;
    let cuatrimestreSeleccionado = document.getElementById('cuatrimestre').value;
    let mostrarInactivos = document.getElementById('mostrarInactivos');
    let incluirInactivos = mostrarInactivos ? mostrarInactivos.checked : false;

    let filasFiltradas = filasAlumnosOriginales.filter(function (fila) {
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

        return coincideNombre && coincideCarrera && coincideGrupo && coincideCuatri && coincideActivo;
    });

    renderizarGruposAlumnos(filasFiltradas);
}

// Agrupa las filas por Carrera + Cuatrimestre + Grupo y pinta una tabla por cada grupo
function renderizarGruposAlumnos(filas) {
    let contenedor = document.getElementById('contenedorGruposAlumnos');
    if (!contenedor) return;

    contenedor.innerHTML = '';

    if (filas.length === 0) {
        contenedor.innerHTML = '<div class="alert alert-info text-center">No se encontraron alumnos con los filtros seleccionados.</div>';
        return;
    }

    let grupos = new Map();
    filas.forEach(function (fila) {
        let clave = fila.dataset.carrera + '|' + fila.dataset.cuatri + '|' + fila.dataset.grupo;
        if (!grupos.has(clave)) {
            grupos.set(clave, {
                carrera: fila.dataset.carrera,
                cuatri: fila.dataset.cuatri,
                grupo: fila.dataset.grupo,
                filas: []
            });
        }
        grupos.get(clave).filas.push(fila);
    });

    // Orden: Carrera (alfabetico), Cuatrimestre (numerico), Grupo (alfabetico)
    let gruposOrdenados = Array.from(grupos.values()).sort(function (a, b) {
        if (a.carrera !== b.carrera) return a.carrera.localeCompare(b.carrera);
        if (a.cuatri !== b.cuatri) return Number(a.cuatri) - Number(b.cuatri);
        return a.grupo.localeCompare(b.grupo);
    });

    gruposOrdenados.forEach(function (grupoInfo) {
        contenedor.appendChild(construirTablaGrupo(grupoInfo));
    });
}

// Construye el bloque visual de un solo grupo: titulo (texto negro, sin fondo) + tabla con scroll
function construirTablaGrupo(grupoInfo) {
    let bloque = document.createElement('div');
    bloque.className = 'mb-4';

    let titulo = document.createElement('div');
    titulo.className = 'titulo-grupo-tabla h6 mb-2';
    titulo.textContent = grupoInfo.carrera + ' - ' + grupoInfo.cuatri + '° ' + grupoInfo.grupo;

    let claveGrupo = grupoInfo.carrera + '|' + grupoInfo.cuatri + '|' + grupoInfo.grupo;
    let nombreTutor = (window.tutoresPorGrupo || {})[claveGrupo];

    let tutorSpan = document.createElement('span');
    tutorSpan.className = 'tutor-grupo-tabla';
    tutorSpan.textContent = nombreTutor ? ' — Tutor: ' + nombreTutor : ' — Sin tutor asignado';
    titulo.appendChild(tutorSpan);

    bloque.appendChild(titulo);

    let scrollWrap = document.createElement('div');
    scrollWrap.className = 'table-responsive';
    scrollWrap.style.maxHeight = '320px';
    scrollWrap.style.overflowY = 'auto';

    let tabla = document.createElement('table');
    tabla.className = 'tabla-grupos fs-6';
    tabla.innerHTML =
        '<thead>' +
        '<tr>' +
        '<th>Matricula</th>' +
        '<th>Nombre Completo</th>' +
        '<th>Correo</th>' +
        '<th>Genero</th>' +
        '<th>Carrera</th>' +
        '<th>Cuatri/Grupo</th>' +
        '<th>Acciones</th>' +
        '</tr>' +
        '</thead>';

    let tbody = document.createElement('tbody');
    grupoInfo.filas.forEach(function (fila) {
        tbody.appendChild(fila.cloneNode(true));
    });
    tabla.appendChild(tbody);

    scrollWrap.appendChild(tabla);
    bloque.appendChild(scrollWrap);
    return bloque;
}

document.addEventListener('DOMContentLoaded', function () {
    let buscarAlumno = document.getElementById('buscarAlumno');
    let carrera = document.getElementById('carrera');
    let grupo = document.getElementById('grupo');
    let cuatrimestre = document.getElementById('cuatrimestre');
    let mostrarInactivos = document.getElementById('mostrarInactivos');

    inicializarAgrupacionAlumnos();

    if (buscarAlumno) buscarAlumno.addEventListener('input', filtrarAlumnos);
    if (carrera) carrera.addEventListener('change', filtrarAlumnos);
    if (grupo) grupo.addEventListener('change', filtrarAlumnos);
    if (cuatrimestre) cuatrimestre.addEventListener('change', filtrarAlumnos);
    if (mostrarInactivos) mostrarInactivos.addEventListener('change', filtrarAlumnos);
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