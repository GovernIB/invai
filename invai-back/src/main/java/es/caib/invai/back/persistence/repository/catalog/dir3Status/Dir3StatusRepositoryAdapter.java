package es.caib.invai.back.persistence.repository.catalog.dir3Status;

import es.caib.invai.back.persistence.model.catalog.dir3Status.LkupDir3StatusEntity;
import es.caib.invai.back.service.mapper.catalog.dir3Status.Dir3StatusMapper;
import es.caib.invai.back.service.model.catalog.dir3Status.Dir3Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link Dir3StatusRepository}.
 *
 * @since 1.0.5
 */
@Repository
@Slf4j
public class Dir3StatusRepositoryAdapter implements Dir3StatusRepository {

    /** Spring Data JPA repository providing raw access to the DIR3 status lookup table. */
    @Autowired
    private Dir3StatusJPARepository dir3StatusJPARepository;

    /** Mapper used to translate between persistent entities and the domain model. */
    @Autowired
    private Dir3StatusMapper dir3StatusMapper;

    @Override
    public List<Dir3Status> findAll() {
        log.debug("Repository: Fetching every DIR3 status lookup entry");
        List<LkupDir3StatusEntity> entities = dir3StatusJPARepository.findAll(Sort.by("name"));
        return dir3StatusMapper.toModelList(entities);
    }
}
