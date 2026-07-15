package es.caib.invai.api.persistence.repository.application;

import es.caib.invai.api.persistence.model.ApplicationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Native Spring Data JPA repository layer providing entity lifecycle updates, custom queries,
 * and constraint validations for {@link ApplicationEntity}.
 * Extends {@link JpaSpecificationExecutor} to enable complex programmatic Criteria inquiries.
 *
 * @since 1.0.0
 */
@Repository
public interface ApplicationJPARepository extends JpaRepository<ApplicationEntity, Long>, JpaSpecificationExecutor<ApplicationEntity> {

    /**
     * Resolves a raw data record matching the database primary key regardless of its current state lifecycle.
     *
     * @param id primary key sequence tracker mapping the target entity
     * @return an {@link Optional} container wrapping the persistent representation map if located
     */
    Optional<ApplicationEntity> findById(Long id);

    /**
     * Resolves an application primary entity row data configuration only if it has not been flagged
     * as logically soft-deleted.
     *
     * @param id primary key sequence tracker mapping the target entity
     * @return an {@link Optional} containing the matched active persistent record, or empty if missing
     */
    @Query("SELECT a FROM ApplicationEntity a WHERE a.id = :id AND a.deletedAt IS NULL")
    Optional<ApplicationEntity> findByIdAndDeletedAtIsNull(@Param("id") Long id);

    /**
     * Determines whether an active application tracking code constraint violation occurs.
     *
     * @param code target identification string to evaluate
     * @return {@code true} if a match exists, {@code false} otherwise
     */
    boolean existsByCodeAndDeletedAtIsNull(String code);

    /**
     * Determines whether an active system prefix registration identifier string is already in use.
     *
     * @param prefix target acronym prefix string to evaluate
     * @return {@code true} if a match exists, {@code false} otherwise
     */
    boolean existsByPrefixAndDeletedAtIsNull(String prefix);

    /**
     * Verifies if alternative active entities conflict with a given application code, excluding a target ID.
     *
     * @param code target identification string to evaluate
     * @param id   the record reference key to exclude from evaluation
     * @return {@code true} if a naming conflict occurs, {@code false} otherwise
     */
    boolean existsByCodeAndIdNotAndDeletedAtIsNull(String code, Long id);

    /**
     * Verifies if alternative active entities conflict with a given system prefix, excluding a target ID.
     *
     * @param prefix target acronym prefix string to evaluate
     * @param id     the record reference key to exclude from evaluation
     * @return {@code true} if a conflict occurs, {@code false} otherwise
     */
    boolean existsByPrefixAndIdNotAndDeletedAtIsNull(String prefix, Long id);

    /**
     * Verifies if any live operational database row fields are currently pointing towards
     * a targeted relational taxonomy classification key indicator.
     *
     * @param categoryId unique primary sequence identifier tracking the taxonomy item
     * @return {@code true} if operational conflicts exist due to active dependencies, {@code false} otherwise
     */
    boolean existsByCategoryIdAndDeletedAtIsNull(Long categoryId);
}