package es.caib.invai.back.interna.maintenance.responsible.person.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) capturing incoming payload attributes required to register or update
 * a Person catalog record within the Manteniments / Responsables framework.
 *
 * @since 1.0.3
 */
@Getter
@Setter
public class PersonInputDTO {

    /**
     * Foreign key unique identification pointer referencing the target Company. Required unless
     * {@link #personalCaib} is {@code true}, in which case the person has no associated company.
     */
    private Long companyId;

    /** Person first name or identification label. */
    @NotBlank(message = "{validation.person.firstname.required}")
    @Size(max = 150, message = "{validation.person.firstname.size}")
    private String firstName;

    /** Person last name(s). */
    @NotBlank(message = "{validation.person.lastname.required}")
    @Size(max = 150, message = "{validation.person.lastname.size}")
    private String lastName;

    /** Contact e-mail address of the person. */
    @NotBlank(message = "{validation.person.email.required}")
    @Size(max = 150, message = "{validation.person.email.size}")
    private String email;

    /** Whether this person is CAIB staff (sourced via the future DIR3 integration), rather than an external company contact. */
    private boolean personalCaib;
}
