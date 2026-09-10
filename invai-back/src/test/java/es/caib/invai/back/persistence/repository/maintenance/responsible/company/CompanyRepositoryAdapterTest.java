package es.caib.invai.back.persistence.repository.maintenance.responsible.company;

import es.caib.invai.back.persistence.model.maintenance.responsible.company.CompanyAudEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.company.CompanyEntity;
import es.caib.invai.back.service.mapper.maintenance.responsible.company.CompanyMapper;
import es.caib.invai.back.service.model.maintenance.responsible.company.Company;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentMatchers;

/**
 * Unit tests for {@link CompanyRepositoryAdapter}, verifying entity/model delegation to the
 * {@link CompanyJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class CompanyRepositoryAdapterTest {

    @Mock
    private CompanyJPARepository companyJPARepository;

    @Mock
    private CompanyAudJPARepository companyAudJPARepository;

    @Mock
    private CompanyMapper companyMapper;

    private CompanyRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private CompanyRepositoryAdapter buildAdapter() {
        CompanyRepositoryAdapter a = new CompanyRepositoryAdapter();
        ReflectionTestUtils.setField(a, "companyJPARepository", companyJPARepository);
        ReflectionTestUtils.setField(a, "companyAudJPARepository", companyAudJPARepository);
        ReflectionTestUtils.setField(a, "companyMapper", companyMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        CompanyEntity entity = new CompanyEntity();
        entity.setId(1L);
        Company model = new Company();
        when(companyJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(companyMapper.toModel(entity)).thenReturn(model);

        Company result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(companyJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        CompanyCriteria criteria = new CompanyCriteria();
        Pageable pageable = Pageable.unpaged();
        CompanyEntity entity = new CompanyEntity();
        Company model = new Company();
        Page<CompanyEntity> entityPage = new PageImpl<>(List.of(entity));
        when(companyJPARepository.findAll(ArgumentMatchers.<Specification<CompanyEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(companyMapper.toModel(entity)).thenReturn(model);

        Page<Company> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByNameAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(companyJPARepository.existsByNameAndDeletedAtIsNull("Plexus")).thenReturn(true);

        assertTrue(adapter.existsByNameAndDeletedAtIsNull("Plexus"));
    }

    @Test
    void existsByNameAndIdNotAndDeletedAtIsNull_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(companyJPARepository.existsByNameAndIdNotAndDeletedAtIsNull("Plexus", 1L)).thenReturn(true);

        assertTrue(adapter.existsByNameAndIdNotAndDeletedAtIsNull("Plexus", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        Company model = new Company();
        CompanyEntity toSave = new CompanyEntity();
        CompanyEntity saved = new CompanyEntity();
        saved.setId(5L);
        saved.setName("Plexus Tech");
        Company response = new Company();
        when(companyMapper.toEntity(model)).thenReturn(toSave);
        when(companyJPARepository.save(toSave)).thenReturn(saved);
        when(companyMapper.toModel(saved)).thenReturn(response);

        Company result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<CompanyAudEntity> captor = ArgumentCaptor.forClass(CompanyAudEntity.class);
        verify(companyAudJPARepository).save(captor.capture());
        CompanyAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getCompanyId());
        assertEquals("Plexus Tech", aud.getName());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        Company model = new Company();
        CompanyEntity toSave = new CompanyEntity();
        CompanyEntity saved = new CompanyEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(companyMapper.toEntity(model)).thenReturn(toSave);
        when(companyJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<CompanyAudEntity> captor = ArgumentCaptor.forClass(CompanyAudEntity.class);
        verify(companyAudJPARepository).save(captor.capture());
        CompanyAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        Company model = new Company();
        CompanyEntity toSave = new CompanyEntity();
        CompanyEntity saved = new CompanyEntity();
        saved.setId(7L);
        Company response = new Company();
        when(companyMapper.toEntity(model)).thenReturn(toSave);
        when(companyJPARepository.save(toSave)).thenReturn(saved);
        when(companyMapper.toModel(saved)).thenReturn(response);

        Company result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<CompanyAudEntity> captor = ArgumentCaptor.forClass(CompanyAudEntity.class);
        verify(companyAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        Company model = new Company();
        model.setId(8L);
        CompanyEntity toSave = new CompanyEntity();
        CompanyEntity saved = new CompanyEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(companyMapper.toEntity(model)).thenReturn(toSave);
        when(companyJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<CompanyAudEntity> captor = ArgumentCaptor.forClass(CompanyAudEntity.class);
        verify(companyAudJPARepository).save(captor.capture());
        CompanyAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
