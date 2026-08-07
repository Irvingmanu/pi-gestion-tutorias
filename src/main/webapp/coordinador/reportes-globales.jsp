<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Reportes Globales</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/alertas.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>
    <style>
        #filtroDesde, #filtroHasta {
            cursor: pointer;
        }
    </style>
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-coordinador.jsp" />

    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Reportes Globales
        </div>

        <div class="row g-3 mb-2">
            <div class="col-md-3">
                <label for="filtroCarrera" class="form-label fw-bold">Carrera</label>
                <select id="filtroCarrera" class="form-select form-control-figma">
                    <option value="">Seleccione la carrera</option>
                    <c:forEach var="c" items="${listaCarreras}">
                        <option value="${c.idCarrera}">${c.nombre}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-3">
                <label for="filtroCuatrimestre" class="form-label fw-bold">Cuatrimestre</label>
                <select id="filtroCuatrimestre" class="form-select form-control-figma">
                    <option value="">Seleccione el cuatrimestre</option>
                    <c:forEach var="c" items="${listaCuatrimestres}">
                        <option value="${c.idCuatrimestre}">${c.numero}°</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-3">
                <label for="filtroGrupo" class="form-label fw-bold">Grupo</label>
                <select id="filtroGrupo" class="form-select form-control-figma">
                    <option value="">Seleccione el grupo</option>
                    <c:forEach var="l" items="${listaLetrasGrupo}">
                        <option value="${l.idLetra}">${l.letra}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-3">
                <label for="filtroTutor" class="form-label fw-bold">Tutor</label>
                <select id="filtroTutor" class="form-select form-control-figma">
                    <option value="">Todos los tutores</option>
                    <c:forEach var="t" items="${listaTutores}">
                        <option value="${t.idTutor}">${t.nombres} ${t.apellidos}</option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div class="row g-3 mb-4 align-items-end">
            <div class="col-md-4">
                <label for="filtroDesde" class="form-label fw-bold">Desde</label>
                <input type="date" id="filtroDesde" class="form-control form-control-figma">
            </div>
            <div class="col-md-4">
                <label for="filtroHasta" class="form-label fw-bold">Hasta</label>
                <input type="date" id="filtroHasta" class="form-control form-control-figma">
            </div>
            <div class="col-md-4">
                <button type="button" id="btnBuscar" class="btn btn-figma w-100">Buscar</button>
            </div>
        </div>

        <div id="avisoReporte" class="alert alert-warning d-none" role="alert"></div>

        <div class="row g-3 mb-4">
            <div class="col-md-3">
                <div class="p-3 bg-white rounded-figma shadow-sm border d-flex align-items-center gap-3">
                    <i class="bi bi-person-check fs-2" style="color:#008B74;"></i>
                    <div>
                        <div class="text-muted small">Alumnos Atendidos</div>
                        <div class="fw-bold fs-4" id="kpiAtendidos">--</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="p-3 bg-white rounded-figma shadow-sm border d-flex align-items-center gap-3">
                    <i class="bi bi-person-plus fs-2" style="color:#008B74;"></i>
                    <div>
                        <div class="text-muted small">Pidieron Tutorías</div>
                        <div class="fw-bold fs-4" id="kpiPidieron">--</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="p-3 bg-white rounded-figma shadow-sm border d-flex align-items-center gap-3">
                    <i class="bi bi-signpost-split fs-2" style="color:#008B74;"></i>
                    <div>
                        <div class="text-muted small">Canalizados</div>
                        <div class="fw-bold fs-4" id="kpiCanalizados">--</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="p-3 bg-white rounded-figma shadow-sm border d-flex align-items-center gap-3">
                    <i class="bi bi-hourglass-split fs-2" style="color:#008B74;"></i>
                    <div>
                        <div class="text-muted small">Pendientes</div>
                        <div class="fw-bold fs-4" id="kpiPendientes">--</div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row g-3">
            <div class="col-md-6">
                <div class="p-3 bg-white rounded-figma shadow-sm border">
                    <div class="fw-bold mb-2">Distribución de Alumnos Canalizados</div>
                    <div style="height: 320px;">
                        <canvas id="graficaPastel"></canvas>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="p-3 bg-white rounded-figma shadow-sm border">
                    <div class="fw-bold mb-2">Estado de Solicitudes de Asesoría (General)</div>
                    <div style="height: 320px;">
                        <canvas id="graficaBarras"></canvas>
                    </div>
                </div>
            </div>
        </div>

        <div class="row g-3 mt-1">
            <div class="col-12">
                <div class="p-3 bg-white rounded-figma shadow-sm border">
                    <div class="fw-bold mb-2">Canalizaciones Detalladas</div>
                    <div class="table-responsive" style="max-height: 320px; overflow-y: auto;">
                        <table class="table table-sm align-middle mb-0">
                            <thead>
                                <tr>
                                    <th>Área</th>
                                    <th>Motivo</th>
                                    <th>Estatus</th>
                                    <th>Fecha</th>
                                </tr>
                            </thead>
                            <tbody id="tablaCanalizacionesBody">
                                <tr><td colspan="4" class="text-center text-muted">--</td></tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <div class="d-flex justify-content-end mt-4 gap-2">
            <button type="button" id="btnExportarExcel" class="btn btn-figma">
                <i class="bi bi-file-earmark-excel"></i> Exportar Excel
            </button>
            <button type="button" id="btnExportarPdf" class="btn btn-figma">
                <i class="bi bi-file-earmark-pdf"></i> Exportar PDF
            </button>
        </div>

    </div>

