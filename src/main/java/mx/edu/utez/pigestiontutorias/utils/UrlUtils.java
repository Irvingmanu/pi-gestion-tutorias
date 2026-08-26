package mx.edu.utez.pigestiontutorias.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utilidad para construir la URL base absoluta de la aplicación a partir de una petición HTTP,
 * usada para generar enlaces absolutos (por ejemplo, en correos de confirmación).
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-08-07
 */
public class UrlUtils {

    private UrlUtils() {}

    /**
     * Construye la URL base absoluta de la aplicación (esquema, host, puerto si no es el
     * predeterminado, y contexto de la aplicación) a partir de la petición HTTP recibida.
     * @param request la petición HTTP de la cual derivar la URL base
     * @return la URL base absoluta de la aplicación, sin barra final
     */
    public static String baseUrl(HttpServletRequest request) {
        String esquema = request.getScheme();
        String host = request.getServerName();
        int puerto = request.getServerPort();

        boolean puertoPorDefecto = ("http".equals(esquema) && puerto == 80)
                || ("https".equals(esquema) && puerto == 443);

        StringBuilder sb = new StringBuilder();
        sb.append(esquema).append("://").append(host);
        if (!puertoPorDefecto) {
            sb.append(":").append(puerto);
        }
        sb.append(request.getContextPath());
        return sb.toString();
    }
}
