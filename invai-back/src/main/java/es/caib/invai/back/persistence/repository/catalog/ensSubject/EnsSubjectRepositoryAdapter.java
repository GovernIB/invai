package es.caib.invai.back.persistence.repository.catalog.ensSubject;

import es.caib.invai.back.persistence.model.catalog.ensSubject.LkupEnsSubjectEntity;
import es.caib.invai.back.service.mapper.catalog.ensSubject.EnsSubjectMapper;
import es.caib.invai.back.service.model.catalog.ensSubject.EnsSubject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link EnsSubjectRepository}.
 *
 * @since 1.0.4
 */
@Repository
@Slf4j
public class EnsSubjectRepositoryAdapter implements EnsSubjectRepository {

    /** Spring Data JPA repository providing raw access to the ENS subjection lookup table. */
    @Autowired
    private EnsSubjectJPARepository ensSubjectJPARepository;

    /** Mapper used to translate between persistent entities and the domain model. */
    @Autowired
    private EnsSubjectMapper ensSubjectMapper;

    @Override
    public List<EnsSubject> findAll() {
        log.debug("Repository: Fetching every ENS subjection lookup entry");
        List<LkupEnsSubjectEntity> entities = ensSubjectJPARepository.findAll(Sort.by("name"));
        return ensSubjectMapper.toModelList(entities);
    }

    @Override
    public EnsSubject findById(Long id) {
        log.debug("Repository: Fetching ENS subjection lookup entry by ID: {}", id);
        return ensSubjectJPARepository.findById(id)
                .map(ensSubjectMapper::toModel)
                .orElse(null);
    }
}
