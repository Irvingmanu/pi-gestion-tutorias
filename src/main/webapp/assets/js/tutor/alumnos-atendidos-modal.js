
/**
 * Controla el modal de alumnos atendidos individualmente del reporte del tutor:
 * carga la lista de atenciones vía fetch, la pinta en la tabla y muestra el
 * detalle de una atención seleccionada.
 * @author 20253ds074-art
 * @date 2026-08-16
 */

let modalAlumnosAtendidosInstancia = null;
let modalDetalleAtencionInstancia = null;
let ultimasAtenciones = [];

/**
 * Obtiene (creando si aún no existe) la instancia del modal Bootstrap de alumnos atendidos.
 * @returns {bootstrap.Modal} la instancia del modal
 */
function obtenerModalAlumnosAtendidos() {
    if (!modalAlumnosAtendidosInstancia) {
        modalAlumnosAtendidosInstancia = new bootstrap.Modal(document.getElementById('modalAlumnosAtendidos'));
    }
    return modalAlumnosAtendidosInstancia;
}

/**
 * Obtiene (creando si aún no existe) la instancia del modal Bootstrap del detalle de una atención.
 * @returns {bootstrap.Modal} la instancia del modal
 */
function obtenerModalDetalleAtencion() {
    if (!modalDetalleAtencionInstancia) {
        modalDetalleAtencionInstancia = new bootstrap.Modal(document.getElementById('modalDetalleAtencion'));
    }
    return modalDetalleAtencionInstancia;
}

/**
 * Escapa un valor para insertarlo como texto seguro dentro de HTML, evitando inyección de marcado.
 * @param {*} texto el valor a escapar (se convierte a texto; null/undefined se trata como cadena vacía)
 * @returns {string} el texto escapado listo para insertarse en HTML
 */
function escaparHtmlAtencion(texto) {
    const div = document.createElement('div');
    div.textContent = texto === undefined || texto === null ? '' : String(texto);
    return div.innerHTML;
}

/**
 * Abre el modal de alumnos atendidos y carga vía fetch las atenciones individuales
 * del tutor, aplicando los filtros recibidos como parámetros de consulta.
 * @param {Object} filtros mapa clave-valor de filtros a aplicar (solo se envían las claves con valor truthy)
 * @returns {void}
 */
function abrirModalAlumnosAtendidos(filtros) {
    const tbody = document.getElementById('tablaAlumnosAtendidosBody');
    tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">Cargando...</td></tr>';
    document.getElementById('avisoSinAtenciones').classList.add('d-none');

    obtenerModalAlumnosAtendidos().show();

    const params = new URLSearchParams();
    params.append('accion', 'atencionesIndividuales');
    if (filtros) {
        Object.keys(filtros).forEach(function (clave) {
            if (filtros[clave]) params.append(clave, filtros[clave]);
        });
    }

    fetch(CONTEXT_PATH + '/ReportesServlet?' + params.toString())
        .then(function (resp) {
            if (!resp.ok) throw new Error('Error del servidor: ' + resp.status);
            return resp.json();
        })
        .then(function (data) {
            ultimasAtenciones = data || [];
            pintarTablaAlumnosAtendidos(ultimasAtenciones);
        })
        .catch(function (err) {
            console.error('Error al cargar los alumnos atendidos:', err);
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No se pudo cargar la información. Intenta de nuevo.</td></tr>';
        });
}

/**
 * Renderiza en la tabla del modal las filas de atenciones individuales, o el aviso
 * de "sin atenciones" cuando la lista está vacía.
 * @param {Array<Object>} lista lista de atenciones a pintar
 * @returns {void}
 */
function pintarTablaAlumnosAtendidos(lista) {
    const tbody = document.getElementById('tablaAlumnosAtendidosBody');
    const aviso = document.getElementById('avisoSinAtenciones');

    if (!lista.length) {
        tbody.innerHTML = '';
        aviso.classList.remove('d-none');
        return;
    }
    aviso.classList.add('d-none');

    tbody.innerHTML = lista.map(function (a, indice) {
        return '<tr>' +
            '<td>' + escaparHtmlAtencion(a.tipo) + '</td>' +
            '<td>' + escaparHtmlAtencion(a.fecha) + ' — ' + escaparHtmlAtencion(a.hora) + '</td>' +
            '<td>' + escaparHtmlAtencion(a.grupoAsignado) + '</td>' +
            '<td>' + escaparHtmlAtencion(a.nombreAlumno) + '</td>' +
            '<td>' + escaparHtmlAtencion(a.estado) + '</td>' +
            '<td class="text-center">' +
                '<button type="button" class="btn btn-sm btn-outline-secondary" onclick="verDetalleAtencion(' + indice + ')">' +
                    '<i class="bi bi-eye"></i> Ver detalles' +
                '</button>' +
            '</td>' +
        '</tr>';
    }).join('');
}

/**
 * Muestra el modal de detalle de la atención individual ubicada en el índice dado
 * de la última lista cargada.
 * @param {number} indice posición de la atención dentro de `ultimasAtenciones`
 * @returns {void}
 */
function verDetalleAtencion(indice) {
    const a = ultimasAtenciones[indice];
    if (!a) return;

    obtenerModalAlumnosAtendidos().hide();

    document.getElementById('tituloModalDetalleAtencion').textContent = 'Detalle de la tutoría — ' + a.tipo;
    document.getElementById('atencionDetalleGrupo').textContent = a.grupoAsignado;
    document.getElementById('atencionDetalleAlumno').textContent = a.nombreAlumno + ' (' + a.matricula + ')';
    document.getElementById('atencionDetalleFecha').textContent = a.fecha;
    document.getElementById('atencionDetalleHora').textContent = a.hora;
    document.getElementById('atencionDetalleTemas').textContent = a.temasTratados;
    document.getElementById('atencionDetalleAcuerdos').textContent = a.acuerdos;
    document.getElementById('atencionDetalleVinculo').textContent = a.vinculoDirecto ? a.vinculoDirecto : 'No aplica';

    obtenerModalDetalleAtencion().show();
}

document.addEventListener('DOMContentLoaded', function () {
    const btnVolver = document.getElementById('btnVolverListaAtenciones');
    if (btnVolver) {
        btnVolver.addEventListener('click', function () {
            obtenerModalDetalleAtencion().hide();
            obtenerModalAlumnosAtendidos().show();
        });
    }
});
