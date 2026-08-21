package mx.edu.utez.pigestiontutorias.listeners;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import mx.edu.utez.pigestiontutorias.models.dao.SolicitudDao;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Complementa la cancelación "al cargar" del SolicitudServlet: esta tarea
// corre en segundo plano sin depender de que alguien visite una pantalla,
// para que las solicitudes vencidas no se queden como "Pendiente" por horas
// si nadie entra a revisarlas.
@WebListener
public class CancelacionSolicitudesListener implements ServletContextListener {

    // Cada cuánto se revisa si hay solicitudes "Pendiente" vencidas.
    // 30 min es un buen balance: no satura la BD y el margen de "1 día o
    // menos" que pide el negocio no necesita algo más agresivo que esto.
    private static final long INTERVALO_MINUTOS = 30;

    private ScheduledExecutorService scheduler;
    private final SolicitudDao solicitudDao = new SolicitudDao();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Hilo daemon: si Tomcat se apaga de golpe, este hilo no impide
        // que el proceso termine.
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread hilo = new Thread(r, "cancelacion-solicitudes-vencidas");
            hilo.setDaemon(true);
            return hilo;
        });

        // La primera corrida es inmediata (delay inicial = 0), y luego se
        // repite cada INTERVALO_MINUTOS mientras la app siga viva.
        scheduler.scheduleAtFixedRate(() -> {
            try {
                solicitudDao.cancelarSolicitudesVencidas();
            } catch (Exception e) {
                // Nunca dejamos que una excepción mate el scheduler: si truena
                // una corrida, simplemente se reintenta en el siguiente ciclo.
                System.err.println("Error en la tarea programada de cancelación de solicitudes: "
                        + e.getMessage());
                e.printStackTrace();
            }
        }, 0, INTERVALO_MINUTOS, TimeUnit.MINUTES);

        System.out.println("Tarea programada de cancelación de solicitudes vencidas iniciada (cada "
                + INTERVALO_MINUTOS + " minutos).");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Se apaga limpiamente al hacer redeploy/detener Tomcat, para no
        // dejar hilos huérfanos ni warnings de memory leak en el log.
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