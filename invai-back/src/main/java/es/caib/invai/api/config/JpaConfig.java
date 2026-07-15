package es.caib.invai.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * Java Persistence API (JPA) and transaction management configuration.
 * Enables global transaction capability suitable for enterprise environments.
 *
 * @since 1.0.0
 */
@Configuration
@EnableTransactionManagement
public class JpaConfig {

    /**
     * Creates and configures the JPA Entity Manager Factory.
     * Uses the persistence unit named 'invaiDS'.
     *
     * @return the configured {@link LocalEntityManagerFactoryBean}
     */
    @Bean(name = "entityManagerFactory")
    public LocalEntityManagerFactoryBean entityManagerFactory() {
        LocalEntityManagerFactoryBean factoryBean = new LocalEntityManagerFactoryBean();
        factoryBean.setPersistenceUnitName("invaiDS");
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
            basePackages = "es.caib.invai.api.persistence.repository"
    )
    public static class RepositoriesInitializerConfiguration {
    }
}