package mx.edu.utez.pigestiontutorias.utils;

import java.util.regex.Pattern;

/**
 * Utilidad para verificar y cambiar la contraseña de un usuario, aplicando las
 * reglas de validación del sistema (campos completos, coincidencia de confirmación,
 * requisitos de complejidad y no repetición de la contraseña anterior) y devolviendo
 * el resultado como una cadena JSON simple.
 * @author J4IROXD
 * @version 1.0
 * @since 2026-08-20
 */
public class CambioPasswordUtil {

    private static final Pattern MAYUSCULA = Pattern.compile(".*[A-Z].*");
    private static final Pattern NUMERO = Pattern.compile(".*[0-9].*");

    /**
     * Verifica si una contraseña en texto plano corresponde al hash almacenado.
     * @param passwordActual la contraseña en texto plano a verificar
     * @param hashGuardado el hash SHA-256 almacenado contra el cual comparar
     * @return una cadena JSON con la propiedad "exito" indicando si la contraseña es válida
     */
    public static String verificarPassword(String passwordActual, String hashGuardado) {
        boolean valido = passwordActual != null
                && PasswordUtil.hash(passwordActual).equals(hashGuardado);
        return "{\"exito\":" + valido + "}";
    }

    /**
     * Contrato funcional que encapsula la operación de persistencia de la nueva
     * contraseña (sin hashear), permitiendo desacoplar esta utilidad del origen de datos.
     */
    public interface ActualizadorPassword {
        /**
         * Actualiza la contraseña del usuario en el origen de datos correspondiente.
         * @param nuevaPasswordSinHash la nueva contraseña en texto plano a persistir
         * @return {@code true} si la actualización se realizó correctamente; {@code false} en caso contrario
         */
        boolean actualizar(String nuevaPasswordSinHash);
    }

    /**
     * Orquesta el flujo completo de cambio de contraseña: valida que los campos estén
     * completos, que la contraseña actual coincida con el hash guardado, que la nueva
     * contraseña y su confirmación coincidan, que cumpla los requisitos de complejidad,
     * que no sea igual a la actual, y finalmente delega la persistencia al actualizador.
     * @param passwordActual la contraseña actual proporcionada por el usuario
     * @param passwordNueva la nueva contraseña deseada
     * @param passwordConfirmar la confirmación de la nueva contraseña
     * @param hashGuardado el hash de la contraseña actualmente almacenado
     * @param actualizador callback encargado de persistir la nueva contraseña
     * @return una cadena JSON describiendo el resultado de la operación (éxito o el error específico)
     */
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

        if (!cumpleRequisitos(passwordNueva)) {
            return "{\"exito\":false,\"campo\":\"nueva\",\"mensaje\":\"La nueva contraseña debe tener entre 8 y 64 caracteres, con al menos una mayúscula y un número.\"}";
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

    /**
     * Verifica que una contraseña cumpla los requisitos de complejidad: longitud entre
     * 8 y 64 caracteres, al menos una mayúscula y al menos un número.
     * @param password la contraseña en texto plano a validar
     * @return {@code true} si cumple todos los requisitos; {@code false} en caso contrario
     */
    private static boolean cumpleRequisitos(String password) {
        return password.length() >= 8
                && password.length() <= 64
                && MAYUSCULA.matcher(password).matches()
                && NUMERO.matcher(password).matches();
    }

    /**
     * Determina si una respuesta JSON generada por esta utilidad representa una operación exitosa.
     * @param resultadoJson la cadena JSON devuelta por {@link #verificarPassword} o {@link #cambiarPassword}
     * @return {@code true} si el JSON contiene "exito":true; {@code false} en caso contrario
     */
    public static boolean fueExitoso(String resultadoJson) {
        return resultadoJson.contains("\"exito\":true");
    }
}