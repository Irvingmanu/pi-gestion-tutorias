<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
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

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL (Tutor) ==================== -->
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

        <div class="banner-grupos h5 mb-3">
            Reportes
        </div>

        <!-- Filtros: Cuatrimestre / Grupo -->
        <div class="row g-3 mb-3">
            <div class="col-md-6">
                <label class="campo-label fs-6" for="cuatrimestre">Cuatrimestre</label>
                <select id="cuatrimestre" class="form-select campo-select">
                    <option selected>Seleccione el cuatrimestre</option>
                    <option>1°</option>
                    <option>2°</option>
                    <option>3°</option>
                </select>
            </div>
            <div class="col-md-6">
                <label class="campo-label fs-6" for="grupo">Grupo</label>
                <select id="grupo" class="form-select campo-select">
                    <option selected>Seleccione el Grupo</option>
                    <option>A</option>
                    <option>B</option>
                    <option>C</option>
                </select>
            </div>
        </div>

        <!-- Filtros: Carrera / Buscar -->
        <div class="row g-3 mb-4 align-items-end">
            <div class="col-md-6">
                <label class="campo-label fs-6" for="carrera">Carrera</label>
                <select id="carrera" class="form-select campo-select">
                    <option selected>Seleccione la carrera</option>
                    <option>Desarrollo de Software Multiplataforma</option>
                    <option>Mantenimiento Industrial</option>
                    <option>Tecnologías de la Información</option>
                </select>
            </div>
            <div class="col-md-6 text-end">
                <button type="button" class="btn-figma">Buscar</button>
            </div>
        </div>

        <!-- Tarjetas de estadisticas -->
        <div class="row g-3 mb-4">
            <div class="col-md-6 d-flex align-items-center gap-2">
                <div class="stat-icon">
                    <img src="<%= request.getContextPath() %>/assets/img/coordinador/atendidos.png" alt="">
                </div>
                <div>
                    <p class="fs-6 fw-medium mb-0">Alumnos Atendidos</p>
                    <p class="fs-6 mb-0">98</p>
                </div>
            </div>
            <div class="col-md-6 d-flex align-items-center gap-2">
                <div class="stat-icon">
                    <img src="<%= request.getContextPath() %>/assets/img/coordinador/canalizados.png" alt="">
                </div>
                <div>
                    <p class="fs-6 fw-medium mb-0">Canalizaciones</p>
                    <p class="fs-6 mb-0">75</p>
                </div>
            </div>
            <div class="col-md-6 d-flex align-items-center gap-2">
                <div class="stat-icon">
                    <img src="<%= request.getContextPath() %>/assets/img/coordinador/pidieronTutoria.png" alt="">
                </div>
                <div>
                    <p class="fs-6 fw-medium mb-0">Grupos Atendidos</p>
                    <p class="fs-6 mb-0">23</p>
                </div>
            </div>
            <div class="col-md-6 d-flex align-items-center gap-2">
                <div class="stat-icon">
                    <img src="<%= request.getContextPath() %>/assets/img/coordinador/pendientes.png" alt="">
                </div>
                <div>
                    <p class="fs-6 fw-medium mb-0">Asistencias</p>
                    <p class="fs-6 mb-0">101</p>
                </div>
            </div>
        </div>

        <!-- Graficas -->
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
                    <a href="#" class="btn btn-exportar fw-medium">
                        <img src="<%= request.getContextPath() %>/assets/css/bi/download.svg" alt="">
                        Exportar PDF / Excel
                    </a>
                </div>
            </div>

        </div>

    </div>

</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
<script>
    /* ==========================================================================
       DATOS DE LAS GRAFICAS
       Declarados como variables independientes para que, mas adelante, sea
       trivial sustituir cada numero por un valor dinamico impreso desde JSP.
       ========================================================================== */

    // --- Dona: "Distribución de Alumnos Canalizados" ---
    var pctAtendidos = 55;
    var pctCanalizaciones = 25;
    var pctAsistencias = 20;

    var coloresDona = {
        atendidos: '#008B74',
        canalizaciones: '#A0CBF3',
        asistencias: '#003351'
    };

    document.getElementById('legendAtendidos').textContent = pctAtendidos;
    document.getElementById('legendCanalizaciones').textContent = pctCanalizaciones;
    document.getElementById('legendAsistencias').textContent = pctAsistencias;

    // --- Barras: "Estado de Reportes (General)" ---
    // Mismos valores mostrados en las tarjetas de estadisticas de arriba.
    var totalAtendidos = 98;
    var totalCanalizaciones = 75;
    var totalAsistencias = 101;

    var coloresBarras = {
        atendidos: '#3F9F9C',
        canalizaciones: '#647DAB',
        asistencias: '#699AB7'
    };

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
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '65%',
            plugins: {
                legend: { display: false }
            }
        }
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
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: { beginAtZero: true },
                x: { grid: { display: false } }
            }
        }
    });
</script>
</body>
</html>
