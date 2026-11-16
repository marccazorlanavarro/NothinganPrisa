
package cat.copernic.nothinganpri.controller;

import cat.copernic.nothinganpri.dto.AgentEditarForm;
import cat.copernic.nothinganpri.dto.CanviSolicitud;
import cat.copernic.nothinganpri.entity.AgentJpaEntity;
import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.entity.SolicitudValidacionJpaEntity;
import cat.copernic.nothinganpri.service.AgentService;
import cat.copernic.nothinganpri.service.ClientService;
import cat.copernic.nothinganpri.service.LogQueryService;
import cat.copernic.nothinganpri.service.SolicitudValidacionService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador que gestiona las operaciones de agentes, clientes
 * y solicitudes de validación dentro de la aplicación.
 *
 * Gestiona el listado y filtrado de clientes, la revisión de solicitudes
 * y las operaciones CRUD de los agentes.
 */
@Controller
public class AgentController {

    private final AgentService agentService;
    private final ClientService clientService;
    private final SolicitudValidacionService solicitudService;
    private final LogQueryService logQueryService;

    /**
     * Constructor del controlador que inicializa los servicios necesarios.
     *
     * @param agentService servicio para gestionar los agentes.
     * @param clientService servicio para gestionar los clientes.
     * @param solicitudService servicio para gestionar las solicitudes de validación.
     * @param logQueryService servicio para registrar advertencias y errores.
     */
    public AgentController(
            AgentService agentService,
            ClientService clientService,
            SolicitudValidacionService solicitudService,
            LogQueryService logQueryService) {

        this.agentService = agentService;
        this.clientService = clientService;
        this.solicitudService = solicitudService;
        this.logQueryService = logQueryService;
    }

    /**
     * Obtiene todos los clientes y carga las solicitudes pendientes
     * para mostrarlas en la vista de gestión de clientes.
     *
     * @param model modelo utilizado para enviar los datos a la vista.
     * @return nombre de la plantilla de listado de clientes.
     */
    @GetMapping("/agent/clients")
    public String listarClients(Model model) {

        List<ClientJpaEntity> clients = clientService.cercarFiltres(
                null, null, null, null, null, null, null
        );

        model.addAttribute(
                "clients",
                clients != null ? clients : Collections.emptyList()
        );

        carregarSolicitudes(model);

        return "llistat-clients";
    }

    /**
     * Filtra los clientes según los datos recibidos del formulario.
     *
     * @param dni DNI del cliente.
     * @param cadDNI fecha de caducidad del DNI.
     * @param Lliconduir número del permiso de conducir.
     * @param cdLLiconduir fecha de caducidad del permiso de conducir.
     * @param adreca dirección del cliente.
     * @param nacionalitat nacionalidad del cliente.
     * @param estat estado de actividad del cliente.
     * @param model modelo utilizado para enviar los resultados a la vista.
     * @return nombre de la plantilla de listado de clientes.
     */
    @PostMapping("/agent/clients")
    public String cercarClients(
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String cadDNI,
            @RequestParam(required = false) String Lliconduir,
            @RequestParam(required = false) String cdLLiconduir,
            @RequestParam(required = false) String adreca,
            @RequestParam(required = false) String nacionalitat,
            @RequestParam(required = false) String estat,
            Model model) {

        Boolean actiu = null;

        if (estat != null && !estat.isBlank()) {
            actiu = Boolean.parseBoolean(estat);
        }

        List<ClientJpaEntity> clients = clientService.cercarFiltres(
                dni,
                cadDNI,
                Lliconduir,
                cdLLiconduir,
                adreca,
                nacionalitat,
                actiu
        );

        model.addAttribute(
                "clients",
                clients != null ? clients : Collections.emptyList()
        );

        model.addAttribute("dni", dni);
        model.addAttribute("cadDNI", cadDNI);
        model.addAttribute("Lliconduir", Lliconduir);
        model.addAttribute("cdLLiconduir", cdLLiconduir);
        model.addAttribute("adreca", adreca);
        model.addAttribute("nacionalitat", nacionalitat);
        model.addAttribute("estat", estat);

        carregarSolicitudes(model);

        return "llistat-clients";
    }

