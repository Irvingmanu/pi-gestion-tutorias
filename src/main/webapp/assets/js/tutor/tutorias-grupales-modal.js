
/**
 * Controla el modal de avance de tutorías grupales del reporte del tutor: carga
 * el avance de cada grupo propio respecto al objetivo del periodo vigente y
 * permite ver el detalle de las sesiones registradas de un grupo.
 * @author 20253ds074-art
 * @date 2026-08-16
 */

let modalTutoriasGrupalesInstancia = null;
let modalDetalleSesionGrupalInstancia = null;
let ultimoAvanceGrupalTutor = [];

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
 * Abre el modal de avance de tutorías grupales y carga vía fetch el avance de
 * cada grupo del tutor respecto al objetivo del periodo escolar vigente.
 * @returns {void}
 */
function abrirModalTutoriasGrupales() {
    const tbody = document.getElementById('tablaTutoriasGrupalesBody');
    tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">Cargando...</td></tr>';
    document.getElementById('avisoSinPeriodoGrupal').classList.add('d-none');

    obtenerModalTutoriasGrupales().show();

    fetch(CONTEXT_PATH + '/ReportesServlet?accion=avanceGrupal')
        .then(function (resp) {
            if (!resp.ok) throw new Error('Error del servidor: ' + resp.status);
            return resp.json();
        })
        .then(function (data) {
            ultimoAvanceGrupalTutor = data.avance || [];

            const subtitulo = document.getElementById('subtituloModalTutoriasGrupales');
            if (data.periodo) {
                subtitulo.textContent = 'Periodo vigente: ' + data.periodo + ' — Objetivo: ' + data.objetivo + ' tutorías grupales por grupo.';
            } else {
                document.getElementById('avisoSinPeriodoGrupal').classList.remove('d-none');
            }

            pintarTablaTutoriasGrupales(ultimoAvanceGrupalTutor);
        })
        .catch(function (err) {
            console.error('Error al cargar el avance de tutorías grupales:', err);
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No se pudo cargar la información. Intenta de nuevo.</td></tr>';
        });
}

/**
 * Genera el HTML del badge visual correspondiente al estatus de avance grupal de un grupo.
 * @param {string} estatus el estatus del grupo ('AL_DIA', 'RIESGO' u otro)
 * @returns {string} el HTML del badge correspondiente
 */
function badgeEstatusGrupal(estatus) {
    if (estatus === 'AL_DIA') return '<span class="badge badge-al-dia">Al día</span>';
    if (estatus === 'RIESGO') return '<span class="badge badge-en-riesgo">En Riesgo</span>';
    return '<span class="badge badge-sin-objetivo">Sin objetivo</span>';
}

/**
 * Renderiza en la tabla del modal las filas de avance grupal por grupo del tutor.
 * @param {Array<Object>} lista lista del avance grupal de cada grupo
 * @returns {void}
 */
function pintarTablaTutoriasGrupales(lista) {
    const tbody = document.getElementById('tablaTutoriasGrupalesBody');

    if (!lista.length) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No tienes grupos asignados en el periodo vigente.</td></tr>';
        return;
    }

    tbody.innerHTML = lista.map(function (a, indice) {
        return '<tr>' +
            '<td>' + escaparHtmlGrupal(a.grupoAsignado) + '</td>' +
            '<td>' + a.realizadas + ' de ' + a.objetivo + '</td>' +
            '<td>' + badgeEstatusGrupal(a.estatus) + '</td>' +
            '<td class="text-center">' +
            '<button type="button" class="btn btn-sm btn-outline-secondary" onclick="verDetalleSesionesGrupal(' + indice + ')">' +
            '<i class="bi bi-eye"></i> Ver detalles' +
            '</button>' +
            '</td>' +
            '</tr>';
    }).join('');
}

/**
 * Muestra el modal de detalle de sesiones grupales del grupo en el índice dado
 * y carga vía fetch la lista de sesiones registradas de ese grupo.
 * @param {number} indice posición del grupo dentro de `ultimoAvanceGrupalTutor`
 * @returns {void}
 */
function verDetalleSesionesGrupal(indice) {
    const fila = ultimoAvanceGrupalTutor[indice];
    if (!fila) return;

    obtenerModalTutoriasGrupales().hide();

    document.getElementById('tituloModalDetalleSesionGrupal').textContent = 'Detalle de sesiones — ' + fila.grupoAsignado;
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
    params.append('idGrupo', fila.idGrupo);

    fetch(CONTEXT_PATH + '/ReportesServlet?' + params.toString())
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
