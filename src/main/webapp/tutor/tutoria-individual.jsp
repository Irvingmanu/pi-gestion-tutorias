<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Area" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.SesionIndividual" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    request.setAttribute("paginaActiva", "individual");

    List<SesionIndividual> sesionesProgramadas = (List<SesionIndividual>) request.getAttribute("sesionesProgramadas");
    List<Area> areasConMotivos = (List<Area>) request.getAttribute("areasConMotivos");
    Map<String, String> nombresAlumnos = (Map<String, String>) request.getAttribute("nombresAlumnos");
    String mensajeError = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Tutoría Individual</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/alertas.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-tutor.jsp" />

    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Tutoría Individual
        </div>

        <ul class="nav nav-tabs mb-4" id="tutoriaTabs" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active" id="tab-programadas-btn" data-bs-toggle="tab"
                        data-bs-target="#tab-programadas" type="button" role="tab" aria-selected="true">
                    Sesiones Programadas
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" id="tab-espontanea-btn" data-bs-toggle="tab"
                        data-bs-target="#tab-espontanea" type="button" role="tab" aria-selected="false">
                    Tutoría Espontánea
                </button>
            </li>
        </ul>

        <div class="tab-content" id="tutoriaTabsContent">

            <!-- ==================== TAB 1: SESIONES PROGRAMADAS ==================== -->
            <div class="tab-pane fade show active" id="tab-programadas" role="tabpanel">

                <div class="table-responsive">
                    <% if (sesionesProgramadas == null || sesionesProgramadas.isEmpty()) { %>
                    <div class="alert alert-info text-center">
                        No tienes sesiones programadas pendientes de completar.
                    </div>
                    <% } else { %>
                    <table class="tabla-grupos fs-6">
                        <thead>
                        <tr>
                            <th>Matrícula</th>
                            <th>Alumno</th>
                            <th>Fecha</th>
                            <th>Hora</th>
                            <th>Acciones</th>
                        </tr>
                        </thead>
                        <tbody>
                        <% for (SesionIndividual sesion : sesionesProgramadas) {
                            String nombreAlumno = nombresAlumnos != null ? nombresAlumnos.get(sesion.getMatricula()) : sesion.getMatricula();
                        %>
                        <tr>
                            <td><%= sesion.getMatricula() %></td>
                            <td><%= nombreAlumno %></td>
                            <td><%= sesion.getFecha() %></td>
                            <td><%= sesion.getHora() %></td>
                            <td>
                                <button type="button" class="btn-figma btn-completar-sesion"
                                        data-id-sesion="<%= sesion.getIdSesionIndividual() %>"
                                        data-matricula="<%= sesion.getMatricula() %>"
                                        data-alumno="<%= nombreAlumno %>"
                                        data-fecha="<%= sesion.getFecha() %>"
                                        data-hora="<%= sesion.getHora() %>">
                                    Completar
                                </button>
                            </td>
                        </tr>
                        <% } %>
                        </tbody>
                    </table>
                    <% } %>
                </div>

            </div>

            <!-- ==================== TAB 2: TUTORÍA ESPONTÁNEA ==================== -->
            <div class="tab-pane fade" id="tab-espontanea" role="tabpanel">

                <div class="form-wrap-figma" style="max-width: 920px;">
                    <form id="formTutoriaEspontanea" action="<%= request.getContextPath() %>/SesionIndividualServlet" method="post">

                        <div class="row g-3 mb-4">
                            <div class="col-12">
                                <label for="matricula" class="form-label fs-6 fw-bold">Matrícula</label>
                                <input type="text" id="matricula" name="matricula" class="form-control form-control-figma fs-6"
                                       placeholder="Escribe la matrícula del alumno"
                                       maxlength="10" minlength="10" pattern="^[a-zA-Z0-9]{10}$"
                                       title="La matrícula debe tener exactamente 10 caracteres, solo letras y números."
                                       oninput="this.value = this.value.replace(/[^a-zA-Z0-9]/g, '')" required>
                            </div>
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="fecha" class="form-label fs-6 fw-bold">Fecha</label>
                                <input type="date" id="fecha" name="fecha" class="form-control form-control-figma fs-6" required>
                            </div>
                            <div class="col-md-6">
                                <label for="hora" class="form-label fs-6 fw-bold">Hora</label>
                                <input type="time" id="hora" name="hora" class="form-control form-control-figma fs-6" required>
                            </div>
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="temasTratados" class="form-label fs-6 fw-bold">Temas Tratados</label>
                                <textarea id="temasTratados" name="temasTratados" class="form-control form-control-figma fs-6"
                                          rows="3" placeholder="Describe los temas tratados en la sesión" required></textarea>
                            </div>
                            <div class="col-md-6">
                                <label for="acuerdos" class="form-label fs-6 fw-bold">Acuerdos</label>
                                <textarea id="acuerdos" name="acuerdos" class="form-control form-control-figma fs-6"
                                          rows="3" placeholder="Describe los acuerdos alcanzados" required></textarea>
                            </div>
                        </div>

                        <p class="fs-5 fw-bold text-center my-4">Vínculo Directo</p>

                        <div class="row g-3 mb-4">
                            <c:forEach var="area" items="${areasConMotivos}">
                            <div class="col-md-6">
                                <label for="motivo-espontanea-${area.idArea}" class="form-label fs-6 fw-bold">${area.nombre}</label>
                                <select id="motivo-espontanea-${area.idArea}" name="idMotivo" class="form-select form-control-figma fs-6">
                                    <option value="">Ninguno</option>
                                    <c:forEach var="motivo" items="${area.motivos}">
                                    <option value="${motivo.idMotivo}">${motivo.nombreMotivo}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            </c:forEach>
                        </div>

                        <div class="d-flex justify-content-end mt-4">
                            <button type="submit" class="btn-figma fw-medium fs-5 px-4 py-2">Guardar</button>
                        </div>

                    </form>
                </div>

            </div>

        </div>

    </div>

