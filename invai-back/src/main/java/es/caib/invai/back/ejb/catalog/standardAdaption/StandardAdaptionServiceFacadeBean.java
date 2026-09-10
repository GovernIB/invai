package es.caib.invai.back.ejb.catalog.standardAdaption;

import es.caib.invai.back.interna.catalog.standardAdaption.DTO.StandardAdaptionOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.standardAdaption.StandardAdaptionRepository;
import es.caib.invai.back.service.facade.catalog.standardAdaption.StandardAdaptionService;
import es.caib.invai.back.service.mapper.catalog.standardAdaption.StandardAdaptionMapper;
import es.caib.invai.back.service.model.catalog.standardAdaption.StandardAdaption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Facade service implementation for reading the Standard Adaption lookup dictionary.
 *
 * @since 1.0.3
 */
@Service
@Slf4j
@Transactional
public class StandardAdaptionServiceFacadeBean implements StandardAdaptionService {

    /** Mapper used to translate domain models into outbound response DTOs. */
    @Autowired
    private StandardAdaptionMapper standardAdaptionMapper;

    /** Repository port used to read the standard adaption lookup dictionary. */
    @Autowired
    private StandardAdaptionRepository standardAdaptionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StandardAdaptionOutputDTO> getAll() {
        log.debug("Facade: Fetching every standard adaption lookup entry");
        List<StandardAdaption> standardAdaptions = standardAdaptionRepository.findAll();
        return standardAdaptionMapper.toResponseList(standardAdaptions);
    }
}
