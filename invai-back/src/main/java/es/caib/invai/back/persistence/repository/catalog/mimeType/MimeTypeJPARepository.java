package es.caib.invai.back.persistence.repository.catalog.mimeType;

import es.caib.invai.back.persistence.model.catalog.mimeType.LkupMimeTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Native Spring Data JPA repository for the file format lookup entries.
 *
 * @since 1.0.6
 */
@Repository
public interface MimeTypeJPARepository extends JpaRepository<LkupMimeTypeEntity, Long> {

    /**
     * Finds the file format registered for the given extension.
     *
     * @param extension the extension, in lower case and without the dot
     * @return the matching entry, if any
     */
    Optional<LkupMimeTypeEntity> findByExtension(String extension);
}
