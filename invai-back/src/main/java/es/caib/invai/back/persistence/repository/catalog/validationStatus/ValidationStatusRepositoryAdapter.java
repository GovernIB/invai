package es.caib.invai.back.persistence.repository.catalog.validationStatus;

import es.caib.invai.back.persistence.model.catalog.validationStatus.LkupValidationStatusEntity;
import es.caib.invai.back.service.mapper.catalog.validationStatus.ValidationStatusMapper;
import es.caib.invai.back.service.model.catalog.validationStatus.ValidationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link ValidationStatusRepository}.
 *
 * @since 1.0.5
 */
@Repository
@Slf4j
public class ValidationStatusRepositoryAdapter implements ValidationStatusRepository {

    /** Spring Data JPA repository providing raw access to the validation status lookup table. */
    @Autowired
    private ValidationStatusJPARepository validationStatusJPARepository;

    /** Mapper used to translate between persistent entities and the domain model. */
    @Autowired
    private ValidationStatusMapper validationStatusMapper;

    @Override
    public List<ValidationStatus> findAll() {
        log.debug("Repository: Fetching every validation status lookup entry");
        List<LkupValidationStatusEntity> entities = validationStatusJPARepository.findAll(Sort.by("name"));
        return validationStatusMapper.toModelList(entities);
    }
}
