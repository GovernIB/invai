package es.caib.invai.api.persistence.repository.application;

import es.caib.invai.api.persistence.model.ApplicationAudEntity;
import es.caib.invai.api.persistence.model.ApplicationEntity;
import es.caib.invai.api.utils.SecurityUtils;
import es.caib.invai.api.service.mapper.ApplicationMapper;
import es.caib.invai.api.service.model.Application;
import es.caib.invai.api.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link ApplicationRepository}.
 * <p>
 * Manages transactional operations targeting application operational metadata and handles
 * historical record keeping by coordinating across relational persistence engines.
 * </p>
 *
 * @since 1.0.0
 */
@Repository
@Slf4j
public class ApplicationRepositoryAdapter implements ApplicationRepository {

    @Autowired
    private ApplicationJPARepository applicationJPARepository;

    @Autowired
    private ApplicationAudJPARepository applicationAudJPARepository;

    @Autowired
    private ApplicationMapper applicationMapper;

    /**
     * Maps a transient domain model, persists its structural state parameters into relational tables,
     * registers historical operational logs, and returns the persistent business snapshot representation.
     *
     * @param application transient domain entity model capturing target creation variables data
     * @return persistent domain application snapshot mirroring saved data variables
     */
    @Override
    public Application create(Application application) {
        log.info("Repository: Persisting new application entity into database");
        try {
            ApplicationEntity applicationEntity = applicationMapper.toEntity(application);
            applicationEntity = applicationJPARepository.save(applicationEntity);

            saveAuditRecord(applicationEntity, "INSERT");

            return applicationMapper.toModel(applicationEntity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure persisting application record into database", e);
            throw e;
        }
    }

    /**
     * Synchronizes modifications onto existing relational records, commits adjustments to active
     * data rows matching the designated primary key, and appends a corresponding audit trail event.
     *
     * @param application property modifications schema containing target update variables data
     * @param id          persistent identity identifier index tracking data records to merge
     * @return updated model domain properties specifications variables
     */
    @Override
    public Application update(Application application, Long id) {
        log.info("Repository: Merging changes into existing application record for ID: {}", id);
        try {
            ApplicationEntity applicationEntity = applicationMapper.toEntity(application);
            applicationEntity.setId(id);
            applicationEntity = applicationJPARepository.save(applicationEntity);

            saveAuditRecord(applicationEntity, "UPDATE");

            return applicationMapper.toModel(applicationEntity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure updating application record with ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Processes logical soft-delete states by updating state records onto relational structures
     * and writing tracking event mutations to historical database scopes.
     *
     * @param application the data schema reference instance targeted for deactivation
     */
    @Override
    public void delete(Application application) {
        log.info("Repository: Soft deleting administrative unit entity with ID: {}", application.getId());
        try {
            ApplicationEntity applicationEntity = applicationMapper.toEntity(application);
            applicationEntity.setId(application.getId());

            applicationEntity = applicationJPARepository.save(applicationEntity);

            saveAuditRecord(applicationEntity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing soft delete workflow for ID: {}", application.getId(), e);
            throw e;
        }
    }

    /**
     * Locates a single active application record, mapping it back to domain structures.
     *
     * @param id unique database sequence row identifier tracking the asset
     * @return mapped domain {@link Application} instance
     * @throws IllegalArgumentException if the record is missing or soft-deleted
     */
    @Override
    public Application findById(Long id) {
        try {
            Optional<ApplicationEntity> result = applicationJPARepository.findByIdAndDeletedAtIsNull(id);
            if (result.isEmpty()) {
                throw new IllegalArgumentException(String.format(Constants.ERR_APP_NOT_FOUND, id));
            }
            return applicationMapper.toModel(result.get());
        } catch (DataAccessException e) {
            log.error("Repository error: Data access failure reading application profile with ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Generates a structural database Specification layer execution matrix to apply
     * complex filtering criteria dynamically.
     *
     * @param criteria dynamic query restrictions mapping parameter object variables
     * @param pageable threshold limits and pagination structures metadata
     * @return paginated slice container listing matched domain profiles
     */
    @Override
    public Page<Application> findAll(ApplicationCriteria criteria, Pageable pageable) {
        log.info("Repository: Fetching paged applications using isolated Specification component");
        try {
            Specification<ApplicationEntity> spec = ApplicationSpecification.filterByCriteria(criteria);

            Page<ApplicationEntity> entityPage = applicationJPARepository.findAll(spec, pageable);
            return entityPage.map(applicationMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Paged application collection fetch exception applied under dynamic filters", e);
            throw e;
        }
    }

    /**
     * Evaluates active identification namespace registries to check for corporate application code collisions.
     *
     * @param code target unique operational code query criteria
     * @return {@code true} if conflicts exist within non-deleted records, {@code false} otherwise
     */
    @Override
    public boolean existsByCode(String code) {
        try {
            return applicationJPARepository.existsByCodeAndDeletedAtIsNull(code);
        } catch (DataAccessException e) {
            log.error("Repository error: Failed evaluation lookup tracking application code uniqueness", e);
            throw e;
        }
    }

    /**
     * Evaluates active system properties to check for acronymous prefix collisions.
     *
     * @param prefix target system acronym registration prefix query criteria
     * @return {@code true} if conflicts exist within non-deleted records, {@code false} otherwise
     */
    @Override
    public boolean existsByPrefix(String prefix) {
        try {
            return applicationJPARepository.existsByPrefixAndDeletedAtIsNull(prefix);
        } catch (DataAccessException e) {
            log.error("Repository error: Failed evaluation lookup tracking system configuration prefix uniqueness", e);
            throw e;
        }
    }

    /**
     * Verifies if alternative active entities conflict with a given application code, excluding a designated record reference ID.
     * Typically utilized during update operations.
     *
     * @param code target unique operational code query criteria
     * @param id   the persistent primary reference identity to exclude from evaluation scopes
     * @return {@code true} if duplicates exist outside the index parameter, {@code false} otherwise
     */
    @Override
    public boolean existsByCodeAndIdNot(String code, Long id) {
        try {
            return applicationJPARepository.existsByCodeAndIdNotAndDeletedAtIsNull(code, id);
        } catch (DataAccessException e) {
            log.error("Repository error: Multi-conditional duplicate detection crash analyzing code uniqueness on update context for ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Verifies if alternative active entities conflict with a given system acronym prefix, excluding a designated record reference ID.
     * Typically utilized during update operations.
     *
     * @param prefix target system acronym registration prefix query criteria
     * @param id     the persistent primary reference identity to exclude from evaluation scopes
     * @return {@code true} if duplicates exist outside the index parameter, {@code false} otherwise
     */
    @Override
    public boolean existsByPrefixAndIdNot(String prefix, Long id) {
        try {
            return applicationJPARepository.existsByPrefixAndIdNotAndDeletedAtIsNull(prefix, id);
        } catch (DataAccessException e) {
            log.error("Repository error: Multi-conditional duplicate detection crash analyzing prefix uniqueness on update context for ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Queries the active database layers using native specifications to verify structural category
     * bindings against active application records.
     *
     * @param categoryId unique primary reference identifier tracking the taxonomy item
     * @return {@code true} if operational conflicts exist due to active dependencies, {@code false} otherwise
     */
    @Override
    public boolean existsByCategoryId(Long categoryId) {
        try {
            return applicationJPARepository.existsByCategoryIdAndDeletedAtIsNull(categoryId);
        } catch (DataAccessException e) {
            log.error("Repository error: Integrity tracking collision check aborted evaluating category dependency graph link for category ID: {}", categoryId, e);
            throw e;
        }
    }

    /**
     * Extracts values from active application entities, mirrors relational association foreign keys,
     * resolves context identity user claims from OIDC profiles, and commits a tracking record
     * to the historical audit system.
     *
     * @param entity active operational entity state row reflecting data variations mapping parameters
     * @param action string tag describing structural mutation contexts (e.g., INSERT, UPDATE, DELETE)
     */
    private void saveAuditRecord(ApplicationEntity entity, String action) {
        try {
            ApplicationAudEntity aud = new ApplicationAudEntity();

            aud.setAppApplicationId(entity.getId());
            aud.setCode(entity.getCode());
            aud.setPrefix(entity.getPrefix());
            aud.setName(entity.getName());
            aud.setDescription(entity.getDescription());
            aud.setExpirationDate(entity.getExpirationDate());

            aud.setCreatedAt(entity.getCreatedAt());
            aud.setCreatedBy(entity.getCreatedBy());
            aud.setUpdatedAt(entity.getUpdatedAt());
            aud.setUpdatedBy(entity.getUpdatedBy());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            if (entity.getCategory() != null) aud.setCategoryId(entity.getCategory().getId());
            if (entity.getSystemType() != null) aud.setSystemTypeId(entity.getSystemType().getId());
            if (entity.getField() != null) aud.setFieldId(entity.getField().getId());
            if (entity.getAdmUnit() != null) aud.setAdmUnitId(entity.getAdmUnit().getId());
            if (entity.getCsCommission() != null) aud.setCommissionId(entity.getCsCommission().getId());
            if (entity.getStatus() != null) aud.setStatusId(entity.getStatus().getId());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());

            Object currentUserObj = SecurityUtils.getCurrentUser();
            if (currentUserObj instanceof OidcUserInfo) {
                OidcUserInfo currentUser = (OidcUserInfo) currentUserObj;
                if (currentUser.getClaims().get("preferred_username") != null) {
                    String username = (String) currentUser.getClaims().get("preferred_username");
                    aud.setAuditUser(username);
                } else {
                    aud.setAuditUser("SYSTEM");
                }
            } else if (currentUserObj instanceof String) {
                aud.setAuditUser((String) currentUserObj);
            } else {
                aud.setAuditUser("SYSTEM");
            }

            applicationAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping application event", e);
            throw e;
        }
    }
}