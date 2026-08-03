package es.caib.invai.api.interna.application.core.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.caib.invai.api.interna.maintenance.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.api.interna.maintenance.category.DTO.CategoryOutputDTO;
import es.caib.invai.api.interna.maintenance.commission.DTO.CommissionOutputDTO;
import es.caib.invai.api.interna.maintenance.field.DTO.FieldOutputDTO;
import es.caib.invai.api.interna.maintenance.systemType.DTO.SystemTypeOutputDTO;
import es.caib.invai.api.service.model.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) wrapping outgoing application system state metadata payload models.
 * <p>
 * Aggregates complete domain entity snapshots along with automated timeline audit log metadata.
 * </p>
 *
 * @since 1.0.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationOutputDTO {

    /** Primary persistent storage unique sequence row reference. */
    private Long id;

    /** Unique administrative identification classification system alphanumeric code. */
    private String code;

    /** Corporate technical shorthand acronym prefix identifying application scopes. */
    private String prefix;

    /** The plain name designation parameter of the application. */
    private String name;

    /** Resolved taxonomy structural component record instance detail. */
    private CategoryOutputDTO category;

    /** Resolved system architecture infrastructure data item snapshot. */
    private SystemTypeOutputDTO systemType;

    /** Resolved functional field assignment context. */
    private FieldOutputDTO field;

    /** Resolved corporate administrative accountability structural node. */
    private AdmUnitOutputDTO admUnit;

    /** Resolved active corporate governing commission structure reference. */
    private CommissionOutputDTO csCommission;

    /** Verbose description text highlighting operational scopes or functional metrics. */
    private String description;

    /** Operational workflow state indicators object summary metrics. */
    private StatusEnum status;

    /** Explicit expiration limit date thresholds. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate expirationDate;

    /** Persistent initial timestamp marking record creation profiles boundaries. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate createdAt;

    /** Username mapping context credentials tracking resource initialization. */
    private String createdBy;

    /** Persistent timeline timestamp tracking active data structural update tasks. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate updatedAt;

    /** Username credential flag detailing last committing resource modifier actor context. */
    private String updatedBy;

    private Long appInformationSystemDbId;

    private Long appDevelopmentId;
}