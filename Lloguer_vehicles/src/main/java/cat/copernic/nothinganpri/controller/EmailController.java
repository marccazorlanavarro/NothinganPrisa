
package cat.copernic.nothinganpri.controller;

import cat.copernic.nothinganpri.service.EmailService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/enviar")
    public String enviarCorreo(
            @RequestParam String destinatario,
            @RequestParam String asunto,
            @RequestParam String contenido) {

        emailService.enviarCorreo(
                destinatario,
                asunto,
                contenido
        );

        return "Correo enviado correctamente";
    }
}
