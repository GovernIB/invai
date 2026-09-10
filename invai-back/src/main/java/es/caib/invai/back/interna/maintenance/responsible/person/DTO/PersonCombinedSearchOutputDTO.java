package es.caib.invai.back.interna.maintenance.responsible.person.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

/**
 * Outbound payload combining local {@code Person} catalog results with Soffid SCIM results for the
 * same request, each with its own independent pagination. Both sides are paginated with the same
 * page number/size, sorted alphabetically. When a search/filter term is given, {@code soffid} is
 * only populated (non-empty {@code Page}, still queried with the same paging) once {@code database}
 * comes back with no matches; when no term is given, both sides are always queried.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonCombinedSearchOutputDTO {

    /** Page of matching persons already present in the local {@code Person} catalog. */
    private Page<PersonOutputDTO> database;

    /** Page of matching Soffid candidates, not yet cached locally. */
    private Page<PersonOutputDTO> soffid;
}