    /**
     * Obtiene las solicitudes pendientes y las clasifica según su tipo.
     * También genera una comparativa entre los valores actuales y los propuestos.
     *
     * @param model modelo donde se almacenan las solicitudes y sus comparativas.
     */
    private void carregarSolicitudes(Model model) {

        List<SolicitudValidacionJpaEntity> pendents =
                solicitudService.obtenerPendientes();

        if (pendents == null) {
            pendents = Collections.emptyList();
        }

        List<SolicitudValidacionJpaEntity> registresInicials =
                pendents.stream()
                        .filter(s -> esTipus(s, "REGISTRO"))
                        .collect(Collectors.toList());

        List<SolicitudValidacionJpaEntity> solicitudesDatos =
                pendents.stream()
                        .filter(s -> esTipus(s, "DATOS_PERSONALES"))
                        .collect(Collectors.toList());

        List<SolicitudValidacionJpaEntity> solicitudesDocumentacion =
                pendents.stream()
                        .filter(s -> esTipus(s, "DOCUMENTACION"))
                        .collect(Collectors.toList());

        List<SolicitudValidacionJpaEntity> solicitudesTarjeta =
                pendents.stream()
                        .filter(s -> esTipus(s, "TARJETA"))
                        .collect(Collectors.toList());

        model.addAttribute("solicitudPorCliente", registresInicials);
        model.addAttribute("solicitudesDatos", solicitudesDatos);
        model.addAttribute("solicitudesDocumentacion", solicitudesDocumentacion);
        model.addAttribute("solicitudesTarjeta", solicitudesTarjeta);

        Map<Long, List<CanviSolicitud>> canvisPerSolicitud =
                new HashMap<>();

        for (SolicitudValidacionJpaEntity solicitud : pendents) {

            if (solicitud == null || solicitud.getId() == null) {
                continue;
            }

            canvisPerSolicitud.put(
                    solicitud.getId(),
                    solicitudService.obtenerComparativa(solicitud)
            );
        }

        model.addAttribute("canvisPerSolicitud", canvisPerSolicitud);
    }

    /**
     * Comprueba si una solicitud pertenece al tipo indicado.
     * La comparación no distingue entre mayúsculas y minúsculas.
     *
     * @param solicitud solicitud que se desea comprobar.
     * @param tipus tipo de solicitud esperado.
     * @return true si el tipo coincide; false en caso contrario.
     */
    private boolean esTipus(
            SolicitudValidacionJpaEntity solicitud,
            String tipus) {

        return solicitud != null
                && solicitud.getTipo() != null
                && tipus.equalsIgnoreCase(solicitud.getTipo());
    }

    /**
     * Obtiene todas las solicitudes pendientes de validación.
     *
     * @param model modelo utilizado para enviar las solicitudes a la vista.
     * @return nombre de la plantilla de solicitudes.
     */
    @GetMapping("/agent/solicitudes")
    public String mostrarSolicitudes(Model model) {

        List<SolicitudValidacionJpaEntity> solicitudes =
                solicitudService.obtenerPendientes();

        model.addAttribute(
                "solicitudes",
                solicitudes != null
                        ? solicitudes
                        : Collections.emptyList()
        );

        return "agent/solicitudes";
    }

    /**
     * Aprueba el registro inicial de un cliente.
     *
     * @param solicitudId identificador de la solicitud.
     * @param principal usuario autenticado que realiza la aprobación.
     * @param redirect objeto para enviar mensajes a la siguiente vista.
     * @return redirección al listado de clientes o al inicio de sesión.
     */
    @PostMapping("/agent/solicitudes/aprobar")
    public String aprobarRegistro(
            @RequestParam("id") Long solicitudId,
            Principal principal,
            RedirectAttributes redirect) {

        if (principal == null) {
            return "redirect:/login";
        }

        try {
            solicitudService.aprobarRegistro(
                    solicitudId,
                    principal.getName()
            );

            redirect.addFlashAttribute(
                    "missatge",
                    "El registre s'ha aprovat correctament."
            );

        } catch (Exception e) {
            registrarErrorControlador("aprovar el registre", e);

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "No s'ha pogut aprovar el registre."
            );
        }

