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
        this.user = System.getenv("MAIL_USER");
        this.password = System.getenv("MAIL_PASSWORD");

        if (this.user == null || this.password == null) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("credentials.properties")) {
                Properties props = new Properties();
                if (in != null) {
                    props.load(in);
                    this.user = props.getProperty("mail.user");
                    this.password = props.getProperty("mail.password");
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