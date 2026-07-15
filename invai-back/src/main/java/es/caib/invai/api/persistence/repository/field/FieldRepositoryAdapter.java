package es.caib.invai.api.persistence.repository.field;

import es.caib.invai.api.service.model.Field;
import es.caib.invai.api.persistence.model.FieldEntity;
import es.caib.invai.api.persistence.model.FieldAudEntity;
import es.caib.invai.api.service.mapper.FieldMapper;
import es.caib.invai.api.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link FieldRepository}.
 * <p>
 * Orchestrates technical structural operations, routing operations across active database layers
 * ({@link FieldJPARepository} and {@link FieldAudJPARepository}) while utilizing
 * mapping layers to enforce decoupling rules.
 * </p>
 *
 * @since 1.0.0
 */
@Repository
@Slf4j
public class FieldRepositoryAdapter implements FieldRepository {

    @Autowired
    private FieldJPARepository fieldJPARepository;

    @Autowired
    private FieldAudJPARepository fieldAudJPARepository;

    @Autowired
    private FieldMapper fieldMapper;

    /**
     * Resolves a functional area field record by its unique key, filtering out soft-deleted data traces.
     *
     * @param id unique sequence row identifier tracking the asset
     * @return the mapped domain {@link Field} instance, or {@code null} if missing or soft-deleted
     */
    @Override
    public Field findById(Long id) {
        return fieldJPARepository.findById(id)
                .filter(f -> f.getDeletedAt() == null)
                .map(fieldMapper::toModel)
                .orElse(null);
    }

    /**
     * Fetches all active operational business fields from the database and maps them to domain models.
     *
     * @param pageable pagination and sorting parameters
     * @return a page containing the mapped domain objects
     */
    @Override
    public Page<Field> findAll(Pageable pageable) {
        return fieldJPARepository.findAllActive(pageable).map(fieldMapper::toModel);
    }

    /**
     * Delegates uniqueness validation based on functional area field labels to the active JPA layer.
     *
     * @param name target descriptive name text to verify
     * @return {@code true} if a match exists, {@code false} otherwise
     */
    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return fieldJPARepository.existsByNameAndDeletedAtIsNull(name);
    }

    /**
     * Delegates update-specific label conflict validation queries to the underlying JPA layer.
     *
     * @param name target descriptive name text to verify
     * @param id   the primary identifier sequence key to exclude
     * @return {@code true} if a duplicate collision occurs, {@code false} otherwise
     */
    @Override
    public boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id) {
        return fieldJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    /**
     * Maps a transient domain field object, persists it into relational systems,
     * registers historical snapshots, and returns a business context data schema model.
     *
     * @param field the data schema model profile values containing registration variables
     * @return persistent domain profile containing data markers
     */
    @Override
    public Field create(Field field) {
        log.info("Repository: Persisting new field entity into database");
        FieldEntity entity = fieldMapper.toEntity(field);
        entity = fieldJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return fieldMapper.toModel(entity);
    }

    /**
     * Maps updated field configurations onto relational records, commits changes to active data rows,
     * appends audit trails, and returns fresh outcomes.
     *
     * @param field property modifications schema containing target update variables data
     * @param id    persistent identity identifier index tracking state records
     * @return updated model domain properties specifications variables
     */
    @Override
    public Field update(Field field, Long id) {
        log.info("Repository: Merging changes into existing field record for ID: {}", id);
        FieldEntity entity = fieldMapper.toEntity(field);
        entity.setId(id);
        entity = fieldJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return fieldMapper.toModel(entity);
    }

    /**
     * Performs a soft logical deactivation by rewriting active field entities,
     * flagging timeline delete records, and saving tracking logs.
     *
     * @param field target domain representation configuration modeling details to remove
     */
    @Override
    public void delete(Field field) {
        log.info("Repository: Soft deleting field entity with ID: {}", field.getId());
        FieldEntity entity = fieldMapper.toEntity(field);
        entity.setId(field.getId());
        entity = fieldJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Compiles point-in-time snapshot mirror values from active field entries,
     * resolves identity user info claims tokens, and saves historic compliance tracks.
     *
     * @param entity the currently tracked persistent data state row entity map representation
     * @param action systemic string token descriptor identifying database mutation contexts (e.g., INSERT, UPDATE, DELETE)
     */
    private void saveAuditRecord(FieldEntity entity, String action) {
        FieldAudEntity aud = new FieldAudEntity();

        aud.setFieldId(entity.getId());
        aud.setName(entity.getName());
        aud.setNameEs(entity.getNameEs());

        aud.setCreatedAt(entity.getCreatedAt());
        aud.setCreatedBy(entity.getCreatedBy());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());

        OidcUserInfo currentUser = (OidcUserInfo) SecurityUtils.getCurrentUser();
        if (currentUser != null && currentUser.getClaims().get("preferred_username") != null) {
            aud.setAuditUser((String) currentUser.getClaims().get("preferred_username"));
        } else {
            aud.setAuditUser("SYSTEM");
        }

        fieldAudJPARepository.save(aud);
    }
}