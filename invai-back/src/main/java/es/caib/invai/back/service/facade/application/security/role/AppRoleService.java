package es.caib.invai.back.service.facade.application.security.role;

import es.caib.invai.back.rest.soffid.SoffidRole;
import es.caib.invai.back.interna.application.security.role.DTO.AppRoleInputDTO;
import es.caib.invai.back.interna.application.security.role.DTO.AppRoleOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.role.AppRoleCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Domain Boundary Outbound Port interfacing the internal transactional domain operations.
 *
 * @since 1.0.4
 */
public interface AppRoleService {

    /**
     * Retrieves a paginated sequence of role assignments scoped to a single parent security anchor.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      the multi-parameter business query filter boundaries
     * @param pageable      pagination structural constraints
     * @return a paginated payload containing corresponding transfer representations
     */
    Page<AppRoleOutputDTO> getAll(Long appSecurityId, AppRoleCriteria criteria, Pageable pageable);

    /**
     * Creates a new application-role assignment from the given input payload. Its validation state is
     * resolved against the application's prefix; being added by hand, a role that doesn't match it
     * is saved already {@code MANUAL}.
     *
     * @param inputDTO the creation payload
     * @return the created assignment, mapped to its output transfer representation
     * @throws es.caib.invai.back.exception.BusinessRuleException if Soffid has no role with the given Soffid id
     */
    AppRoleOutputDTO create(AppRoleInputDTO inputDTO);

    /**
     * Assigns a Soffid role the caller has already fetched from Soffid to a security anchor, with no
     * further call to Soffid: the role's data is stored, with the assignment, in a new security role
     * row. Used when many roles are assigned at once, such as all those Soffid has for an application.
     * Its validation state is resolved against the application's prefix.
     *
     * @param appSecurityId the security anchor the role is assigned to
     * @param soffidRole    the role as Soffid reported it
     * @return the created assignment, mapped to its output transfer representation
     * @since 1.0.6
     */
    AppRoleOutputDTO assign(Long appSecurityId, SoffidRole soffidRole);

    /**
     * Assigns to a security anchor the roles Soffid has for an application: those whose name starts
     * with the application's prefix followed by an underscore (case-sensitive, so prefix {@code AP2}
     * matches {@code AP2_ADMIN} but not {@code ap2_admin} nor {@code XAP2_ADMIN}). Each role's data -
     * as Soffid reports it now - is stored in {@code INV_SECURITY_ROLE}. It is idempotent: a role the
     * anchor already has an active assignment to (matched by its Soffid id) is skipped, so calling it
     * again only adds the roles Soffid has gained since. Roles are never removed. The roles are all
     * read from Soffid before any is assigned, so a Soffid failure leaves the anchor untouched.
     *
     * @param appSecurityId the security anchor the roles are assigned to
     * @param prefix        the application's prefix naming the roles to assign
     * @return the assignments created by this call, empty if there was nothing new to assign
     * @throws es.caib.invai.back.exception.BusinessRuleException   if Soffid can't be queried
     * @throws es.caib.invai.back.exception.SoffidTimeoutException if Soffid takes too long to answer
     * @since 1.0.6
     */
    List<AppRoleOutputDTO> assignByPrefix(Long appSecurityId, String prefix);

    /**
     * Updates the application-role assignment identified by {@code id} with the given
     * input payload.
     *
     * @param id       identifier of the assignment to update
     * @param inputDTO the update payload
     * @return the updated assignment, mapped to its output transfer representation
     */
    AppRoleOutputDTO update(Long id, AppRoleInputDTO inputDTO);

    /**
     * Validates manually the application-role assignment identified by {@code id}: its state goes
     * from {@code NOT_VALIDATED} to {@code MANUAL}, stamping the moment and the acting user. It only
     * changes the state. One-way: any other state is refused.
     *
     * @param id identifier of the assignment to validate
     * @throws es.caib.invai.back.exception.BusinessRuleException if the assignment doesn't exist, is
     *                                                            inactive, or isn't {@code NOT_VALIDATED}
     * @since 1.0.6
     */
    void validate(Long id);

    /**
     * Resolves again the validation state of every active assignment of a security anchor against the
     * application's (new) prefix, typically after the prefix changed: {@code VALIDATED} if the role's
     * name starts with the prefix followed by an underscore, {@code NOT_VALIDATED} if not. Roles
     * already {@code MANUAL} are left as they are: a person has already vouched for them. Reads
     * nothing from Soffid and assigns nothing new.
     *
     * @param appSecurityId the security anchor whose assignments are recalculated
     * @param prefix        the application's prefix
     * @since 1.0.6
     */
    void recalculateValidation(Long appSecurityId, String prefix);

    /**
     * Deletes (logically) the application-role assignment identified by {@code id}.
     *
     * @param id identifier of the assignment to delete
     */
    void delete(Long id);
}
