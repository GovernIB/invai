package es.caib.invai.back.ejb.catalog.mimeType;

import es.caib.invai.back.interna.catalog.mimeType.DTO.MimeTypeOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.mimeType.MimeTypeRepository;
import es.caib.invai.back.service.facade.catalog.mimeType.MimeTypeService;
import es.caib.invai.back.service.mapper.catalog.mimeType.MimeTypeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Facade service implementation exposing the file format lookup.
 *
 * @since 1.0.6
 */
@Service
@Slf4j
@Transactional
public class MimeTypeServiceFacadeBean implements MimeTypeService {

    /** Mapper used to convert file format domain models into DTOs. */
    @Autowired
    private MimeTypeMapper mimeTypeMapper;

    /** Repository port used to query the file format lookup. */
    @Autowired
    private MimeTypeRepository mimeTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MimeTypeOutputDTO> getAll() {
        log.debug("Facade: Fetching every file format lookup entry");
        return mimeTypeMapper.toResponseList(mimeTypeRepository.findAll());
    }
}
