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
    <!-- Select2 (buscador de "Grupo" en el modal Carga Masiva): mismo CDN/tema que
         coordinador/formulario-alumno.jsp, estilizado en global.css bajo
         .select2-container--bootstrap-5. -->
    <link href="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/css/select2.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/select2-bootstrap-5-theme@1.3.0/dist/select2-bootstrap-5-theme.min.css" rel="stylesheet">
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

        <!-- ==================== CARGA MASIVA: filas invalidas de la ultima subida ====================
             AlumnoServlet.procesarCargaMasivaAlumnos() guarda esta lista en SESSION (no en
             la URL, para no saturarla con una lista larga de numeros de fila) cuando alguna
             fila del Excel se descarta. Se expone a JS via window.filasInvalidasExcel (ver
             el bloque de toasts en alumnos.js) y se borra de la sesion aqui mismo, para que
             no vuelva a salir si el coordinador recarga la pagina despues. -->
        <c:if test="${not empty sessionScope.filasInvalidasExcel}">
            <script>
                window.filasInvalidasExcel = [<c:forEach var="fila" items="${sessionScope.filasInvalidasExcel}" varStatus="st">${fila}${st.last ? '' : ', '}</c:forEach>];
            </script>
            <c:remove var="filasInvalidasExcel" scope="session" />
        </c:if>

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
            <div class="col-md-4">
                <label class="campo-label fs-6" for="grupo">Grupo</label>
                <select id="grupo" class="campo-select">
                    <option value="" selected>Seleccione el Grupo</option>
                </select>
            </div>
            <div class="col-md-3">
                <label class="campo-label fs-6" for="cuatrimestre">Cuatrimestre</label>
                <select id="cuatrimestre" class="campo-select">
                    <option value="" selected>Seleccione el cuatrimestre</option>
                </select>
            </div>
            <div class="col-md-2 text-center">
                <label class="campo-label fs-6">Nuevo Grupo</label>
                <button type="button" class="btn-figma w-100" data-bs-toggle="modal" data-bs-target="#modalNuevoGrupo">Nuevo Grupo</button>
            </div>
            <div class="col-md-3 text-center">
                <label class="campo-label fs-6">Añadir Alumnos</label>
                <div class="d-flex gap-2">
                    <a href="${pageContext.request.contextPath}/gestion-grupos?accion=nuevo" class="btn-figma text-decoration-none flex-fill">Agregar</a>
                    <button type="button" class="btn-figma flex-fill" data-bs-toggle="modal" data-bs-target="#modalCargaMasiva">Carga Masiva</button>
                </div>
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
                            data-generacion="${grupoAlumno.generacion}"
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

        <!-- ==================== ACORDEONES POR GRUPO (Carrera + Cuatrimestre + Grupo) ====================
             Se generan dinamicamente en alumnos.js a partir de la fuente oculta de arriba,
             uno por Grupo (Bootstrap 5 .accordion). Sin data-bs-parent en los .accordion-item
             (ver construirAcordeonGrupo en alumnos.js): permite tener varios grupos abiertos
             a la vez, necesario para que el buscador pueda abrir mas de uno con coincidencias. -->
        <div id="contenedorGruposAlumnos" class="accordion mb-3"></div>

        <!-- ==================== ESTADO VACIO ====================
             #contenedorGruposAlumnos se reconstruye por completo en cada filtro (no existe
             un <tbody> unico y persistente como en asignacion.jsp). Cuando no hay ningun
             alumno que coincida, se oculta la estructura de tabla por completo (ni
             encabezados) y se muestra esta tarjeta en su lugar; alumnos.js alterna entre
             una y otra segun haya o no resultados (ver renderizarGruposAlumnos() y
             aplicarBusquedaAlumno()). -->
        <div class="tarjeta-sin-resultados" id="tarjetaSinResultados" style="display:none;">
            No se encontraron alumnos con los filtros seleccionados.
        </div>

    </div>

</div>

