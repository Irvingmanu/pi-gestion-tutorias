package mx.edu.utez.pigestiontutorias.utils;

import jakarta.servlet.http.HttpServletRequest;

public class UrlUtils {

    private UrlUtils() {}

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
