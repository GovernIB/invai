package es.caib.invai.back.persistence.repository.maintenance.systems.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) capturing search filters used to build dynamic database queries
 * targeting host server infrastructure assets.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServerCriteria {

    /** Partial host name identification label token filter. */
    private String name;

    /** Foreign key filter matching the target deployment environment zone. */
    private Long environmentId;

    /** Foreign key filter matching the target server type lookup entry. */
    private Long serverTypeId;

    /** Discriminator code filter matching the target server type lookup entry (e.g. DATABASE, APPLICATION). */
    private String serverTypeCode;

    /** Master registry identity indicator managing active vs logical soft-deleted state lifecycles. */
    private Long statusId;

    /** Global text lookup variable cross-referencing the {@code name} column. */
    private String search;
}
