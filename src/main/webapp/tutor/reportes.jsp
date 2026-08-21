<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sistema de Gestión de Tutorías - Reportes</title>
    <link href="${pageContext.request.contextPath}/assets/css/bootstrap.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/bi/bootstrap-icons.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/global.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/coordinador/navbar.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/alertas.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>
    <style>
        .card-grafica {
            min-height: 340px;
            display: flex;
            flex-direction: column;
        }
        .card-grafica .grafica-contenido {
            flex-grow: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
        }
        .card-grafica canvas {
            max-height: 260px;
        }
        .grafica-vacio {
            color: #9ca3af;
            font-size: 0.95rem;
            text-align: center;
        }

        #filtroDesde, #filtroHasta {
            cursor: pointer;
        }
        #filtroDesde:hover, #filtroHasta:hover {
            border-color: var(--borde-campo);
        }
        #filtroDesde:focus, #filtroHasta:focus {
            border-color: var(--utez-green);
            box-shadow: 0 0 0 0.15rem rgba(0, 139, 116, 0.25);
        }

        .tarjeta-kpi-clickeable {
            cursor: pointer;
            transition: box-shadow .15s ease, transform .15s ease;
        }
        .tarjeta-kpi-clickeable:hover, .tarjeta-kpi-clickeable:focus-visible {
            box-shadow: 0 .5rem 1rem rgba(0,0,0,.1) !important;
            transform: translateY(-2px);
        }
        .badge-al-dia {
            background-color: #E1F3E9;
            color: #008B74;
        }
        .badge-en-riesgo {
            background-color: #FBE2E1;
            color: #DC2626;
        }
        .badge-sin-objetivo {
            background-color: #E5E7EB;
            color: #374151;
        }
        /* Reutilizadas por todos los modales de "Ver detalles" (Tutorías Grupales,
           Alumnos Atendidos, Canalizados...) para partir la ficha en secciones. */
        .seccion-detalle-titulo {
            font-weight: 600;
            color: #0B1C30;
            font-size: .95rem;
            margin-bottom: .75rem;
        }
        .seccion-detalle-card {
            background-color: #F8FAFC;
            border-radius: var(--radius-figma, 12px);
            padding: 1rem 1.25rem;
        }
        .seccion-detalle-card + .seccion-detalle-card {
            margin-top: 1rem;
        }
        .campo-detalle-label {
            color: #6B7280;
            font-size: .8rem;
            margin-bottom: .15rem;
        }
        .campo-detalle-valor {
            margin-bottom: .85rem;
            white-space: pre-wrap;
        }
        .btn-volver-figma {
            color: var(--utez-green) !important;
            text-decoration: none;
        }
        .btn-volver-figma:hover, .btn-volver-figma:focus {
            color: var(--utez-green) !important;
            text-decoration: underline;
        }
        .buscador-alumno-wrap {
            position: relative;
        }
        #resultadosBuscadorAlumno {
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            z-index: 1050;
            max-height: 280px;
            overflow-y: auto;
            box-shadow: 0 .5rem 1rem rgba(0,0,0,.15);
            border-radius: var(--radius-figma, 12px);
        }
    </style>
</head>
<body>

