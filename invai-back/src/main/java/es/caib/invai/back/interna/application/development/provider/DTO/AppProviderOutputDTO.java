package es.caib.invai.back.interna.application.development.provider.DTO;

import es.caib.invai.back.interna.maintenance.development.role.DTO.RoleOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Outbound transport payload detailing a provider assignment linked to a development module.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppProviderOutputDTO {

    /** Unique identifier. */
    private Long id;
    /** Name of the provider company. */
    private String companyName;
    /** Role held by the provider on this development. */
    private RoleOutputDTO role;
    /** Date the provider's engagement started. */
    private LocalDateTime startDate;
    /** Date the provider's engagement expires, or {@code null} if open-ended. */
    private LocalDateTime expireDate;
    /** Timestamp when this record was soft-deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
