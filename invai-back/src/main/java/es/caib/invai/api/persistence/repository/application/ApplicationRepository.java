package es.caib.invai.api.persistence.repository.application;

import es.caib.invai.api.service.model.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Outbound Port boundary interface declaring relational persistence mechanisms
 * for the application domain model layer.
 * Enforces architectural boundary isolation patterns by dealing exclusively with the core {@link Application} domain structure.
 *
 * @since 1.0.0
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
     * Verifies whether any active application record is currently bound to the specified category.
     *
     * @param categoryId unique primary sequence identifier tracking the category
     * @return {@code true} if at least one active application depends on this category, {@code false} otherwise
     */
    boolean existsByCategoryId(Long categoryId);
}