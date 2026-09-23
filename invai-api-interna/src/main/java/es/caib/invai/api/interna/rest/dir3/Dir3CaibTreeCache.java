package es.caib.invai.api.interna.rest.dir3;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Caches the DIR3CAIB organizational unit tree rooted at the configured department code, refreshed
 * proactively on a fixed schedule and pre-warmed at startup, rather than lazily on request.
 * <p>
 * A background refresh is safe here — unlike in {@code invai-back}'s caller, where it previously
 * always failed with 401 — because {@link Dir3CaibClient} authenticates to DIR3CAIB with its own
 * fixed service credentials ({@code Dir3CaibConfig}), not a forwarded end-user token, so refreshing
 * never depends on an active user session.
 * </p>
 *
 * @since 1.0.4
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class Dir3CaibTreeCache {

    private static final boolean CO_OFFICIAL = true;
    private static final long REFRESH_RATE_MILLIS = 60 * 60 * 1000;

    private final Dir3CaibClient dir3CaibClient;

    /**
     * DIR3CAIB code of the single root this cache tracks; requests for any other root/denomination
     * combination bypass the cache and call {@link Dir3CaibClient} directly.
     */
    @Value("${es.caib.invai.dir3caib.root-adm-unit-code}")
    private String rootAdmUnitCode;

    private volatile List<UnidadRest> cachedTree = List.of();

    @PostConstruct
    void warmUpOnStartup() {
        refresh();
    }

    /**
     * Fetches a fresh tree snapshot for the configured root and stores it. Failures are logged and
     * swallowed rather than propagated, so a transient DIR3CAIB outage during a scheduled refresh
     * simply keeps serving the last known-good snapshot instead of taking the cache down.
     */
    @Scheduled(fixedRate = REFRESH_RATE_MILLIS)
    public void refresh() {
        try {
            log.debug("Refreshing cached DIR3CAIB organizational unit tree rooted at: {}", rootAdmUnitCode);
            cachedTree = dir3CaibClient.getTree(rootAdmUnitCode, CO_OFFICIAL);
        } catch (RuntimeException e) {
            log.warn("Failed to refresh cached DIR3CAIB tree; keeping previous snapshot", e);
        }
    }

    /**
     * Returns the cached tree when {@code rootCode}/{@code coOfficial} match the configured root
     * this cache tracks; otherwise falls through to a live, uncached {@link Dir3CaibClient} call.
     *
     * @param rootCode   the root unit's code
     * @param coOfficial whether to prefer the co-official denomination when one exists
     * @return every descendant unit (root included), or an empty list if the root itself is not found
     */
    public List<UnidadRest> getTree(String rootCode, boolean coOfficial) {
        if (rootAdmUnitCode.equals(rootCode) && coOfficial == CO_OFFICIAL) {
            return cachedTree;
        }
        return dir3CaibClient.getTree(rootCode, coOfficial);
    }
}
