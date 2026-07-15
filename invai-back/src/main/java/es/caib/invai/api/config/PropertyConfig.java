package es.caib.invai.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Profile;

/**
 * Conditional property file configuration wrapper depending on the active Spring profile.
 *
 * @since 1.0.0
 */
@Configuration
public class PropertyConfig {

    /**
     * Local environment property configuration.
     * Activated when passing the VM argument {@code -Dspring.profiles.active=local}.
     */
    @Configuration
    @Profile("local")
    @PropertySource(value = "file:../scripts/configuracio/invai-local.properties", encoding = "UTF-8")
    public static class LocalPropertyConfig {
    }

    /**
     * Server environment property configuration (Dev, Test, Prod).
     * Activated by default when the 'local' profile is NOT active.
     * Resolves the properties file path via the system variable {@code es.caib.invai.system.properties}.
     */
    @Configuration
    @Profile("!local")
    @PropertySource(value = "file:${es.caib.invai.system.properties}", encoding = "UTF-8")
    public static class ServerPropertyConfig {
    }
}