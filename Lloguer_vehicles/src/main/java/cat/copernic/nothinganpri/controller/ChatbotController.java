package cat.copernic.nothinganpri.controller;

import cat.copernic.nothinganpri.dto.ChatRequest;
import cat.copernic.nothinganpri.dto.ChatResponse;
import cat.copernic.nothinganpri.dto.PreguntaFrequent;
import cat.copernic.nothinganpri.service.ChatbotLimitException;
import cat.copernic.nothinganpri.service.ChatbotService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Només rep la petició, crida el servei i tradueix els errors a codis HTTP. */
@RestController
public class ChatbotController {

    private final ChatbotService service;

    public ChatbotController(ChatbotService service) {
        this.service = service;
    }

    // Públic (també per a visitants sense sessió). Veure SecurityConfig.
    @PostMapping("/api/chatbot/preguntar")
    public ResponseEntity<ChatResponse> preguntar(@RequestBody ChatRequest request,
                                                  Authentication authentication,
                                                  HttpServletRequest http) {
        boolean identificat = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        String email = identificat ? authentication.getName() : null;
        String clauLimit = identificat ? email : http.getRemoteAddr();

        try {
            return ResponseEntity.ok(new ChatResponse(
                    service.preguntar(request.getMissatge(), email, clauLimit)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ChatResponse(e.getMessage()));
        } catch (ChatbotLimitException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ChatResponse(e.getMessage()));
        }
    }

    // Sota /agent/**: només AGENT i ADMIN (ja ho limita SecurityConfig).
    // Exemple: /agent/chatbot/preguntes-frequents?limit=10
    @GetMapping("/agent/chatbot/preguntes-frequents")
    public List<PreguntaFrequent> preguntesFrequents(@RequestParam(defaultValue = "10") int limit) {
        return service.preguntesFrequents(limit);
    }
}