// Overlay global de "cargando" (ver includes/cargando.jsp): se activa SOLO, sin que cada
// formulario o boton tenga que llamar nada a mano. Cubre los dos patrones que ya usa toda
// la app para mandar una accion al servidor:
//   1) Un <form> que se envia de verdad (boton submit habilitado, o Enter) -> evento
//      'submit' del navegador.
//   2) Un <form> oculto que un script manda con formX.submit() (patron de confirmacion:
//      prepararEliminacion(), prepararReactivacion(), etc. en varios *.js de este proyecto)
//      -> este NO dispara el evento 'submit' (asi lo define el estandar), por eso se
//      intercepta aparte sobreescribiendo HTMLFormElement.prototype.submit.
// Y ademas cualquier fetch() (generarCredenciales, verificarPassword, exportar CSV, etc.),
// interceptando window.fetch una sola vez.
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

    // Expuestas por si alguna pantalla necesita mostrar/ocultar el overlay a mano
    // (ej. alrededor de un XMLHttpRequest en vez de fetch).
    window.mostrarCargando = mostrarCargando;
    window.ocultarCargando = ocultarCargando;

    // Caso 1: submit "real". Bubble (no captura): para cuando llega hasta aqui el
    // formulario ya paso cualquier validacion propia de la pagina (los scripts que
    // cancelan un submit invalido, ej. solicitud.js, llaman preventDefault() en el
    // listener puesto directo sobre el <form>, que corre ANTES de que el evento
    // burbujee hasta document). Si event.defaultPrevented quedo en true, no se
    // muestra el overlay: el envio no va a ninguna parte.
    document.addEventListener('submit', function (evento) {
        if (!evento.defaultPrevented) {
            mostrarCargando();
        }
    });

    // Caso 2: formX.submit() desde JS (formularios ocultos de confirmar
    // eliminar/reactivar, etc.). Se parchea una sola vez, para TODOS los <form> de
    // la pagina (actuales y los que se agreguen despues), sin tocar cada archivo
    // que ya llama a .submit().
    var submitOriginal = HTMLFormElement.prototype.submit;
    HTMLFormElement.prototype.submit = function () {
        mostrarCargando();
        return submitOriginal.apply(this, arguments);
    };

    // Caso 3: peticiones fetch() (AJAX sin recargar la pagina, ej. generarCredenciales,
    // verificarPassword, exportar CSV). finally() garantiza que se oculte tanto si la
    // peticion sale bien como si falla.
    if (window.fetch) {
        var fetchOriginal = window.fetch;
        window.fetch = function () {
            mostrarCargando();
            return fetchOriginal.apply(this, arguments).finally(ocultarCargando);
        };
    }

    // Red de seguridad: si la pagina se restaura desde el bfcache (boton "Atras" del
    // navegador) con el overlay todavia marcado como visible de la accion anterior, se
    // oculta para no dejarlo pegado tapando la pantalla.
    window.addEventListener('pageshow', function (evento) {
        if (evento.persisted) ocultarCargando();
    });
})();
