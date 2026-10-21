package es.caib.invai.back.persistence.repository.catalog.documentStatus;

import es.caib.invai.back.service.mapper.catalog.documentStatus.DocumentStatusMapper;
import es.caib.invai.back.service.model.catalog.documentStatus.DocumentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Infrastructure repository adapter implementing the outbound port {@link DocumentStatusRepository}.
 *
 * @since 1.0.6
 */
@Repository
@Slf4j
public class DocumentStatusRepositoryAdapter implements DocumentStatusRepository {

    /** Spring Data JPA repository used to access the document status lookup entries. */
    @Autowired
    private DocumentStatusJPARepository documentStatusJPARepository;

    /** Mapper used to convert between status domain models and JPA entities. */
    @Autowired
    private DocumentStatusMapper documentStatusMapper;

    @Override
    public List<DocumentStatus> findAll() {
        log.debug("Repository: Fetching every document status lookup entry");
        return documentStatusMapper.toModelList(documentStatusJPARepository.findAll(Sort.by("name")));
    }

    @Override
    public DocumentStatus findById(Long id) {
        return documentStatusJPARepository.findById(id).map(documentStatusMapper::toModel).orElse(null);
    }
}
