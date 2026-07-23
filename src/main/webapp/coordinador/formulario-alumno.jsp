<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Alumno" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Genero" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Carrera" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Cuatrimestre" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.LetraGrupo" %>
<%
    request.setAttribute("paginaActiva", "grupos");

    Alumno alumnoEdit = (Alumno) request.getAttribute("alumnoEdit");
    Alumno alumnoConError = (Alumno) request.getAttribute("alumno");
    Alumno alumnoFormulario = alumnoEdit != null ? alumnoEdit : alumnoConError;

    List<Genero> listaGeneros = (List<Genero>) request.getAttribute("listaGeneros");
    List<Carrera> listaCarreras = (List<Carrera>) request.getAttribute("listaCarreras");
    List<Cuatrimestre> listaCuatrimestres = (List<Cuatrimestre>) request.getAttribute("listaCuatrimestres");
    List<LetraGrupo> listaLetrasGrupo = (List<LetraGrupo>) request.getAttribute("listaLetrasGrupo");

    boolean esEdicion = alumnoEdit != null || "editar".equals(request.getParameter("accion"));
    String tituloBanner = esEdicion ? "Editar Alumno" : "Nuevo Alumno";

    String codigoError = (String) request.getAttribute("error");
    String mensajeError = null;
    if ("matricula_duplicada".equals(codigoError)) {
        mensajeError = "Esta matrícula ya está registrada en el sistema.";
    } else if ("correo_duplicado".equals(codigoError)) {
        mensajeError = "Este correo ya está registrado en el sistema.";
    } else if ("telefono_duplicado".equals(codigoError)) {
        mensajeError = "Este número de teléfono ya está registrado en el sistema.";
    } else if ("formato_invalido".equals(codigoError)) {
        mensajeError = "Verifica los datos. El formato de uno o más campos es incorrecto.";
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - <%= tituloBanner %></title>
    <link href="<%= request.getContextPath() %>/assets/css/bootstrap.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/global.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/alertas.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL ==================== -->
    <jsp:include page="../includes/navbar.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            <%= tituloBanner %>
        </div>

        <!-- Formulario de nuevo/edicion de alumno -->
        <form class="form-wrap-figma mt-3" style="max-width: 720px;" action="<%= request.getContextPath() %>/AlumnoServlet" method="post">

            <input type="hidden" name="accion" value="<%= esEdicion ? "editar" : "nuevo" %>">

            <div class="row">

                <!-- Columna izquierda -->
                <div class="col-md-6">

                    <div class="mb-4">
                        <label for="nombres" class="form-label fs-6 fw-bold">Nombres</label>
                        <input type="text" id="nombres" name="nombres" class="form-control form-control-figma w-100 fs-6"
                               value="<%= alumnoFormulario != null ? alumnoFormulario.getNombres() : "" %>" placeholder="Escribe los nombres"
                               pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$" title="Solo se permiten letras y espacios." required>
                    </div>

                    <div class="mb-4">
                        <label for="apellidos" class="form-label fs-6 fw-bold">Apellidos</label>
                        <input type="text" id="apellidos" name="apellidos" class="form-control form-control-figma w-100 fs-6"
                               value="<%= alumnoFormulario != null ? alumnoFormulario.getApellidos() : "" %>" placeholder="Escribe los apellidos"
                               pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$" title="Solo se permiten letras y espacios." required>
                    </div>

                    <div class="mb-4">
                        <label for="correo" class="form-label fs-6 fw-bold">Correo</label>
                        <input type="email" id="correo" name="correo" class="form-control form-control-figma w-100 fs-6"
                               value="<%= alumnoFormulario != null ? alumnoFormulario.getCorreoInstitucional() : "" %>"
                               placeholder="Escribe el correo" pattern="^[a-zA-Z0-9._-]+@utez\.edu\.mx$"
                               title="El correo debe tener un formato válido y terminar en @utez.edu.mx"
                               oninput="this.value = this.value.replace(/[^a-zA-Z0-9.\-_@]/g, '')" required>
                    </div>

                    <div class="mb-4">
                        <label for="matricula" class="form-label fs-6 fw-bold">Matrícula</label>
                        <input type="text" id="matricula" name="matricula" class="form-control form-control-figma w-100 fs-6"
                               value="<%= alumnoFormulario != null ? alumnoFormulario.getMatricula() : "" %>" placeholder="Escribe la matrícula"
                               pattern="^[a-zA-Z0-9]+$" title="La matrícula solo debe contener letras y números, sin espacios ni símbolos."
                               oninput="this.value = this.value.replace(/[^a-zA-Z0-9]/g, '')"
                               <%= esEdicion ? "readonly" : "" %> required>
                    </div>

                    <div class="mb-4">
                        <label for="telefono" class="form-label fs-6 fw-bold">Teléfono</label>
                        <input type="text" id="telefono" name="telefono" class="form-control form-control-figma w-100 fs-6"
                               value="<%= alumnoFormulario != null ? alumnoFormulario.getTelefono() : "" %>" placeholder="Escribe el teléfono"
                               pattern="^\d{10}$" maxlength="10" minlength="10"
                               title="Debe contener exactamente 10 dígitos numéricos."
                               oninput="this.value = this.value.replace(/[^0-9]/g, '')" required>
                    </div>

                </div>

                <!-- Columna derecha -->
                <div class="col-md-6">

                    <div class="mb-4">
                        <label for="genero" class="form-label fs-6 fw-bold">Género</label>
                        <select id="genero" name="idGenero" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" <%= alumnoFormulario == null ? "selected" : "" %>>Seleccione el género</option>
                            <% for (Genero genero : listaGeneros) { %>
                            <option value="<%= genero.getId() %>"
                                    <%= (alumnoFormulario != null && alumnoFormulario.getIdGenero() == genero.getId()) ? "selected" : "" %>>
                                <%= genero.getNombre() %>
                            </option>
                            <% } %>
                        </select>
                    </div>

                    <div class="mb-4">
                        <label for="carrera" class="form-label fs-6 fw-bold">Carrera</label>
                        <select id="carrera" name="idCarrera" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" <%= alumnoFormulario == null ? "selected" : "" %>>Seleccione la carrera</option>
                            <% for (Carrera carrera : listaCarreras) { %>
                            <option value="<%= carrera.getIdCarrera() %>"
                                    <%= (alumnoFormulario != null && alumnoFormulario.getIdCarrera() == carrera.getIdCarrera()) ? "selected" : "" %>>
                                <%= carrera.getNombre() %>
                            </option>
                            <% } %>
                        </select>
                    </div>

                    <div class="mb-4">
                        <label for="cuatrimestre" class="form-label fs-6 fw-bold">Cuatrimestre</label>
                        <select id="cuatrimestre" name="idCuatrimestre" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" <%= alumnoFormulario == null ? "selected" : "" %>>Seleccione el cuatrimestre</option>
                            <% for (Cuatrimestre cuatrimestre : listaCuatrimestres) { %>
                            <option value="<%= cuatrimestre.getIdCuatrimestre() %>"
                                    <%= (alumnoFormulario != null && alumnoFormulario.getIdCuatrimestre() == cuatrimestre.getIdCuatrimestre()) ? "selected" : "" %>>
                                <%= cuatrimestre.getNumero() %>°
                            </option>
                            <% } %>
                        </select>
                    </div>

                    <div class="mb-4">
                        <label for="letraGrupo" class="form-label fs-6 fw-bold">Grupo</label>
                        <select id="letraGrupo" name="idLetraGrupo" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" <%= alumnoFormulario == null ? "selected" : "" %>>Seleccione el grupo</option>
                            <% for (LetraGrupo letraGrupo : listaLetrasGrupo) { %>
                            <option value="<%= letraGrupo.getIdLetra() %>"
                                    <%= (alumnoFormulario != null && alumnoFormulario.getIdLetraGrupo() == letraGrupo.getIdLetra()) ? "selected" : "" %>>
                                <%= letraGrupo.getLetra() %>
                            </option>
                            <% } %>
                        </select>
                    </div>

                </div>

            </div>

            <div class="d-flex justify-content-center gap-3 mt-4">
                <button type="button" id="btnCancelarFormulario" class="btn-cancelar-figma fw-medium fs-5 px-4 py-2"
                        data-url-cancelar="<%= request.getContextPath() %>/AlumnoServlet" onclick="confirmarCancelacion()">Cancelar</button>
                <button type="submit" class="btn-figma fw-medium fs-5 px-4 py-2">Guardar</button>
            </div>

        </form>

    </div>

</div>

<jsp:include page="../includes/alertas.jsp" />

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/alertas.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/coordinador/alumnos.js"></script>

<% if (mensajeError != null) { %>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        mostrarAlerta('error', 'Error', '<%= mensajeError %>');
    });
</script>
<% } %>
</body>
</html>
