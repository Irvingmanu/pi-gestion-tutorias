<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Carrera" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Cuatrimestre" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.LetraGrupo" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Alumno" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.dao.AlumnoDAO" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.dao.AsistenciaGrupalDao" %>
<%
    AlumnoDAO alumnoDAO = new AlumnoDAO();
    List<LetraGrupo> listaGrupos = alumnoDAO.getAllLetrasGrupo();
    List<Carrera> listaCarreras = alumnoDAO.getAllCarreras();
    List<Cuatrimestre> listaCuatrimestres = alumnoDAO.getAllCuatrimestres();

    String grupoParam = request.getParameter("idLetraGrupo");
    String carreraParam = request.getParameter("idCarrera");
    String cuatrimestreParam = request.getParameter("idCuatrimestre");
    String fechaParam = request.getParameter("fecha");

    List<Alumno> listaAlumnos = (List<Alumno>) request.getAttribute("listaAlumnosGrupo");

    List<Integer> idsAsistidos = (List<Integer>) request.getAttribute("idsAsistidos");
    if (idsAsistidos == null) {
        idsAsistidos = new ArrayList<>();
    }
    try {
        if (grupoParam != null && !grupoParam.trim().isEmpty() &&
                carreraParam != null && !carreraParam.trim().isEmpty() &&
                cuatrimestreParam != null && !cuatrimestreParam.trim().isEmpty()) {

            AsistenciaGrupalDao tempDao = new AsistenciaGrupalDao();
            listaAlumnos = tempDao.getAlumnosPorFiltros(
                    Integer.parseInt(grupoParam),
                    Integer.parseInt(carreraParam),
                    Integer.parseInt(cuatrimestreParam)
            );
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    request.setAttribute("paginaActiva", "grupal");
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

    <!-- Agrega esta línea para que tu modal tenga diseño -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/alertas.css">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-tutor.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="titulo-principal banner-grupos h5 mb-4">
            Asistencia de Tutoría Grupal
        </div>

        <div class="form-wrap-figma" style="max-width: 900px;">

            <!-- Formulario 1: Filtros y Consulta (Método GET) -->
            <form action="<%= request.getContextPath() %>/tutor/asistencia-grupal" method="get">

                <!-- Fila 1: Grupo / Carrera -->
                <div class="row g-3 mb-4">
                    <div class="col-md-6">
                        <label for="grupo" class="form-label fs-6 fw-bold">Grupo</label>
                        <select id="grupo" name="idLetraGrupo" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" selected>Seleccione el grupo</option>
                            <% for (LetraGrupo grupo : listaGrupos) {
                                String sel = (grupoParam != null && grupoParam.equals(String.valueOf(grupo.getIdLetra()))) ? "selected" : "";
                            %>
                            <option value="<%= grupo.getIdLetra() %>" <%= sel %>><%= grupo.getLetra() %></option>
                            <% } %>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label for="carrera" class="form-label fs-6 fw-bold">Carrera</label>
                        <select id="carrera" name="idCarrera" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" selected>Seleccione la carrera</option>
                            <% for (Carrera carrera : listaCarreras) {
                                String sel = (carreraParam != null && carreraParam.equals(String.valueOf(carrera.getIdCarrera()))) ? "selected" : "";
                            %>
                            <option value="<%= carrera.getIdCarrera() %>" <%= sel %>><%= carrera.getNombre() %></option>
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
                            <% for (Cuatrimestre cuatrimestre : listaCuatrimestres) {
                                String sel = (cuatrimestreParam != null && cuatrimestreParam.equals(String.valueOf(cuatrimestre.getIdCuatrimestre()))) ? "selected" : "";
                            %>
                            <option value="<%= cuatrimestre.getIdCuatrimestre() %>" <%= sel %>><%= cuatrimestre.getNumero() %>°</option>
                            <% } %>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label for="fecha" class="form-label fs-6 fw-bold">Fecha</label>
                        <input type="date" id="fecha" name="fecha" class="form-control form-control-figma w-100 fs-6" value="<%= fechaParam != null ? fechaParam : "" %>" required>
                    </div>
                </div>

                <div class="d-flex justify-content-end mb-4">
                    <button type="submit" id="btnConsultar" class="btn btn-secondary px-4">Consultar Alumnos</button>
                </div>
            </form>

            <!-- Formulario 2: Tabla de Alumnos y Guardado (Método POST) -->
            <!-- Formulario 2: Tabla de Alumnos y Guardado (Método POST) -->
            <form id="formGuardar" action="<%= request.getContextPath() %>/tutor/asistencia-grupal" method="post">
                <input type="hidden" name="idLetraGrupo" value="<%= grupoParam != null ? grupoParam : "" %>">
                <input type="hidden" name="idCarrera" value="<%= carreraParam != null ? carreraParam : "" %>">
                <input type="hidden" name="idCuatrimestre" value="<%= cuatrimestreParam != null ? cuatrimestreParam : "" %>">
                <input type="hidden" name="fecha" value="<%= fechaParam != null ? fechaParam : "" %>">

                <div class="table-responsive mb-4">
                    <table class="tabla-grupos fs-6 w-100">
                        <thead>
                        <tr>
                            <th>Nombres</th>
                            <th>Apellidos</th>
                            <th class="text-center">Asistencia</th>
                        </tr>
                        </thead>
                        <tbody>
                        <%
                            if (listaAlumnos != null && !listaAlumnos.isEmpty()) {
                                for (Alumno alumno : listaAlumnos) {
                                    boolean yaAsistio = idsAsistidos.contains(alumno.getIdAlumno());
                                    String marcado = yaAsistio ? "checked" : "";
                        %>
                        <tr>
                            <td><%= alumno.getNombres() %></td>
                            <td><%= alumno.getApellidos() %></td>
                            <td class="text-center">
                                <input type="checkbox" name="asistencia" value="<%= alumno.getIdAlumno() %>" <%= marcado %> />
                            </td>
                        </tr>
                        <%
                            }
                        } else {
                        %>
                        <tr>
                            <td colspan="3" class="text-center text-muted py-3">Seleccione los filtros y presione "Consultar Alumnos".</td>
                        </tr>
                        <%
                            }
                        %>

                        </tbody>
                    </table>
                </div>

                <% if (listaAlumnos != null && !listaAlumnos.isEmpty()) { %>
                <div class="d-flex justify-content-end mt-4">
                    <button type="submit" class="btn-figma fw-medium fs-5 px-4 py-2">Guardar</button>
                </div>
                <% } %>

            </form>
        </div>

    </div>

</div>
<!-- Modal de Confirmación -->
<div class="modal fade" id="modalConfirmacion" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" style="max-width: 420px;">
        <div class="modal-content alerta-card p-0 border-0">
            <div class="alerta-body">
                <div class="confirmacion-icono-wrap">
                    <div id="confirmacionIconoCirculo" class="confirmacion-icono">
                        <img id="confirmacionIcono" src="" data-base-path="<%= request.getContextPath() %>/assets/img/alertas/" alt="Confirmación">
                    </div>
                </div>
                <div class="alerta-texto">
                    <h3 id="confirmacionTitulo" class="confirmacion-titulo"></h3>
                    <p id="confirmacionMensaje" class="confirmacion-mensaje"></p>
                </div>
                <div class="confirmacion-botones" style="display: flex; gap: 12px; width: 100%;">
                    <button type="button" class="btn-confirmar-cancelar w-100" data-bs-dismiss="modal">Cancelar</button>
                    <button type="button" id="btnConfirmacionAceptar" class="btn-confirmar w-100">Sí, guardar</button>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/alertas.js"></script>
</body>
</html>
