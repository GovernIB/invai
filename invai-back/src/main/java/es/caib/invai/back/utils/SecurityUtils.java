package es.caib.invai.back.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Technical security utility component providing static convenience operations to interface
 * with the active Spring Security engine framework.
 * <p>
 * Evaluates execution context parameters, inspects user principals, and decouples calling logic
 * from localized ThreadLocal configuration stores.
 * </p>
 *
 * @since 1.0.1
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityUtils {

    /** Fallback literal token used when operations run outside a valid authenticated user session context. */
    private static final String SYSTEM_USER = "SYSTEM_USER";

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

        if (Objects.isNull(authentication) || !authentication.isAuthenticated()) {
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

    /**
     * Tells whether the user bound to the current security context satisfies a security expression
     * like the ones {@code @PreAuthorize} takes (e.g. {@link Constants#PRE_AUTH_SECURITY}).
     *
     * @param expression the security expression, made of {@code hasRole} and {@code hasAnyRole}
     * @return {@code true} if the user satisfies it; {@code false} if they don't or nobody is logged in
     */
    public static boolean hasAccess(String expression) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return false;
        }
        SecurityExpressionRoot root = new SecurityExpressionRoot(authentication) { };
        return Boolean.TRUE.equals(new SpelExpressionParser().parseExpression(expression)
                .getValue(new StandardEvaluationContext(root), Boolean.class));
    }

    /**
     * Collects the authorities of the user bound to the current security context.
     *
     * @return the authorities, empty if nobody is logged in
     */
    public static Set<String> getCurrentAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return Set.of();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}
