package es.caib.invai.back.service.mapper.maintenance.general.commission;

import es.caib.invai.back.interna.maintenance.general.commission.DTO.CommissionInputDTO;
import es.caib.invai.back.interna.maintenance.general.commission.DTO.CommissionOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.general.commission.CommissionEntity;
import es.caib.invai.back.service.model.maintenance.general.commission.Commission;
import es.caib.invai.back.service.model.maintenance.general.commission.CommissionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the generated {@link CommissionMapperImpl}, exercising every conversion direction
 * declared on {@link CommissionMapper}.
 */
class CommissionMapperTest {

    private CommissionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CommissionMapperImpl();
    }

    @Test
    void toModel_mapsEveryEntityField() {
        CommissionEntity entity = new CommissionEntity();
        entity.setId(1L);
        entity.setName("Comissio Tecnica");
        entity.setNameEs("Comision Tecnica");
        entity.setExpedientNumber("EXP-2026-001");
        entity.setCommissionType(CommissionType.TECNICA);
        entity.setApprovalDate(LocalDate.of(2026, 1, 15));

        Commission model = mapper.toModel(entity);

        assertEquals(1L, model.getId());
        assertEquals("Comissio Tecnica", model.getName());
        assertEquals("Comision Tecnica", model.getNameEs());
        assertEquals("EXP-2026-001", model.getExpedientNumber());
        assertEquals(CommissionType.TECNICA, model.getCommissionType());
        assertEquals(LocalDate.of(2026, 1, 15), model.getApprovalDate());
    }

    @Test
    void toModel_null_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    @Test
    void toEntity_mapsEveryModelField() {
        Commission model = new Commission();
        model.setId(2L);
        model.setName("Comissio Superior");
        model.setNameEs("Comision Superior");
        model.setExpedientNumber("EXP-2026-002");
        model.setCommissionType(CommissionType.SUPERIOR);
        model.setApprovalDate(LocalDate.of(2026, 3, 10));

        CommissionEntity entity = mapper.toEntity(model);

        assertEquals(2L, entity.getId());
        assertEquals("Comissio Superior", entity.getName());
        assertEquals("Comision Superior", entity.getNameEs());
        assertEquals("EXP-2026-002", entity.getExpedientNumber());
        assertEquals(CommissionType.SUPERIOR, entity.getCommissionType());
        assertEquals(LocalDate.of(2026, 3, 10), entity.getApprovalDate());
    }

    @Test
    void toResponse_flattensDomainModelIntoOutputDTO() {
        Commission model = new Commission();
        model.setId(3L);
        model.setName("Comissio Revisio");
        model.setNameEs("Comision Revision");
        model.setExpedientNumber("EXP-2026-003");
        model.setCommissionType(CommissionType.TECNICA);
        model.setApprovalDate(LocalDate.of(2026, 4, 20));

        CommissionOutputDTO response = mapper.toResponse(model);

        assertEquals(3L, response.getId());
        assertEquals("Comissio Revisio", response.getName());
        assertEquals("Comision Revision", response.getNameEs());
        assertEquals("EXP-2026-003", response.getExpedientNumber());
        assertEquals(CommissionType.TECNICA, response.getCommissionType());
        assertEquals(LocalDate.of(2026, 4, 20), response.getApprovalDate());
    }

    @Test
    void toModelFromInput_ignoresAuditFieldsAndId() {
        CommissionInputDTO inputDTO = new CommissionInputDTO();
        inputDTO.setName("Analytics Board");
        inputDTO.setNameEs("Consejo Analitico");
        inputDTO.setExpedientNumber("EXP-2026-004");
        inputDTO.setCommissionType(CommissionType.TECNICA);
        inputDTO.setApprovalDate(LocalDate.of(2026, 5, 1));

        Commission model = mapper.toModelFromInput(inputDTO);

        assertEquals("Analytics Board", model.getName());
        assertEquals("Consejo Analitico", model.getNameEs());
        assertEquals("EXP-2026-004", model.getExpedientNumber());
        assertEquals(CommissionType.TECNICA, model.getCommissionType());
        assertEquals(LocalDate.of(2026, 5, 1), model.getApprovalDate());
        assertNull(model.getId());
        assertNull(model.getCreatedAt());
        assertNull(model.getDeletedAt());
    }

    @Test
    void updateModelFromInput_mergesFieldsWithoutTouchingId() {
        Commission existing = new Commission();
        existing.setId(4L);
        existing.setName("Old Name");
        existing.setNameEs("Nombre Antiguo");
        existing.setExpedientNumber("EXP-OLD");
        existing.setCommissionType(CommissionType.TECNICA);
        existing.setApprovalDate(LocalDate.of(2025, 1, 1));

        CommissionInputDTO inputDTO = new CommissionInputDTO();
        inputDTO.setName("New Name");
        inputDTO.setNameEs("Nombre Nuevo");
        inputDTO.setExpedientNumber("EXP-NEW");
        inputDTO.setCommissionType(CommissionType.SUPERIOR);
        inputDTO.setApprovalDate(LocalDate.of(2026, 6, 15));

        mapper.updateModelFromInput(inputDTO, existing);

        assertEquals(4L, existing.getId());
        assertEquals("New Name", existing.getName());
        assertEquals("Nombre Nuevo", existing.getNameEs());
        assertEquals("EXP-NEW", existing.getExpedientNumber());
        assertEquals(CommissionType.SUPERIOR, existing.getCommissionType());
        assertEquals(LocalDate.of(2026, 6, 15), existing.getApprovalDate());
    }

    @Test
    void map_byId_buildsShallowReferenceStub() {
        CommissionOutputDTO stub = mapper.map(5L);

        assertEquals(5L, stub.getId());
    }

    @Test
    void map_nullId_returnsNull() {
        assertNull(mapper.map(null));
    }
}
