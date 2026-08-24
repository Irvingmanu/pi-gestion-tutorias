<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Registro de Tutoría Grupal</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/gestion-grupos.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/alertas.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/tutor/asistencia-grid.css" rel="stylesheet">
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-tutor.jsp" />

    <!-- ==================== CONTENIDO PRINCIPAL ==================== -->
    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="titulo-principal banner-grupos h5 mb-4">
            Registro de Tutoría Grupal
        </div>

        <c:choose>
            <c:when test="${empty gruposAsignados}">
                <div class="alert alert-warning text-center fs-5">
                    No tienes grupos asignados para tutoría.
                </div>
            </c:when>
            <c:otherwise>
                <div class="form-wrap-figma form-wrap-figma--ancho">
                    <!-- novalidate: quitamos los mensajes nativos del navegador, igual que en coordinador -->
                    <form id="formRegistroGrupal" class="needs-validation" action="${pageContext.request.contextPath}/tutoria-grupal" method="post" novalidate>
                        <!-- Bandera para el guardado parcial: cuando el tutor solo justifica/corrige
                             asistencia de sesiones ya registradas (sin llenar Fecha/Acuerdos/Temas
                             ni tocar la columna "Sesión actual"), registro-grupal.js la pone en "true"
                             para que el servidor NO cree una sesión nueva y solo actualice la asistencia. -->
                        <input type="hidden" id="inputSoloAsistencia" name="soloAsistencia" value="false">

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="grupoAsignado" class="form-label fs-6 fw-bold">Grupo</label>
                                <select id="grupoAsignado" name="idGrupo" class="form-select form-control-figma w-100 fs-6" required>
                                    <c:if test="${gruposAsignados.size() > 1}">
                                        <option value="" ${empty idGrupoPreseleccionado ? 'selected' : ''}>Seleccione el grupo</option>
                                    </c:if>
                                    <c:forEach var="grupo" items="${gruposAsignados}">
                                        <option value="${grupo.idGrupo}" ${idGrupoPreseleccionado == grupo.idGrupo ? 'selected' : ''}>
                                                ${grupo.nombreGrupo}
                                        </option>
                                    </c:forEach>
                                </select>
                                <div class="invalid-feedback">Selecciona un grupo.</div>
                            </div>
                            <div class="col-md-6">
                                <label for="fecha" class="form-label fs-6 fw-bold">Fecha</label>
                                <input type="date" id="fecha" name="fecha" class="form-control form-control-figma w-100 fs-6"
                                       <c:if test="${not empty periodoVigente}">min="<fmt:formatDate value="${periodoVigente.fechaInicio}" pattern="yyyy-MM-dd"/>"</c:if>
                                       required>
                                <div class="invalid-feedback">Selecciona una fecha válida.</div>
                            </div>
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="acuerdos" class="form-label fs-6 fw-bold">Acuerdos</label>
                                <textarea id="acuerdos" name="acuerdos" class="form-control form-control-figma w-100 fs-6"
                                          rows="3" placeholder="Describe los acuerdos alcanzados" required></textarea>
                                <div class="invalid-feedback">Este campo es obligatorio.</div>
                            </div>
                            <div class="col-md-6">
                                <label for="asesoriasGrupales" class="form-label fs-6 fw-bold">Asesorías grupales (Opcional)</label>
                                <input type="text" id="asesoriasGrupales" name="asesorias" class="form-control form-control-figma w-100 fs-6"
                                       placeholder="Describe las asesorías grupales">
                            </div>
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-12">
                                <label for="temasTratados" class="form-label fs-6 fw-bold">Temas Tratados</label>
                                <textarea id="temasTratados" name="temas" class="form-control form-control-figma w-100 fs-6"
                                          rows="3" placeholder="Describe los temas tratados en la sesión" required></textarea>
                                <div class="invalid-feedback">Este campo es obligatorio.</div>
                            </div>
                        </div>

                        <div id="contenedorAsistencia" class="mb-4" style="display: none;">
                            <p class="fs-5 fw-bold text-center my-3">Lista de Asistencia</p>
                            <p class="texto-ayuda-cuadricula text-center text-muted mb-3">
                                Haz clic sobre una celda de asistencia para alternar su estado. En sesiones de días
                                anteriores solo puedes Justificar (J) las faltas; si ya tiene Presente (✓) registrado
                                no se puede modificar. En la sesión de hoy solo puedes marcar Presente (✓) o Ausente (X).
                            </p>

                            <div class="tabla-asistencia-grid-wrap mb-3">
                                <table class="tabla-asistencia-grid" id="tablaCuadricula">
                                    <thead id="theadCuadricula"></thead>
                                    <tbody id="cuerpoTablaAsistencia"></tbody>
                                </table>
                            </div>

                            <div class="leyenda-asistencia">
                                <span class="leyenda-item"><span class="leyenda-icono estado-presente"></span> Presente</span>
                                <span class="leyenda-item"><span class="leyenda-icono estado-falta"></span> Ausente</span>
                                <span class="leyenda-item"><span class="leyenda-icono estado-justificado"></span> Justificado (no resta % de asistencia)</span>
                            </div>
                        </div>

                        <div class="d-flex justify-content-end mt-4">
                            <!-- Botón deshabilitado hasta que el formulario sea válido, igual que en coordinador -->
                            <button type="submit" id="btnGuardarGrupal" class="btn-figma fw-medium fs-5 px-4 py-2" disabled>Guardar</button>
                        </div>

                    </form>
                </div>
            </c:otherwise>
        </c:choose>

    </div>

</div>

<jsp:include page="../includes/alertas.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script>
    window.APP_CONTEXT = '${pageContext.request.contextPath}';
</script>
<!-- Referencia a tu nuevo script separado -->
<script src="${pageContext.request.contextPath}/assets/js/tutor/registro-grupal.js"></script>

</body>
</html>
