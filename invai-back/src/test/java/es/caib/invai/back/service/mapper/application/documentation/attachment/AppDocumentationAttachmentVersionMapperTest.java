package es.caib.invai.back.service.mapper.application.documentation.attachment;

import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentVersionInputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentInputDTO;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachmentVersion;
import es.caib.invai.back.service.model.catalog.mimeType.MimeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the methods of {@link AppDocumentationAttachmentVersionMapper} that build an attachment
 * from its input and the uploaded file: what comes from the input, from the file and from its
 * content.
 */
class AppDocumentationAttachmentVersionMapperTest {

    /** SHA-256 of the ASCII text {@code hello}. */
    private static final String HELLO_SHA256 = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";

    private static final byte[] HELLO = "hello".getBytes(StandardCharsets.UTF_8);

    private AppDocumentationAttachmentVersionMapper mapper;
    private MimeType pdf;

    @BeforeEach
    void setUp() {
        mapper = new AppDocumentationAttachmentVersionMapperImpl();
        pdf = new MimeType();
        pdf.setId(1L);
        pdf.setExtension("pdf");
        pdf.setMimeType("application/pdf");
        pdf.setName("PDF");
    }

    private static MockMultipartFile file(String name) {
        return new MockMultipartFile("file", name, "application/octet-stream", HELLO);
    }

    @Test
    void fromANewDocument_takesTheVersionAndTheNoteFromTheInputAndTheRestFromTheFile() {
        AppDocumentationAttachmentInputDTO inputDTO = new AppDocumentationAttachmentInputDTO(3L, "Manual", 7L, 5L, 9L, "1.0", "first", null);

        AppDocumentationAttachmentVersion attachment = mapper.toModelFromInput(inputDTO, 10L, file("C:\\docs\\Manual.PDF"), pdf, HELLO);

        assertNull(attachment.getId());
        assertEquals(10L, attachment.getAppDocumentationAttachmentId());
        assertEquals("1.0", attachment.getVersionLabel());
        assertEquals("first", attachment.getChangeNote());
        assertEquals("Manual.PDF", attachment.getFileName());
        assertSame(pdf, attachment.getMimeType());
        assertEquals(HELLO.length, attachment.getFileSize());
        assertEquals(HELLO_SHA256, attachment.getChecksumSha256());
        assertTrue(attachment.isCurrent());
        assertNull(attachment.getCreatedAt());
        assertNull(attachment.getDeletedAt());
    }

    @Test
    void fromANewDocument_aBlankChangeNoteBecomesNull() {
        AppDocumentationAttachmentInputDTO inputDTO = new AppDocumentationAttachmentInputDTO(3L, "Manual", 7L, 5L, 9L, "1.0", "   ", null);

        assertNull(mapper.toModelFromInput(inputDTO, 10L, file("manual.pdf"), pdf, HELLO).getChangeNote());
    }

    @Test
    void fromANewAttachment_takesTheVersionAndTheNoteFromTheInputAndTheRestFromTheFile() {
        AppDocumentationAttachmentVersionInputDTO inputDTO = new AppDocumentationAttachmentVersionInputDTO("2.0", "  second  ", null);

        AppDocumentationAttachmentVersion attachment = mapper.toModelFromInput(inputDTO, 10L, file("/home/user/manual-v2.pdf"), pdf, HELLO);

        assertNull(attachment.getId());
        assertEquals(10L, attachment.getAppDocumentationAttachmentId());
        assertEquals("2.0", attachment.getVersionLabel());
        assertEquals("second", attachment.getChangeNote());
        assertEquals("manual-v2.pdf", attachment.getFileName());
        assertSame(pdf, attachment.getMimeType());
        assertEquals(HELLO.length, attachment.getFileSize());
        assertEquals(HELLO_SHA256, attachment.getChecksumSha256());
        assertTrue(attachment.isCurrent());
    }

    @Test
    void fromANewAttachment_noChangeNoteStaysNull() {
        AppDocumentationAttachmentVersionInputDTO inputDTO = new AppDocumentationAttachmentVersionInputDTO("2.0", null, null);

        assertNull(mapper.toModelFromInput(inputDTO, 10L, file("manual.pdf"), pdf, HELLO).getChangeNote());
    }
}