<!-- ==================== MODAL: NUEVO GRUPO ====================
     Da de alta un grupo independiente (sin necesidad de registrar un alumno primero),
     reutilizando GrupoDao.findOrCreate() a traves de AlumnoServlet (accion=crearGrupo).
     La cascada Academia->Carrera->Cuatrimestre y la exclusion de letras ya usadas se
     resuelven en cliente (ver alumnos.js), pero la creacion real es un POST normal.
     data-bs-backdrop="static"/data-bs-keyboard="false": solo se cierra por los botones
     propios, para que intentarCerrarModalNuevoGrupo() siempre pueda confirmar primero. -->
<div class="modal fade" id="modalNuevoGrupo" tabindex="-1" aria-labelledby="modalNuevoGrupoLabel" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="formNuevoGrupo" action="${pageContext.request.contextPath}/gestion-grupos" method="POST" autocomplete="off">
                <input type="hidden" name="accion" value="crearGrupo">
                <div class="modal-header">
                    <h5 class="modal-title" id="modalNuevoGrupoLabel">Nuevo Grupo</h5>
                    <button type="button" class="btn-close" id="btnCerrarNuevoGrupo" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label fs-6 fw-bold" for="modalAcademia">Academia</label>
                        <select id="modalAcademia" class="form-select form-control-figma w-100 fs-6">
                            <option value="">Todas las academias</option>
                            <c:forEach var="academia" items="${listaAcademias}">
                                <option value="${academia.idAcademia}">${academia.nombre}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fs-6 fw-bold" for="modalCarrera">Carrera</label>
                        <select id="modalCarrera" name="idCarrera" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" selected>Seleccione la carrera</option>
                            <c:forEach var="carrera" items="${listaCarreras}">
                                <option value="${carrera.idCarrera}" data-nombre="${carrera.nombre}" data-academia-id="${carrera.idAcademia}" data-nivel="${carrera.nivel}">${carrera.nombre}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fs-6 fw-bold" for="modalCuatrimestre">Cuatrimestre</label>
                        <select id="modalCuatrimestre" name="cuatrimestre" class="form-select form-control-figma w-100 fs-6" required disabled>
                            <option value="" selected>Seleccione primero la carrera</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fs-6 fw-bold" for="modalLetra">Letra del Grupo</label>
                        <select id="modalLetra" name="letra" class="form-select form-control-figma w-100 fs-6" required disabled>
                            <option value="" selected>Seleccione primero el cuatrimestre</option>
                        </select>
                        <div class="form-text">Se excluyen las letras que ya existen para esa carrera y cuatrimestre; se preselecciona la primera libre.</div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fs-6 fw-bold" for="modalAnioInicio">Año de Inicio</label>
                        <!-- Reemplaza el input de texto libre "AAAA-AAAA": el Año de Fin ya no lo
                             escribe el coordinador (evitaba valores absurdos como "2999-2303"),
                             lo calcula el backend segun el Cuatrimestre elegido (TSU 1-6: +2 años;
                             Ingeniería 7-10: +1 año), ver crearGrupoIndependiente() en
                             AlumnoServlet. El valor sugerido, los limites min/max (año actual -5
                             a +1, para no permitir "2000" ni "2099") y el texto de ayuda con ese
                             mismo rango se fijan en JS al abrir el modal (alumnos.js), para que
                             el coordinador no tenga que adivinar que año es valido; el backend
                             valida el mismo rango otra vez por si acaso, por si se manda un POST
                             directo saltandose el HTML. autocomplete="off": el navegador puede
                             reponer aqui un año capturado en una prueba anterior (mismo name=
                             "anioInicio" reusado cada vez que se abre el modal) y pisar en
                             silencio la sugerencia de JS sin que se note a simple vista. -->
                        <input type="number" id="modalAnioInicio" name="anioInicio" class="form-control form-control-figma w-100 fs-6"
                               step="1" autocomplete="off" required>
                        <div class="form-text" id="modalAnioInicioAyuda"></div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fs-6 fw-bold" for="modalPeriodo">Periodo Escolar</label>
                        <!-- Poblado con ${listaPeriodos} (AlumnoServlet.doGet -> PeriodoEscolarDao.
                             getActivos()): solo periodos con ESTADO='S', cualquier año (no solo
                             el actual). accion=crearGrupo valida que el idPeriodo elegido
                             corresponda a uno de estos, no resuelve "el periodo vigente" solo. -->
                        <select id="modalPeriodo" name="idPeriodo" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" selected>Seleccione el periodo escolar</option>
                            <c:forEach var="periodo" items="${listaPeriodos}">
                                <option value="${periodo.idPeriodo}">${periodo.nombre}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-cancelar-figma" id="btnCancelarNuevoGrupo">Cancelar</button>
                    <button type="submit" class="btn-figma">Guardar</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- ==================== MODAL: CARGA MASIVA DE ALUMNOS ====================
     Un archivo Excel = un Grupo (elegido aqui, no en el Excel: mejora de UX para no pedir
     ID_GRUPO en la hoja). Columnas esperadas, orden de tabla plana (fila 1 = encabezados,
     datos desde fila 2): A) Matricula (opcional SOLO si el grupo es de 1er cuatrimestre;
     de 2do en adelante es OBLIGATORIA, para no autogenerarle una matricula nueva a un
     alumno de reingreso que ya tiene la suya) B) Nombres C) Apellido paterno
     D) Apellido materno (requerido) E) Correo (opcional, se autogenera) F) Genero
     ("Masculino"/"Femenino"/"Otro") G) Telefono, 10 digitos (requerido).
     Ver AlumnoServlet.procesarCargaMasivaAlumnos(). -->
