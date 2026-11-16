
package cat.copernic.nothinganpri.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import cat.copernic.nothinganpri.dto.AgentEditarForm;
import cat.copernic.nothinganpri.entity.AgentJpaEntity;
import cat.copernic.nothinganpri.repository.AgentRepository;

/**
 * Servei que gestiona la lògica de negoci dels agents.
 */
@Service
public class AgentService {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private UsuariService usuariService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LogQueryService logQueryService;

    /**
     * Registra els errors del mòdul AGENTS.
     */
    private void registrarError(String operacio, Exception e) {

        logQueryService.registrarError(
            "AGENTS",
            "Error en " + operacio + ": " + e.getMessage()
        );
    }

    public List<AgentJpaEntity> llistarTots() {

        try {
            return agentRepository.findByRolIn(
                List.of("ADMIN", "ADMINISTRADOR", "AGENT")
            );

        } catch (Exception e) {
            registrarError("llistar tots els agents", e);
            throw e;
        }
    }

    public List<AgentJpaEntity> cercarFiltres(
            String dni,
            Boolean activo) {

        try {
            return agentRepository.cercarAgents(
                dni == null || dni.trim().isEmpty()
                    ? null : dni.trim(),
                activo,
                List.of("ADMINISTRADOR", "AGENT", "ADMIN")
            );

        } catch (Exception e) {
            registrarError("cercar filtres d'agents", e);
            throw e;
        }
    }

    public List<AgentJpaEntity> filtrarPerDNI(String dni) {

        try {
            return agentRepository
                .findByActivoTrueAndDNIContainingIgnoreCaseAndRolIn(
                    dni,
                    List.of("ADMINISTRADOR", "AGENT", "ADMIN")
                );

        } catch (Exception e) {
            registrarError("filtrar agents per DNI", e);
            throw e;
        }
    }

    public AgentJpaEntity buscarAgentPerDNI(String dni) {

        try {
            return agentRepository.findByDNIAndRolIn(
                dni,
                List.of("ADMINISTRADOR", "AGENT", "ADMIN")
            );

        } catch (Exception e) {
            registrarError("buscar agent per DNI", e);
            throw e;
        }
    }

    /**
     * Registra un agent comprovant que les seves dades no estiguin duplicades.
     */
    @Transactional
    public void guardar(AgentJpaEntity agent) {

        try {
            if (agentRepository.existsByDNI(agent.getDNI())) {
                throw new IllegalArgumentException(
                    "Aquest DNI ja està registrat."
                );
            }

            if (usuariService.emailExisteix(agent.getEmail())) {
                throw new IllegalArgumentException(
                    "Aquest email ja està registrat."
                );
            }

            if (usuariService.telefonExisteix(agent.getTelefon())) {
                throw new IllegalArgumentException(
                    "Aquest telèfon ja està registrat."
                );
            }

            String passwordEncriptada =
                passwordEncoder.encode(agent.getPassword());

            agent.setPassword(passwordEncriptada);
            agent.setDataContracte(LocalDate.now());

            agentRepository.save(agent);

        } catch (Exception e) {
            registrarError("guardar un agent", e);
            throw e;
        }
    }

    public AgentJpaEntity buscarPerId(Long id) {

        try {
            return agentRepository.findById(id).orElse(null);

        } catch (Exception e) {
            registrarError("buscar agent per ID", e);
            throw e;
        }
    }

    /**
     * Actualitza les dades d'un agent existent.
     */
    @Transactional
    public AgentJpaEntity actualizar(Long id, AgentEditarForm form) {

        try {
            AgentJpaEntity agent = agentRepository.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException("L'agent no existeix.")
                );

            if (agentRepository.existsByDNIAndIdNot(
                    form.getDNI(), id)) {
                throw new IllegalArgumentException(
                    "Aquest DNI ja està registrat."
                );
            }

            if (usuariService.emailExisteixEnAltreUsuari(
                    form.getEmail(), id)) {
                throw new IllegalArgumentException(
                    "Aquest email ja està registrat."
                );
            }

            if (usuariService.telefonExisteixEnAltreUsuari(
                    form.getTelefon(), id)) {
                throw new IllegalArgumentException(
                    "Aquest telèfon ja està registrat."
                );
            }

            agent.setDNI(form.getDNI());
            agent.setNom(form.getNom());
            agent.setCognoms(form.getCognoms());
            agent.setTelefon(form.getTelefon());
            agent.setEmail(form.getEmail());
            agent.setDataContracte(form.getDataContracte());

            return agentRepository.save(agent);

        } catch (Exception e) {
            registrarError("actualitzar un agent", e);
            throw e;
        }
    }

    /**
     * Desactiva un agent sense eliminar-lo de la base de dades.
     */
    @Transactional
    public void desactivar(Long id) {

        try {
            AgentJpaEntity agent = agentRepository.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException("L'agent no existeix.")
                );

            agent.setActivo(false);
            agentRepository.save(agent);

        } catch (Exception e) {
            registrarError("desactivar un agent", e);
            throw e;
        }
    }

    /**
     * Reactiva un agent que estava desactivat.
     */
    @Transactional
    public void activar(Long id) {

        try {
            AgentJpaEntity agent = agentRepository.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException("L'agent no existeix.")
                );

            agent.setActivo(true);
            agentRepository.save(agent);

        } catch (Exception e) {
            registrarError("activar un agent", e);
            throw e;
        }
    }
}
