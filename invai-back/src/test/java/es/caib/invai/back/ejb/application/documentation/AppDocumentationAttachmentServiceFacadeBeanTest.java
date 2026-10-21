package es.caib.invai.back.ejb.application.documentation;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentVersionDownloadDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentInputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationMetadataInputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentOutputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentSimpleOutputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentUpdateInputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentVersionInputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.application.documentation.attachment.AppDocumentationAttachmentCriteria;
import es.caib.invai.back.persistence.repository.application.documentation.attachment.AppDocumentationAttachmentRepository;
import es.caib.invai.back.persistence.repository.application.documentation.attachment.AppDocumentationAttachmentVersionRepository;
import es.caib.invai.back.persistence.repository.application.documentation.attachment.AppDocumentationMetadataRepository;
import es.caib.invai.back.persistence.repository.catalog.appSection.AppSectionRepository;
import es.caib.invai.back.persistence.repository.catalog.documentStatus.DocumentStatusRepository;
import es.caib.invai.back.persistence.repository.catalog.mimeType.MimeTypeRepository;
import es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory.DocumentCategoryRepository;
import es.caib.invai.back.service.mapper.application.documentation.attachment.AppDocumentationAttachmentVersionMapper;
import es.caib.invai.back.service.mapper.application.documentation.attachment.AppDocumentationAttachmentMapper;
import es.caib.invai.back.service.mapper.application.documentation.attachment.AppDocumentationMetadataMapper;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachment;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachmentVersion;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationMetadata;
import es.caib.invai.back.service.model.catalog.appSection.AppSection;
import es.caib.invai.back.service.model.catalog.documentStatus.DocumentStatus;
import es.caib.invai.back.service.model.catalog.mimeType.MimeType;
import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppDocumentationAttachmentServiceFacadeBean}, exercising every branch of its business
 * rules with all the repositories and the mapper fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class AppDocumentationAttachmentServiceFacadeBeanTest {

    private static final byte[] PDF_BYTES = "%PDF-1.7 content".getBytes(StandardCharsets.UTF_8);

    @Mock
    private AppDocumentationAttachmentMapper appDocumentationAttachmentMapper;
    @Mock
    private AppDocumentationAttachmentVersionMapper appDocumentationAttachmentVersionMapper;
    @Mock
    private AppDocumentationMetadataMapper appDocumentationMetadataMapper;
    @Mock
    private AppDocumentationMetadataRepository appDocumentationMetadataRepository;
    @Mock
    private AppDocumentationAttachmentRepository appDocumentationAttachmentRepository;
    @Mock
    private AppDocumentationAttachmentVersionRepository appDocumentationAttachmentVersionRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private AppSectionRepository appSectionRepository;
    @Mock
    private DocumentStatusRepository documentStatusRepository;
    @Mock
    private MimeTypeRepository mimeTypeRepository;
    @Mock
    private DocumentCategoryRepository documentCategoryRepository;

    @InjectMocks
    private AppDocumentationAttachmentServiceFacadeBean facade;

    private MimeType pdf;
    private AppDocumentationAttachment stored;
    private AppDocumentationAttachmentVersion firstAttachment;

    @BeforeEach
    void setUp() {
        pdf = new MimeType();
        pdf.setId(1L);
        pdf.setExtension("pdf");
        pdf.setMimeType("application/pdf");
        pdf.setName("PDF");

        DocumentCategory category = new DocumentCategory();
        category.setId(5L);

        stored = new AppDocumentationAttachment();
        stored.setId(10L);
        stored.setApplicationId(3L);
        stored.setTitle("Manual API v2");
        stored.setDocumentCategory(category);
    }

    private static BusinessRuleException rule(org.junit.jupiter.api.function.Executable executable) {
        return assertThrows(BusinessRuleException.class, executable);
    }

    private static MockMultipartFile file(String name, byte[] content) {
        return new MockMultipartFile("file", name, "application/octet-stream", content);
    }

    private static AppDocumentationAttachmentInputDTO createInput() {
        return new AppDocumentationAttachmentInputDTO(3L, "  Manual API v2  ", 7L, 5L, 9L, " 1.0 ", "  first  ", null);
    }

    private static AppDocumentationAttachmentVersion version(Long id, Long documentationAttachmentId) {
        AppDocumentationAttachmentVersion version = new AppDocumentationAttachmentVersion();
        version.setId(id);
        version.setAppDocumentationAttachmentId(documentationAttachmentId);
        return version;
    }

    /** Lets a document be created and read back: every reference exists and the detail can be assembled. */
    private void stubCreate() {
        when(applicationRepository.findById(3L)).thenReturn(mock(Application.class));
        when(appSectionRepository.findById(7L)).thenReturn(new AppSection());
        when(documentStatusRepository.findById(9L)).thenReturn(new DocumentStatus());
        when(documentCategoryRepository.findById(5L)).thenReturn(new DocumentCategory());
        when(mimeTypeRepository.findByExtension("pdf")).thenReturn(pdf);
        AppDocumentationAttachment toSave = new AppDocumentationAttachment();
        when(appDocumentationAttachmentMapper.toModelFromInput(any(AppDocumentationAttachmentInputDTO.class))).thenReturn(toSave);
        when(appDocumentationAttachmentRepository.create(toSave)).thenReturn(stored);
        firstAttachment = new AppDocumentationAttachmentVersion();
        when(appDocumentationAttachmentVersionMapper.toModelFromInput(any(AppDocumentationAttachmentInputDTO.class), eq(10L), any(MultipartFile.class), eq(pdf), any(byte[].class)))
                .thenReturn(firstAttachment);
        when(appDocumentationAttachmentVersionRepository.create(eq(firstAttachment), any(byte[].class))).thenReturn(version(100L, 10L));
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.findActiveByDocumentationAttachmentId(10L)).thenReturn(List.of());
        when(appDocumentationAttachmentMapper.toResponse(stored)).thenReturn(new AppDocumentationAttachmentOutputDTO());
    }

    // ------------------------------------------------------------------
    // getAll / getById
    // ------------------------------------------------------------------

    @Test
    void getAll_attachesTheCurrentAttachmentOfEachDocumentBeforeMapping() {
        AppDocumentationAttachmentCriteria filter = new AppDocumentationAttachmentCriteria();
        Pageable pageable = Pageable.unpaged();
        AppDocumentationAttachmentVersion current = version(100L, 10L);
        AppDocumentationAttachmentSimpleOutputDTO dto = new AppDocumentationAttachmentSimpleOutputDTO();
        when(appDocumentationAttachmentRepository.findAll(filter, pageable)).thenReturn(new PageImpl<>(List.of(stored)));
        when(appDocumentationAttachmentVersionRepository.findCurrentByDocumentationAttachmentIds(List.of(10L))).thenReturn(Map.of(10L, current));
        when(appDocumentationAttachmentMapper.toSimpleResponse(stored)).thenReturn(dto);

        Page<AppDocumentationAttachmentSimpleOutputDTO> result = facade.getAll(filter, pageable);

        assertSame(current, stored.getCurrentAttachment());
        assertSame(dto, result.getContent().get(0));
    }

    @Test
    void getAll_aDocumentWithoutACurrentAttachment_keepsItNull() {
        AppDocumentationAttachmentCriteria filter = new AppDocumentationAttachmentCriteria();
        Pageable pageable = Pageable.unpaged();
        when(appDocumentationAttachmentRepository.findAll(filter, pageable)).thenReturn(new PageImpl<>(List.of(stored)));
        when(appDocumentationAttachmentVersionRepository.findCurrentByDocumentationAttachmentIds(List.of(10L))).thenReturn(Map.of());
        when(appDocumentationAttachmentMapper.toSimpleResponse(stored)).thenReturn(new AppDocumentationAttachmentSimpleOutputDTO());

        facade.getAll(filter, pageable);

        assertNull(stored.getCurrentAttachment());
    }

    @Test
    void getById_found_returnsTheDocumentWithItsActiveAttachments() {
        List<AppDocumentationAttachmentVersion> attachments = List.of(version(101L, 10L), version(100L, 10L));
        AppDocumentationAttachmentOutputDTO dto = new AppDocumentationAttachmentOutputDTO();
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.findActiveByDocumentationAttachmentId(10L)).thenReturn(attachments);
        when(appDocumentationAttachmentMapper.toResponse(stored)).thenReturn(dto);

        assertSame(dto, facade.getById(10L));
        assertSame(attachments, stored.getAttachments());
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(appDocumentationAttachmentRepository.findById(99L)).thenReturn(null);

        assertEquals(Constants.ERR_DOCUMENTATION_NOT_FOUND, rule(() -> facade.getById(99L)).getMessage());
    }

    @Test
    void getById_aDeletedDocument_isReturnedWithTheAttachmentsAndMetadataDeletedAlongWithIt() {
        LocalDateTime deletedAt = LocalDateTime.of(2026, 10, 20, 12, 0);
        stored.setDeletedAt(deletedAt);
        AppDocumentationAttachmentVersion attachment = version(100L, 10L);
        AppDocumentationMetadata ofTheAttachment = storedMeta(100L, "a", "1");
        AppDocumentationAttachmentOutputDTO dto = new AppDocumentationAttachmentOutputDTO();
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.findDeletedWithDocumentationAttachment(10L, deletedAt)).thenReturn(List.of(attachment));
        when(appDocumentationMetadataRepository.findDeletedWithDocumentationAttachment(List.of(100L), deletedAt)).thenReturn(List.of(ofTheAttachment));
        when(appDocumentationAttachmentMapper.toResponse(stored)).thenReturn(dto);

        assertSame(dto, facade.getById(10L));

        assertEquals(List.of(attachment), stored.getAttachments());
        assertEquals(List.of(ofTheAttachment), attachment.getMetadata());
        verify(appDocumentationAttachmentVersionRepository, never()).findActiveByDocumentationAttachmentId(anyLong());
        verify(appDocumentationMetadataRepository, never()).findActiveByAttachmentIds(any());
    }

    @Test
    void getById_anActiveDocument_neverLooksForTheDeletedOnes() {
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentMapper.toResponse(stored)).thenReturn(new AppDocumentationAttachmentOutputDTO());

        facade.getById(10L);

        verify(appDocumentationAttachmentVersionRepository, never()).findDeletedWithDocumentationAttachment(anyLong(), any());
        verify(appDocumentationMetadataRepository, never()).findDeletedWithDocumentationAttachment(any(), any());
    }

    @Test
    void create_happyPath_storesTheDocumentAndItsAttachmentBuiltByTheMapper() {
        stubCreate();
        AppDocumentationAttachmentInputDTO inputDTO = createInput();
        MockMultipartFile upload = file("C:\\docs\\Manual.PDF", PDF_BYTES);

        AppDocumentationAttachmentOutputDTO result = facade.create(inputDTO, upload);

        assertNotNull(result);
        verify(appDocumentationAttachmentVersionMapper).toModelFromInput(eq(inputDTO), eq(10L), eq(upload), eq(pdf), aryEq(PDF_BYTES));
        verify(appDocumentationAttachmentVersionRepository).create(eq(firstAttachment), aryEq(PDF_BYTES));
    }

    @Test
    void create_doesNotLookForDuplicatedFiles() {
        stubCreate();

        facade.create(createInput(), file("manual.pdf", PDF_BYTES));

        verify(appDocumentationAttachmentVersionRepository, never()).existsActiveWithLabel(anyLong(), any());
    }

    @Test
    void create_applicationNotFound_throwsAndStoresNothing() {
        when(applicationRepository.findById(3L)).thenReturn(null);

        assertEquals(Constants.ERR_APP_NOT_FOUND, rule(() -> facade.create(createInput(), file("a.pdf", PDF_BYTES))).getMessage());
        verify(appDocumentationAttachmentRepository, never()).create(any());
    }

    @Test
    void create_sectionNotFound_throws() {
        when(applicationRepository.findById(3L)).thenReturn(mock(Application.class));
        when(appSectionRepository.findById(7L)).thenReturn(null);

        assertEquals(Constants.ERR_DOCUMENTATION_SECTION_NOT_FOUND, rule(() -> facade.create(createInput(), file("a.pdf", PDF_BYTES))).getMessage());
    }

    @Test
    void create_statusNotFound_throws() {
        when(applicationRepository.findById(3L)).thenReturn(mock(Application.class));
        when(appSectionRepository.findById(7L)).thenReturn(new AppSection());
        when(documentStatusRepository.findById(9L)).thenReturn(null);

        assertEquals(Constants.ERR_DOCUMENTATION_STATUS_NOT_FOUND, rule(() -> facade.create(createInput(), file("a.pdf", PDF_BYTES))).getMessage());
    }

    @Test
    void create_categoryNotFound_throws() {
        when(applicationRepository.findById(3L)).thenReturn(mock(Application.class));
        when(appSectionRepository.findById(7L)).thenReturn(new AppSection());
        when(documentStatusRepository.findById(9L)).thenReturn(new DocumentStatus());
        when(documentCategoryRepository.findById(5L)).thenReturn(null);

        assertEquals(Constants.ERR_DOCUMENTATION_CATEGORY_NOT_FOUND, rule(() -> facade.create(createInput(), file("a.pdf", PDF_BYTES))).getMessage());
    }

    @Test
    void create_categoryDeleted_throws() {
        DocumentCategory deleted = new DocumentCategory();
        deleted.setDeletedAt(LocalDateTime.now());
        when(applicationRepository.findById(3L)).thenReturn(mock(Application.class));
        when(appSectionRepository.findById(7L)).thenReturn(new AppSection());
        when(documentStatusRepository.findById(9L)).thenReturn(new DocumentStatus());
        when(documentCategoryRepository.findById(5L)).thenReturn(deleted);

        assertEquals(Constants.ERR_DOCUMENTATION_CATEGORY_NOT_ACTIVE, rule(() -> facade.create(createInput(), file("a.pdf", PDF_BYTES))).getMessage());
    }

    @Test
    void create_emptyFile_throws() {
        stubReferencesOnly();

        assertEquals(Constants.ERR_DOCUMENTATION_ATTACHMENT_FILE_EMPTY, rule(() -> facade.create(createInput(), file("a.pdf", new byte[0]))).getMessage());
    }

    @Test
    void create_missingFile_throws() {
        stubReferencesOnly();

        assertEquals(Constants.ERR_DOCUMENTATION_ATTACHMENT_FILE_EMPTY, rule(() -> facade.create(createInput(), null)).getMessage());
    }

    @Test
    void create_fileOverFiveMegabytes_throws() {
        stubReferencesOnly();
        byte[] tooBig = new byte[5 * 1024 * 1024 + 1];

        assertEquals(Constants.ERR_DOCUMENTATION_ATTACHMENT_FILE_TOO_LARGE, rule(() -> facade.create(createInput(), file("a.pdf", tooBig))).getMessage());
    }

    @Test
    void create_fileOfExactlyFiveMegabytes_isAccepted() {
        stubCreate();
        byte[] limit = new byte[5 * 1024 * 1024];
        limit[0] = 1;

        assertNotNull(facade.create(createInput(), file("a.pdf", limit)));
    }

    @Test
    void create_extensionNotInTheCatalog_throws() {
        stubReferencesOnly();
        when(mimeTypeRepository.findByExtension("exe")).thenReturn(null);

        assertEquals(Constants.ERR_DOCUMENTATION_ATTACHMENT_FILE_TYPE_NOT_ALLOWED, rule(() -> facade.create(createInput(), file("virus.exe", PDF_BYTES))).getMessage());
        verify(appDocumentationAttachmentRepository, never()).create(any());
    }

    @Test
    void create_fileWithoutExtension_throwsWithoutLookingItUp() {
        stubReferencesOnly();

        assertEquals(Constants.ERR_DOCUMENTATION_ATTACHMENT_FILE_TYPE_NOT_ALLOWED, rule(() -> facade.create(createInput(), file("README", PDF_BYTES))).getMessage());
        verify(mimeTypeRepository, never()).findByExtension(any());
    }

    /** Stubs the application and the references so the failure under test happens in the file checks. */
    private void stubReferencesOnly() {
        when(applicationRepository.findById(3L)).thenReturn(mock(Application.class));
        when(appSectionRepository.findById(7L)).thenReturn(new AppSection());
        when(documentStatusRepository.findById(9L)).thenReturn(new DocumentStatus());
        when(documentCategoryRepository.findById(5L)).thenReturn(new DocumentCategory());
    }

    // ------------------------------------------------------------------
    // createAttachment
    // ------------------------------------------------------------------

    @Test
    void createAttachment_happyPath_unmarksThePreviousCurrentAttachmentBeforeStoringTheNewOne() {
        AppDocumentationAttachmentVersionInputDTO inputDTO = new AppDocumentationAttachmentVersionInputDTO(" 2.0 ", null, null);
        MockMultipartFile upload = file("manual-v2.pdf", PDF_BYTES);
        AppDocumentationAttachmentVersion newAttachment = new AppDocumentationAttachmentVersion();
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.existsActiveWithLabel(10L, "2.0")).thenReturn(false);
        when(mimeTypeRepository.findByExtension("pdf")).thenReturn(pdf);
        when(appDocumentationAttachmentVersionMapper.toModelFromInput(eq(inputDTO), eq(10L), eq(upload), eq(pdf), aryEq(PDF_BYTES))).thenReturn(newAttachment);
        when(appDocumentationAttachmentVersionRepository.create(eq(newAttachment), aryEq(PDF_BYTES))).thenReturn(version(101L, 10L));
        when(appDocumentationAttachmentVersionRepository.findActiveByDocumentationAttachmentId(10L)).thenReturn(List.of());
        when(appDocumentationAttachmentMapper.toResponse(stored)).thenReturn(new AppDocumentationAttachmentOutputDTO());

        facade.createAttachment(10L, inputDTO, upload);

        InOrder inOrder = inOrder(appDocumentationAttachmentVersionRepository);
        inOrder.verify(appDocumentationAttachmentVersionRepository).unmarkCurrent(10L);
        inOrder.verify(appDocumentationAttachmentVersionRepository).create(eq(newAttachment), aryEq(PDF_BYTES));
    }

    @Test
    void createAttachment_documentNotFound_throws() {
        when(appDocumentationAttachmentRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = rule(() -> facade.createAttachment(99L, new AppDocumentationAttachmentVersionInputDTO("2.0", null, null), file("a.pdf", PDF_BYTES)));

        assertEquals(Constants.ERR_DOCUMENTATION_NOT_FOUND, ex.getMessage());
    }

    @Test
    void createAttachment_documentDeleted_throws() {
        stored.setDeletedAt(LocalDateTime.now());
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);

        BusinessRuleException ex = rule(() -> facade.createAttachment(10L, new AppDocumentationAttachmentVersionInputDTO("2.0", null, null), file("a.pdf", PDF_BYTES)));

        assertEquals(Constants.ERR_DOCUMENTATION_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void createAttachment_labelAlreadyUsed_throwsAndKeepsTheCurrentAttachment() {
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.existsActiveWithLabel(10L, "1.0")).thenReturn(true);

        BusinessRuleException ex = rule(() -> facade.createAttachment(10L, new AppDocumentationAttachmentVersionInputDTO("1.0", null, null), file("a.pdf", PDF_BYTES)));

        assertEquals(Constants.ERR_DOCUMENTATION_ATTACHMENT_VERSION_DUPLICATED, ex.getMessage());
        verify(appDocumentationAttachmentVersionRepository, never()).unmarkCurrent(anyLong());
    }

    @Test
    void createAttachment_invalidFile_throwsAndKeepsTheCurrentAttachment() {
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.existsActiveWithLabel(10L, "2.0")).thenReturn(false);
        when(mimeTypeRepository.findByExtension("exe")).thenReturn(null);

        BusinessRuleException ex = rule(() -> facade.createAttachment(10L, new AppDocumentationAttachmentVersionInputDTO("2.0", null, null), file("a.exe", PDF_BYTES)));

        assertEquals(Constants.ERR_DOCUMENTATION_ATTACHMENT_FILE_TYPE_NOT_ALLOWED, ex.getMessage());
        verify(appDocumentationAttachmentVersionRepository, never()).unmarkCurrent(anyLong());
        verify(appDocumentationAttachmentVersionRepository, never()).create(any(), any());
    }

    // ------------------------------------------------------------------
    // update
    // ------------------------------------------------------------------

    @Test
    void update_changedCategory_isValidatedAndOnlyTheMainRecordIsPersisted() {
        AppDocumentationAttachmentUpdateInputDTO inputDTO = new AppDocumentationAttachmentUpdateInputDTO("  New title ", 7L, 6L, 9L);
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appSectionRepository.findById(7L)).thenReturn(new AppSection());
        when(documentStatusRepository.findById(9L)).thenReturn(new DocumentStatus());
        when(documentCategoryRepository.findById(6L)).thenReturn(new DocumentCategory());
        when(appDocumentationAttachmentVersionRepository.findActiveByDocumentationAttachmentId(10L)).thenReturn(List.of());
        when(appDocumentationAttachmentMapper.toResponse(stored)).thenReturn(new AppDocumentationAttachmentOutputDTO());

        facade.update(10L, inputDTO);

        assertEquals("New title", inputDTO.getTitle());
        verify(appDocumentationAttachmentMapper).updateModelFromInput(inputDTO, stored);
        verify(appDocumentationAttachmentRepository).update(stored, 10L);
        verify(appDocumentationAttachmentVersionRepository, never()).unmarkCurrent(anyLong());
        verify(appDocumentationAttachmentVersionRepository, never()).create(any(), any());
    }

    @Test
    void update_unchangedCategory_isNotCheckedAgain_soAnInactiveOneStillWorks() {
        AppDocumentationAttachmentUpdateInputDTO inputDTO = new AppDocumentationAttachmentUpdateInputDTO("Title", 7L, 5L, 9L);
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appSectionRepository.findById(7L)).thenReturn(new AppSection());
        when(documentStatusRepository.findById(9L)).thenReturn(new DocumentStatus());
        when(appDocumentationAttachmentVersionRepository.findActiveByDocumentationAttachmentId(10L)).thenReturn(List.of());
        when(appDocumentationAttachmentMapper.toResponse(stored)).thenReturn(new AppDocumentationAttachmentOutputDTO());

        facade.update(10L, inputDTO);

        verify(documentCategoryRepository, never()).findById(anyLong());
        verify(appDocumentationAttachmentRepository).update(stored, 10L);
    }

    @Test
    void update_newCategoryDeleted_throws() {
        DocumentCategory deleted = new DocumentCategory();
        deleted.setDeletedAt(LocalDateTime.now());
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appSectionRepository.findById(7L)).thenReturn(new AppSection());
        when(documentStatusRepository.findById(9L)).thenReturn(new DocumentStatus());
        when(documentCategoryRepository.findById(6L)).thenReturn(deleted);

        BusinessRuleException ex = rule(() -> facade.update(10L, new AppDocumentationAttachmentUpdateInputDTO("T", 7L, 6L, 9L)));

        assertEquals(Constants.ERR_DOCUMENTATION_CATEGORY_NOT_ACTIVE, ex.getMessage());
        verify(appDocumentationAttachmentRepository, never()).update(any(), anyLong());
    }

    @Test
    void update_documentNotFound_throws() {
        when(appDocumentationAttachmentRepository.findById(99L)).thenReturn(null);

        assertEquals(Constants.ERR_DOCUMENTATION_NOT_FOUND, rule(() -> facade.update(99L, new AppDocumentationAttachmentUpdateInputDTO("T", 7L, 5L, 9L))).getMessage());
    }

    @Test
    void update_documentDeleted_throws() {
        stored.setDeletedAt(LocalDateTime.now());
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);

        assertEquals(Constants.ERR_DOCUMENTATION_NOT_ACTIVE, rule(() -> facade.update(10L, new AppDocumentationAttachmentUpdateInputDTO("T", 7L, 5L, 9L))).getMessage());
    }

    @Test
    void update_sectionNotFound_throws() {
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appSectionRepository.findById(7L)).thenReturn(null);

        assertEquals(Constants.ERR_DOCUMENTATION_SECTION_NOT_FOUND, rule(() -> facade.update(10L, new AppDocumentationAttachmentUpdateInputDTO("T", 7L, 5L, 9L))).getMessage());
    }

    // ------------------------------------------------------------------
    // delete
    // ------------------------------------------------------------------

    @Test
    void delete_marksTheDocumentAndAllItsAttachmentsAsDeletedTogether() {
        when(appDocumentationAttachmentVersionRepository.findActiveByDocumentationAttachmentId(10L)).thenReturn(List.of(version(100L, 10L)));
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);

        facade.delete(10L);

        assertNotNull(stored.getDeletedAt());
        assertNotNull(stored.getDeletedBy());
        verify(appDocumentationAttachmentRepository).delete(stored);
        verify(appDocumentationMetadataRepository).deleteAllByAttachmentIds(List.of(100L), stored.getDeletedAt(), stored.getDeletedBy());
        verify(appDocumentationAttachmentVersionRepository).deleteAllByDocumentationAttachmentId(10L, stored.getDeletedAt(), stored.getDeletedBy());
    }

    @Test
    void delete_documentNotFound_throws() {
        when(appDocumentationAttachmentRepository.findById(99L)).thenReturn(null);

        assertEquals(Constants.ERR_DOCUMENTATION_NOT_FOUND, rule(() -> facade.delete(99L)).getMessage());
        verify(appDocumentationAttachmentVersionRepository, never()).deleteAllByDocumentationAttachmentId(anyLong(), any(), any());
    }

    @Test
    void delete_alreadyDeleted_throws() {
        stored.setDeletedAt(LocalDateTime.now());
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);

        assertEquals(Constants.ERR_DOCUMENTATION_NOT_ACTIVE, rule(() -> facade.delete(10L)).getMessage());
        verify(appDocumentationAttachmentRepository, never()).delete(any());
    }

    // ------------------------------------------------------------------
    // download
    // ------------------------------------------------------------------

    @Test
    void download_anyVersionOfTheDocument_returnsItsFileNameMediaTypeAndBytes() {
        AppDocumentationAttachmentVersion old = version(100L, 10L);
        old.setFileName("manual-v1.pdf");
        old.setMimeType(pdf);
        old.setCurrent(false);
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.findById(100L)).thenReturn(old);
        when(appDocumentationAttachmentVersionRepository.findContentById(100L)).thenReturn(PDF_BYTES);

        AppDocumentationAttachmentVersionDownloadDTO result = facade.download(10L, 100L, false);

        assertEquals("manual-v1.pdf", result.getFileName());
        assertEquals("application/pdf", result.getMimeType());
        assertArrayEquals(PDF_BYTES, result.getContent());
        verify(appDocumentationAttachmentVersionRepository).registerDownload(100L, false);
    }

    @Test
    void download_documentNotFound_throws() {
        when(appDocumentationAttachmentRepository.findById(99L)).thenReturn(null);

        assertEquals(Constants.ERR_DOCUMENTATION_NOT_FOUND, rule(() -> facade.download(99L, 100L, false)).getMessage());
    }

    @Test
    void download_documentDeleted_throws() {
        stored.setDeletedAt(LocalDateTime.now());
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);

        assertEquals(Constants.ERR_DOCUMENTATION_NOT_FOUND, rule(() -> facade.download(10L, 100L, false)).getMessage());
    }

    @Test
    void download_versionNotFound_throws() {
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.findById(100L)).thenReturn(null);

        assertEquals(Constants.ERR_DOCUMENTATION_ATTACHMENT_NOT_FOUND, rule(() -> facade.download(10L, 100L, false)).getMessage());
    }

    @Test
    void download_versionDeleted_throws() {
        AppDocumentationAttachmentVersion deleted = version(100L, 10L);
        deleted.setDeletedAt(LocalDateTime.now());
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.findById(100L)).thenReturn(deleted);

        assertEquals(Constants.ERR_DOCUMENTATION_ATTACHMENT_NOT_FOUND, rule(() -> facade.download(10L, 100L, false)).getMessage());
        verify(appDocumentationAttachmentVersionRepository, never()).findContentById(anyLong());
    }

    @Test
    void download_versionOfAnotherDocument_throws() {
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.findById(100L)).thenReturn(version(100L, 11L));

        assertEquals(Constants.ERR_DOCUMENTATION_ATTACHMENT_NOT_FOUND, rule(() -> facade.download(10L, 100L, false)).getMessage());
        verify(appDocumentationAttachmentVersionRepository, never()).findContentById(eq(100L));
    }

    // ------------------------------------------------------------------
    // metadata
    // ------------------------------------------------------------------

    private static AppDocumentationMetadataInputDTO meta(String key, String value) {
        return new AppDocumentationMetadataInputDTO(key, value);
    }

    private static AppDocumentationMetadata storedMeta(Long attachmentId, String key, String value) {
        AppDocumentationMetadata metadata = new AppDocumentationMetadata();
        metadata.setAppDocumentationAttachmentVersionId(attachmentId);
        metadata.setMetaKey(key);
        metadata.setMetaValue(value);
        return metadata;
    }

    @Test
    void getById_handsTheMetadataOfEachAttachmentToIt() {
        AppDocumentationAttachmentVersion first = version(100L, 10L);
        AppDocumentationAttachmentVersion second = version(101L, 10L);
        AppDocumentationMetadata ofTheFirst = storedMeta(100L, "a", "1");
        AppDocumentationMetadata ofTheSecond = storedMeta(101L, "b", "2");
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.findActiveByDocumentationAttachmentId(10L)).thenReturn(List.of(second, first));
        when(appDocumentationMetadataRepository.findActiveByAttachmentIds(List.of(101L, 100L))).thenReturn(List.of(ofTheFirst, ofTheSecond));
        when(appDocumentationAttachmentMapper.toResponse(stored)).thenReturn(new AppDocumentationAttachmentOutputDTO());

        facade.getById(10L);

        assertEquals(List.of(ofTheFirst), first.getMetadata());
        assertEquals(List.of(ofTheSecond), second.getMetadata());
    }

    @Test
    void create_withMetadata_storesItForTheFirstAttachment() {
        stubCreate();
        AppDocumentationMetadataInputDTO item = meta("  eni.clase  ", " Manual ");
        AppDocumentationAttachmentInputDTO inputDTO = createInput();
        inputDTO.setMetadata(List.of(item));
        AppDocumentationMetadata model = new AppDocumentationMetadata();
        when(appDocumentationMetadataMapper.toModelFromInput(item, 100L)).thenReturn(model);

        facade.create(inputDTO, file("manual.pdf", PDF_BYTES));

        assertEquals("eni.clase", item.getKey());
        assertEquals("Manual", item.getValue());
        verify(appDocumentationMetadataRepository).create(model);
    }

    @Test
    void create_withoutMetadata_storesNone() {
        stubCreate();

        facade.create(createInput(), file("manual.pdf", PDF_BYTES));

        verify(appDocumentationMetadataRepository, never()).create(any());
    }

    @Test
    void create_metadataWithARepeatedKey_throwsAndStoresNothing() {
        stubReferencesOnly();
        AppDocumentationAttachmentInputDTO inputDTO = createInput();
        inputDTO.setMetadata(List.of(meta("k", "1"), meta(" k ", "2")));

        assertEquals(Constants.ERR_DOCUMENTATION_METADATA_DUPLICATED, rule(() -> facade.create(inputDTO, file("a.pdf", PDF_BYTES))).getMessage());
        verify(appDocumentationAttachmentRepository, never()).create(any());
    }

    @Test
    void createAttachment_withMetadata_storesItForTheNewAttachment() {
        AppDocumentationMetadataInputDTO item = meta("k", "v");
        AppDocumentationAttachmentVersionInputDTO inputDTO = new AppDocumentationAttachmentVersionInputDTO("2.0", null, List.of(item));
        MockMultipartFile upload = file("manual-v2.pdf", PDF_BYTES);
        AppDocumentationAttachmentVersion newAttachment = new AppDocumentationAttachmentVersion();
        AppDocumentationMetadata model = new AppDocumentationMetadata();
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.existsActiveWithLabel(10L, "2.0")).thenReturn(false);
        when(mimeTypeRepository.findByExtension("pdf")).thenReturn(pdf);
        when(appDocumentationAttachmentVersionMapper.toModelFromInput(eq(inputDTO), eq(10L), eq(upload), eq(pdf), aryEq(PDF_BYTES))).thenReturn(newAttachment);
        when(appDocumentationAttachmentVersionRepository.create(eq(newAttachment), aryEq(PDF_BYTES))).thenReturn(version(101L, 10L));
        when(appDocumentationMetadataMapper.toModelFromInput(item, 101L)).thenReturn(model);
        when(appDocumentationAttachmentVersionRepository.findActiveByDocumentationAttachmentId(10L)).thenReturn(List.of());
        when(appDocumentationAttachmentMapper.toResponse(stored)).thenReturn(new AppDocumentationAttachmentOutputDTO());

        facade.createAttachment(10L, inputDTO, upload);

        verify(appDocumentationMetadataRepository).create(model);
    }

    @Test
    void createAttachment_metadataWithARepeatedKey_throwsAndKeepsTheCurrentAttachment() {
        AppDocumentationAttachmentVersionInputDTO inputDTO = new AppDocumentationAttachmentVersionInputDTO("2.0", null, List.of(meta("k", "1"), meta("k", "2")));
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.existsActiveWithLabel(10L, "2.0")).thenReturn(false);

        BusinessRuleException ex = rule(() -> facade.createAttachment(10L, inputDTO, file("a.pdf", PDF_BYTES)));

        assertEquals(Constants.ERR_DOCUMENTATION_METADATA_DUPLICATED, ex.getMessage());
        verify(appDocumentationAttachmentVersionRepository, never()).unmarkCurrent(anyLong());
    }

    @Test
    void download_inline_registersAViewInsteadOfADownload() {
        AppDocumentationAttachmentVersion shown = version(100L, 10L);
        shown.setFileName("manual.pdf");
        shown.setMimeType(pdf);
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.findById(100L)).thenReturn(shown);
        when(appDocumentationAttachmentVersionRepository.findContentById(100L)).thenReturn(PDF_BYTES);

        facade.download(10L, 100L, true);

        verify(appDocumentationAttachmentVersionRepository).registerDownload(100L, true);
    }

    @Test
    void download_thatFails_registersNothing() {
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.findById(100L)).thenReturn(version(100L, 11L));

        rule(() -> facade.download(10L, 100L, false));

        verify(appDocumentationAttachmentVersionRepository, never()).registerDownload(anyLong(), anyBoolean());
    }

    // ------------------------------------------------------------------
    // deleteAttachment
    // ------------------------------------------------------------------

    private static AppDocumentationAttachmentVersion attachment(Long id, boolean current) {
        AppDocumentationAttachmentVersion attachment = version(id, 10L);
        attachment.setCurrent(current);
        return attachment;
    }

    @Test
    void deleteAttachment_aNonCurrentOne_isDeletedWithItsMetadataAndTheCurrentOneStays() {
        AppDocumentationAttachmentVersion old = attachment(100L, false);
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.findById(100L)).thenReturn(old);
        when(appDocumentationAttachmentVersionRepository.findActiveByDocumentationAttachmentId(10L)).thenReturn(List.of(attachment(101L, true), old));

        facade.deleteAttachment(10L, 100L);

        assertNotNull(old.getDeletedAt());
        assertNotNull(old.getDeletedBy());
        verify(appDocumentationAttachmentVersionRepository).delete(old);
        verify(appDocumentationMetadataRepository).deleteAllByAttachmentIds(List.of(100L), old.getDeletedAt(), old.getDeletedBy());
        verify(appDocumentationAttachmentVersionRepository, never()).markCurrent(anyLong());
        verify(appDocumentationAttachmentRepository, never()).delete(any());
    }

    @Test
    void deleteAttachment_theCurrentOne_makesTheMostRecentOfTheRestTheCurrentOneAfterDeletingIt() {
        AppDocumentationAttachmentVersion current = attachment(102L, true);
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.findById(102L)).thenReturn(current);
        when(appDocumentationAttachmentVersionRepository.findActiveByDocumentationAttachmentId(10L))
                .thenReturn(List.of(current, attachment(101L, false), attachment(100L, false)));

        facade.deleteAttachment(10L, 102L);

        InOrder inOrder = inOrder(appDocumentationAttachmentVersionRepository);
        inOrder.verify(appDocumentationAttachmentVersionRepository).delete(current);
        inOrder.verify(appDocumentationAttachmentVersionRepository).markCurrent(101L);
        verify(appDocumentationAttachmentVersionRepository, never()).markCurrent(100L);
        verify(appDocumentationMetadataRepository).deleteAllByAttachmentIds(List.of(102L), current.getDeletedAt(), current.getDeletedBy());
    }

    @Test
    void deleteAttachment_theOnlyOneLeft_deletesTheWholeDocumentInstead() {
        AppDocumentationAttachmentVersion only = attachment(100L, true);
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.findById(100L)).thenReturn(only);
        when(appDocumentationAttachmentVersionRepository.findActiveByDocumentationAttachmentId(10L)).thenReturn(List.of(only));

        facade.deleteAttachment(10L, 100L);

        assertNotNull(stored.getDeletedAt());
        verify(appDocumentationAttachmentRepository).delete(stored);
        verify(appDocumentationAttachmentVersionRepository).deleteAllByDocumentationAttachmentId(10L, stored.getDeletedAt(), stored.getDeletedBy());
        verify(appDocumentationMetadataRepository).deleteAllByAttachmentIds(List.of(100L), stored.getDeletedAt(), stored.getDeletedBy());
        verify(appDocumentationAttachmentVersionRepository, never()).delete(any());
        verify(appDocumentationAttachmentVersionRepository, never()).markCurrent(anyLong());
    }

    @Test
    void deleteAttachment_documentNotFound_throws() {
        when(appDocumentationAttachmentRepository.findById(99L)).thenReturn(null);

        assertEquals(Constants.ERR_DOCUMENTATION_NOT_FOUND, rule(() -> facade.deleteAttachment(99L, 100L)).getMessage());
    }

    @Test
    void deleteAttachment_documentDeleted_throws() {
        stored.setDeletedAt(LocalDateTime.now());
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);

        assertEquals(Constants.ERR_DOCUMENTATION_NOT_ACTIVE, rule(() -> facade.deleteAttachment(10L, 100L)).getMessage());
    }

    @Test
    void deleteAttachment_attachmentNotFound_throws() {
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.findById(100L)).thenReturn(null);

        assertEquals(Constants.ERR_DOCUMENTATION_ATTACHMENT_NOT_FOUND, rule(() -> facade.deleteAttachment(10L, 100L)).getMessage());
    }

    @Test
    void deleteAttachment_attachmentAlreadyDeleted_throws() {
        AppDocumentationAttachmentVersion deleted = attachment(100L, false);
        deleted.setDeletedAt(LocalDateTime.now());
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.findById(100L)).thenReturn(deleted);

        assertEquals(Constants.ERR_DOCUMENTATION_ATTACHMENT_NOT_FOUND, rule(() -> facade.deleteAttachment(10L, 100L)).getMessage());
        verify(appDocumentationAttachmentVersionRepository, never()).delete(any());
    }

    @Test
    void deleteAttachment_attachmentOfAnotherDocument_throws() {
        when(appDocumentationAttachmentRepository.findById(10L)).thenReturn(stored);
        when(appDocumentationAttachmentVersionRepository.findById(100L)).thenReturn(version(100L, 11L));

        assertEquals(Constants.ERR_DOCUMENTATION_ATTACHMENT_NOT_FOUND, rule(() -> facade.deleteAttachment(10L, 100L)).getMessage());
        verify(appDocumentationAttachmentVersionRepository, never()).delete(any());
    }
}
