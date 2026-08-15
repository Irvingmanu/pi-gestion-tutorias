<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%
    request.setAttribute("paginaActiva", "grupos");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Gestión de Grupos</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/alertas.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <!-- ==================== BARRA LATERAL ==================== -->
    <jsp:include page="../includes/navbar-coordinador.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-3">
            Gestión de Grupos
        </div>

        <div class="row g-3 mb-3">
            <div class="col-md-4">
                <label class="campo-label fs-6" for="buscarAlumno">Buscar Alumno</label>
                <input type="text" id="buscarAlumno" class="campo-buscar"
                       placeholder="Buscar por nombre o apellido">
            </div>
            <div class="col-md-4">
                <label class="campo-label fs-6" for="academiaFiltroPrincipal">Academia</label>
                <!-- Filtro OPCIONAL: solo oculta opciones de #carreraFiltroPrincipal por JS,
                     igual que el filtro de Academia en formulario-alumno.jsp. -->
                <select id="academiaFiltroPrincipal" class="campo-select">
                    <option value="">Todas las academias</option>
                    <c:forEach var="academia" items="${listaAcademias}">
                        <option value="${academia.idAcademia}">${academia.nombre}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-4">
                <label class="campo-label fs-6" for="carreraFiltroPrincipal">Carrera</label>
                <!-- Habilitado desde el inicio, con TODAS las carreras del sistema (no solo
                     las que ya tienen grupos); value sigue siendo el NOMBRE de la carrera
                     porque asi es como filtrarAlumnos() compara contra data-carrera de cada
                     fila. data-academia-id es solo para el filtro de Academia de arriba. -->
                <select id="carreraFiltroPrincipal" class="campo-select">
                    <option value="" selected>Seleccione la carrera</option>
                    <c:forEach var="carrera" items="${listaCarreras}">
                        <option value="${carrera.nombre}" data-academia-id="${carrera.idAcademia}">${carrera.nombre}</option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div class="row g-3 mb-3 align-items-end">
            <div class="col-md-5">
                <label class="campo-label fs-6" for="grupo">Grupo</label>
                <select id="grupo" class="campo-select">
                    <option value="" selected>Seleccione el Grupo</option>
                </select>
            </div>
            <div class="col-md-5">
                <label class="campo-label fs-6" for="cuatrimestre">Cuatrimestre</label>
                <select id="cuatrimestre" class="campo-select">
                    <option value="" selected>Seleccione el cuatrimestre</option>
                </select>
            </div>
            <div class="col-md-2 text-center">
                <label class="campo-label fs-6">Nuevo Alumno</label>
                <a href="${pageContext.request.contextPath}/gestion-grupos?accion=nuevo" class="btn-figma text-decoration-none">Agregar</a>
            </div>
        </div>

        <div class="row g-3 mb-3">
            <div class="col-md-6 d-flex align-items-center gap-2">
                <input type="checkbox" id="mostrarInactivos" class="form-check-input">
                <label class="form-check-label fs-6" for="mostrarInactivos">Mostrar alumnos dados de baja</label>
            </div>
        </div>

        <!-- ==================== FUENTE DE DATOS OCULTA ====================
             El JSP renderiza la tabla igual que siempre; alumnos.js la lee
             de aqui (oculta) y pinta las tablas agrupadas mas abajo. -->
        <div class="table-responsive mb-auto" id="tablaOriginalAlumnos" style="display:none;">
            <c:if test="${empty listaAlumnos}">
                <div class="alert alert-info text-center">
                    No hay alumnos registrados todavía.
                </div>
            </c:if>
            <c:if test="${not empty listaAlumnos}">
                <table class="tabla-grupos fs-6">
                    <colgroup>
                        <col class="col-matricula">
                        <col class="col-nombre">
                        <col class="col-correo">
                        <col class="col-genero">
                        <col class="col-carrera">
                        <col class="col-carrera">
                        <col class="col-acciones">
                    </colgroup>
                    <thead>
                    <tr>
                        <th>Matricula</th>
                        <th>Nombre Completo</th>
                        <th>Correo</th>
                        <th>Genero</th>
                        <th>Carrera</th>
                        <th>Cuatri/Grupo</th>
                        <th>Acciones</th>
                    </tr>
                    </thead>
                    <tbody id="tablaAlumnosOriginal">
                    <c:forEach var="alumno" items="${listaAlumnos}">
                        <c:set var="grupoAlumno" value="${gruposPorId[alumno.idGrupo]}" />
                        <tr class="${alumno.estado == 'N' ? 'fila-inactiva' : ''}"
                            data-nombre="${fn:toLowerCase(alumno.nombres)} ${fn:toLowerCase(alumno.apellidos)}"
                            data-carrera="${grupoAlumno.nombreCarrera}"
                            data-cuatri="${grupoAlumno.cuatrimestre}"
                            data-grupo="${grupoAlumno.letra}"
                            data-grupo-id="${alumno.idGrupo}"
                            data-academia="${grupoAlumno.idAcademia}"
                            data-activo="${alumno.estado == 'N' ? 'N' : 'S'}">
                            <td>${alumno.matricula}</td>
                            <td>
                                    ${alumno.nombres} ${alumno.apellidos}
                                <c:if test="${alumno.estado == 'N'}">
                                    <span class="badge-inactivo">(Baja)</span>
                                </c:if>
                            </td>
                            <td>${alumno.correoInstitucional}</td>
                            <td>${nombresGenero[alumno.idGenero]}</td>
                            <td>${grupoAlumno.nombreCarrera}</td>
                            <td>${grupoAlumno.cuatrimestre}&deg; ${grupoAlumno.letra}</td>
                            <td>
                                <div class="d-flex justify-content-center gap-2">
                                    <c:if test="${alumno.estado != 'N'}">
                                        <a href="${pageContext.request.contextPath}/gestion-grupos?accion=prepararEdicion&matricula=${alumno.matricula}" class="btn-accion btn-editar">
                                            <img src="${pageContext.request.contextPath}/assets/img/coordinador/editar.png" width="16" alt="Editar">
                                        </a>
                                        <button type="button" class="btn-accion btn-eliminar" onclick="prepararEliminacion('${alumno.matricula}')">
                                            <img src="${pageContext.request.contextPath}/assets/img/coordinador/eliminar.png" width="16" alt="Eliminar">
                                        </button>
                                    </c:if>
                                    <c:if test="${alumno.estado == 'N'}">
                                        <button type="button" class="btn-accion btn-reactivar" onclick="prepararReactivacion('${alumno.matricula}')">
                                            <img src="${pageContext.request.contextPath}/assets/img/coordinador/reactivar.png" width="16" alt="Reactivar">
                                        </button>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:if>
        </div>

        <!-- ==================== TABLAS AGRUPADAS (Carrera + Cuatrimestre + Grupo) ====================
             Se generan dinamicamente en alumnos.js a partir de la fuente oculta de arriba. -->
        <div id="contenedorGruposAlumnos" class="mb-3"></div>

        <!-- ==================== ESTADO VACIO ====================
             #contenedorGruposAlumnos se reconstruye por completo en cada filtro (no existe
             un <tbody> unico y persistente como en asignacion.jsp), asi que esta fila vive
             en su propia tabla, oculta por defecto y sincronizada por alumnos.js. -->
        <table class="tabla-grupos fs-6" id="tablaSinResultados" style="display:none;">
            <thead>
            <tr>
                <th>Matricula</th>
                <th>Nombre Completo</th>
                <th>Correo</th>
                <th>Genero</th>
                <th>Carrera</th>
                <th>Cuatri/Grupo</th>
                <th>Acciones</th>
            </tr>
            </thead>
            <tbody>
            <tr id="filaSinResultados" style="display: none;">
                <td colspan="7" class="text-center text-muted">No se encontraron alumnos con los filtros seleccionados.</td>
            </tr>
            </tbody>
        </table>

    </div>

