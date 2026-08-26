package mx.edu.utez.pigestiontutorias.models;

/**
 * Resumen estadístico del desempeño de un tutor: alumnos atendidos, canalizaciones,
 * grupos atendidos y asistencias, con sus respectivos porcentajes calculados.
 * @author J4IROXD
 * @version 1.0
 * @since 2026-07-28
 */
public class ReporteTutor {

        private int alumnosAtendidos;
        private int canalizaciones;
        private int gruposAtendidos;
        private int asistencias;

        /** @return la cantidad de alumnos atendidos */
        public int getAlumnosAtendidos() { return alumnosAtendidos; }
        /** @param alumnosAtendidos la cantidad de alumnos atendidos a asignar */
        public void setAlumnosAtendidos(int alumnosAtendidos) { this.alumnosAtendidos = alumnosAtendidos; }

        /** @return la cantidad de canalizaciones */
        public int getCanalizaciones() { return canalizaciones; }
        /** @param canalizaciones la cantidad de canalizaciones a asignar */
        public void setCanalizaciones(int canalizaciones) { this.canalizaciones = canalizaciones; }

        /** @return la cantidad de grupos atendidos */
        public int getGruposAtendidos() { return gruposAtendidos; }
        /** @param gruposAtendidos la cantidad de grupos atendidos a asignar */
        public void setGruposAtendidos(int gruposAtendidos) { this.gruposAtendidos = gruposAtendidos; }

        /** @return la cantidad de asistencias */
        public int getAsistencias() { return asistencias; }
        /** @param asistencias la cantidad de asistencias a asignar */
        public void setAsistencias(int asistencias) { this.asistencias = asistencias; }

        /**
         * Calcula el porcentaje que representan los alumnos atendidos sobre el total
         * (alumnos atendidos + canalizaciones + asistencias).
         * @return el porcentaje de alumnos atendidos, redondeado, o 0 si el total es cero
         */
        public int getPctAtendidos() {
            int total = alumnosAtendidos + canalizaciones + asistencias;
            return total == 0 ? 0 : Math.round(alumnosAtendidos * 100f / total);
        }
        /**
         * Calcula el porcentaje que representan las canalizaciones sobre el total
         * (alumnos atendidos + canalizaciones + asistencias).
         * @return el porcentaje de canalizaciones, redondeado, o 0 si el total es cero
         */
        public int getPctCanalizaciones() {
            int total = alumnosAtendidos + canalizaciones + asistencias;
            return total == 0 ? 0 : Math.round(canalizaciones * 100f / total);
        }
        /**
         * Calcula el porcentaje que representan las asistencias sobre el total, como el
         * remanente de 100 menos los otros dos porcentajes, para asegurar que la suma sea exacta.
         * @return el porcentaje de asistencias, o 0 si el total es cero
         */
        public int getPctAsistencias() {
            int total = alumnosAtendidos + canalizaciones + asistencias;
            return total == 0 ? 0 : 100 - getPctAtendidos() - getPctCanalizaciones();
        }
}
