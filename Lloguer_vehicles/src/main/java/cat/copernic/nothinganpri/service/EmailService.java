
package cat.copernic.nothinganpri.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Enviament de correus de text normal.
    public void enviarCorreo(
            String destinatario,
            String asunto,
            String contenido) {

        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(contenido);

        mailSender.send(mensaje);
    }

    // Enviament de correus amb disseny HTML.
    public void enviarCorreoHtml(
            String destinatario,
            String asunto,
            String contenidoHtml) {

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mensaje,
                    true,
                    "UTF-8"
            );

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);

            helper.setText(
                    "Tu solicitud de cancelación ha sido aceptada. "
                    + "Consulta este correo en formato HTML para ver el detalle.",
                    contenidoHtml
            );

            mailSender.send(mensaje);

        } catch (MessagingException e) {
            throw new RuntimeException("Error al preparar el correo HTML.", e);
        }
    }
}
