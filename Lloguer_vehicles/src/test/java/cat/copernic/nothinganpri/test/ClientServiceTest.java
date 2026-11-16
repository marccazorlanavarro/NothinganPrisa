
package cat.copernic.nothinganpri.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cat.copernic.nothinganpri.dto.DocumentClient;
import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.enums.EstatReserva;
import cat.copernic.nothinganpri.repository.ClientRepository;
import cat.copernic.nothinganpri.repository.ReservaJpaRepository;
import cat.copernic.nothinganpri.service.ClientService;
import cat.copernic.nothinganpri.service.LogQueryService;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ReservaJpaRepository reservaRepository;

    @Mock
    private LogQueryService logQueryService;

    @InjectMocks
    private ClientService clientService;

    private ClientJpaEntity client;

    @BeforeEach
    void setUp() {
        client = new ClientJpaEntity(
                "12345678A",
                "2030-01-01",
                "B",
                "2031-01-01",
                "123456789",
                "Barcelona",
                "Espanyola",
                "reverso.png",
                new byte[] { 1, 2, 3 },
                "image/png",
                new byte[] { 4, 5, 6 },
                "image/jpeg",
                null,
                null,
                "Joan",
                "",
                "600123456",
                "joan@gmail.com",
                "password",
                "CLIENT"
        );
    }

    @Test
    void testLlistarTots() {
        List<ClientJpaEntity> clients = Arrays.asList(client);

        when(clientRepository.findByRolAndActivoTrue("CLIENT"))
                .thenReturn(clients);

        List<ClientJpaEntity> resultat = clientService.llistarTots();

        assertEquals(1, resultat.size());
        assertEquals(client, resultat.get(0));

        verify(clientRepository).findByRolAndActivoTrue("CLIENT");
    }

    @Test
    void testLlistarTotsSenseClients() {
        when(clientRepository.findByRolAndActivoTrue("CLIENT"))
                .thenReturn(Collections.emptyList());

        List<ClientJpaEntity> resultat = clientService.llistarTots();

        assertTrue(resultat.isEmpty());
    }

    @Test
    void testCercarFiltresAmbValors() {
        when(clientRepository.cercarClients(
                "12345678A",
                "2030",
                "B",
                "2031",
                "Barcelona",
                "Espanyola",
                true,
                "CLIENT"
        )).thenReturn(Arrays.asList(client));

        List<ClientJpaEntity> resultat = clientService.cercarFiltres(
                "12345678A",
                "2030",
                "B",
                "2031",
                "Barcelona",
                "Espanyola",
                true
        );

        assertEquals(1, resultat.size());

        verify(clientRepository).cercarClients(
                "12345678A",
                "2030",
                "B",
                "2031",
                "Barcelona",
                "Espanyola",
                true,
                "CLIENT"
        );
    }

    @Test
    void testCercarFiltresAmbValorsBuits() {
        when(clientRepository.cercarClients(
                null, null, null, null, null, null, false, "CLIENT"
        )).thenReturn(Collections.emptyList());

        List<ClientJpaEntity> resultat = clientService.cercarFiltres(
                " ",
                "",
                null,
                "   ",
                " ",
                null,
                false
        );

        assertTrue(resultat.isEmpty());

        verify(clientRepository).cercarClients(
                null, null, null, null, null, null, false, "CLIENT"
        );
    }

    @Test
    void testCercarFiltresEliminaEspais() {
        when(clientRepository.cercarClients(
                "12345678A",
                "2030",
                "B",
                "2031",
                "Barcelona",
                "Espanyola",
                true,
                "CLIENT"
        )).thenReturn(Arrays.asList(client));

        clientService.cercarFiltres(
                " 12345678A ",
                " 2030 ",
                " B ",
                " 2031 ",
                " Barcelona ",
                " Espanyola ",
                true
        );

        verify(clientRepository).cercarClients(
                "12345678A",
                "2030",
                "B",
                "2031",
                "Barcelona",
                "Espanyola",
                true,
                "CLIENT"
        );
    }

    @Test
    void testFindByDNIExisteix() {
        when(clientRepository.findByDNIAndRol("12345678A", "CLIENT"))
                .thenReturn(Optional.of(client));

        Optional<ClientJpaEntity> resultat =
                clientService.findByDNI("12345678A");

        assertTrue(resultat.isPresent());
        assertEquals(client, resultat.get());
    }

    @Test
    void testFindByDNINoExisteix() {
        when(clientRepository.findByDNIAndRol("99999999Z", "CLIENT"))
                .thenReturn(Optional.empty());

        Optional<ClientJpaEntity> resultat =
                clientService.findByDNI("99999999Z");

        assertTrue(resultat.isEmpty());
    }

    @Test
    void testFindByIdExisteix() {
        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(client));

        Optional<ClientJpaEntity> resultat = clientService.findById(1L);

        assertTrue(resultat.isPresent());
        assertEquals(client, resultat.get());
    }

    @Test
    void testFindByIdNoExisteix() {
        when(clientRepository.findById(2L))
                .thenReturn(Optional.empty());

        Optional<ClientJpaEntity> resultat = clientService.findById(2L);

        assertTrue(resultat.isEmpty());
    }

    @Test
    void testEliminarClientCorrectament() {
        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(client));

        when(reservaRepository.existsByClient_IdAndEstatIn(
                eq(1L),
                anyList()
        )).thenReturn(false);

        boolean resultat = clientService.eliminarClient(1L);

        assertTrue(resultat);
        assertFalse(client.isActivo());

        verify(clientRepository).save(client);

        verify(reservaRepository).existsByClient_IdAndEstatIn(
                eq(1L),
                eq(Arrays.asList(
                        EstatReserva.PENDENT,
                        EstatReserva.ACTIVA,
                        EstatReserva.EN_CURS
                ))
        );

        verify(logQueryService).registrarInfo(
                eq("CLIENTS"),
                contains("Client desactivat correctament")
        );
    }

    @Test
    void testEliminarClientNoExisteix() {
        when(clientRepository.findById(1L))
                .thenReturn(Optional.empty());

        boolean resultat = clientService.eliminarClient(1L);

        assertFalse(resultat);

        verify(reservaRepository, never())
                .existsByClient_IdAndEstatIn(anyLong(), anyList());

        verify(clientRepository, never()).save(any());

        verify(logQueryService).registrarWarning(
                eq("CLIENTS"),
                anyString()
        );
    }

    @Test
    void testEliminarUsuariQueNoEsClient() {
        client.setRol("ADMIN");

        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(client));

        boolean resultat = clientService.eliminarClient(1L);

        assertFalse(resultat);

        verify(reservaRepository, never())
                .existsByClient_IdAndEstatIn(anyLong(), anyList());

        verify(clientRepository, never()).save(any());

        verify(logQueryService).registrarWarning(
                eq("CLIENTS"),
                anyString()
        );
    }

    @Test
    void testEliminarClientAmbReservesActives() {
        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(client));

        when(reservaRepository.existsByClient_IdAndEstatIn(
                eq(1L),
                anyList()
        )).thenReturn(true);

        boolean resultat = clientService.eliminarClient(1L);

        assertFalse(resultat);
        assertTrue(client.isActivo());

        verify(clientRepository, never()).save(any());

        verify(logQueryService).registrarWarning(
                eq("CLIENTS"),
                anyString()
        );
    }

    @Test
    void testActivarClientCorrectament() {
        client.setActivo(false);

        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(client));

        clientService.activarClient(1L);

        assertTrue(client.isActivo());

        verify(clientRepository).save(client);

        verify(logQueryService).registrarInfo(
                eq("CLIENTS"),
                contains("Client reactivat correctament")
        );
    }

    @Test
    void testActivarClientNoExisteix() {
        when(clientRepository.findById(1L))
                .thenReturn(Optional.empty());

        clientService.activarClient(1L);

        verify(clientRepository, never()).save(any());

        verify(logQueryService).registrarWarning(
                eq("CLIENTS"),
                anyString()
        );
    }

    @Test
    void testActivarUsuariQueNoEsClient() {
        client.setRol("ADMIN");

        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(client));

        clientService.activarClient(1L);

        verify(clientRepository, never()).save(any());

        verify(logQueryService).registrarWarning(
                eq("CLIENTS"),
                anyString()
        );
    }

    @Test
    void testObtenirDniCorrectament() {
        when(clientRepository.findByDNIAndRol("12345678A", "CLIENT"))
                .thenReturn(Optional.of(client));

        Optional<DocumentClient> resultat =
                clientService.obtenirDni("12345678A");

        assertTrue(resultat.isPresent());

        assertArrayEquals(
                new byte[] { 1, 2, 3 },
                resultat.get().getDades()
        );

        assertEquals("image/png", resultat.get().getTipus());
    }

    @Test
    void testObtenirDniClientNoExisteix() {
        when(clientRepository.findByDNIAndRol("99999999Z", "CLIENT"))
                .thenReturn(Optional.empty());

        Optional<DocumentClient> resultat =
                clientService.obtenirDni("99999999Z");

        assertTrue(resultat.isEmpty());
    }

    @Test
    void testObtenirDniSenseFotografia() {
        client.setFotoDniAnverso(null);

        when(clientRepository.findByDNIAndRol("12345678A", "CLIENT"))
                .thenReturn(Optional.of(client));

        Optional<DocumentClient> resultat =
                clientService.obtenirDni("12345678A");

        assertTrue(resultat.isEmpty());
    }

    @Test
    void testObtenirLlicenciaCorrectament() {
        when(clientRepository.findByDNIAndRol("12345678A", "CLIENT"))
                .thenReturn(Optional.of(client));

        Optional<DocumentClient> resultat =
                clientService.obtenirLlicencia("12345678A");

        assertTrue(resultat.isPresent());

        assertArrayEquals(
                new byte[] { 4, 5, 6 },
                resultat.get().getDades()
        );

        assertEquals("image/jpeg", resultat.get().getTipus());
    }

    @Test
    void testObtenirLlicenciaClientNoExisteix() {
        when(clientRepository.findByDNIAndRol("99999999Z", "CLIENT"))
                .thenReturn(Optional.empty());

        Optional<DocumentClient> resultat =
                clientService.obtenirLlicencia("99999999Z");

        assertTrue(resultat.isEmpty());
    }

    @Test
    void testObtenirLlicenciaSenseFotografia() {
        client.setFotoPermiso(null);

        when(clientRepository.findByDNIAndRol("12345678A", "CLIENT"))
                .thenReturn(Optional.of(client));

        Optional<DocumentClient> resultat =
                clientService.obtenirLlicencia("12345678A");

        assertTrue(resultat.isEmpty());
    }
}
