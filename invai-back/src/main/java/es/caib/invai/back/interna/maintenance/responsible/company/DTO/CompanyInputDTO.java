package es.caib.invai.back.interna.maintenance.responsible.company.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) defining the inbound API request payload structure
 * for registering or modifying Company resource data entries.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompanyInputDTO {

    /** Company corporate name label string. */
    @NotBlank(message = "{validation.company.name.required}")
    @Size(max = 150, message = "{validation.company.name.size}")
    private String name;
}
