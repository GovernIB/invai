package es.caib.invai.back.persistence.repository.catalog.status;

import es.caib.invai.back.persistence.model.catalog.status.LkupStatusEntity;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapper;
import es.caib.invai.back.service.model.catalog.status.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link StatusRepository}.
 *
 * @since 1.0.3
 */
@Repository
@Slf4j
public class StatusRepositoryAdapter implements StatusRepository {

    /** Spring Data JPA repository providing raw access to the status lookup table. */
    @Autowired
    private StatusJPARepository statusJPARepository;

    /** Mapper used to translate between persistent entities and the domain model. */
    @Autowired
    private StatusMapper statusMapper;

    @Override
    public List<Status> findAll() {
        log.debug("Repository: Fetching every status lookup entry");
        List<LkupStatusEntity> entities = statusJPARepository.findAll(Sort.by("name"));
        return statusMapper.toModelList(entities);
    }
}
