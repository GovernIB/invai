package es.caib.invai.api.interna.application.development.provider.DTO;

import es.caib.invai.api.interna.maintenance.role.DTO.RoleOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
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
public class AppProviderOutputDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String companyName;
    private RoleOutputDTO role;
    private LocalDateTime startDate;
    private LocalDateTime expireDate;
    private LocalDateTime deletedAt;
}
