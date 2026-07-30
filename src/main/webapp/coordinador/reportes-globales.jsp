<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Carrera" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Cuatrimestre" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.LetraGrupo" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO" %>
<%
    request.setAttribute("paginaActiva", "reportes");

    AlumnoDAO alumnoDAO = new AlumnoDAO();
    List<Carrera> listaCarreras = alumnoDAO.getAllCarreras();
    List<Cuatrimestre> listaCuatrimestres = alumnoDAO.getAllCuatrimestres();
    List<LetraGrupo> listaLetrasGrupo = alumnoDAO.getAllLetrasGrupo();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Reportes Globales</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.4/chart.umd.min.js"></script>
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL (Coordinador) ==================== -->
    <jsp:include page="../includes/navbar-coordinador.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Reportes Globales
        </div>

        <!-- ---- Filtros ---- -->
        <div class="row g-3 mb-2">
            <div class="col-md-4">
                <label for="filtroCarrera" class="form-label fw-bold">Carrera</label>
                <select id="filtroCarrera" class="form-select form-control-figma">
                    <option value="">Seleccione la carrera</option>
                    <% for (Carrera c : listaCarreras) { %>
                    <option value="<%= c.getIdCarrera() %>"><%= c.getNombre() %></option>
                    <% } %>
                </select>
            </div>
            <div class="col-md-4">
                <label for="filtroCuatrimestre" class="form-label fw-bold">Cuatrimestre</label>
                <select id="filtroCuatrimestre" class="form-select form-control-figma">
                    <option value="">Seleccione el cuatrimestre</option>
                    <% for (Cuatrimestre c : listaCuatrimestres) { %>
                    <option value="<%= c.getIdCuatrimestre() %>"><%= c.getNumero() %>°</option>
                    <% } %>
                </select>
            </div>
            <div class="col-md-4">
                <label for="filtroGrupo" class="form-label fw-bold">Grupo</label>
                <select id="filtroGrupo" class="form-select form-control-figma">
                    <option value="">Seleccione el grupo</option>
                    <% for (LetraGrupo l : listaLetrasGrupo) { %>
                    <option value="<%= l.getIdLetra() %>"><%= l.getLetra() %></option>
                    <% } %>
                </select>
            </div>
        </div>

        <!-- ---- Rango de fechas: sin esto el reporte mezcla todo el histórico ---- -->
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

        <!-- ---- KPIs ---- -->
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

        <!-- ---- Gráficas ---- -->
        <div class="row g-3">
            <div class="col-md-6">
                <div class="p-3 bg-white rounded-figma shadow-sm border">
                    <div class="fw-bold mb-2">Distribución de Alumnos Canalizados</div>
                    <canvas id="graficaPastel" height="220"></canvas>
                </div>
            </div>
            <div class="col-md-6">
                <div class="p-3 bg-white rounded-figma shadow-sm border">
                    <div class="fw-bold mb-2">Estado de Solicitudes de Asesoría (General)</div>
                    <canvas id="graficaBarras" height="220"></canvas>
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

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/coordinador/reportes.js"></script>
<script>
    let graficaBarrasCoordinador = null;

    // Esta gráfica sí es propia de la vista del coordinador (Pendientes /
    // Atendidas / Canalizadas), por eso vive aquí y no en reportes.js.
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
                plugins: { legend: { display: false } },
                scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
            }
        });
    }

    function buscarReporteCoordinador() {
        cargarReporte({
            contextPath: '<%= request.getContextPath() %>',
            filtros: ['filtroCarrera', 'filtroCuatrimestre', 'filtroGrupo', 'filtroDesde', 'filtroHasta'],
            onDatos: pintarBarrasCoordinador
        });
    }

    document.getElementById('btnBuscar').addEventListener('click', buscarReporteCoordinador);
    document.addEventListener('DOMContentLoaded', buscarReporteCoordinador);
</script>
</body>
</html>
