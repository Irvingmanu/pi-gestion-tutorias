package mx.edu.utez.pigestiontutorias.models;

public class ReporteTutor {

        private int alumnosAtendidos;
        private int canalizaciones;
        private int gruposAtendidos;
        private int asistencias;

        public int getAlumnosAtendidos() { return alumnosAtendidos; }
        public void setAlumnosAtendidos(int alumnosAtendidos) { this.alumnosAtendidos = alumnosAtendidos; }

        public int getCanalizaciones() { return canalizaciones; }
        public void setCanalizaciones(int canalizaciones) { this.canalizaciones = canalizaciones; }

        public int getGruposAtendidos() { return gruposAtendidos; }
        public void setGruposAtendidos(int gruposAtendidos) { this.gruposAtendidos = gruposAtendidos; }

        public int getAsistencias() { return asistencias; }
        public void setAsistencias(int asistencias) { this.asistencias = asistencias; }

        public int getPctAtendidos() {
            int total = alumnosAtendidos + canalizaciones + asistencias;
            return total == 0 ? 0 : Math.round(alumnosAtendidos * 100f / total);
        }
        public int getPctCanalizaciones() {
            int total = alumnosAtendidos + canalizaciones + asistencias;
            return total == 0 ? 0 : Math.round(canalizaciones * 100f / total);
        }
        public int getPctAsistencias() {
            int total = alumnosAtendidos + canalizaciones + asistencias;
            return total == 0 ? 0 : 100 - getPctAtendidos() - getPctCanalizaciones();
        }
}
