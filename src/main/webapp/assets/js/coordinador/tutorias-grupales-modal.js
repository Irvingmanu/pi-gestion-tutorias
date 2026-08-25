
let modalTutoriasGrupalesInstancia = null;
let modalDetalleSesionGrupalInstancia = null;
let ultimoAvanceGrupal = [];

function obtenerModalTutoriasGrupales() {
    if (!modalTutoriasGrupalesInstancia) {
        modalTutoriasGrupalesInstancia = new bootstrap.Modal(document.getElementById('modalTutoriasGrupales'));
    }
    return modalTutoriasGrupalesInstancia;
}

function obtenerModalDetalleSesionGrupal() {
    if (!modalDetalleSesionGrupalInstancia) {
        modalDetalleSesionGrupalInstancia = new bootstrap.Modal(document.getElementById('modalDetalleSesionGrupal'));
    }
    return modalDetalleSesionGrupalInstancia;
}

function escaparHtmlGrupal(texto) {
    const div = document.createElement('div');
    div.textContent = texto === undefined || texto === null ? '' : String(texto);
    return div.innerHTML;
}

function abrirModalTutoriasGrupales() {
    const tbody = document.getElementById('tablaTutoriasGrupalesBody');
    tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Cargando...</td></tr>';
    document.getElementById('avisoSinPeriodoGrupal').classList.add('d-none');

    obtenerModalTutoriasGrupales().show();

    fetch(CONTEXT_PATH + '/reportes-globales?accion=avanceGrupal')
        .then(function (resp) {
            if (!resp.ok) throw new Error('Error del servidor: ' + resp.status);
            return resp.json();
        })
        .then(function (data) {
            ultimoAvanceGrupal = data.avance || [];

            const subtitulo = document.getElementById('subtituloModalTutoriasGrupales');
            if (data.periodo) {
                subtitulo.textContent = 'Periodo vigente: ' + data.periodo + ' — Objetivo: ' + data.objetivo + ' tutorías grupales por tutor.';
            } else {
                document.getElementById('avisoSinPeriodoGrupal').classList.remove('d-none');
            }

            pintarTablaTutoriasGrupales(ultimoAvanceGrupal);
        })
        .catch(function (err) {
            console.error('Error al cargar el avance de tutorías grupales:', err);
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No se pudo cargar la información. Intenta de nuevo.</td></tr>';
        });
}

function badgeEstatusGrupal(estatus) {
    if (estatus === 'AL_DIA') return '<span class="badge badge-al-dia">Al día</span>';
    if (estatus === 'RIESGO') return '<span class="badge badge-en-riesgo">En Riesgo</span>';
    return '<span class="badge badge-sin-objetivo">Sin objetivo</span>';
}

function pintarTablaTutoriasGrupales(lista) {
    const tbody = document.getElementById('tablaTutoriasGrupalesBody');

    if (!lista.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No hay tutores con grupos asignados en el periodo vigente.</td></tr>';
        return;
    }

    tbody.innerHTML = lista.map(function (a, indice) {
        const enRiesgo = a.estatus === 'RIESGO';
        return '<tr>' +
            '<td>' + escaparHtmlGrupal(a.nombreTutor) + '</td>' +
            '<td>' + escaparHtmlGrupal(a.grupoAsignado) + '</td>' +
            '<td>' + a.realizadas + ' de ' + a.objetivo + '</td>' +
            '<td>' + badgeEstatusGrupal(a.estatus) + '</td>' +
            '<td class="text-center">' +
            '<button type="button" class="btn btn-sm btn-outline-secondary me-1" onclick="verDetalleSesionesGrupal(' + indice + ')">' +
            '<i class="bi bi-eye"></i> Ver detalles' +
            '</button>' +
            '<button type="button" class="btn-alertar-tutor' + (enRiesgo ? ' riesgo' : '') + '" title="Enviar alerta por correo" onclick="confirmarAlertaTutor(' + indice + ')">' +
            '<i class="bi bi-envelope' + (enRiesgo ? '-exclamation-fill' : '') + '"></i>' +
            '</button>' +
            '</td>' +
            '</tr>';
    }).join('');
}

function confirmarAlertaTutor(indice) {
    const fila = ultimoAvanceGrupal[indice];
    if (!fila) return;

    mostrarConfirmacion(
        'advertencia',
        '¿Enviar alerta?',
        'Se enviará un correo a ' + fila.nombreTutor + ' con su avance actual (' + fila.realizadas + ' de ' + fila.objetivo + ').',
        'Sí, enviar',
        function () {
            enviarAlertaTutorGrupal(fila);
        }
    );
}

