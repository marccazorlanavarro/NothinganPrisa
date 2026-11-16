package cat.copernic.nothinganpri.test;

import cat.copernic.nothinganpri.entity.VehicleJpaEntity;
import cat.copernic.nothinganpri.enums.CategoriaVehicle;
import cat.copernic.nothinganpri.enums.TipusCombustible;
import cat.copernic.nothinganpri.repository.ReservaJpaRepository;
import cat.copernic.nothinganpri.repository.VehicleJpaRepository;
import cat.copernic.nothinganpri.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleJpaRepository repository;

    @Mock
    private ReservaJpaRepository reservaRepository;

    @InjectMocks
    private VehicleService service;

    private VehicleJpaEntity vehicle;

    @BeforeEach
    void setUp() {
        vehicle = new VehicleJpaEntity();
        vehicle.setId(1L);
        vehicle.setMatricula("1234ABC");
        vehicle.setMarca("Seat");
        vehicle.setModel("Leon");
        vehicle.setAnyMatriculacio(2022);
        vehicle.setCategoria(CategoriaVehicle.TURISME);
        vehicle.setCombustible(TipusCombustible.GASOLINA);
        vehicle.setPlaces(5);
        vehicle.setPreuDia(new BigDecimal("10.00"));
        vehicle.setFiancaEstandard(new BigDecimal("200.00"));
        vehicle.setMinDies(1);
    }

    // 1. Prueba de actualización correcta
    @Test
    void guardar_ActualitzacioVehicleValid_RetornaVehicleDesat() {
        when(repository.findByMatricula("1234ABC")).thenReturn(Optional.of(vehicle));
        when(repository.save(any(VehicleJpaEntity.class))).thenReturn(vehicle);

        VehicleJpaEntity actualitzat = service.guardar(vehicle);

        assertNotNull(actualitzat);
        assertEquals("1234ABC", actualitzat.getMatricula());
        verify(repository, times(1)).save(vehicle);
    }

    // 2. Prueba del caso de vehículo inexistente por ID
    @Test
    void buscarPerId_VehicleInexistent_RetornaOptionalBuit() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        Optional<VehicleJpaEntity> resultat = service.buscarPerId(999L);

        assertFalse(resultat.isPresent());
        assertTrue(resultat.isEmpty());
    }

    // 3. Prueba de error al intentar usar la matrícula de otro vehículo existente
    @Test
    void guardar_MatriculaDuplicadaDiferentId_LlançaExcepcio() {
        VehicleJpaEntity vehicleExistent = new VehicleJpaEntity();
        vehicleExistent.setId(2L); // ID diferente
        vehicleExistent.setMatricula("1234ABC");

        when(repository.findByMatricula("1234ABC")).thenReturn(Optional.of(vehicleExistent));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.guardar(vehicle);
        });

        assertTrue(ex.getMessage().contains("Ja existeix un vehicle amb la matrícula"));
    }

    // 4. Baixa definitiva: sense cap reserva s'esborra de la BD
    @Test
    void donarDeBaixa_SenseReserves_EsborraDefinitivament() {
        when(repository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(reservaRepository.existsByVehicleId(1L)).thenReturn(false);

        boolean eliminat = service.donarDeBaixa(1L);

        assertTrue(eliminat);
        verify(repository).delete(vehicle);
        verify(repository, never()).save(any());
    }

    // 5. Amb reserves futures/en curs: es bloqueja la baixa
    @Test
    void donarDeBaixa_AmbReservesVigents_LlançaExcepcio() {
        when(repository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(reservaRepository.existsByVehicleId(1L)).thenReturn(true);
        when(reservaRepository.existsByVehicleIdAndEstatInAndDataFiGreaterThanEqual(eq(1L), anyList(), any()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.donarDeBaixa(1L));
        verify(repository, never()).delete(any());
    }

    // 6. Només historial passat: baixa lògica, no s'esborra
    @Test
    void donarDeBaixa_NomesReservesPassades_BaixaLogica() {
        when(repository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(reservaRepository.existsByVehicleId(1L)).thenReturn(true);
        when(reservaRepository.existsByVehicleIdAndEstatInAndDataFiGreaterThanEqual(eq(1L), anyList(), any()))
                .thenReturn(false);

        boolean eliminat = service.donarDeBaixa(1L);

        assertFalse(eliminat);
        assertFalse(vehicle.isActiu());
        verify(repository).save(vehicle);
        verify(repository, never()).delete(any());
    }
}