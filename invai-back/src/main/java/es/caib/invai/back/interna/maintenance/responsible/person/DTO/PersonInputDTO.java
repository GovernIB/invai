package es.caib.invai.back.interna.maintenance.responsible.person.DTO;

import es.caib.invai.back.utils.Constants;

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
    @NotBlank(message = "{" + Constants.VALIDATION_PERSON_FIRSTNAME_REQUIRED + "}")
    @Size(max = 150, message = "{" + Constants.VALIDATION_PERSON_FIRSTNAME_SIZE + "}")
    private String firstName;

    /** Person last name(s). */
    @NotBlank(message = "{" + Constants.VALIDATION_PERSON_LASTNAME_REQUIRED + "}")
    @Size(max = 150, message = "{" + Constants.VALIDATION_PERSON_LASTNAME_SIZE + "}")
    private String lastName;

    /** Contact e-mail address of the person. */
    @NotBlank(message = "{" + Constants.VALIDATION_PERSON_EMAIL_REQUIRED + "}")
    @Size(max = 150, message = "{" + Constants.VALIDATION_PERSON_EMAIL_SIZE + "}")
    private String email;

    /** Whether this person is CAIB staff (sourced via the future DIR3 integration), rather than an external company contact. */
    private boolean personalCaib;
}
