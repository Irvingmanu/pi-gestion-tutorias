// Buscador de alumnos de coordinador/reportes-globales.jsp: a diferencia del buscador del
// tutor (ver assets/js/tutor/buscador-alumno.js), aqui se puede encontrar a CUALQUIER alumno
// del sistema (el coordinador no esta atado a un tutor). Al seleccionar un resultado no se
// abre ningun modal: se fija su matricula como filtro (igual que Carrera/Cuatrimestre/Grupo/
// Tutor) y se recalculan las mismas tarjetas KPI y graficas del dashboard, acotadas a ese alumno.
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
            fetch(CONTEXT_PATH + '/reportes-globales?accion=buscarAlumnos&texto=' + encodeURIComponent(texto))
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
        resultados.innerHTML = '<div class="list-group-item text-muted small">Sin coincidencias.</div>';
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

    if (typeof buscarReporteCoordinador === 'function') buscarReporteCoordinador();
}

function quitarFiltroAlumno() {
    document.getElementById('filtroMatricula').value = '';
    document.getElementById('filtroAlumnoActivo').classList.add('d-none');
    if (typeof buscarReporteCoordinador === 'function') buscarReporteCoordinador();
}

document.addEventListener('DOMContentLoaded', inicializarBuscadorAlumno);
