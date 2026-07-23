<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Carrera" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Cuatrimestre" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.LetraGrupo" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO" %>
<%
    AlumnoDAO alumnoDAO = new AlumnoDAO();
    List<LetraGrupo> listaGrupos = alumnoDAO.getAllLetrasGrupo();
    List<Carrera> listaCarreras = alumnoDAO.getAllCarreras();
    List<Cuatrimestre> listaCuatrimestres = alumnoDAO.getAllCuatrimestres();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Registro de Tutoría Grupal</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
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
        <a href="<%= request.getContextPath() %>/tutor/registro-grupal.jsp" class="nav-item-grupos active">
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

        <div class="titulo-principal banner-grupos h5 mb-4">
            Registro de Tutoría Grupal
        </div>

        <div class="form-wrap-figma" style="max-width: 900px;">
            <form action="<%= request.getContextPath() %>/TutoriaServlet" method="post">

                <input type="hidden" name="accion" value="registrarGrupal">

                <!-- Fila 1: Grupo / Fecha -->
                <div class="row g-3 mb-4">
                    <div class="col-md-6">
                        <label for="grupo" class="form-label fs-6 fw-bold">Grupo</label>
                        <select id="grupo" name="idLetraGrupo" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" selected>Seleccione el grupo</option>
                            <% for (LetraGrupo grupo : listaGrupos) { %>
                            <option value="<%= grupo.getIdLetra() %>"><%= grupo.getLetra() %></option>
                            <% } %>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label for="fecha" class="form-label fs-6 fw-bold">Fecha</label>
                        <input type="date" id="fecha" name="fecha" class="form-control form-control-figma w-100 fs-6" required>
                    </div>
                </div>

                <!-- Fila 2: Carrera / Cuatrimestre -->
                <div class="row g-3 mb-4">
                    <div class="col-md-6">
                        <label for="carrera" class="form-label fs-6 fw-bold">Carrera</label>
                        <select id="carrera" name="idCarrera" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" selected>Seleccione la carrera</option>
                            <% for (Carrera carrera : listaCarreras) { %>
                            <option value="<%= carrera.getIdCarrera() %>"><%= carrera.getNombre() %></option>
                            <% } %>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label for="cuatrimestre" class="form-label fs-6 fw-bold">Cuatrimestre</label>
                        <select id="cuatrimestre" name="idCuatrimestre" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" selected>Seleccione el cuatrimestre</option>
                            <% for (Cuatrimestre cuatrimestre : listaCuatrimestres) { %>
                            <option value="<%= cuatrimestre.getIdCuatrimestre() %>"><%= cuatrimestre.getNumero() %>°</option>
                            <% } %>
                        </select>
                    </div>
                </div>

                <!-- Fila 3: Acuerdos / Asesorias grupales -->
                <div class="row g-3 mb-4">
                    <div class="col-md-6">
                        <label for="acuerdos" class="form-label fs-6 fw-bold">Acuerdos</label>
                        <textarea id="acuerdos" name="acuerdos" class="form-control form-control-figma w-100 fs-6"
                                  rows="3" placeholder="Describe los acuerdos alcanzados" required></textarea>
                    </div>
                    <div class="col-md-6">
                        <label for="asesoriasGrupales" class="form-label fs-6 fw-bold">Asesorías grupales</label>
                        <input type="text" id="asesoriasGrupales" name="asesoriasGrupales" class="form-control form-control-figma w-100 fs-6"
                               placeholder="Describe las asesorías grupales">
                    </div>
                </div>

                <!-- Fila 4: Temas Tratados / Tomar asistencia -->
                <div class="row g-3 mb-4">
                    <div class="col-md-6">
                        <label for="temasTratados" class="form-label fs-6 fw-bold">Temas Tratados</label>
                        <textarea id="temasTratados" name="temasTratados" class="form-control form-control-figma w-100 fs-6"
                                  rows="3" placeholder="Describe los temas tratados en la sesión" required></textarea>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label fs-6 fw-bold d-block">Tomar asistencia</label>
                        <a href="<%= request.getContextPath() %>/tutor/asistencia-grupal.jsp" class="btn-figma fw-medium fs-5 px-4 py-2 w-100">IR</a>
                    </div>
                </div>

                <div class="d-flex justify-content-end mt-4">
                    <button type="submit" class="btn-figma fw-medium fs-5 px-4 py-2">Guardar</button>
                </div>

            </form>
        </div>

    </div>

</div>

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
</body>
</html>
