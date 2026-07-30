// Compartido entre coordinador/reportes-globales.jsp y tutor/reportes.jsp.
// Pinta las tarjetas KPI que existan en el DOM (si una vista no tiene
// cierta tarjeta, simplemente se ignora) y la gráfica de pastel, que es
// idéntica en ambas pantallas. La gráfica de barras SÍ cambia entre
// coordinador y tutor, así que cada JSP la define con su propio callback.

let graficaPastelReporte = null;

function cargarReporte(opciones) {
    // opciones = {
    //   contextPath: '<%= request.getContextPath() %>',
    //   filtros: ['filtroCarrera', 'filtroCuatrimestre', 'filtroGrupo', 'filtroDesde', 'filtroHasta'],
    //   onDatos: function (data) { ... } // opcional, para pintar la gráfica de barras propia de la vista
    // }
    const mapaParametros = {
        filtroCarrera: 'idCarrera',
        filtroCuatrimestre: 'idCuatrimestre',
        filtroGrupo: 'idLetraGrupo',
        filtroDesde: 'desde',
        filtroHasta: 'hasta'
    };

    const params = new URLSearchParams();
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
            if (!resp.ok) throw new Error('Error del servidor');
            return resp.json();
        })
        .then(function (data) {
            pintarKpi('kpiAtendidos', data.totalAtendidos);
            pintarKpi('kpiPidieron', data.totalPidieronTutorias);
            pintarKpi('kpiCanalizados', data.totalCanalizados);
            pintarKpi('kpiPendientes', data.totalPendientes);
            pintarKpi('kpiGruposAtendidos', data.totalGruposAtendidos);
            pintarKpi('kpiAsistencias', data.totalAsistencias);

            pintarPastelReporte(data.distribucionCanalizados || []);

            if (typeof opciones.onDatos === 'function') {
                opciones.onDatos(data);
            }
        })
        .catch(function () {
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
        options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
    });
}
