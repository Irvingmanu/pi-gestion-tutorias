package mx.edu.utez.pigestiontutorias.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidad para calcular el hash SHA-256 de contraseñas en texto plano, usado para
 * almacenar y verificar contraseñas de usuario sin guardarlas en texto claro.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-08-19
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * Calcula el hash SHA-256 de un texto plano y lo devuelve como cadena hexadecimal.
     * @param textoPlano el texto (contraseña) a hashear
     * @return la representación hexadecimal del hash SHA-256 del texto
     * @throws IllegalStateException si el algoritmo SHA-256 no está disponible en la JVM
     */
    public static String hash(String textoPlano) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(textoPlano.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en esta JVM", e);
        }
    }
}