<div class="container-fluid min-vh-100 d-flex p-4 gap-4">

    <jsp:include page="../includes/navbar-tutor.jsp" />

    <div class="flex-grow-1 px-4 py-2 d-flex flex-column">

        <h2 class="titulo-principal h5 mb-3 mt-2">Sistema de Gestión de Tutorías</h2>

        <div class="banner-grupos h5 mb-4">
            Reportes
        </div>

        <div class="row g-3 mb-2">
            <div class="col-md-6">
                <label for="filtroGrupoAsignado" class="form-label fw-bold">Grupo Asignado</label>
                <select id="filtroGrupoAsignado" class="form-select form-control-figma">
                    <option value="">Todos mis grupos</option>
                    <c:forEach var="grupo" items="${listaGruposTutor}">
                        <option value="${grupo.idGrupo}"
                                data-id-carrera="${grupo.idCarrera}"
                                data-nombre-carrera="${grupo.nombreCarrera}"
                                data-cuatrimestre="${grupo.cuatrimestre}"
                                data-letra="${grupo.letra}">
                                ${grupo.nombreGrupo}
                        </option>
                    </c:forEach>
                </select>
                <c:if test="${empty listaGruposTutor}">
                    <div class="form-text text-muted">No tienes grupos asignados actualmente.</div>
                </c:if>
            </div>
            <!-- Los tres selects de abajo ya no son visibles/editables por el tutor: se sincronizan
                 en JS desde #filtroGrupoAsignado (ver mas abajo) para no tener que tocar el resto de
                 filtrosReporteActuales()/construirParamsExport()/cargarReporte(), que ya leian estos
                 mismos IDs. Antes eran 3 <select> independientes (Carrera con TODAS las carreras del
                 sistema, Cuatrimestre 1-10, Grupo A-F) que dejaban al tutor armar cualquier combinacion,
                 incluidas las que no le pertenecen -- ahora solo puede elegir entre sus propios grupos
                 asignados (ASIGNACION_TUTOR), listados arriba. -->
            <select id="filtroCuatrimestre" class="d-none" aria-hidden="true"></select>
            <select id="filtroGrupo" class="d-none" aria-hidden="true"></select>
            <select id="filtroCarrera" class="d-none" aria-hidden="true"></select>
        </div>

        <div class="row g-3 mb-4 align-items-end">
            <div class="col-md-4">
                <label for="filtroDesde" class="form-label fw-bold">Desde</label>
                <input type="date" id="filtroDesde" class="form-control form-control-figma">
            </div>
            <div class="col-md-4">
                <label for="filtroHasta" class="form-label fw-bold">Hasta</label>
                <input type="date" id="filtroHasta" class="form-control form-control-figma">
            </div>
            <div class="col-md-4">
                <button type="button" id="btnBuscar" class="btn btn-figma w-100">Buscar</button>
            </div>
        </div>

        <div id="avisoReporte" class="alert alert-warning d-none" role="alert"></div>

        <div class="row g-3 mb-3">
            <div class="col-md-6">
                <label for="buscadorAlumno" class="form-label fw-bold">Buscar alumno (nombre o matrícula)</label>
                <div class="buscador-alumno-wrap">
                    <input type="text" id="buscadorAlumno" class="form-control form-control-figma"
                           placeholder="Ej. Juan Pérez o EN2312345" autocomplete="off">
                    <div id="resultadosBuscadorAlumno" class="list-group d-none"></div>
                </div>
                <input type="hidden" id="filtroMatricula" value="">
                <div id="filtroAlumnoActivo" class="alert alert-info d-none d-flex justify-content-between align-items-center py-2 px-3 mt-2 mb-0">
                    <span>Mostrando el reporte de: <strong id="nombreAlumnoFiltro"></strong> (<span id="matriculaAlumnoFiltro"></span>)</span>
                    <button type="button" class="btn btn-sm btn-outline-secondary" id="btnQuitarFiltroAlumno">Quitar filtro</button>
                </div>
            </div>
        </div>

        <div class="row g-3 mb-3">
            <div class="col-md-3">
                <div class="p-3 bg-white rounded-figma shadow-sm border d-flex align-items-center gap-3 tarjeta-kpi-clickeable"
                     id="cardAlumnosAtendidos" role="button" tabindex="0">
                    <div class="rounded-circle d-flex justify-content-center align-items-center"
                         style="width:44px; height:44px; background-color:#008B74;">
                        <i class="bi bi-person-check text-white fs-5"></i>
                    </div>
                    <div>
                        <div class="text-muted small">Tutorías Individuales</div>
                        <div class="fw-bold fs-4" id="kpiAtendidos">--</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="p-3 bg-white rounded-figma shadow-sm border d-flex align-items-center gap-3 tarjeta-kpi-clickeable"
                     id="cardTutoriasGrupales" role="button" tabindex="0">
                    <div class="rounded-circle d-flex justify-content-center align-items-center"
                         style="width:44px; height:44px; background-color:#008B74;">
                        <i class="bi bi-people text-white fs-5"></i>
                    </div>
                    <div>
                        <div class="text-muted small">Tutorías Grupales</div>
                        <div class="fw-bold fs-4" id="kpiGruposAtendidos">--</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="p-3 bg-white rounded-figma shadow-sm border d-flex align-items-center gap-3 tarjeta-kpi-clickeable"
                     id="cardCanalizados" role="button" tabindex="0">
                    <div class="rounded-circle d-flex justify-content-center align-items-center"
                         style="width:44px; height:44px; background-color:#008B74;">
                        <i class="bi bi-signpost-split text-white fs-5"></i>
                    </div>
                    <div>
                        <div class="text-muted small">Canalizaciones</div>
                        <div class="fw-bold fs-4" id="kpiCanalizados">--</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="p-3 bg-white rounded-figma shadow-sm border d-flex align-items-center gap-3 tarjeta-kpi-clickeable"
                     id="cardPendientes" role="button" tabindex="0">
                    <div class="rounded-circle d-flex justify-content-center align-items-center"
                         style="width:44px; height:44px; background-color:#008B74;">
                        <i class="bi bi-hourglass-split text-white fs-5"></i>
                    </div>
                    <div>
                        <div class="text-muted small">Pendientes</div>
                        <div class="fw-bold fs-4" id="kpiPendientes">--</div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row g-3">
            <div class="col-md-6">
                <div class="p-3 bg-white rounded-figma shadow-sm border card-grafica">
                    <div class="fw-bold mb-2">Distribución de Alumnos Canalizados</div>
                    <div class="grafica-contenido">
                        <canvas id="graficaPastel"></canvas>
                        <div id="pastelVacio" class="grafica-vacio d-none">
                            No hay canalizaciones registradas en este periodo.
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="p-3 bg-white rounded-figma shadow-sm border card-grafica">
                    <div class="fw-bold mb-2">Estado de Reportes (General)</div>
                    <div class="grafica-contenido">
                        <canvas id="graficaBarras"></canvas>
                    </div>
                </div>
            </div>
        </div>

        <div class="d-flex justify-content-end mt-4 gap-2">
            <button type="button" id="btnExportarExcel" class="btn btn-figma">
                <i class="bi bi-file-earmark-excel"></i> Exportar Excel
            </button>
            <button type="button" id="btnExportarPdf" class="btn btn-figma">
                <i class="bi bi-file-earmark-pdf"></i> Exportar PDF
            </button>
        </div>

    </div>

