package es.caib.invai.back.persistence.repository.application.documentation.attachment;

import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentEntity;
import es.caib.invai.back.service.mapper.application.documentation.attachment.AppDocumentationAttachmentMapper;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachment;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the listing of {@link AppDocumentationAttachmentRepositoryAdapter}: it only goes through
 * its own JPA repository, and the sort is left to the specification instead of the pageable.
 */
@ExtendWith(MockitoExtension.class)
class AppDocumentationAttachmentRepositoryAdapterTest {

    @Mock
    private EntityManager entityManager;
    @Mock
    private AppDocumentationAttachmentJPARepository appDocumentationAttachmentJPARepository;
    @Mock
    private AppDocumentationAttachmentAudJPARepository appDocumentationAttachmentAudJPARepository;
    @Mock
    private AppDocumentationAttachmentMapper appDocumentationAttachmentMapper;

    @InjectMocks
    private AppDocumentationAttachmentRepositoryAdapter adapter;

    private static AppDocumentationAttachment model(Long id) {
        AppDocumentationAttachment model = new AppDocumentationAttachment();
        model.setId(id);
        return model;
    }

    private void stubPage(Long... ids) {
        List<AppDocumentationAttachmentEntity> entities = new ArrayList<>();
        for (Long id : ids) {
            AppDocumentationAttachmentEntity entity = new AppDocumentationAttachmentEntity();
            entity.setId(id);
            entities.add(entity);
            when(appDocumentationAttachmentMapper.toModel(entity)).thenReturn(model(id));
        }
        when(appDocumentationAttachmentJPARepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(entities));
    }

    @Test
    void findAll_mapsEveryDocumentOfThePage() {
        stubPage(10L, 11L);

        Page<AppDocumentationAttachment> result = adapter.findAll(new AppDocumentationAttachmentCriteria(), PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertEquals(10L, result.getContent().get(0).getId());
        assertEquals(11L, result.getContent().get(1).getId());
    }

    @Test
    void findAll_runsTheQueryUnsorted_becauseTheSpecificationAppliesTheSort() {
        stubPage(10L);

        adapter.findAll(new AppDocumentationAttachmentCriteria(), PageRequest.of(2, 5, Sort.by("versionLabel").descending()));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(appDocumentationAttachmentJPARepository).findAll(any(Specification.class), captor.capture());
        assertEquals(2, captor.getValue().getPageNumber());
        assertEquals(5, captor.getValue().getPageSize());
        assertTrue(captor.getValue().getSort().isUnsorted());
    }
}
