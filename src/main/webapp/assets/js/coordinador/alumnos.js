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

let filasAlumnosOriginales = [];

function cargarFilasAlumnosOriginales() {
    let tbodyOriginal = document.getElementById('tablaAlumnosOriginal');
    filasAlumnosOriginales = tbodyOriginal ? Array.from(tbodyOriginal.querySelectorAll('tr')) : [];
}

function inicializarAgrupacionAlumnos() {
    let contenedorGrupos = document.getElementById('contenedorGruposAlumnos');
    if (!contenedorGrupos) return;

    if (filasAlumnosOriginales.length === 0) {

        contenedorGrupos.innerHTML = '<div class="alert alert-info text-center">No hay alumnos registrados todavía.</div>';
        return;
    }

    filtrarAlumnos();
}

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

    aplicarBusquedaAlumno();
}

function renderizarGruposAlumnos(filas) {
    let contenedor = document.getElementById('contenedorGruposAlumnos');
    if (!contenedor) return;

    contenedor.innerHTML = '';

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

    let gruposOrdenados = Array.from(grupos.values()).sort(function (a, b) {
        if (a.carrera !== b.carrera) return a.carrera.localeCompare(b.carrera);
        if (a.cuatri !== b.cuatri) return Number(a.cuatri) - Number(b.cuatri);
        return a.grupo.localeCompare(b.grupo);
    });

    gruposOrdenados.forEach(function (grupoInfo) {
        contenedor.appendChild(construirAcordeonGrupo(grupoInfo));
    });
}

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

            item.style.display = 'none';
        }
    });

    let tarjetaSinResultados = document.getElementById('tarjetaSinResultados');
    if (texto && acordeones.length > 0) {
        let sinCoincidencias = !algunGrupoConCoincidencia;
        if (tarjetaSinResultados) tarjetaSinResultados.style.display = sinCoincidencias ? '' : 'none';
    }
}

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

function aplicarFiltroAcademiaPrincipal() {
    let selectAcademia = document.getElementById('academiaFiltroPrincipal');
    let selectCarrera = document.getElementById('carreraFiltroPrincipal');
    if (!selectAcademia || !selectCarrera) return;

    let idAcademia = selectAcademia.value;
    let opcionSeleccionadaSigueVisible = false;

    Array.prototype.forEach.call(selectCarrera.options, function (opcion) {
        if (!opcion.value) {
            return;
        }

        let coincide = !idAcademia || opcion.getAttribute('data-academia-id') === idAcademia;
        opcion.style.display = coincide ? '' : 'none';
        if (coincide && opcion.selected) {
            opcionSeleccionadaSigueVisible = true;
        }
    });

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
            if (!opcion.value) return;

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

    modalNuevoGrupoEl.addEventListener('show.bs.modal', function () {
        formNuevoGrupo.reset();
        aplicarFiltroAcademiaModal();
        poblarCuatrimestresModal(null);
        resetearSelectLetraModal('Seleccione primero el cuatrimestre');

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

    modalNuevoGrupoEl.addEventListener('shown.bs.modal', function () {
        inputModalAnioInicio.value = String(new Date().getFullYear());
    });

    let btnCancelarNuevoGrupo = document.getElementById('btnCancelarNuevoGrupo');
    let btnCerrarNuevoGrupo = document.getElementById('btnCerrarNuevoGrupo');
    if (btnCancelarNuevoGrupo) btnCancelarNuevoGrupo.addEventListener('click', intentarCerrarModalNuevoGrupo);
    if (btnCerrarNuevoGrupo) btnCerrarNuevoGrupo.addEventListener('click', intentarCerrarModalNuevoGrupo);

});

document.addEventListener('DOMContentLoaded', function () {
    let selectCargaGrupo = document.getElementById('cargaMasivaGrupo');
    let inputCargaArchivo = document.getElementById('cargaMasivaArchivo');
    let formCargaMasiva = document.getElementById('formCargaMasivaAlumnos');
    let modalCargaMasivaEl = document.getElementById('modalCargaMasiva');
    if (!formCargaMasiva || !selectCargaGrupo || !inputCargaArchivo || !modalCargaMasivaEl) return;

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

    modalCargaMasivaEl.addEventListener('show.bs.modal', function () {
        formCargaMasiva.reset();
        if ($cargaMasivaGrupo) $cargaMasivaGrupo.trigger('change');
    });

    let btnCancelarCargaMasiva = document.getElementById('btnCancelarCargaMasiva');
    let btnCerrarCargaMasiva = document.getElementById('btnCerrarCargaMasiva');
    if (btnCancelarCargaMasiva) btnCancelarCargaMasiva.addEventListener('click', intentarCerrarModalCargaMasiva);
    if (btnCerrarCargaMasiva) btnCerrarCargaMasiva.addEventListener('click', intentarCerrarModalCargaMasiva);
});

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

    if (window.filasInvalidasExcel && window.filasInvalidasExcel.length > 0) {
        let insertados = parseInt(parametros.get('insertados') || '0', 10);
        let listaFilas = window.filasInvalidasExcel.join('\n');

        if (insertados > 0) {
            mostrarAlerta(
                'advertencia',
                'Carga parcial',
                'Se registraron ' + insertados + ' alumno(s), pero se omitieron estas filas:\n' + listaFilas
            );
        } else {
            mostrarAlerta(
                'error',
                'No se registró ningún alumno',
                'Ninguna fila del archivo pasó la validación:\n' + listaFilas
            );
        }
    }
});