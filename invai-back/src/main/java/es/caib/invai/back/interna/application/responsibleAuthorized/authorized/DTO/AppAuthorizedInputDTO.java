package es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO;

import es.caib.invai.back.utils.Constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Inbound payload for creating or updating an authorized-person assignment, including the
 * multi-select list of authorization type identifiers to attach to it and, when the person has
 * no local record yet, the inline name/e-mail fields used to resolve or create one.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppAuthorizedInputDTO {

    /** Foreign key unique identification pointer referencing the parent Responsables tab anchor (AppResponsibleAuthorized). */
    @NotNull(message = "{" + Constants.VALIDATION_APPAUTHORIZED_APP_RESPONSIBLE_AUTHORIZED_ID + "}")
    private Long appResponsibleAuthorizedId;

    /**
     * Foreign key unique identification pointer referencing the authorized person. Optional: when
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

    /** List of authorization type catalog identifiers to attach to this assignment. */
    @NotEmpty(message = "{" + Constants.VALIDATION_APPAUTHORIZED_AUTHORIZATION_TYPE_IDS + "}")
    private List<Long> authorizationTypeIds;

    /** Free-text remarks about this authorization. Optional; editable both on create and on update. */
    private String observation;

    /**
     * Whether the linked person is CAIB internal staff. Only consulted on create: if it differs
     * from what is currently stored on the linked {@code Person} record, that record is updated
     * to match. Not editable via update (see the class-level restriction on mutable fields).
     */
    private boolean personalCaib;
}