function enviarAlertaTutorGrupal(fila) {
    const params = new URLSearchParams();
    params.append('accion', 'alertarTutor');
    params.append('idTutor', fila.idTutor);
    params.append('idGrupo', fila.idGrupo);

    fetch(CONTEXT_PATH + '/reportes-globales', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
        .then(function (resp) { return resp.json(); })
        .then(function (data) {
            if (data.exito) {
                mostrarToast('exito', 'Alerta enviada', data.mensaje);
            } else {
                mostrarToast('error', 'Error', data.mensaje || 'No se pudo enviar la alerta.');
            }
        })
        .catch(function (err) {
            console.error('Error al enviar la alerta al tutor:', err);
            mostrarToast('error', 'Error', 'Ocurrió un error al enviar la alerta.');
        });
}

function verDetalleSesionesGrupal(indice) {
    const fila = ultimoAvanceGrupal[indice];
    if (!fila) return;

    obtenerModalTutoriasGrupales().hide();

    document.getElementById('tituloModalDetalleSesionGrupal').textContent = 'Detalle de sesiones — ' + fila.nombreTutor;
    document.getElementById('detalleNombreTutor').textContent = fila.nombreTutor;
    document.getElementById('detalleGrupoAsignado').textContent = fila.grupoAsignado;
    document.getElementById('detalleAvance').textContent = fila.realizadas + ' de ' + fila.objetivo;
    document.getElementById('detalleEstatus').innerHTML = badgeEstatusGrupal(fila.estatus);

    const contenedor = document.getElementById('listaSesionesGrupales');
    const avisoSinSesiones = document.getElementById('avisoSinSesiones');
    contenedor.innerHTML = '';
    avisoSinSesiones.classList.add('d-none');

    obtenerModalDetalleSesionGrupal().show();

    const params = new URLSearchParams();
    params.append('accion', 'detalleSesiones');
    params.append('idTutor', fila.idTutor);
    params.append('idGrupo', fila.idGrupo);

    fetch(CONTEXT_PATH + '/reportes-globales?' + params.toString())
        .then(function (resp) {
            if (!resp.ok) throw new Error('Error del servidor: ' + resp.status);
            return resp.json();
        })
        .then(function (sesiones) {
            if (!sesiones.length) {
                avisoSinSesiones.classList.remove('d-none');
                return;
            }
            pintarListaSesionesGrupales(sesiones);
        })
        .catch(function (err) {
            console.error('Error al cargar el detalle de sesiones:', err);
            contenedor.innerHTML = '<div class="text-center text-muted py-3">No se pudo cargar el detalle de las sesiones.</div>';
        });
}

function pintarListaSesionesGrupales(sesiones) {
    const contenedor = document.getElementById('listaSesionesGrupales');

    contenedor.innerHTML = sesiones.map(function (s, i) {
        const asesorias = s.asesoriasGrupales && s.asesoriasGrupales.trim() !== ''
            ? escaparHtmlGrupal(s.asesoriasGrupales) : 'No capturado';

        return '' +
            '<div class="seccion-detalle-card">' +
            '<div class="seccion-detalle-titulo">Detalles de la Sesión ' + (i + 1) + '</div>' +
            '<div class="row g-3">' +
            '<div class="col-md-6">' +
            '<div class="campo-detalle-label">Fecha</div>' +
            '<div class="campo-detalle-valor fw-semibold">' + escaparHtmlGrupal(s.fecha) + '</div>' +
            '</div>' +
            '<div class="col-md-6">' +
            '<div class="campo-detalle-label">Hora</div>' +
            '<div class="campo-detalle-valor fw-semibold">' + escaparHtmlGrupal(s.hora) + '</div>' +
            '</div>' +
            '<div class="col-12">' +
            '<div class="campo-detalle-label">Temas Tratados</div>' +
            '<div class="campo-detalle-valor">' + escaparHtmlGrupal(s.temasTratados) + '</div>' +
            '</div>' +
            '<div class="col-12">' +
            '<div class="campo-detalle-label">Acuerdos</div>' +
            '<div class="campo-detalle-valor mb-0">' + escaparHtmlGrupal(s.acuerdos) + '</div>' +
            '</div>' +
            '</div>' +
            '</div>' +
            '<div class="seccion-detalle-card">' +
            '<div class="seccion-detalle-titulo">Asesorías Grupales</div>' +
            '<div class="campo-detalle-valor mb-0">' + asesorias + '</div>' +
            '</div>';
    }).join('');
}

document.addEventListener('DOMContentLoaded', function () {
    const btnVolver = document.getElementById('btnVolverListaTutorias');
    if (btnVolver) {
        btnVolver.addEventListener('click', function () {
            obtenerModalDetalleSesionGrupal().hide();
            obtenerModalTutoriasGrupales().show();
        });
    }
});
