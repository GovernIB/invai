package es.caib.invai.back.ejb.catalog.ensSubject;

import es.caib.invai.back.interna.catalog.ensSubject.DTO.EnsSubjectOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.ensSubject.EnsSubjectRepository;
import es.caib.invai.back.service.facade.catalog.ensSubject.EnsSubjectService;
import es.caib.invai.back.service.mapper.catalog.ensSubject.EnsSubjectMapper;
import es.caib.invai.back.service.model.catalog.ensSubject.EnsSubject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Facade service implementation for reading the ENS subjection lookup dictionary.
 *
 * @since 1.0.4
 */
@Service
@Slf4j
@Transactional
public class EnsSubjectServiceFacadeBean implements EnsSubjectService {

    /** Mapper used to translate domain models into outbound response DTOs. */
    @Autowired
    private EnsSubjectMapper ensSubjectMapper;

    /** Repository port used to read the ENS subjection lookup dictionary. */
    @Autowired
    private EnsSubjectRepository ensSubjectRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EnsSubjectOutputDTO> getAll() {
        log.debug("Facade: Fetching every ENS subjection lookup entry");
        List<EnsSubject> ensSubjects = ensSubjectRepository.findAll();
        return ensSubjectMapper.toResponseList(ensSubjects);
    }
}
