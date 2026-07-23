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
    <title>Sistema de Gestión de Tutorías - Asistencia de Tutoría Grupal</title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
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
            Asistencia de Tutoría Grupal
        </div>

        <div class="form-wrap-figma" style="max-width: 900px;">
            <form action="#" method="post">

                <!-- Fila 1: Grupo / Carrera -->
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
                        <label for="carrera" class="form-label fs-6 fw-bold">Carrera</label>
                        <select id="carrera" name="idCarrera" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" selected>Seleccione la carrera</option>
                            <% for (Carrera carrera : listaCarreras) { %>
                            <option value="<%= carrera.getIdCarrera() %>"><%= carrera.getNombre() %></option>
                            <% } %>
                        </select>
                    </div>
                </div>

                <!-- Fila 2: Cuatrimestre / Fecha -->
                <div class="row g-3 mb-4">
                    <div class="col-md-6">
                        <label for="cuatrimestre" class="form-label fs-6 fw-bold">Cuatrimestre</label>
                        <select id="cuatrimestre" name="idCuatrimestre" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" selected>Seleccione el cuatrimestre</option>
                            <% for (Cuatrimestre cuatrimestre : listaCuatrimestres) { %>
                            <option value="<%= cuatrimestre.getIdCuatrimestre() %>"><%= cuatrimestre.getNumero() %>°</option>
                            <% } %>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label for="fecha" class="form-label fs-6 fw-bold">Fecha</label>
                        <input type="date" id="fecha" name="fecha" class="form-control form-control-figma w-100 fs-6" required>
                    </div>
                </div>

                <!-- Tabla de asistencia — misma estructura/clases que la tabla de alumnos en
                     coordinador/gestion-grupos.jsp (.table-responsive + .tabla-grupos) -->
                <div class="table-responsive mb-4">
                    <table class="tabla-grupos fs-6">
                        <thead>
                        <tr>
                            <th>Nombres</th>
                            <th>Apellidos</th>
                            <th>Asistencia</th>
                        </tr>
                        </thead>
                        <tbody>
                        <%-- TODO: reemplazar por <c:forEach items="${listaAlumnosGrupo}" var="alumno"> cuando el controlador exista --%>
                        <tr>
                            <td>Porfirio Alexander</td>
                            <td>Lopez Salgado</td>
                            <td class="text-center">
                                <input type="checkbox" name="asistencia[]" value="1" class="form-check-input">
                            </td>
                        </tr>
                        <tr>
                            <td>María Fernanda</td>
                            <td>Cruz Hernández</td>
                            <td class="text-center">
                                <input type="checkbox" name="asistencia[]" value="2" class="form-check-input">
                            </td>
                        </tr>
                        <tr>
                            <td>Jesús Emiliano</td>
                            <td>Rivera Castillo</td>
                            <td class="text-center">
                                <input type="checkbox" name="asistencia[]" value="3" class="form-check-input">
                            </td>
                        </tr>
                        <tr>
                            <td>Ana Karen</td>
                            <td>Morales Vázquez</td>
                            <td class="text-center">
                                <input type="checkbox" name="asistencia[]" value="4" class="form-check-input">
                            </td>
                        </tr>
                        <tr>
                            <td>Diego Alberto</td>
                            <td>Sánchez Ortega</td>
                            <td class="text-center">
                                <input type="checkbox" name="asistencia[]" value="5" class="form-check-input">
                            </td>
                        </tr>
                        </tbody>
                    </table>
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
