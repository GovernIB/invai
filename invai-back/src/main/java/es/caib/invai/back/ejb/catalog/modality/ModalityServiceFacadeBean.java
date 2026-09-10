package es.caib.invai.back.ejb.catalog.modality;

import es.caib.invai.back.interna.catalog.modality.DTO.ModalityOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.modality.ModalityRepository;
import es.caib.invai.back.service.facade.catalog.modality.ModalityService;
import es.caib.invai.back.service.mapper.catalog.modality.ModalityMapper;
import es.caib.invai.back.service.model.catalog.modality.Modality;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Facade service implementation for reading the Modality lookup dictionary.
 *
 * @since 1.0.3
 */
@Service
@Slf4j
@Transactional
public class ModalityServiceFacadeBean implements ModalityService {

    /** Mapper used to translate domain models into outbound response DTOs. */
    @Autowired
    private ModalityMapper modalityMapper;

    /** Repository port used to read the modality lookup dictionary. */
    @Autowired
    private ModalityRepository modalityRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ModalityOutputDTO> getAll() {
        log.debug("Facade: Fetching every modality lookup entry");
        List<Modality> modalities = modalityRepository.findAll();
        return modalityMapper.toResponseList(modalities);
    }
}
