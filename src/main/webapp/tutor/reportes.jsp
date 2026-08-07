<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Reportes</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/alertas.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>
    <style>
        .card-grafica {
            min-height: 340px;
            display: flex;
            flex-direction: column;
        }
        .card-grafica .grafica-contenido {
            flex-grow: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
        }
        .card-grafica canvas {
            max-height: 260px;
        }
        .grafica-vacio {
            color: #9ca3af;
            font-size: 0.95rem;
            text-align: center;
        }

        #filtroDesde, #filtroHasta {
            cursor: pointer;
        }
        #filtroDesde:hover, #filtroHasta:hover {
            border-color: var(--borde-campo);
        }
        #filtroDesde:focus, #filtroHasta:focus {
            border-color: var(--utez-green);
            box-shadow: 0 0 0 0.15rem rgba(0, 139, 116, 0.25);
        }
    </style>
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-tutor.jsp" />

    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Reportes
        </div>

        <div class="row g-3 mb-2">
            <div class="col-md-4">
                <label for="filtroCuatrimestre" class="form-label fw-bold">Cuatrimestre</label>
                <select id="filtroCuatrimestre" class="form-select form-control-figma">
                    <option value="">Seleccione el cuatrimestre</option>
                    <c:forEach var="cuatri" items="${listaCuatrimestres}">
                        <option value="${cuatri.idCuatrimestre}">${cuatri.numero}°</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-4">
                <label for="filtroGrupo" class="form-label fw-bold">Grupo</label>
                <select id="filtroGrupo" class="form-select form-control-figma">
                    <option value="">Seleccione el grupo</option>
                    <c:forEach var="letra" items="${listaLetrasGrupo}">
                        <option value="${letra.idLetra}">${letra.letra}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-4">
                <label for="filtroCarrera" class="form-label fw-bold">Carrera</label>
                <select id="filtroCarrera" class="form-select form-control-figma">
                    <option value="">Seleccione la carrera</option>
                    <c:forEach var="carrera" items="${listaCarreras}">
                        <option value="${carrera.idCarrera}">${carrera.nombre}</option>
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
                    <div class="rounded-circle d-flex justify-content-center align-items-center"
                         style="width:44px; height:44px; background-color:#008B74;">
                        <i class="bi bi-person-check text-white fs-5"></i>
                    </div>
                    <div>
                        <div class="text-muted small">Alumnos Atendidos</div>
                        <div class="fw-bold fs-4" id="kpiAtendidos">--</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="p-3 bg-white rounded-figma shadow-sm border d-flex align-items-center gap-3">
                    <div class="rounded-circle d-flex justify-content-center align-items-center"
                         style="width:44px; height:44px; background-color:#008B74;">
                        <i class="bi bi-signpost-split text-white fs-5"></i>
                    </div>
                    <div>
                        <div class="text-muted small">Canalizaciones</div>
                        <div class="fw-bold fs-4" id="kpiCanalizados">--</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="p-3 bg-white rounded-figma shadow-sm border d-flex align-items-center gap-3">
                    <div class="rounded-circle d-flex justify-content-center align-items-center"
                         style="width:44px; height:44px; background-color:#008B74;">
                        <i class="bi bi-calendar2-check text-white fs-5"></i>
                    </div>
                    <div>
                        <div class="text-muted small">Grupos Atendidos</div>
                        <div class="fw-bold fs-4" id="kpiGruposAtendidos">--</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="p-3 bg-white rounded-figma shadow-sm border d-flex align-items-center gap-3">
                    <div class="rounded-circle d-flex justify-content-center align-items-center"
                         style="width:44px; height:44px; background-color:#008B74;">
                        <i class="bi bi-check-circle text-white fs-5"></i>
                    </div>
                    <div>
                        <div class="text-muted small">Asistencias</div>
                        <div class="fw-bold fs-4" id="kpiAsistencias">--</div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row g-3">
            <div class="col-md-6">
                <div class="p-3 bg-white rounded-figma shadow-sm border card-grafica">
                    <div class="fw-bold mb-2">Distribución de Alumnos Canalizados</div>
                    <div class="grafica-contenido">
                        <canvas id="graficaPastel"></canvas>
                        <div id="pastelVacio" class="grafica-vacio d-none">
                            No hay canalizaciones registradas en este periodo.
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="p-3 bg-white rounded-figma shadow-sm border card-grafica">
                    <div class="fw-bold mb-2">Estado de Reportes (General)</div>
                    <div class="grafica-contenido">
                        <canvas id="graficaBarras"></canvas>
                    </div>
                </div>
            </div>
        </div>

        <div class="d-flex justify-content-end mt-4">
            <button type="button" id="btnExportar" class="btn btn-figma">
                <i class="bi bi-download"></i> Exportar PDF / Excel
            </button>
        </div>

    </div>

</div>

<!-- Inclusión de los modales unificados -->
<jsp:include page="../includes/alertas.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/tutor/reportes.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script>
    let graficaBarrasTutor = null;

    function pintarBarrasTutor(data) {
        const ctx = document.getElementById('graficaBarras');

        if (graficaBarrasTutor) graficaBarrasTutor.destroy();
        graficaBarrasTutor = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Atendidos', 'Canalizaciones', 'Asistencias'],
                datasets: [{
                    data: [data.totalAtendidos, data.totalCanalizados, data.totalAsistencias],
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

    function actualizarEstadoPastel(data) {
        const canvas = document.getElementById('graficaPastel');
        const aviso = document.getElementById('pastelVacio');
        const distribucion = data.distribucionCanalizados || [];

        if (distribucion.length === 0) {
            canvas.classList.add('d-none');
            aviso.classList.remove('d-none');
        } else {
            canvas.classList.remove('d-none');
            aviso.classList.add('d-none');
        }
    }

    function buscarReporteTutor() {
        cargarReporte({
            contextPath: '${pageContext.request.contextPath}',
            filtros: ['filtroCarrera', 'filtroCuatrimestre', 'filtroGrupo', 'filtroDesde', 'filtroHasta'],
            onDatos: function (data) {
                pintarBarrasTutor(data);
                actualizarEstadoPastel(data);
            }
        });
    }

    document.getElementById('btnBuscar').addEventListener('click', buscarReporteTutor);
    document.addEventListener('DOMContentLoaded', buscarReporteTutor);

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

    function construirUrlExport() {
        const params = new URLSearchParams();

        const selectCarrera = document.getElementById('filtroCarrera');
        const selectCuatrimestre = document.getElementById('filtroCuatrimestre');
        const selectGrupo = document.getElementById('filtroGrupo');
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
        if (desde) params.append('desde', desde);
        if (hasta) params.append('hasta', hasta);
        params.append('formato', 'csv');

        return '${pageContext.request.contextPath}/ReportesServlet?' + params.toString();
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

    document.getElementById('btnExportar').addEventListener('click', function () {
        mostrarConfirmacion(
            'advertencia',
            '¿Deseas descargar?',
            'Estás a punto de exportar el reporte en formato CSV.',
            'Sí, descargar',
            () => {
                descargarReporteCsv();
            }
        );
    });
</script>
</body>
</html>