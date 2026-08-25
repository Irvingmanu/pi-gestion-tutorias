package mx.edu.utez.pigestiontutorias.utils;

import java.util.concurrent.ConcurrentHashMap;

public class SesionActivaManager {

    private static final ConcurrentHashMap<String, String> sesionesActivas = new ConcurrentHashMap<>();

    private SesionActivaManager() {
    }

    public static void registrarSesion(String correo, String sessionId) {
        if (correo == null || sessionId == null) return;
        sesionesActivas.put(correo.trim().toUpperCase(), sessionId);
    }

    public static boolean esSesionValida(String correo, String sessionId) {
        if (correo == null || sessionId == null) return false;
        String activa = sesionesActivas.get(correo.trim().toUpperCase());
        return sessionId.equals(activa);
    }

    public static void eliminarSesion(String correo, String sessionId) {
        if (correo == null || sessionId == null) return;
        sesionesActivas.remove(correo.trim().toUpperCase(), sessionId);
    }
}