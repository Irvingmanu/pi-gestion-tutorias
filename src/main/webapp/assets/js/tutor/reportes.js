/**
 * Controla la vista de reportes del tutor: recolecta los filtros del formulario,
 * consulta al servidor los datos del reporte, pinta los KPIs, la gráfica de
 * pastel de canalizaciones y la tabla de canalizaciones.
 * @author Irvingmanu
 * @date 2026-08-07
 */

let graficaPastelReporte = null;

/**
 * Recolecta los filtros activos del formulario, consulta el reporte al servidor
 * vía fetch, y pinta los KPIs, la gráfica de pastel y la tabla de canalizaciones
 * con la respuesta; delega en el callback `onDatos` de las opciones para pintar
 * gráficas adicionales (por ejemplo, de barras).
 * @param {Object} opciones opciones de carga: contextPath, filtros (array de ids de campos) y onDatos (callback opcional)
 * @returns {void}
 */
function cargarReporte(opciones) {
    const mapaParametros = {
        filtroCarrera: 'idCarrera',
        filtroCuatrimestre: 'cuatrimestre',
        filtroGrupo: 'letra',
        filtroTutor: 'idTutor',
        filtroDesde: 'desde',
        filtroHasta: 'hasta',
        filtroMatricula: 'matricula'
    };

    const params = new URLSearchParams();
    params.append('accion', 'datos');
    (opciones.filtros || []).forEach(function (idCampo) {
        const campo = document.getElementById(idCampo);
        if (campo && campo.value) {
            params.set(mapaParametros[idCampo] || idCampo, campo.value);
        }
    });

    const aviso = document.getElementById('avisoReporte');
    if (aviso) aviso.classList.add('d-none');

    fetch(opciones.contextPath + '/ReportesServlet?' + params.toString())
        .then(function (resp) {
            if (!resp.ok) throw new Error('Error del servidor: ' + resp.status);
            return resp.json();
        })
        .then(function (data) {
            pintarKpi('kpiAtendidos', data.totalAtendidos);
            pintarKpi('kpiPidieron', data.totalPidieronTutorias);
            pintarKpi('kpiCanalizados', data.totalCanalizados);
            pintarKpi('kpiPendientes', data.totalPendientes);
            pintarKpi('kpiGruposAtendidos', data.totalGruposAtendidos);
            pintarKpi('kpiAsistencias', data.totalAsistencias);

            try {
                pintarPastelReporte(data.distribucionCanalizados || []);
            } catch (errPastel) {
                console.error('Error al pintar gráfica de pastel:', errPastel);
            }

            try {
                pintarTablaCanalizaciones(data.canalizaciones || []);
            } catch (errTabla) {
                console.error('Error al pintar tabla de canalizaciones:', errTabla);
            }

            if (typeof opciones.onDatos === 'function') {
                try {
                    opciones.onDatos(data);
                } catch (errBarras) {
                    console.error('Error al pintar gráfica de barras:', errBarras);
                }
            }
        })
        .catch(function (err) {
            console.error('Error al cargar el reporte:', err);
            if (aviso) {
                aviso.textContent = 'No se pudo cargar el reporte. Intenta de nuevo.';
                aviso.classList.remove('d-none');
            }
        });
}

/**
 * Coloca el valor de un indicador (KPI) en el elemento con el id dado, mostrando
 * "--" cuando el valor es nulo o indefinido.
 * @param {string} id el id del elemento HTML donde escribir el valor
 * @param {number|string} valor el valor del KPI a mostrar
 * @returns {void}
 */
function pintarKpi(id, valor) {
    const el = document.getElementById(id);
    if (el) el.textContent = (valor !== undefined && valor !== null) ? valor : '--';
}

/**
 * Escapa un valor para insertarlo como texto seguro dentro de HTML, evitando inyección de marcado.
 * @param {*} texto el valor a escapar (se convierte a texto; null/undefined se trata como cadena vacía)
 * @returns {string} el texto escapado listo para insertarse en HTML
 */
function escaparHtml(texto) {
    const div = document.createElement('div');
    div.textContent = texto === undefined || texto === null ? '' : String(texto);
    return div.innerHTML;
}

/**
 * Renderiza en la tabla del reporte las filas de canalizaciones, o un mensaje de
 * "sin canalizaciones" cuando la lista está vacía.
 * @param {Array<Object>} lista lista de canalizaciones a pintar
 * @returns {void}
 */
function pintarTablaCanalizaciones(lista) {
    const tbody = document.getElementById('tablaCanalizacionesBody');
    if (!tbody) return;

    if (lista.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No hay canalizaciones registradas en este periodo.</td></tr>';
        return;
    }

    tbody.innerHTML = lista.map(function (c) {
        const badge = c.estatus === 'Pendiente' ? 'warning' : 'success';
        return '<tr>' +
            '<td>' + escaparHtml(c.nombreArea) + '</td>' +
            '<td>' + escaparHtml(c.nombreMotivo) + '</td>' +
            '<td><span class="badge text-bg-' + badge + '">' + escaparHtml(c.estatus) + '</span></td>' +
            '<td>' + escaparHtml(c.fechaCanalizacion) + '</td>' +
            '</tr>';
    }).join('');
}

/**
 * Dibuja (o redibuja, destruyendo la instancia anterior) la gráfica de pastel
 * (doughnut) con la distribución de canalizaciones por área de apoyo.
 * @param {Array<Object>} distribucion lista de distribución, cada elemento con nombreServicio y totalAbsoluto
 * @returns {void}
 */
function pintarPastelReporte(distribucion) {
    const ctx = document.getElementById('graficaPastel');
    if (!ctx) return;

    const etiquetas = distribucion.map(function (d) { return d.nombreServicio; });
    const valores = distribucion.map(function (d) { return d.totalAbsoluto; });

    if (graficaPastelReporte) graficaPastelReporte.destroy();
    graficaPastelReporte = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: etiquetas,
            datasets: [{
                data: valores,
                backgroundColor: ['#008B74', '#8FD9C4', '#0B2544', '#7FA8C9', '#CC5052']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { position: 'bottom' } }
        }
    });
}