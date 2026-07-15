package es.caib.invai.api.interna.commission.DTO;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) capturing the incoming presentation layer delta payload properties
 * required to register or modify corporate working governance Commission oversight panel profiles.
 * <p>
 * Restricts text length using constraint parameters routed directly to localization property definitions.
 * </p>
 *
 * @since 1.0.0
 */
@Getter
@Setter
public class CommissionInputDTO {

    /**
     * Official regionalized naming identifier tracking the governance committee group (typically in Catalan).
     * Text sequence cannot be left empty and is constrained to a maximum allocation boundary of 100 characters.
     */
    @NotBlank(message = "{validation.commission.name.required}")
    @Size(max = 100, message = "{validation.commission.name.size}")
    private String name;

    /**
     * Translated variant corporate descriptive layout naming string mapping explicitly to Spanish locale sets.
     * Text sequence cannot be left empty and is constrained to a maximum allocation boundary of 100 characters.
     */
    @NotBlank(message = "{validation.commission.nameEs.required}")
    @Size(max = 100, message = "{validation.commission.nameEs.size}")
    private String nameEs;
}