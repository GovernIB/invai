package es.caib.invai.api.persistence.repository.application.system_database.system;

import es.caib.invai.api.persistence.model.AppSystemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppSystemJPARepository extends JpaRepository<AppSystemEntity, Long>, JpaSpecificationExecutor<AppSystemEntity> {

    Optional<AppSystemEntity> findById(Long id);

    boolean existsByInformationSystemDbIdAndSystemId(Long informationSystemDbId, Long systemId);

    boolean existsByInformationSystemDbIdAndSystemIdAndIdNot(Long informationSystemDbId, Long systemId, Long id);
}
