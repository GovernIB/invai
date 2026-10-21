package es.caib.invai.back.persistence.repository.catalog.mimeType;

import es.caib.invai.back.service.mapper.catalog.mimeType.MimeTypeMapper;
import es.caib.invai.back.service.model.catalog.mimeType.MimeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Infrastructure repository adapter implementing the outbound port {@link MimeTypeRepository}.
 *
 * @since 1.0.6
 */
@Repository
@Slf4j
public class MimeTypeRepositoryAdapter implements MimeTypeRepository {

    /** Spring Data JPA repository used to access the file format lookup entries. */
    @Autowired
    private MimeTypeJPARepository mimeTypeJPARepository;

    /** Mapper used to convert between file format domain models and JPA entities. */
    @Autowired
    private MimeTypeMapper mimeTypeMapper;

    @Override
    public List<MimeType> findAll() {
        log.debug("Repository: Fetching every file format lookup entry");
        return mimeTypeMapper.toModelList(mimeTypeJPARepository.findAll(Sort.by("extension")));
    }

    @Override
    public MimeType findByExtension(String extension) {
        return mimeTypeJPARepository.findByExtension(extension).map(mimeTypeMapper::toModel).orElse(null);
    }
}
