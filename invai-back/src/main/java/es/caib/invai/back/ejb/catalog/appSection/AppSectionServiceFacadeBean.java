package es.caib.invai.back.ejb.catalog.appSection;

import es.caib.invai.back.interna.catalog.appSection.DTO.AppSectionOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.appSection.AppSectionRepository;
import es.caib.invai.back.service.facade.catalog.appSection.AppSectionService;
import es.caib.invai.back.service.mapper.catalog.appSection.AppSectionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Facade service implementation exposing the application section lookup.
 *
 * @since 1.0.6
 */
@Service
@Slf4j
@Transactional
public class AppSectionServiceFacadeBean implements AppSectionService {

    /** Mapper used to convert section domain models into DTOs. */
    @Autowired
    private AppSectionMapper appSectionMapper;

    /** Repository port used to query the section lookup. */
    @Autowired
    private AppSectionRepository appSectionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AppSectionOutputDTO> getAll() {
        log.debug("Facade: Fetching every application section lookup entry");
        return appSectionMapper.toResponseList(appSectionRepository.findAll());
    }
}
