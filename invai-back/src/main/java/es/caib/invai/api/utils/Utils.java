package es.caib.invai.api.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * Global static utility catalog providing stateless operational routines for data sanitization
 * and core security context principal evaluation.
 *
 * @since 1.0.0
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
        if (target == null) {
            return;
        }

        Field[] fields = target.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (field.getType().equals(String.class)) {
                try {
                    field.setAccessible(true);
                    String value = (String) field.get(target);

                    if (value != null) {
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
        if (currentUserObj instanceof OidcUserInfo) {
            OidcUserInfo currentUser = (OidcUserInfo) currentUserObj;
            if (currentUser.getClaims().get("preferred_username") != null) {
                return (String) currentUser.getClaims().get("preferred_username");
            }
        } else if (currentUserObj instanceof String) {
            return (String) currentUserObj;
        }
        return Constants.SYSTEM_USER_FALLBACK;
    }
}