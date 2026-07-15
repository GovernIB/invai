package es.caib.invai.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Locale;

/**
 * Internationalization (i18n) and localization configuration.
 * Configures strict supported locales to ensure proper resolution constraints.
 *
 * @since 1.0.0
 */
@Configuration
public class LocaleConfig {

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