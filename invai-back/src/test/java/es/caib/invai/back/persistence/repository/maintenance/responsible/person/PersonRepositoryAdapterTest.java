package es.caib.invai.back.persistence.repository.maintenance.responsible.person;

import es.caib.invai.back.persistence.model.maintenance.responsible.company.CompanyEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.person.PersonAudEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.person.PersonEntity;
import es.caib.invai.back.service.mapper.maintenance.responsible.person.PersonMapper;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
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
 * Unit tests for {@link PersonRepositoryAdapter}, verifying entity/model delegation to the
 * {@link PersonJPARepository} and the historical audit record written on every mutation.
 */
@ExtendWith(MockitoExtension.class)
class PersonRepositoryAdapterTest {

    @Mock
    private PersonJPARepository personJPARepository;

    @Mock
    private PersonAudJPARepository personAudJPARepository;

    @Mock
    private PersonMapper personMapper;

    private PersonRepositoryAdapter adapter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private PersonRepositoryAdapter buildAdapter() {
        PersonRepositoryAdapter a = new PersonRepositoryAdapter();
        ReflectionTestUtils.setField(a, "personJPARepository", personJPARepository);
        ReflectionTestUtils.setField(a, "personAudJPARepository", personAudJPARepository);
        ReflectionTestUtils.setField(a, "personMapper", personMapper);
        return a;
    }

    @Test
    void findById_found_returnsMappedModel() {
        adapter = buildAdapter();
        PersonEntity entity = new PersonEntity();
        entity.setId(1L);
        Person model = new Person();
        when(personJPARepository.findById(1L)).thenReturn(Optional.of(entity));
        when(personMapper.toModel(entity)).thenReturn(model);

        Person result = adapter.findById(1L);

        assertSame(model, result);
    }

