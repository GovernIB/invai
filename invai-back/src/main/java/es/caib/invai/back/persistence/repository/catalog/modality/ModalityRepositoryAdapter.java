package es.caib.invai.back.persistence.repository.catalog.modality;

import es.caib.invai.back.persistence.model.catalog.modality.LkupModalityEntity;
import es.caib.invai.back.service.mapper.catalog.modality.ModalityMapper;
import es.caib.invai.back.service.model.catalog.modality.Modality;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link ModalityRepository}.
 *
 * @since 1.0.3
 */
@Repository
@Slf4j
public class ModalityRepositoryAdapter implements ModalityRepository {

    /** Spring Data JPA repository providing raw access to the modality lookup table. */
    @Autowired
    private ModalityJPARepository modalityJPARepository;

    /** Mapper used to translate between persistent entities and the domain model. */
    @Autowired
    private ModalityMapper modalityMapper;

    @Override
    public List<Modality> findAll() {
        log.debug("Repository: Fetching every modality lookup entry");
        List<LkupModalityEntity> entities = modalityJPARepository.findAll(Sort.by("name"));
        return modalityMapper.toModelList(entities);
    }
}
