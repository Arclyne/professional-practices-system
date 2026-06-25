package mx.uv.fei.domain.manager.infrastructure;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Component
public class SmtpEmailManager {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailManager.class);
    private static final String MAIL_CONFIG_FILE = "mail.properties";
    private static final String MAIL_EMAIL_PROPERTY = "mail.system.email";
    private static final String MAIL_PASSWORD_PROPERTY = "mail.system.password";

    private final Properties mailProperties = new Properties();

    @Inject
    public SmtpEmailManager() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(MAIL_CONFIG_FILE)) {
            if (inputStream != null) {
                mailProperties.load(inputStream);
            }
        } catch (IOException e) {
            log.error("No se pudo cargar el archivo de configuración {}.", MAIL_CONFIG_FILE, e);
        }
    }

    public void sendEmailCopyAsync(String recipientEmail, String subject, String body) {
        CompletableFuture.runAsync(() -> {
            try {
                sendEmail(recipientEmail, subject, body);
            } catch (MessagingException e) {
                log.error("Fallo al enviar correo asíncrono a: {}.", recipientEmail, e);
            }
        });
    }

    private void sendEmail(String recipientEmail, String subject, String body) throws MessagingException {
        Session mailSession = buildMailSession();
        Message message = buildMessage(mailSession, recipientEmail, subject, body);
        Transport.send(message);
    }

    private Session buildMailSession() {
        return Session.getInstance(mailProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        mailProperties.getProperty(MAIL_EMAIL_PROPERTY),
                        mailProperties.getProperty(MAIL_PASSWORD_PROPERTY)
                );
            }
        });
    }

    private Message buildMessage(Session mailSession, String recipientEmail, String subject, String body) throws MessagingException {
        Message message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(mailProperties.getProperty(MAIL_EMAIL_PROPERTY)));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject(subject);
        message.setText(body);
        return message;
    }
}