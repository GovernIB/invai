package es.caib.invai.back.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JpaConfig}, verifying that {@code hibernate.show_sql}/{@code format_sql}
 * (which {@code persistence.xml} sets to {@code false}, as required in production) are only
 * re-enabled when the {@code local} Spring profile is active.
 */
@ExtendWith(MockitoExtension.class)
class JpaConfigTest {

    @Mock
    private Environment environment;

    private JpaConfig buildConfig() {
        JpaConfig config = new JpaConfig();
        ReflectionTestUtils.setField(config, "environment", environment);
        return config;
    }

    @Test
    void entityManagerFactory_localProfileActive_reEnablesShowSqlAndFormatSql() {
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);

        LocalEntityManagerFactoryBean factoryBean = buildConfig().entityManagerFactory();

        assertEquals("true", factoryBean.getJpaPropertyMap().get("hibernate.show_sql"));
        assertEquals("true", factoryBean.getJpaPropertyMap().get("hibernate.format_sql"));
    }

    @Test
    void entityManagerFactory_localProfileNotActive_leavesPersistenceXmlDefaultsUntouched() {
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);

        LocalEntityManagerFactoryBean factoryBean = buildConfig().entityManagerFactory();

        assertNull(factoryBean.getJpaPropertyMap().get("hibernate.show_sql"));
        assertNull(factoryBean.getJpaPropertyMap().get("hibernate.format_sql"));
    }
}
