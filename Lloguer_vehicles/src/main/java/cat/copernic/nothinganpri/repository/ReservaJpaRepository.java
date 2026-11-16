package cat.copernic.nothinganpri.repository;

import cat.copernic.nothinganpri.entity.ReservaJpaEntity;
import cat.copernic.nothinganpri.enums.EstatReserva;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ReservaJpaRepository extends JpaRepository<ReservaJpaEntity, Long> {

    @Query("SELECT COUNT(r) FROM ReservaJpaEntity r WHERE r.vehicle.id = :vehicleId " +
           "AND r.estat = 'ACTIVA' " + 
           "AND (r.dataInici <= :dataFi AND r.dataFi >= :dataInici)")
    long countConflictingReserves(@Param("vehicleId") Long vehicleId, 
                                  @Param("dataInici") LocalDate dataInici, 
                                  @Param("dataFi") LocalDate dataFi);
    // RF41: té el vehicle alguna reserva activa? (activa = encara no ha acabat: dataFi >= avui)
    // Spring Data crea la consulta sola a partir del nom del mètode. No cal escriure SQL.
    boolean existsByVehicleIdAndDataFiGreaterThanEqual(Long vehicleId, LocalDate avui);

    // RF41: el vehicle té o ha tingut alguna reserva, sigui quin sigui l'estat?
    // Si no n'hi ha cap, es pot esborrar definitivament sense trencar la clau forana vehicle_id.
    boolean existsByVehicleId(Long vehicleId);

    // Ids de tots els vehicles amb alguna reserva (una sola consulta per al llistat)
    @Query("SELECT DISTINCT r.vehicle.id FROM ReservaJpaEntity r")
    java.util.Set<Long> findIdsVehiclesAmbReserves();
    
// --- RF54 FILTRO (Lógica backend per permetre la cerca combinada per múltiples criteris) ---
    @Query("SELECT r FROM ReservaJpaEntity r WHERE " +
           "(:codi IS NULL OR r.codiReserva = :codi) AND " +
           "(:data IS NULL OR r.dataInici = :data) AND " +
           "(:matricula IS NULL OR r.vehicle.matricula = :matricula) AND " +
           "(:email IS NULL OR r.client.email = :email)")
    java.util.List<ReservaJpaEntity> cercarAmbFiltres(@Param("codi") String codi,
                                            @Param("data") LocalDate data,
                                            @Param("matricula") String matricula,
                                            @Param("email") String email);
                                            
    // --- RF91 INFORME CLIENT (Lògica de càlcul backend) ---
    long countByClientId(Long clientId);
    java.util.List<ReservaJpaEntity> findByClientId(Long clientId);


    boolean existsByClient_IdAndEstatIn(
        Long clientId,
        List<EstatReserva> estats
    );

   boolean existsByVehicleIdAndEstatInAndDataFiGreaterThanEqual(
    Long vehicleId, 
    List<EstatReserva> estats, 
    LocalDate dataFi
    );
}