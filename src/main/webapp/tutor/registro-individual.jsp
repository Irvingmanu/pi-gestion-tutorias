<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Area" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Motivo" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.dao.AreaDAO" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Registro de Tutoría Individual</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
</head>
<body>

<%
    // Carga las áreas con sus motivos ya incluidos
    AreaDAO areaDAO = new AreaDAO();
    List<Area> areas = areaDAO.getAll();
    for (Area a : areas) {
        Area completa = areaDAO.getById(a.getIdArea());
        a.setMotivos(completa.getMotivos());
    }
    request.setAttribute("areas", areas);
%>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL (Tutor) ==================== -->
    <aside class="sidebar-grupos">
        <div class="sidebar-logo">
            <img src="<%= request.getContextPath() %>/assets/img/tutor/logoUtez.png" alt="UTEZ">
        </div>

        <a href="<%= request.getContextPath() %>/tutor/registro-individual.jsp" class="nav-item-grupos active">
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
        <a href="<%= request.getContextPath() %>/tutor/reportes.jsp" class="nav-item-grupos">
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
            Registro de Tutoría Individual
        </div>

        <div class="form-wrap-figma">
            <form id="formRegistroIndividual" action="<%= request.getContextPath() %>/TutoriaServlet" method="post">

                <input type="hidden" name="accion" value="registrarIndividual">
                <input type="hidden" id="idAreaCanalizacion" name="idAreaCanalizacion" value="">
                <input type="hidden" id="observacionesCanalizacion" name="observacionesCanalizacion" value="">

                <!-- Fila 1: Matricula / Fecha -->
                <div class="row g-3 mb-5">
                    <div class="col-md-6">
                        <label for="matricula" class="form-label fs-6 fw-bold">Matrícula</label>
                        <input type="text" id="matricula" name="matricula" class="form-control form-control-figma w-100 fs-6"
                               placeholder="Escribe la matrícula del alumno"
                               pattern="^[a-zA-Z0-9]+$" title="La matrícula solo debe contener letras y números, sin espacios ni símbolos."
                               oninput="this.value = this.value.replace(/[^a-zA-Z0-9]/g, '')" required>
                    </div>
                    <div class="col-md-6">
                        <label for="fecha" class="form-label fs-6 fw-bold">Fecha</label>
                        <input type="date" id="fecha" name="fecha" class="form-control form-control-figma w-100 fs-6" required>
                    </div>
                </div>

                <!-- Fila 2: Temas Tratados / Acuerdos -->
                <div class="row g-3 mb-5">
                    <div class="col-md-6">
                        <label for="temasTratados" class="form-label fs-6 fw-bold">Temas Tratados</label>
                        <textarea id="temasTratados" name="temasTratados" class="form-control form-control-figma w-100 fs-6"
                                  rows="3" placeholder="Describe los temas tratados en la sesión" required></textarea>
                    </div>
                    <div class="col-md-6">
                        <label for="acuerdos" class="form-label fs-6 fw-bold">Acuerdos</label>
                        <textarea id="acuerdos" name="acuerdos" class="form-control form-control-figma w-100 fs-6"
                                  rows="3" placeholder="Describe los acuerdos alcanzados" required></textarea>
                    </div>
                </div>

                <!-- Vínculo Directo (dinámico desde AREA_APOYO + MOTIVO_AREA) -->
                <p class="fs-5 fw-bold text-center my-4">Vínculo Directo</p>

                <div class="row g-3 mb-5">
                    <% for (Area area : areas) { %>
                    <div class="col-md-6">
                        <label for="area_<%= area.getIdArea() %>" class="form-label fs-6 fw-bold"><%= area.getNombre() %></label>
                        <select id="area_<%= area.getIdArea() %>" class="form-select form-control-figma select-canalizacion w-100 fs-6">
                            <option value="">Seleccione el motivo</option>
                            <% if (area.getMotivos() != null) {
                                for (Motivo m : area.getMotivos()) { %>
                            <option value="<%= m.getIdMotivo() %>" data-id-area="<%= area.getIdArea() %>">
                                <%= m.getNombreMotivo() %>
                            </option>
                            <% } } %>
                        </select>
                    </div>
                    <% } %>
                </div>

                <div class="d-flex justify-content-end mt-4">
                    <button type="submit" class="btn-figma fw-medium fs-5 px-4 py-2">Guardar</button>
                </div>

            </form>
        </div>

    </div>

</div>

<%@ include file="/includes/alertas.jsp" %>

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/alertas.js"></script>

<script>
    document.addEventListener('DOMContentLoaded', function () {

        // --- Lógica de "solo un área canalizable a la vez" ---
        const selects = document.querySelectorAll('.select-canalizacion');
        const inputIdArea = document.getElementById('idAreaCanalizacion');
        const inputObservaciones = document.getElementById('observacionesCanalizacion');

        selects.forEach(select => {
            select.addEventListener('change', function () {
                if (select.value !== '') {
                    selects.forEach(otro => {
                        if (otro !== select) otro.selectedIndex = 0;
                    });
                    const opcionSeleccionada = select.options[select.selectedIndex];
                    inputIdArea.value = opcionSeleccionada.dataset.idArea;
                    inputObservaciones.value = opcionSeleccionada.text;
                } else {
                    inputIdArea.value = '';
                    inputObservaciones.value = '';
                }
            });
        });

        // --- Validación del formulario usando el sistema de alertas propio ---
        const form = document.getElementById('formRegistroIndividual');
        form.addEventListener('submit', function (event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
                mostrarAlerta('advertencia', 'Faltan datos', 'Por favor completa todos los campos obligatorios.');
            }
        });

        // --- Mensajes que vienen del servlet después de un forward ---
        <% if (request.getAttribute("exito") != null) { %>
        mostrarAlerta('exito', 'Éxito', 'Se registró exitosamente');
        <% } %>
        <% if (request.getAttribute("error") != null) { %>
        mostrarAlerta('error', 'Error', '<%= request.getAttribute("error") %>');
        <% } %>
    });
</script>
</body>
</html>