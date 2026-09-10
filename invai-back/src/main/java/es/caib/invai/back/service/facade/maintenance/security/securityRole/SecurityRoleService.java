package es.caib.invai.back.service.facade.maintenance.security.securityRole;

import es.caib.invai.back.interna.maintenance.security.securityRole.DTO.SecurityRoleOutputDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting Security Roles ({@code SecurityRole}).
 * Acts as the primary application service boundary exposed to external API web controllers.
 * <p>
 * Currently limited to searching/listing candidates against Soffid: local CRUD is not exposed
 * yet, pending a future iteration that adds the resolve-or-create flow feeding
 * {@code INV_APP_ROLE} assignments.
 * </p>
 *
 * @since 1.0.4
 */
public interface SecurityRoleService {

    /**
     * Lists roles that can be assigned to an application (feeding {@code INV_APP_ROLE}) against
     * the Soffid SCIM API, to feed the security role typeahead. Does not touch the local
     * {@code SecurityRole} catalog: results are unpersisted Soffid candidates, mapped into
     * {@link SecurityRoleOutputDTO} with {@code id} left {@code null} (no local row exists yet) -
     * the same "null id means not yet cached locally" convention already used elsewhere in this
     * codebase for external-source search results (see {@code Person.searchSoffid}).
     *
     * @param name the text to search for, matched (word by word) against the role's name, or
     * {@code null}/blank to list every Soffid role in this project's namespace (name starting
     * with {@code "INV_"})
     * @param pageable the pagination parameters
     * @return the requested page of matching Soffid role candidates, mapped into
     * {@link SecurityRoleOutputDTO}
     */
    Page<SecurityRoleOutputDTO> searchSoffid(String name, Pageable pageable);
}
