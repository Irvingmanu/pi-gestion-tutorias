function confirmarCancelacion() {
    let boton = document.getElementById('btnCancelarFormulario');
    let urlDestino = boton ? boton.dataset.urlCancelar : '/';

    mostrarConfirmacion(
        'advertencia',
        '¿Descartar cambios?',
        'Si sales ahora, perderás todos los datos que has ingresado.',
        'Sí, salir',
        function () {
            window.location.href = urlDestino;
        }
    );
}

function prepararEliminacion(matricula) {
    mostrarConfirmacion(
        'critica',
        '¿Eliminar alumno?',
        'El alumno se dará de baja y no podrá acceder al sistema, pero se conservará su historial.',
        'Eliminar',
        function () {
            document.getElementById('inputEliminarMatricula').value = matricula;
            document.getElementById('formEliminarAlumno').submit();
        }
    );
}

function prepararReactivacion(matricula) {
    mostrarConfirmacion(
        'advertencia',
        '¿Reactivar alumno?',
        'El alumno volverá a aparecer en los listados y podrá acceder al sistema nuevamente.',
        'Reactivar',
        function () {
            document.getElementById('inputReactivarMatricula').value = matricula;
            document.getElementById('formReactivarAlumno').submit();
        }
    );
}

// ==================== AGRUPACION DE LA TABLA POR CARRERA + CUATRIMESTRE + GRUPO ====================
// El JSP renderiza UNA sola tabla oculta (#tablaOriginalAlumnos) con todos los alumnos.
// Aqui la leemos una sola vez, y cada vez que cambia un filtro agrupamos las filas que
// coinciden y pintamos una tabla independiente (con scroll) por cada combinacion de
// Carrera + Cuatrimestre + Grupo, sin volver a pedir nada al servidor.

let filasAlumnosOriginales = [];

// Lee las filas de la tabla oculta del JSP hacia filasAlumnosOriginales. Separado de
// inicializarAgrupacionAlumnos() porque el filtro de Cuatrimestre/Grupo (poblarSelect*)
// tambien necesita estas filas ya cargadas ANTES de pintarse (ver valoresUnicos).
function cargarFilasAlumnosOriginales() {
    let tbodyOriginal = document.getElementById('tablaAlumnosOriginal');
    filasAlumnosOriginales = tbodyOriginal ? Array.from(tbodyOriginal.querySelectorAll('tr')) : [];
}

// Pinta el primer grupo (o el mensaje de "no hay alumnos"), respetando el filtro de
// "Mostrar alumnos dados de baja" (desmarcado por defecto).
function inicializarAgrupacionAlumnos() {
    let contenedorGrupos = document.getElementById('contenedorGruposAlumnos');
    if (!contenedorGrupos) return;

    if (filasAlumnosOriginales.length === 0) {
        // El JSP no encontro alumnos en BD (listaAlumnos vacia)
        contenedorGrupos.innerHTML = '<div class="alert alert-info text-center">No hay alumnos registrados todavía.</div>';
        return;
    }

    filtrarAlumnos();
}

// Filtrado en tiempo real: ningun filtro es obligatorio. El texto de "Buscar Alumno" ya NO
// participa aqui (antes reconstruia todo desde cero con cada letra escrita); ver
// aplicarBusquedaAlumno() mas abajo, que opera directo sobre el DOM ya renderizado.
function filtrarAlumnos() {
    let selectAcademiaFiltro = document.getElementById('academiaFiltroPrincipal');
    let academiaSeleccionada = selectAcademiaFiltro ? selectAcademiaFiltro.value : '';
    let carreraSeleccionada = document.getElementById('carreraFiltroPrincipal').value;
    let grupoSeleccionado = document.getElementById('grupo').value;
    let cuatrimestreSeleccionado = document.getElementById('cuatrimestre').value;
    let mostrarInactivos = document.getElementById('mostrarInactivos');
    let incluirInactivos = mostrarInactivos ? mostrarInactivos.checked : false;

    let filasFiltradas = filasAlumnosOriginales.filter(function (fila) {
        let academia = fila.dataset.academia || '';
        let carrera = fila.dataset.carrera || '';
        let cuatri = fila.dataset.cuatri || '';
        let grupo = fila.dataset.grupo || '';
        let activo = fila.dataset.activo !== 'N';

        let coincideAcademia = academiaSeleccionada === '' || academia === academiaSeleccionada;
        let coincideCarrera = carreraSeleccionada === '' || carrera === carreraSeleccionada;
        let coincideGrupo = grupoSeleccionado === '' || grupo === grupoSeleccionado;
        let coincideCuatri = cuatrimestreSeleccionado === '' || cuatri === cuatrimestreSeleccionado;
        let coincideActivo = activo || incluirInactivos;

        return coincideAcademia && coincideCarrera && coincideGrupo && coincideCuatri && coincideActivo;
    });

    renderizarGruposAlumnos(filasFiltradas);

    // Reaplica la busqueda activa (si habia una) sobre los acordeones recien reconstruidos,
    // para no perderla al cambiar otro filtro (Academia/Carrera/Grupo/Cuatrimestre/Inactivos).
    aplicarBusquedaAlumno();
}

