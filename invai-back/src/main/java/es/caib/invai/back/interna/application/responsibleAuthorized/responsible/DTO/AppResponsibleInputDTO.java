package es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO;

import es.caib.invai.back.utils.Constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

/**
 * Inbound payload for creating or updating a responsible assignment, including, when the person
 * has no local record yet, the inline name/e-mail fields used to resolve or create one.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppResponsibleInputDTO {

    /** Foreign key unique identification pointer referencing the parent Responsables tab anchor. */
    @NotNull(message = "{" + Constants.VALIDATION_APPRESPONSIBLE_APP_RESPONSIBLE_AUTHORIZED_ID + "}")
    private Long appResponsibleAuthorizedId;

    /**
     * Foreign key unique identification pointer referencing the responsible person. Optional: when
     * {@code null}, {@link #personFirstName}, {@link #personLastName} and {@link #personEmail} are
     * used instead to resolve an existing person by email or create a new one (see the facade for
     * the exact rule).
     */
    private Long personId;

    /** Person's first name, used only when {@link #personId} is {@code null} to create a new person. */
    private String personFirstName;

    /** Person's last name(s), used only when {@link #personId} is {@code null} to create a new person. */
    private String personLastName;

    /** Person's email, used only when {@link #personId} is {@code null} to resolve-or-create a person. */
    private String personEmail;

    /**
     * Foreign key referencing the person's company, used only when {@link #personId} is
     * {@code null} and {@link #personalCaib} is {@code false}: an external person cannot be
     * created without one.
     */
    private Long companyId;

    /** Foreign key unique identification pointer referencing the responsibility type held. */
    @NotNull(message = "{" + Constants.VALIDATION_APPRESPONSIBLE_RESPONSIBLE_TYPE_ID + "}")
    private Long responsibleTypeId;

    /** Free-text job title/position held by the responsible person for this assignment. */
    private String jobTitle;

    /** Free-text remarks about this responsible assignment. Optional; editable both on create and on update. */
    private String observation;

    /**
     * Whether the linked person is CAIB internal staff. Kept in sync with the linked
     * {@code Person} record on both create and update: if it differs from what is currently
     * stored, the person's own {@code isPersonalCaib} flag is updated to match.
     */
    private boolean personalCaib;
}
