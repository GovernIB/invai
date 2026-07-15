package es.caib.invai.api.service.model;

import lombok.Getter;

/**
 * Type-safe domain enumeration defining core structural runtime lifecycle constraints
 * allowed across system catalog records.
 *
 * @since 1.0.0
 */
@Getter
public enum StatusEnum {

    /** Asset configuration state is active, discoverable, and dynamically editable. */
    ACTIVE(1L),

    /** Asset state is suspended or out of commission, skipping dynamic indexing loops. */
    INACTIVE(2L);

    /** Persistent primary key numerical identifier matching the relational status code map. */
    private final Long id;

    /**
     * Internal constructor setting explicit primary sequence links for database enum translations.
     *
     * @param id primary tracking index sequence value mapping database dictionary rows
     */
    StatusEnum(Long id) {
        this.id = id;
    }
}