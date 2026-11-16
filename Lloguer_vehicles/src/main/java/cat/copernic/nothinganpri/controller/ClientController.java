package cat.copernic.nothinganpri.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.nothinganpri.dto.DocumentClient;
import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.service.ClientService;

/**
 * Controlador encarregat de gestionar les operacions relacionades amb els clients,
 * incloent-hi la consulta de fitxes, la visualització de documents i la seva activació
 * o desactivació.
 */
@Controller
public class ClientController {

    @Autowired
    private ClientService clientService;

    // MOSTRAR FITXA DEL CLIENT
    /**
     * Mostra la fitxa d'un client a partir del seu DNI.
     * Si el client no existeix, redirigeix a la llista de clients.
     *
     * @param dni DNI del client que es vol consultar.
     * @param model Model utilitzat per enviar les dades del client a la vista.
     * @return Nom de la vista de la fitxa del client o redirecció a la llista.
     */
    @GetMapping("/clients/{dni}")
    public String mostrarClient(
            @PathVariable String dni,
            Model model) {

        Optional<ClientJpaEntity> client =
                clientService.findByDNI(dni);

        if (client.isEmpty()) {
            return "redirect:/clients";
        }

        model.addAttribute("client", client.get());

        return "fitxa-client";
    }

    // MOSTRAR DOCUMENT DNI
    @GetMapping("/clients/{dni}/document/dni")
    public ResponseEntity<byte[]> veureDni(
            @PathVariable String dni) {

        Optional<DocumentClient> document =
                clientService.obtenirDni(dni);

        return crearRespostaDocument(document);
    }

    // MOSTRAR LLICENCIA
    @GetMapping("/clients/{dni}/document/llicencia")
    public ResponseEntity<byte[]> veureLlicencia(
            @PathVariable String dni) {

        Optional<DocumentClient> document =
                clientService.obtenirLlicencia(dni);

        return crearRespostaDocument(document);
    }

    // GENERAR RESPOSTA DEL DOCUMENT
    /**
     * Genera la resposta HTTP per mostrar un document del client.
     * Si el document no existeix, retorna un error 404. En cas contrari,
     * configura les capçaleres i el tipus de contingut abans de retornar-lo.
     *
     * @param document Document del client que es vol mostrar.
     * @return Resposta HTTP amb les dades del document o un error 404.
     */
    private ResponseEntity<byte[]> crearRespostaDocument(
            Optional<DocumentClient> document) {

        if (document.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        DocumentClient dades = document.get();

        MediaType mediaType =
                obtenirMediaType(dades.getTipus());

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline")
                .header(
                        "X-Content-Type-Options",
                        "nosniff")
                .contentType(mediaType)
                .body(dades.getDades());
    }

    // OBTENIR TIPUS MIME
    /**
     * Obté el tipus MIME del document a partir de la seva cadena de tipus.
     * Si el tipus és nul, buit o no és vàlid, retorna el tipus genèric
     * application/octet-stream.
     *
     * @param tipus Cadena que representa el tipus MIME del document.
     * @return Tipus de contingut MIME corresponent o el tipus genèric.
     */
    private MediaType obtenirMediaType(String tipus) {

        if (tipus == null || tipus.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(tipus);
        } catch (IllegalArgumentException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    // DESACTIVAR CLIENT
    /**
     * Desactiva un client mitjançant el seu identificador.
     * Si no es pot desactivar, mostra un missatge d'error; altrament,
     * informa de l'èxit. Finalment, redirigeix a la fitxa del client
     * si encara existeix, o a la llista general de clients.
     *
     * @param id Identificador del client que es vol desactivar.
     * @param redirectAttributes Atributs temporals per mostrar missatges a la vista.
     * @return Ruta de redirecció després de l'operació.
     */
    @PostMapping("/clients/desactivar/{id}")
    public String desactivarClient(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        boolean desactivat =
                clientService.eliminarClient(id);

        if (!desactivat) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No es pot desactivar el client perquè té reserves pendents, actives o en curs.");

        } else {

            redirectAttributes.addFlashAttribute(
                    "success",
                    "El client s'ha desactivat correctament.");
        }

        Optional<ClientJpaEntity> client =
                clientService.findById(id);

        if (client.isPresent()) {
            return "redirect:/clients/"
                    + client.get().getDNI();
        }

        return "redirect:/clients";
    }

    // ACTIVAR CLIENT
    /**
     * Activa un client mitjançant el seu identificador i mostra un missatge
     * de confirmació abans de redirigir a la llista de clients.
     *
     * @param id Identificador del client que es vol activar.
     * @param redirectAttributes Atributs temporals per mostrar el missatge d'èxit.
     * @return Ruta de redirecció a la llista de clients.
     */
    @PostMapping("/clients/activar/{id}")
    public String activarClient(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        clientService.activarClient(id);

        redirectAttributes.addFlashAttribute(
                "success",
                "El client s'ha activat correctament.");

        return "redirect:/clients";
    }
}