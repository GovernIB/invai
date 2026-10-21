package es.caib.invai.back.interna.application.documentation.attachment;

import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentVersionDownloadDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentInputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentOutputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentSimpleOutputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentUpdateInputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentVersionInputDTO;
import es.caib.invai.back.persistence.repository.application.documentation.attachment.AppDocumentationAttachmentCriteria;
import es.caib.invai.back.service.facade.application.documentation.attachment.AppDocumentationAttachmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppDocumentationAttachmentController}, verifying that every endpoint delegates to
 * {@link AppDocumentationAttachmentService}, returns the expected HTTP status code and, for the download,
 * builds the headers the browser needs.
 */
@ExtendWith(MockitoExtension.class)
class AppDocumentationAttachmentControllerTest {

    private static final byte[] CONTENT = "%PDF-1.7".getBytes(StandardCharsets.UTF_8);

    @Mock
    private AppDocumentationAttachmentService appDocumentationAttachmentService;

    private AppDocumentationAttachmentController controller;

    @BeforeEach
    void setUp() {
        controller = new AppDocumentationAttachmentController(appDocumentationAttachmentService);
    }

    @Test
    void getAll_scopesTheFilterByTheApplicationAndReturnsOk() {
        AppDocumentationAttachmentCriteria filter = new AppDocumentationAttachmentCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Page<AppDocumentationAttachmentSimpleOutputDTO> page = new PageImpl<>(List.of(new AppDocumentationAttachmentSimpleOutputDTO()));
        when(appDocumentationAttachmentService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<AppDocumentationAttachmentSimpleOutputDTO>> response = controller.getAll(3L, filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
        assertEquals(3L, filter.getApplicationId());
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        AppDocumentationAttachmentOutputDTO dto = new AppDocumentationAttachmentOutputDTO();
        when(appDocumentationAttachmentService.getById(10L)).thenReturn(dto);

        ResponseEntity<AppDocumentationAttachmentOutputDTO> response = controller.getById(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        AppDocumentationAttachmentInputDTO inputDTO = new AppDocumentationAttachmentInputDTO(3L, "Title", 7L, 5L, 9L, "1.0", null, null);
        MockMultipartFile file = new MockMultipartFile("file", "a.pdf", "application/pdf", CONTENT);
        AppDocumentationAttachmentOutputDTO dto = new AppDocumentationAttachmentOutputDTO();
        when(appDocumentationAttachmentService.create(inputDTO, file)).thenReturn(dto);

        ResponseEntity<AppDocumentationAttachmentOutputDTO> response = controller.create(inputDTO, file);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void createAttachment_returnsCreatedWithServiceResult() {
        AppDocumentationAttachmentVersionInputDTO inputDTO = new AppDocumentationAttachmentVersionInputDTO("2.0", null, null);
        MockMultipartFile file = new MockMultipartFile("file", "a.pdf", "application/pdf", CONTENT);
        AppDocumentationAttachmentOutputDTO dto = new AppDocumentationAttachmentOutputDTO();
        when(appDocumentationAttachmentService.createAttachment(10L, inputDTO, file)).thenReturn(dto);

        ResponseEntity<AppDocumentationAttachmentOutputDTO> response = controller.createAttachment(10L, inputDTO, file);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        AppDocumentationAttachmentUpdateInputDTO inputDTO = new AppDocumentationAttachmentUpdateInputDTO("Title", 7L, 5L, 9L);
        AppDocumentationAttachmentOutputDTO dto = new AppDocumentationAttachmentOutputDTO();
        when(appDocumentationAttachmentService.update(10L, inputDTO)).thenReturn(dto);

        ResponseEntity<AppDocumentationAttachmentOutputDTO> response = controller.update(10L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> response = controller.delete(10L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appDocumentationAttachmentService).delete(10L);
    }

    @Test
    void deleteAttachment_returnsNoContent() {
        ResponseEntity<Void> response = controller.deleteAttachment(10L, 100L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appDocumentationAttachmentService).deleteAttachment(10L, 100L);
    }

    @Test
    void download_byDefault_isAnAttachmentWithTheFileNameAndMediaType() {
        when(appDocumentationAttachmentService.download(10L, 100L, false)).thenReturn(new AppDocumentationAttachmentVersionDownloadDTO("Manual API.pdf", "application/pdf", CONTENT));

        ResponseEntity<byte[]> response = controller.download(10L, 100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(CONTENT, response.getBody());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertEquals(CONTENT.length, response.getHeaders().getContentLength());
        String disposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertTrue(disposition.startsWith("attachment"), disposition);
        assertTrue(disposition.contains("filename*=UTF-8''Manual%20API.pdf"), disposition);
        assertEquals("nosniff", response.getHeaders().getFirst("X-Content-Type-Options"));
        verify(appDocumentationAttachmentService).download(10L, 100L, false);
    }

    @Test
    void view_isTheSameFileShownInTheBrowser_onlyTheDispositionChanges() {
        when(appDocumentationAttachmentService.download(10L, 100L, true)).thenReturn(new AppDocumentationAttachmentVersionDownloadDTO("manual.pdf", "application/pdf", CONTENT));

        ResponseEntity<byte[]> response = controller.view(10L, 100L);

        String disposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertTrue(disposition.startsWith("inline"), disposition);
        assertArrayEquals(CONTENT, response.getBody());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertEquals("nosniff", response.getHeaders().getFirst("X-Content-Type-Options"));
        assertNull(response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING));
        verify(appDocumentationAttachmentService).download(10L, 100L, true);
    }

    @Test
    void download_nonAsciiFileName_isEncodedInTheDisposition() {
        when(appDocumentationAttachmentService.download(10L, 100L, false)).thenReturn(new AppDocumentationAttachmentVersionDownloadDTO("Guía d'instal·lació.pdf", "application/pdf", CONTENT));

        ResponseEntity<byte[]> response = controller.download(10L, 100L);

        String disposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertTrue(disposition.contains("filename*=UTF-8''"), disposition);
    }
}
