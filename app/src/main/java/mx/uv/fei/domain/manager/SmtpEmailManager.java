package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Component
public class SmtpEmailManager {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailManager.class);
    private final Properties configurationProperties = new Properties();

    @Inject
    public SmtpEmailManager() {
        try (InputStream propertiesInputStream = getClass().getClassLoader().getResourceAsStream("mail.properties")) {
            if (propertiesInputStream != null) {
                configurationProperties.load(propertiesInputStream);
            }
        } catch (IOException e) {
            log.error("No se pudo cargar el archivo de configuración mail.properties", e);
        }
    }

    public void sendEmailCopyAsync(String recipientEmail, String subject, String body) {
        CompletableFuture.runAsync(() -> {
            try {
                sendEmail(recipientEmail, subject, body);
            } catch (MessagingException e) {
                log.error("Fallo al enviar correo asíncrono a: {}", recipientEmail, e);
            }
        });
    }

    private void sendEmail(String recipientEmail, String subject, String body) throws MessagingException {
        Session session = Session.getInstance(configurationProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        configurationProperties.getProperty("mail.system.email"),
                        configurationProperties.getProperty("mail.system.password")
                );
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(configurationProperties.getProperty("mail.system.email")));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }
}
