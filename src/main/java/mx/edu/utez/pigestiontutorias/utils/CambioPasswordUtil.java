package mx.edu.utez.pigestiontutorias.utils;

/**
 * Logica reutilizable para el flujo de "Cambiar contraseña", pensada para
 * usarse igual desde PerfilServlet de Coordinador, Tutor y Alumno.
 * Cada Servlet le pasa el hash guardado del usuario y una funcion (lambda)
 * que sabe como persistir la password en SU propio DAO.
 */
public class CambioPasswordUtil {

    /** Paso 1 del modal: solo confirma si la contraseña actual es correcta. */
    public static String verificarPassword(String passwordActual, String hashGuardado) {
        boolean valido = passwordActual != null
                && PasswordUtil.hash(passwordActual).equals(hashGuardado);
        return "{\"exito\":" + valido + "}";
    }

    /** Cada rol implementa esto con su propio DAO, ej: coordinadorDAO::actualizarPassword */
    public interface ActualizadorPassword {
        boolean actualizar(String nuevaPasswordSinHash);
    }

    /** Paso 2 del modal: valida todo y, si esta bien, guarda la nueva contraseña. */
    public static String cambiarPassword(String passwordActual, String passwordNueva, String passwordConfirmar,
                                         String hashGuardado, ActualizadorPassword actualizador) {

        if (passwordActual == null || passwordNueva == null || passwordConfirmar == null
                || passwordActual.isBlank() || passwordNueva.isBlank() || passwordConfirmar.isBlank()) {
            return "{\"exito\":false,\"mensaje\":\"Completa todos los campos.\"}";
        }

        if (!PasswordUtil.hash(passwordActual).equals(hashGuardado)) {
            return "{\"exito\":false,\"campo\":\"actual\",\"mensaje\":\"La contraseña actual es incorrecta.\"}";
        }

        if (!passwordNueva.equals(passwordConfirmar)) {
            return "{\"exito\":false,\"campo\":\"confirmar\",\"mensaje\":\"Las contraseñas no coinciden.\"}";
        }

        if (passwordNueva.length() < 8) {
            return "{\"exito\":false,\"campo\":\"nueva\",\"mensaje\":\"La nueva contraseña debe tener al menos 8 caracteres.\"}";
        }

        if (PasswordUtil.hash(passwordNueva).equals(hashGuardado)) {
            return "{\"exito\":false,\"campo\":\"nueva\",\"mensaje\":\"La nueva contraseña no puede ser igual a la actual.\"}";
        }

        boolean actualizado = actualizador.actualizar(passwordNueva);

        if (!actualizado) {
            return "{\"exito\":false,\"mensaje\":\"No se pudo actualizar la contraseña, intenta de nuevo.\"}";
        }

        return "{\"exito\":true,\"mensaje\":\"Tu contraseña se actualizó correctamente.\"}";
    }

    /** Util para que el Servlet sepa si debe disparar el correo de confirmacion. */
    public static boolean fueExitoso(String resultadoJson) {
        return resultadoJson.contains("\"exito\":true");
    }
}