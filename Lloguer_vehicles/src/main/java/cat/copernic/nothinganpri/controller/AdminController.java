
package cat.copernic.nothinganpri.controller;

import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.repository.UsuariJpaRepository;
import cat.copernic.nothinganpri.service.LogQueryService;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AdminController {

    private final UsuariJpaRepository usuariRepository;
    private final LogQueryService logQueryService;

    public AdminController(
            UsuariJpaRepository usuariRepository,
            LogQueryService logQueryService) {
        this.usuariRepository = usuariRepository;
        this.logQueryService = logQueryService;
    }

    @GetMapping("/admin/clients")
    public String mostrarClients(Model model) {

        List<ClientJpaEntity> clients = usuariRepository.findByRol("CLIENT");

        model.addAttribute("clients", clients);

        return "admin/clients";
    }

    @GetMapping("/admin/logs")
    public String mostrarLogs(Model model) throws IOException {
        List<String> logs = logQueryService.getLogs();

        model.addAttribute("logs", logs);
        model.addAttribute(
                "logStats",
                logQueryService.getStatistics(logs));

        return "logs";
    }

    @PostMapping("/admin/logs/clear")
    public String borrarLogs() throws IOException {

        try {
            logQueryService.clearLogs();

            logQueryService.registrarInfo(
                    "ADMIN",
                    "S'han esborrat els logs del sistema.");

            return "redirect:/admin/logs";

        } catch (IOException e) {
            logQueryService.registrarError(
                    "ADMIN",
                    "Error en esborrar els logs: " + e.getMessage());
            throw e;
        }
    }
}
