<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Carrera" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Cuatrimestre" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.LetraGrupo" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.ReporteTutor" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Reportes</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/reportes.css" rel="stylesheet">
</head>
<body>

<%
    List<Carrera> carreras = (List<Carrera>) request.getAttribute("carreras");
    List<Cuatrimestre> cuatrimestres = (List<Cuatrimestre>) request.getAttribute("cuatrimestres");
    List<LetraGrupo> letrasGrupo = (List<LetraGrupo>) request.getAttribute("letrasGrupo");
    ReporteTutor reporte = (ReporteTutor) request.getAttribute("reporte");

    if (carreras == null) carreras = Collections.emptyList();
    if (cuatrimestres == null) cuatrimestres = Collections.emptyList();
    if (letrasGrupo == null) letrasGrupo = Collections.emptyList();

    Integer filtroCuatrimestre = (Integer) request.getAttribute("filtroCuatrimestre");
    Integer filtroGrupo = (Integer) request.getAttribute("filtroGrupo");
    Integer filtroCarrera = (Integer) request.getAttribute("filtroCarrera");

    int alumnosAtendidos = reporte != null ? reporte.getAlumnosAtendidos() : 0;
    int canalizaciones = reporte != null ? reporte.getCanalizaciones() : 0;
    int gruposAtendidos = reporte != null ? reporte.getGruposAtendidos() : 0;
    int asistencias = reporte != null ? reporte.getAsistencias() : 0;
    int pctAtendidos = reporte != null ? reporte.getPctAtendidos() : 0;
    int pctCanalizaciones = reporte != null ? reporte.getPctCanalizaciones() : 0;
    int pctAsistencias = reporte != null ? reporte.getPctAsistencias() : 0;
%>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <aside class="sidebar-grupos">
        <div class="sidebar-logo">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/logoUtez.png" alt="UTEZ">
        </div>
        <a href="<%= request.getContextPath() %>/tutor/registro-individual.jsp" class="nav-item-grupos">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/tutoriaIndividual.png" alt="Tutoría Individual">
            <span>Tutoría Individual</span>
        </a>
        <a href="<%= request.getContextPath() %>/tutor/registro-grupal.jsp" class="nav-item-grupos">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/tutoriaGrupal.png" alt="Tutoría Grupal">
            <span>Tutoría Grupal</span>
        </a>
        <a href="<%= request.getContextPath() %>/tutor/solicitudes.jsp" class="nav-item-grupos">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/solicitudes.png" alt="Solicitudes">
            <span>Solicitudes</span>
        </a>
        <a href="<%= request.getContextPath() %>/ReportesServlet" class="nav-item-grupos active">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/reportes.png" alt="Reportes">
            <span>Reportes</span>
        </a>
        <a href="<%= request.getContextPath() %>/tutor/perfil.jsp" class="nav-item-grupos mt-auto">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/perfil.png" alt="Perfil">
            <span>Perfil</span>
        </a>
    </aside>

    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-3">
            Reportes
        </div>

        <form id="formFiltros" action="<%= request.getContextPath() %>/ReportesServlet" method="get">

            <div class="row g-3 mb-3">
                <div class="col-md-6">
                    <label class="campo-label fs-6" for="cuatrimestre">Cuatrimestre</label>
                    <select id="cuatrimestre" name="cuatrimestre" class="form-select campo-select">
                        <option value="">Seleccione el cuatrimestre</option>
                        <% for (Cuatrimestre c : cuatrimestres) { %>
                        <option value="<%= c.getIdCuatrimestre() %>"
                                <%= (filtroCuatrimestre != null && filtroCuatrimestre == c.getIdCuatrimestre()) ? "selected" : "" %>>
                            <%= c.getNumero() %>°
                        </option>
                        <% } %>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="campo-label fs-6" for="grupo">Grupo</label>
                    <select id="grupo" name="grupo" class="form-select campo-select">
                        <option value="">Seleccione el Grupo</option>
                        <% for (LetraGrupo l : letrasGrupo) { %>
                        <option value="<%= l.getIdLetra() %>"
                                <%= (filtroGrupo != null && filtroGrupo == l.getIdLetra()) ? "selected" : "" %>>
                            <%= l.getLetra() %>
                        </option>
                        <% } %>
                    </select>
                </div>
            </div>

            <div class="row g-3 mb-4 align-items-end">
                <div class="col-md-6">
                    <label class="campo-label fs-6" for="carrera">Carrera</label>
                    <select id="carrera" name="carrera" class="form-select campo-select">
                        <option value="">Seleccione la carrera</option>
                        <% for (Carrera c : carreras) { %>
                        <option value="<%= c.getIdCarrera() %>"
                                <%= (filtroCarrera != null && filtroCarrera == c.getIdCarrera()) ? "selected" : "" %>>
                            <%= c.getNombre() %>
                        </option>
                        <% } %>
                    </select>
                </div>
                <div class="col-md-6 text-end">
                    <button type="submit" class="btn-figma">Buscar</button>
                </div>
            </div>
        </form>

        <div class="row g-3 mb-4">
            <div class="col-md-6 d-flex align-items-center gap-2">
                <div class="stat-icon">
                    <img src="<%= request.getContextPath() %>/assets/img/coordinador/atendidos.png" alt="">
                </div>
                <div>
                    <p class="fs-6 fw-medium mb-0">Alumnos Atendidos</p>
                    <p class="fs-6 mb-0"><%= alumnosAtendidos %></p>
                </div>
            </div>
            <div class="col-md-6 d-flex align-items-center gap-2">
                <div class="stat-icon">
                    <img src="<%= request.getContextPath() %>/assets/img/coordinador/canalizados.png" alt="">
                </div>
                <div>
                    <p class="fs-6 fw-medium mb-0">Canalizaciones</p>
                    <p class="fs-6 mb-0"><%= canalizaciones %></p>
                </div>
            </div>
            <div class="col-md-6 d-flex align-items-center gap-2">
                <div class="stat-icon">
                    <img src="<%= request.getContextPath() %>/assets/img/coordinador/pidieronTutoria.png" alt="">
                </div>
                <div>
                    <p class="fs-6 fw-medium mb-0">Grupos Atendidos</p>
                    <p class="fs-6 mb-0"><%= gruposAtendidos %></p>
                </div>
            </div>
            <div class="col-md-6 d-flex align-items-center gap-2">
                <div class="stat-icon">
                    <img src="<%= request.getContextPath() %>/assets/img/coordinador/pendientes.png" alt="">
                </div>
                <div>
                    <p class="fs-6 fw-medium mb-0">Asistencias</p>
                    <p class="fs-6 mb-0"><%= asistencias %></p>
                </div>
            </div>
        </div>

        <div class="row g-4">

            <div class="col-md-6">
                <p class="grafica-titulo fs-5 mb-3">Distribución de Alumnos Canalizados</p>
                <div class="d-flex align-items-center gap-4 flex-wrap">
                    <div class="grafica-dona-wrap">
                        <canvas id="graficaDona"></canvas>
                    </div>
                    <div class="d-flex flex-column gap-2">
                        <div class="d-flex align-items-center gap-2">
                            <span class="legend-dot" style="background-color:#008B74;"></span>
                            <span class="fs-6">Atendidos <span id="legendAtendidos"></span>%</span>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <span class="legend-dot" style="background-color:#A0CBF3;"></span>
                            <span class="fs-6">Canalizaciones <span id="legendCanalizaciones"></span>%</span>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <span class="legend-dot" style="background-color:#003351;"></span>
                            <span class="fs-6">Asistencias <span id="legendAsistencias"></span>%</span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-6 d-flex flex-column">
                <p class="grafica-titulo fs-5 mb-3">Estado de Reportes (General)</p>
                <div class="grafica-barras-wrap mb-3">
                    <canvas id="graficaBarras"></canvas>
                </div>
                <div class="mt-auto d-flex justify-content-end">
                    <button type="button" id="btnExportar" class="btn btn-exportar fw-medium">
                        <img src="<%= request.getContextPath() %>/assets/css/bi/download.svg" alt="">
                        Exportar Excel
                    </button>
                </div>
            </div>

        </div>

    </div>

