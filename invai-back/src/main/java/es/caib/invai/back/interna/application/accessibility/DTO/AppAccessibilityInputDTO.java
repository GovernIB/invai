package es.caib.invai.back.interna.application.accessibility.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Inbound payload for creating or updating an accessibility anchor. {@code applicationId} is the
 * only field enforced by bean validation ({@code @NotNull}); every other field may be left
 * {@code null}. However, {@code classificationSegmentId}, {@code complianceId}, {@code
 * publicUrl}, {@code mobileApplication}, and {@code expireDate} being {@code null} (or {@code
 * mobileApplication} being {@code true} with {@code mobileApplicationName} left {@code null})
 * makes {@code ApplicationServiceFacadeBean#isAccessibilityIncomplete} report the owning
 * application's "Accessibilitat" tab as incomplete; {@code nonAccessibleContent} and {@code
 * observations} are never required for completeness. On an update, sending {@code null} for
 * {@code complianceId}/{@code classificationSegmentId} does not clear an already-set reference —
 * see {@code AppAccessibilityMapper#updateModelFromInput}.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppAccessibilityInputDTO {

    /** Identifier of the application this anchor belongs to; required, and immutable after creation. */
    @NotNull(message = "{validation.appaccessibility.applicationId}")
    private Long applicationId;

    /** FK to the compliance situation catalog; required for the "Accessibilitat" tab to be considered complete. */
    private Long complianceId;

    /** FK to the classification segment catalog; required for the "Accessibilitat" tab to be considered complete. */
    private Long classificationSegmentId;

    /** Public URL of the web portal; required for the "Accessibilitat" tab to be considered complete. */
    @Size(max = 255, message = "{validation.appaccessibility.publicUrl.overflow}")
    private String publicUrl;

    /** Whether the application has an associated mobile application; required for the "Accessibilitat" tab to be considered complete. */
    private Boolean mobileApplication;

    /** Name of the associated mobile application; required only when {@code mobileApplication} is {@code true}. */
    @Size(max = 255, message = "{validation.appaccessibility.mobileApplicationName.overflow}")
    private String mobileApplicationName;

    /** Non-accessible content and the justification for why it hasn't been fixed; never required. */
    private String nonAccessibleContent;

    /** Relevant observations regarding accessibility; never required. */
    private String observations;

    /** End-of-validity date for this accessibility evaluation; required for the "Accessibilitat" tab to be considered complete. */
    private LocalDateTime expireDate;
}
