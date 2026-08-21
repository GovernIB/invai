package es.caib.invai.back.service.mapper.application.responsibleAuthorized.type;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.type.AppAuthorizedTypeLinkEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.authorized.AppAuthorizedEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.authorizationType.AuthorizationTypeEntity;
import es.caib.invai.back.service.model.application.responsibleAuthorized.type.AppAuthorizedTypeLink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link AppAuthorizedTypeLinkMapperImpl}, exercising every
 * conversion direction declared on {@link AppAuthorizedTypeLinkMapper}, including the nested
 * {@code appAuthorized.id}/{@code authorizationType.id} flattening in both directions.
 */
class AppAuthorizedTypeLinkMapperTest {

    private AppAuthorizedTypeLinkMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AppAuthorizedTypeLinkMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        AppAuthorizedEntity appAuthorized = new AppAuthorizedEntity();
        appAuthorized.setId(10L);
        AuthorizationTypeEntity authorizationType = new AuthorizationTypeEntity();
        authorizationType.setId(20L);

        AppAuthorizedTypeLinkEntity entity = new AppAuthorizedTypeLinkEntity();
        entity.setId(1L);
        entity.setAppAuthorized(appAuthorized);
        entity.setAuthorizationType(authorizationType);

        AppAuthorizedTypeLink model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals(10L, model.getAppAuthorizedId());
        assertEquals(20L, model.getAuthorizationTypeId());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toModel_missingNestedAssociations_leavesFlattenedIdsNull() {
        AppAuthorizedTypeLinkEntity entity = new AppAuthorizedTypeLinkEntity();
        entity.setId(2L);

        AppAuthorizedTypeLink model = mapper.toModel(entity);

        assertEquals(2L, model.getId());
        assertNull(model.getAppAuthorizedId());
        assertNull(model.getAuthorizationTypeId());
    }

    @Test
    void toEntity_mapsEveryModelField() {
        AppAuthorizedTypeLink model = AppAuthorizedTypeLink.builder()
                .id(3L)
                .appAuthorizedId(30L)
                .authorizationTypeId(40L)
                .build();

        AppAuthorizedTypeLinkEntity entity = mapper.toEntity(model);

        assertEquals(3L, entity.getId());
        assertEquals(30L, entity.getAppAuthorized().getId());
        assertEquals(40L, entity.getAuthorizationType().getId());
    }

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toEntity_missingFlattenedIds_buildsNestedAssociationsWithNullId() {
        AppAuthorizedTypeLink model = AppAuthorizedTypeLink.builder().id(4L).build();

        AppAuthorizedTypeLinkEntity entity = mapper.toEntity(model);

        assertEquals(4L, entity.getId());
        assertNull(entity.getAppAuthorized().getId());
        assertNull(entity.getAuthorizationType().getId());
    }
}
