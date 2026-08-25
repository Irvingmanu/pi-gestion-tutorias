
(function () {
    'use strict';

    function mostrarCargando() {
        var overlay = document.getElementById('overlayCargando');
        if (overlay) overlay.classList.add('mostrando');
    }

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
            if (typeof url === 'string' && url.indexOf('/verificar-sesion') !== -1) {
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
