/**
 * Controla el modal de "Alumnos canalizados" del coordinador: carga las
 * canalizaciones según filtros desde el endpoint de reportes globales, las
 * pinta en tabla con su estatus y permite ver el detalle de cada una en un
 * modal secundario.
 * @author 20253ds074-art
 * @date 2026-08-16
 */
let modalCanalizadosInstancia = null;
let modalDetalleCanalizacionInstancia = null;
let ultimasCanalizaciones = [];

/**
 * Obtiene (creando una única vez) la instancia del modal de Bootstrap de
 * la lista de alumnos canalizados.
 * @returns {bootstrap.Modal} la instancia del modal
 */
function obtenerModalCanalizados() {
    if (!modalCanalizadosInstancia) {
        modalCanalizadosInstancia = new bootstrap.Modal(document.getElementById('modalCanalizados'));
    }
    return modalCanalizadosInstancia;
}

/**
 * Obtiene (creando una única vez) la instancia del modal de Bootstrap del
 * detalle de una canalización.
 * @returns {bootstrap.Modal} la instancia del modal
 */
function obtenerModalDetalleCanalizacion() {
    if (!modalDetalleCanalizacionInstancia) {
        modalDetalleCanalizacionInstancia = new bootstrap.Modal(document.getElementById('modalDetalleCanalizacion'));
    }
    return modalDetalleCanalizacionInstancia;
}

/**
 * Escapa un valor como texto seguro para insertarlo dentro de HTML,
 * evitando inyección de marcado.
 * @param {*} texto - el valor a escapar (se convierte a texto)
 * @returns {string} el texto escapado listo para insertarse como HTML
 */
function escaparHtmlCanalizacion(texto) {
    const div = document.createElement('div');
    div.textContent = texto === undefined || texto === null ? '' : String(texto);
    return div.innerHTML;
}

/**
 * Construye el HTML de un badge de Bootstrap con el color correspondiente
 * al estatus de una canalización (verde si "Atendido", amarillo en otro caso).
 * @param {string} estatus - el estatus de la canalización
 * @returns {string} el HTML del badge
 */
function badgeEstatusCanalizacion(estatus) {
    const clase = estatus === 'Atendido' ? 'text-bg-success' : 'text-bg-warning';
    return '<span class="badge ' + clase + '">' + escaparHtmlCanalizacion(estatus) + '</span>';
}

/**
 * Abre el modal de alumnos canalizados y carga la lista de canalizaciones
 * desde el servidor, aplicando los filtros indicados.
 * @param {Object} [filtros] - pares clave/valor con los filtros a aplicar en la consulta (p.ej. carrera, cuatrimestre, fechas)
 * @returns {void}
 */
function abrirModalCanalizados(filtros) {
    const tbody = document.getElementById('tablaCanalizadosBody');
    tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">Cargando...</td></tr>';
    document.getElementById('avisoSinCanalizaciones').classList.add('d-none');

    obtenerModalCanalizados().show();

    const params = new URLSearchParams();
    params.append('accion', 'canalizacionesDetalle');
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
            ultimasCanalizaciones = data || [];
            pintarTablaCanalizados(ultimasCanalizaciones);
        })
        .catch(function (err) {
            console.error('Error al cargar los alumnos canalizados:', err);
            tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No se pudo cargar la información. Intenta de nuevo.</td></tr>';
        });
}

/**
 * Pinta las filas de la tabla de alumnos canalizados a partir de la lista
 * recibida, o muestra el aviso de "sin canalizaciones" si está vacía.
 * @param {Array<Object>} lista - la lista de canalizaciones a pintar
 * @returns {void}
 */
function pintarTablaCanalizados(lista) {
    const tbody = document.getElementById('tablaCanalizadosBody');
    const aviso = document.getElementById('avisoSinCanalizaciones');

    if (!lista.length) {
        tbody.innerHTML = '';
        aviso.classList.remove('d-none');
        return;
    }
    aviso.classList.add('d-none');

    tbody.innerHTML = lista.map(function (c, indice) {
        return '<tr>' +
            '<td>' + escaparHtmlCanalizacion(c.nombreAlumno) + '</td>' +
            '<td>' + escaparHtmlCanalizacion(c.grupoAsignado) + '</td>' +
            '<td>' + escaparHtmlCanalizacion(c.fechaCanalizacion) + '</td>' +
            '<td>' + escaparHtmlCanalizacion(c.nombreTutor || 'N/D') + '</td>' +
            '<td>' + escaparHtmlCanalizacion(c.nombreArea) + '</td>' +
            '<td>' + badgeEstatusCanalizacion(c.estatus) + '</td>' +
            '<td class="text-center">' +
            '<button type="button" class="btn btn-sm btn-outline-secondary" onclick="verDetalleCanalizacion(' + indice + ')">' +
            '<i class="bi bi-eye"></i> Ver detalles' +
            '</button>' +
            '</td>' +
            '</tr>';
    }).join('');
}

/**
 * Muestra el modal de detalle de una canalización específica de la última
 * lista cargada, ocultando el modal de la lista.
 * @param {number} indice - el índice de la canalización dentro de `ultimasCanalizaciones`
 * @returns {void}
 */
function verDetalleCanalizacion(indice) {
    const c = ultimasCanalizaciones[indice];
    if (!c) return;

    obtenerModalCanalizados().hide();

    document.getElementById('canalizacionDetalleAlumno').textContent = c.nombreAlumno + ' (' + c.matricula + ')';
    document.getElementById('canalizacionDetalleGrupo').textContent = c.grupoAsignado;
    document.getElementById('canalizacionDetalleFecha').textContent = c.fechaCanalizacion;
    document.getElementById('canalizacionDetalleTutor').textContent = c.nombreTutor || 'No disponible';
    document.getElementById('canalizacionDetalleArea').textContent = c.nombreArea;
    document.getElementById('canalizacionDetalleEstatus').innerHTML = badgeEstatusCanalizacion(c.estatus);
    document.getElementById('canalizacionDetalleMotivo').textContent = c.nombreMotivo || 'No especificado';
    document.getElementById('canalizacionDetalleObservaciones').textContent = c.observaciones || 'Sin observaciones.';

    obtenerModalDetalleCanalizacion().show();
}

document.addEventListener('DOMContentLoaded', function () {
    const btnVolver = document.getElementById('btnVolverListaCanalizados');
    if (btnVolver) {
        btnVolver.addEventListener('click', function () {
            obtenerModalDetalleCanalizacion().hide();
            obtenerModalCanalizados().show();
        });
    }
});
