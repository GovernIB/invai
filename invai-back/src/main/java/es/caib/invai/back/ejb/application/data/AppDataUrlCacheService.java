package es.caib.invai.back.ejb.application.data;

import es.caib.invai.back.persistence.repository.application.data.AppDataRepository;
import es.caib.invai.back.service.model.application.data.AppData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Persists the URL that won {@code invai-api-interna}'s candidate race as a cache hint on {@code
 * AppData}, so the next call skips straight to it instead of racing candidates again - see {@code
 * AppDataServiceFacadeBean}. {@code useOpenDataUrl}/{@code useReuseUrl} are deliberately left
 * untouched (never set to {@code true}): that flag means "a person typed this in by hand", so it
 * must stay {@code false} for an auto-resolved URL, letting the caller tell the two apart.
 * <p>
 * Runs in its own {@link Propagation#REQUIRES_NEW} transaction because the caller ({@code
 * AppDataServiceFacadeBean#getById}) runs read-only - a plain {@code @Transactional} method on the
 * same class wouldn't do, since a same-class call bypasses Spring's transactional proxy entirely.
 * </p>
 *
 * @since 1.0.5
 */
@Service
@Slf4j
public class AppDataUrlCacheService {

    /** Repository used to persist the cached URL hint. */
    @Autowired
    private AppDataRepository appDataRepository;

    /**
     * Persists {@code resolvedUrl} as the cached "Open Data" candidate for the given anchor, if it
     * differs from what's already stored - a no-op otherwise, avoiding a redundant write (and audit
     * row) every time the same candidate keeps winning.
     *
     * @param appData     the anchor whose {@code openDataUrl} to update (not itself persisted - only
     *                    used to read the current value and the anchor's ID)
     * @param resolvedUrl the URL that won the race, to cache for the next call
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cacheOpenDataUrl(AppData appData, String resolvedUrl) {
        if (appData.isUseOpenDataUrl() || Objects.equals(resolvedUrl, appData.getOpenDataUrl())) {
            return;
        }
        log.info("Caching auto-resolved Open Data URL for application data anchor ID: {}", appData.getId());
        appData.setOpenDataUrl(resolvedUrl);
        appDataRepository.update(appData, appData.getId());
    }

    /**
     * Persists {@code resolvedUrl} as the cached "Reutilització" candidate for the given anchor, if
     * it differs from what's already stored - a no-op otherwise, avoiding a redundant write (and
     * audit row) every time the same candidate keeps winning.
     *
     * @param appData     the anchor whose {@code reuseUrl} to update (not itself persisted - only
     *                    used to read the current value and the anchor's ID)
     * @param resolvedUrl the URL that won the race, to cache for the next call
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cacheReuseUrl(AppData appData, String resolvedUrl) {
        if (appData.isUseReuseUrl() || Objects.equals(resolvedUrl, appData.getReuseUrl())) {
            return;
        }
        log.info("Caching auto-resolved Reutilització URL for application data anchor ID: {}", appData.getId());
        appData.setReuseUrl(resolvedUrl);
        appDataRepository.update(appData, appData.getId());
    }
}
