
(function () {
    var INTERVALO_MS = 5000;
    var yaSeDisparo = false;

    var metaCtx = document.querySelector('meta[name="context-path"]');
    var contextPath = metaCtx ? metaCtx.content : "";

    function verificarSesion() {
        fetch(contextPath + "/verificar-sesion", { credentials: "same-origin" })
            .then(function (respuesta) {
                if (!respuesta.ok) return null;
                return respuesta.json();
            })
            .then(function (datos) {
                if (datos && datos.valida === false && !yaSeDisparo) {
                    yaSeDisparo = true;
                    window.location.reload();
                }
            })
            .catch(function () {

            });
    }

    verificarSesion();
    setInterval(verificarSesion, INTERVALO_MS);
})();