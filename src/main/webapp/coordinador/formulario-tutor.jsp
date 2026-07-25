<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Tutor" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Academia" %>
<%
    request.setAttribute("paginaActiva", "tutores");

    Tutor tutorEdit = (Tutor) request.getAttribute("tutorEdit");
    Tutor tutorConError = (Tutor) request.getAttribute("tutor");
    Tutor tutorFormulario = tutorEdit != null ? tutorEdit : tutorConError;

    List<Academia> listaAcademias = (List<Academia>) request.getAttribute("listaAcademias");

    String accionParam = request.getParameter("accion");
    boolean esEdicion = tutorEdit != null || "editar".equals(accionParam) || "prepararEdicion".equals(accionParam);
    String tituloBanner = esEdicion ? "Editar Tutor" : "Nuevo Tutor";

    String codigoError = (String) request.getAttribute("error");
    String mensajeError = null;
    if ("nomina_duplicada".equals(codigoError)) {
        mensajeError = "Esta nómina ya está registrada en el sistema.";
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
    <link href="<%= request.getContextPath() %>/assets/css/coordinador/gestion-tutores.css" rel="stylesheet">
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

        <form class="form-wrap-figma mt-3" style="max-width: 920px;" action="<%= request.getContextPath() %>/TutoresServlet" method="post">

            <input type="hidden" name="accion" value="<%= esEdicion ? "editar" : "nuevo" %>">

            <div class="row">

                <div class="col-md-6">

                    <div class="mb-4">
                        <label for="nombres" class="form-label fs-6 fw-bold">Nombres</label>
                        <input type="text" id="nombres" name="nombres" class="form-control form-control-figma w-100 fs-6"
                               value="<%= tutorFormulario != null && tutorFormulario.getNombres() != null ? tutorFormulario.getNombres() : "" %>"
                               placeholder="Escribe los nombres" pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$"
                               title="Solo se permiten letras y espacios." required>
                    </div>

                    <div class="mb-4">
                        <label for="apellidos" class="form-label fs-6 fw-bold">Apellidos</label>
                        <input type="text" id="apellidos" name="apellidos" class="form-control form-control-figma w-100 fs-6"
                               value="<%= tutorFormulario != null && tutorFormulario.getApellidos() != null ? tutorFormulario.getApellidos() : "" %>"
                               placeholder="Escribe los apellidos" pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$"
                               title="Solo se permiten letras y espacios." required>
                    </div>

                    <div class="mb-4">
                        <label for="academia" class="form-label fs-6 fw-bold">Academia</label>
                        <select id="academia" name="idAcademia" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" <%= tutorFormulario == null ? "selected" : "" %>>Seleccione la academia</option>
                            <% if (listaAcademias != null) {
                                for (Academia academia : listaAcademias) { %>
                            <option value="<%= academia.getIdAcademia() %>"
                                    <%= (tutorFormulario != null && tutorFormulario.getIdAcademia() == academia.getIdAcademia()) ? "selected" : "" %>>
                                <%= academia.getNombre() %>
                            </option>
                            <%  }
                            } %>
                        </select>
                    </div>

                    <div class="mb-4">
                        <label for="telefono" class="form-label fs-6 fw-bold">Teléfono</label>
                        <input type="text" id="telefono" name="telefono" class="form-control form-control-figma w-100 fs-6"
                               value="<%= tutorFormulario != null && tutorFormulario.getTelefono() != null ? tutorFormulario.getTelefono() : "" %>"
                               placeholder="+52 ..." pattern="^\d{10}$" maxlength="10" minlength="10"
                               title="Debe contener exactamente 10 dígitos numéricos."
                               oninput="this.value = this.value.replace(/[^0-9]/g, '')" required>
                    </div>

                    <div class="mb-4">
                        <label for="selectDia" class="form-label fs-6 fw-bold">Días</label>
                        <select id="selectDia" class="form-select form-control-figma w-100 fs-6">
                            <option value="" selected>Seleccione los días disponibles</option>
                            <option value="Lunes">Lunes</option>
                            <option value="Martes">Martes</option>
                            <option value="Miércoles">Miércoles</option>
                            <option value="Jueves">Jueves</option>
                            <option value="Viernes">Viernes</option>
                        </select>
                    </div>

                </div>

                <div class="col-md-6">

                    <div class="mb-4">
                        <label for="correo" class="form-label fs-6 fw-bold">Correo</label>
                        <input type="email" id="correo" name="correo" class="form-control form-control-figma w-100 fs-6"
                               value="<%= tutorFormulario != null && tutorFormulario.getCorreoInstitucional() != null ? tutorFormulario.getCorreoInstitucional() : "" %>"
                               placeholder="Escribe el correo" pattern="^[a-zA-Z0-9._-]+@utez\.edu\.mx$"
                               title="El correo debe tener un formato válido y terminar en @utez.edu.mx"
                               oninput="this.value = this.value.replace(/[^a-zA-Z0-9.\-_@]/g, '')" required>
                    </div>

                    <div class="mb-4">
                        <label for="nomina" class="form-label fs-6 fw-bold">Nómina</label>
                        <input type="text" id="nomina" name="nomina" class="form-control form-control-figma w-100 fs-6"
                               value="<%= tutorFormulario != null && tutorFormulario.getNomina() > 0 ? tutorFormulario.getNomina() : "" %>"
                               placeholder="Escribe la nómina" pattern="^[0-9]+$"
                               title="La nómina solo debe contener números."
                               oninput="this.value = this.value.replace(/[^0-9]/g, '')"
                            <%= esEdicion ? "readonly" : "" %> required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label fs-6 fw-bold">Horarios de Atención</label>
                        <div class="d-flex align-items-center gap-2 mb-2">
                            <div class="row g-2 flex-grow-1 align-items-center">
                                <div class="col-6">
                                    <input type="time" id="horarioDesde" class="form-control form-control-figma fs-6" value="08:00">
                                </div>
                                <div class="col-6">
                                    <input type="time" id="horarioHasta" class="form-control form-control-figma fs-6" value="10:00">
                                </div>
                            </div>
                            <button type="button" id="btnAgregarHorario" class="btn-figma btn-figma-sm flex-shrink-0" title="Agregar Horario">+</button>
                        </div>
                    </div>

                    <div id="contenedorHorarios" class="d-flex flex-column gap-2 mt-2 mb-4">
                        <% if (tutorFormulario != null && tutorFormulario.getHorariosDispo() != null) {
                            for (String horario : tutorFormulario.getHorariosDispo()) { %>
                        <div class="d-flex align-items-center gap-2 mb-2 horario-item">
                            <input type="text" class="form-control form-control-figma fs-6" value="<%= horario %>" readonly>
                            <input type="hidden" name="horariosDispo" value="<%= horario %>">
                            <button type="button" class="btn-cancelar-figma btn-cancelar-figma-sm flex-shrink-0" onclick="eliminarHorario(this)" title="Eliminar Horario">-</button>
                        </div>
                        <%   }
                        } %>
                    </div>

                </div>

            </div>

            <div class="d-flex justify-content-center gap-3 mt-4">
                <button type="button" id="btnCancelarFormulario" class="btn-cancelar-figma fw-medium fs-5 px-4 py-2"
                        data-url-cancelar="<%= request.getContextPath() %>/TutoresServlet" onclick="confirmarCancelacion()">Cancelar</button>
                <button type="submit" class="btn-figma fw-medium fs-5 px-4 py-2">Guardar</button>
            </div>

        </form>

    </div>

</div>

<jsp:include page="../includes/alertas.jsp" />

<script src="<%= request.getContextPath() %>/assets/js/bootstrap.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/alertas.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/coordinador/tutor.js"></script>

<% if (mensajeError != null) { %>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        mostrarAlerta('error', 'Error', '<%= mensajeError %>');
    });
</script>
<% } %>
</body>
</html>
