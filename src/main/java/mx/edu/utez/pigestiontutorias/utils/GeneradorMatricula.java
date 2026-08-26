package mx.edu.utez.pigestiontutorias.utils;

import java.time.LocalDate;
import java.time.Year;
import java.util.Locale;
import java.util.Set;

/**
 * Utilidad para generar el prefijo de matrícula automática de un alumno de primer
 * cuatrimestre, combinando el año actual, un dígito que identifica el periodo escolar
 * según el mes de inicio, y una sigla derivada del nombre de la carrera.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-08-20
 */
public final class GeneradorMatricula {

    private GeneradorMatricula() {
    }

    /**
     * Construye el prefijo completo de matrícula combinando el año actual, el dígito
     * del periodo escolar y la sigla de la carrera.
     * @param nombreCarrera el nombre de la carrera del alumno
     * @param fechaInicioPeriodo la fecha de inicio del periodo escolar del grupo
     * @return el prefijo de matrícula resultante (año + dígito de periodo + sigla de carrera)
     */
    public static String construirPrefijo(String nombreCarrera, java.sql.Date fechaInicioPeriodo) {
        String anioActual = String.valueOf(Year.now().getValue());
        return anioActual + resolverDigitoPeriodo(fechaInicioPeriodo) + resolverSiglaCarrera(nombreCarrera);
    }

    /**
     * Determina el dígito que identifica el periodo escolar según el mes de la fecha
     * de inicio: "1" para enero-abril, "2" para mayo-agosto y "3" para septiembre-diciembre.
     * @param fechaInicio la fecha de inicio del periodo escolar
     * @return el dígito ("1", "2" o "3") correspondiente al periodo
     */
    public static String resolverDigitoPeriodo(java.sql.Date fechaInicio) {
        LocalDate fecha = fechaInicio.toLocalDate();
        int mes = fecha.getMonthValue();
        if (mes <= 4) return "1";
        if (mes <= 8) return "2";
        return "3";
    }

    /**
     * Genera una sigla de dos letras a partir de las iniciales de las palabras
     * significativas del nombre de la carrera, ignorando conectores comunes.
     * @param nombreCarrera el nombre de la carrera a partir del cual generar la sigla
     * @return la sigla de dos letras en minúsculas; "xx" si el nombre es nulo o vacío
     */
    public static String resolverSiglaCarrera(String nombreCarrera) {
        if (nombreCarrera == null || nombreCarrera.isBlank()) {
            return "xx";
        }

        Set<String> conectores = Set.of("de", "del", "en", "y", "la", "las", "los", "el", "tsu", "ing");
        StringBuilder sigla = new StringBuilder();

        for (String palabra : nombreCarrera.trim().split("\\s+")) {
            if (sigla.length() >= 2) break;

            String limpia = palabra.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
            if (limpia.isEmpty() || conectores.contains(limpia)) continue;
            sigla.append(limpia.charAt(0));
        }

        if (sigla.length() < 2) {
            String soloLetras = nombreCarrera.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
            String relleno = (soloLetras + "xx");
            return relleno.substring(0, 2);
        }

        return sigla.toString();
    }
}