// Agrupa las filas por Carrera + Cuatrimestre + Grupo y pinta una tabla por cada grupo
function renderizarGruposAlumnos(filas) {
    let contenedor = document.getElementById('contenedorGruposAlumnos');
    if (!contenedor) return;

    contenedor.innerHTML = '';

    // filas.length ya ES el conteo de <tr> que van a quedar visibles (equivalente a
    // contar los <tr> normales con display:'' en gestion-tutores.js/asignacion.js,
    // pero aqui el conteo se conoce antes de pintar nada porque las tablas se
    // reconstruyen desde cero en cada filtro, no se ocultan filas existentes). Sin
    // resultados: se oculta la estructura de tabla por completo (contenedor ya quedo
    // vacio arriba) y se muestra la tarjeta en su lugar.
    let tarjetaSinResultados = document.getElementById('tarjetaSinResultados');
    let sinResultados = filas.length === 0;
    if (tarjetaSinResultados) tarjetaSinResultados.style.display = sinResultados ? '' : 'none';

    if (sinResultados) {
        return;
    }

    let grupos = new Map();
    filas.forEach(function (fila) {
        let clave = fila.dataset.carrera + '|' + fila.dataset.cuatri + '|' + fila.dataset.grupo;
        if (!grupos.has(clave)) {
            grupos.set(clave, {
                carrera: fila.dataset.carrera,
                idCarrera: fila.dataset.carreraId,
                cuatri: fila.dataset.cuatri,
                grupo: fila.dataset.grupo,
                idGrupo: fila.dataset.grupoId,
                generacion: fila.dataset.generacion,
                filas: []
            });
        }
        grupos.get(clave).filas.push(fila);
    });

    // Orden: Carrera (alfabetico), Cuatrimestre (numerico), Grupo (alfabetico)
    let gruposOrdenados = Array.from(grupos.values()).sort(function (a, b) {
        if (a.carrera !== b.carrera) return a.carrera.localeCompare(b.carrera);
        if (a.cuatri !== b.cuatri) return Number(a.cuatri) - Number(b.cuatri);
        return a.grupo.localeCompare(b.grupo);
    });

    gruposOrdenados.forEach(function (grupoInfo) {
        contenedor.appendChild(construirAcordeonGrupo(grupoInfo));
    });
}

// Construye un .accordion-item de Bootstrap 5 para un solo grupo: encabezado con Nombre
// del Grupo, Generacion y Tutor, y el cuerpo con la tabla de alumnos (con scroll). Empieza
// siempre colapsado (aria-expanded="false", sin la clase "show" en el collapse); lo abre
// aplicarBusquedaAlumno() si hay una coincidencia de busqueda dentro. Sin data-bs-parent:
// permite varios grupos abiertos a la vez (la busqueda puede abrir mas de uno).
function construirAcordeonGrupo(grupoInfo) {
    let idHeader = 'acordeonHeaderGrupo' + grupoInfo.idGrupo;
    let idCollapse = 'acordeonCollapseGrupo' + grupoInfo.idGrupo;

    let item = document.createElement('div');
    item.className = 'accordion-item';

    let nombreTutor = (window.tutoresPorGrupo || {})[grupoInfo.idGrupo];
    let etiquetaTutor = nombreTutor ? 'Tutor: ' + nombreTutor : 'Sin tutor asignado';
    let etiquetaGeneracion = grupoInfo.generacion ? 'Gen ' + grupoInfo.generacion : 'Sin generación';

    let header = document.createElement('h2');
    header.className = 'accordion-header';
    header.id = idHeader;

    let boton = document.createElement('button');
    boton.type = 'button';
    boton.className = 'accordion-button collapsed';
    boton.setAttribute('data-bs-toggle', 'collapse');
    boton.setAttribute('data-bs-target', '#' + idCollapse);
    boton.setAttribute('aria-expanded', 'false');
    boton.setAttribute('aria-controls', idCollapse);
    boton.textContent = grupoInfo.carrera + ' - ' + grupoInfo.cuatri + '° ' + grupoInfo.grupo
        + ' (' + etiquetaGeneracion + ') — ' + etiquetaTutor;
    header.appendChild(boton);
    item.appendChild(header);

    let colapso = document.createElement('div');
    colapso.id = idCollapse;
    colapso.className = 'accordion-collapse collapse';
    colapso.setAttribute('aria-labelledby', idHeader);

    let cuerpo = document.createElement('div');
    cuerpo.className = 'accordion-body';

    // Acceso directo al historial de tutorias (individuales y grupales) de TODO el grupo,
    // sin tener que ir a Reportes Globales y volver a elegir Carrera/Cuatrimestre/Grupo a
    // mano (ver ReportesGlobalesServlet.doGet, atributos prefiltro*).
    let barraAcciones = document.createElement('div');
    barraAcciones.className = 'd-flex justify-content-end mb-2';

    let linkHistorial = document.createElement('a');
    linkHistorial.className = 'btn-figma text-decoration-none py-1 px-3 fs-6';
    linkHistorial.textContent = 'Ver historial de tutorías del grupo';
    linkHistorial.href = (window.APP_CONTEXT || '') + '/reportes-globales'
        + '?idCarrera=' + encodeURIComponent(grupoInfo.idCarrera || '')
        + '&cuatrimestre=' + encodeURIComponent(grupoInfo.cuatri || '')
        + '&letra=' + encodeURIComponent(grupoInfo.grupo || '');
    barraAcciones.appendChild(linkHistorial);
    cuerpo.appendChild(barraAcciones);

    let scrollWrap = document.createElement('div');
    scrollWrap.className = 'table-responsive';
    scrollWrap.style.maxHeight = '320px';
    scrollWrap.style.overflowY = 'auto';

    let tabla = document.createElement('table');
    tabla.className = 'tabla-grupos fs-6';
    tabla.innerHTML =
        '<thead>' +
        '<tr>' +
        '<th>Matricula</th>' +
        '<th>Nombre Completo</th>' +
        '<th>Correo</th>' +
        '<th>Genero</th>' +
        '<th>Carrera</th>' +
        '<th>Cuatri/Grupo</th>' +
        '<th>Acciones</th>' +
        '</tr>' +
        '</thead>';

    let tbody = document.createElement('tbody');
    grupoInfo.filas.forEach(function (fila) {
        tbody.appendChild(fila.cloneNode(true));
    });
    tabla.appendChild(tbody);

    scrollWrap.appendChild(tabla);
    cuerpo.appendChild(scrollWrap);
    colapso.appendChild(cuerpo);
    item.appendChild(colapso);

    return item;
}

