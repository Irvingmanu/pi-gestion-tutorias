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
    <title>Sistema de Gestión de Tutorías - Reportes</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/alertas.css" rel="stylesheet">
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

    <jsp:include page="/includes/navbar-tutor.jsp" />

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

<div class="modal fade" id="modalAlerta" tabindex="-1" aria-labelledby="alertaTitulo" aria-hidden="true" data-bs-backdrop="static">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content alerta-card">
            <div class="modal-body alerta-body">
                <div class="alerta-icono-wrap">
                    <div class="alerta-icono" id="alertaIconoCirculo">
                        <img id="alertaIcono" src="" alt="" data-base-path="<%= request.getContextPath() %>/assets/img/alertas/">
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
                        <img id="confirmacionIcono" src="" alt="" data-base-path="<%= request.getContextPath() %>/assets/img/alertas/">
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
                <img id="toastIcono" src="" alt="" data-base-path="<%= request.getContextPath() %>/assets/img/alertas/">
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

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/coordinador/reportes.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/alertas.js"></script>
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
            contextPath: '<%= request.getContextPath() %>',
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

        return '<%= request.getContextPath() %>/ReportesServlet?' + params.toString();
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