</div>

<!-- ==================== MODAL: Tutorías Grupales ==================== -->
<div class="modal fade" id="modalTutoriasGrupales" tabindex="-1" aria-labelledby="tituloModalTutoriasGrupales" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content" style="border-radius: var(--radius-figma, 16px); border: none;">
            <div class="modal-header">
                <div>
                    <h5 class="modal-title fw-bold" id="tituloModalTutoriasGrupales">Tutorías Grupales</h5>
                    <div class="text-muted small" id="subtituloModalTutoriasGrupales">Avance de tus grupos frente al objetivo del periodo vigente.</div>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                <div id="avisoSinPeriodoGrupal" class="aviso-inline aviso-inline-advertencia d-none" role="alert">
                    No hay un periodo escolar vigente, por lo que no es posible calcular tu avance de tutorías grupales.
                </div>
                <div class="d-flex justify-content-end mb-3">
                    <a href="${pageContext.request.contextPath}/tutoria-grupal" class="btn btn-figma">
                        <i class="bi bi-journal-plus"></i> Registrar Tutoría Grupal
                    </a>
                </div>
                <div class="table-responsive">
                    <table class="table table-sm align-middle mb-0">
                        <thead>
                        <tr>
                            <th>Grupo Asignado</th>
                            <th>Tutorías Impartidas</th>
                            <th>Estatus</th>
                            <th class="text-center">Acciones</th>
                        </tr>
                        </thead>
                        <tbody id="tablaTutoriasGrupalesBody">
                        <tr><td colspan="4" class="text-center text-muted">Cargando...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="modalDetalleSesionGrupal" tabindex="-1" aria-labelledby="tituloModalDetalleSesionGrupal" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content" style="border-radius: var(--radius-figma, 16px); border: none;">
            <div class="modal-header">
                <div>
                    <button type="button" class="btn btn-sm btn-volver-figma ps-0" id="btnVolverListaTutorias">
                        <i class="bi bi-arrow-left"></i> Volver al seguimiento
                    </button>
                    <h5 class="modal-title fw-bold mb-0" id="tituloModalDetalleSesionGrupal">Detalle de sesiones</h5>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">

                <div class="seccion-detalle-card mb-4">
                    <div class="seccion-detalle-titulo">Grupo y avance</div>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Grupo Asignado</div>
                            <div class="campo-detalle-valor fw-semibold" id="detalleGrupoAsignado">--</div>
                        </div>
                        <div class="col-md-3">
                            <div class="campo-detalle-label">Tutorías Impartidas</div>
                            <div class="campo-detalle-valor fw-semibold" id="detalleAvance">--</div>
                        </div>
                        <div class="col-md-3">
                            <div class="campo-detalle-label">Estatus</div>
                            <div class="campo-detalle-valor" id="detalleEstatus">--</div>
                        </div>
                    </div>
                </div>

                <div id="avisoSinSesiones" class="aviso-inline aviso-inline-advertencia d-none" role="alert">
                    Todavía no has registrado sesiones grupales para este grupo en el periodo vigente.
                </div>
                <div id="listaSesionesGrupales"></div>

            </div>
        </div>
    </div>