</div>

<form id="formEliminarAlumno" action="${pageContext.request.contextPath}/gestion-grupos" method="POST" style="display:none;">
    <input type="hidden" name="accion" value="eliminar">
    <input type="hidden" name="matricula" id="inputEliminarMatricula">
</form>

<form id="formReactivarAlumno" action="${pageContext.request.contextPath}/gestion-grupos" method="POST" style="display:none;">
    <input type="hidden" name="accion" value="reactivar">
    <input type="hidden" name="matricula" id="inputReactivarMatricula">
</form>

<jsp:include page="../includes/alertas.jsp" />

<script>
    // Tutor asignado por grupo, indexado por ID_GRUPO, usado en alumnos.js para
    // mostrarlo junto al titulo de cada tabla agrupada.
    window.tutoresPorGrupo = {
        <c:forEach var="entrada" items="${tutoresPorGrupo}" varStatus="status">
        "${entrada.key}": "${fn:replace(entrada.value, '\"', '\\\"')}"${status.last ? '' : ','}
        </c:forEach>
    };

    // Grupos que realmente existen en BD (GrupoDao.getAll()), usado en alumnos.js para
    // que los filtros de Cuatrimestre/Grupo solo ofrezcan combinaciones reales en vez
    // de un rango fijo (1-11 / A-F) que puede no tener ningun alumno.
    window.gruposExistentes = [
        <c:forEach var="g" items="${listaGrupos}" varStatus="status">
        {carrera: "${fn:replace(g.nombreCarrera, '\"', '\\\"')}", cuatri: "${g.cuatrimestre}", letra: "${g.letra}"}${status.last ? '' : ','}
        </c:forEach>
    ];
</script>
<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/alumnos.js"></script>
</body>
</html>