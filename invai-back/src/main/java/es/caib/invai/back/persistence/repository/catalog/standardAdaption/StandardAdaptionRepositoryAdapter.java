package es.caib.invai.back.persistence.repository.catalog.standardAdaption;

import es.caib.invai.back.persistence.model.catalog.standardAdaption.LkupStandardAdaptionEntity;
import es.caib.invai.back.service.mapper.catalog.standardAdaption.StandardAdaptionMapper;
import es.caib.invai.back.service.model.catalog.standardAdaption.StandardAdaption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link StandardAdaptionRepository}.
 *
 * @since 1.0.3
 */
@Repository
@Slf4j
public class StandardAdaptionRepositoryAdapter implements StandardAdaptionRepository {

    /** Spring Data JPA repository providing raw access to the standard adaption lookup table. */
    @Autowired
    private StandardAdaptionJPARepository standardAdaptionJPARepository;

    /** Mapper used to translate between persistent entities and the domain model. */
    @Autowired
    private StandardAdaptionMapper standardAdaptionMapper;

    @Override
    public List<StandardAdaption> findAll() {
        log.debug("Repository: Fetching every standard adaption lookup entry");
        List<LkupStandardAdaptionEntity> entities = standardAdaptionJPARepository.findAll(Sort.by("name"));
        return standardAdaptionMapper.toModelList(entities);
    }
}