</div>

<!-- ==================== MODAL: COMPLETAR SESIÓN PROGRAMADA ==================== -->
<div class="modal fade" id="modalCompletarSesion" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content">
            <form id="formCompletarSesion" action="<%= request.getContextPath() %>/SesionIndividualServlet" method="post">
                <input type="hidden" name="idSesion" id="modalIdSesion">

                <div class="modal-header">
                    <h5 class="modal-title">Completar sesión — <span id="modalAlumnoInfo"></span></h5>
                    <button type="button" class="btn-close" id="btnCerrarModalCompletar" aria-label="Cerrar"></button>
                </div>

                <div class="modal-body">
                    <div class="mb-3">
                        <label for="modalTemasTratados" class="form-label fs-6 fw-bold">Temas Tratados</label>
                        <textarea id="modalTemasTratados" name="temasTratados" class="form-control form-control-figma w-100 fs-6"
                                  rows="3" placeholder="Describe los temas tratados en la sesión" required></textarea>
                    </div>
                    <div class="mb-3">
                        <label for="modalAcuerdos" class="form-label fs-6 fw-bold">Acuerdos</label>
                        <textarea id="modalAcuerdos" name="acuerdos" class="form-control form-control-figma w-100 fs-6"
                                  rows="3" placeholder="Describe los acuerdos alcanzados" required></textarea>
                    </div>

                    <p class="fs-6 fw-bold text-center my-3">Vínculo Directo</p>

                    <div class="row g-3">
                        <c:forEach var="area" items="${areasConMotivos}">
                        <div class="col-md-6">
                            <label for="motivo-modal-${area.idArea}" class="form-label fs-6 fw-bold">${area.nombre}</label>
                            <select id="motivo-modal-${area.idArea}" name="idMotivo" class="form-select form-control-figma w-100 fs-6">
                                <option value="">Ninguno</option>
                                <c:forEach var="motivo" items="${area.motivos}">
                                <option value="${motivo.idMotivo}">${motivo.nombreMotivo}</option>
                                </c:forEach>
                            </select>
                        </div>
                        </c:forEach>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" id="btnCancelarModalCompletar" class="btn-cancelar-figma fw-medium px-4 py-2">Cancelar</button>
                    <button type="submit" class="btn-figma fw-medium px-4 py-2">Guardar</button>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="../includes/alertas.jsp" />

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/alertas.js"></script>