</div>

<!-- ==================== MODAL: Alumnos Atendidos ==================== -->
<div class="modal fade" id="modalAlumnosAtendidos" tabindex="-1" aria-labelledby="tituloModalAlumnosAtendidos" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-scrollable">
        <div class="modal-content" style="border-radius: var(--radius-figma, 16px); border: none;">
            <div class="modal-header">
                <div>
                    <h5 class="modal-title fw-bold" id="tituloModalAlumnosAtendidos">Alumnos Atendidos</h5>
                    <div class="text-muted small">Tutorías Individuales y Espontáneas completadas (no incluye tutorías grupales).</div>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                <div id="avisoSinAtenciones" class="aviso-inline aviso-inline-advertencia d-none" role="alert">
                    No hay tutorías individuales o espontáneas registradas con los filtros seleccionados.
                </div>
                <div class="table-responsive">
                    <table class="table table-sm align-middle mb-0">
                        <thead>
                        <tr>
                            <th>Tipo</th>
                            <th>Fecha y Hora</th>
                            <th>Grupo</th>
                            <th>Alumno</th>
                            <th>Estado</th>
                            <th class="text-center">Acción</th>
                        </tr>
                        </thead>
                        <tbody id="tablaAlumnosAtendidosBody">
                        <tr><td colspan="6" class="text-center text-muted">Cargando...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="modalDetalleAtencion" tabindex="-1" aria-labelledby="tituloModalDetalleAtencion" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content" style="border-radius: var(--radius-figma, 16px); border: none;">
            <div class="modal-header">
                <div>
                    <button type="button" class="btn btn-sm btn-volver-figma ps-0" id="btnVolverListaAtenciones">
                        <i class="bi bi-arrow-left"></i> Volver al listado
                    </button>
                    <h5 class="modal-title fw-bold mb-0" id="tituloModalDetalleAtencion">Detalle de la tutoría</h5>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                <div class="seccion-detalle-card">
                    <div class="seccion-detalle-titulo">Detalles de la Sesión</div>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Grupo</div>
                            <div class="campo-detalle-valor fw-semibold" id="atencionDetalleGrupo">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Alumno</div>
                            <div class="campo-detalle-valor fw-semibold" id="atencionDetalleAlumno">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Fecha</div>
                            <div class="campo-detalle-valor fw-semibold" id="atencionDetalleFecha">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Hora</div>
                            <div class="campo-detalle-valor fw-semibold" id="atencionDetalleHora">--</div>
                        </div>
                        <div class="col-12">
                            <div class="campo-detalle-label">Temas Tratados</div>
                            <div class="campo-detalle-valor" id="atencionDetalleTemas">--</div>
                        </div>
                        <div class="col-12">
                            <div class="campo-detalle-label">Acuerdos</div>
                            <div class="campo-detalle-valor mb-0" id="atencionDetalleAcuerdos">--</div>
                        </div>
                    </div>
                </div>
                <div class="seccion-detalle-card">
                    <div class="seccion-detalle-titulo">Vínculo Directo</div>
                    <div class="campo-detalle-valor mb-0" id="atencionDetalleVinculo">--</div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- ==================== MODAL: Canalizados ==================== -->
