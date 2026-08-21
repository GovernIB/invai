package es.caib.invai.back.service.facade.maintenance.responsible.person;

import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting Person catalog components.
 * Acts as the primary service boundary exposed to external API web controllers.
 *
 * @since 1.0.3
 */
public interface PersonService {

    /**
     * Retrieves a person by its unique internal identifier.
     *
     * @param id primary key tracking index mapping the person profile
     * @return the mapped presentation outbound DTO schema representation
     */
    PersonOutputDTO getById(Long id);

    /**
     * Retrieves the complete list of all persons matching pagination thresholds.
     *
     * @param filter   dynamic search criteria constraints
     * @param pageable pagination threshold constraints and structural sorting rules
     * @return a page wrapper grouping matching outbound data DTO schemas
     */
    Page<PersonOutputDTO> getAll(PersonCriteria filter, Pageable pageable);

    /**
     * Creates a new person record based on the input data.
     *
     * @param inputDTO The required data to register the person.
     * @return The created person along with its generated ID.
     */
    PersonOutputDTO create(PersonInputDTO inputDTO);

    /**
     * Updates the modifiable fields of an existing person identified by its ID.
     *
     * @param id       The unique identifier of the person to be modified.
     * @param inputDTO The new data to apply to the record.
     * @return The updated person snapshot details.
     */
    PersonOutputDTO update(Long id, PersonInputDTO inputDTO);

    /**
     * Performs a logical deletion of the record in the system by changing its status to inactive.
     *
     * @param id The unique identifier of the person to deactivate.
     */
    void delete(Long id);

    /**
     * Reactivates a logically soft-deleted person back to active state.
     *
     * @param id the target identifier mapping the person instance intended for reactivation
     * @return the reactivated domain representation mapped into a {@link PersonOutputDTO}
     */
    PersonOutputDTO reactivate(Long id);
}
