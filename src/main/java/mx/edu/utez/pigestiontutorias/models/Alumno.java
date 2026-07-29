package mx.edu.utez.pigestiontutorias.models;

public class Alumno {
    private Integer idAlumno;
    private String matricula;
    private String nombres;
    private String apellidos;
    private String correoInstitucional;
    private String telefono;
    private Integer idGenero;
    private Integer idCarrera;
    private Integer idCuatrimestre;
    private Integer idLetraGrupo;
    private Integer idUsuario;
    private String activo;

    private Carrera carrera;
    private Cuatrimestre cuatrimestre;
    private LetraGrupo letraGrupo;

    public Alumno() {
    }

    public Alumno(String matricula, String nombres, String apellidos, String correoInstitucional,
                  String telefono, Integer idGenero, Integer idCarrera, Integer idCuatrimestre,
                  Integer idLetraGrupo, Integer idUsuario) {
        this.matricula = matricula;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correoInstitucional = correoInstitucional;
        this.telefono = telefono;
        this.idGenero = idGenero;
        this.idCarrera = idCarrera;
        this.idCuatrimestre = idCuatrimestre;
        this.idLetraGrupo = idLetraGrupo;
        this.idUsuario = idUsuario;
    }

    public Integer getIdAlumno() { return idAlumno; }
    public void setIdAlumno(Integer idAlumno) { this.idAlumno = idAlumno; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreoInstitucional() { return correoInstitucional; }
    public void setCorreoInstitucional(String correoInstitucional) { this.correoInstitucional = correoInstitucional; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Integer getIdGenero() { return idGenero; }
    public void setIdGenero(Integer idGenero) { this.idGenero = idGenero; }

    public Integer getIdCarrera() { return idCarrera; }
    public void setIdCarrera(Integer idCarrera) { this.idCarrera = idCarrera; }

    public Integer getIdCuatrimestre() { return idCuatrimestre; }
    public void setIdCuatrimestre(Integer idCuatrimestre) { this.idCuatrimestre = idCuatrimestre; }

    public Integer getIdLetraGrupo() { return idLetraGrupo; }
    public void setIdLetraGrupo(Integer idLetraGrupo) { this.idLetraGrupo = idLetraGrupo; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getActivo() { return activo; }
    public void setActivo(String activo) { this.activo = activo; }

    public Carrera getCarrera() { return carrera; }
    public void setCarrera(Carrera carrera) { this.carrera = carrera; }

    public Cuatrimestre getCuatrimestre() { return cuatrimestre; }
    public void setCuatrimestre(Cuatrimestre cuatrimestre) { this.cuatrimestre = cuatrimestre; }

    public LetraGrupo getLetraGrupo() { return letraGrupo; }
    public void setLetraGrupo(LetraGrupo letraGrupo) { this.letraGrupo = letraGrupo; }

    @Override
    public String toString() {
        return matricula + ',' + nombres + ',' + apellidos + ',' + correoInstitucional;
    }
}