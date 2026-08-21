package es.caib.invai.back.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link LocaleConfig}, verifying the custom {@link LocaleResolver}'s branching
 * logic: preferring Catalan when the request's {@code Referer} header points to a "/ca/" path,
 * and otherwise delegating to the standard Accept-Language based resolution with a Catalan default.
 */
class LocaleConfigTest {

    private final LocaleConfig localeConfig = new LocaleConfig();

    @Test
    void resolveLocale_refererContainingCaSegment_returnsCatalan() {
        LocaleResolver resolver = localeConfig.localeResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Referer", "https://invai.example.com/ca/aplicacions");

        Locale resolved = resolver.resolveLocale(request);

        assertEquals(new Locale("ca"), resolved);
    }

    @Test
    void resolveLocale_refererEndingWithCa_returnsCatalan() {
        LocaleResolver resolver = localeConfig.localeResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Referer", "https://invai.example.com/ca");

        Locale resolved = resolver.resolveLocale(request);

        assertEquals(new Locale("ca"), resolved);
    }

    @Test
    void resolveLocale_refererPointingToSpanishPath_fallsBackToAcceptHeaderResolution() {
        LocaleResolver resolver = localeConfig.localeResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Referer", "https://invai.example.com/es/aplicacions");
        request.addPreferredLocale(new Locale("es"));

        Locale resolved = resolver.resolveLocale(request);

        assertEquals(new Locale("es"), resolved);
    }

    @Test
    void resolveLocale_noReferer_fallsBackToAcceptHeaderResolutionWithCatalanDefault() {
        LocaleResolver resolver = localeConfig.localeResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();
        // No Referer header and no Accept-Language -> AcceptHeaderLocaleResolver falls back
        // to the configured default locale (Catalan).

        Locale resolved = resolver.resolveLocale(request);

        assertEquals(new Locale("ca"), resolved);
    }

    @Test
    void resolveLocale_unsupportedAcceptLanguage_fallsBackToDefaultCatalan() {
        LocaleResolver resolver = localeConfig.localeResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addPreferredLocale(Locale.FRENCH);

        Locale resolved = resolver.resolveLocale(request);

        assertEquals(new Locale("ca"), resolved);
    }
}