// ==================== BUSQUEDA DE ALUMNO SOBRE LOS ACORDEONES YA RENDERIZADOS ====================
// A diferencia de los demas filtros (Academia/Carrera/Grupo/Cuatrimestre/Inactivos), que
// reconstruyen los acordeones desde cero via filtrarAlumnos(), la busqueda por nombre NO
// reconstruye nada: opera directo sobre el DOM que ya esta pintado, mostrando/ocultando
// <tr> y .accordion-item, y forzando con la API de Bootstrap (bootstrap.Collapse) a que se
// abran los grupos con coincidencias. Se vuelve a aplicar sola despues de cada
// reconstruccion (ver filtrarAlumnos()) para no perder la busqueda activa al cambiar otro
// filtro.

// Fuerza abierto/cerrado un .accordion-item via la API de Bootstrap, sin depender de que
// el coordinador le haya dado clic al boton. getOrCreateInstance ya es no-op si el collapse
// ya esta en el estado pedido, asi que es seguro llamarlo en cada tecla escrita.
function abrirAcordeonItem(item) {
    let colapso = item.querySelector('.accordion-collapse');
    if (!colapso || typeof bootstrap === 'undefined') return;
    bootstrap.Collapse.getOrCreateInstance(colapso, { toggle: false }).show();
}

function colapsarAcordeonItem(item) {
    let colapso = item.querySelector('.accordion-collapse');
    if (!colapso || typeof bootstrap === 'undefined') return;
    bootstrap.Collapse.getOrCreateInstance(colapso, { toggle: false }).hide();
}

function aplicarBusquedaAlumno() {
    let inputBuscar = document.getElementById('buscarAlumno');
    let contenedor = document.getElementById('contenedorGruposAlumnos');
    if (!inputBuscar || !contenedor) return;

    let texto = inputBuscar.value.trim().toLowerCase();
    let acordeones = contenedor.querySelectorAll('.accordion-item');
    let algunGrupoConCoincidencia = false;

    acordeones.forEach(function (item) {
        let filas = item.querySelectorAll('tbody tr');

        if (!texto) {
            // Buscador vacio: se restaura el estado original (filas visibles, acordeon
            // cerrado), sin importar si antes tenia coincidencias o no.
            filas.forEach(function (fila) {
                fila.style.display = '';
            });
            item.style.display = '';
            colapsarAcordeonItem(item);
            return;
        }

        let huboCoincidencia = false;
        filas.forEach(function (fila) {
            let coincide = (fila.dataset.nombre || '').includes(texto);
            fila.style.display = coincide ? '' : 'none';
            if (coincide) huboCoincidencia = true;
        });

        if (huboCoincidencia) {
            item.style.display = '';
            abrirAcordeonItem(item);
            algunGrupoConCoincidencia = true;
        } else {
            // Grupo entero sin coincidencias: se oculta el acordeon completo.
            item.style.display = 'none';
        }
    });

    // Si hay texto de busqueda y ningun grupo tuvo coincidencias, se reusa la tarjeta de
    // "sin resultados" que ya existe (en vez de dejar la pantalla en blanco). Con el
    // buscador vacio no se toca: filtrarAlumnos() ya la dejo como corresponde segun los
    // demas filtros.
    let tarjetaSinResultados = document.getElementById('tarjetaSinResultados');
    if (texto && acordeones.length > 0) {
        let sinCoincidencias = !algunGrupoConCoincidencia;
        if (tarjetaSinResultados) tarjetaSinResultados.style.display = sinCoincidencias ? '' : 'none';
    }
}

// ==================== FILTROS DE CUATRIMESTRE/GRUPO LIMITADOS A GRUPOS CON ALUMNOS ====================
// En vez de un rango fijo (1-11 / A-F) o de listar TODOS los grupos que existen en BD
// (window.gruposExistentes incluye grupos recien creados desde el modal "Nuevo Grupo"
// que aun no tienen ningun alumno), estos <select> se arman a partir de
// filasAlumnosOriginales: solo ofrecen combinaciones Carrera+Cuatrimestre+Grupo que
// realmente tienen registros en la tabla, para no dejar elegir un filtro que nunca
// va a traer resultados.

function valoresUnicos(campo, filtroCarrera, filtroCuatri) {
    let datasetKey = campo === 'letra' ? 'grupo' : campo;
    let vistos = new Set();
    filasAlumnosOriginales.forEach(function (fila) {
        let carrera = fila.dataset.carrera || '';
        let cuatri = fila.dataset.cuatri || '';
        if (filtroCarrera && carrera !== filtroCarrera) return;
        if (filtroCuatri && cuatri !== filtroCuatri) return;
        vistos.add(fila.dataset[datasetKey] || '');
    });
    return Array.from(vistos);
}

