
package cat.copernic.nothinganpri.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import cat.copernic.nothinganpri.dto.AgentEditarForm;
import cat.copernic.nothinganpri.entity.AgentJpaEntity;
import cat.copernic.nothinganpri.repository.AgentRepository;
import cat.copernic.nothinganpri.service.AgentService;
import cat.copernic.nothinganpri.service.LogQueryService;
import cat.copernic.nothinganpri.service.UsuariService;

@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

    private static final List<String> ROLES_LISTAR =
            List.of("ADMIN", "ADMINISTRADOR", "AGENT");

    private static final List<String> ROLES_CERCAR =
            List.of("ADMINISTRADOR", "AGENT", "ADMIN");

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private UsuariService usuariService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LogQueryService logQueryService;

    @InjectMocks
    private AgentService agentService;

    private AgentJpaEntity agent;

    @BeforeEach
    void setUp() {
        agent = new AgentJpaEntity(
                "12345678A",
                LocalDate.of(2024, 1, 1),
                "Joan",
                "",
                "600123456",
                "joan@gmail.com",
                "password",
                "AGENT"
        );
    }

    @Test
    void testLlistarTots() {
        List<AgentJpaEntity> agents = List.of(agent);

        when(agentRepository.findByRolIn(ROLES_LISTAR))
                .thenReturn(agents);

        List<AgentJpaEntity> resultat = agentService.llistarTots();

        assertEquals(1, resultat.size());
        assertEquals(agent, resultat.get(0));

        verify(agentRepository).findByRolIn(ROLES_LISTAR);
    }

    @Test
    void testCercarFiltres() {
        List<AgentJpaEntity> agents = List.of(agent);

        when(agentRepository.cercarAgents(
                "12345678A",
                true,
                ROLES_CERCAR
        )).thenReturn(agents);

        List<AgentJpaEntity> resultat =
                agentService.cercarFiltres(" 12345678A ", true);

        assertEquals(1, resultat.size());
        assertEquals(agent, resultat.get(0));

        verify(agentRepository).cercarAgents(
                "12345678A", true, ROLES_CERCAR);
    }

    @Test
    void testCercarFiltresAmbDniNull() {
        when(agentRepository.cercarAgents(
                null, true, ROLES_CERCAR
        )).thenReturn(List.of(agent));

        List<AgentJpaEntity> resultat =
                agentService.cercarFiltres(null, true);

        assertEquals(1, resultat.size());

        verify(agentRepository).cercarAgents(
                null, true, ROLES_CERCAR);
    }

    @Test
    void testCercarFiltresAmbDniBuit() {
        when(agentRepository.cercarAgents(
                null, false, ROLES_CERCAR
        )).thenReturn(List.of());

        List<AgentJpaEntity> resultat =
                agentService.cercarFiltres("   ", false);

        assertTrue(resultat.isEmpty());

        verify(agentRepository).cercarAgents(
                null, false, ROLES_CERCAR);
    }

    @Test
    void testFiltrarPerDNI() {
        when(agentRepository.findByActivoTrueAndDNIContainingIgnoreCaseAndRolIn(
                "123", ROLES_CERCAR
        )).thenReturn(List.of(agent));

        List<AgentJpaEntity> resultat =
                agentService.filtrarPerDNI("123");

        assertEquals(1, resultat.size());
        assertEquals(agent, resultat.get(0));

        verify(agentRepository)
                .findByActivoTrueAndDNIContainingIgnoreCaseAndRolIn(
                        "123", ROLES_CERCAR);
    }

    @Test
    void testBuscarAgentPerDNI() {
        when(agentRepository.findByDNIAndRolIn(
                "12345678A", ROLES_CERCAR
        )).thenReturn(agent);

        AgentJpaEntity resultat =
                agentService.buscarAgentPerDNI("12345678A");

        assertEquals(agent, resultat);

        verify(agentRepository).findByDNIAndRolIn(
                "12345678A", ROLES_CERCAR);
    }

    @Test
    void testGuardarCorrectament() {
        when(agentRepository.existsByDNI("12345678A")).thenReturn(false);
        when(usuariService.emailExisteix("joan@gmail.com")).thenReturn(false);
        when(usuariService.telefonExisteix("600123456")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("passwordEncriptada");

        agentService.guardar(agent);

        assertEquals("passwordEncriptada", agent.getPassword());
        assertEquals(LocalDate.now(), agent.getDataContracte());

        verify(passwordEncoder).encode("password");
        verify(agentRepository).save(agent);
    }

    @Test
    void testGuardarAmbDNIDuplicat() {
        when(agentRepository.existsByDNI("12345678A")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.guardar(agent));

        assertEquals("Aquest DNI ja està registrat.", exception.getMessage());

        verify(agentRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);

        verify(logQueryService).registrarError(
                eq("AGENTS"),
                contains("guardar un agent"));
    }

    @Test
    void testGuardarAmbEmailDuplicat() {
        when(agentRepository.existsByDNI("12345678A")).thenReturn(false);
        when(usuariService.emailExisteix("joan@gmail.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.guardar(agent));

        assertEquals("Aquest email ja està registrat.", exception.getMessage());

        verify(agentRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);

        verify(logQueryService).registrarError(
                eq("AGENTS"),
                contains("guardar un agent"));
    }

    @Test
    void testGuardarAmbTelefonDuplicat() {
        when(agentRepository.existsByDNI("12345678A")).thenReturn(false);
        when(usuariService.emailExisteix("joan@gmail.com")).thenReturn(false);
        when(usuariService.telefonExisteix("600123456")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.guardar(agent));

        assertEquals("Aquest telèfon ja està registrat.", exception.getMessage());

        verify(agentRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);

        verify(logQueryService).registrarError(
                eq("AGENTS"),
                contains("guardar un agent"));
    }

    @Test
    void testBuscarPerIdExisteix() {
        when(agentRepository.findById(1L)).thenReturn(Optional.of(agent));

        AgentJpaEntity resultat = agentService.buscarPerId(1L);

        assertEquals(agent, resultat);
        verify(agentRepository).findById(1L);
    }

    @Test
    void testBuscarPerIdNoExisteix() {
        when(agentRepository.findById(1L)).thenReturn(Optional.empty());

        AgentJpaEntity resultat = agentService.buscarPerId(1L);

        assertNull(resultat);
        verify(agentRepository).findById(1L);
    }

    @Test
    void testActualizarCorrectament() {
        AgentEditarForm form = mock(AgentEditarForm.class);

        when(agentRepository.findById(1L)).thenReturn(Optional.of(agent));
        when(form.getDNI()).thenReturn("87654321B");
        when(form.getEmail()).thenReturn("nou@gmail.com");
        when(form.getTelefon()).thenReturn("611222333");
        when(form.getNom()).thenReturn("Maria");
        when(form.getCognoms()).thenReturn("Garcia");
        when(form.getDataContracte()).thenReturn(LocalDate.of(2025, 1, 1));

        when(agentRepository.existsByDNIAndIdNot("87654321B", 1L))
                .thenReturn(false);

        when(usuariService.emailExisteixEnAltreUsuari("nou@gmail.com", 1L))
                .thenReturn(false);

        when(usuariService.telefonExisteixEnAltreUsuari("611222333", 1L))
                .thenReturn(false);

        when(agentRepository.save(agent)).thenReturn(agent);

        AgentJpaEntity resultat = agentService.actualizar(1L, form);

        assertEquals("87654321B", resultat.getDNI());
        assertEquals("Maria", resultat.getNom());
        assertEquals("Garcia", resultat.getCognoms());
        assertEquals("611222333", resultat.getTelefon());
        assertEquals("nou@gmail.com", resultat.getEmail());
        assertEquals(LocalDate.of(2025, 1, 1), resultat.getDataContracte());

        verify(agentRepository).save(agent);
    }

    @Test
    void testActualizarAgentNoExisteix() {
        AgentEditarForm form = mock(AgentEditarForm.class);

        when(agentRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.actualizar(1L, form));

        assertEquals("L'agent no existeix.", exception.getMessage());

        verify(agentRepository, never()).save(any());

        verify(logQueryService).registrarError(
                eq("AGENTS"),
                contains("actualitzar un agent"));
    }

    @Test
    void testActualizarAmbDNIDuplicat() {
        AgentEditarForm form = mock(AgentEditarForm.class);

        when(agentRepository.findById(1L)).thenReturn(Optional.of(agent));
        when(form.getDNI()).thenReturn("87654321B");

        when(agentRepository.existsByDNIAndIdNot("87654321B", 1L))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.actualizar(1L, form));

        assertEquals("Aquest DNI ja està registrat.", exception.getMessage());

        verify(agentRepository, never()).save(any());

        verify(logQueryService).registrarError(
                eq("AGENTS"),
                contains("actualitzar un agent"));
    }

    @Test
    void testActualizarAmbEmailDuplicat() {
        AgentEditarForm form = mock(AgentEditarForm.class);

        when(agentRepository.findById(1L)).thenReturn(Optional.of(agent));
        when(form.getDNI()).thenReturn("87654321B");
        when(form.getEmail()).thenReturn("duplicat@gmail.com");

        when(agentRepository.existsByDNIAndIdNot("87654321B", 1L))
                .thenReturn(false);

        when(usuariService.emailExisteixEnAltreUsuari("duplicat@gmail.com", 1L))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.actualizar(1L, form));

        assertEquals("Aquest email ja està registrat.", exception.getMessage());

        verify(agentRepository, never()).save(any());

        verify(logQueryService).registrarError(
                eq("AGENTS"),
                contains("actualitzar un agent"));
    }

    @Test
    void testActualizarAmbTelefonDuplicat() {
        AgentEditarForm form = mock(AgentEditarForm.class);

        when(agentRepository.findById(1L)).thenReturn(Optional.of(agent));
        when(form.getDNI()).thenReturn("87654321B");
        when(form.getEmail()).thenReturn("nou@gmail.com");
        when(form.getTelefon()).thenReturn("611222333");

        when(agentRepository.existsByDNIAndIdNot("87654321B", 1L))
                .thenReturn(false);

        when(usuariService.emailExisteixEnAltreUsuari("nou@gmail.com", 1L))
                .thenReturn(false);

        when(usuariService.telefonExisteixEnAltreUsuari("611222333", 1L))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.actualizar(1L, form));

        assertEquals("Aquest telèfon ja està registrat.", exception.getMessage());

        verify(agentRepository, never()).save(any());

        verify(logQueryService).registrarError(
                eq("AGENTS"),
                contains("actualitzar un agent"));
    }

    @Test
    void testDesactivar() {
        when(agentRepository.findById(1L)).thenReturn(Optional.of(agent));

        agentService.desactivar(1L);

        assertFalse(agent.isActivo());
        verify(agentRepository).save(agent);
    }

    @Test
    void testDesactivarAgentNoExisteix() {
        when(agentRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.desactivar(1L));

        assertEquals("L'agent no existeix.", exception.getMessage());

        verify(agentRepository, never()).save(any());

        verify(logQueryService).registrarError(
                eq("AGENTS"),
                contains("desactivar un agent"));
    }

    @Test
    void testActivar() {
        agent.setActivo(false);

        when(agentRepository.findById(1L)).thenReturn(Optional.of(agent));

        agentService.activar(1L);

        assertTrue(agent.isActivo());
        verify(agentRepository).save(agent);
    }

    @Test
    void testActivarAgentNoExisteix() {
        when(agentRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.activar(1L));

        assertEquals("L'agent no existeix.", exception.getMessage());

        verify(agentRepository, never()).save(any());

        verify(logQueryService).registrarError(
                eq("AGENTS"),
                contains("activar un agent"));
    }
}
