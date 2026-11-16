 
package cat.copernic.nothinganpri.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsuariController {

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }
}