    @Test
    void findById_notFound_returnsNull() {
        adapter = buildAdapter();
        when(personJPARepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(adapter.findById(99L));
    }

    @Test
    void findAll_delegatesToJPARepositoryAndMapsPage() {
        adapter = buildAdapter();
        PersonCriteria criteria = new PersonCriteria();
        Pageable pageable = Pageable.unpaged();
        PersonEntity entity = new PersonEntity();
        Person model = new Person();
        Page<PersonEntity> entityPage = new PageImpl<>(List.of(entity));
        when(personJPARepository.findAll(ArgumentMatchers.<Specification<PersonEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(personMapper.toModel(entity)).thenReturn(model);

        Page<Person> result = adapter.findAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(model, result.getContent().get(0));
    }

    @Test
    void existsByEmail_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(personJPARepository.existsByEmailAndDeletedAtIsNull("joan@example.com")).thenReturn(true);

        assertTrue(adapter.existsByEmail("joan@example.com"));
    }

    @Test
    void existsByEmailAndIdNot_delegatesToJPARepository() {
        adapter = buildAdapter();
        when(personJPARepository.existsByEmailAndIdNotAndDeletedAtIsNull("joan@example.com", 1L)).thenReturn(true);

        assertTrue(adapter.existsByEmailAndIdNot("joan@example.com", 1L));
    }

    @Test
    void create_savesEntityAndWritesInsertAuditRecord() {
        adapter = buildAdapter();
        Person model = new Person();
        PersonEntity toSave = new PersonEntity();
        PersonEntity saved = new PersonEntity();
        saved.setId(5L);
        saved.setFirstName("Joan");
        saved.setLastName("Puig");
        saved.setEmail("joan.puig@example.com");
        CompanyEntity company = new CompanyEntity();
        company.setId(10L);
        saved.setCompany(company);
        Person response = new Person();
        when(personMapper.toEntity(model)).thenReturn(toSave);
        when(personJPARepository.save(toSave)).thenReturn(saved);
        when(personMapper.toModel(saved)).thenReturn(response);

        Person result = adapter.create(model);

        assertSame(response, result);
        ArgumentCaptor<PersonAudEntity> captor = ArgumentCaptor.forClass(PersonAudEntity.class);
        verify(personAudJPARepository).save(captor.capture());
        PersonAudEntity aud = captor.getValue();
        assertEquals(5L, aud.getPersonId());
        assertEquals("Joan", aud.getFirstName());
        assertEquals("Puig", aud.getLastName());
        assertEquals("joan.puig@example.com", aud.getEmail());
        assertEquals(10L, aud.getCompanyId());
        assertEquals("INSERT", aud.getAudAction());
        assertNotNull(aud.getCreatedAt());
        assertEquals("SYSTEM_USER", aud.getCreatedBy());
        assertNotNull(aud.getAuditDate());
        assertEquals("SYSTEM_USER", aud.getAuditUser());
    }

    @Test
    void create_entityWithExistingAuditFields_preservesThemOnAuditRecord() {
        adapter = buildAdapter();
        Person model = new Person();
        PersonEntity toSave = new PersonEntity();
        PersonEntity saved = new PersonEntity();
        saved.setId(6L);
        LocalDateTime existingCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        saved.setCreatedAt(existingCreatedAt);
        saved.setCreatedBy("jdoe");
        when(personMapper.toEntity(model)).thenReturn(toSave);
        when(personJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<PersonAudEntity> captor = ArgumentCaptor.forClass(PersonAudEntity.class);
        verify(personAudJPARepository).save(captor.capture());
        PersonAudEntity aud = captor.getValue();
        assertEquals(existingCreatedAt, aud.getCreatedAt());
        assertEquals("jdoe", aud.getCreatedBy());
    }

    @Test
    void create_entityWithoutCompany_writesNullForeignKey() {
        adapter = buildAdapter();
        Person model = new Person();
        PersonEntity toSave = new PersonEntity();
        PersonEntity saved = new PersonEntity();
        saved.setId(9L);
        when(personMapper.toEntity(model)).thenReturn(toSave);
        when(personJPARepository.save(toSave)).thenReturn(saved);

        adapter.create(model);

        ArgumentCaptor<PersonAudEntity> captor = ArgumentCaptor.forClass(PersonAudEntity.class);
        verify(personAudJPARepository).save(captor.capture());
        PersonAudEntity aud = captor.getValue();
        assertNull(aud.getCompanyId());
    }

    @Test
    void update_setsIdAndWritesUpdateAuditRecord() {
        adapter = buildAdapter();
        Person model = new Person();
        PersonEntity toSave = new PersonEntity();
        PersonEntity saved = new PersonEntity();
        saved.setId(7L);
        Person response = new Person();
        when(personMapper.toEntity(model)).thenReturn(toSave);
        when(personJPARepository.save(toSave)).thenReturn(saved);
        when(personMapper.toModel(saved)).thenReturn(response);

        Person result = adapter.update(model, 7L);

        assertSame(response, result);
        assertEquals(7L, toSave.getId());
        ArgumentCaptor<PersonAudEntity> captor = ArgumentCaptor.forClass(PersonAudEntity.class);
        verify(personAudJPARepository).save(captor.capture());
        assertEquals("UPDATE", captor.getValue().getAudAction());
    }

    @Test
    void delete_setsIdAndWritesDeleteAuditRecord() {
        adapter = buildAdapter();
        Person model = new Person();
        model.setId(8L);
        PersonEntity toSave = new PersonEntity();
        PersonEntity saved = new PersonEntity();
        saved.setId(8L);
        saved.setDeletedAt(LocalDateTime.now());
        saved.setDeletedBy("jdoe");
        when(personMapper.toEntity(model)).thenReturn(toSave);
        when(personJPARepository.save(toSave)).thenReturn(saved);

        adapter.delete(model);

        assertEquals(8L, toSave.getId());
        ArgumentCaptor<PersonAudEntity> captor = ArgumentCaptor.forClass(PersonAudEntity.class);
        verify(personAudJPARepository).save(captor.capture());
        PersonAudEntity aud = captor.getValue();
        assertEquals("DELETE", aud.getAudAction());
        assertNotNull(aud.getDeletedAt());
        assertEquals("jdoe", aud.getDeletedBy());
    }
}
