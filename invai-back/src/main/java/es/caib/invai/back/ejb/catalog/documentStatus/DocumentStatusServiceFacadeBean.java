package es.caib.invai.back.ejb.catalog.documentStatus;

import es.caib.invai.back.interna.catalog.documentStatus.DTO.DocumentStatusOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.documentStatus.DocumentStatusRepository;
import es.caib.invai.back.service.facade.catalog.documentStatus.DocumentStatusService;
import es.caib.invai.back.service.mapper.catalog.documentStatus.DocumentStatusMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Facade service implementation exposing the document status lookup.
 *
 * @since 1.0.6
 */
@Service
@Slf4j
@Transactional
public class DocumentStatusServiceFacadeBean implements DocumentStatusService {

    /** Mapper used to convert status domain models into DTOs. */
    @Autowired
    private DocumentStatusMapper documentStatusMapper;

    /** Repository port used to query the document status lookup. */
    @Autowired
    private DocumentStatusRepository documentStatusRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentStatusOutputDTO> getAll() {
        log.debug("Facade: Fetching every document status lookup entry");
        return documentStatusMapper.toResponseList(documentStatusRepository.findAll());
    }
}