</div>

<%@ include file="/includes/alertas.jsp" %>

<iframe name="iframeExportar" style="display:none;"></iframe>
<form id="formExportar" action="<%= request.getContextPath() %>/ReportesServlet" method="post" target="iframeExportar">
    <input type="hidden" name="cuatrimestre" value="<%= filtroCuatrimestre != null ? filtroCuatrimestre : "" %>">
    <input type="hidden" name="grupo" value="<%= filtroGrupo != null ? filtroGrupo : "" %>">
    <input type="hidden" name="carrera" value="<%= filtroCarrera != null ? filtroCarrera : "" %>">
</form>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/alertas.js"></script>
<script>
    var pctAtendidos = <%= pctAtendidos %>;
    var pctCanalizaciones = <%= pctCanalizaciones %>;
    var pctAsistencias = <%= pctAsistencias %>;

    var totalAtendidos = <%= alumnosAtendidos %>;
    var totalCanalizaciones = <%= canalizaciones %>;
    var totalAsistencias = <%= asistencias %>;

    var coloresDona = { atendidos: '#008B74', canalizaciones: '#A0CBF3', asistencias: '#003351' };
    var coloresBarras = { atendidos: '#3F9F9C', canalizaciones: '#647DAB', asistencias: '#699AB7' };

    document.getElementById('legendAtendidos').textContent = pctAtendidos;
    document.getElementById('legendCanalizaciones').textContent = pctCanalizaciones;
    document.getElementById('legendAsistencias').textContent = pctAsistencias;

    new Chart(document.getElementById('graficaDona'), {
        type: 'doughnut',
        data: {
            labels: ['Atendidos', 'Canalizaciones', 'Asistencias'],
            datasets: [{
                data: [pctAtendidos, pctCanalizaciones, pctAsistencias],
                backgroundColor: [coloresDona.atendidos, coloresDona.canalizaciones, coloresDona.asistencias],
                borderWidth: 0
            }]
        },
        options: { responsive: true, maintainAspectRatio: false, cutout: '65%', plugins: { legend: { display: false } } }
    });

    new Chart(document.getElementById('graficaBarras'), {
        type: 'bar',
        data: {
            labels: ['ATENDIDOS', 'CANALIZACIONES', 'ASISTENCIAS'],
            datasets: [{
                data: [totalAtendidos, totalCanalizaciones, totalAsistencias],
                backgroundColor: [coloresBarras.atendidos, coloresBarras.canalizaciones, coloresBarras.asistencias],
                borderRadius: 6,
                maxBarThickness: 64
            }]
        },
        options: {
            responsive: true, maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: true }, x: { grid: { display: false } } }
        }
    });

    document.getElementById('btnExportar').addEventListener('click', function () {
        mostrarConfirmacion(
            'advertencia',
            '¿Exportar reporte?',
            'Se descargará un archivo con los datos del reporte actual.',
            'Sí, exportar',
            () => {
                document.getElementById('formExportar').submit();
                mostrarToast('exito', 'Éxito', 'El reporte se está descargando');
            }
        );
    });
</script>
</body>
</html>