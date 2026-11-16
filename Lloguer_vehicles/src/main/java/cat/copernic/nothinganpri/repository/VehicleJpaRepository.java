package cat.copernic.nothinganpri.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cat.copernic.nothinganpri.entity.VehicleJpaEntity; // ajusta el paquete si es distinto

@Repository
public interface VehicleJpaRepository extends JpaRepository<VehicleJpaEntity, Long> {

    Optional<VehicleJpaEntity> findByMatricula(String matricula);

    boolean existsByMatriculaIgnoreCase(String matricula);

    boolean existsByMatriculaIgnoreCaseAndIdNot(String matricula, Long id);
}