</div>

<div class="modal fade" id="modalAlerta" tabindex="-1" aria-labelledby="alertaTitulo" aria-hidden="true" data-bs-backdrop="static">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content alerta-card">
            <div class="modal-body alerta-body">
                <div class="alerta-icono-wrap">
                    <div class="alerta-icono" id="alertaIconoCirculo">
                        <img id="alertaIcono" src="" alt="" data-base-path="${pageContext.request.contextPath}/assets/img/alertas/">
                    </div>
                </div>
                <div class="alerta-texto">
                    <h2 class="alerta-titulo" id="alertaTitulo"></h2>
                    <p class="alerta-mensaje" id="alertaMensaje"></p>
                </div>
                <div class="alerta-botones" id="alertaBotones">
                    <button type="button" class="alerta-btn alerta-btn-secundario" id="alertaBtnCancelar" data-bs-dismiss="modal">Cancelar</button>
                    <button type="button" class="alerta-btn alerta-btn-exito" id="alertaBtnAceptar" data-bs-dismiss="modal">Aceptar</button>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="modalConfirmacion" tabindex="-1" aria-labelledby="confirmacionTitulo" aria-hidden="true" data-bs-backdrop="static">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content alerta-card">
            <div class="modal-body alerta-body">
                <div class="confirmacion-icono-wrap">
                    <div class="confirmacion-icono" id="confirmacionIconoCirculo">
                        <img id="confirmacionIcono" src="" alt="" data-base-path="${pageContext.request.contextPath}/assets/img/alertas/">
                    </div>
                </div>
                <h2 class="confirmacion-titulo" id="confirmacionTitulo"></h2>
                <p class="confirmacion-mensaje" id="confirmacionMensaje"></p>
                <div class="confirmacion-botones">
                    <button type="button" class="btn-confirmar-cancelar" id="btnConfirmacionCancelar" data-bs-dismiss="modal">Cancelar</button>
                    <button type="button" class="btn-confirmar" id="btnConfirmacionAceptar">Aceptar</button>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="toast-container position-fixed top-0 end-0 p-3">
    <div id="toastNotificacion" class="toast toast-alerta" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="toast-alerta-cuerpo">
            <div class="toast-alerta-icono" id="toastIconoCirculo">
                <img id="toastIcono" src="" alt="" data-base-path="${pageContext.request.contextPath}/assets/img/alertas/">
            </div>
            <div class="toast-alerta-contenido">
                <p class="toast-alerta-titulo" id="toastTitulo"></p>
                <p class="toast-alerta-mensaje" id="toastMensaje"></p>
            </div>
            <button type="button" class="btn-close toast-alerta-cerrar" data-bs-dismiss="toast" aria-label="Cerrar"></button>
        </div>
        <div class="toast-progress-bar" id="toastBarra"></div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/tutor/reportes.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script>
    const CONTEXT_PATH = "${pageContext.request.contextPath}";
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
            contextPath: CONTEXT_PATH,
            filtros: ['filtroCarrera', 'filtroCuatrimestre', 'filtroGrupo', 'filtroTutor', 'filtroDesde', 'filtroHasta'],
            onDatos: pintarBarrasCoordinador
        });
    }

    document.getElementById('btnBuscar').addEventListener('click', buscarReporteCoordinador);
    document.addEventListener('DOMContentLoaded', buscarReporteCoordinador);

    function habilitarClickCompletoFecha(id) {
        const input = document.getElementById(id);
        input.addEventListener('click', function () {
            if (typeof input.showPicker === 'function') {
                input.showPicker();
            }
        });
    }
    habilitarClickCompletoFecha('filtroDesde');
    habilitarClickCompletoFecha('filtroHasta');

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
        params.append('formato', formato);

        return CONTEXT_PATH + '/ReportesServlet?' + params.toString();
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


</script>
</body>
</html>