/**
 * Implementa el buscador de alumnos por matrícula o nombre (autocompletado
 * con debounce) usado en los reportes globales del coordinador para filtrar
 * por un alumno específico.
 * @author 20253ds074-art
 * @date 2026-08-16
 */
let temporizadorBusquedaAlumno = null;

/**
 * Escapa un valor como texto seguro para insertarlo dentro de HTML,
 * evitando inyección de marcado.
 * @param {*} texto - el valor a escapar (se convierte a texto)
 * @returns {string} el texto escapado listo para insertarse como HTML
 */
function escaparHtmlBuscador(texto) {
    const div = document.createElement('div');
    div.textContent = texto === undefined || texto === null ? '' : String(texto);
    return div.innerHTML;
}

/**
 * Inicializa el buscador de alumnos: engancha el listener de búsqueda con
 * debounce sobre el input, el cierre del panel de resultados al hacer clic
 * fuera, y el botón para quitar el filtro de alumno activo.
 * @returns {void}
 */
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

/**
 * Pinta la lista de resultados del buscador de alumnos, o un mensaje de
 * "sin coincidencias" si la lista está vacía. Guarda los resultados en el
 * dataset del contenedor para poder seleccionarlos después por índice.
 * @param {Array<Object>} lista - la lista de alumnos encontrados
 * @returns {void}
 */
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

/**
 * Selecciona un alumno de los resultados del buscador: fija el filtro de
 * matrícula, muestra el chip del alumno activo y refresca la trayectoria y
 * el reporte del coordinador con el nuevo filtro.
 * @param {number} indice - el índice del alumno dentro de los resultados mostrados
 * @returns {void}
 */
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

    if (typeof cargarTrayectoriaAlumno === 'function') cargarTrayectoriaAlumno(alumno.matricula);
    if (typeof buscarReporteCoordinador === 'function') buscarReporteCoordinador();
}

/**
 * Quita el filtro de alumno activo del reporte del coordinador, ocultando el
 * chip del alumno y refrescando la trayectoria y el reporte sin ese filtro.
 * @returns {void}
 */
function quitarFiltroAlumno() {
    document.getElementById('filtroMatricula').value = '';
    document.getElementById('filtroAlumnoActivo').classList.add('d-none');
    if (typeof ocultarTrayectoriaAlumno === 'function') ocultarTrayectoriaAlumno();
    if (typeof buscarReporteCoordinador === 'function') buscarReporteCoordinador();
}

document.addEventListener('DOMContentLoaded', inicializarBuscadorAlumno);
