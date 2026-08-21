package es.caib.invai.back.persistence.repository.maintenance.responsible.person;

import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Outbound Port boundary interface declaring relational persistence mechanisms
 * for the person domain model layer.
 *
 * @since 1.0.3
 */
public interface PersonRepository {

    /**
     * Registers a new person profile structure inside relational tracking systems.
     */
    Person create(Person person);

    /**
     * Commits changes onto active domain records maps identified by a target primary sequence index.
     */
    Person update(Person person, Long id);

    /**
     * Flags tracking profiles context records as soft-deleted inside persistence layers.
     */
    void delete(Person person);

    /**
     * Resolves an active person record profile matching an identification primary index.
     */
    Person findById(Long id);

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries.
     */
    Page<Person> findAll(PersonCriteria filter, Pageable pageable);

    /**
     * Confirms uniqueness of the e-mail address across active person registries.
     */
    boolean existsByEmail(String email);

    /**
     * Checks for duplicate e-mail conflicts excluding a target primary reference row.
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Resolves the currently active person with the given email, if any.
     *
     * @param email the email address to look up
     * @return the matching active person, or {@code null} if none is found
     */
    Person findByEmail(String email);

    /**
     * Resolves every active person currently belonging to the given company.
     *
     * @param companyId the company identifier
     * @return the matching active persons
     */
    List<Person> findAllActiveByCompanyId(Long companyId);
}
