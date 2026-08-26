package mx.edu.utez.pigestiontutorias.utils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Utilidad en memoria que controla la sesión activa de cada usuario por correo electrónico,
 * permitiendo forzar una única sesión válida por usuario (usada para invalidar sesiones
 * anteriores cuando el usuario inicia sesión desde otro dispositivo o navegador).
 * @author J4IROXD
 * @version 1.0
 * @since 2026-08-20
 */
public class SesionActivaManager {

    private static final ConcurrentHashMap<String, String> sesionesActivas = new ConcurrentHashMap<>();

    private SesionActivaManager() {
    }

    /**
     * Registra el identificador de sesión como el único activo para el correo dado,
     * reemplazando cualquier sesión previamente registrada.
     * @param correo el correo electrónico del usuario
     * @param sessionId el identificador de la sesión HTTP a registrar como activa
     */
    public static void registrarSesion(String correo, String sessionId) {
        if (correo == null || sessionId == null) return;
        sesionesActivas.put(correo.trim().toUpperCase(), sessionId);
    }

    /**
     * Verifica si el identificador de sesión dado es el que está actualmente activo
     * para el correo indicado.
     * @param correo el correo electrónico del usuario
     * @param sessionId el identificador de la sesión HTTP a verificar
     * @return {@code true} si la sesión coincide con la activa registrada; {@code false} en caso contrario
     */
    public static boolean esSesionValida(String correo, String sessionId) {
        if (correo == null || sessionId == null) return false;
        String activa = sesionesActivas.get(correo.trim().toUpperCase());
        return sessionId.equals(activa);
    }

    /**
     * Elimina el registro de sesión activa del correo dado, solo si coincide con el
     * identificador de sesión proporcionado.
     * @param correo el correo electrónico del usuario
     * @param sessionId el identificador de la sesión HTTP a eliminar
     */
    public static void eliminarSesion(String correo, String sessionId) {
        if (correo == null || sessionId == null) return;
        sesionesActivas.remove(correo.trim().toUpperCase(), sessionId);
    }
}