<% if (mensajeError != null) { %>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        mostrarAlerta('error', 'Error', '<%= mensajeError %>');
    });
</script>
<% } %>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        var modalEl = document.getElementById('modalCompletarSesion');
        var modal = new bootstrap.Modal(modalEl);

        document.querySelectorAll('.btn-completar-sesion').forEach(function (btn) {
            btn.addEventListener('click', function () {
                document.getElementById('modalIdSesion').value = btn.dataset.idSesion;
                document.getElementById('modalAlumnoInfo').textContent =
                    btn.dataset.alumno + ' (' + btn.dataset.matricula + ') - ' + btn.dataset.fecha + ' ' + btn.dataset.hora;
                modal.show();
            });
        });

        function confirmarCierreModalCompletar() {
            mostrarConfirmacion(
                'critica',
                '¿Estás seguro de salir?',
                'Se perderán los datos que no hayas guardado.',
                'Sí, salir',
                function () {
                    bootstrap.Modal.getInstance(modalEl).hide();
                }
            );
        }

        document.getElementById('btnCerrarModalCompletar').addEventListener('click', confirmarCierreModalCompletar);
        document.getElementById('btnCancelarModalCompletar').addEventListener('click', confirmarCierreModalCompletar);

        var formCompletarSesion = document.getElementById('formCompletarSesion');
        formCompletarSesion.addEventListener('submit', function (e) {
            e.preventDefault();

            if (!formCompletarSesion.checkValidity()) {
                formCompletarSesion.reportValidity();
                return;
            }

            mostrarConfirmacion(
                'advertencia',
                '¿Completar sesión?',
                'Estás a punto de registrar los temas, acuerdos y canalizaciones.',
                'Sí, guardar',
                function () {
                    formCompletarSesion.submit();
                }
            );
        });

        var formTutoriaEspontanea = document.getElementById('formTutoriaEspontanea');
        formTutoriaEspontanea.addEventListener('submit', function (e) {
            e.preventDefault();

            if (!formTutoriaEspontanea.checkValidity()) {
                formTutoriaEspontanea.reportValidity();
                return;
            }

            mostrarConfirmacion(
                'advertencia',
                '¿Registrar tutoría?',
                'Estás a punto de registrar la tutoría espontánea con los datos ingresados.',
                'Sí, guardar',
                function () {
                    formTutoriaEspontanea.submit();
                }
            );
        });

        var parametros = new URLSearchParams(window.location.search);
        var exito = parametros.get('exito');
        var errorUrl = parametros.get('error');

        if (exito === 'completada') {
            mostrarToast('exito', '¡Éxito!', 'La sesión fue completada correctamente');
        } else if (exito === 'tutoria_guardada') {
            mostrarToast('exito', 'Guardado', 'Tutoría espontánea registrada correctamente.');
        } else if (errorUrl === 'matricula_invalida') {
            mostrarAlerta('advertencia', 'Formato incorrecto', 'La matrícula debe tener exactamente 10 caracteres.');
        } else if (errorUrl === 'matricula_no_existe') {
            mostrarAlerta('error', 'Matrícula no encontrada', 'El alumno no está registrado en el sistema. Verifica el dato.');
        }

        if (exito || errorUrl) {
            window.history.replaceState(null, null, window.location.pathname);
        }
    });
</script>
</body>
</html>
