package es.caib.invai.back.ejb.application.data;

import es.caib.invai.back.persistence.repository.application.data.AppDataRepository;
import es.caib.invai.back.service.model.application.data.AppData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppDataUrlCacheService}, exercising the no-op guards (unchanged URL,
 * manual override in effect) and the actual persistence of a newly auto-resolved candidate.
 */
@ExtendWith(MockitoExtension.class)
class AppDataUrlCacheServiceTest {

    @Mock
    private AppDataRepository appDataRepository;

    @InjectMocks
    private AppDataUrlCacheService appDataUrlCacheService;

    private AppData appData;

    @BeforeEach
    void setUp() {
        appData = new AppData();
        appData.setId(1L);
    }

    @Test
    void cacheOpenDataUrl_newCandidate_persistsItWithoutTouchingUseFlag() {
        appData.setOpenDataUrl(null);
        appData.setUseOpenDataUrl(false);
        when(appDataRepository.update(appData, 1L)).thenReturn(appData);

        appDataUrlCacheService.cacheOpenDataUrl(appData, "https://intranet.caib.es/sedeibapi/externa/openapi.json");

        assertEquals("https://intranet.caib.es/sedeibapi/externa/openapi.json", appData.getOpenDataUrl());
        assertFalse(appData.isUseOpenDataUrl());
        verify(appDataRepository).update(appData, 1L);
    }

    @Test
    void cacheOpenDataUrl_sameCandidateAsAlreadyCached_isANoOp() {
        appData.setOpenDataUrl("https://intranet.caib.es/sedeibapi/externa/openapi.json");
        appData.setUseOpenDataUrl(false);

        appDataUrlCacheService.cacheOpenDataUrl(appData, "https://intranet.caib.es/sedeibapi/externa/openapi.json");

        verify(appDataRepository, never()).update(any(), any());
    }

    @Test
    void cacheOpenDataUrl_manualOverrideInEffect_isANoOpEvenIfCandidateDiffers() {
        appData.setOpenDataUrl("https://example.test/manual/openapi.json");
        appData.setUseOpenDataUrl(true);

        appDataUrlCacheService.cacheOpenDataUrl(appData, "https://intranet.caib.es/sedeibapi/externa/openapi.json");

        verify(appDataRepository, never()).update(any(), any());
    }

    @Test
    void cacheOpenDataUrl_candidateChangedFromPreviousCache_overwritesIt() {
        appData.setOpenDataUrl("https://intranet.caib.es/sedeibapi/externa/api-docs.json");
        appData.setUseOpenDataUrl(false);
        when(appDataRepository.update(appData, 1L)).thenReturn(appData);

        appDataUrlCacheService.cacheOpenDataUrl(appData, "https://intranet.caib.es/sedeibapi/externa/openapi.json");

        assertEquals("https://intranet.caib.es/sedeibapi/externa/openapi.json", appData.getOpenDataUrl());
        verify(appDataRepository).update(appData, 1L);
    }

    @Test
    void cacheReuseUrl_newCandidate_persistsItWithoutTouchingUseFlag() {
        appData.setReuseUrl(null);
        appData.setUseReuseUrl(false);
        when(appDataRepository.update(appData, 1L)).thenReturn(appData);

        appDataUrlCacheService.cacheReuseUrl(appData, "https://intranet.caib.es/sedeibapi/externa/swagger.json");

        assertEquals("https://intranet.caib.es/sedeibapi/externa/swagger.json", appData.getReuseUrl());
        assertFalse(appData.isUseReuseUrl());
        verify(appDataRepository).update(appData, 1L);
    }

    @Test
    void cacheReuseUrl_sameCandidateAsAlreadyCached_isANoOp() {
        appData.setReuseUrl("https://intranet.caib.es/sedeibapi/externa/swagger.json");
        appData.setUseReuseUrl(false);

        appDataUrlCacheService.cacheReuseUrl(appData, "https://intranet.caib.es/sedeibapi/externa/swagger.json");

        verify(appDataRepository, never()).update(any(), any());
    }

    @Test
    void cacheReuseUrl_manualOverrideInEffect_isANoOpEvenIfCandidateDiffers() {
        appData.setReuseUrl("https://example.test/manual/back");
        appData.setUseReuseUrl(true);

        appDataUrlCacheService.cacheReuseUrl(appData, "https://intranet.caib.es/sedeibapi/externa/swagger.json");

        verify(appDataRepository, never()).update(any(), any());
    }
}
