
/**
 * Controla el buscador de alumnos (con autocompletado) del reporte del tutor:
 * consulta al servidor con debounce mientras el usuario escribe, pinta los
 * resultados, y aplica/quita el filtro de alumno seleccionado en el reporte.
 * @author 20253ds074-art
 * @date 2026-08-16
 */

let temporizadorBusquedaAlumno = null;

/**
 * Escapa un valor para insertarlo como texto seguro dentro de HTML, evitando inyección de marcado.
 * @param {*} texto el valor a escapar (se convierte a texto; null/undefined se trata como cadena vacía)
 * @returns {string} el texto escapado listo para insertarse en HTML
 */
function escaparHtmlBuscador(texto) {
    const div = document.createElement('div');
    div.textContent = texto === undefined || texto === null ? '' : String(texto);
    return div.innerHTML;
}

/**
 * Inicializa el buscador de alumnos: escucha la entrada de texto para disparar
 * la búsqueda con debounce, cierra el listado al hacer clic fuera, y conecta
 * el botón para quitar el filtro de alumno activo.
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

/**
 * Renderiza el listado desplegable de resultados del buscador de alumnos, o un
 * mensaje de "sin coincidencias" cuando la lista está vacía.
 * @param {Array<Object>} lista lista de alumnos coincidentes a mostrar
 * @returns {void}
 */
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

/**
 * Aplica como filtro activo del reporte al alumno seleccionado del listado de
 * resultados del buscador, y refresca el reporte del tutor.
 * @param {number} indice posición del alumno dentro de los resultados guardados en el dataset del contenedor
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

    if (typeof buscarReporteTutor === 'function') buscarReporteTutor();
}

/**
 * Quita el filtro de alumno activo del reporte del tutor y lo refresca.
 * @returns {void}
 */
function quitarFiltroAlumno() {
    document.getElementById('filtroMatricula').value = '';
    document.getElementById('filtroAlumnoActivo').classList.add('d-none');
    if (typeof buscarReporteTutor === 'function') buscarReporteTutor();
}

document.addEventListener('DOMContentLoaded', inicializarBuscadorAlumno);
