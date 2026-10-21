package es.caib.invai.back.utils;

import es.caib.invai.back.exception.BusinessRuleException;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link Utils}, covering its reflective field sanitization routine and its
 * current-user resolution logic across every {@link SecurityUtils#getCurrentUser()} outcome.
 */
class UtilsTest {

    @Getter
    @Setter
    private static class SanitizeTarget {
        private String name;
        private String description;
        private Long id;
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sanitize_null_doesNothing() {
        Utils.sanitize(null);
    }

    @Test
    void sanitize_trimsAllStringFieldsAndLeavesOthersUntouched() {
        SanitizeTarget target = new SanitizeTarget();
        target.setName("  padded name  ");
        target.setDescription(null);
        target.setId(42L);

        Utils.sanitize(target);

        assertEquals("padded name", target.getName());
        assertNull(target.getDescription());
        assertEquals(42L, target.getId());
    }

    @Test
    void resolveCurrentUsername_unauthenticated_returnsSystemFallback() {
        SecurityContextHolder.clearContext();

        String result = Utils.resolveCurrentUsername();

        assertEquals("SYSTEM_USER", result);
    }

    @Test
    void resolveCurrentUsername_stringPrincipal_returnsPrincipalValue() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "jdoe", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        String result = Utils.resolveCurrentUsername();

        assertEquals("jdoe", result);
    }

    @Test
    void resolveCurrentUsername_oidcPrincipalWithPreferredUsername_returnsClaimValue() {
        OidcIdToken idToken = new OidcIdToken("token-value", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("sub", "user-1"));
        OidcUserInfo userInfo = new OidcUserInfo(Map.of("preferred_username", "mmartin"));
        DefaultOidcUser oidcUser = new DefaultOidcUser(
                List.of(new SimpleGrantedAuthority("ROLE_USER")), idToken, userInfo);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                oidcUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        String result = Utils.resolveCurrentUsername();

        assertEquals("mmartin", result);
    }

    @Test
    void join_completedFuture_returnsResult() {
        CompletableFuture<String> future = CompletableFuture.completedFuture("value");

        assertEquals("value", Utils.join(future));
    }

    @Test
    void join_futureFailsWithRuntimeException_unwrapsAndRethrowsOriginal() {
        IllegalStateException original = new IllegalStateException("boom");
        CompletableFuture<String> future = CompletableFuture.failedFuture(original);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> Utils.join(future));
        assertEquals(original, thrown);
    }

    @Test
    void join_futureFailsWithCheckedException_rethrowsCompletionException() {
        CompletableFuture<String> future = CompletableFuture.failedFuture(new Exception("checked failure"));

        assertThrows(CompletionException.class, () -> Utils.join(future));
    }

    // --- readContent ---

    @Test
    void readContent_returnsTheBytesOfTheUploadedFile() {
        byte[] bytes = "%PDF-1.7".getBytes(StandardCharsets.UTF_8);

        assertArrayEquals(bytes, Utils.readContent(new MockMultipartFile("file", "a.pdf", "application/pdf", bytes)));
    }

    @Test
    void readContent_fileThatCannotBeRead_throwsBusinessRuleException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException("disk failure"));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> Utils.readContent(file));

        assertEquals(Constants.ERR_DOCUMENTATION_ATTACHMENT_FILE_UNREADABLE, ex.getMessage());
    }

    // --- baseName ---

    @Test
    void baseName_nullName_returnsNull() {
        assertNull(Utils.baseName(null));
    }

    @Test
    void baseName_plainName_isKept() {
        assertEquals("manual.pdf", Utils.baseName("manual.pdf"));
    }

    @Test
    void baseName_dropsUnixFolders() {
        assertEquals("manual.pdf", Utils.baseName("/home/user/docs/manual.pdf"));
    }

    @Test
    void baseName_dropsWindowsFolders() {
        assertEquals("Manual.PDF", Utils.baseName("C:\\docs\\Manual.PDF"));
    }

    // --- sha256Hex ---

    @Test
    void sha256Hex_isTheLowerCaseHexDigestOfTheBytes() {
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
                Utils.sha256Hex("hello".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void sha256Hex_sameContentGivesTheSameDigestAndDifferentContentDoesNot() {
        byte[] a = "a".getBytes(StandardCharsets.UTF_8);

        assertEquals(Utils.sha256Hex(a), Utils.sha256Hex("a".getBytes(StandardCharsets.UTF_8)));
        assertNotEquals(Utils.sha256Hex(a), Utils.sha256Hex("b".getBytes(StandardCharsets.UTF_8)));
        assertEquals(64, Utils.sha256Hex(a).length());
    }
}
