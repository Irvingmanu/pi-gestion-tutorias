<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Carrera" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Cuatrimestre" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.LetraGrupo" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO" %>
<%
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
    <title>Sistema de Gestión de Tutorías - Reportes</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.4/chart.umd.min.js"></script>
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL (Tutor) ==================== -->
    <aside class="sidebar-grupos">
        <div class="sidebar-logo">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/logoUtez.png" alt="UTEZ">
        </div>

        <a href="<%= request.getContextPath() %>/tutoria-individual" class="nav-item-grupos">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/tutoriaIndividual.png" alt="Tutoría Individual">
            <span>Tutoría Individual</span>
        </a>
        <a href="<%= request.getContextPath() %>/tutoria-grupal" class="nav-item-grupos">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/tutoriaGrupal.png" alt="Tutoría Grupal">
            <span>Tutoría Grupal</span>
        </a>
        <a href="<%= request.getContextPath() %>/solicitudes" class="nav-item-grupos">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/solicitudes.png" alt="Solicitudes">
            <span>Solicitudes</span>
        </a>
        <a href="<%= request.getContextPath() %>/tutor/reportes.jsp" class="nav-item-grupos active">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/reportes.png" alt="Reportes">
            <span>Reportes</span>
        </a>
        <a href="<%= request.getContextPath() %>/tutor/perfil.jsp" class="nav-item-grupos mt-auto">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/perfil.png" alt="Perfil">
            <span>Perfil</span>
        </a>
    </aside>

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Reportes
        </div>

        <!-- ---- Filtros ---- -->
        <div class="row g-3 mb-2">
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
            <div class="col-md-4">
                <label for="filtroCarrera" class="form-label fw-bold">Carrera</label>
                <select id="filtroCarrera" class="form-select form-control-figma">
                    <option value="">Seleccione la carrera</option>
                    <% for (Carrera c : listaCarreras) { %>
                    <option value="<%= c.getIdCarrera() %>"><%= c.getNombre() %></option>
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
                    <div class="fw-bold mb-2">Estado de Reportes (General)</div>
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
    let graficaBarrasTutor = null;

    // Gráfica propia de la vista del tutor (Atendidos / Canalizaciones /
    // Asistencias), distinta a la del coordinador a propósito.
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
                plugins: { legend: { display: false } },
                scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
            }
        });
    }

    function buscarReporteTutor() {
        cargarReporte({
            contextPath: '<%= request.getContextPath() %>',
            filtros: ['filtroCarrera', 'filtroCuatrimestre', 'filtroGrupo', 'filtroDesde', 'filtroHasta'],
            onDatos: pintarBarrasTutor
        });
    }

    document.getElementById('btnBuscar').addEventListener('click', buscarReporteTutor);
    document.addEventListener('DOMContentLoaded', buscarReporteTutor);
</script>
</body>
</html>
