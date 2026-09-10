package es.caib.invai.back.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import java.util.Map;

/**
 * Java Persistence API (JPA) and transaction management configuration.
 * Enables global transaction capability suitable for enterprise environments.
 *
 * @since 1.0.1
 */
@Configuration
@EnableTransactionManagement
public class JpaConfig {

    /** Used to detect the active Spring profile, to re-enable SQL logging only for "local". */
    @Autowired
    private Environment environment;

    /**
     * Creates and configures the JPA Entity Manager Factory.
     * Uses the persistence unit named 'invaiDS'. {@code hibernate.show_sql}/{@code format_sql} are
     * {@code false} in {@code persistence.xml} (required in production per the JavaEE standard);
     * re-enabled here only when the {@code local} Spring profile is active, for developer
     * convenience.
     *
     * @return the configured {@link LocalEntityManagerFactoryBean}
     */
    @Bean(name = "entityManagerFactory")
    public LocalEntityManagerFactoryBean entityManagerFactory() {
        LocalEntityManagerFactoryBean factoryBean = new LocalEntityManagerFactoryBean();
        factoryBean.setPersistenceUnitName("invaiDS");
        if (environment.acceptsProfiles(Profiles.of("local"))) {
            factoryBean.setJpaPropertyMap(Map.of(
                    "hibernate.show_sql", "true",
                    "hibernate.format_sql", "true"
            ));
        }
        return factoryBean;
    }

    /**
     * Configures the transaction manager using JTA (Java Transaction API),
     * tailored for enterprise application server environments.
     *
     * @return the {@link PlatformTransactionManager} instance
     */
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager() {
        return new JtaTransactionManager();
    }

    /**
     * Inner configuration class to initialize and scan Spring Data JPA repositories.
     * Ensures repositories are bootstrap-loaded after the entity manager factory is ready.
     */
    @Configuration
    @DependsOn("entityManagerFactory")
    @EnableJpaRepositories(
            basePackages = "es.caib.invai.back.persistence"
    )
    public static class RepositoriesInitializerConfiguration {
    }
}