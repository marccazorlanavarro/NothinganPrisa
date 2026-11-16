package cat.copernic.nothinganpri.test;

import cat.copernic.nothinganpri.document.HistoricReservaDocument;
import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.entity.ReservaJpaEntity;
import cat.copernic.nothinganpri.entity.VehicleJpaEntity;
import cat.copernic.nothinganpri.enums.EstatReserva;
import cat.copernic.nothinganpri.repository.HistoricReservaRepository;
import cat.copernic.nothinganpri.repository.ReservaJpaRepository;
import cat.copernic.nothinganpri.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaris JUnit 5 de {@link ReservaService}.
 * Verifiquen la lògica de negoci de reserves (crear, anul·lar, targeta, conflictes)
 * i l'escriptura a l'històric MongoDB, sense base de dades real (Mockito).
 */
@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaJpaRepository reservaRepository;

    @Mock
    private HistoricReservaRepository historicReservaRepository;

    @InjectMocks
    private ReservaService service;

    private VehicleJpaEntity vehicle;
    private ClientJpaEntity client;
    private ReservaJpaEntity reserva;

    @BeforeEach
    void setUp() {
        vehicle = new VehicleJpaEntity();
        vehicle.setId(10L);
        vehicle.setMatricula("1234ABC");
        vehicle.setMarca("Renault");
        vehicle.setModel("Clio");

        client = new ClientJpaEntity();
        client.setId(64L);
        client.setNom("Marc");
        client.setCognoms("Cazorla");
        client.setEmail("marccazorlanavarro@gmail.com");
        client.setNumtarjetacredit("4111111111111111");

        reserva = new ReservaJpaEntity();
        reserva.setId(100L);
        reserva.setCodiReserva("ABC12345");
        reserva.setVehicle(vehicle);
        reserva.setClient(client);
        reserva.setDataInici(LocalDate.of(2026, 11, 19));
        reserva.setDataFi(LocalDate.of(2026, 11, 22));
        reserva.setImportTotal(396.0);
        reserva.setEstat(EstatReserva.ACTIVA);
    }

    // ---------- crearReserva ----------

    @Test
    void crearReserva_DatesValidesSenseConflicte_DesaIRetornaReserva() throws Exception {
        when(reservaRepository.countConflictingReserves(eq(10L), any(), any())).thenReturn(0L);
        when(reservaRepository.save(any(ReservaJpaEntity.class))).thenAnswer(inv -> {
            ReservaJpaEntity r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });
        when(historicReservaRepository.save(any(HistoricReservaDocument.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ReservaJpaEntity creada = service.crearReserva(reserva);

        assertNotNull(creada);
        assertNotNull(creada.getCodiReserva());
        assertEquals(8, creada.getCodiReserva().length());
        assertEquals(EstatReserva.ACTIVA, creada.getEstat());
        verify(reservaRepository).save(any(ReservaJpaEntity.class));
        verify(historicReservaRepository).save(any(HistoricReservaDocument.class));
    }

    @Test
    void crearReserva_DataIniciPosteriorADataFi_LlançaExcepcio() {
        reserva.setDataInici(LocalDate.of(2026, 11, 25));
        reserva.setDataFi(LocalDate.of(2026, 11, 20));

        Exception ex = assertThrows(Exception.class, () -> service.crearReserva(reserva));
        assertTrue(ex.getMessage().contains("inici"));
        verify(reservaRepository, never()).save(any());
        verify(historicReservaRepository, never()).save(any());
    }

    @Test
    void crearReserva_VehicleNoDisponible_LlançaExcepcio() {
        when(reservaRepository.countConflictingReserves(eq(10L), any(), any())).thenReturn(1L);

        Exception ex = assertThrows(Exception.class, () -> service.crearReserva(reserva));
        assertTrue(ex.getMessage().toLowerCase().contains("disponible"));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void crearReserva_GuardaHistoricAmbAccioCreada() throws Exception {
        when(reservaRepository.countConflictingReserves(eq(10L), any(), any())).thenReturn(0L);
        when(reservaRepository.save(any(ReservaJpaEntity.class))).thenAnswer(inv -> {
            ReservaJpaEntity r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });
        when(historicReservaRepository.save(any(HistoricReservaDocument.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.crearReserva(reserva);

        ArgumentCaptor<HistoricReservaDocument> captor =
                ArgumentCaptor.forClass(HistoricReservaDocument.class);
        verify(historicReservaRepository).save(captor.capture());

        HistoricReservaDocument doc = captor.getValue();
        assertEquals(ReservaService.ACCIO_CREADA, doc.getAccio());
        assertEquals(100L, doc.getReservaId());
        assertEquals("1234ABC", doc.getMatricula());
        assertEquals(64L, doc.getClientId());
        assertEquals(396.0, doc.getImportTotal());
    }

    // ---------- crearReservaClient / targeta ----------

    @Test
    void crearReservaClient_SenseTargeta_LlançaExcepcio() {
        client.setNumtarjetacredit(null);

        Exception ex = assertThrows(Exception.class, () -> service.crearReservaClient(reserva));
        assertEquals(ReservaService.MISSATGE_SENSE_TARGETA, ex.getMessage());
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void crearReservaClient_AmbTargeta_CreaReserva() throws Exception {
        when(reservaRepository.countConflictingReserves(eq(10L), any(), any())).thenReturn(0L);
        when(reservaRepository.save(any(ReservaJpaEntity.class))).thenAnswer(inv -> {
            ReservaJpaEntity r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });
        when(historicReservaRepository.save(any(HistoricReservaDocument.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ReservaJpaEntity creada = service.crearReservaClient(reserva);

        assertNotNull(creada);
        assertEquals(EstatReserva.ACTIVA, creada.getEstat());
        verify(reservaRepository).save(any(ReservaJpaEntity.class));
    }

    @Test
    void teTargeta_ClientAmbNumero_RetornaTrue() {
        assertTrue(service.teTargeta(client));
    }

    @Test
    void teTargeta_ClientSenseNumero_RetornaFalse() {
        client.setNumtarjetacredit("   ");
        assertFalse(service.teTargeta(client));
    }

    // ---------- anullarReserva ----------

    @Test
    void anullarReserva_ReservaExistent_CanviaEstatIHistoric() throws Exception {
        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(ReservaJpaEntity.class))).thenReturn(reserva);
        when(historicReservaRepository.save(any(HistoricReservaDocument.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.anullarReserva(100L);

        assertEquals(EstatReserva.ANUL_LADA, reserva.getEstat());
        verify(reservaRepository).save(reserva);

        ArgumentCaptor<HistoricReservaDocument> captor =
                ArgumentCaptor.forClass(HistoricReservaDocument.class);
        verify(historicReservaRepository).save(captor.capture());
        assertEquals(ReservaService.ACCIO_ANULLADA, captor.getValue().getAccio());
    }

    @Test
    void anullarReserva_IdInexistent_LlançaExcepcio() {
        when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(Exception.class, () -> service.anullarReserva(999L));
        assertTrue(ex.getMessage().contains("no trobada") || ex.getMessage().contains("999"));
        verify(historicReservaRepository, never()).save(any());
    }

    // ---------- solicitarAnulacio ----------

    @Test
    void solicitarAnulacio_ClientPropietari_MarcaSolicitud() throws Exception {
        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(ReservaJpaEntity.class))).thenReturn(reserva);

        service.solicitarAnulacio(100L, "marccazorlanavarro@gmail.com");

        assertTrue(reserva.isAnulacioSolicitada());
        verify(reservaRepository).save(reserva);
    }

    @Test
    void solicitarAnulacio_EmailDiferent_LlançaExcepcio() {
        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));

        Exception ex = assertThrows(Exception.class,
                () -> service.solicitarAnulacio(100L, "altre@test.com"));
        assertTrue(ex.getMessage().toLowerCase().contains("només")
                || ex.getMessage().toLowerCase().contains("teves"));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void solicitarAnulacio_JaSolicitada_LlançaExcepcio() {
        reserva.setAnulacioSolicitada(true);
        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));

        Exception ex = assertThrows(Exception.class,
                () -> service.solicitarAnulacio(100L, "marccazorlanavarro@gmail.com"));
        assertTrue(ex.getMessage().toLowerCase().contains("ja has sol·licitat")
                || ex.getMessage().toLowerCase().contains("ja has"));
    }

    // ---------- informe històric (Mongo) ----------

    @Test
    void obtenirHistoricClient_RetornaLlistaDelRepositori() {
        HistoricReservaDocument doc = new HistoricReservaDocument();
        doc.setClientId(64L);
        doc.setAccio(ReservaService.ACCIO_CREADA);
        when(historicReservaRepository.findByClientIdAndAccioOrderByDataIniciDesc(
                64L, ReservaService.ACCIO_CREADA)).thenReturn(List.of(doc));

        List<HistoricReservaDocument> resultat = service.obtenirHistoricClient(64L);

        assertEquals(1, resultat.size());
        assertEquals(64L, resultat.get(0).getClientId());
    }

    @Test
    void comptarReservesClient_RetornaComptadorMongo() {
        when(historicReservaRepository.countByClientIdAndAccio(64L, ReservaService.ACCIO_CREADA))
                .thenReturn(3L);

        assertEquals(3L, service.comptarReservesClient(64L));
    }

    @Test
    void totalImportHistoric_SumaImports() {
        HistoricReservaDocument d1 = new HistoricReservaDocument();
        d1.setImportTotal(100.0);
        HistoricReservaDocument d2 = new HistoricReservaDocument();
        d2.setImportTotal(50.5);
        when(historicReservaRepository.findByClientIdInAndAccioOrderByClientNomAscDataIniciDesc(
                anyCollection(), eq(ReservaService.ACCIO_CREADA)))
                .thenReturn(List.of(d1, d2));

        double total = service.totalImportHistoric(List.of(64L));

        assertEquals(150.5, total, 0.001);
    }
}