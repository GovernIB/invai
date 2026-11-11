package es.caib.invai.back.interna;

import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Guards the role of every endpoint of {@code invai-back}: each controller must be secured, and the
 * roles that really reach each endpoint (the {@code @PreAuthorize} of the method or, failing that,
 * of its class) must be the ones this test lists per area.
 * The login controllers are the only ones outside the roles.
 */
class ControllerSecurityTest {

    private static final String BASE_PACKAGE = "es.caib.invai.back.interna";
    private static final Set<String> LOGIN_PACKAGES = Set.of("auth");

    private static final Set<String> ALL = Set.of(Constants.ALL_ROLES);
    private static final Set<String> SUPER = Set.of(Constants.ROLE_SUPER);
    private static final Set<String> SYSTEMS = Set.of(Constants.ROLE_SUPER, Constants.ROLE_SISTEMES);
    private static final Set<String> INTEGRATION = Set.of(Constants.ROLE_SUPER, Constants.ROLE_WS);
    private static final Set<String> DEVELOPMENT = Set.of(Constants.ROLE_SUPER, Constants.ROLE_DESENVO);
    private static final Set<String> SECURITY = Set.of(Constants.ROLE_SUPER, Constants.ROLE_SEGURETAT);
    private static final Set<String> DATA = Set.of(Constants.ROLE_SUPER, Constants.ROLE_DADES);
    private static final Set<String> ACCESSIBILITY = Set.of(Constants.ROLE_SUPER, Constants.ROLE_ACCESIBILITAT);
    private static final Set<String> RESPONSIBLE_WRITE = Set.of(Constants.ROLE_SUPER, Constants.ROLE_SEGURETAT, Constants.ROLE_SISTEMES);
    private static final Set<String> RESPONSIBLE_READ = Set.of(Constants.ROLE_SUPER, Constants.ROLE_SISTEMES, Constants.ROLE_DESENVO,
            Constants.ROLE_SEGURETAT, Constants.ROLE_DADES, Constants.ROLE_ACCESIBILITAT);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static List<Class<?>> controllers() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        List<Class<?>> controllers = new ArrayList<>();
        for (BeanDefinition definition : scanner.findCandidateComponents(BASE_PACKAGE)) {
            controllers.add(Class.forName(definition.getBeanClassName()));
        }
        return controllers;
    }

    private static boolean isLogin(Class<?> controller) {
        String area = controller.getPackageName().substring(BASE_PACKAGE.length() + 1);
        return LOGIN_PACKAGES.contains(area);
    }

    private static List<Method> endpoints(Class<?> controller) {
        List<Method> endpoints = new ArrayList<>();
        for (Method method : controller.getDeclaredMethods()) {
            if (AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class)) {
                endpoints.add(method);
            }
        }
        return endpoints;
    }

    /** The roles each endpoint is meant for. */
    private static Set<String> expectedRoles(Class<?> controller, Method endpoint) {
        String area = controller.getPackageName().substring(BASE_PACKAGE.length() + 1);
        String name = endpoint.getName();
        if (area.startsWith("maintenance.")) {
            if (!reads(endpoint) || area.equals("maintenance.responsible.roleTransfer")) {
                return SUPER;
            }
            return area.equals("maintenance.responsible.person") ? RESPONSIBLE_WRITE : ALL;
        }
        if (area.equals("application.core")) {
            return switch (name) {
                case "getAll", "getById" -> ALL;
                case "assignSoffidRoles" -> SECURITY;
                default -> SUPER;
            };
        }
        if (area.equals("application.dir3")) {
            return name.equals("startDir3MismatchAudit") ? SUPER : ALL;
        }
        if (area.startsWith("application.documentation") || area.startsWith("catalog")) {
            return ALL;
        }
        if (area.equals("application.responsibleAuthorized.responsible") || area.equals("application.responsibleAuthorized.authorized")) {
            return name.equals("getAllByAppResponsibleAuthorizedId") ? RESPONSIBLE_READ : RESPONSIBLE_WRITE;
        }
        if (area.equals("application.responsibleAuthorized.dir3")) {
            return RESPONSIBLE_WRITE;
        }
        if (area.startsWith("application.system_database")) {
            return SYSTEMS;
        }
        if (area.startsWith("application.integration")) {
            return INTEGRATION;
        }
        if (area.startsWith("application.development")) {
            return DEVELOPMENT;
        }
        if (area.startsWith("application.security")) {
            return SECURITY;
        }
        if (area.equals("application.data")) {
            return DATA;
        }
        if (area.startsWith("application.accessibility")) {
            return ACCESSIBILITY;
        }
        return fail("No roles listed for " + controller.getName() + ": add its area to this test");
    }

    private static boolean reads(Method endpoint) {
        RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(endpoint, RequestMapping.class);
        return mapping != null && java.util.Arrays.asList(mapping.method()).contains(org.springframework.web.bind.annotation.RequestMethod.GET);
    }

    private static String effectiveExpression(Class<?> controller, Method endpoint) {
        PreAuthorize annotation = AnnotatedElementUtils.findMergedAnnotation(endpoint, PreAuthorize.class);
        if (annotation == null) {
            annotation = AnnotatedElementUtils.findMergedAnnotation(controller, PreAuthorize.class);
        }
        assertNotNull(annotation, endpoint + " has no @PreAuthorize, neither does its class");
        return annotation.value();
    }

    private boolean allows(String expression, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "jdoe", null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
        return SecurityUtils.hasAccess(expression);
    }

    @Test
    void preAuthorizeConstants_areOpenToExactlyTheirRoles() {
        assertConstantAllows(Constants.PRE_AUTH_SUPER, SUPER);
        assertConstantAllows(Constants.PRE_AUTH_ANY_ROLE, ALL);
        assertConstantAllows(Constants.PRE_AUTH_SYSTEMS, SYSTEMS);
        assertConstantAllows(Constants.PRE_AUTH_INTEGRATION, INTEGRATION);
        assertConstantAllows(Constants.PRE_AUTH_DEVELOPMENT, DEVELOPMENT);
        assertConstantAllows(Constants.PRE_AUTH_SECURITY, SECURITY);
        assertConstantAllows(Constants.PRE_AUTH_DATA, DATA);
        assertConstantAllows(Constants.PRE_AUTH_ACCESSIBILITY, ACCESSIBILITY);
        assertConstantAllows(Constants.PRE_AUTH_RESPONSIBLE_WRITE, RESPONSIBLE_WRITE);
        assertConstantAllows(Constants.PRE_AUTH_RESPONSIBLE_READ, RESPONSIBLE_READ);
    }

    private void assertConstantAllows(String expression, Set<String> expected) {
        for (String role : Constants.ALL_ROLES) {
            assertEquals(expected.contains(role), allows(expression, role), expression + " for " + role);
        }
        assertFalse(allows(expression, "OTHER_ROLE"), expression + " for a foreign role");
    }

    @Test
    void everyController_isSecuredAtClassLevelExceptTheLoginOnes() throws ClassNotFoundException {
        List<Class<?>> controllers = controllers();
        assertFalse(controllers.isEmpty());
        for (Class<?> controller : controllers) {
            boolean secured = AnnotatedElementUtils.findMergedAnnotation(controller, PreAuthorize.class) != null;
            assertEquals(!isLogin(controller), secured, controller.getName());
        }
    }

    @Test
    void everyEndpoint_isOpenToExactlyTheRolesOfItsArea() throws ClassNotFoundException {
        int checked = 0;
        for (Class<?> controller : controllers()) {
            if (isLogin(controller)) {
                continue;
            }
            for (Method endpoint : endpoints(controller)) {
                String expression = effectiveExpression(controller, endpoint);
                Set<String> expected = expectedRoles(controller, endpoint);
                for (String role : Constants.ALL_ROLES) {
                    assertEquals(expected.contains(role), allows(expression, role),
                            controller.getSimpleName() + "#" + endpoint.getName() + " for " + role);
                }
                assertFalse(allows(expression, "OTHER_ROLE"), controller.getSimpleName() + "#" + endpoint.getName() + " for a foreign role");
                checked++;
            }
        }
        assertEquals(true, checked > 250, "endpoints checked: " + checked);
    }
}
