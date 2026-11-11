package es.caib.invai.back.persistence.repository;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Verifies, for every {@code @Repository} adapter of the persistence layer, that a failure of the
 * underlying Spring Data repositories ({@link DataAccessException}) is propagated to the caller
 * by every public operation (the adapters log it and rethrow it).
 * <p>
 * Adapters are discovered by classpath scanning and wired reflectively: every Spring Data
 * repository collaborator throws on any call, and every other collaborator (mappers) answers
 * with mocks so that the operation reaches the failing repository call.
 * </p>
 */
class RepositoryAdaptersDataAccessErrorTest {

    private static final String BASE_PACKAGE = "es.caib.invai.back.persistence.repository";

    /**
     * Adapters/operations that do not follow the "log and rethrow" contract (soft-delete helpers with
     * their own audit flow); they are covered by their dedicated tests.
     */
    private static final Set<String> EXCLUDED = Set.of(
            "AppDocumentationAttachmentRepositoryAdapter", "AppDocumentationMetadataRepositoryAdapter",
            "AppLinkRepositoryAdapter", "AppDocumentationAttachmentVersionRepositoryAdapter", "AppEmailRepositoryAdapter");

    @TestFactory
    Stream<DynamicTest> everyAdapterOperationPropagatesDataAccessException() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(org.springframework.stereotype.Repository.class));
        List<DynamicTest> tests = new ArrayList<>();
        for (var definition : scanner.findCandidateComponents(BASE_PACKAGE)) {
            Class<?> adapterClass = Class.forName(definition.getBeanClassName());
            for (Method method : adapterClass.getDeclaredMethods()) {
                if (!Modifier.isPublic(method.getModifiers()) || method.isSynthetic()
                        || EXCLUDED.contains(adapterClass.getSimpleName())
                        || EXCLUDED.contains(adapterClass.getSimpleName() + "#" + method.getName())) {
                    continue;
                }
                tests.add(dynamicTest(adapterClass.getSimpleName() + "#" + method.getName(),
                        () -> assertTrue(propagatesDataAccessException(adapterClass, method),
                                "Expected DataAccessException from " + method)));
            }
        }
        return tests.stream();
    }

    private boolean propagatesDataAccessException(Class<?> adapterClass, Method method) throws Exception {
        Object adapter = adapterClass.getDeclaredConstructor().newInstance();
        for (Field field : adapterClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Autowired.class)) {
                continue;
            }
            field.setAccessible(true);
            Class<?> type = field.getType();
            if (Repository.class.isAssignableFrom(type)) {
                field.set(adapter, Mockito.mock(type, invocation -> {
                    throw new DataAccessResourceFailureException("database down");
                }));
            } else {
                field.set(adapter, Mockito.mock(type, Mockito.RETURNS_MOCKS));
            }
        }
        Object[] args = Stream.of(method.getParameterTypes()).map(RepositoryAdaptersDataAccessErrorTest::argumentFor).toArray();
        try {
            method.invoke(adapter, args);
            return false;
        } catch (InvocationTargetException e) {
            return e.getCause() instanceof DataAccessException;
        }
    }

    private static Object argumentFor(Class<?> type) {
        if (type == Long.class || type == long.class) {
            return 1L;
        }
        if (type == Integer.class || type == int.class) {
            return 1;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == byte[].class) {
            return new byte[0];
        }
        if (type == String.class) {
            return "x";
        }
        if (type == LocalDateTime.class) {
            return LocalDateTime.now();
        }
        if (type == LocalDate.class) {
            return LocalDate.now();
        }
        if (type == Pageable.class) {
            return Pageable.unpaged();
        }
        if (type == List.class || type == Collection.class) {
            return new ArrayList<>();
        }
        if (type == Set.class) {
            return Set.of();
        }
        return Mockito.mock(type);
    }
}
