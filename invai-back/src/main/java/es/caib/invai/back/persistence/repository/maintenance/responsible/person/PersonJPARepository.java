package es.caib.invai.back.persistence.repository.maintenance.responsible.person;

import es.caib.invai.back.persistence.model.maintenance.responsible.person.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Native Spring Data JPA repository layer providing entity lifecycle updates for {@link PersonEntity}.
 *
 * @since 1.0.3
 */
@Repository
public interface PersonJPARepository extends JpaRepository<PersonEntity, Long>, JpaSpecificationExecutor<PersonEntity> {

    /**
     * Finds a person entity by its unique identifier.
     *
     * @param id the person identifier
     * @return an {@link Optional} containing the matching entity, or empty if none found
     */
    Optional<PersonEntity> findById(Long id);

    /**
     * Checks whether an active (not logically deleted) person exists with the given email.
     *
     * @param email the email address to check
     * @return {@code true} if an active person with that email exists
     */
    boolean existsByEmailAndDeletedAtIsNull(String email);

    /**
     * Checks whether another active person (excluding the given identifier) exists with the given email.
     *
     * @param email the email address to check
     * @param id    the identifier to exclude from the check
     * @return {@code true} if an active person other than the given identifier owns that email
     */
    boolean existsByEmailAndIdNotAndDeletedAtIsNull(String email, Long id);

    /**
     * Finds the currently active person with the given email, if any.
     *
     * @param email the email address to look up
     * @return the matching active person entity, if present
     */
    Optional<PersonEntity> findByEmailAndDeletedAtIsNull(String email);

    /**
     * Finds every active (not logically deleted) person belonging to the given company.
     *
     * @param companyId the company identifier
     * @return the matching active person entities
     */
    List<PersonEntity> findAllByCompanyIdAndDeletedAtIsNull(Long companyId);
}
