<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
    request.setAttribute("paginaActiva", "reportes");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Reportes Globales</title>
    <link href="../assets/css/bootstrap.css" rel="stylesheet">
    <link href="../assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="../assets/css/coordinador/reportes.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar.jsp" />

    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-3">
            Reportes Globales
        </div>

        <div class="row g-3 mb-3">
            <div class="col-md-6">
                <label class="campo-label fs-6" for="carrera">Carrera</label>
                <select id="carrera" class="form-select campo-select">
                    <option selected>Seleccione la carrera</option>
                    <option>Desarrollo de Software Multiplataforma</option>
                    <option>Mantenimiento Industrial</option>
                    <option>Tecnologías de la Información</option>
                </select>
            </div>
            <div class="col-md-6">
                <label class="campo-label fs-6" for="cuatrimestre">Cuatrimestre</label>
                <select id="cuatrimestre" class="form-select campo-select">
                    <option selected>Seleccione el cuatrimestre</option>
                    <option>1°</option>
                    <option>2°</option>
                    <option>3°</option>
                </select>
            </div>
        </div>

        <div class="row g-3 mb-4 align-items-end">
            <div class="col-md-6">
                <label class="campo-label fs-6" for="grupo">Grupo</label>
                <select id="grupo" class="form-select campo-select">
                    <option selected>Seleccione el Grupo</option>
                    <option>A</option>
                    <option>B</option>
                    <option>C</option>
                </select>
            </div>
            <div class="col-md-6 text-end">
                <button type="button" class="btn-figma">Buscar</button>
            </div>
        </div>

        <div class="row g-3 mb-4">
            <div class="col-md-6 d-flex align-items-center gap-2">
                <div class="stat-icon">
                    <img src="../assets/img/coordinador/atendidos.png" alt="">
                </div>
                <div>
                    <p class="fs-6 fw-medium mb-0">Alumnos Atendidos</p>
                    <p class="fs-6 mb-0">98</p>
                </div>
            </div>
            <div class="col-md-6 d-flex align-items-center gap-2">
                <div class="stat-icon">
                    <img src="../assets/img/coordinador/canalizados.png" alt="">
                </div>
                <div>
                    <p class="fs-6 fw-medium mb-0">Canalizados</p>
                    <p class="fs-6 mb-0">75</p>
                </div>
            </div>
            <div class="col-md-6 d-flex align-items-center gap-2">
                <div class="stat-icon">
                    <img src="../assets/img/coordinador/pidieronTutoria.png" alt="">
                </div>
                <div>
                    <p class="fs-6 fw-medium mb-0">Pidieron Tutorías</p>
                    <p class="fs-6 mb-0">23</p>
                </div>
            </div>
            <div class="col-md-6 d-flex align-items-center gap-2">
                <div class="stat-icon">
                    <img src="../assets/img/coordinador/pendientes.png" alt="">
                </div>
                <div>
                    <p class="fs-6 fw-medium mb-0">Pendientes</p>
                    <p class="fs-6 mb-0">101</p>
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
                            <span class="fs-6">Servicio Médico <span id="legendServicioMedico"></span>%</span>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <span class="legend-dot" style="background-color:#A0CBF3;"></span>
                            <span class="fs-6">Psicología <span id="legendPsicologia"></span>%</span>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <span class="legend-dot" style="background-color:#003351;"></span>
                            <span class="fs-6">Becas <span id="legendBecas"></span>%</span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-6 d-flex flex-column">
                <p class="grafica-titulo fs-5 mb-3">Estado de Solicitudes de Asesoría (General)</p>
                <div class="grafica-barras-wrap mb-3">
                    <canvas id="graficaBarras"></canvas>
                </div>
                <div class="mt-auto d-flex justify-content-end">
                    <a href="https://docs.google.com/spreadsheets/d/1zv0IH2VBxcrhwLzWJxy9tqAwfscXnNtD2ct0eivN3Gc/edit?hl=es&gid=0#gid=0"
                       target="_blank" rel="noopener" class="btn btn-exportar fw-medium">
                        <img src="../assets/css/bi/download.svg" alt="">
                        Exportar PDF / Excel
                    </a>
                </div>
            </div>

        </div>

    </div>

</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>
<script src="../assets/js/bootstrap.js"></script>
<script>
    /* ==========================================================================
       DATOS DE LAS GRAFICAS
       Declarados como variables independientes para que, mas adelante, sea
       trivial sustituir cada numero por un valor dinamico impreso desde JSP
       (ej. var pctServicioMedico = <%= "${reporte.pctServicioMedico}" %>;)
       ========================================================================== */

    // --- Dona: "Distribución de Alumnos Canalizados" ---
    // Porcentajes reales tomados del diseño de Figma (nodo 858:826)
    var pctServicioMedico = 55;
    var pctPsicologia = 25;
    var pctBecas = 20;

    var coloresDona = {
        servicioMedico: '#008B74',
        psicologia: '#A0CBF3',
        becas: '#003351'
    };

    document.getElementById('legendServicioMedico').textContent = pctServicioMedico;
    document.getElementById('legendPsicologia').textContent = pctPsicologia;
    document.getElementById('legendBecas').textContent = pctBecas;

    // --- Barras: "Estado de Solicitudes de Asesoría (General)" ---
    // El diseño de Figma no trae valores numéricos para esta grafica (solo
    // proporciones visuales de las barras), asi que estos son valores de
    // muestra a reemplazar por los reales del backend (ej. desde un bean
    // "reporte" expuesto por el servlet: ${reporte.totalPendientes}, etc.)
    var totalPendientes = 45;
    var totalAtendidas = 35;
    var totalCanalizadas = 10;

    var coloresBarras = {
        pendientes: '#3F9F9C',
        atendidas: '#647DAB',
        canalizadas: '#699AB7'
    };

    new Chart(document.getElementById('graficaDona'), {
        type: 'doughnut',
        data: {
            labels: ['Servicio Médico', 'Psicología', 'Becas'],
            datasets: [{
                data: [pctServicioMedico, pctPsicologia, pctBecas],
                backgroundColor: [coloresDona.servicioMedico, coloresDona.psicologia, coloresDona.becas],
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
            labels: ['Pendientes', 'Atendidas', 'Canalizadas'],
            datasets: [{
                data: [totalPendientes, totalAtendidas, totalCanalizadas],
                backgroundColor: [coloresBarras.pendientes, coloresBarras.atendidas, coloresBarras.canalizadas],
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