<div class="modal fade" id="modalCargaMasiva" tabindex="-1" aria-labelledby="modalCargaMasivaLabel" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="formCargaMasivaAlumnos" action="${pageContext.request.contextPath}/gestion-grupos" method="POST" enctype="multipart/form-data">
                <input type="hidden" name="accion" value="cargaMasivaAlumnos">
                <div class="modal-header">
                    <h5 class="modal-title" id="modalCargaMasivaLabel">Carga Masiva de Alumnos</h5>
                    <button type="button" class="btn-close" id="btnCerrarCargaMasiva" aria-label="Cerrar"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label fs-6 fw-bold" for="cargaMasivaGrupo">Grupo</label>
                        <!-- Reusa ${listaGrupos} (ya la carga AlumnoServlet.doGet con
                             GrupoDao.getAll(), solo grupos activos), mismo patron que
                             "Asignar a Grupo" en formulario-alumno.jsp. Searchable Select
                             (Select2, ver alumnos.js): util si hay muchos grupos. El texto
                             del primer <option> ya trae el mismo placeholder que se
                             configura en JS, para que el <select> nativo siga siendo
                             legible si el CDN de Select2 no carga. -->
                        <select id="cargaMasivaGrupo" name="idGrupo" class="form-select form-control-figma w-100 fs-6" required>
                            <option value="" selected>Escriba para buscar un grupo...</option>
                            <c:forEach var="grupo" items="${listaGrupos}">
                                <option value="${grupo.idGrupo}">${grupo.nombreCarrera} - ${grupo.cuatrimestre}° ${grupo.letra} (Gen ${grupo.generacion})</option>
                            </c:forEach>
                        </select>
                        <div class="form-text">Todos los alumnos del archivo se asignan a este grupo.</div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fs-6 fw-bold" for="cargaMasivaArchivo">Archivo Excel (.xlsx, .xls)</label>
                        <input type="file" id="cargaMasivaArchivo" name="archivoExcel" class="form-control form-control-figma w-100 fs-6"
                               accept=".xlsx,.xls" required>
                        <div class="form-text">
                            Columnas: A) Matrícula &middot; B) Nombres &middot; C) Apellido paterno &middot;
                            D) Apellido materno &middot; E) Correo (opcional) &middot; F) Género (Masculino/Femenino/Otro) &middot;
                            G) Teléfono (10 dígitos). La primera fila es de encabezados.
                            <strong>Matrícula:</strong> opcional (se autogenera) solo si el grupo elegido es de 1er cuatrimestre;
                            para 2° en adelante es obligatoria (alumnos de reingreso ya tienen la suya).
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-cancelar-figma" id="btnCancelarCargaMasiva">Cancelar</button>
                    <button type="submit" class="btn-figma">Subir archivo</button>
                </div>
            </form>
        </div>
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
<!-- Select2 (buscador de "Grupo" en el modal Carga Masiva): jQuery debe cargar antes que
     Select2, y ambos antes que alumnos.js, que los usa. -->
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/js/select2.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/coordinador/alumnos.js"></script>
</body>
</html>