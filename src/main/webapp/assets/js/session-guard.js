
/**
 * Vigila en segundo plano que la sesión del usuario siga siendo válida en el
 * servidor, consultando periódicamente el endpoint /verificar-sesion y forzando
 * una recarga de la página si el servidor reporta que la sesión ya no es válida.
 * @author J4IROXD
 * @date 2026-08-20
 */
(function () {
    var INTERVALO_MS = 5000;
    var yaSeDisparo = false;

    var metaCtx = document.querySelector('meta[name="context-path"]');
    var contextPath = metaCtx ? metaCtx.content : "";

    /**
     * Consulta al servidor si la sesión actual sigue siendo válida y recarga la
     * página una sola vez si detecta que ya expiró o fue invalidada.
     * @returns {void}
     */
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