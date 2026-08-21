package es.caib.invai.back.interna.maintenance.development.layer.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) defining the inbound API request payload structure
 * for registering or modifying architecture Layer resource data entries.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LayerInputDTO {

    /** Layer name. */
    @NotBlank(message = "{validation.layer.name.required}")
    @Size(max = 100, message = "{validation.layer.name.size}")
    private String name;
}
