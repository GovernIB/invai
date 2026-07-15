package es.caib.invai.api.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

/**
 * Technical security utility component providing static convenience operations to interface
 * with the active Spring Security engine framework.
 * <p>
 * Evaluates execution context parameters, inspects user principals, and decouples calling logic
 * from localized ThreadLocal configuration stores.
 * </p>
 *
 * @since 1.0.0
 */
public final class SecurityUtils {

    /** Fallback literal token used when operations run outside a valid authenticated user session context. */
    private static final String SYSTEM_USER = "SYSTEM_USER";

    /**
     * Suppresses default constructor instantiation routines to guarantee utility pattern isolation.
     */
    private SecurityUtils() {
    }

    /**
     * Resolves the identity metadata of the currently authenticated actor bound to the
     * active {@link SecurityContextHolder}.
     * <p>
     * If no security context exists, or if the runtime thread originates from asynchronous systems
     * (e.g., cron schedulers, system init routines), a default system identifier fallback is supplied.
     * </p>
     *
     * @return an {@link OidcUserInfo} instance if authenticated via OpenID Connect,
     * a {@link String} literal representing the principal identifier name,
     * or a default fallback string flag {@code "SYSTEM_USER"} if unauthenticated
     */
    public static Object getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return SYSTEM_USER;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof DefaultOidcUser) {
            return ((DefaultOidcUser) principal).getUserInfo();
        } else if (principal instanceof String) {
            return principal;
        }

        return SYSTEM_USER;
    }
}