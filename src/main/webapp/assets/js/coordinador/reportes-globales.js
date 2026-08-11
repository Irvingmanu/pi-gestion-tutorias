// Logica dedicada a reportes-globales.jsp: dibuja la grafica de barras,
// dispara la busqueda de KPIs/graficas via cargarReporte() (definida en
// tutor/reportes.js) y maneja la exportacion a Excel/PDF/CSV.
//
// Requiere que la vista defina antes: window.CONTEXT_PATH = "<context-path>";

let graficaBarrasCoordinador = null;

function pintarBarrasCoordinador(data) {
    const ctx = document.getElementById('graficaBarras');

    if (graficaBarrasCoordinador) graficaBarrasCoordinador.destroy();
    graficaBarrasCoordinador = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Pendientes', 'Atendidas', 'Canalizadas'],
            datasets: [{
                data: [data.totalPendientes, data.totalAtendidos, data.totalCanalizados],
                backgroundColor: ['#8FD9C4', '#7FA8C9', '#0B2544']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
        }
    });
}

function buscarReporteCoordinador() {
    cargarReporte({
        contextPath: window.CONTEXT_PATH,
        filtros: ['filtroCarrera', 'filtroCuatrimestre', 'filtroGrupo', 'filtroTutor', 'filtroDesde', 'filtroHasta'],
        onDatos: pintarBarrasCoordinador
    });
}

function habilitarClickCompletoFecha(id) {
    const input = document.getElementById(id);
    input.addEventListener('click', function () {
        if (typeof input.showPicker === 'function') {
            input.showPicker();
        }
    });
}

function construirUrlExport(formato) {
    const params = new URLSearchParams();

    const selectCarrera = document.getElementById('filtroCarrera');
    const selectCuatrimestre = document.getElementById('filtroCuatrimestre');
    const selectGrupo = document.getElementById('filtroGrupo');
    const selectTutor = document.getElementById('filtroTutor');
    const desde = document.getElementById('filtroDesde').value;
    const hasta = document.getElementById('filtroHasta').value;

    if (selectCarrera.value) {
        params.append('idCarrera', selectCarrera.value);
        params.append('nombreCarrera', selectCarrera.options[selectCarrera.selectedIndex].text);
    }
    if (selectCuatrimestre.value) {
        params.append('idCuatrimestre', selectCuatrimestre.value);
        params.append('nombreCuatrimestre', selectCuatrimestre.options[selectCuatrimestre.selectedIndex].text);
    }
    if (selectGrupo.value) {
        params.append('idLetraGrupo', selectGrupo.value);
        params.append('nombreGrupo', selectGrupo.options[selectGrupo.selectedIndex].text);
    }
    if (selectTutor.value) {
        params.append('idTutor', selectTutor.value);
        params.append('nombreTutor', selectTutor.options[selectTutor.selectedIndex].text);
    }
    if (desde) params.append('desde', desde);
    if (hasta) params.append('hasta', hasta);
    if (formato) params.append('formato', formato);

    return window.CONTEXT_PATH + '/ReportesServlet?' + params.toString();
}

async function descargarReporte(formato) {
    const url = construirUrlExport(formato);
    try {
        const resp = await fetch(url);
        if (!resp.ok) throw new Error('Respuesta no válida del servidor');

        const blob = await resp.blob();
        const disposition = resp.headers.get('Content-Disposition') || '';
        const match = disposition.match(/filename="?([^"]+)"?/);
        const nombreArchivo = match ? match[1] : 'reporte_tutorias.' + formato;

        const linkUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = linkUrl;
        a.download = nombreArchivo;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(linkUrl);

        mostrarToast('exito', 'Éxito', 'El reporte se descargó correctamente');
    } catch (err) {
        console.error(err);
        mostrarToast('error', 'Error', 'Ocurrió un error al exportar el reporte');
    }
}

async function descargarReporteCsv() {
    const url = construirUrlExport();
    try {
        const resp = await fetch(url);
        if (!resp.ok) throw new Error('Respuesta no válida del servidor');

        const blob = await resp.blob();
        const disposition = resp.headers.get('Content-Disposition') || '';
        const match = disposition.match(/filename="?([^"]+)"?/);
        const nombreArchivo = match ? match[1] : 'reporte_tutorias.csv';

        const linkUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = linkUrl;
        a.download = nombreArchivo;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(linkUrl);

        mostrarToast('exito', 'Éxito', 'El reporte se descargó correctamente');
    } catch (err) {
        console.error(err);
        mostrarToast('error', 'Error', 'Ocurrió un error al exportar el reporte');
    }
}

document.addEventListener('DOMContentLoaded', function () {
    document.getElementById('btnBuscar').addEventListener('click', buscarReporteCoordinador);
    buscarReporteCoordinador();

    habilitarClickCompletoFecha('filtroDesde');
    habilitarClickCompletoFecha('filtroHasta');

    document.getElementById('btnExportarExcel').addEventListener('click', function () {
        mostrarConfirmacion(
            'advertencia',
            '¿Deseas descargar?',
            'Estás a punto de exportar el reporte en formato Excel.',
            'Sí, descargar',
            () => {
                descargarReporte('excel');
            }
        );
    });

    document.getElementById('btnExportarPdf').addEventListener('click', function () {
        mostrarConfirmacion(
            'advertencia',
            '¿Deseas descargar?',
            'Estás a punto de exportar el reporte en formato PDF.',
            'Sí, descargar',
            () => {
                descargarReporte('pdf');
            }
        );
    });
});