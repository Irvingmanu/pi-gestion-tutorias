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
            <div class="col-md-5">
                <label class="campo-label fs-6" for="buscarAlumno">Buscar Alumno</label>
                <input type="text" id="buscarAlumno" class="campo-buscar"
                       placeholder="Buscar por nombre o apellido">
            </div>
            <div class="col-md-5">
                <label class="campo-label fs-6" for="carrera">Carrera</label>
                <select id="carrera" class="campo-select">
                    <option value="" selected>Seleccione la carrera</option>
                    <c:forEach var="carrera" items="${listaCarreras}">
                        <option value="${carrera.nombre}">${carrera.nombre}</option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div class="row g-3 mb-3 align-items-end">
            <div class="col-md-5">
                <label class="campo-label fs-6" for="grupo">Grupo</label>
                <select id="grupo" class="campo-select">
                    <option value="" selected>Seleccione el Grupo</option>
                    <c:forEach var="letraGrupo" items="${listaLetrasGrupo}">
                        <option value="${letraGrupo.letra}">${letraGrupo.letra}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-5">
                <label class="campo-label fs-6" for="cuatrimestre">Cuatrimestre</label>
                <select id="cuatrimestre" class="campo-select">
                    <option value="" selected>Seleccione el cuatrimestre</option>
                    <c:forEach var="cuatrimestre" items="${listaCuatrimestres}">
                        <option value="${cuatrimestre.numero}">${cuatrimestre.numero}&deg;</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-2 text-center">
                <label class="campo-label fs-6">Nuevo Alumno</label>
                <a href="${pageContext.request.contextPath}/gestion-grupos?accion=nuevo" class="btn-figma text-decoration-none">Agregar</a>
            </div>
        </div>

        <!-- NUEVO: checkbox de inactivos + boton Buscar en la misma fila -->
        <div class="row g-3 mb-3 align-items-center">
            <div class="col-md-6 d-flex align-items-center gap-2">
                <input type="checkbox" id="mostrarInactivos" class="form-check-input">
                <label class="form-check-label fs-6" for="mostrarInactivos">Mostrar alumnos dados de baja</label>
            </div>
            <div class="col-md-6 text-md-end">
                <button type="button" id="btnBuscarAlumnos" class="btn-figma px-4">Buscar</button>
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
                        <tr class="${alumno.activo == 'N' ? 'fila-inactiva' : ''}"
                            data-nombre="${fn:toLowerCase(alumno.nombres)} ${fn:toLowerCase(alumno.apellidos)}"
                            data-carrera="${nombresCarrera[alumno.idCarrera]}"
                            data-cuatri="${numerosCuatrimestre[alumno.idCuatrimestre]}"
                            data-grupo="${nombresLetra[alumno.idLetraGrupo]}"
                            data-activo="${alumno.activo == 'N' ? 'N' : 'S'}">
                            <td>${alumno.matricula}</td>
                            <td>
                                    ${alumno.nombres} ${alumno.apellidos}
                                <c:if test="${alumno.activo == 'N'}">
                                    <span class="badge-inactivo">(Baja)</span>
                                </c:if>
                            </td>
                            <td>${alumno.correoInstitucional}</td>
                            <td>${nombresGenero[alumno.idGenero]}</td>
                            <td>${nombresCarrera[alumno.idCarrera]}</td>
                            <td>${numerosCuatrimestre[alumno.idCuatrimestre]}&deg; ${nombresLetra[alumno.idLetraGrupo]}</td>
                            <td>
                                <div class="d-flex justify-content-center gap-2">
                                    <c:if test="${alumno.activo != 'N'}">
                                        <a href="${pageContext.request.contextPath}/gestion-grupos?accion=prepararEdicion&matricula=${alumno.matricula}" class="btn-accion btn-editar">
                                            <img src="${pageContext.request.contextPath}/assets/img/coordinador/editar.png" width="16" alt="Editar">
                                        </a>
                                        <button type="button" class="btn-accion btn-eliminar" onclick="prepararEliminacion('${alumno.matricula}')">
                                            <img src="${pageContext.request.contextPath}/assets/img/coordinador/eliminar.png" width="16" alt="Eliminar">
                                        </button>
                                    </c:if>
                                    <c:if test="${alumno.activo == 'N'}">
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
        <div id="contenedorGruposAlumnos" class="mb-auto"></div>

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

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/alumnos.js"></script>
</body>
</html>