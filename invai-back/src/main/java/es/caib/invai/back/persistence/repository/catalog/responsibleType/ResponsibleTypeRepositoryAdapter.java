package es.caib.invai.back.persistence.repository.catalog.responsibleType;

import es.caib.invai.back.persistence.model.catalog.responsibleType.LkupResponsibleTypeEntity;
import es.caib.invai.back.service.mapper.catalog.responsibleType.ResponsibleTypeMapper;
import es.caib.invai.back.service.model.catalog.responsibleType.ResponsibleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link ResponsibleTypeRepository}.
 *
 * @since 1.0.3
 */
@Repository
@Slf4j
public class ResponsibleTypeRepositoryAdapter implements ResponsibleTypeRepository {

    /** Spring Data JPA repository providing raw access to the responsible type lookup table. */
    @Autowired
    private ResponsibleTypeJPARepository responsibleTypeJPARepository;

    /** Mapper used to translate between persistent entities and the domain model. */
    @Autowired
    private ResponsibleTypeMapper responsibleTypeMapper;

    @Override
    public List<ResponsibleType> findAll() {
        log.info("Repository: Fetching every responsible type lookup entry");
        List<LkupResponsibleTypeEntity> entities = responsibleTypeJPARepository.findAll(Sort.by("name"));
        return responsibleTypeMapper.toModelList(entities);
    }

    @Override
    public ResponsibleType findById(Long id) {
        log.info("Repository: Fetching responsible type lookup entry by ID: {}", id);
        return responsibleTypeJPARepository.findById(id)
                .map(responsibleTypeMapper::toModel)
                .orElse(null);
    }
}
