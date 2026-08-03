package es.caib.invai.api.persistence.repository.application.core;

import es.caib.invai.api.service.model.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Outbound Port boundary interface declaring relational persistence mechanisms
 * for the application domain model layer.
 * Enforces architectural boundary isolation patterns by dealing exclusively with the core {@link Application} domain structure.
 *
 * @since 1.0.1
 */
public interface ApplicationRepository {

    /**
     * Registers a new operational system profile structure inside structural relational systems.
     *
     * @param application transient domain configuration mapping target variables properties
     * @return persistent model profile containing persistent state data markers
     */
    Application create(Application application);

    /**
     * Commits changes onto active domain records maps identified by a target primary sequence index.
     *
     * @param application business schema properties map detailing updates structures parameters
     * @param id          primary persistent key reference index tracking database entries rows
     * @return updated model domain properties specifications variables
     */
    Application update(Application application, Long id);

    /**
     * Flags tracking profiles context records as soft-deleted inside persistence layers.
     *
     * @param application target domain representation configuration modeling details to deactivate
     */
    void delete(Application application);

    /**
     * Resolves an active application record profile matching an identification primary index.
     *
     * @param id unique database sequence row identifier tracking the asset
     * @return mapped core business model context layer data structures representation
     * @throws IllegalArgumentException if the record is missing or soft-deleted
     */
    Application findById(Long id);

    /**
     * Evaluates complex multi-dimensional search criteria parameters using custom programmatic matrices.
     *
     * @param criteria searching constraints and properties filtering flags parameters wrapper
     * @param pageable sorting parameters and page segment definitions thresholds
     * @return paginated container summarizing valid domain entries maps
     */
    Page<Application> findAll(ApplicationCriteria criteria, Pageable pageable);

    /**
     * Confirms code uniqueness across active application registries.
     *
     * @param code core identifier value token
     * @return {@code true} if conflicts exist, {@code false} otherwise
     */
    boolean existsByCode(String code);

    /**
     * Confirms system prefix uniqueness across active application registries.
     *
     * @param prefix application short acronym prefix text
     * @return {@code true} if conflicts exist, {@code false} otherwise
     */
    boolean existsByPrefix(String prefix);

    /**
     * Checks for corporate code usage across alternative active profile entries.
     *
     * @param code core identifier value token
     * @param id   database sequence primary reference row token to exclude from tracking
     * @return {@code true} if a code duplicate occurs outside the index parameter, {@code false} otherwise
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * Checks for short prefix usage across alternative active profile entries.
     *
     * @param prefix application short acronym prefix text
     * @param id     database sequence primary reference row token to exclude from tracking
     * @return {@code true} if a prefix duplicate occurs outside the index parameter, {@code false} otherwise
     */
    boolean existsByPrefixAndIdNot(String prefix, Long id);

    /**
     * Verifies whether any active application instance within the system remains associated
     * with a specific taxonomy category sequence index.
     *
     * @param categoryId unique primary reference identifier tracking the taxonomy item
     * @return {@code true} if operational conflicts exist due to active dependencies, {@code false} otherwise
     */
    boolean existsByCategoryId(Long categoryId);

    /**
     * Verifies whether any active application instance within the system remains associated
     * with a specific taxonomy system type sequence index.
     *
     * @param systemTypeId unique primary reference identifier tracking the system architecture item
     * @return {@code true} if operational conflicts exist due to active dependencies, {@code false} otherwise
     */
    boolean existsBySystemTypeId(Long systemTypeId);

    /**
     * Verifies whether any active application instance within the system remains associated
     * with a specific taxonomy field/sector sequence index.
     *
     * @param fieldId unique primary reference identifier tracking the sector taxonomy item
     * @return {@code true} if operational conflicts exist due to active dependencies, {@code false} otherwise
     */
    boolean existsByFieldId(Long fieldId);

    /**
     * Verifies whether any active application instance within the system remains associated
     * with a specific administrative unit sequence index.
     *
     * @param admUnitId unique primary reference identifier tracking the organizational structure item
     * @return {@code true} if operational conflicts exist due to active dependencies, {@code false} otherwise
     */
    boolean existsByAdmUnitId(Long admUnitId);

    /**
     * Verifies whether any active application instance within the system remains associated
     * with a specific context evaluation commission sequence index.
     *
     * @param commissionId unique primary reference identifier tracking the technical commission item
     * @return {@code true} if operational conflicts exist due to active dependencies, {@code false} otherwise
     */
    boolean existsByCommissionId(Long commissionId);
}