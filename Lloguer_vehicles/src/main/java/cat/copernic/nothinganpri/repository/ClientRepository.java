
package cat.copernic.nothinganpri.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cat.copernic.nothinganpri.entity.ClientJpaEntity;

/**
 * Repositori per gestionar les operacions d'accés a les dades dels clients.
 */
@Repository
public interface ClientRepository extends JpaRepository<ClientJpaEntity, Long> {

    /**
     * Cerca els clients actius segons el seu rol.
     *
     * @param rol rol del client.
     * @return llista de clients actius amb el rol indicat.
     */
    List<ClientJpaEntity> findByRolAndActivoTrue(String rol);

    /**
     * Cerca un client pel seu DNI i rol.
     *
     * @param DNI DNI del client.
     * @param rol rol del client.
     * @return client trobat, si existeix.
     */
    Optional<ClientJpaEntity> findByDNIAndRol(String DNI, String rol);

    /**
     * Cerca clients aplicant diferents filtres opcionals.
     *
     * @param dni DNI del client.
     * @param cadDNI caducitat del DNI.
     * @param llicencia llicència de conduir.
     * @param cadLlicencia caducitat de la llicència.
     * @param adreca adreça del client.
     * @param nacionalitat nacionalitat del client.
     * @param activo estat d'activitat del client.
     * @param rol rol del client.
     * @return llista de clients que coincideixen amb els filtres indicats.
     */
    @Query("SELECT c FROM ClientJpaEntity c WHERE " +
            "c.rol = :rol AND " +
            "(:dni IS NULL OR LOWER(c.DNI) LIKE LOWER(CONCAT('%', :dni, '%'))) AND " +
            "(:cadDNI IS NULL OR LOWER(c.cadDNI) LIKE LOWER(CONCAT('%', :cadDNI, '%'))) AND " +
            "(:llicencia IS NULL OR LOWER(c.Lliconduir) LIKE LOWER(CONCAT('%', :llicencia, '%'))) AND " +
            "(:cadLlicencia IS NULL OR LOWER(c.cdLLiconduir) LIKE LOWER(CONCAT('%', :cadLlicencia, '%'))) AND " +
            "(:adreca IS NULL OR LOWER(c.adreca) LIKE LOWER(CONCAT('%', :adreca, '%'))) AND " +
            "(:nacionalitat IS NULL OR LOWER(c.nacionalitat) LIKE LOWER(CONCAT('%', :nacionalitat, '%'))) AND " +
            "(:activo IS NULL OR c.activo = :activo)")
    List<ClientJpaEntity> cercarClients(
            @Param("dni") String dni,
            @Param("cadDNI") String cadDNI,
            @Param("llicencia") String llicencia,
            @Param("cadLlicencia") String cadLlicencia,
            @Param("adreca") String adreca,
            @Param("nacionalitat") String nacionalitat,
            @Param("activo") Boolean activo,
            @Param("rol") String rol);
}
