// Modal "Pendientes" de tutor/reportes.jsp: lista las solicitudes de tutoria pendientes de
// los alumnos asignados a ESTE tutor (el servidor filtra por el tutor de la sesion, ver
// ReportesServlet). El boton "Ir a Solicitudes" al inicio del modal lleva al modulo donde el
// tutor puede responderlas.
// Requiere que la vista defina antes: const CONTEXT_PATH = "<context-path>";
// y que ya esten cargados alertas.js y bootstrap.js.

let modalPendientesInstancia = null;
let modalDetallePendienteInstancia = null;
let ultimasSolicitudesPendientesTutor = [];

function obtenerModalPendientes() {
    if (!modalPendientesInstancia) {
        modalPendientesInstancia = new bootstrap.Modal(document.getElementById('modalSolicitudesPendientes'));
    }
    return modalPendientesInstancia;
}

function obtenerModalDetallePendiente() {
    if (!modalDetallePendienteInstancia) {
        modalDetallePendienteInstancia = new bootstrap.Modal(document.getElementById('modalDetalleSolicitudPendiente'));
    }
    return modalDetallePendienteInstancia;
}

function escaparHtmlPendiente(texto) {
    const div = document.createElement('div');
    div.textContent = texto === undefined || texto === null ? '' : String(texto);
    return div.innerHTML;
}

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

    fetch(CONTEXT_PATH + '/ReportesServlet?' + params.toString())
        .then(function (resp) {
            if (!resp.ok) throw new Error('Error del servidor: ' + resp.status);
            return resp.json();
        })
        .then(function (data) {
            ultimasSolicitudesPendientesTutor = data || [];
            pintarTablaPendientes(ultimasSolicitudesPendientesTutor);
        })
        .catch(function (err) {
            console.error('Error al cargar las solicitudes pendientes:', err);
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No se pudo cargar la información. Intenta de nuevo.</td></tr>';
        });
}

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

function verDetallePendiente(indice) {
    const s = ultimasSolicitudesPendientesTutor[indice];
    if (!s) return;

    obtenerModalPendientes().hide();

    const duracionTexto = s.duracion ? ' (' + s.duracion + (s.duracion === 1 ? ' hora' : ' horas') + ')' : '';

    document.getElementById('tituloModalDetalleSolicitudPendiente').textContent = 'Detalle de la solicitud — ' + s.asunto;
    document.getElementById('pendienteDetalleAlumno').textContent = s.nombreAlumno;
    document.getElementById('pendienteDetalleMatricula').textContent = s.matricula;
    document.getElementById('pendienteDetalleAsunto').textContent = s.asunto;
    document.getElementById('pendienteDetalleFecha').textContent = s.fechaPropuesta + (s.horaPropuesta ? ' - ' + s.horaPropuesta : '') + duracionTexto;
    document.getElementById('pendienteDetalleDescripcion').textContent = s.descripcion || 'Sin descripción.';

    obtenerModalDetallePendiente().show();
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
