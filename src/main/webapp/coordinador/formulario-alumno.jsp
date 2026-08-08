<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="mx.edu.utez.pigestiontutorias.models.Alumno" %>
<%
    request.setAttribute("paginaActiva", "grupos");

    Alumno alumnoEdit = (Alumno) request.getAttribute("alumnoEdit");
    Alumno alumnoConError = (Alumno) request.getAttribute("alumno");
    Alumno alumnoFormulario = alumnoEdit != null ? alumnoEdit : alumnoConError;
    request.setAttribute("alumnoFormulario", alumnoFormulario);

    boolean esEdicion = alumnoEdit != null || "editar".equals(request.getParameter("accion"));
    request.setAttribute("esEdicion", esEdicion);
    request.setAttribute("tituloBanner", esEdicion ? "Editar Alumno" : "Nuevo Alumno");

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
    request.setAttribute("mensajeError", mensajeError);
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - ${tituloBanner}</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/alertas.css" rel="stylesheet">

    <style>
        /* Ajuste para iconos de error en inputs personalizados */
        .form-control-figma.is-invalid, .form-select.is-invalid {
            border-color: #dc3545 !important;
            background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12' width='12' height='12' fill='none' stroke='%23dc3545'%3e%3ccircle cx='6' cy='6' r='4.5'/%3e%3cpath stroke-linejoin='round' d='M5.8 3.6h.4L6 6.5z'/%3e%3ccircle cx='6' cy='8.2' r='.6' fill='%23dc3545' stroke='none'/%3e%3c/svg%3e");
            background-repeat: no-repeat;
            background-position: right calc(.375em + .1875rem) center;
            background-size: calc(.75em + .375rem) calc(.75em + .375rem);
        }

        /* Estilo para degradar el botón cuando está deshabilitado */
        .btn-figma:disabled {
            background-color: #7ab899 !important;
            color: #ffffff;
            cursor: not-allowed;
            opacity: 0.6;
            box-shadow: none;
            border: none;
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
            ${tituloBanner}
        </div>

        <!-- Formulario con novalidate para validación manual -->
        <form id="formGuardar" class="form-wrap-figma mt-3 needs-validation" style="max-width: 720px;" action="${pageContext.request.contextPath}/gestion-grupos" method="post" novalidate>

            <input type="hidden" name="accion" value="${esEdicion ? 'editar' : 'nuevo'}">

            <div class="row">

                <!-- Columna izquierda -->
                <div class="col-md-6">

                    <div class="mb-4">
                        <label for="nombres" class="form-label fs-6 fw-bold">Nombres</label>
                        <input type="text" id="nombres" name="nombres" class="form-control form-control-figma w-100 fs-6"
                               value="${alumnoFormulario.nombres}" placeholder="Escribe los nombres"
                               pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$" required>
                        <div class="invalid-feedback">Solo se permiten letras y espacios.</div>
                    </div>

                    <div class="mb-4">
                        <label for="apellidos" class="form-label fs-6 fw-bold">Apellidos</label>
                        <input type="text" id="apellidos" name="apellidos" class="form-control form-control-figma w-100 fs-6"
                               value="${alumnoFormulario.apellidos}" placeholder="Escribe los apellidos"
                               pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$" required>
                        <div class="invalid-feedback">Solo se permiten letras y espacios.</div>
                    </div>

                    <div class="mb-4">
                        <label for="correo" class="form-label fs-6 fw-bold">Correo</label>
                        <input type="email" id="correo" name="correo" class="form-control form-control-figma w-100 fs-6"
                               value="${alumnoFormulario.correoInstitucional}"
                               placeholder="Escribe el correo" pattern="^[a-zA-Z0-9._-]+@utez\.edu\.mx$"
                               oninput="this.value = this.value.replace(/[^a-zA-Z0-9.\-_@]/g, '')" required>
                        <div class="invalid-feedback">El correo debe tener un formato válido y terminar en @utez.edu.mx.</div>
                    </div>

                    <div class="mb-4">
                        <label for="matricula" class="form-label fs-6 fw-bold">Matrícula</label>
                        <input type="text" id="matricula" name="matricula" class="form-control form-control-figma w-100 fs-6"
                               value="${alumnoFormulario.matricula}" placeholder="Escribe la matrícula"
                               style="text-transform: uppercase;"
                               maxlength="10" minlength="10" pattern="^[a-zA-Z0-9]{10}$"
                               oninput="this.value = this.value.replace(/[^a-zA-Z0-9]/g, '').toUpperCase()"
                        ${esEdicion ? 'readonly' : ''} required>
                        <div class="invalid-feedback">La matrícula debe tener exactamente 10 caracteres alfanuméricos.</div>
                    </div>

                    <div class="mb-4">
                        <label for="telefono" class="form-label fs-6 fw-bold">Teléfono</label>
                        <input type="text" id="telefono" name="telefono" class="form-control form-control-figma w-100 fs-6"
                               value="${alumnoFormulario.telefono}" placeholder="Escribe el teléfono"
                               pattern="^\d{10}$" maxlength="10" minlength="10"
                               oninput="this.value = this.value.replace(/[^0-9]/g, '')" required>
                        <div class="invalid-feedback">Debe contener exactamente 10 dígitos numéricos.</div>
                    </div>

                </div>

                <!-- Columna derecha -->
                <div class="col-md-6">

                    <div class="mb-4">
                        <label for="genero" class="form-label fs-6 fw-bold">Género</label>
                        <select id="genero" name="idGenero" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" ${empty alumnoFormulario ? 'selected' : ''}>Seleccione el género</option>
                            <c:forEach var="genero" items="${listaGeneros}">
                                <option value="${genero.id}" ${alumnoFormulario != null && alumnoFormulario.idGenero == genero.id ? 'selected' : ''}>
                                        ${genero.nombre}
                                </option>
                            </c:forEach>
                        </select>
                        <div class="invalid-feedback">Por favor seleccione un género.</div>
                    </div>

                    <div class="mb-4">
                        <label for="carrera" class="form-label fs-6 fw-bold">Carrera</label>
                        <select id="carrera" name="idCarrera" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" ${empty alumnoFormulario ? 'selected' : ''}>Seleccione la carrera</option>
                            <c:forEach var="carrera" items="${listaCarreras}">
                                <option value="${carrera.idCarrera}" ${alumnoFormulario != null && alumnoFormulario.idCarrera == carrera.idCarrera ? 'selected' : ''}>
                                        ${carrera.nombre}
                                </option>
                            </c:forEach>
                        </select>
                        <div class="invalid-feedback">Por favor seleccione una carrera.</div>
                    </div>

                    <div class="mb-4">
                        <label for="cuatrimestre" class="form-label fs-6 fw-bold">Cuatrimestre</label>
                        <select id="cuatrimestre" name="idCuatrimestre" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" ${empty alumnoFormulario ? 'selected' : ''}>Seleccione el cuatrimestre</option>
                            <c:forEach var="cuatrimestre" items="${listaCuatrimestres}">
                                <option value="${cuatrimestre.idCuatrimestre}" ${alumnoFormulario != null && alumnoFormulario.idCuatrimestre == cuatrimestre.idCuatrimestre ? 'selected' : ''}>
                                        ${cuatrimestre.numero}°
                                </option>
                            </c:forEach>
                        </select>
                        <div class="invalid-feedback">Por favor seleccione un cuatrimestre.</div>
                    </div>

                    <div class="mb-4">
                        <label for="letraGrupo" class="form-label fs-6 fw-bold">Grupo</label>
                        <select id="letraGrupo" name="idLetraGrupo" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" ${empty alumnoFormulario ? 'selected' : ''}>Seleccione el grupo</option>
                            <c:forEach var="letraGrupo" items="${listaLetrasGrupo}">
                                <option value="${letraGrupo.idLetra}" ${alumnoFormulario != null && alumnoFormulario.idLetraGrupo == letraGrupo.idLetra ? 'selected' : ''}>
                                        ${letraGrupo.letra}
                                </option>
                            </c:forEach>
                        </select>
                        <div class="invalid-feedback">Por favor seleccione un grupo.</div>
                    </div>

                </div>

            </div>

            <div class="d-flex justify-content-center gap-3 mt-4">
                <button type="button" id="btnCancelarFormulario" class="btn-cancelar-figma fw-medium fs-5 px-4 py-2"
                        data-url-cancelar="${pageContext.request.contextPath}/gestion-grupos" onclick="confirmarCancelacion()">Cancelar</button>
                <button type="submit" id="btnGuardar" class="btn-figma fw-medium fs-5 px-4 py-2" disabled>Guardar</button>
            </div>

        </form>

    </div>

</div>

<jsp:include page="../includes/alertas.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/alumnos.js"></script>

<!-- Script para validar el formulario en tiempo real -->
<script>
    document.addEventListener('DOMContentLoaded', function () {
        const form = document.getElementById('formGuardar');
        const btnGuardar = document.getElementById('btnGuardar');
        const inputsRequeridos = form.querySelectorAll('input[required], select[required]');

        function verificarFormulario() {
            let esValido = true;
            inputsRequeridos.forEach(input => {
                if (!input.checkValidity()) {
                    esValido = false;
                }
            });
            btnGuardar.disabled = !esValido;
        }

        inputsRequeridos.forEach(input => {
            input.addEventListener('input', function () {
                if (this.checkValidity()) {
                    this.classList.remove('is-invalid');
                } else {
                    this.classList.add('is-invalid');
                }
                verificarFormulario();
            });

            input.addEventListener('change', function () {
                if (this.checkValidity()) {
                    this.classList.remove('is-invalid');
                } else {
                    this.classList.add('is-invalid');
                }
                verificarFormulario();
            });

            input.addEventListener('blur', function () {
                if (!this.checkValidity()) {
                    this.classList.add('is-invalid');
                }
                verificarFormulario();
            });
        });

        // Verificación inicial por si estamos en modo edición
        verificarFormulario();
    });
</script>

<c:if test="${not empty mensajeError}">
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            mostrarAlerta('error', 'Error', '${mensajeError}');
        });
    </script>
</c:if>
</body>
</html>