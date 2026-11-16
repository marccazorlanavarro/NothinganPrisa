package cat.copernic.nothinganpri.controller;

import cat.copernic.nothinganpri.service.VehicleService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * RF45 Filtre de vehicles. Es fa amb GET perquè només consultem dades (no en modifiquem).
 */
@Controller
public class DisponibilitatController {

    private final VehicleService service;

    public DisponibilitatController(VehicleService service) {
        this.service = service;
    }

    @GetMapping("/vehicles/disponibilitat")
    public String filtrar(
            @RequestParam(required = false) String matricula,
            @RequestParam(required = false) String marca,
            @RequestParam(name = "model", required = false) String modelVehicle,
            @RequestParam(required = false) Integer any,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInici,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFi,
            Model model) {

        // Tornem els filtres a la vista perquè el formulari no s'esborri en cercar
        model.addAttribute("matricula", matricula);
        model.addAttribute("marca", marca);
        model.addAttribute("modelVehicle", modelVehicle);
        model.addAttribute("any", any);
        model.addAttribute("dataInici", dataInici);
        model.addAttribute("dataFi", dataFi);

        try {
            model.addAttribute("vehicles",
                    service.filtrar(matricula, marca, modelVehicle, any, dataInici, dataFi));
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorNegoci", e.getMessage());
        }

        return "consulta-disponibilitat";
    }
}
