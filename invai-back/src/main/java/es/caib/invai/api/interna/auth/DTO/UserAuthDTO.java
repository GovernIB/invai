package es.caib.invai.api.interna.auth.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) representing the current security context user authentication status summary.
 * <p>
 * Transmits fundamental security details to client-side presentation layers to evaluate active
 * frontend session states.
 * </p>
 *
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthDTO {

    /** Flag indicator evaluating whether the requesting web client session context is actively authenticated. */
    private boolean authenticated;

    /** The unique corporate directory username credential principal login identifier of the logged-in user. */
    private String username;
}