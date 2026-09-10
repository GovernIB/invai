package es.caib.invai.back.ejb.catalog.responsibleType;

import es.caib.invai.back.interna.catalog.responsibleType.DTO.ResponsibleTypeOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.responsibleType.ResponsibleTypeRepository;
import es.caib.invai.back.service.facade.catalog.responsibleType.ResponsibleTypeService;
import es.caib.invai.back.service.mapper.catalog.responsibleType.ResponsibleTypeMapper;
import es.caib.invai.back.service.model.catalog.responsibleType.ResponsibleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Facade service implementation for reading the Responsible Type lookup dictionary.
 *
 * @since 1.0.3
 */
@Service
@Slf4j
@Transactional
public class ResponsibleTypeServiceFacadeBean implements ResponsibleTypeService {

    /** Mapper used to translate domain models into outbound response DTOs. */
    @Autowired
    private ResponsibleTypeMapper responsibleTypeMapper;

    /** Repository port used to read the responsible type lookup dictionary. */
    @Autowired
    private ResponsibleTypeRepository responsibleTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ResponsibleTypeOutputDTO> getAll() {
        log.debug("Facade: Fetching every responsible type lookup entry");
        List<ResponsibleType> responsibleTypes = responsibleTypeRepository.findAll();
        return responsibleTypeMapper.toResponseList(responsibleTypes);
    }
}
