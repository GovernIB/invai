package es.caib.invai.back.service.model.application.dir3;

import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonDir3CheckOutputDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;

/**
 * Picture of the DIR3CAIB unit of a set of people, taken from Soffid in a handful of bulk calls so
 * that checking each of them against an application's administrative unit is a lookup in memory,
 * not two calls to Soffid per person. It holds, as they were when it was taken, each person's primary
 * Soffid group (by e-mail) and the DIR3CAIB code of every group having one. Immutable, so it can be
 * shared by every application checked in a run; it is never kept beyond that run.
 *
 * @since 1.0.6
 */
public class PrimaryGroupDir3 {

    /** Primary Soffid group of each person found, by lower-cased e-mail; the group is {@code null} if the user has none. */
    private final Map<String, String> primaryGroupByEmail;

    /** DIR3CAIB code (trimmed) of every Soffid group having one, by group name. */
    private final Map<String, String> dir3ByGroup;

    /**
     * @param primaryGroupByEmail primary Soffid group of each person found, by lower-cased e-mail
     * @param dir3ByGroup         DIR3CAIB code of every Soffid group having one, by group name
     */
    public PrimaryGroupDir3(Map<String, String> primaryGroupByEmail, Map<String, String> dir3ByGroup) {
        this.primaryGroupByEmail = Map.copyOf(primaryGroupByEmail);
        this.dir3ByGroup = Map.copyOf(dir3ByGroup);
    }

    /**
     * Checks a person against an administrative unit from the picture, with the same result as
     * asking Soffid about them right now: no match (and no group or DIR3) if the person was not
     * found, has no primary group, or that group has no DIR3; otherwise a match only when the
     * group's DIR3CAIB code is exactly the unit's (ignoring case and surrounding spaces).
     *
     * @param emailAddress the person's e-mail address
     * @param admUnitCode  the DIR3CAIB code to check against (an application's administrative unit)
     * @return the check result, with {@code groupDir3 == null} when it could not be determined
     */
    public PersonDir3CheckOutputDTO check(String emailAddress, String admUnitCode) {
        if (StringUtils.isBlank(emailAddress) || StringUtils.isBlank(admUnitCode)) {
            return new PersonDir3CheckOutputDTO(null, null, admUnitCode, false);
        }
        String primaryGroup = primaryGroupByEmail.get(emailAddress.trim().toLowerCase());
        String groupDir3 = Objects.nonNull(primaryGroup) ? dir3ByGroup.get(primaryGroup) : null;
        boolean matches = Objects.nonNull(groupDir3) && groupDir3.equalsIgnoreCase(admUnitCode.trim());
        return new PersonDir3CheckOutputDTO(primaryGroup, groupDir3, admUnitCode, matches);
    }
}
