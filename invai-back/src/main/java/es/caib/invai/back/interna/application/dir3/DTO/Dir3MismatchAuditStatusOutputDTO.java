package es.caib.invai.back.interna.application.dir3.DTO;

import es.caib.invai.back.service.model.application.dir3.Dir3MismatchAuditState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object (DTO) reporting the progress and outcome of the background DIR3 audit of
 * every application ({@code PUT /application/dir3/check-all} launches it,
 * {@code GET /application/dir3/check-all/status} reads it).
 *
 * @since 1.0.6
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Dir3MismatchAuditStatusOutputDTO {

    /** Current lifecycle state of the audit. */
    private Dir3MismatchAuditState state;

    /** Username of the person who launched the audit, or {@code null} while {@code IDLE}. */
    private String startedBy;

    /** Moment the audit was launched, or {@code null} while {@code IDLE}. */
    private LocalDateTime startedAt;

    /** Moment the audit ended, or {@code null} while it is {@code IDLE} or {@code RUNNING}. */
    private LocalDateTime finishedAt;

    /** Number of applications the audit has to go through (0 until the list has been loaded). */
    private int totalApplications;

    /** Number of applications already audited, successfully or not. */
    private int processedApplications;

    /** Applications found mismatching so far (at the end, every application currently mismatching among those audited). */
    private List<ApplicationDir3MismatchOutputDTO> mismatchedApplications;

    /**
     * Message key explaining why the audit ended {@code FAILED} when the cause is known (e.g. the
     * launching user's session expired), or {@code null} otherwise.
     */
    private String failureReason;

    /**
     * Applications whose audit failed (e.g. Soffid couldn't be queried). They were left exactly as
     * they were and can be audited again with {@code PUT /application/dir3/check/{id}}.
     */
    private List<Long> failedApplicationIds;
}
