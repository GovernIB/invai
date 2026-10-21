package es.caib.invai.back.interna.application.dir3.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) reporting the outcome of a DIR3 mismatch audit for one application:
 * the bulk audit ({@code PUT /application/dir3/check-all}) lists only the applications
 * where at least one active responsible/authorized assignment has a DIR3 that doesn't match the
 * application's own administrative unit, while the single-application audit
 * ({@code PUT /application/dir3/check/{id}}) always returns the audited application with
 * its resulting state.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationDir3MismatchOutputDTO {

    /** Unique identifier of the flagged application. */
    private Long id;

    /** Display name of the flagged application. */
    private String name;

    /** {@code true} if at least one active assignment mismatches (always {@code true} in the bulk audit's result list). */
    private boolean dir3Mismatch;
}
