package es.caib.invai.api.interna.application.system_database.system.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object (DTO) capturing incoming payload attributes required to register or update
 * an Application to System linking asset record within the enterprise inventory framework.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppSystemInputDTO {

    /**
     * Foreign key unique identification pointer referencing the parent Application Information System Database grouping.
     */
    @NotNull(message = "{validation.applicationsystem.informationSystemDbId}")
    private Long informationSystemDbId;

    /**
     * Foreign key unique identification pointer referencing the target host infrastructure System profile.
     */
    @NotNull(message = "{validation.applicationsystem.system}")
    private Long systemId;
}
