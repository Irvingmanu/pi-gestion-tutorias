// Verifica si la sesion actual sigue siendo la activa para este usuario: al cargar la
// pagina y despues cada 5 segundos. Si en algun momento deja de serlo (porque se inicio
// sesion en otro dispositivo/navegador con el mismo usuario), recarga la pagina: el
// FiltroAutenticacion (ya actualizado) detecta la sesion invalida y redirige solo a
// login.jsp?motivo=sesion_duplicada, mostrando ahi el aviso correspondiente.
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
                // Si falla la peticion (ej. sin internet momentaneamente), no hacemos nada:
                // no queremos sacar al usuario por un error de red pasajero.
            });
    }

    verificarSesion();
    setInterval(verificarSesion, INTERVALO_MS);
})();