// Filtro OPCIONAL Academia -> Carrera (cliente, sin fetch, sin bloquear el select):
// #carreraFiltroPrincipal ya viene con TODAS las carreras del sistema renderizadas
// (JSTL) y habilitado desde el inicio; esto solo oculta (display:none) las <option>
// cuyo data-academia-id no coincida. Mismo patron que formulario-alumno.js.
function aplicarFiltroAcademiaPrincipal() {
    let selectAcademia = document.getElementById('academiaFiltroPrincipal');
    let selectCarrera = document.getElementById('carreraFiltroPrincipal');
    if (!selectAcademia || !selectCarrera) return;

    let idAcademia = selectAcademia.value;
    let opcionSeleccionadaSigueVisible = false;

    Array.prototype.forEach.call(selectCarrera.options, function (opcion) {
        if (!opcion.value) {
            return; // el placeholder "Seleccione la carrera" siempre se conserva
        }

        let coincide = !idAcademia || opcion.getAttribute('data-academia-id') === idAcademia;
        opcion.style.display = coincide ? '' : 'none';
        if (coincide && opcion.selected) {
            opcionSeleccionadaSigueVisible = true;
        }
    });

    // Si la carrera elegida ya no pertenece a la academia filtrada, se limpia la
    // seleccion y se re-dispara el resto de la cascada (Cuatrimestre/Grupo/tabla).
    // En cualquier otro caso, la tabla igual se refiltra de una vez: cambiar de
    // academia debe ocultar/mostrar filas al instante, no solo tocar el <select>.
    if (selectCarrera.value && !opcionSeleccionadaSigueVisible) {
        selectCarrera.value = '';
        selectCarrera.dispatchEvent(new Event('change'));
    } else {
        filtrarAlumnos();
    }
}

function poblarSelectCuatrimestre(carreraSeleccionada) {
    let select = document.getElementById('cuatrimestre');
    if (!select) return;
    let valorActual = select.value;

    let opciones = valoresUnicos('cuatri', carreraSeleccionada, '')
        .sort(function (a, b) { return Number(a) - Number(b); });

    select.innerHTML = '<option value="">Seleccione el cuatrimestre</option>';
    opciones.forEach(function (numero) {
        let opcion = document.createElement('option');
        opcion.value = numero;
        opcion.textContent = numero + '°';
        select.appendChild(opcion);
    });

    select.value = opciones.indexOf(valorActual) !== -1 ? valorActual : '';
}

function poblarSelectGrupo(carreraSeleccionada, cuatrimestreSeleccionado) {
    let select = document.getElementById('grupo');
    if (!select) return;
    let valorActual = select.value;

    let opciones = valoresUnicos('letra', carreraSeleccionada, cuatrimestreSeleccionado).sort();

    select.innerHTML = '<option value="">Seleccione el Grupo</option>';
    opciones.forEach(function (letra) {
        let opcion = document.createElement('option');
        opcion.value = letra;
        opcion.textContent = letra;
        select.appendChild(opcion);
    });

    select.value = opciones.indexOf(valorActual) !== -1 ? valorActual : '';
}

document.addEventListener('DOMContentLoaded', function () {
    // Este archivo tambien se carga en formulario-alumno.jsp (solo por confirmarCancelacion()),
    // que reutiliza el id "cuatrimestre" para su propia cascada Carrera->Cuatrimestre. Sin
    // este guard, este bloque (pensado para el listado de gestion-grupos.jsp) lo pisaba con
    // las opciones del listado apenas cargaba la pagina.
    let contenedorGrupos = document.getElementById('contenedorGruposAlumnos');
    if (!contenedorGrupos) {
        return;
    }

    let buscarAlumno = document.getElementById('buscarAlumno');
    let academiaFiltro = document.getElementById('academiaFiltroPrincipal');
    let carrera = document.getElementById('carreraFiltroPrincipal');
    let grupo = document.getElementById('grupo');
    let cuatrimestre = document.getElementById('cuatrimestre');
    let mostrarInactivos = document.getElementById('mostrarInactivos');

    // #carreraFiltroPrincipal ya viene con todas sus opciones desde el JSP (JSTL);
    // solo falta poblar Cuatrimestre/Grupo, que si siguen siendo dinamicos. Las filas
    // se cargan primero porque poblarSelectCuatrimestre/poblarSelectGrupo ya dependen
    // de filasAlumnosOriginales (ver valoresUnicos).
    cargarFilasAlumnosOriginales();
    poblarSelectCuatrimestre('');
    poblarSelectGrupo('', '');
    aplicarFiltroAcademiaPrincipal();
    inicializarAgrupacionAlumnos();

    if (buscarAlumno) buscarAlumno.addEventListener('input', aplicarBusquedaAlumno);
    if (academiaFiltro) academiaFiltro.addEventListener('change', aplicarFiltroAcademiaPrincipal);
    if (carrera) carrera.addEventListener('change', function () {
        poblarSelectCuatrimestre(carrera.value);
        poblarSelectGrupo(carrera.value, cuatrimestre.value);
        filtrarAlumnos();
    });
    if (cuatrimestre) cuatrimestre.addEventListener('change', function () {
        poblarSelectGrupo(carrera.value, cuatrimestre.value);
        filtrarAlumnos();
    });
    if (grupo) grupo.addEventListener('change', filtrarAlumnos);
    if (mostrarInactivos) mostrarInactivos.addEventListener('change', filtrarAlumnos);
});

