package mx.edu.utez.pigestiontutorias.models.dao;
import java.util.List;

public interface Dao<T, K> {
    /**
     * Crea un nuevo registro de la entidad en la fuente de datos.
     * @param entidad la entidad a crear
     * @return {@code true} si la operación se realizó con éxito; {@code false} en caso contrario
     */
    boolean create(T entidad);

    /**
     * Obtiene todos los registros de la entidad.
     * @return la lista completa de entidades encontradas
     */
    List<T> getAll();

    /**
     * Obtiene una entidad a partir de su llave primaria.
     * @param id la llave primaria de la entidad buscada
     * @return la entidad encontrada, o {@code null} si no existe
     */
    T getById(K id);

    /**
     * Actualiza los datos de una entidad existente.
     * @param entidad la entidad con los datos actualizados
     * @return {@code true} si la operación se realizó con éxito; {@code false} en caso contrario
     */
    boolean update(T entidad);

    /**
     * Elimina una entidad a partir de su llave primaria.
     * @param id la llave primaria de la entidad a eliminar
     * @return {@code true} si la operación se realizó con éxito; {@code false} en caso contrario
     */
    boolean delete(K id);
}
