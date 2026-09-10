package es.caib.invai.back.interna.application.core.DTO;

import es.caib.invai.back.utils.Constants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

/**
 * Inbound payload for creating or updating an {@code Application}. {@code code} and {@code prefix}
 * must each be unique across applications — enforced by the facade, not by validation here.
 *
 * @since 1.0.1
 */
@Getter
@Setter
public class ApplicationInputDTO {

    /** Display name of the application. */
    @NotBlank(message = "{" + Constants.VALIDATION_APPLICATION_NAME + "}")
    @Size(max = 100, message = "{" + Constants.ERR_APPLICATION_NAME_OVERFLOW + "}")
    private String name;

    /** Short (max 3 characters) acronym used to prefix identifiers generated for this application. */
    @NotBlank(message = "{" + Constants.VALIDATION_APPLICATION_PREFIX + "}")
    @Size(max = 3, message = "{" + Constants.ERR_APPLICATION_PREFIX_OVERFLOW + "}")
    private String prefix;

    /** Unique alphanumeric code identifying the application. */
    @NotBlank(message = "{" + Constants.VALIDATION_APPLICATION_CODE + "}")
    @Size(min = Constants.CODE_MIN_LENGTH, max = Constants.APPLICATION_CODE_MAX_LENGTH, message = "{" + Constants.ERR_APPLICATION_CODE_SIZE + "}")
    private String code;

    /** Identifier of the {@code Category} this application is classified under. */
    @NotNull(message = "{" + Constants.VALIDATION_APPLICATION_CATEGORY_ID + "}")
    private Long categoryId;

    /** Identifier of the {@code SystemType} (infrastructure/architecture classification) of this application. */
    @NotNull(message = "{" + Constants.VALIDATION_APPLICATION_SYSTEM_TYPE_ID + "}")
    private Long systemTypeId;

    /** Identifier of the {@code Field} (business domain) this application belongs to. */
    @NotNull(message = "{" + Constants.VALIDATION_APPLICATION_FIELD_ID + "}")
    private Long fieldId;

    /**
     * DIR3CAIB code of the administrative unit responsible for the application. Optional. When
     * provided, it must match a unit in the live DIR3CAIB tree (validated by the facade); no local
     * record is created or referenced — the code is stored as-is.
     */
    private String admUnitCode;

    /** Identifier of the {@code Commission} overseeing this application. */
    @NotNull(message = "{" + Constants.VALIDATION_APPLICATION_CS_COMMISSION_ID + "}")
    private Long commissionId;

    /** Free-text description of the application. Optional. */
    private String description;

    /** Identifier of the {@code Status} (e.g. active/inactive) to set on the application. */
    @NotNull(message = "{" + Constants.VALIDATION_APPLICATION_STATUS_ID + "}")
    private Long statusId;
}