// ==================== MODAL "NUEVO GRUPO" ====================
// Cascada Academia -> Carrera (mismo patron de solo ocultar <option>, sin fetch) y
// Carrera -> Cuatrimestre segun el NIVEL de la carrera (TSU 1-6 / ING 7-10, igual que
// formulario-alumno.js). La Letra es un <select> fijo A-F (mismo rango que el alta de
// alumno); al elegir Cuatrimestre se consulta window.gruposExistentes (grupos reales
// de BD, no solo los que ya tienen alumnos) para deshabilitar las letras ya usadas en
// esa Carrera+Cuatrimestre y preseleccionar la primera libre. El formulario hace un
// POST real a AlumnoServlet (accion=crearGrupo), que usa GrupoDao.findOrCreate().
document.addEventListener('DOMContentLoaded', function () {
    let selectModalAcademia = document.getElementById('modalAcademia');
    let selectModalCarrera = document.getElementById('modalCarrera');
    let selectModalCuatrimestre = document.getElementById('modalCuatrimestre');
    let selectModalLetra = document.getElementById('modalLetra');
    let inputModalAnioInicio = document.getElementById('modalAnioInicio');
    let ayudaModalAnioInicio = document.getElementById('modalAnioInicioAyuda');
    let selectModalPeriodo = document.getElementById('modalPeriodo');
    let formNuevoGrupo = document.getElementById('formNuevoGrupo');
    let modalNuevoGrupoEl = document.getElementById('modalNuevoGrupo');
    if (!formNuevoGrupo || !selectModalCarrera) return;

    function aplicarFiltroAcademiaModal() {
        let idAcademia = selectModalAcademia.value;
        let opcionSeleccionadaSigueVisible = false;

        Array.prototype.forEach.call(selectModalCarrera.options, function (opcion) {
            if (!opcion.value) return; // el placeholder "Seleccione la carrera" siempre se conserva

            let coincide = !idAcademia || opcion.getAttribute('data-academia-id') === idAcademia;
            opcion.style.display = coincide ? '' : 'none';
            if (coincide && opcion.selected) opcionSeleccionadaSigueVisible = true;
        });

        if (selectModalCarrera.value && !opcionSeleccionadaSigueVisible) {
            selectModalCarrera.value = '';
            selectModalCarrera.dispatchEvent(new Event('change'));
        }
    }

    function poblarCuatrimestresModal(nivel) {
        selectModalCuatrimestre.innerHTML = '';

        if (!nivel) {
            selectModalCuatrimestre.disabled = true;
            let opcionVacia = document.createElement('option');
            opcionVacia.value = '';
            opcionVacia.textContent = 'Seleccione primero la carrera';
            opcionVacia.selected = true;
            selectModalCuatrimestre.appendChild(opcionVacia);
            return;
        }

        let desde = nivel === 'TSU' ? 1 : 7;
        let hasta = nivel === 'TSU' ? 6 : 10;

        let opcionVacia = document.createElement('option');
        opcionVacia.value = '';
        opcionVacia.textContent = 'Seleccione el cuatrimestre';
        opcionVacia.selected = true;
        selectModalCuatrimestre.appendChild(opcionVacia);

        for (let numero = desde; numero <= hasta; numero++) {
            let opcion = document.createElement('option');
            opcion.value = String(numero);
            opcion.textContent = numero + '°';
            selectModalCuatrimestre.appendChild(opcion);
        }

        selectModalCuatrimestre.disabled = false;
    }

    function resetearSelectLetraModal(mensaje) {
        selectModalLetra.innerHTML = '';
        let opcionVacia = document.createElement('option');
        opcionVacia.value = '';
        opcionVacia.textContent = mensaje;
        opcionVacia.selected = true;
        selectModalLetra.appendChild(opcionVacia);
        selectModalLetra.disabled = true;
    }

    // Exclusion dinamica: arma el <select> con A-F, deshabilitando las letras que ya
    // existen en window.gruposExistentes para esa Carrera+Cuatrimestre, y preselecciona
    // la primera letra libre. Si las 6 ya existen, deja el placeholder vacio+required
    // seleccionado para que el navegador bloquee el envio (el select sigue habilitado:
    // un <select disabled> no participa en la validacion de "required").
    function poblarSelectLetraModal(carreraNombre, cuatrimestre) {
        if (!carreraNombre || !cuatrimestre) {
            resetearSelectLetraModal('Seleccione primero el cuatrimestre');
            return;
        }

        let letras = ['A', 'B', 'C', 'D', 'E', 'F'];
        let usadas = new Set();
        (window.gruposExistentes || []).forEach(function (g) {
            if (g.carrera === carreraNombre && String(g.cuatri) === String(cuatrimestre)) {
                usadas.add(g.letra);
            }
        });

        selectModalLetra.innerHTML = '';
        selectModalLetra.disabled = false;

        let primeraLibre = null;
        letras.forEach(function (letra) {
            let opcion = document.createElement('option');
            opcion.value = letra;
            opcion.textContent = usadas.has(letra) ? letra + ' (ya existe)' : letra;
            opcion.disabled = usadas.has(letra);
            if (!usadas.has(letra) && primeraLibre === null) primeraLibre = letra;
            selectModalLetra.appendChild(opcion);
        });

        if (primeraLibre) {
            selectModalLetra.value = primeraLibre;
        } else {
            let opcionVacia = document.createElement('option');
            opcionVacia.value = '';
            opcionVacia.textContent = 'Sin letras disponibles (A-F ocupadas)';
            opcionVacia.selected = true;
            selectModalLetra.insertBefore(opcionVacia, selectModalLetra.firstChild);
        }
    }

    // Cualquier seleccion hecha cuenta como "datos capturados" para la confirmacion
    // de cierre; la Letra solo cuenta si el select ya esta habilitado (con sugerencia
    // real), no en su placeholder inicial deshabilitado.
    function modalNuevoGrupoTieneCambios() {
        return selectModalAcademia.value !== ''
            || selectModalCarrera.value !== ''
            || selectModalCuatrimestre.value !== ''
            || (!selectModalLetra.disabled && selectModalLetra.value !== '')
            || inputModalAnioInicio.value !== String(new Date().getFullYear())
            || (selectModalPeriodo && selectModalPeriodo.value !== '');
    }

    function cerrarModalNuevoGrupo() {
        let instancia = bootstrap.Modal.getInstance(modalNuevoGrupoEl);
        if (instancia) instancia.hide();
    }

    function intentarCerrarModalNuevoGrupo() {
        if (!modalNuevoGrupoTieneCambios()) {
            cerrarModalNuevoGrupo();
            return;
        }
        mostrarConfirmacion(
            'advertencia',
            '¿Descartar cambios?',
            'Si cierras ahora, perderás los datos del grupo que estabas creando.',
            'Sí, salir',
            cerrarModalNuevoGrupo
        );
    }

    selectModalAcademia.addEventListener('change', aplicarFiltroAcademiaModal);

    selectModalCarrera.addEventListener('change', function () {
        let opcionElegida = selectModalCarrera.options[selectModalCarrera.selectedIndex];
        let nivel = opcionElegida ? opcionElegida.getAttribute('data-nivel') : null;
        poblarCuatrimestresModal(nivel);
        resetearSelectLetraModal('Seleccione primero el cuatrimestre');
    });

    selectModalCuatrimestre.addEventListener('change', function () {
        let opcionCarrera = selectModalCarrera.options[selectModalCarrera.selectedIndex];
        let nombreCarrera = opcionCarrera ? opcionCarrera.getAttribute('data-nombre') : null;
        poblarSelectLetraModal(nombreCarrera, selectModalCuatrimestre.value);
    });

    // El modal se reabre varias veces en la misma pagina: se limpia por completo cada
    // vez que se abre para no arrastrar la seleccion de la vez anterior.
    modalNuevoGrupoEl.addEventListener('show.bs.modal', function () {
        formNuevoGrupo.reset();
        aplicarFiltroAcademiaModal();
        poblarCuatrimestresModal(null);
        resetearSelectLetraModal('Seleccione primero el cuatrimestre');

        // Sugerencia de Año de Inicio = año actual, con rango año actual -5 a +1: -5 porque
        // un grupo de 6° cuatrimestre o mas registrado HOY corresponde a una generacion que
        // arranco varios años atras (cohortes activas/rezagadas), no solo el año pasado.
        // formNuevoGrupo.reset() ya limpio el campo a su estado inicial (sin value en el
        // HTML), asi que se sugiere aqui. El texto de ayuda muestra el rango explicitamente
        // para que el coordinador no tenga que adivinarlo (mismo rango que valida
        // AlumnoServlet si se manda el POST directo).
        let anioActual = new Date().getFullYear();
        let anioMinimo = anioActual - 5;
        let anioMaximo = anioActual + 1;
        inputModalAnioInicio.min = String(anioMinimo);
        inputModalAnioInicio.max = String(anioMaximo);
        inputModalAnioInicio.value = String(anioActual);
        if (ayudaModalAnioInicio) {
            ayudaModalAnioInicio.textContent = 'Debe estar entre ' + anioMinimo + ' y ' + anioMaximo
                + '. El Año de Fin se calcula solo según el Cuatrimestre (TSU: +2 años, Ingeniería: +1 año).';
        }
    });

    // Refuerzo anti-autocompletado: el autocompletado del navegador (aunque el <input>
    // tenga autocomplete="off") a veces repone un valor capturado en una prueba anterior
    // DESPUES de que se abre el modal, pisando en silencio la sugerencia de arriba sin que
    // se note a simple vista. shown.bs.modal se dispara cuando el modal ya esta totalmente
    // visible (mas tarde que show.bs.modal), asi que se vuelve a forzar el año sugerido aqui.
    modalNuevoGrupoEl.addEventListener('shown.bs.modal', function () {
        inputModalAnioInicio.value = String(new Date().getFullYear());
    });

    let btnCancelarNuevoGrupo = document.getElementById('btnCancelarNuevoGrupo');
    let btnCerrarNuevoGrupo = document.getElementById('btnCerrarNuevoGrupo');
    if (btnCancelarNuevoGrupo) btnCancelarNuevoGrupo.addEventListener('click', intentarCerrarModalNuevoGrupo);
    if (btnCerrarNuevoGrupo) btnCerrarNuevoGrupo.addEventListener('click', intentarCerrarModalNuevoGrupo);

    // El "Exito al guardar" se muestra en el bloque de toasts por parametros de URL
    // (?exito=grupo_creado), al recargar despues del POST real a /gestion-grupos.
});

