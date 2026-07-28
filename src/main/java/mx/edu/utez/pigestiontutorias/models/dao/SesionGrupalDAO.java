package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.SesionGrupal;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.*;

public class SesionGrupalDAO {

    public int registrarSesion(SesionGrupal sesion) {
        int idGenerado = 0;
        // Ajusta los nombres de las columnas según tu esquema real en la BD
        String query = "INSERT INTO SESION_GRUPAL (id_letra_grupo, id_carrera, id_cuatrimestre, fecha, acuerdos, asesorias_grupales, temas_tratados) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, sesion.getIdLetraGrupo());
            ps.setInt(2, sesion.getIdCarrera());
            ps.setInt(3, sesion.getIdCuatrimestre());
            ps.setDate(4, sesion.getFecha());
            ps.setString(5, sesion.getAcuerdos());
            ps.setString(6, sesion.getAsesoriasGrupales());
            ps.setString(7, sesion.getTemasTratados());

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1); // Recuperamos el ID de la nueva sesión
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idGenerado;
    }
}