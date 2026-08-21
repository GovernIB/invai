package es.caib.invai.back.persistence.repository.application.core;

import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Native Spring Data JPA repository layer providing entity lifecycle updates, custom queries,
 * and constraint validations for {@link ApplicationEntity}.
 * Extends {@link JpaSpecificationExecutor} to enable complex programmatic Criteria inquiries.
 *
 * @since 1.0.1
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
     * Determines whether an active application tracking code constraint violation occurs.
     *
     * @param code target identification string to evaluate
     * @return {@code true} if a match exists, {@code false} otherwise
     */
    boolean existsByCode(String code);

    /**
     * Determines whether an active system prefix registration identifier string is already in use.
     *
     * @param prefix target acronym prefix string to evaluate
     * @return {@code true} if a match exists, {@code false} otherwise
     */
    boolean existsByPrefix(String prefix);

    /**
     * Verifies if alternative active entities conflict with a given application code, excluding a target ID.
     *
     * @param code target identification string to evaluate
     * @param id   the record reference key to exclude from evaluation
     * @return {@code true} if a naming conflict occurs, {@code false} otherwise
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * Verifies if alternative active entities conflict with a given system prefix, excluding a target ID.
     *
     * @param prefix target acronym prefix string to evaluate
     * @param id     the record reference key to exclude from evaluation
     * @return {@code true} if a conflict occurs, {@code false} otherwise
     */
    boolean existsByPrefixAndIdNot(String prefix, Long id);

    /**
     * Verifies if any live operational database row fields are currently pointing towards
     * a targeted relational taxonomy classification key indicator.
     *
     * @param categoryId unique primary sequence identifier tracking the taxonomy item
     * @return {@code true} if operational conflicts exist due to active dependencies, {@code false} otherwise
     */
    boolean existsByCategoryIdAndDeletedAtIsNull(Long categoryId);

    /**
     * Verifies if any active database application instances are currently bound to a targeted system type configuration index.
     *
     * @param systemTypeId unique sequence identifier tracking the target architecture platform configuration profile
     * @return {@code true} if matching dependencies are detected, {@code false} otherwise
     */
    boolean existsBySystemTypeIdAndDeletedAtIsNull(Long systemTypeId);

    /**
     * Verifies if any active database application instances are currently bound to a targeted operational business field index.
     *
     * @param fieldId unique sequence identifier tracking the target operational context profile
     * @return {@code true} if matching dependencies are detected, {@code false} otherwise
     */
    boolean existsByFieldIdAndDeletedAtIsNull(Long fieldId);

    /**
     * Verifies if any active database application instances are currently bound to a targeted administrative unit reference index.
     *
     * @param admUnitId unique sequence identifier tracking the target organizational entity metadata profile
     * @return {@code true} if matching dependencies are detected, {@code false} otherwise
     */
    boolean existsByAdmUnitIdAndDeletedAtIsNull(Long admUnitId);

    /**
     * Verifies if any active database application instances are currently bound to a targeted technical commission index tracker.
     *
     * @param commissionId unique sequence identifier tracking the target structural regulatory profile
     * @return {@code true} if matching dependencies are detected, {@code false} otherwise
     */
    boolean existsByCsCommissionIdAndDeletedAtIsNull(Long commissionId);

    /**
     * Verifies if any active database application instances are currently bound to a targeted execution lifecycle status state.
     *
     * @param statusId unique sequence identifier tracking the target operational metric state profile
     * @return {@code true} if matching dependencies are detected, {@code false} otherwise
     */
    boolean existsByStatusIdAndDeletedAtIsNull(Long statusId);
}