// ==================== MODAL "CARGA MASIVA DE ALUMNOS" ====================
// Un archivo Excel = un Grupo (elegido en #cargaMasivaGrupo). El POST (con el archivo) lo
// hace el propio <form enctype="multipart/form-data">, asi que aqui solo se maneja abrir/
// cerrar el modal con confirmacion si ya se eligio grupo o archivo, igual que el resto de
// los modales de esta pagina. El "Exito"/"Error" real se muestra en el bloque de toasts por
// parametros de URL (?exito=carga_masiva_alumnos / ?error=...), al recargar despues del POST.
document.addEventListener('DOMContentLoaded', function () {
    let selectCargaGrupo = document.getElementById('cargaMasivaGrupo');
    let inputCargaArchivo = document.getElementById('cargaMasivaArchivo');
    let formCargaMasiva = document.getElementById('formCargaMasivaAlumnos');
    let modalCargaMasivaEl = document.getElementById('modalCargaMasiva');
    if (!formCargaMasiva || !selectCargaGrupo || !inputCargaArchivo || !modalCargaMasivaEl) return;

    // Searchable Select (Select2) para "Grupo": util si hay muchos grupos activos. Mismo
    // patron defensivo que #asignarGrupo en formulario-alumno.js: si el CDN de jQuery/
    // Select2 no cargo, el <select> nativo sigue funcionando. dropdownParent es OBLIGATORIO
    // aqui porque el select vive dentro de un modal de Bootstrap: sin eso, el menu
    // desplegable de Select2 se renderiza como hijo de <body> (fuera del modal) y el
    // z-index/overflow del modal lo deja tapado o inutilizable.
    let $cargaMasivaGrupo = (typeof jQuery !== 'undefined' && jQuery.fn.select2)
        ? jQuery(selectCargaGrupo)
        : null;

    if ($cargaMasivaGrupo) {
        $cargaMasivaGrupo.select2({
            theme: 'bootstrap-5',
            width: '100%',
            dropdownParent: jQuery('#modalCargaMasiva'),
            placeholder: 'Escriba para buscar un grupo...'
        });
    }

    function cargaMasivaTieneCambios() {
        return selectCargaGrupo.value !== '' || (inputCargaArchivo.files && inputCargaArchivo.files.length > 0);
    }

    function cerrarModalCargaMasiva() {
        let instancia = bootstrap.Modal.getInstance(modalCargaMasivaEl);
        if (instancia) instancia.hide();
    }

    function intentarCerrarModalCargaMasiva() {
        if (!cargaMasivaTieneCambios()) {
            cerrarModalCargaMasiva();
            return;
        }
        mostrarConfirmacion(
            'advertencia',
            '¿Descartar carga masiva?',
            'Si cierras ahora, perderás el grupo y el archivo seleccionados.',
            'Sí, salir',
            cerrarModalCargaMasiva
        );
    }

    // El modal se reabre varias veces en la misma pagina: se limpia por completo cada vez
    // que se abre para no arrastrar la seleccion de la vez anterior. formCargaMasiva.reset()
    // ya deja el <select> real en su placeholder; el trigger('change') fuerza a Select2 a
    // releer ese valor y refrescar lo que muestra (si no, el widget se queda mostrando la
    // ultima opcion elegida aunque el <select> real ya se haya limpiado).
    modalCargaMasivaEl.addEventListener('show.bs.modal', function () {
        formCargaMasiva.reset();
        if ($cargaMasivaGrupo) $cargaMasivaGrupo.trigger('change');
    });

    let btnCancelarCargaMasiva = document.getElementById('btnCancelarCargaMasiva');
    let btnCerrarCargaMasiva = document.getElementById('btnCerrarCargaMasiva');
    if (btnCancelarCargaMasiva) btnCancelarCargaMasiva.addEventListener('click', intentarCerrarModalCargaMasiva);
    if (btnCerrarCargaMasiva) btnCerrarCargaMasiva.addEventListener('click', intentarCerrarModalCargaMasiva);
});

