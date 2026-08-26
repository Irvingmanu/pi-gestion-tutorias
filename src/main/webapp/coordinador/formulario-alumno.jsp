<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  Autor: Irvingmanu
  Fecha de creación: 2026-07-17
  Descripción: Formulario de coordinador para dar de alta o editar un alumno,
  incluyendo la asignación de grupo (con buscador Select2).
--%>
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
    <!-- Select2 (buscador de "Asignar a Grupo"): mismo CDN/tema que tutor/tutoria-individual.jsp,
         estilizado en global.css bajo .select2-container--bootstrap-5 para heredar los bordes
         redondeados de form-control-figma. -->
    <link href="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/css/select2.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/select2-bootstrap-5-theme@1.3.0/dist/select2-bootstrap-5-theme.min.css" rel="stylesheet">

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

        /* #matricula usa style="text-transform: uppercase" para que la matricula real se
           vea en mayusculas, pero ese transform tambien afecta al ::placeholder por
           herencia del navegador: sin este reset, el placeholder ("Se genera
           automaticamente al elegir el grupo") se veia en mayusculas sostenidas aunque el
           texto de origen ya esta en formato oracion normal, distinto al de #correo. */
        #matricula::placeholder {
            text-transform: none;
        }

        /* Deteccion de autofill del navegador (Chrome/Edge no disparan 'input' al
           autocompletar un campo): esta animacion no se ve, solo sirve como "gancho" para
           que formulario-alumno.js la escuche via el evento 'animationstart' y revalide el
           campo. Mismo patron que formulario-area.js. */
        @keyframes onAutoFillStart {
            from {}
            to {}
        }

        input:-webkit-autofill {
            animation-name: onAutoFillStart;
        }
    </style>
</head>
<body data-mensaje-error="${mensajeError}" data-context-path="${pageContext.request.contextPath}">

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

                <!-- ==================== SECCIÓN 1: INFORMACIÓN PERSONAL ==================== -->
                <div class="col-md-6">
                    <h5 class="fw-bold fs-6 mb-3 text-secondary border-bottom pb-2">Información Personal</h5>

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
                               pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$" required>
                        <div class="invalid-feedback">Solo se permiten letras y espacios.</div>
                    </div>

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
                        <label for="telefono" class="form-label fs-6 fw-bold">Teléfono</label>
                        <input type="text" id="telefono" name="telefono" class="form-control form-control-figma w-100 fs-6"
                               value="${alumnoFormulario.telefono}" placeholder="Escribe el teléfono"
                               pattern="^\d{10}$" maxlength="10" minlength="10"
                               oninput="this.value = this.value.replace(/[^0-9]/g, '')" required>
                        <div class="invalid-feedback">Debe contener exactamente 10 dígitos numéricos.</div>
                    </div>
                </div>

                <!-- ==================== SECCIÓN 2: INFORMACIÓN ACADÉMICA ==================== -->
                <div class="col-md-6">
                    <h5 class="fw-bold fs-6 mb-3 text-secondary border-bottom pb-2">Información Académica</h5>

                    <div class="mb-4">
                        <label for="asignarGrupo" class="form-label fs-6 fw-bold">Asignar a Grupo</label>
                        <!-- Reemplaza los selects individuales de Academia/Carrera/Cuatrimestre/Letra:
                             el grupo ya debe existir (se crea aparte desde gestion-grupos.jsp, modal
                             "Nuevo Grupo"), asi que aqui solo se elige entre los que ya hay. Al
                             cambiar, dispara la generacion de Matricula/Correo (o el desbloqueo
                             manual si es un alumno rezagado, ver alCambiarGrupo() en
                             formulario-alumno.js) segun el data-cuatri de la opcion elegida.
                             Searchable Select (Select2, ver formulario-alumno.js): necesario porque
                             la lista de grupos crece sin tope (una por Carrera+Cuatrimestre+Letra) y
                             un <select> nativo deja de ser usable pasados ~50. El texto del primer
                             <option> ya trae el mismo placeholder que se configura en JS, para que
                             el <select> nativo siga siendo legible si el CDN de Select2 no carga. -->
                        <select id="asignarGrupo" name="idGrupo" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" ${empty alumnoFormulario ? 'selected' : ''}>Escriba para buscar un grupo...</option>
                            <c:forEach var="grupo" items="${listaGrupos}">
                                <option value="${grupo.idGrupo}" data-cuatri="${grupo.cuatrimestre}"
                                    ${alumnoFormulario != null && alumnoFormulario.idGrupo == grupo.idGrupo ? 'selected' : ''}>
                                        ${grupo.nombreCarrera} - ${grupo.cuatrimestre}° ${grupo.letra} (Gen ${grupo.generacion})
                                </option>
                            </c:forEach>
                        </select>
                        <div class="invalid-feedback">Por favor seleccione un grupo.</div>
                    </div>

                    <div class="mb-4">
                        <label for="matricula" class="form-label fs-6 fw-bold">Matrícula</label>
                        <!-- readonly: la genera GET /generarCredenciales (GenerarCredencialesServlet)
                             al elegir "Asignar a Grupo". -->
                        <input type="text" id="matricula" name="matricula" class="form-control form-control-figma w-100 fs-6"
                               value="${alumnoFormulario.matricula}" placeholder="Se genera automáticamente al elegir el grupo"
                               style="text-transform: uppercase;"
                               maxlength="10" minlength="10" pattern="^[a-zA-Z0-9]{10}$"
                               oninput="this.value = this.value.replace(/[^a-zA-Z0-9]/g, '').toUpperCase()"
                               readonly required>
                        <div class="invalid-feedback">La matrícula debe tener exactamente 10 caracteres alfanuméricos.</div>
                    </div>

                    <div class="mb-4">
                        <label for="correo" class="form-label fs-6 fw-bold">Correo</label>
                        <!-- readonly: se autocompleta desde la Matricula al elegir "Asignar a Grupo"
                             (ver obtenerCredenciales() en formulario-alumno.js, que llama a
                             GET /generarCredenciales). -->
                        <input type="email" id="correo" name="correo" class="form-control form-control-figma w-100 fs-6"
                               value="${alumnoFormulario.correoInstitucional}"
                               placeholder="Se genera automáticamente al elegir el grupo" pattern="^[a-zA-Z0-9._\-]+@utez\.edu\.mx$"
                               oninput="this.value = this.value.replace(/[^a-zA-Z0-9.\-_@]/g, '')" readonly required>
                        <div class="invalid-feedback">El correo debe tener un formato válido y terminar en @utez.edu.mx.</div>
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

<jsp:include page="../includes/cargando.jsp" />
<jsp:include page="../includes/alertas.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/cargando.js"></script>
<!-- Select2 (buscador de "Asignar a Grupo"): jQuery debe cargar antes que Select2, y ambos
     antes que formulario-alumno.js, que los usa. Mismo orden que tutor/tutoria-individual.jsp. -->
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/js/select2.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/alumnos.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/formulario-alumno.js"></script>
</body>
</html>
