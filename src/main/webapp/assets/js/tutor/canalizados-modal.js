
/**
 * Controla el modal de canalizaciones del reporte del tutor: carga el listado
 * vía fetch, lo pinta en la tabla, muestra el detalle de una canalización y
 * permite enviar un recordatorio al área de apoyo cuando sigue "En proceso".
 * @author 20253ds074-art
 * @date 2026-08-16
 */

let modalCanalizadosInstancia = null;
let modalDetalleCanalizacionInstancia = null;
let ultimasCanalizacionesTutor = [];

/**
 * Obtiene (creando si aún no existe) la instancia del modal Bootstrap de la lista de canalizaciones.
 * @returns {bootstrap.Modal} la instancia del modal
 */
function obtenerModalCanalizados() {
    if (!modalCanalizadosInstancia) {
        modalCanalizadosInstancia = new bootstrap.Modal(document.getElementById('modalCanalizados'));
    }
    return modalCanalizadosInstancia;
}

/**
 * Obtiene (creando si aún no existe) la instancia del modal Bootstrap del detalle de una canalización.
 * @returns {bootstrap.Modal} la instancia del modal
 */
function obtenerModalDetalleCanalizacion() {
    if (!modalDetalleCanalizacionInstancia) {
        modalDetalleCanalizacionInstancia = new bootstrap.Modal(document.getElementById('modalDetalleCanalizacion'));
    }
    return modalDetalleCanalizacionInstancia;
}

/**
 * Escapa un valor para insertarlo como texto seguro dentro de HTML, evitando inyección de marcado.
 * @param {*} texto el valor a escapar (se convierte a texto; null/undefined se trata como cadena vacía)
 * @returns {string} el texto escapado listo para insertarse en HTML
 */
function escaparHtmlCanalizacion(texto) {
    const div = document.createElement('div');
    div.textContent = texto === undefined || texto === null ? '' : String(texto);
    return div.innerHTML;
}

/**
 * Genera el HTML del badge visual correspondiente al estatus de una canalización.
 * @param {string} estatus el estatus de la canalización ('Atendido' u otro)
 * @returns {string} el HTML del badge correspondiente
 */
function badgeEstatusCanalizacion(estatus) {
    const clase = estatus === 'Atendido' ? 'text-bg-success' : 'text-bg-warning';
    return '<span class="badge ' + clase + '">' + escaparHtmlCanalizacion(estatus) + '</span>';
}

/**
 * Abre el modal de canalizaciones y carga vía fetch su detalle, aplicando los
 * filtros recibidos como parámetros de consulta.
 * @param {Object} filtros mapa clave-valor de filtros a aplicar (solo se envían las claves con valor truthy)
 * @returns {void}
 */
function abrirModalCanalizados(filtros) {
    const tbody = document.getElementById('tablaCanalizadosBody');
    tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">Cargando...</td></tr>';
    document.getElementById('avisoSinCanalizaciones').classList.add('d-none');

    obtenerModalCanalizados().show();

    const params = new URLSearchParams();
    params.append('accion', 'canalizacionesDetalle');
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
            ultimasCanalizacionesTutor = data || [];
            pintarTablaCanalizados(ultimasCanalizacionesTutor);
        })
        .catch(function (err) {
            console.error('Error al cargar las canalizaciones:', err);
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No se pudo cargar la información. Intenta de nuevo.</td></tr>';
        });
}

/**
 * Renderiza en la tabla del modal las filas de canalizaciones, o el aviso de
 * "sin canalizaciones" cuando la lista está vacía.
 * @param {Array<Object>} lista lista de canalizaciones a pintar
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
 * Muestra el modal de detalle de la canalización ubicada en el índice dado de
 * la última lista cargada, mostrando el botón de recordatorio solo si sigue "En proceso".
 * @param {number} indice posición de la canalización dentro de `ultimasCanalizacionesTutor`
 * @returns {void}
 */
function verDetalleCanalizacion(indice) {
    const c = ultimasCanalizacionesTutor[indice];
    if (!c) return;

    obtenerModalCanalizados().hide();

    document.getElementById('canalizacionDetalleAlumno').textContent = c.nombreAlumno + ' (' + c.matricula + ')';
    document.getElementById('canalizacionDetalleGrupo').textContent = c.grupoAsignado;
    document.getElementById('canalizacionDetalleFecha').textContent = c.fechaCanalizacion;
    document.getElementById('canalizacionDetalleArea').textContent = c.nombreArea;
    document.getElementById('canalizacionDetalleEstatus').innerHTML = badgeEstatusCanalizacion(c.estatus);
    document.getElementById('canalizacionDetalleMotivo').textContent = c.nombreMotivo || 'No especificado';
    document.getElementById('canalizacionDetalleObservaciones').textContent = c.observaciones || 'Sin observaciones.';

    const btnRecordar = document.getElementById('btnRecordarAreaApoyo');
    if (c.estatus === 'En proceso') {
        btnRecordar.classList.remove('d-none');
        btnRecordar.onclick = function () {
            confirmarRecordatorioArea(c);
        };
    } else {
        btnRecordar.classList.add('d-none');
        btnRecordar.onclick = null;
    }

    obtenerModalDetalleCanalizacion().show();
}

/**
 * Muestra un diálogo de confirmación antes de enviar el recordatorio al área de apoyo.
 * @param {Object} canalizacion la canalización sobre la cual se enviará el recordatorio
 * @returns {void}
 */
function confirmarRecordatorioArea(canalizacion) {
    mostrarConfirmacion(
        'advertencia',
        '¿Enviar recordatorio?',
        'Se enviará un correo al encargado de ' + canalizacion.nombreArea + ' para darle seguimiento a la atención de ' + canalizacion.nombreAlumno + '.',
        'Sí, enviar',
        function () {
            enviarRecordatorioArea(canalizacion);
        }
    );
}

/**
 * Envía por POST al endpoint de reportes la solicitud de recordatorio al área de
 * apoyo y muestra un toast con el resultado de la operación.
 * @param {Object} canalizacion la canalización (debe incluir idCanalizacion) sobre la cual enviar el recordatorio
 * @returns {void}
 */
function enviarRecordatorioArea(canalizacion) {
    const params = new URLSearchParams();
    params.append('accion', 'recordarAreaApoyo');
    params.append('idCanalizacion', canalizacion.idCanalizacion);

    fetch(CONTEXT_PATH + '/ReportesServlet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
        .then(function (resp) { return resp.json(); })
        .then(function (data) {
            if (data.exito) {
                mostrarToast('exito', 'Recordatorio enviado', data.mensaje);
            } else {
                mostrarToast('error', 'Error', data.mensaje || 'No se pudo enviar el recordatorio.');
            }
        })
        .catch(function (err) {
            console.error('Error al enviar el recordatorio al área:', err);
            mostrarToast('error', 'Error', 'Ocurrió un error al enviar el recordatorio.');
        });
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
