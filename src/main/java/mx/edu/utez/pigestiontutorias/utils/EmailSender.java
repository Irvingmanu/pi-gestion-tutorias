package mx.edu.utez.pigestiontutorias.utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;
import java.util.Properties;

public class EmailSender {

    private String user;
    private String password;

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