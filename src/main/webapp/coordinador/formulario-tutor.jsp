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
    } else if ("registro_fallido".equals(codigoError)) {
        mensajeError = "No se pudo guardar el tutor. Intenta de nuevo.";
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
    <style>
        /* Pequeño ajuste para que los iconos de feedback de Bootstrap no se encimen si usas padding personalizado */
        .form-control-figma.is-invalid {
            border-color: #dc3545;
            background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12' width='12' height='12' fill='none' stroke='%23dc3545'%3e%3ccircle cx='6' cy='6' r='4.5'/%3e%3cpath stroke-linejoin='round' d='M5.8 3.6h.4L6 6.5z'/%3e%3ccircle cx='6' cy='8.2' r='.6' fill='%23dc3545' stroke='none'/%3e%3c/svg%3e");
            background-repeat: no-repeat;
            background-position: right calc(.375em + .1875rem) center;
            background-size: calc(.75em + .375rem) calc(.75em + .375rem);
        }
    </style>
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL ==================== -->
    <jsp:include page="../includes/navbar-coordinador.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            <%= tituloBanner %>
        </div>

        <!-- Se agregó el atributo 'novalidate' para desactivar los tooltips nativos del navegador -->
        <form id="formGuardar" class="form-wrap-figma mt-3 needs-validation" style="max-width: 1100px;" action="<%= request.getContextPath() %>/gestion-tutores" method="post" novalidate>

            <input type="hidden" name="accion" value="<%= esEdicion ? "editar" : "nuevo" %>">
            <input type="hidden" name="idTutor" value="<%= tutorFormulario != null ? tutorFormulario.getIdTutor() : 0 %>">
            <input type="hidden" name="idUsuario" value="<%= tutorFormulario != null ? tutorFormulario.getIdUsuario() : 0 %>">

            <div class="row">

                <!-- ==================== SECCIÓN 1: INFORMACIÓN PERSONAL ==================== -->
                <div class="col-lg-4 col-md-6 mb-4">
                    <h5 class="fw-bold fs-6 mb-3 text-secondary border-bottom pb-2">Información Personal</h5>

                    <div class="mb-4">
                        <label for="nombres" class="form-label fs-6 fw-bold">Nombres</label>
                        <input type="text" id="nombres" name="nombres" class="form-control form-control-figma w-100 fs-6"
                               value="<%= tutorFormulario != null && tutorFormulario.getNombres() != null ? tutorFormulario.getNombres() : "" %>"
                               placeholder="Escribe los nombres" pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$"
                               required>
                        <div class="invalid-feedback">
                            Ingresa un nombre válido (solo letras).
                        </div>
                    </div>

                    <div class="mb-4">
                        <label for="apellidos" class="form-label fs-6 fw-bold">Apellidos</label>
                        <input type="text" id="apellidos" name="apellidos" class="form-control form-control-figma w-100 fs-6"
                               value="<%= tutorFormulario != null && tutorFormulario.getApellidos() != null ? tutorFormulario.getApellidos() : "" %>"
                               placeholder="Escribe los apellidos" pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$"
                               required>
                        <div class="invalid-feedback">
                            Ingresa apellidos válidos (solo letras).
                        </div>
                    </div>

                    <div class="mb-4">
                        <label for="telefono" class="form-label fs-6 fw-bold">Teléfono</label>
                        <input type="text" id="telefono" name="telefono" class="form-control form-control-figma w-100 fs-6"
                               value="<%= tutorFormulario != null && tutorFormulario.getTelefono() != null ? tutorFormulario.getTelefono() : "" %>"
                               placeholder="+52 ..." pattern="^\d{10}$" maxlength="10" minlength="10"
                               oninput="this.value = this.value.replace(/[^0-9]/g, '')" required>
                        <div class="invalid-feedback">
                            Ingresa un teléfono válido de 10 dígitos.
                        </div>
                    </div>

                    <div class="mb-4">
                        <label for="correo" class="form-label fs-6 fw-bold">Correo</label>
                        <input type="email" id="correo" name="correo" class="form-control form-control-figma w-100 fs-6"
                               value="<%= tutorFormulario != null && tutorFormulario.getCorreoInstitucional() != null ? tutorFormulario.getCorreoInstitucional() : "" %>"
                               placeholder="Escribe el correo" pattern="^[a-zA-Z0-9._-]+@utez\.edu\.mx$"
                               oninput="this.value = this.value.replace(/[^a-zA-Z0-9.\-_@]/g, '')" required>
                        <div class="invalid-feedback">
                            El correo debe terminar en @utez.edu.mx
                        </div>
                    </div>
                </div>

                <!-- ==================== SECCIÓN 2: INFORMACIÓN LABORAL ==================== -->
                <div class="col-lg-4 col-md-6 mb-4">
                    <h5 class="fw-bold fs-6 mb-3 text-secondary border-bottom pb-2">Información Laboral</h5>

                    <div class="mb-4">
                        <label for="nomina" class="form-label fs-6 fw-bold">Nómina</label>
                        <input type="text" id="nomina" name="nomina" class="form-control form-control-figma w-100 fs-6"
                               value="<%= tutorFormulario != null && tutorFormulario.getNomina() > 0 ? tutorFormulario.getNomina() : "" %>"
                               placeholder="Escribe la nómina" maxlength="4" minlength="4" pattern="^[0-9]{4}$"
                               oninput="this.value = this.value.replace(/[^0-9]/g, '')"
                            <%= esEdicion ? "readonly" : "" %> required>
                        <div class="invalid-feedback">
                            La nómina debe tener exactamente 4 dígitos.
                        </div>
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
                        <div class="invalid-feedback">
                            Por favor selecciona una academia.
                        </div>
                    </div>
                </div>

                <!-- ==================== SECCIÓN 3: HORARIO DE ATENCIÓN ==================== -->
                <div class="col-lg-4 col-md-12 mb-4">
                    <h5 class="fw-bold fs-6 mb-3 text-secondary border-bottom pb-2">Horario de Atención</h5>

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

                    <div class="mb-3">
                        <label class="form-label fs-6 fw-bold">Horas</label>
                        <div class="d-flex align-items-center gap-2 mb-2">
                            <div class="row g-2 flex-grow-1 align-items-center">
                                <div class="col-6">
                                    <input type="time" id="horarioDesde" class="form-control form-control-figma fs-6" value="08:00" min="08:00" max="20:00" style="cursor: pointer;" onclick="this.showPicker()" onchange="validarLimitesHora(this)">
                                </div>
                                <div class="col-6">
                                    <input type="time" id="horarioHasta" class="form-control form-control-figma fs-6" value="10:00" min="08:00" max="20:00" style="cursor: pointer;" onclick="this.showPicker()" onchange="validarLimitesHora(this)">
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

            <div class="d-flex justify-content-center gap-3 mt-4 border-top pt-4">
                <button type="button" id="btnCancelarFormulario" class="btn-cancelar-figma fw-medium fs-5 px-4 py-2"
                        data-url-cancelar="<%= request.getContextPath() %>/gestion-tutores" onclick="confirmarCancelacion()">Cancelar</button>
                <!-- Se agregó un ID al botón de Guardar y se inicializa deshabilitado -->
                <button type="submit" id="btnGuardar" class="btn-figma fw-medium fs-5 px-4 py-2" disabled>Guardar</button>
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

<!-- Lógica para Validación Visual y Activación del Botón -->
<script>
    function validarLimitesHora(input) {
        const minHora = '08:00';
        const maxHora = '20:00';
        if (input.value) {
            if (input.value < minHora) { input.value = minHora; }
            else if (input.value > maxHora) { input.value = maxHora; }
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        const form = document.getElementById('formGuardar');
        const btnGuardar = document.getElementById('btnGuardar');
        const inputsRequeridos = form.querySelectorAll('input[required], select[required]');

        // Función que verifica si todo el formulario cumple los requisitos
        function verificarFormulario() {
            let esValido = true;
            inputsRequeridos.forEach(input => {
                if (!input.checkValidity()) {
                    esValido = false;
                }
            });
            // Habilita o deshabilita el botón según el resultado
            btnGuardar.disabled = !esValido;
        }

        // Listener para cada campo: pinta de rojo si está mal, quita el rojo si está bien
        inputsRequeridos.forEach(input => {
            // Validar mientras escribe
            input.addEventListener('input', function () {
                if (this.checkValidity()) {
                    this.classList.remove('is-invalid');
                } else {
                    this.classList.add('is-invalid');
                }
                verificarFormulario();
            });

            // Validar también cuando quita el foco (clic afuera)
            input.addEventListener('blur', function () {
                if (!this.checkValidity()) {
                    this.classList.add('is-invalid');
                }
                verificarFormulario();
            });
        });

        // Verificación inicial por si estamos en modo edición y los campos ya vienen llenos
        verificarFormulario();
    });
</script>
</body>
</html>