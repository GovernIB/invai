package es.caib.invai.back.interna.application.system_database.system.DTO;

import es.caib.invai.back.utils.Constants;

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
    @NotNull(message = "{" + Constants.VALIDATION_APPLICATIONSYSTEM_INFORMATION_SYSTEM_DB + "}")
    private Long informationSystemDbId;

    /**
     * Foreign key unique identification pointer referencing the target host infrastructure System profile.
     */
    @NotNull(message = "{" + Constants.VALIDATION_APPLICATIONSYSTEM_SYSTEM + "}")
    private Long systemId;
}
