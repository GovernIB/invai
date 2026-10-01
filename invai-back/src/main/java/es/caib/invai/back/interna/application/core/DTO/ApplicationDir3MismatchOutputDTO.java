package es.caib.invai.back.interna.application.core.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) identifying a single application flagged by the bulk DIR3 mismatch
 * audit ({@code PUT /interna/application/dir3-check-all}): at least one of its active
 * responsible/authorized assignments has a DIR3 that doesn't match the application's own
 * administrative unit.
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

    /** Always {@code true} - only applications with at least one mismatching assignment are returned. */
    private boolean dir3Mismatch;
}
