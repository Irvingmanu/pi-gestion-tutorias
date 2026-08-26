package mx.edu.utez.pigestiontutorias.utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;
import java.util.Properties;

/**
 * Utilidad para el envío de correos electrónicos del sistema (recuperación de contraseña,
 * notificaciones y recordatorios de canalización, alertas de tutorías grupales, recordatorios
 * de solicitudes de tutoría y confirmaciones de cambio de contraseña) usando SMTP de Gmail.
 * @author Irvingmanu
 * @version 1.0
 * @since 2026-07-17
 */
public class EmailSender {

    private String user;
    private String password;

    /**
     * Construye el remitente, obteniendo las credenciales SMTP primero desde las variables
     * de entorno SMTP_USER/SMTP_PASS, y si no están disponibles, desde el archivo
     * credentials.properties del classpath.
     */
    public EmailSender() {
        this.user = System.getenv("SMTP_USER");
        this.password = System.getenv("SMTP_PASS");

        if (this.user == null || this.password == null) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("credentials.properties")) {
                Properties props = new Properties();
                if (in != null) {
                    props.load(in);
                    this.user = props.getProperty("smtp.user");
                    this.password = props.getProperty("smtp.pass");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Construye la sesión de correo autenticada contra el servidor SMTP de Gmail.
     * @return la sesión de JavaMail configurada con las credenciales del remitente
     */
    private Session getSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });
    }

    /**
     * Envía al correo del usuario el código de recuperación de contraseña.
     * @param destEmail el correo electrónico destinatario
     * @param codigo el código de recuperación de 6 caracteres a incluir en el mensaje
     * @return {@code true} si el correo se envió correctamente; {@code false} si ocurrió un error de envío
     */
    public boolean enviarCodigoRecuperacion(String destEmail, String codigo) {
        try {
            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destEmail));
            message.setSubject("Código de Recuperación de Contraseña - UTEZ");

            String htmlContent = "<h2>Recuperación de contraseña</h2>"
                    + "<p>Usa el siguiente código de 6 caracteres:</p>"
                    + "<h1 style='color:#00847b;'>" + codigo + "</h1>"
                    + "<p>Si no fuiste tú, ignora este mensaje.</p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Notifica al encargado de un área que un tutor canalizó a un alumno hacia ella,
     * incluyendo un enlace para confirmar la atención de la canalización.
     * @param destEmail el correo electrónico del encargado del área destino
     * @param nombreEncargado el nombre del encargado del área destino
     * @param nombreArea el nombre del área hacia la que se canalizó al alumno
     * @param nombreAlumno el nombre completo del alumno canalizado
     * @param matricula la matrícula del alumno canalizado
     * @param motivoODetalle el motivo o detalle de la canalización
     * @param linkConfirmacion el enlace que el encargado debe usar para confirmar la atención
     * @return {@code true} si el correo se envió correctamente; {@code false} si ocurrió un error de envío
     */
    public boolean enviarConfirmacionCanalizacion(String destEmail, String nombreEncargado, String nombreArea,
                                                  String nombreAlumno, String matricula, String motivoODetalle,
                                                  String linkConfirmacion) {
        try {
            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destEmail));
            message.setSubject("Nueva canalización - " + nombreArea + " - UTEZ");

            String htmlContent = "<h2>Nueva canalización a tu área</h2>"
                    + "<p>Hola " + nombreEncargado + ",</p>"
                    + "<p>Un tutor canalizó a un alumno a <strong>" + nombreArea + "</strong>:</p>"
                    + "<p><strong>Alumno:</strong> " + nombreAlumno + " (matrícula " + matricula + ")<br>"
                    + "<strong>Motivo:</strong> " + motivoODetalle + "</p>"
                    + "<p>Cuando hayas atendido al alumno, confirma la canalización dando clic aquí:</p>"
                    + "<p><a href='" + linkConfirmacion + "' style='color:#00847b;'>Confirmar canalización atendida</a></p>"
                    + "<p>Si el link no funciona, copia y pega esta dirección en tu navegador:<br>" + linkConfirmacion + "</p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envía a un tutor un recordatorio de su avance pendiente en las tutorías grupales
     * del periodo vigente para su grupo asignado.
     * @param destEmail el correo electrónico del tutor
     * @param nombreTutor el nombre del tutor
     * @param grupoAsignado el nombre del grupo asignado al tutor
     * @param realizadas la cantidad de tutorías grupales ya realizadas
     * @param objetivo la cantidad objetivo de tutorías grupales para el periodo
     * @return {@code true} si el correo se envió correctamente; {@code false} si ocurrió un error de envío
     */
    public boolean enviarAlertaTutoriasGrupales(String destEmail, String nombreTutor, String grupoAsignado,
                                                int realizadas, int objetivo) {
        try {
            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destEmail));
            message.setSubject("Recordatorio: tutorías grupales pendientes - UTEZ");

            String htmlContent = "<h2>Seguimiento de tutorías grupales</h2>"
                    + "<p>Hola " + nombreTutor + ",</p>"
                    + "<p>Tu avance de tutorías grupales en el grupo <strong>" + grupoAsignado + "</strong> es de "
                    + "<strong>" + realizadas + " de " + objetivo + "</strong> sesiones registradas en este periodo.</p>"
                    + "<p>Por favor ponte al corriente registrando tus próximas sesiones en el módulo de "
                    + "\"Registro de Tutoría Grupal\".</p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envía a un tutor un recordatorio de que tiene una solicitud de tutoría pendiente por atender.
     * @param destEmail el correo electrónico del tutor
     * @param nombreTutor el nombre del tutor
     * @param nombreAlumno el nombre del alumno que generó la solicitud
     * @param asunto el asunto de la solicitud de tutoría
     * @return {@code true} si el correo se envió correctamente; {@code false} si ocurrió un error de envío
     */
    public boolean enviarRecordatorioSolicitud(String destEmail, String nombreTutor, String nombreAlumno, String asunto) {
        try {
            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destEmail));
            message.setSubject("Recordatorio: solicitud de tutoría pendiente - UTEZ");

            String htmlContent = "<h2>Solicitud de tutoría pendiente</h2>"
                    + "<p>Estimado(a) " + nombreTutor + ", se le solicita amablemente que atienda la solicitud de su alumno "
                    + "<strong>" + nombreAlumno + "</strong> en la brevedad posible.</p>"
                    + "<p><strong>Asunto de la solicitud:</strong> " + asunto + "</p>"
                    + "<p>Puedes revisarla y responderla desde el módulo de \"Solicitudes de Tutoría\".</p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envía al encargado de un área un recordatorio de que un alumno canalizado sigue
     * pendiente de atención, incluyendo un enlace para confirmar la atención.
     * @param destEmail el correo electrónico del encargado del área destino
     * @param nombreEncargado el nombre del encargado del área destino
     * @param nombreArea el nombre del área hacia la que se canalizó al alumno
     * @param nombreAlumno el nombre completo del alumno canalizado
     * @param matricula la matrícula del alumno canalizado
     * @param motivoODetalle el motivo o detalle de la canalización
     * @param linkConfirmacion el enlace que el encargado debe usar para confirmar la atención
     * @return {@code true} si el correo se envió correctamente; {@code false} si ocurrió un error de envío
     */
    public boolean enviarRecordatorioCanalizacion(String destEmail, String nombreEncargado, String nombreArea,
                                                  String nombreAlumno, String matricula, String motivoODetalle,
                                                  String linkConfirmacion) {
        try {
            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destEmail));
            message.setSubject("Recordatorio: canalización pendiente - " + nombreArea + " - UTEZ");

            String htmlContent = "<h2>Recordatorio de canalización pendiente</h2>"
                    + "<p>Hola " + nombreEncargado + ",</p>"
                    + "<p>Un alumno sigue esperando ser atendido en <strong>" + nombreArea + "</strong>:</p>"
                    + "<p><strong>Alumno:</strong> " + nombreAlumno + " (matrícula " + matricula + ")<br>"
                    + "<strong>Motivo:</strong> " + motivoODetalle + "</p>"
                    + "<p>Por favor dale seguimiento a la brevedad. Cuando lo hayas atendido, confirma la canalización aquí:</p>"
                    + "<p><a href='" + linkConfirmacion + "' style='color:#00847b;'>Confirmar canalización atendida</a></p>"
                    + "<p>Si el link no funciona, copia y pega esta dirección en tu navegador:<br>" + linkConfirmacion + "</p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Notifica al usuario que su contraseña fue actualizada exitosamente.
     * @param destEmail el correo electrónico del usuario cuya contraseña cambió
     * @return {@code true} si el correo se envió correctamente; {@code false} si ocurrió un error de envío
     */
    public boolean enviarConfirmacionCambio(String destEmail) {
        try {
            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destEmail));
            message.setSubject("Contraseña actualizada - UTEZ");

            String htmlContent = "<h2>¡Contraseña actualizada!</h2>"
                    + "<p>Tu contraseña ha sido cambiada exitosamente.</p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}