package es.caib.invai.back.ejb.maintenance.systems.databaseVendor;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.systems.databaseVendor.DTO.DatabaseVendorInputDTO;
import es.caib.invai.back.interna.maintenance.systems.databaseVendor.DTO.DatabaseVendorOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.databaseVendor.DatabaseVendorCriteria;
import es.caib.invai.back.persistence.repository.maintenance.systems.databaseVendor.DatabaseVendorRepository;
import es.caib.invai.back.service.mapper.maintenance.systems.databaseVendor.DatabaseVendorMapper;
import es.caib.invai.back.service.model.maintenance.systems.databaseVendor.DatabaseVendor;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DatabaseVendorServiceFacadeBean}, exercising every branch of its business
 * rules with the {@link DatabaseVendorRepository} and {@link DatabaseVendorMapper} collaborators
 * fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class DatabaseVendorServiceFacadeBeanTest {

    @Mock
    private DatabaseVendorMapper databaseVendorMapper;

    @Mock
    private DatabaseVendorRepository databaseVendorRepository;

    @InjectMocks
    private DatabaseVendorServiceFacadeBean databaseVendorServiceFacadeBean;

    private DatabaseVendor activeDatabaseVendor;

    @BeforeEach
    void setUp() {
        activeDatabaseVendor = new DatabaseVendor();
        activeDatabaseVendor.setId(1L);
        activeDatabaseVendor.setName("Oracle");
        activeDatabaseVendor.setDefaultPort(1521);
    }

    private DatabaseVendorInputDTO inputDTO(String name, Integer defaultPort) {
        DatabaseVendorInputDTO dto = new DatabaseVendorInputDTO();
        dto.setName(name);
        dto.setDefaultPort(defaultPort);
        return dto;
    }

    @Test
    void getById_found_returnsMappedResponse() {
        DatabaseVendorOutputDTO expected = new DatabaseVendorOutputDTO();
        when(databaseVendorRepository.findById(1L)).thenReturn(activeDatabaseVendor);
        when(databaseVendorMapper.toResponse(activeDatabaseVendor)).thenReturn(expected);

        DatabaseVendorOutputDTO result = databaseVendorServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(databaseVendorRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> databaseVendorServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_DATABASEVENDOR_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        DatabaseVendorCriteria criteria = new DatabaseVendorCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<DatabaseVendor> domainPage = new PageImpl<>(List.of(activeDatabaseVendor));
        DatabaseVendorOutputDTO mapped = new DatabaseVendorOutputDTO();
        when(databaseVendorRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(databaseVendorMapper.toResponse(activeDatabaseVendor)).thenReturn(mapped);

        Page<DatabaseVendorOutputDTO> result = databaseVendorServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueName_persistsAndReturnsResponse() {
        DatabaseVendorInputDTO input = inputDTO("PostgreSQL", 5432);
        DatabaseVendor model = new DatabaseVendor();
        DatabaseVendor saved = new DatabaseVendor();
        DatabaseVendorOutputDTO response = new DatabaseVendorOutputDTO();
        when(databaseVendorRepository.existsByName("PostgreSQL")).thenReturn(false);
        when(databaseVendorMapper.toModelFromInput(input)).thenReturn(model);
        when(databaseVendorRepository.create(model)).thenReturn(saved);
        when(databaseVendorMapper.toResponse(saved)).thenReturn(response);

        DatabaseVendorOutputDTO result = databaseVendorServiceFacadeBean.create(input);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        DatabaseVendorInputDTO input = inputDTO("Oracle", 1521);
        when(databaseVendorRepository.existsByName("Oracle")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> databaseVendorServiceFacadeBean.create(input));
        assertEquals(Constants.ERR_DATABASEVENDOR_DUPLICATED, ex.getMessage());
        verify(databaseVendorRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        DatabaseVendorInputDTO input = inputDTO("X", 1234);
        when(databaseVendorRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> databaseVendorServiceFacadeBean.update(99L, input));
        assertEquals(Constants.ERR_DATABASEVENDOR_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        DatabaseVendorInputDTO input = inputDTO("Taken", 1521);
        when(databaseVendorRepository.findById(1L)).thenReturn(activeDatabaseVendor);
        when(databaseVendorRepository.existsByNameAndIdNot("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> databaseVendorServiceFacadeBean.update(1L, input));
        assertEquals(Constants.ERR_DATABASEVENDOR_OWNED_BY_OTHER, ex.getMessage());
        verify(databaseVendorMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        DatabaseVendorInputDTO input = inputDTO("Updated", 3306);
        DatabaseVendor updated = new DatabaseVendor();
        DatabaseVendorOutputDTO response = new DatabaseVendorOutputDTO();
        when(databaseVendorRepository.findById(1L)).thenReturn(activeDatabaseVendor);
        when(databaseVendorRepository.existsByNameAndIdNot("Updated", 1L)).thenReturn(false);
        when(databaseVendorRepository.update(activeDatabaseVendor, 1L)).thenReturn(updated);
        when(databaseVendorMapper.toResponse(updated)).thenReturn(response);

        DatabaseVendorOutputDTO result = databaseVendorServiceFacadeBean.update(1L, input);

        verify(databaseVendorMapper).updateModelFromInput(input, activeDatabaseVendor);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(databaseVendorRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> databaseVendorServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_DATABASEVENDOR_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeDatabaseVendor.setDeletedAt(LocalDateTime.now());
        when(databaseVendorRepository.findById(1L)).thenReturn(activeDatabaseVendor);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> databaseVendorServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_DATABASEVENDOR_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesDatabaseVendor() {
        when(databaseVendorRepository.findById(1L)).thenReturn(activeDatabaseVendor);

        databaseVendorServiceFacadeBean.delete(1L);

        assertNotNull(activeDatabaseVendor.getDeletedAt());
        verify(databaseVendorRepository, times(1)).delete(activeDatabaseVendor);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(databaseVendorRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> databaseVendorServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_DATABASEVENDOR_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(databaseVendorRepository.findById(1L)).thenReturn(activeDatabaseVendor);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> databaseVendorServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_DATABASEVENDOR_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesDatabaseVendor() {
        activeDatabaseVendor.setDeletedAt(LocalDateTime.now());
        activeDatabaseVendor.setDeletedBy("someone");
        DatabaseVendor reactivated = new DatabaseVendor();
        DatabaseVendorOutputDTO response = new DatabaseVendorOutputDTO();
        when(databaseVendorRepository.findById(1L)).thenReturn(activeDatabaseVendor);
        when(databaseVendorRepository.update(eq(activeDatabaseVendor), eq(1L))).thenReturn(reactivated);
        when(databaseVendorMapper.toResponse(reactivated)).thenReturn(response);

        DatabaseVendorOutputDTO result = databaseVendorServiceFacadeBean.reactivate(1L);

        assertNull(activeDatabaseVendor.getDeletedAt());
        assertNull(activeDatabaseVendor.getDeletedBy());
        assertEquals(response, result);
    }
}