// Toasts/alertas de exito y error via parametros en la URL (?exito=, ?error=)
document.addEventListener('DOMContentLoaded', function () {
    const parametros = new URLSearchParams(window.location.search);
    const exito = parametros.get('exito');

    if (exito) {
        switch (exito) {
            case 'guardado':
                mostrarToast('exito', '¡Éxito!', 'El alumno fue guardado correctamente');
                break;
            case 'editado':
                mostrarToast('exito', '¡Éxito!', 'El alumno fue editado correctamente');
                break;
            case 'eliminado':
                mostrarToast('exito', '¡Éxito!', 'El alumno fue eliminado correctamente');
                break;
            case 'reactivado':
                mostrarToast('exito', '¡Éxito!', 'El alumno fue reactivado correctamente');
                break;
            case 'grupo_creado':
                mostrarToast('exito', '¡Éxito!', 'El grupo fue creado correctamente');
                break;
            case 'carga_masiva_alumnos': {
                // Si hubo filas invalidas, se omite este toast: mas abajo se muestra una
                // alerta mas completa (con el numero de cada fila omitida), que ya incluye
                // este mismo conteo de "insertados".
                if (window.filasInvalidasExcel && window.filasInvalidasExcel.length > 0) break;
                let insertados = parametros.get('insertados') || '0';
                mostrarToast('exito', '¡Éxito!', 'Se registraron ' + insertados + ' alumno(s) correctamente.');
                break;
            }
        }

        window.history.replaceState(null, null, window.location.pathname);
    }

    const error = parametros.get('error');

    if (error) {
        switch (error) {
            case 'matricula_duplicada':
                mostrarAlerta('error', 'Error', 'Esta matrícula ya está registrada en el sistema.');
                break;
            case 'correo_duplicado':
                mostrarAlerta('error', 'Error', 'Este correo ya está registrado en el sistema.');
                break;
            case 'correo':
                mostrarAlerta('error', 'Error', 'El correo debe terminar en @utez.edu.mx.');
                break;
            case 'alumno_en_uso':
                mostrarAlerta('error', 'No se puede eliminar', 'Este alumno ya tiene asistencias u otros registros vinculados en el sistema.');
                break;
            case 'reactivacion_fallida':
                mostrarAlerta('error', 'Error', 'No se pudo reactivar al alumno.');
                break;
            // Antes todos los campos del modal "Nuevo Grupo" caian en un solo
            // "grupo_datos_invalidos" generico, y el coordinador tenia que adivinar cual de
            // los 5 campos era el problema (ver crearGrupoIndependiente() en AlumnoServlet,
            // que ahora corta en el primero que falle y manda un codigo especifico por campo).
            case 'grupo_carrera_invalida':
                mostrarAlerta('error', 'Error', 'Selecciona una Carrera válida.');
                break;
            case 'grupo_cuatrimestre_invalido':
                mostrarAlerta('error', 'Error', 'El Cuatrimestre debe corresponder al nivel de la Carrera elegida (TSU: 1° a 6°, Ingeniería/Licenciatura: 7° a 10°).');
                break;
            case 'grupo_letra_invalida':
                mostrarAlerta('error', 'Error', 'Selecciona una Letra válida (A-F).');
                break;
            case 'grupo_anio_invalido': {
                // Mismo rango que se sugiere en el modal (ver show.bs.modal mas abajo):
                // año actual -5 a +1.
                let anioActual = new Date().getFullYear();
                mostrarAlerta('error', 'Error', 'El Año de Inicio debe estar entre ' + (anioActual - 5) + ' y ' + (anioActual + 1) + '.');
                break;
            }
            case 'grupo_periodo_invalido':
                mostrarAlerta('error', 'Error', 'Selecciona un Periodo Escolar válido de la lista.');
                break;
            case 'grupo_duplicado':
                mostrarAlerta('error', 'Error', 'Ese grupo ya existe para la carrera, cuatrimestre y periodo elegidos.');
                break;
            case 'grupo_creacion_fallida':
                mostrarAlerta('error', 'Error', 'No se pudo crear el grupo por un error interno. Intenta de nuevo.');
                break;
            case 'grupo_datos_invalidos':
                mostrarAlerta('error', 'Error', 'Verifica los datos del grupo: Carrera, Cuatrimestre, Letra, Año de Inicio y Periodo Escolar.');
                break;
            case 'carga_sin_grupo':
                mostrarAlerta('error', 'Error', 'Selecciona un Grupo válido antes de subir el archivo.');
                break;
            case 'archivo_vacio':
                mostrarAlerta('error', 'Error', 'Selecciona un archivo Excel (.xlsx o .xls) antes de subir.');
                break;
            case 'archivo_invalido':
                // Si hay filas invalidas con numero de renglon (window.filasInvalidasExcel),
                // se omite este mensaje generico: mas abajo se muestra uno mas especifico
                // con el detalle fila por fila. Ambos usan el mismo modal (mostrarAlerta),
                // asi que mostrar los dos aqui pisaria el primero con el segundo sin que se
                // llegue a ver.
                if (!window.filasInvalidasExcel || window.filasInvalidasExcel.length === 0) {
                    mostrarAlerta('error', 'Error', 'No se pudo leer el archivo, o ninguna fila tenía datos válidos. Verifica el formato de las columnas y vuelve a intentar.');
                }
                break;
            case 'carga_fallida':
                mostrarAlerta('error', 'Error', 'No se pudo guardar la carga masiva por un error interno. Intenta de nuevo.');
                break;
        }

        window.history.replaceState(null, null, window.location.pathname);
    }

    // Filas del ultimo Excel de carga masiva que se omitieron por datos invalidos/
    // duplicados (window.filasInvalidasExcel, ver gestion-grupos.jsp: AlumnoServlet la
    // guarda en SESSION para no saturar la URL, el JSP la vuelca a este global y la borra
    // de la sesion). mostrarAlerta() es un modal (bootstrap.Modal), no un toast: se queda
    // en pantalla hasta que el coordinador le da clic a "Aceptar", a proposito, para que le
    // de tiempo de leer y anotar los numeros de fila antes de que desaparezca solo.
    // Se avisa aqui, fuera de los switch de arriba, porque aplica tanto si la carga fue
    // "exito parcial" (exito=carga_masiva_alumnos) como si fallo por completo
    // (error=archivo_invalido, cuando NINGUNA fila paso la validacion) — el titulo y tipo
    // cambian segun cual de los dos haya sido.
    if (window.filasInvalidasExcel && window.filasInvalidasExcel.length > 0) {
        let insertados = parseInt(parametros.get('insertados') || '0', 10);
        let listaFilas = window.filasInvalidasExcel.join(', ');

        if (insertados > 0) {
            mostrarAlerta(
                'advertencia',
                'Carga parcial',
                'Se registraron ' + insertados + ' alumno(s), pero se omitieron las siguientes filas por datos inválidos: ' + listaFilas + '.'
            );
        } else {
            mostrarAlerta(
                'error',
                'No se registró ningún alumno',
                'Ninguna fila del archivo pasó la validación. Filas con error: ' + listaFilas + '.'
            );
        }
    }
});