package mx.edu.utez.pigestiontutorias.utils;

import java.time.LocalDate;
import java.time.Year;
import java.util.Locale;
import java.util.Set;

public final class GeneradorMatricula {

    private GeneradorMatricula() {
    }

    public static String construirPrefijo(String nombreCarrera, java.sql.Date fechaInicioPeriodo) {
        String anioActual = String.valueOf(Year.now().getValue());
        return anioActual + resolverDigitoPeriodo(fechaInicioPeriodo) + resolverSiglaCarrera(nombreCarrera);
    }

    public static String resolverDigitoPeriodo(java.sql.Date fechaInicio) {
        LocalDate fecha = fechaInicio.toLocalDate();
        int mes = fecha.getMonthValue();
        if (mes <= 4) return "1";
        if (mes <= 8) return "2";
        return "3";
    }

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
