package es.caib.invai.back.persistence.model.application.security.role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToOne;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the mapping that lets an application role be saved together with the row holding the data of
 * its Soffid role: without the cascade, saving a new assignment fails because its role row is transient.
 */
class AppRoleEntityTest {

    @Test
    void securityRole_isSavedAndMergedTogetherWithTheAssignment() throws NoSuchFieldException {
        ManyToOne mapping = AppRoleEntity.class.getDeclaredField("securityRole").getAnnotation(ManyToOne.class);

        assertTrue(Arrays.asList(mapping.cascade()).containsAll(
                Arrays.asList(CascadeType.PERSIST, CascadeType.MERGE)));
    }
}
