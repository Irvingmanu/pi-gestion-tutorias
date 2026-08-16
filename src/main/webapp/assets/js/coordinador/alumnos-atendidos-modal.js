// Modal "Alumnos Atendidos" de reportes-globales.jsp: lista las sesiones Individual y
// Espontánea completadas (excluye estrictamente las grupales, que tienen su propio modal
// de "Tutorías Grupales"), respetando los mismos filtros de la barra de Reportes Globales
// para que el desglose sea coherente con el KPI de la tarjeta.
// Requiere que la vista defina antes: const CONTEXT_PATH = "<context-path>";
// y que ya esten cargados alertas.js y bootstrap.js.

let modalAlumnosAtendidosInstancia = null;
let modalDetalleAtencionInstancia = null;
let ultimasAtenciones = [];

function obtenerModalAlumnosAtendidos() {
    if (!modalAlumnosAtendidosInstancia) {
        modalAlumnosAtendidosInstancia = new bootstrap.Modal(document.getElementById('modalAlumnosAtendidos'));
    }
    return modalAlumnosAtendidosInstancia;
}

function obtenerModalDetalleAtencion() {
    if (!modalDetalleAtencionInstancia) {
        modalDetalleAtencionInstancia = new bootstrap.Modal(document.getElementById('modalDetalleAtencion'));
    }
    return modalDetalleAtencionInstancia;
}

function escaparHtmlAtencion(texto) {
    const div = document.createElement('div');
    div.textContent = texto === undefined || texto === null ? '' : String(texto);
    return div.innerHTML;
}

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
