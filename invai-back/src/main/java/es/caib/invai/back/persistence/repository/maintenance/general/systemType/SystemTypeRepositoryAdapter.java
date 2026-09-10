package es.caib.invai.back.persistence.repository.maintenance.general.systemType;

import es.caib.invai.back.service.model.maintenance.general.systemType.SystemType;
import es.caib.invai.back.persistence.model.maintenance.general.systemType.SystemTypeEntity;
import es.caib.invai.back.persistence.model.maintenance.general.systemType.SystemTypeAudEntity;
import es.caib.invai.back.service.mapper.maintenance.general.systemType.SystemTypeMapper;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link SystemTypeRepository}.
 * <p>
 * Orchestrates technical structural operations, routing operations across active database layers
 * ({@link SystemTypeJPARepository} and {@link SystemTypeAudJPARepository}) while utilizing
 * mapping layers to enforce decoupling rules.
 * </p>
 *
 * @since 1.0.2
 */
@Repository
@Slf4j
public class SystemTypeRepositoryAdapter implements SystemTypeRepository {

    /** Spring Data JPA repository providing CRUD access to live system type entities. */
    @Autowired
    private SystemTypeJPARepository systemTypeJPARepository;

    /** Spring Data JPA repository providing persistence of historical system type audit snapshots. */
    @Autowired
    private SystemTypeAudJPARepository systemTypeAudJPARepository;

    /** Mapper converting between SystemType domain models and JPA entities. */
    @Autowired
    private SystemTypeMapper systemTypeMapper;

    /**
     * Resolves a system type record by its unique key, filtering out soft-deleted data traces.
     *
     * @param id unique sequence row identifier tracking the asset
     * @return the mapped domain {@link SystemType} instance, or {@code null} if missing or soft-deleted
     */
    @Override
    public SystemType findById(Long id) {
        return systemTypeJPARepository.findById(id)
                
                .map(systemTypeMapper::toModel)
                .orElse(null);
    }

    /**
     * Fetches all architectural system types from the database matching the supplied dynamic
     * criteria filters and maps them to domain models.
     *
     * @param filter   DTO containing dynamic search predicates used to build the query
     * @param pageable pagination and sorting parameters
     * @return a page containing the mapped domain objects
     */
    @Override
    public Page<SystemType> findAll(SystemTypeCriteria filter, Pageable pageable) {
        Specification<SystemTypeEntity> spec = SystemTypeSpecification.filterByCriteria(filter);
        return systemTypeJPARepository.findAll(spec, pageable).map(systemTypeMapper::toModel);
    }

    /**
     * Delegates uniqueness validation based on system type labels to the active JPA layer.
     *
     * @param name target descriptive name text to verify
     * @return {@code true} if a match exists, {@code false} otherwise
     */
    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return systemTypeJPARepository.existsByNameAndDeletedAtIsNull(name);
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
        return systemTypeJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    @Override
    public boolean existsByNameEsAndDeletedAtIsNull(String nameEs) {
        return systemTypeJPARepository.existsByNameEsAndDeletedAtIsNull(nameEs);
    }

    @Override
    public boolean existsByNameEsAndIdNotAndDeletedAtIsNull(String nameEs, Long id) {
        return systemTypeJPARepository.existsByNameEsAndIdNotAndDeletedAtIsNull(nameEs, id);
    }

    /**
     * Maps a transient domain system type object, persists it into relational systems,
     * registers historical snapshots, and returns a business context data schema model.
     *
     * @param systemType the data schema model profile values containing registration variables
     * @return persistent domain profile containing data markers
     */
    @Override
    public SystemType create(SystemType systemType) {
        log.info("Repository: Persisting new system type entity into database");
        SystemTypeEntity entity = systemTypeMapper.toEntity(systemType);

        entity = systemTypeJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return systemTypeMapper.toModel(entity);
    }

    /**
     * Maps updated system type configurations onto relational records, commits changes to active data rows,
     * appends audit trails, and returns fresh outcomes.
     *
     * @param systemType property modifications schema containing target update variables data
     * @param id         persistent identity identifier index tracking state records
     * @return updated model domain properties specifications variables
     */
    @Override
    public SystemType update(SystemType systemType, Long id) {
        log.info("Repository: Merging changes into existing system type record for ID: {}", id);
        SystemTypeEntity entity = systemTypeMapper.toEntity(systemType);
        entity.setId(id);

        entity = systemTypeJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return systemTypeMapper.toModel(entity);
    }

    /**
     * Performs a soft logical deactivation by rewriting active system type entities,
     * flagging timeline delete records, and saving tracking logs.
     *
     * @param systemType target domain representation configuration modeling details to remove
     */
    @Override
    public void delete(SystemType systemType) {
        log.info("Repository: Soft deleting system type entity with ID: {}", systemType.getId());
        SystemTypeEntity entity = systemTypeMapper.toEntity(systemType);
        entity.setId(systemType.getId());

        entity = systemTypeJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Compiles point-in-time snapshot mirror values from active system type entries,
     * resolves identity user info claims tokens, and saves historic compliance tracks.
     *
     * @param entity the currently tracked persistent data state row entity map representation
     * @param action systemic string token descriptor identifying database mutation contexts (e.g., INSERT, UPDATE, DELETE)
     */
    private void saveAuditRecord(SystemTypeEntity entity, String action) {
        SystemTypeAudEntity aud = new SystemTypeAudEntity();

        aud.setSystemTypeId(entity.getId());
        aud.setName(entity.getName());
        aud.setNameEs(entity.getNameEs());

        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
       
                    aud.setUpdatedAt(entity.getUpdatedAt());
            aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        systemTypeAudJPARepository.save(aud);
    }
}