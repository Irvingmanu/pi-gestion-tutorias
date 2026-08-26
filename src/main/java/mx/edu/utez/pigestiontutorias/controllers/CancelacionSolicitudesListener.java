package mx.edu.utez.pigestiontutorias.controllers;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import mx.edu.utez.pigestiontutorias.models.dao.SolicitudDao;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Listener del contexto de la aplicación que programa una tarea periódica en segundo
 * plano para cancelar automáticamente las solicitudes de tutoría vencidas.
 * @author J4IROXD
 * @version 1.0
 * @since 2026-08-21
 */
@WebListener
public class CancelacionSolicitudesListener implements ServletContextListener {

    private static final long INTERVALO_MINUTOS = 30;

    private ScheduledExecutorService scheduler;
    private final SolicitudDao solicitudDao = new SolicitudDao();

    /**
     * Se ejecuta al arrancar la aplicación: crea un scheduler de un solo hilo daemon
     * y programa en él, a intervalos fijos, la cancelación de solicitudes vencidas.
     * @param sce el evento de inicialización del contexto de la aplicación
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread hilo = new Thread(r, "cancelacion-solicitudes-vencidas");
            hilo.setDaemon(true);
            return hilo;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                solicitudDao.cancelarSolicitudesVencidas();
            } catch (Exception e) {

                System.err.println("Error en la tarea programada de cancelación de solicitudes: "
                        + e.getMessage());
                e.printStackTrace();
            }
        }, 0, INTERVALO_MINUTOS, TimeUnit.MINUTES);

        System.out.println("Tarea programada de cancelación de solicitudes vencidas iniciada (cada "
                + INTERVALO_MINUTOS + " minutos).");
    }

    /**
     * Se ejecuta al detener la aplicación: apaga ordenadamente el scheduler de la
     * tarea de cancelación de solicitudes, forzando su cierre si no termina a tiempo.
     * @param sce el evento de destrucción del contexto de la aplicación
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {

        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Tarea programada de cancelación de solicitudes vencidas detenida.");
    }
}