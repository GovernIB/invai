package es.caib.invai.back.service.model.application.dir3;

import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonDir3CheckOutputDTO;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link PrimaryGroupDir3}: checking a person from the picture must give the same
 * answer as asking Soffid about them would.
 */
class PrimaryGroupDir3Test {

    private final PrimaryGroupDir3 primaryGroupDir3 = new PrimaryGroupDir3(
            Map.of("ana@caib.es", "sgaip", "joan@caib.es", "dgtic", "pere@caib.es", "sensedir3"),
            Map.of("sgaip", "A04027054", "dgtic", "A04003003"));

    @Test
    void check_groupDir3EqualsTheUnit_matches() {
        PersonDir3CheckOutputDTO result = primaryGroupDir3.check("ana@caib.es", "A04027054");

        assertTrue(result.isMatches());
        assertEquals("sgaip", result.getPersonGroup());
        assertEquals("A04027054", result.getGroupDir3());
        assertEquals("A04027054", result.getAdmUnitCode());
    }

    @Test
    void check_ignoresCaseAndSurroundingSpacesOfTheUnitAndTheEmail() {
        assertTrue(primaryGroupDir3.check("  ANA@Caib.es ", " a04027054 ").isMatches());
    }

    @Test
    void check_groupDir3DiffersFromTheUnit_doesNotMatchButReportsTheGroupAndItsDir3() {
        PersonDir3CheckOutputDTO result = primaryGroupDir3.check("joan@caib.es", "A04027054");

        assertFalse(result.isMatches());
        assertEquals("dgtic", result.getPersonGroup());
        assertEquals("A04003003", result.getGroupDir3());
    }

    @Test
    void check_groupWithoutDir3_doesNotMatchAndHasNoDir3() {
        PersonDir3CheckOutputDTO result = primaryGroupDir3.check("pere@caib.es", "A04027054");

        assertFalse(result.isMatches());
        assertEquals("sensedir3", result.getPersonGroup());
        assertNull(result.getGroupDir3());
    }

    @Test
    void check_personNotInThePrimaryGroupDir3_doesNotMatchAndHasNoGroup() {
        PersonDir3CheckOutputDTO result = primaryGroupDir3.check("ausente@caib.es", "A04027054");

        assertFalse(result.isMatches());
        assertNull(result.getPersonGroup());
        assertNull(result.getGroupDir3());
    }

    @Test
    void check_blankEmailOrUnit_doesNotMatch() {
        assertFalse(primaryGroupDir3.check(" ", "A04027054").isMatches());
        assertFalse(primaryGroupDir3.check(null, "A04027054").isMatches());
        assertFalse(primaryGroupDir3.check("ana@caib.es", " ").isMatches());
        assertFalse(primaryGroupDir3.check("ana@caib.es", null).isMatches());
    }

    @Test
    void primaryGroupDir3_isAnIndependentCopyOfTheMapsItWasBuiltFrom() {
        java.util.HashMap<String, String> groups = new java.util.HashMap<>(Map.of("ana@caib.es", "sgaip"));
        java.util.HashMap<String, String> dir3 = new java.util.HashMap<>(Map.of("sgaip", "A04027054"));
        PrimaryGroupDir3 copy = new PrimaryGroupDir3(groups, dir3);

        groups.put("ana@caib.es", "otro");
        dir3.put("sgaip", "A00000000");

        assertTrue(copy.check("ana@caib.es", "A04027054").isMatches());
    }
}
