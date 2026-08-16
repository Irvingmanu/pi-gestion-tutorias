let graficaPastelReporte = null;

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

function pintarKpi(id, valor) {
    const el = document.getElementById(id);
    if (el) el.textContent = (valor !== undefined && valor !== null) ? valor : '--';
}

// Escapa texto libre antes de insertarlo como HTML: nombreMotivo/observaciones puede venir
// de un campo de texto libre que capturo el tutor (observacionesCanalizacion), asi que no es
// seguro insertarlo directo en innerHTML.
function escaparHtml(texto) {
    const div = document.createElement('div');
    div.textContent = texto === undefined || texto === null ? '' : String(texto);
    return div.innerHTML;
}

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