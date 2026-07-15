package es.caib.invai.api.interna.application.DTO;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) capturing incoming payload attributes required to register or update
 * an Application software asset record within the enterprise inventory system framework.
 * <p>
 * Enforces declarative bean validation attributes tied to localization message bundles and map structures.
 * </p>
 *
 * @since 1.0.0
 */
@Getter
@Setter
public class ApplicationInputDTO {

    /** The public descriptive identification name designation parameter of the application. */
    @NotBlank(message = "{validation.application.name}")
    private String name;

    /** Corporate technical shorthand acronym prefix routing indicator identifying application scopes. */
    @NotBlank(message = "{validation.application.prefix}")
    private String prefix;

    /** Unique administrative identification classification system alphanumeric code string tracker. */
    @NotBlank(message = "{validation.application.code}")
    private String code;

    /** Foreign primary reference key tracking target taxonomic classification Category records. */
    @NotNull(message = "{validation.application.categoryId}")
    private Long categoryId;

    /** Foreign primary reference key tracking structural architecture execution infrastructure classifications. */
    @NotNull(message = "{validation.application.systemTypeId}")
    private Long systemTypeId;

    /** Foreign primary reference key detailing the targeted operational business field perimeter layout. */
    @NotNull(message = "{validation.application.fieldId}")
    private Long fieldId;

    /** Foreign primary reference key mapping structural corporate administrative accountability unit locations. */
    @NotNull(message = "{validation.application.admUnitId}")
    private Long admUnitId;

    /** Foreign primary reference key defining the supervising governance working commission group. */
    @NotNull(message = "{validation.application.commissionId}")
    private Long commissionId;

    /** Verbose description narrative text highlighting structural operational scopes or functional metrics. */
    private String description;

    /** Target status configuration map status identifier tracking active lifecycle asset boundaries. */
    @NotNull(message = "{validation.application.statusId}")
    private Long statusId;
}