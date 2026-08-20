package mx.edu.utez.pigestiontutorias.utils;

import java.time.LocalDate;
import java.time.Year;
import java.util.Locale;
import java.util.Set;

// Logica compartida para armar el PREFIJO de una MATRICULA nueva (todo menos el contador
// final de 3 digitos), usada tanto por GenerarCredencialesServlet (alta individual, boton
// "Asignar a Grupo") como por AlumnoServlet.procesarCargaMasivaAlumnos (carga masiva por
// Excel): las dos rutas deben generar matriculas con exactamente el mismo formato para el
// mismo Grupo, asi que viven en un solo lugar en vez de duplicarse.
//
// Formato de MATRICULA (10 caracteres, igual al REGEX_MATRICULA de AlumnoServlet):
//   [Año actual: 4] + [Digito de Periodo: 1] + [Sigla de Carrera: 2] + [Contador: 3]
//   Ej. 2026 + 3 + ds + 071 = "20263ds071"
public final class GeneradorMatricula {

    private GeneradorMatricula() {
    }

    // Arma el prefijo (Anio+Periodo+Sigla) para el Grupo indicado; el contador de 3 digitos
    // se resuelve aparte, contra la BD, con AlumnoDAO.obtenerSiguienteContador().
    public static String construirPrefijo(String nombreCarrera, java.sql.Date fechaInicioPeriodo) {
        String anioActual = String.valueOf(Year.now().getValue());
        return anioActual + resolverDigitoPeriodo(fechaInicioPeriodo) + resolverSiglaCarrera(nombreCarrera);
    }

    // Digito 1/2/3 del cuatrimestre de ingreso, segun el MES de FECHA_INICIO del periodo
    // (convencion cuatrimestral: Enero-Abril / Mayo-Agosto / Septiembre-Diciembre). Siempre
    // regresa exactamente 1 caracter. OJO: no se usa el ID_PERIODO (la PK autoincremental de
    // PERIODO_ESCOLAR) tal cual, porque esa secuencia no tiene tope de digitos (con
    // suficientes periodos creados llega a 2+ digitos) y el formato de MATRICULA exige
    // exactamente 10 caracteres siempre.
    public static String resolverDigitoPeriodo(java.sql.Date fechaInicio) {
        LocalDate fecha = fechaInicio.toLocalDate();
        int mes = fecha.getMonthValue();
        if (mes <= 4) return "1";
        if (mes <= 8) return "2";
        return "3";
    }

    // No existe una columna SIGLA en CARRERA (solo NOMBRE, NIVEL e ID_ACADEMIA; ver
    // BD_INTEGRADORA_SGT.sql). Lo ideal seria:
    //   SELECT SIGLA FROM CARRERA WHERE ID_CARRERA = ?
    // con una columna nueva que el coordinador capture al dar de alta cada carrera (hoy
    // CARRERA es un catalogo estatico que se puebla por script, ver CarreraDao). Mientras
    // esa columna no exista, se deriva heuristicamente del NOMBRE: la inicial de cada
    // palabra significativa (se ignoran conectores como "de", "en", "del", "TSU", "ING"),
    // hasta 2 letras, en minusculas. Ej. "TSU en Desarrollo de Software" -> "ds".
    public static String resolverSiglaCarrera(String nombreCarrera) {
        if (nombreCarrera == null || nombreCarrera.isBlank()) {
            return "xx";
        }

        Set<String> conectores = Set.of("de", "del", "en", "y", "la", "las", "los", "el", "tsu", "ing");
        StringBuilder sigla = new StringBuilder();

        for (String palabra : nombreCarrera.trim().split("\\s+")) {
            if (sigla.length() >= 2) break;
            // Solo a-z ASCII (sin acentos/ñ): REGEX_MATRICULA en AlumnoServlet no admite
            // nada fuera de [a-zA-Z0-9], asi que una tilde aqui volveria a romper el formato.
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
