package es.caib.invai.back.utils;

import es.caib.invai.back.exception.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.Objects;

/**
 * Global static utility catalog providing stateless operational routines for data sanitization
 * and core security context principal evaluation.
 *
 * @since 1.0.1
 */
@Slf4j
@Component
public final class Utils {

    /**
     * Iterates programmatically over all declared properties of any given object payload,
     * executing a `.trim()` sanitization sweep across all non-null String instances.
     *
     * @param target the target object instance payload to sanitize
     */
    public static void sanitize(Object target) {
        if (Objects.isNull(target)) {
            return;
        }

        Field[] fields = target.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (field.getType().equals(String.class)) {
                try {
                    field.setAccessible(true);
                    String value = (String) field.get(target);

                    if (Objects.nonNull(value)) {
                        field.set(target, value.trim());
                    }
                } catch (IllegalAccessException e) {
                    log.error("Sanitizer error: Failed to access field [{}] on target object [{}]",
                            field.getName(), target.getClass().getSimpleName(), e);
                }
            }
        }
    }

    /**
     * Extracts securely the primary credential identifier tracking the currently authenticated
     * session identity within active OIDC token claims context.
     *
     * @return the resolved username string, or system fallback indicator constants if missing
     */
    public static String resolveCurrentUsername() {
        Object currentUserObj = SecurityUtils.getCurrentUser();
        if (currentUserObj instanceof OidcUserInfo currentUser) {
            if (Objects.nonNull(currentUser.getClaims().get("preferred_username"))) {
                return (String) currentUser.getClaims().get("preferred_username");
            }
        } else if (currentUserObj instanceof String) {
            return (String) currentUserObj;
        }
        return Constants.SYSTEM_USER_FALLBACK;
    }

    /**
     * Waits for the given future, unwrapping {@link CompletionException} so the original runtime
     * exception thrown by the future's task (e.g., a {@code BusinessRuleException} raised by an
     * outbound call run via {@link CompletableFuture#supplyAsync}) propagates as-is instead of
     * being masked by the future's own wrapper.
     *
     * @param future the future to wait for
     * @param <T> the future's result type
     * @return the future's result
     */
    public static <T> T join(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException ex) {
            if (ex.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw ex;
        }
    }

    /**
     * Slices an in-memory row list into the requested page, for listings with no backing query to
     * paginate at the database level (e.g., built from a cached external directory, or filtered
     * catalog data).
     *
     * @param rows     the full, already-filtered row list
     * @param pageable the pagination parameters to apply
     * @param <T>      the row type
     * @return the requested page of rows
     */
    public static <T> Page<T> paginate(List<T> rows, Pageable pageable) {
        int start = (int) Math.min(pageable.getOffset(), rows.size());
        int end = Math.min(start + pageable.getPageSize(), rows.size());
        return new PageImpl<>(rows.subList(start, end), pageable, rows.size());
    }

    /**
     * Reads the bytes of an uploaded file.
     *
     * @param file the uploaded file
     * @return the bytes of the file
     * @throws BusinessRuleException if the file can't be read
     */
    public static byte[] readContent(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            log.error("Utils error: Failure reading the uploaded file", e);
            throw new BusinessRuleException(Constants.ERR_DOCUMENTATION_ATTACHMENT_FILE_UNREADABLE);
        }
    }

    /**
     * Gets the name of a file without the folders some browsers send along with it.
     *
     * @param fileName the name as sent by the client, may be {@code null}
     * @return the name without any folder, or {@code null} if there was none
     */
    public static String baseName(String fileName) {
        if (Objects.isNull(fileName)) {
            return null;
        }
        return fileName.substring(Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\')) + 1);
    }

    /**
     * Calculates the SHA-256 of the given bytes.
     *
     * @param content the bytes to digest
     * @return the digest in lower case hexadecimal
     */
    public static String sha256Hex(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

}