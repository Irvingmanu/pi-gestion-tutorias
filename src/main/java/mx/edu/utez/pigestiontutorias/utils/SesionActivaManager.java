package mx.edu.utez.pigestiontutorias.utils;

import java.util.concurrent.ConcurrentHashMap;

// Controla que cada usuario (identificado por su correo institucional) solo tenga
// UNA sesion activa a la vez. Si inicia sesion en otro navegador/pestaña de incognito,
// la sesion anterior queda invalidada automaticamente en el siguiente request que haga.
public class SesionActivaManager {

    // correo institucional (en mayusculas) -> ID de la HttpSession vigente
    private static final ConcurrentHashMap<String, String> sesionesActivas = new ConcurrentHashMap<>();

    private SesionActivaManager() {
    }

    // Se llama al hacer login exitoso. Registra esta sesion como la unica valida
    // para ese correo, pisando el registro anterior si ya existia uno.
    public static void registrarSesion(String correo, String sessionId) {
        if (correo == null || sessionId == null) return;
        sesionesActivas.put(correo.trim().toUpperCase(), sessionId);
    }

    // Se llama en cada request (filtro). True si esta sessionId sigue siendo
    // la sesion activa reconocida para ese correo.
    public static boolean esSesionValida(String correo, String sessionId) {
        if (correo == null || sessionId == null) return false;
        String activa = sesionesActivas.get(correo.trim().toUpperCase());
        return sessionId.equals(activa);
    }

    // Se llama en logout. Solo borra el registro si la sesion que cierra sesion
    // es efectivamente la activa (evita que un logout de una sesion vieja/invalida
    // borre por error el registro de una sesion nueva y legitima).
    public static void eliminarSesion(String correo, String sessionId) {
        if (correo == null || sessionId == null) return;
        sesionesActivas.remove(correo.trim().toUpperCase(), sessionId);
    }
}