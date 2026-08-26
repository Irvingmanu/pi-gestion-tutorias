/**
 * Muestra un overlay de carga global durante envíos de formularios y peticiones
 * fetch, interceptando submit/fetch/pageshow para dar retroalimentación visual
 * al usuario mientras la petición está en curso.
 * @author Irvingmanu
 * @date 2026-08-24
 */
(function () {
    'use strict';

    /**
     * Muestra el overlay de carga global agregándole la clase "mostrando".
     * @returns {void}
     */
    function mostrarCargando() {
        var overlay = document.getElementById('overlayCargando');
        if (overlay) overlay.classList.add('mostrando');
    }

    /**
     * Oculta el overlay de carga global quitándole la clase "mostrando".
     * @returns {void}
     */
    function ocultarCargando() {
        var overlay = document.getElementById('overlayCargando');
        if (overlay) overlay.classList.remove('mostrando');
    }

    window.mostrarCargando = mostrarCargando;
    window.ocultarCargando = ocultarCargando;

    document.addEventListener('submit', function (evento) {
        if (!evento.defaultPrevented) {
            mostrarCargando();
        }
    });

    var submitOriginal = HTMLFormElement.prototype.submit;
    HTMLFormElement.prototype.submit = function () {
        mostrarCargando();
        return submitOriginal.apply(this, arguments);
    };

    if (window.fetch) {
        var fetchOriginal = window.fetch;
        window.fetch = function () {
            var url = arguments[0];
            var opciones = arguments[1] || {};
            var esVerificarSesion = typeof url === 'string' && url.indexOf('/verificar-sesion') !== -1;
            var esEdicionMotivo = typeof url === 'string' && url.indexOf('/areas-apoyo') !== -1
                && (opciones.method === 'POST' || opciones.method === 'PUT' || opciones.method === 'DELETE');
            if (esVerificarSesion || esEdicionMotivo) {
                return fetchOriginal.apply(this, arguments);
            }
            mostrarCargando();
            return fetchOriginal.apply(this, arguments).finally(ocultarCargando);
        };
    }

    window.addEventListener('pageshow', function (evento) {
        if (evento.persisted) ocultarCargando();
    });
})();
