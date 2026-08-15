package mx.edu.utez.pigestiontutorias.models.dao;

import mx.edu.utez.pigestiontutorias.models.Academia;
import mx.edu.utez.pigestiontutorias.utils.SQLConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Catalogo estatico: ACADEMIA se puebla directo en BD via script SQL, no hay alta/edicion/baja
// desde la aplicacion. Este DAO solo expone lectura, para llenar los <select> de Academia en
// formulario-alumno.jsp, formulario-tutor.jsp y el filtro en cascada de asignacion.jsp.
public class AcademiaDao {

    public List<Academia> getAll() {
        List<Academia> lista = new ArrayList<>();
        String sql = "SELECT ID_ACADEMIA, NOMBRE FROM ACADEMIA ORDER BY NOMBRE";

        try (Connection con = SQLConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Academia(rs.getInt("ID_ACADEMIA"), rs.getString("NOMBRE")));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener las academias: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }
}
