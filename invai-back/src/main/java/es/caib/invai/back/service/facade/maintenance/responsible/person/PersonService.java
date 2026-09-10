package es.caib.invai.back.service.facade.maintenance.responsible.person;

import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonCombinedSearchOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonCriteria;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
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

    /**
     * Lists CAIB personnel against the Soffid SCIM API, to feed both the Person "getAll" style
     * listing and the "assign a responsible/authorized person" typeahead. Does not touch the local
     * {@code Person} catalog: results are unpersisted Soffid candidates, mapped into
     * {@link PersonOutputDTO} with {@code id}, {@code company} and {@code deletedAt} left
     * {@code null} (no local row exists yet) and {@code personalCaib} set to {@code true} (Soffid
     * only surfaces CAIB staff) - the same "null id means not yet cached locally" convention
     * already used elsewhere in this codebase for external-source search results.
     *
     * @param fullName the text to search for, matched (word by word) against the full name, or
     * {@code null}/blank to list every active Soffid user
     * @param pageable the pagination parameters
     * @return the requested page of matching Soffid candidates, mapped into {@link PersonOutputDTO}
     */
    Page<PersonOutputDTO> searchSoffid(String fullName, Pageable pageable);

    /**
     * Lists/searches persons combining both the local {@code Person} catalog and Soffid, each
     * independently paginated with the same page number/size. When {@code search} is blank, both
     * sources are always queried in parallel. When {@code search} is given, the local catalog is
     * queried first; Soffid is only queried when the local search comes back empty, so a term
     * already found locally never triggers an external call, and a term found nowhere yields an
     * empty result on both sides.
     *
     * @param search free text matched against first name, last name and e-mail (locally) or full
     * name (Soffid), or blank/{@code null} to list both sources unfiltered
     * @param pageable the pagination parameters, applied identically to both sources
     * @return the local and Soffid pages for the given request
     */
    PersonCombinedSearchOutputDTO searchCombined(String search, Pageable pageable);

    /**
     * Resolves the person to link into an assignment (e.g. AppResponsible/AppAuthorized): reuses
     * an existing active person found by {@code email}, or creates a new one from the given
     * fields when none exists — the path used when the assignment targets someone with no local
     * {@code Person} row yet (e.g. CAIB staff sourced from Soffid). Callers are responsible for
     * validating that {@code firstName}/{@code lastName}/{@code email} are non-blank before
     * calling this method; only the company requirement is enforced here.
     *
     * @param firstName the person's first name, used only when creating a new row
     * @param lastName the person's last name, used only when creating a new row
     * @param email the person's e-mail, used to look up an existing active person and, when
     * creating, stored on the new row
     * @param personalCaib whether the person is CAIB personnel
     * @param companyId the company to link when {@code personalCaib} is {@code false}; ignored otherwise
     * @return the existing or newly created {@link Person}
     * @throws BusinessRuleException if {@code personalCaib} is {@code false} and {@code companyId} is {@code null}
     */
    Person resolveOrCreatePerson(String firstName, String lastName, String email, boolean personalCaib, Long companyId);

    /**
     * Keeps a linked person's {@code personalCaib} flag in sync with a value submitted alongside
     * an assignment (e.g. AppResponsible/AppAuthorized). A no-op when the value already matches
     * what is stored. Enforces the same business rule as {@link #create}/{@link #update}: a
     * person cannot be flagged as external (non-CAIB) without an associated company.
     *
     * @param personId the linked person's identifier
     * @param personalCaib the submitted CAIB flag value
     * @throws BusinessRuleException if setting the person to non-CAIB and it has no company
     */
    void syncPersonalCaib(Long personId, boolean personalCaib);
}
