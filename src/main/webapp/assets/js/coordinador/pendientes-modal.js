
/**
 * Controla el modal de solicitudes pendientes del reporte global del coordinador:
 * carga la lista vía fetch, la pinta en la tabla, muestra el detalle de una
 * solicitud seleccionada y permite enviar un recordatorio por correo al tutor.
 * @author 20253ds074-art
 * @date 2026-08-16
 */

let modalPendientesInstancia = null;
let modalDetallePendienteInstancia = null;
let ultimasSolicitudesPendientes = [];

/**
 * Obtiene (creando si aún no existe) la instancia del modal Bootstrap de la lista de pendientes.
 * @returns {bootstrap.Modal} la instancia del modal de solicitudes pendientes
 */
function obtenerModalPendientes() {
    if (!modalPendientesInstancia) {
        modalPendientesInstancia = new bootstrap.Modal(document.getElementById('modalSolicitudesPendientes'));
    }
    return modalPendientesInstancia;
}

/**
 * Obtiene (creando si aún no existe) la instancia del modal Bootstrap del detalle de una solicitud pendiente.
 * @returns {bootstrap.Modal} la instancia del modal de detalle
 */
function obtenerModalDetallePendiente() {
    if (!modalDetallePendienteInstancia) {
        modalDetallePendienteInstancia = new bootstrap.Modal(document.getElementById('modalDetalleSolicitudPendiente'));
    }
    return modalDetallePendienteInstancia;
}

/**
 * Escapa un valor para insertarlo como texto seguro dentro de HTML, evitando inyección de marcado.
 * @param {*} texto el valor a escapar (se convierte a texto; null/undefined se trata como cadena vacía)
 * @returns {string} el texto escapado listo para insertarse en HTML
 */
function escaparHtmlPendiente(texto) {
    const div = document.createElement('div');
    div.textContent = texto === undefined || texto === null ? '' : String(texto);
    return div.innerHTML;
}

/**
 * Abre el modal de solicitudes pendientes y carga su contenido vía fetch al endpoint
 * de reportes globales, aplicando los filtros recibidos como parámetros de consulta.
 * @param {Object} filtros mapa clave-valor de filtros a aplicar (solo se envían las claves con valor truthy)
 * @returns {void}
 */
function abrirModalPendientes(filtros) {
    const tbody = document.getElementById('tablaPendientesBody');
    tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Cargando...</td></tr>';
    document.getElementById('avisoSinPendientes').classList.add('d-none');

    obtenerModalPendientes().show();

    const params = new URLSearchParams();
    params.append('accion', 'solicitudesPendientes');
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
            ultimasSolicitudesPendientes = data || [];
            pintarTablaPendientes(ultimasSolicitudesPendientes);
        })
        .catch(function (err) {
            console.error('Error al cargar las solicitudes pendientes:', err);
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No se pudo cargar la información. Intenta de nuevo.</td></tr>';
        });
}

/**
 * Renderiza en la tabla del modal las filas de solicitudes pendientes recibidas,
 * o muestra el aviso de "sin pendientes" cuando la lista está vacía.
 * @param {Array<Object>} lista lista de solicitudes pendientes a pintar
 * @returns {void}
 */
function pintarTablaPendientes(lista) {
    const tbody = document.getElementById('tablaPendientesBody');
    const aviso = document.getElementById('avisoSinPendientes');

    if (!lista.length) {
        tbody.innerHTML = '';
        aviso.classList.remove('d-none');
        return;
    }
    aviso.classList.add('d-none');

    tbody.innerHTML = lista.map(function (s, indice) {
        return '<tr>' +
            '<td>' + escaparHtmlPendiente(s.nombreAlumno) + '</td>' +
            '<td>' + escaparHtmlPendiente(s.asunto) + '</td>' +
            '<td>' + escaparHtmlPendiente(s.fechaPropuesta) + '</td>' +
            '<td><span class="badge text-bg-warning">' + escaparHtmlPendiente(s.estatus) + '</span></td>' +
            '<td class="text-center">' +
            '<button type="button" class="btn btn-sm btn-outline-secondary" onclick="verDetallePendiente(' + indice + ')">' +
            '<i class="bi bi-eye"></i> Ver' +
            '</button>' +
            '</td>' +
            '</tr>';
    }).join('');
}

/**
 * Muestra el modal de detalle de la solicitud pendiente ubicada en el índice dado
 * de la última lista cargada, y configura el botón para recordar al tutor.
 * @param {number} indice posición de la solicitud dentro de `ultimasSolicitudesPendientes`
 * @returns {void}
 */
function verDetallePendiente(indice) {
    const s = ultimasSolicitudesPendientes[indice];
    if (!s) return;

    obtenerModalPendientes().hide();

    const duracionTexto = s.duracion ? ' (' + s.duracion + (s.duracion === 1 ? ' hora' : ' horas') + ')' : '';

    document.getElementById('tituloModalDetalleSolicitudPendiente').textContent = 'Detalle de la solicitud — ' + s.asunto;
    document.getElementById('pendienteDetalleAlumno').textContent = s.nombreAlumno;
    document.getElementById('pendienteDetalleMatricula').textContent = s.matricula;
    document.getElementById('pendienteDetalleAsunto').textContent = s.asunto;
    document.getElementById('pendienteDetalleFecha').textContent = s.fechaPropuesta + (s.horaPropuesta ? ' - ' + s.horaPropuesta : '') + duracionTexto;
    document.getElementById('pendienteDetalleDescripcion').textContent = s.descripcion || 'Sin descripción.';
    document.getElementById('pendienteDetalleTutor').textContent = s.nombreTutor;

    const btnRecordar = document.getElementById('btnRecordarTutor');
    btnRecordar.onclick = function () {
        confirmarRecordatorioTutor(s);
    };

    obtenerModalDetallePendiente().show();
}

/**
 * Muestra un diálogo de confirmación antes de enviar el recordatorio al tutor de una solicitud.
 * @param {Object} solicitud la solicitud sobre la cual se enviará el recordatorio
 * @returns {void}
 */
function confirmarRecordatorioTutor(solicitud) {
    mostrarConfirmacion(
        'advertencia',
        '¿Enviar recordatorio?',
        'Se enviará un correo a ' + solicitud.nombreTutor + ' para que atienda la solicitud de ' + solicitud.nombreAlumno + '.',
        'Sí, enviar',
        function () {
            enviarRecordatorioTutor(solicitud);
        }
    );
}

/**
 * Envía por POST al endpoint de reportes globales la solicitud de recordatorio al tutor
 * y muestra un toast con el resultado de la operación.
 * @param {Object} solicitud la solicitud (debe incluir idSolicitud) sobre la cual enviar el recordatorio
 * @returns {void}
 */
function enviarRecordatorioTutor(solicitud) {
    const params = new URLSearchParams();
    params.append('accion', 'recordarTutorSolicitud');
    params.append('idSolicitud', solicitud.idSolicitud);

    fetch(CONTEXT_PATH + '/reportes-globales', {
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
            console.error('Error al enviar el recordatorio al tutor:', err);
            mostrarToast('error', 'Error', 'Ocurrió un error al enviar el recordatorio.');
        });
}

document.addEventListener('DOMContentLoaded', function () {
    const btnVolver = document.getElementById('btnVolverListaPendientes');
    if (btnVolver) {
        btnVolver.addEventListener('click', function () {
            obtenerModalDetallePendiente().hide();
            obtenerModalPendientes().show();
        });
    }
});
