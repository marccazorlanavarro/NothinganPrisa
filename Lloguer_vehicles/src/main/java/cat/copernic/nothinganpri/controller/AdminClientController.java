
package cat.copernic.nothinganpri.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.service.ClientService;
import cat.copernic.nothinganpri.service.ReservaService;

/**
 * Controlador per gestionar els clients des del panell de l'agent.
 */
@Controller
@RequestMapping("/agent/clients")
@PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRADOR', 'AGENT')")
public class AdminClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Evita que el formulari principal pugui modificar camps sensibles.
     */
    @InitBinder("client")
    public void configurarBinder(WebDataBinder binder) {

        binder.setDisallowedFields(
                "password",
                "rol",
                "activo",
                "DNI",
                "fotoDniAnverso",
                "fotoDniAnversoTipus",
                "fotoPermiso",
                "fotoPermisoTipus",
                "fotoDniReverso",
                "titularTargeta",
                "numtarjetacredit",
                "caducitatTargeta"
        );
    }

    /**
     * Muestra la ficha de un cliente y su historial de reservas.
     */
    @GetMapping("/{dni}/perfil")
    public String mostrarPerfil(
            @PathVariable String dni,
            Model model,
            RedirectAttributes redirectAttributes) {

        ClientJpaEntity client = clientService.findByDNI(dni)
                .orElse(null);

        if (client == null) {
            redirectAttributes.addFlashAttribute(
                    "error", "No s'ha trobat el client.");

            return "redirect:/agent/clients";
        }

        model.addAttribute("client", client);

        model.addAttribute(
                "reserves",
                reservaService.obtenirHistoricClient(client.getId())
        );

        return "editar-client";
    }

    /**
     * Guarda los datos personales y los documentos del cliente.
     */
    @PostMapping("/{dni}/guardar")
    public String guardarDatos(
            @PathVariable String dni,
            @ModelAttribute("client") ClientJpaEntity dades,
            @RequestParam(value = "fotoDniFile", required = false)
            MultipartFile fotoDni,
            @RequestParam(value = "fotoPermisoFile", required = false)
            MultipartFile fotoPermiso,
            RedirectAttributes redirectAttributes) {

        try {

            ClientJpaEntity actualizado =
                    clientService.actualizarClientAdmin(
                            dni,
                            dades,
                            fotoDni,
                            fotoPermiso
                    );

            redirectAttributes.addFlashAttribute(
                    "missatge",
                    "Les dades del client s'han actualitzat correctament."
            );

            return "redirect:/agent/clients/"
                    + actualizado.getDNI() + "/perfil";

        } catch (IllegalArgumentException | IOException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/agent/clients/" + dni + "/perfil";
        }
    }

    /**
     * Guarda los datos de la tarjeta del cliente.
     */
    @PostMapping("/{dni}/guardar-targeta")
    public String guardarTargeta(
            @PathVariable String dni,
            @RequestParam(required = false) String titularTargeta,
            @RequestParam(required = false) String numtarjetacredit,
            @RequestParam(required = false) String caducitatTargeta,
            RedirectAttributes redirectAttributes) {

        ClientJpaEntity client = clientService.findByDNI(dni)
                .orElse(null);

        if (client == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No s'ha trobat el client."
            );

            return "redirect:/agent/clients";
        }

        client.setTitularTargeta(titularTargeta);
        client.setCaducitatTargeta(caducitatTargeta);

        if (numtarjetacredit != null
                && !numtarjetacredit.isBlank()) {

            client.setNumtarjetacredit(
                    numtarjetacredit.replaceAll("\\s+", "")
            );
        }

        clientService.guardarClient(client);

        redirectAttributes.addFlashAttribute(
                "missatge",
                "Les dades de la targeta s'han actualitzat."
        );

        return "redirect:/agent/clients/" + dni + "/perfil";
    }

    /**
     * Cambia la contraseña del cliente después de comprobarla.
     */
    @PostMapping("/{dni}/guardar-contrasenya")
    public String guardarContrasenya(
            @PathVariable String dni,
            @RequestParam String novaContrasenya,
            @RequestParam String confirmarContrasenya,
            RedirectAttributes redirectAttributes) {

        if (novaContrasenya == null
                || novaContrasenya.length() < 8) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "La contrasenya ha de tenir almenys 8 caràcters."
            );

            return "redirect:/agent/clients/" + dni + "/perfil";
        }

        if (!novaContrasenya.equals(confirmarContrasenya)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Les contrasenyes no coincideixen."
            );

            return "redirect:/agent/clients/" + dni + "/perfil";
        }

        ClientJpaEntity client = clientService.findByDNI(dni)
                .orElse(null);

        if (client == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No s'ha trobat el client."
            );

            return "redirect:/agent/clients";
        }

        client.setPassword(
                passwordEncoder.encode(novaContrasenya)
        );

        clientService.guardarClient(client);

        redirectAttributes.addFlashAttribute(
                "missatge",
                "La contrasenya s'ha canviat correctament."
        );

        return "redirect:/agent/clients/" + dni + "/perfil";
    }
}
