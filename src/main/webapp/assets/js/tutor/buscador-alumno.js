// Buscador de alumnos de tutor/reportes.jsp: solo encuentra alumnos que pertenecen a los
// grupos de ESTE tutor (el servidor filtra por el tutor de la sesion, ver
// ReportesServlet.responderBuscarAlumnosTutor). A diferencia de un modal aparte, al
// seleccionar un resultado el mismo dashboard (tarjetas KPI + graficas de pastel/barras)
// se recalcula acotado a ese alumno, reutilizando buscarReporteTutor() ya definido en la vista.
// Requiere que la vista defina antes: const CONTEXT_PATH = "<context-path>";

let temporizadorBusquedaAlumno = null;

function escaparHtmlBuscador(texto) {
    const div = document.createElement('div');
    div.textContent = texto === undefined || texto === null ? '' : String(texto);
    return div.innerHTML;
}

function inicializarBuscadorAlumno() {
    const input = document.getElementById('buscadorAlumno');
    const resultados = document.getElementById('resultadosBuscadorAlumno');
    if (!input || !resultados) return;

    input.addEventListener('input', function () {
        const texto = input.value.trim();
        clearTimeout(temporizadorBusquedaAlumno);

        if (texto.length < 2) {
            resultados.classList.add('d-none');
            resultados.innerHTML = '';
            return;
        }

        temporizadorBusquedaAlumno = setTimeout(function () {
            fetch(CONTEXT_PATH + '/ReportesServlet?accion=buscarAlumnos&texto=' + encodeURIComponent(texto))
                .then(function (resp) { return resp.json(); })
                .then(function (data) { pintarResultadosBuscadorAlumno(data || []); })
                .catch(function (err) { console.error('Error al buscar alumnos:', err); });
        }, 300);
    });

    document.addEventListener('click', function (e) {
        if (!resultados.contains(e.target) && e.target !== input) {
            resultados.classList.add('d-none');
        }
    });

    const btnQuitar = document.getElementById('btnQuitarFiltroAlumno');
    if (btnQuitar) btnQuitar.addEventListener('click', quitarFiltroAlumno);
}

function pintarResultadosBuscadorAlumno(lista) {
    const resultados = document.getElementById('resultadosBuscadorAlumno');

    if (!lista.length) {
        resultados.innerHTML = '<div class="list-group-item text-muted small">Sin coincidencias entre tus alumnos asignados.</div>';
        resultados.classList.remove('d-none');
        return;
    }

    resultados.innerHTML = lista.map(function (a, indice) {
        return '<button type="button" class="list-group-item list-group-item-action" onclick="seleccionarAlumnoBuscador(' + indice + ')">' +
            '<div class="fw-semibold">' + escaparHtmlBuscador(a.nombreCompleto) + '</div>' +
            '<div class="text-muted small">' + escaparHtmlBuscador(a.matricula) + ' — ' + escaparHtmlBuscador(a.grupoAsignado) + '</div>' +
            '</button>';
    }).join('');
    resultados.classList.remove('d-none');
    resultados.dataset.items = JSON.stringify(lista);
}

// Al seleccionar un alumno no se abre ningun modal: se fija su matricula como filtro
// (igual que Carrera/Cuatrimestre/Grupo) y se vuelve a pedir el mismo reporte de siempre,
// que ahora la BD acota a ese alumno (ver ReportesDao.generarReporte con matricula).
function seleccionarAlumnoBuscador(indice) {
    const resultados = document.getElementById('resultadosBuscadorAlumno');
    const lista = JSON.parse(resultados.dataset.items || '[]');
    const alumno = lista[indice];
    if (!alumno) return;

    resultados.classList.add('d-none');
    document.getElementById('buscadorAlumno').value = '';

    document.getElementById('filtroMatricula').value = alumno.matricula;
    document.getElementById('nombreAlumnoFiltro').textContent = alumno.nombreCompleto;
    document.getElementById('matriculaAlumnoFiltro').textContent = alumno.matricula;
    document.getElementById('filtroAlumnoActivo').classList.remove('d-none');

    if (typeof buscarReporteTutor === 'function') buscarReporteTutor();
}

function quitarFiltroAlumno() {
    document.getElementById('filtroMatricula').value = '';
    document.getElementById('filtroAlumnoActivo').classList.add('d-none');
    if (typeof buscarReporteTutor === 'function') buscarReporteTutor();
}

document.addEventListener('DOMContentLoaded', inicializarBuscadorAlumno);