        return "redirect:/agent/clients";
    }

    /**
     * Aprueba los cambios propuestos en una solicitud de un cliente.
     *
     * @param solicitudId identificador de la solicitud.
     * @param principal usuario autenticado que realiza la aprobación.
     * @param redirect objeto para enviar mensajes a la siguiente vista.
     * @return redirección al listado de clientes o al inicio de sesión.
     */
    @PostMapping("/agent/solicitudes/aprobar-cambios")
    public String aprobarCambios(
            @RequestParam("id") Long solicitudId,
            Principal principal,
            RedirectAttributes redirect) {

        if (principal == null) {
            return "redirect:/login";
        }

        try {
            solicitudService.aprobarSolicitud(
                    solicitudId,
                    principal.getName()
            );

            redirect.addFlashAttribute(
                    "missatge",
                    "Els canvis s'han aprovat correctament."
            );

        } catch (Exception e) {
            registrarErrorControlador("aprovar els canvis", e);

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "No s'han pogut aprovar els canvis."
            );
        }

        return "redirect:/agent/clients";
    }

    /**
     * Muestra el formulario para rechazar una solicitud pendiente.
     * Si la solicitud no existe o ya ha sido revisada, redirige al listado.
     *
     * @param solicitudId identificador de la solicitud.
     * @param model modelo utilizado para enviar los datos a la vista.
     * @param redirect objeto para enviar mensajes a la siguiente vista.
     * @return nombre de la plantilla de rechazo o redirección al listado.
     */
    @GetMapping("/agent/solicitudes/rebutjar")
    public String mostrarFormulariRebuig(
            @RequestParam("id") Long solicitudId,
            Model model,
            RedirectAttributes redirect) {

        List<SolicitudValidacionJpaEntity> pendents =
                solicitudService.obtenerPendientes();

        if (pendents == null) {
            pendents = Collections.emptyList();
        }

        SolicitudValidacionJpaEntity solicitud =
                pendents.stream()
                        .filter(s -> s != null && s.getId() != null)
                        .filter(s -> solicitudId.equals(s.getId()))
                        .findFirst()
                        .orElse(null);

        if (solicitud == null) {
            logQueryService.registrarWarning(
                    "AGENT",
                    "Intent de consultar un formulari de rebuig d'una sol·licitud inexistent o ja revisada."
            );

            redirect.addFlashAttribute(
                    "error",
                    "La sol·licitud no existeix o ja ha estat revisada."
            );

            return "redirect:/agent/clients";
        }

        model.addAttribute("solicitud", solicitud);

        Map<String, String> datosLegibles = Collections.emptyMap();

        String json = solicitud.getDatosPropuestos();

        if (json != null && !json.isBlank()) {
            Map<String, String> resultado =
                    solicitudService.obtenerDatosLegibles(json);

            if (resultado != null) {
                datosLegibles = resultado;
            }
        }

        model.addAttribute("datosLegibles", datosLegibles);
        model.addAttribute("fotoDniProposada", null);
        model.addAttribute("fotoPermisProposat", null);

        if (esTipus(solicitud, "DOCUMENTACION")) {

            model.addAttribute(
                    "fotoDniProposada",
                    solicitudService.obtenerFotoDni(json)
            );

            model.addAttribute(
                    "fotoPermisProposat",
                    solicitudService.obtenerFotoPermiso(json)
            );
        }

        return "rebutjar-registre";
    }

    /**
     * Rechaza una solicitud indicando el motivo y el agente que realiza la acción.
     *
     * @param solicitudId identificador de la solicitud.
     * @param motivo motivo del rechazo.
     * @param principal usuario autenticado que realiza el rechazo.
     * @param redirect objeto para enviar mensajes a la siguiente vista.
     * @return redirección al listado de clientes o al inicio de sesión.
     */
    @PostMapping("/agent/solicitudes/rechazar")
    public String rechazarSolicitud(
            @RequestParam("id") Long solicitudId,
            @RequestParam("motivo") String motivo,
            Principal principal,
            RedirectAttributes redirect) {

        if (principal == null) {
            return "redirect:/login";
        }

        try {
            solicitudService.rechazarSolicitud(
                    solicitudId,
                    principal.getName(),
                    motivo
            );

            redirect.addFlashAttribute(
                    "missatge",
                    "La sol·licitud s'ha rebutjat correctament."
            );

        } catch (Exception e) {
            registrarErrorControlador("rebutjar la sol·licitud", e);

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "No s'ha pogut rebutjar la sol·licitud."
            );
        }

        return "redirect:/agent/clients";
    }

    /**
     * Muestra el listado de agentes activos.
     *
     * @param model modelo utilizado para enviar los agentes a la vista.
     * @return nombre de la plantilla de gestión de agentes.
     */
    @GetMapping("/admin/agents")
    public String llistarAgents(Model model) {

        List<AgentJpaEntity> agents =
                agentService.cercarFiltres(null, true);

        model.addAttribute(
                "agents",
                agents != null ? agents : Collections.emptyList()
        );

        model.addAttribute("estat", "true");

        return "gestio-agents";
    }

    /**
     * Muestra el formulario para registrar un nuevo agente.
     * Establece la fecha actual como fecha inicial del contrato.
     *
     * @param model modelo utilizado para enviar el agente a la vista.
     * @return nombre de la plantilla de creación de agentes.
     */
    @GetMapping("/admin/agents/nou")
    public String mostrarFormulariAgent(Model model) {

        AgentJpaEntity agent = new AgentJpaEntity();
        agent.setDataContracte(LocalDate.now());

        model.addAttribute("agent", agent);

        return "nou-agent";
    }

    /**
     * Guarda un nuevo agente en el sistema.
     *
     * @param agent datos del agente recibidos desde el formulario.
     * @param model modelo utilizado para mostrar posibles errores.
     * @return redirección al listado o formulario de creación.
     */
    @PostMapping("/admin/agents/guardar")
    public String guardarAgent(
            @ModelAttribute("agent") AgentJpaEntity agent,
            Model model) {

        try {
            agentService.guardar(agent);
            return "redirect:/admin/agents";

        } catch (IllegalArgumentException e) {
            registrarErrorControlador("guardar l'agent", e);

            model.addAttribute("error", e.getMessage());
            return "nou-agent";

        } catch (Exception e) {
            registrarErrorControlador("guardar l'agent", e);

            model.addAttribute(
                    "error",
                    "No s'ha pogut guardar l'agent."
            );
            return "nou-agent";
        }
    }

    /**
     * Filtra los agentes según su DNI y estado de actividad.
     *
     * @param dni DNI del agente.
     * @param activo estado de actividad del agente.
     * @param model modelo utilizado para enviar los resultados a la vista.
     * @return nombre de la plantilla de gestión de agentes.
     */
    @PostMapping("/admin/agents/filtrar")
    public String filtrarAgents(
            @RequestParam(value = "dni", required = false) String dni,
            @RequestParam(value = "activo", required = false) Boolean activo,
            Model model) {

        List<AgentJpaEntity> agents =
                agentService.cercarFiltres(dni, activo);

        model.addAttribute(
                "agents",
                agents != null ? agents : Collections.emptyList()
        );

        model.addAttribute("dni", dni);
        model.addAttribute(
                "estat",
                activo == null ? "" : activo.toString()
        );

        return "gestio-agents";
    }

    /**
     * Busca y muestra la ficha de un agente a partir de su DNI.
     *
     * @param dni DNI del agente que se desea consultar.
     * @param model modelo utilizado para enviar el agente a la vista.
     * @param redirect objeto para enviar mensajes a la siguiente vista.
     * @return nombre de la plantilla de información o redirección al listado.
     */
    @GetMapping("/admin/agents/dni/{dni}")
    public String veureFitxaAgent(
            @org.springframework.web.bind.annotation.PathVariable String dni,
            Model model,
            RedirectAttributes redirect) {

        AgentJpaEntity agent = agentService.buscarAgentPerDNI(dni);

        if (agent == null) {
            logQueryService.registrarWarning(
                    "AGENT",
                    "Intent de consultar la fitxa d'un agent inexistent."
            );

            redirect.addFlashAttribute(
                    "error",
                    "No s'ha trobat l'agent."
            );

            return "redirect:/admin/agents";
        }

        model.addAttribute("agent", agent);

        return "informacio-agents";
    }

    /**
     * Muestra el formulario para editar los datos de un agente.
     *
     * @param id identificador del agente.
     * @param model modelo utilizado para enviar el agente a la vista.
     * @param redirect objeto para enviar mensajes a la siguiente vista.
     * @return nombre de la plantilla de edición o redirección al listado.
     */
    @GetMapping("/admin/agents/editar/{id}")
    public String mostrarEditarAgent(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            Model model,
            RedirectAttributes redirect) {

        AgentJpaEntity agent = agentService.buscarPerId(id);

        if (agent == null) {
            logQueryService.registrarWarning(
                    "AGENT",
                    "Intent de modificar un agent inexistent."
            );

            redirect.addFlashAttribute(
                    "error",
                    "No s'ha trobat l'agent."
            );

            return "redirect:/admin/agents";
        }

        model.addAttribute("agent", agent);

        return "editar-agent";
    }

    /**
     * Actualiza los datos de un agente existente.
     *
     * @param id identificador del agente que se desea actualizar.
     * @param form formulario con los nuevos datos del agente.
     * @param model modelo utilizado para mostrar posibles errores.
     * @param redirect objeto para enviar mensajes a la siguiente vista.
     * @return redirección al listado o nombre de la plantilla de edición.
     */
    @PostMapping("/admin/agents/editar/{id}")
    public String actualizarAgent(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @ModelAttribute AgentEditarForm form,
            Model model,
            RedirectAttributes redirect) {

        try {
            agentService.actualizar(id, form);

            redirect.addFlashAttribute(
                    "missatge",
                    "L'agent s'ha actualitzat correctament."
            );

            return "redirect:/admin/agents";

        } catch (IllegalArgumentException e) {
            registrarErrorControlador("actualitzar l'agent", e);

            model.addAttribute("error", e.getMessage());
            model.addAttribute("agent", form);

            return "editar-agent";

        } catch (Exception e) {
            registrarErrorControlador("actualitzar l'agent", e);

            model.addAttribute(
                    "error",
                    "No s'ha pogut actualitzar l'agent."
            );

            model.addAttribute("agent", form);

            return "editar-agent";
        }
    }

    /**
     * Desactiva un agente existente.
     *
     * @param id identificador del agente que se desea desactivar.
     * @param redirect objeto para enviar mensajes a la siguiente vista.
     * @return redirección al listado de agentes.
     */
    @PostMapping("/admin/agents/desactivar/{id}")
    public String desactivarAgent(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            RedirectAttributes redirect) {

        try {
            agentService.desactivar(id);

            redirect.addFlashAttribute(
                    "missatge",
                    "L'agent s'ha desactivat correctament."
            );

        } catch (IllegalArgumentException e) {
            registrarErrorControlador("desactivar l'agent", e);
            redirect.addFlashAttribute("error", e.getMessage());

        } catch (Exception e) {
            registrarErrorControlador("desactivar l'agent", e);

            redirect.addFlashAttribute(
                    "error",
                    "No s'ha pogut desactivar l'agent."
            );
        }

        return "redirect:/admin/agents";
    }

    /**
     * Activa un agente que se encontraba desactivado.
     *
     * @param id identificador del agente que se desea activar.
     * @param redirect objeto para enviar mensajes a la siguiente vista.
     * @return redirección al listado de agentes.
     */
    @PostMapping("/admin/agents/activar/{id}")
    public String activarAgent(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            RedirectAttributes redirect) {

        try {
            agentService.activar(id);

            redirect.addFlashAttribute(
                    "missatge",
                    "L'agent s'ha activat correctament."
            );

        } catch (IllegalArgumentException e) {
            registrarErrorControlador("activar l'agent", e);
            redirect.addFlashAttribute("error", e.getMessage());

        } catch (Exception e) {
            registrarErrorControlador("activar l'agent", e);

            redirect.addFlashAttribute(
                    "error",
                    "No s'ha pogut activar l'agent."
            );
        }

        return "redirect:/admin/agents";
    }

    /**
     * Registra una excepción como advertencia o error según su tipo.
     *
     * @param operacion descripción de la operación que ha fallado.
     * @param e excepción producida durante la operación.
     */
    private void registrarErrorControlador(String operacion, Exception e) {

        if (e instanceof IllegalArgumentException
                || e instanceof IllegalStateException) {

            logQueryService.registrarWarning(
                    "AGENT",
                    "No s'ha pogut " + operacion + ": " + e.getMessage()
            );

        } else {
            logQueryService.registrarError(
                    "AGENT",
                    "Error inesperat en intentar " + operacion + ": " + e.getMessage()
            );
        }
    }
}
