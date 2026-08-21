package es.caib.invai.back.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Locale;

/**
 * Internationalization (i18n) and localization configuration.
 * Configures strict supported locales to ensure proper resolution constraints.
 *
 * @since 1.0.1
 */
@Configuration
public class LocaleConfig {

    /**
     * Explicitly declares the validation/exception message bundle, rather than relying on Spring Boot's
     * {@code MessageSourceAutoConfiguration}. Under WAR deployment on an external servlet container
     * (JBoss/WildFly), that autoconfiguration's classpath-scanning condition can fail to detect the
     * {@code messages_*.properties} bundle depending on the module's classloader isolation, silently
     * falling back to an empty {@link MessageSource} that can never resolve any key - even though the
     * bundle files are present and correct on the deployed classpath. Declaring the bean explicitly
     * removes that ambiguity.
     *
     * @return the configured {@link MessageSource} backed by the {@code messages} resource bundle
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(false);
        return messageSource;
    }

    /**
     * Resolves the user locale using the incoming HTTP 'Accept-Language' header.
     * Enforces strict match restrictions against registered locales, falling back
     * to Catalan ('ca') as system baseline if unmatched or missing.
     *
     * @return the configured {@link LocaleResolver}
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver lr = new AcceptHeaderLocaleResolver() {
            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                String referer = request.getHeader("Referer");
                if (referer != null && (referer.contains("/ca/") || referer.endsWith("/ca"))) {
                    return new Locale("ca");
                }

                return super.resolveLocale(request);
            }
        };

        Locale catalan = new Locale("ca");
        Locale spanish = new Locale("es");

        lr.setSupportedLocales(Arrays.asList(catalan, spanish));
        lr.setDefaultLocale(catalan);

        return lr;
    }
}