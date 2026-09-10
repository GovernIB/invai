package es.caib.invai.back.persistence.repository.maintenance.responsible.person;

import es.caib.invai.back.persistence.model.maintenance.responsible.person.PersonAudEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.person.PersonEntity;
import es.caib.invai.back.service.mapper.maintenance.responsible.person.PersonMapper;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link PersonRepository}.
 * Coordinates transactional mutations and logs snapshots onto the audit ledger database layers.
 *
 * @since 1.0.3
 */
@Repository
@Slf4j
public class PersonRepositoryAdapter implements PersonRepository {

    /** Spring Data JPA repository used for CRUD and query operations on {@link PersonEntity}. */
    @Autowired
    private PersonJPARepository personJPARepository;

    /** Spring Data JPA repository used to persist historical audit snapshots of person records. */
    @Autowired
    private PersonAudJPARepository personAudJPARepository;

    /** Mapper used to convert between Person domain models and JPA entities. */
    @Autowired
    private PersonMapper personMapper;

    @Override
    public Person create(Person person) {
        log.info("Repository: Persisting new person entity into database");
        try {
            PersonEntity entity = personMapper.toEntity(person);
            entity = personJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");

            return personMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure persisting person record", e);
            throw e;
        }
    }

    @Override
    public Person update(Person person, Long id) {
        log.info("Repository: Merging changes into existing person record for ID: {}", id);
        try {
            PersonEntity entity = personMapper.toEntity(person);
            entity.setId(id);
            entity = personJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");

            return personMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure updating person record with ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public void delete(Person person) {
        log.info("Repository: Soft deleting person entity with ID: {}", person.getId());
        try {
            PersonEntity entity = personMapper.toEntity(person);
            entity.setId(person.getId());
            entity = personJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing soft delete workflow for ID: {}", person.getId(), e);
            throw e;
        }
    }

    @Override
    public Person findById(Long id) {
        try {
            return personJPARepository.findById(id)
                    .map(personMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Data access failure reading person profile with ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public Page<Person> findAll(PersonCriteria filter, Pageable pageable) {
        log.debug("Repository: Fetching paged persons using standard query layouts");
        try {
            Specification<PersonEntity> spec = PersonSpecification.filterByCriteria(filter);
            return personJPARepository.findAll(spec, pageable).map(personMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Paged person collection fetch exception", e);
            throw e;
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        try {
            return personJPARepository.existsByEmailAndDeletedAtIsNull(email);
        } catch (DataAccessException e) {
            log.error("Repository error: Failed evaluation lookup tracking email uniqueness", e);
            throw e;
        }
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Long id) {
        try {
            return personJPARepository.existsByEmailAndIdNotAndDeletedAtIsNull(email, id);
        } catch (DataAccessException e) {
            log.error("Repository error: Duplicate detection crash analyzing update context for ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public Person findByEmail(String email) {
        try {
            return personJPARepository.findByEmailAndDeletedAtIsNull(email)
                    .map(personMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Data access failure resolving person by email", e);
            throw e;
        }
    }

    @Override
    public List<Person> findAllActiveByCompanyId(Long companyId) {
        try {
            return personJPARepository.findAllByCompanyIdAndDeletedAtIsNull(companyId).stream()
                    .map(personMapper::toModel)
                    .toList();
        } catch (DataAccessException e) {
            log.error("Repository error: Failed lookup of active persons for company ID: {}", companyId, e);
            throw e;
        }
    }

    /**
     * Commits a flat audit record mirror tracing the current transactional context phase status.
     */
    private void saveAuditRecord(PersonEntity entity, String action) {
        try {
            PersonAudEntity aud = new PersonAudEntity();

            aud.setPersonId(entity.getId());
            aud.setFirstName(entity.getFirstName());
            aud.setLastName(entity.getLastName());
            aud.setEmail(entity.getEmail());
            aud.setCompanyId(entity.getCompany() != null ? entity.getCompany().getId() : null);

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
            aud.setUpdatedAt(entity.getUpdatedAt());
            aud.setUpdatedBy(entity.getUpdatedBy());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());
            aud.setAuditUser(Utils.resolveCurrentUsername());

            personAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping person event", e);
            throw e;
        }
    }
}
