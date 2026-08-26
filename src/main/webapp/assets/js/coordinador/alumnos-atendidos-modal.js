/**
 * Controla el modal de "Alumnos atendidos" del coordinador: carga las
 * atenciones individuales según filtros desde el endpoint de reportes
 * globales, las pinta en tabla y permite ver el detalle de cada una en un
 * modal secundario.
 * @author 20253ds074-art
 * @date 2026-08-16
 */
let modalAlumnosAtendidosInstancia = null;
let modalDetalleAtencionInstancia = null;
let ultimasAtenciones = [];

/**
 * Obtiene (creando una única vez) la instancia del modal de Bootstrap de
 * la lista de alumnos atendidos.
 * @returns {bootstrap.Modal} la instancia del modal
 */
function obtenerModalAlumnosAtendidos() {
    if (!modalAlumnosAtendidosInstancia) {
        modalAlumnosAtendidosInstancia = new bootstrap.Modal(document.getElementById('modalAlumnosAtendidos'));
    }
    return modalAlumnosAtendidosInstancia;
}

/**
 * Obtiene (creando una única vez) la instancia del modal de Bootstrap del
 * detalle de una atención individual.
 * @returns {bootstrap.Modal} la instancia del modal
 */
function obtenerModalDetalleAtencion() {
    if (!modalDetalleAtencionInstancia) {
        modalDetalleAtencionInstancia = new bootstrap.Modal(document.getElementById('modalDetalleAtencion'));
    }
    return modalDetalleAtencionInstancia;
}

/**
 * Escapa un valor como texto seguro para insertarlo dentro de HTML,
 * evitando inyección de marcado.
 * @param {*} texto - el valor a escapar (se convierte a texto)
 * @returns {string} el texto escapado listo para insertarse como HTML
 */
function escaparHtmlAtencion(texto) {
    const div = document.createElement('div');
    div.textContent = texto === undefined || texto === null ? '' : String(texto);
    return div.innerHTML;
}

/**
 * Abre el modal de alumnos atendidos y carga la lista de atenciones
 * individuales desde el servidor, aplicando los filtros indicados.
 * @param {Object} [filtros] - pares clave/valor con los filtros a aplicar en la consulta (p.ej. carrera, cuatrimestre, fechas)
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

    fetch(CONTEXT_PATH + '/reportes-globales?' + params.toString())
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
 * Pinta las filas de la tabla de alumnos atendidos a partir de la lista de
 * atenciones recibida, o muestra el aviso de "sin atenciones" si está vacía.
 * @param {Array<Object>} lista - la lista de atenciones a pintar
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
 * Muestra el modal de detalle de una atención individual específica de la
 * última lista cargada, ocultando el modal de la lista.
 * @param {number} indice - el índice de la atención dentro de `ultimasAtenciones`
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