<div class="modal fade" id="modalCanalizados" tabindex="-1" aria-labelledby="tituloModalCanalizados" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-scrollable">
        <div class="modal-content" style="border-radius: var(--radius-figma, 16px); border: none;">
            <div class="modal-header">
                <div>
                    <h5 class="modal-title fw-bold" id="tituloModalCanalizados">Canalizados</h5>
                    <div class="text-muted small">Alumnos que has derivado a algún área de apoyo (Psicología, Servicio Médico, Becas, etc.).</div>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                <div id="avisoSinCanalizaciones" class="aviso-inline aviso-inline-advertencia d-none" role="alert">
                    No hay canalizaciones registradas con los filtros seleccionados.
                </div>
                <div class="table-responsive">
                    <table class="table table-sm align-middle mb-0">
                        <thead>
                        <tr>
                            <th>Alumno</th>
                            <th>Grupo</th>
                            <th>Fecha de Canalización</th>
                            <th>Área de Canalización</th>
                            <th>Estatus</th>
                            <th class="text-center">Acción</th>
                        </tr>
                        </thead>
                        <tbody id="tablaCanalizadosBody">
                        <tr><td colspan="6" class="text-center text-muted">Cargando...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="modalDetalleCanalizacion" tabindex="-1" aria-labelledby="tituloModalDetalleCanalizacion" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content" style="border-radius: var(--radius-figma, 16px); border: none;">
            <div class="modal-header">
                <div>
                    <button type="button" class="btn btn-sm btn-volver-figma ps-0" id="btnVolverListaCanalizados">
                        <i class="bi bi-arrow-left"></i> Volver al listado
                    </button>
                    <h5 class="modal-title fw-bold mb-0" id="tituloModalDetalleCanalizacion">Detalle de la canalización</h5>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                <div class="seccion-detalle-card">
                    <div class="seccion-detalle-titulo">Datos del Alumno</div>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Alumno</div>
                            <div class="campo-detalle-valor fw-semibold" id="canalizacionDetalleAlumno">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Grupo</div>
                            <div class="campo-detalle-valor fw-semibold" id="canalizacionDetalleGrupo">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Fecha de Canalización</div>
                            <div class="campo-detalle-valor fw-semibold" id="canalizacionDetalleFecha">--</div>
                        </div>
                    </div>
                </div>
                <div class="seccion-detalle-card">
                    <div class="seccion-detalle-titulo">Datos de la Canalización</div>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Área de Canalización</div>
                            <div class="campo-detalle-valor fw-semibold" id="canalizacionDetalleArea">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Estatus</div>
                            <div class="campo-detalle-valor fw-semibold" id="canalizacionDetalleEstatus">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Motivo</div>
                            <div class="campo-detalle-valor fw-semibold" id="canalizacionDetalleMotivo">--</div>
                        </div>
                        <div class="col-12">
                            <div class="campo-detalle-label">Observaciones</div>
                            <div class="campo-detalle-valor mb-0" id="canalizacionDetalleObservaciones">--</div>
                        </div>
                    </div>
                    <div class="text-end">
                        <button type="button" class="btn btn-figma d-none" id="btnRecordarAreaApoyo">
                            Enviar correo de recordatorio
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- ==================== MODAL: Pendientes ==================== -->
<div class="modal fade" id="modalSolicitudesPendientes" tabindex="-1" aria-labelledby="tituloModalSolicitudesPendientes" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-scrollable">
        <div class="modal-content" style="border-radius: var(--radius-figma, 16px); border: none;">
            <div class="modal-header">
                <div>
                    <h5 class="modal-title fw-bold" id="tituloModalSolicitudesPendientes">Solicitudes Pendientes</h5>
                    <div class="text-muted small">Solicitudes de tutoría de tus alumnos que aún no han sido procesadas.</div>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                <div class="d-flex justify-content-end mb-3">
                    <a href="${pageContext.request.contextPath}/solicitudes" class="btn btn-figma">
                        Ir a Solicitudes
                    </a>
                </div>
                <div id="avisoSinPendientes" class="aviso-inline aviso-inline-advertencia d-none" role="alert">
                    No hay solicitudes pendientes con los filtros seleccionados.
                </div>
                <div class="table-responsive">
                    <table class="table table-sm align-middle mb-0">
                        <thead>
                        <tr>
                            <th>Alumno</th>
                            <th>Asunto</th>
                            <th>Fecha Propuesta</th>
                            <th>Estatus</th>
                            <th class="text-center">Acción</th>
                        </tr>
                        </thead>
                        <tbody id="tablaPendientesBody">
                        <tr><td colspan="5" class="text-center text-muted">Cargando...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="modalDetalleSolicitudPendiente" tabindex="-1" aria-labelledby="tituloModalDetalleSolicitudPendiente" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content" style="border-radius: var(--radius-figma, 16px); border: none;">
            <div class="modal-header">
                <div>
                    <button type="button" class="btn btn-sm btn-volver-figma ps-0" id="btnVolverListaPendientes">
                        <i class="bi bi-arrow-left"></i> Volver al listado
                    </button>
                    <h5 class="modal-title fw-bold mb-0" id="tituloModalDetalleSolicitudPendiente">Detalle de la solicitud</h5>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                <div class="seccion-detalle-card">
                    <div class="seccion-detalle-titulo">Datos de la Solicitud</div>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Nombre del Alumno</div>
                            <div class="campo-detalle-valor fw-semibold" id="pendienteDetalleAlumno">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Matrícula</div>
                            <div class="campo-detalle-valor fw-semibold" id="pendienteDetalleMatricula">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Asunto</div>
                            <div class="campo-detalle-valor fw-semibold" id="pendienteDetalleAsunto">--</div>
                        </div>
                        <div class="col-md-6">
                            <div class="campo-detalle-label">Fecha Propuesta</div>
                            <div class="campo-detalle-valor fw-semibold" id="pendienteDetalleFecha">--</div>
                        </div>
                        <div class="col-12">
                            <div class="campo-detalle-label">Descripción</div>
                            <div class="campo-detalle-valor mb-0" id="pendienteDetalleDescripcion">--</div>
                        </div>
                    </div>
                </div>
                <div class="text-end">
                    <a href="${pageContext.request.contextPath}/solicitudes" class="btn btn-figma">
                        Ir a Solicitudes
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Inclusión de los modales unificados -->
<jsp:include page="../includes/alertas.jsp" />

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/tutor/reportes.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/alertas.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/tutor/tutorias-grupales-modal.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/tutor/alumnos-atendidos-modal.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/tutor/canalizados-modal.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/tutor/pendientes-modal.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/tutor/buscador-alumno.js"></script>
<script>
    const CONTEXT_PATH = "${pageContext.request.contextPath}";
    let graficaBarrasTutor = null;

    function pintarBarrasTutor(data) {
        const ctx = document.getElementById('graficaBarras');

        if (graficaBarrasTutor) graficaBarrasTutor.destroy();
        graficaBarrasTutor = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Atendidos', 'Canalizaciones', 'Asistencias'],
                datasets: [{
                    data: [data.totalAtendidos, data.totalCanalizados, data.totalAsistencias],
                    backgroundColor: ['#8FD9C4', '#7FA8C9', '#0B2544']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
            }
        });
    }

    function actualizarEstadoPastel(data) {
        const canvas = document.getElementById('graficaPastel');
        const aviso = document.getElementById('pastelVacio');
        const distribucion = data.distribucionCanalizados || [];

        if (distribucion.length === 0) {
            canvas.classList.add('d-none');
            aviso.classList.remove('d-none');
        } else {
            canvas.classList.remove('d-none');
            aviso.classList.add('d-none');
        }
    }

    function buscarReporteTutor() {
        cargarReporte({
            contextPath: CONTEXT_PATH,
            filtros: ['filtroCarrera', 'filtroCuatrimestre', 'filtroGrupo', 'filtroDesde', 'filtroHasta', 'filtroMatricula'],
            onDatos: function (data) {
                pintarBarrasTutor(data);
                actualizarEstadoPastel(data);
            }
        });
    }

    document.getElementById('btnBuscar').addEventListener('click', buscarReporteTutor);
    document.addEventListener('DOMContentLoaded', buscarReporteTutor);

    // #filtroGrupoAsignado solo ofrece los grupos que ASIGNACION_TUTOR le dio a este tutor
    // (ver ReportesServlet, rama que hace forward a esta vista). Al elegir uno, se replican
    // su carrera/cuatrimestre/letra en los <select> ocultos de siempre (filtroCarrera,
    // filtroCuatrimestre, filtroGrupo) para no duplicar la logica de busqueda/exportacion
    // que ya lee esos IDs.
    function fijarSelectOculto(select, valor, texto) {
        select.innerHTML = '';
        if (valor) {
            const opcion = document.createElement('option');
            opcion.value = valor;
            opcion.text = texto;
            select.appendChild(opcion);
        }
        select.value = valor || '';
    }

    document.getElementById('filtroGrupoAsignado').addEventListener('change', function () {
        const opcionElegida = this.options[this.selectedIndex];
        fijarSelectOculto(document.getElementById('filtroCarrera'),
            opcionElegida.dataset.idCarrera || '', opcionElegida.dataset.nombreCarrera || '');
        fijarSelectOculto(document.getElementById('filtroCuatrimestre'),
            opcionElegida.dataset.cuatrimestre || '', (opcionElegida.dataset.cuatrimestre || '') + '°');
        fijarSelectOculto(document.getElementById('filtroGrupo'),
            opcionElegida.dataset.letra || '', opcionElegida.dataset.letra || '');
        buscarReporteTutor();
    });

    function habilitarClickCompletoFecha(id) {
        const input = document.getElementById(id);
        input.addEventListener('click', function () {
            if (typeof input.showPicker === 'function') {
                input.showPicker();
            }
        });
    }

    habilitarClickCompletoFecha('filtroDesde');
    habilitarClickCompletoFecha('filtroHasta');

    function filtrosReporteActuales() {
        return {
            idCarrera: document.getElementById('filtroCarrera').value,
            cuatrimestre: document.getElementById('filtroCuatrimestre').value,
            letra: document.getElementById('filtroGrupo').value,
            desde: document.getElementById('filtroDesde').value,
            hasta: document.getElementById('filtroHasta').value,
            matricula: document.getElementById('filtroMatricula').value
        };
    }

    document.getElementById('cardTutoriasGrupales').addEventListener('click', abrirModalTutoriasGrupales);
    document.getElementById('cardTutoriasGrupales').addEventListener('keydown', function (e) {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            abrirModalTutoriasGrupales();
        }
    });

    function abrirModalAlumnosAtendidosConFiltros() {
        abrirModalAlumnosAtendidos(filtrosReporteActuales());
    }
    document.getElementById('cardAlumnosAtendidos').addEventListener('click', abrirModalAlumnosAtendidosConFiltros);
    document.getElementById('cardAlumnosAtendidos').addEventListener('keydown', function (e) {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            abrirModalAlumnosAtendidosConFiltros();
        }
    });

    function abrirModalCanalizadosConFiltros() {
        abrirModalCanalizados(filtrosReporteActuales());
    }
    document.getElementById('cardCanalizados').addEventListener('click', abrirModalCanalizadosConFiltros);
    document.getElementById('cardCanalizados').addEventListener('keydown', function (e) {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            abrirModalCanalizadosConFiltros();
        }
    });

    function abrirModalPendientesConFiltros() {
        abrirModalPendientes(filtrosReporteActuales());
    }
    document.getElementById('cardPendientes').addEventListener('click', abrirModalPendientesConFiltros);
    document.getElementById('cardPendientes').addEventListener('keydown', function (e) {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            abrirModalPendientesConFiltros();
        }
    });

    function construirParamsExport(formato) {
        const params = new URLSearchParams();

        const selectCarrera = document.getElementById('filtroCarrera');
        const selectCuatrimestre = document.getElementById('filtroCuatrimestre');
        const selectGrupo = document.getElementById('filtroGrupo');
        const desde = document.getElementById('filtroDesde').value;
        const hasta = document.getElementById('filtroHasta').value;

        if (selectCarrera.value) {
            params.append('idCarrera', selectCarrera.value);
            params.append('nombreCarrera', selectCarrera.options[selectCarrera.selectedIndex].text);
        }
        if (selectCuatrimestre.value) {
            params.append('cuatrimestre', selectCuatrimestre.value);
            params.append('nombreCuatrimestre', selectCuatrimestre.options[selectCuatrimestre.selectedIndex].text);
        }
        if (selectGrupo.value) {
            params.append('letra', selectGrupo.value);
            params.append('nombreGrupo', selectGrupo.options[selectGrupo.selectedIndex].text);
        }
        if (desde) params.append('desde', desde);
        if (hasta) params.append('hasta', hasta);

        const matriculaFiltro = document.getElementById('filtroMatricula').value;
        if (matriculaFiltro) {
            params.append('matricula', matriculaFiltro);
            params.append('nombreAlumno', document.getElementById('nombreAlumnoFiltro').textContent);
        }

        // Las graficas solo existen como <canvas> en el navegador: se capturan como PNG
        // (misma imagen que ve el tutor) para que el Excel/PDF las incluya tal cual.
        if (graficaPastelReporte) params.append('imagenPastel', graficaPastelReporte.toBase64Image());
        if (graficaBarrasTutor) params.append('imagenBarras', graficaBarrasTutor.toBase64Image());

        const accion = formato === 'excel' ? 'exportarExcel' : 'exportarPdf';
        params.append('accion', accion);

        return params;
    }

    async function descargarReporte(formato) {
        const params = construirParamsExport(formato);
        try {
            const resp = await fetch(CONTEXT_PATH + '/ReportesServlet', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params.toString()
            });
            if (!resp.ok) throw new Error('Respuesta no válida del servidor');

            const blob = await resp.blob();
            const disposition = resp.headers.get('Content-Disposition') || '';
            const match = disposition.match(/filename="?([^"]+)"?/);
            const nombreArchivo = match ? match[1] : 'reporte_tutorias.' + formato;

            const linkUrl = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = linkUrl;
            a.download = nombreArchivo;
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(linkUrl);

            mostrarToast('exito', 'Éxito', 'El reporte se descargó correctamente');
        } catch (err) {
            console.error(err);
            mostrarToast('error', 'Error', 'Ocurrió un error al exportar el reporte');
        }
    }

    document.getElementById('btnExportarExcel').addEventListener('click', function () {
        mostrarConfirmacion(
            'advertencia',
            '¿Deseas descargar?',
            'Estás a punto de exportar el reporte en formato Excel.',
            'Sí, descargar',
            () => {
                descargarReporte('excel');
            }
        );
    });

    document.getElementById('btnExportarPdf').addEventListener('click', function () {
        mostrarConfirmacion(
            'advertencia',
            '¿Deseas descargar?',
            'Estás a punto de exportar el reporte en formato PDF.',
            'Sí, descargar',
            () => {
                descargarReporte('pdf');
            }
        );
    });
</script>
</body>
</html>
