
/**
 * Controla el modal de avance de tutorías grupales del reporte global del coordinador:
 * carga el avance por tutor respecto al objetivo del periodo, permite ver el detalle
 * de las sesiones de un tutor y enviar una alerta por correo a los tutores en riesgo.
 * @author 20253ds074-art
 * @date 2026-08-17
 */

let modalTutoriasGrupalesInstancia = null;
let modalDetalleSesionGrupalInstancia = null;
let ultimoAvanceGrupal = [];

/**
 * Obtiene (creando si aún no existe) la instancia del modal Bootstrap de avance de tutorías grupales.
 * @returns {bootstrap.Modal} la instancia del modal
 */
function obtenerModalTutoriasGrupales() {
    if (!modalTutoriasGrupalesInstancia) {
        modalTutoriasGrupalesInstancia = new bootstrap.Modal(document.getElementById('modalTutoriasGrupales'));
    }
    return modalTutoriasGrupalesInstancia;
}

/**
 * Obtiene (creando si aún no existe) la instancia del modal Bootstrap del detalle de sesiones grupales.
 * @returns {bootstrap.Modal} la instancia del modal
 */
function obtenerModalDetalleSesionGrupal() {
    if (!modalDetalleSesionGrupalInstancia) {
        modalDetalleSesionGrupalInstancia = new bootstrap.Modal(document.getElementById('modalDetalleSesionGrupal'));
    }
    return modalDetalleSesionGrupalInstancia;
}

/**
 * Escapa un valor para insertarlo como texto seguro dentro de HTML, evitando inyección de marcado.
 * @param {*} texto el valor a escapar (se convierte a texto; null/undefined se trata como cadena vacía)
 * @returns {string} el texto escapado listo para insertarse en HTML
 */
function escaparHtmlGrupal(texto) {
    const div = document.createElement('div');
    div.textContent = texto === undefined || texto === null ? '' : String(texto);
    return div.innerHTML;
}

/**
 * Abre el modal de avance de tutorías grupales y carga vía fetch el avance de cada
 * tutor respecto al objetivo del periodo escolar vigente.
 * @returns {void}
 */
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

/**
 * Genera el HTML del badge visual correspondiente al estatus de avance grupal de un tutor.
 * @param {string} estatus el estatus del tutor ('AL_DIA', 'RIESGO' u otro)
 * @returns {string} el HTML del badge correspondiente
 */
function badgeEstatusGrupal(estatus) {
    if (estatus === 'AL_DIA') return '<span class="badge badge-al-dia">Al día</span>';
    if (estatus === 'RIESGO') return '<span class="badge badge-en-riesgo">En Riesgo</span>';
    return '<span class="badge badge-sin-objetivo">Sin objetivo</span>';
}

/**
 * Renderiza en la tabla del modal las filas de avance grupal por tutor.
 * @param {Array<Object>} lista lista del avance grupal de cada tutor
 * @returns {void}
 */
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

/**
 * Muestra un diálogo de confirmación antes de enviar la alerta de avance a un tutor.
 * @param {number} indice posición del tutor dentro de `ultimoAvanceGrupal`
 * @returns {void}
 */
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

/**
 * Envía por POST al endpoint de reportes globales la alerta al tutor por su avance
 * de tutorías grupales, y muestra un toast con el resultado.
 * @param {Object} fila la fila de avance del tutor (debe incluir idTutor e idGrupo)
 * @returns {void}
 */
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

/**
 * Muestra el modal de detalle de sesiones grupales del tutor en el índice dado
 * y carga vía fetch la lista de sesiones de su grupo.
 * @param {number} indice posición del tutor dentro de `ultimoAvanceGrupal`
 * @returns {void}
 */
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

/**
 * Renderiza en el contenedor de detalle las tarjetas con la información de cada
 * sesión grupal (fecha, hora, temas, acuerdos y asesorías grupales).
 * @param {Array<Object>} sesiones lista de sesiones grupales a pintar
 * @returns {void}
 */
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
