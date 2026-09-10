package es.caib.invai.back.persistence.repository.maintenance.security.identityProvider;

import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object (DTO) capturing search filters and evaluation metrics used
 * to build dynamic database queries targeting the IdentityProvider registry ecosystem.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IdentityProviderCriteria {

    /** Identity provider descriptive label used to filter results (partial, case-insensitive match). */
    private String name;

    /**
     * Master registry identity indicator managing active vs logical soft-deleted runtime ecosystem state lifecycles.
     *
     * @see StatusEnum
     */
    private Long statusId;

    /**
     * Global text lookup variable cross-referencing keywords concurrently across the {@code name}
     * column inside the programmatic criteria context.
     */
    private String search;
}
