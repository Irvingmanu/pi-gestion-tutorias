<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="paginaActiva" value="grupos" scope="request"/>
<!-- alumnoFormulario, esEdicion, tituloBanner y mensajeError ya vienen calculados desde
     AlumnoServlet (forwardAFormulario/resolverMensajeError): esta vista solo los consume. -->
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
<body data-mensaje-error="${mensajeError}">

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
                        <label for="apellidoPaterno" class="form-label fs-6 fw-bold">Apellido paterno</label>
                        <input type="text" id="apellidoPaterno" name="apellidoPaterno" class="form-control form-control-figma w-100 fs-6"
                               value="${alumnoFormulario.apellidoPaterno}" placeholder="Escribe el apellido paterno"
                               pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$" required>
                        <div class="invalid-feedback">Solo se permiten letras y espacios.</div>
                    </div>

                    <div class="mb-4">
                        <label for="apellidoMaterno" class="form-label fs-6 fw-bold">Apellido materno</label>
                        <input type="text" id="apellidoMaterno" name="apellidoMaterno" class="form-control form-control-figma w-100 fs-6"
                               value="${alumnoFormulario.apellidoMaterno}" placeholder="Escribe el apellido materno"
                               pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]*$">
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
                        <label for="academiaFiltro" class="form-label fs-6 fw-bold">Academia</label>
                        <!-- Filtro OPCIONAL: no lleva "name" (no viaja en el POST) ni "required".
                             Solo oculta/muestra opciones de #carreraSelect por JS, en cliente. -->
                        <select id="academiaFiltro" class="form-select form-control-figma w-100 fs-6">
                            <option value="">Todas las academias</option>
                            <c:forEach var="academia" items="${listaAcademias}">
                                <option value="${academia.idAcademia}"
                                        ${alumnoFormulario != null && alumnoFormulario.grupo != null && alumnoFormulario.grupo.idAcademia == academia.idAcademia ? 'selected' : ''}>
                                        ${academia.nombre}
                                </option>
                            </c:forEach>
                        </select>
                        <div class="form-text">Filtro opcional para acortar la lista de Carrera.</div>
                    </div>

                    <div class="mb-4">
                        <label for="carreraSelect" class="form-label fs-6 fw-bold">Carrera</label>
                        <!-- Habilitado desde el inicio, con TODAS las carreras del sistema ya
                             renderizadas; el filtro de Academia (arriba) solo oculta opciones
                             por JS via data-academia-id, nunca bloquea ni recarga este select. -->
                        <select id="carreraSelect" name="idCarrera" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" ${empty alumnoFormulario ? 'selected' : ''}>Seleccione la carrera</option>
                            <c:forEach var="carrera" items="${listaCarreras}">
                                <option value="${carrera.idCarrera}" data-nivel="${carrera.nivel}" data-academia-id="${carrera.idAcademia}"
                                        ${alumnoFormulario != null && alumnoFormulario.grupo != null && alumnoFormulario.grupo.idCarrera == carrera.idCarrera ? 'selected' : ''}>
                                        ${carrera.nombre}
                                </option>
                            </c:forEach>
                        </select>
                        <div class="invalid-feedback">Por favor seleccione una carrera.</div>
                    </div>

                    <div class="mb-4">
                        <label for="cuatrimestre" class="form-label fs-6 fw-bold">Cuatrimestre</label>
                        <select id="cuatrimestre" name="cuatrimestre" class="form-select form-control-figma w-100 fs-6" required disabled
                                data-cuatrimestre-actual="${alumnoFormulario.grupo.cuatrimestre}">
                            <option value="" selected>Seleccione primero la carrera</option>
                        </select>
                        <div class="invalid-feedback">Por favor seleccione un cuatrimestre.</div>
                    </div>

                    <div class="mb-4">
                        <label for="letra" class="form-label fs-6 fw-bold">Grupo</label>
                        <select id="letra" name="letra" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" ${empty alumnoFormulario ? 'selected' : ''}>Seleccione el grupo</option>
                            <option value="A" ${alumnoFormulario != null && alumnoFormulario.grupo != null && alumnoFormulario.grupo.letra == 'A' ? 'selected' : ''}>A</option>
                            <option value="B" ${alumnoFormulario != null && alumnoFormulario.grupo != null && alumnoFormulario.grupo.letra == 'B' ? 'selected' : ''}>B</option>
                            <option value="C" ${alumnoFormulario != null && alumnoFormulario.grupo != null && alumnoFormulario.grupo.letra == 'C' ? 'selected' : ''}>C</option>
                            <option value="D" ${alumnoFormulario != null && alumnoFormulario.grupo != null && alumnoFormulario.grupo.letra == 'D' ? 'selected' : ''}>D</option>
                            <option value="E" ${alumnoFormulario != null && alumnoFormulario.grupo != null && alumnoFormulario.grupo.letra == 'E' ? 'selected' : ''}>E</option>
                            <option value="F" ${alumnoFormulario != null && alumnoFormulario.grupo != null && alumnoFormulario.grupo.letra == 'F' ? 'selected' : ''}>F</option>
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
<script src="${pageContext.request.contextPath}/assets/js/coordinador/formulario-alumno.js"></script>
</body>
</html>