package es.caib.invai.back.persistence.model;

import es.caib.invai.back.config.BackgroundJobAuthentication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for the audit fields {@link BaseEntity} fills in before saving: they must also work
 * when the actor is not an OpenID Connect user, as in the background DIR3 audit, whose thread runs
 * under a plain username.
 */
class BaseEntityTest {

    /** Minimal concrete entity to exercise the mapped superclass. */
    private static class TestEntity extends BaseEntity {
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void onUpdate_backgroundJob_recordsTheLaunchingUsername() {
        SecurityContextHolder.getContext().setAuthentication(new BackgroundJobAuthentication("u98441",
                new UsernamePasswordAuthenticationToken("u98441", null, List.of())));
        TestEntity entity = new TestEntity();

        entity.onUpdate();

        assertEquals("u98441", entity.getUpdatedBy());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    void onCreate_backgroundJob_recordsTheLaunchingUsername() {
        SecurityContextHolder.getContext().setAuthentication(new BackgroundJobAuthentication("u98441",
                new UsernamePasswordAuthenticationToken("u98441", null, List.of())));
        TestEntity entity = new TestEntity();

        entity.onCreate();

        assertEquals("u98441", entity.getCreatedBy());
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    void onUpdate_noAuthentication_recordsTheSystemUserInsteadOfFailing() {
        TestEntity entity = new TestEntity();

        entity.onUpdate();

        assertEquals("SYSTEM_USER", entity.getUpdatedBy());
    }

    @Test
    void onUpdate_softDeletedEntity_leavesTheUpdateFieldsUntouched() {
        SecurityContextHolder.getContext().setAuthentication(new BackgroundJobAuthentication("u98441",
                new UsernamePasswordAuthenticationToken("u98441", null, List.of())));
        TestEntity entity = new TestEntity();
        entity.setDeletedAt(java.time.LocalDateTime.now());

        entity.onUpdate();

        assertEquals(null, entity.getUpdatedBy());
    }
}
