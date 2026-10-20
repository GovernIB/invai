package es.caib.invai.back.interna.application.integration.connection.DTO;

import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.interna.maintenance.development.technology.DTO.TechnologyOutputDTO;
import es.caib.invai.back.interna.maintenance.integration.externalSystem.DTO.ExternalSystemOutputDTO;
import es.caib.invai.back.rest.soffid.SoffidRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for an integration
 * connection. {@code rolesMismatch} is never persisted - it's resolved live on every read by comparing
 * {@code requiredRoles} against whatever Soffid currently reports as granted to {@code username} (see
 * {@code AppIntegrationConnectionServiceFacadeBean}).
 *
 * @since 1.0.5
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppIntegrationConnectionOutputDTO {

    /** The unique data storage database auto-incremented primary key. */
    private Long id;

    /** Identifier of the integration anchor this connection belongs to. */
    private Long appIntegrationId;

    /** The other system, when it is an application already registered in the inventory. */
    private ApplicationOutputDTO application;

    /** The other system, when it is outside the inventory. */
    private ExternalSystemOutputDTO externalSystem;

    /** Technology used by this integration. */
    private TechnologyOutputDTO technology;

    /** Soffid username. */
    private String username;

    /** Roles required by this integration, persisted as a snapshot, in the same shape Soffid's own role search returns. */
    private List<SoffidRole> requiredRoles;

    /**
     * Roles Soffid currently reports as granted to {@link #username} - resolved live on every
     * read, never persisted. {@code null} if the live lookup itself failed (as opposed to an empty
     * list, which means the account genuinely holds none).
     */
    private List<SoffidRole> grantedRoles;

    /**
     * Whether {@link #grantedRoles} differs from {@link #requiredRoles} (missing a required one,
     * or carrying an extra one) - resolved live, never persisted. {@code null} if the live
     * granted-roles lookup itself failed.
     */
    private Boolean rolesMismatch;

    /** Timestamp at which this record was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
