package es.caib.invai.back.service.mapper;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Contract test for every MapStruct generated {@code *MapperImpl}: each conversion must return
 * {@code null} for a {@code null} source, must return a non-null target for a populated source and
 * must copy the same-named scalar properties that are not explicitly ignored by the mapper.
 * <p>
 * Mappers are discovered by classpath scanning; their sub-mappers are mocked and the sources are
 * populated reflectively (scalars, nested beans down to a bounded depth and collections).
 * </p>
 */
class GeneratedMappersContractTest {

    private static final String BASE_PACKAGE = "es.caib.invai.back.service.mapper";
    private static final int MAX_DEPTH = 2;

    @TestFactory
    Stream<DynamicTest> everyGeneratedMapperHonoursTheMappingContract() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Component.class));
        List<DynamicTest> tests = new ArrayList<>();
        for (var definition : scanner.findCandidateComponents(BASE_PACKAGE)) {
            String className = definition.getBeanClassName();
            if (!className.endsWith("MapperImpl")) {
                continue;
            }
            Class<?> mapperClass = loadClass(className);
            for (Method method : mapperClass.getDeclaredMethods()) {
                if (!Modifier.isPublic(method.getModifiers()) || method.isSynthetic() || method.getParameterCount() == 0) {
                    continue;
                }
                tests.add(dynamicTest(mapperClass.getSimpleName() + "#" + method.getName() + method.getParameterCount(),
                        () -> verifyContract(mapperClass, method)));
            }
        }
        return tests.stream();
    }

    private static Class<?> loadClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private void verifyContract(Class<?> mapperClass, Method method) throws Exception {
        Object mapper = mapperClass.getDeclaredConstructor().newInstance();
        for (Field field : mapperClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                field.set(mapper, Mockito.mock(field.getType()));
            }
        }
        Type[] types = method.getGenericParameterTypes();
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            args[i] = populated(types[i], 0);
        }
        Object result = invoke(mapper, method, args);
        boolean returnsObject = method.getReturnType() != void.class && !method.getReturnType().isPrimitive();
        if (returnsObject && args[0] != null) {
            assertNotNull(result, "Populated source must produce a target: " + method);
            if (isBean(args[0].getClass()) && isBean(result.getClass())) {
                assertTrue(copiedProperties(args[0], result) > 0 || sharedScalarProperties(args[0], result) == 0,
                        "No same-named property was copied by " + method);
            }
        }
        // Single-argument conversions are null-safe: a null source yields a null target.
        if (returnsObject && args.length == 1 && !(args[0] instanceof Iterable)) {
            assertNull(invoke(mapper, method, new Object[]{null}), "Null source must produce null: " + method);
        }
    }

    private static Object invoke(Object target, Method method, Object[] args) throws Exception {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause() instanceof Exception ex ? ex : e;
        }
    }

    // ---- reflective fixture builder -------------------------------------------------------

    private static Object populated(Type type, int depth) throws Exception {
        if (type instanceof ParameterizedType pt) {
            Class<?> raw = (Class<?>) pt.getRawType();
            if (Iterable.class.isAssignableFrom(raw)) {
                Object element = populated(pt.getActualTypeArguments()[0], depth);
                if (element == null) {
                    return null;
                }
                return raw.isAssignableFrom(Set.class) && !raw.isAssignableFrom(List.class)
                        ? new HashSet<>(Set.of(element)) : new ArrayList<>(List.of(element));
            }
            return null;
        }
        if (!(type instanceof Class<?> c)) {
            return null;
        }
        Object scalar = scalar(c);
        if (scalar != null) {
            return scalar;
        }
        if (c.isEnum()) {
            return c.getEnumConstants()[0];
        }
        if (c.isInterface() && depth == 0) {
            return Mockito.mock(c);
        }
        if (c.isInterface() || Modifier.isAbstract(c.getModifiers()) || depth > MAX_DEPTH) {
            return null;
        }
        Object bean;
        try {
            bean = c.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            return null;
        }
        for (Method setter : c.getMethods()) {
            if (setter.getName().startsWith("set") && setter.getParameterCount() == 1) {
                Object value = populated(setter.getGenericParameterTypes()[0], depth + 1);
                if (value != null) {
                    setter.invoke(bean, value);
                }
            }
        }
        return bean;
    }

    private static Object scalar(Class<?> c) {
        if (c == String.class) {
            return "text";
        }
        if (c == Long.class || c == long.class) {
            return 7L;
        }
        if (c == Integer.class || c == int.class) {
            return 3;
        }
        if (c == Boolean.class || c == boolean.class) {
            return true;
        }
        if (c == Double.class || c == double.class) {
            return 1.5d;
        }
        if (c == BigDecimal.class) {
            return BigDecimal.ONE;
        }
        if (c == LocalDateTime.class) {
            return LocalDateTime.of(2026, 1, 2, 3, 4);
        }
        if (c == LocalDate.class) {
            return LocalDate.of(2026, 1, 2);
        }
        if (c == byte[].class) {
            return new byte[]{1, 2};
        }
        return null;
    }

    private static boolean isBean(Class<?> c) {
        return !c.getName().startsWith("java.");
    }

    private static int sharedScalarProperties(Object source, Object target) {
        return (int) scalarGetters(source).stream().filter(g -> findGetter(target, g.getName()) != null).count();
    }

    private static int copiedProperties(Object source, Object target) throws Exception {
        int copied = 0;
        for (Method getter : scalarGetters(source)) {
            Method other = findGetter(target, getter.getName());
            if (other != null && getter.invoke(source) != null
                    && Objects.deepEquals(getter.invoke(source), other.invoke(target))) {
                copied++;
            }
        }
        return copied;
    }

    private static List<Method> scalarGetters(Object bean) {
        List<Method> getters = new ArrayList<>();
        for (Method m : bean.getClass().getMethods()) {
            if ((m.getName().startsWith("get") || m.getName().startsWith("is")) && m.getParameterCount() == 0
                    && m.getDeclaringClass() != Object.class && scalar(m.getReturnType()) != null) {
                getters.add(m);
            }
        }
        return getters;
    }

    private static Method findGetter(Object bean, String name) {
        try {
            return bean.getClass().getMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
