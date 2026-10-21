package es.caib.invai.back.persistence.repository.catalog.appSection;

import es.caib.invai.back.service.mapper.catalog.appSection.AppSectionMapper;
import es.caib.invai.back.service.model.catalog.appSection.AppSection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Infrastructure repository adapter implementing the outbound port {@link AppSectionRepository}.
 *
 * @since 1.0.6
 */
@Repository
@Slf4j
public class AppSectionRepositoryAdapter implements AppSectionRepository {

    /** Spring Data JPA repository used to access the section lookup entries. */
    @Autowired
    private AppSectionJPARepository appSectionJPARepository;

    /** Mapper used to convert between section domain models and JPA entities. */
    @Autowired
    private AppSectionMapper appSectionMapper;

    @Override
    public List<AppSection> findAll() {
        log.debug("Repository: Fetching every application section lookup entry");
        return appSectionMapper.toModelList(appSectionJPARepository.findAll(Sort.by("name")));
    }

    @Override
    public AppSection findById(Long id) {
        return appSectionJPARepository.findById(id).map(appSectionMapper::toModel).orElse(null);
    }
}
