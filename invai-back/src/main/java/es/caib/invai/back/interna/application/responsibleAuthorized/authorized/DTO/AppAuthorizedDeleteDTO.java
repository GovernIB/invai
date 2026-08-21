package es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Inbound payload optionally carrying a free-text observation to capture when
 * deleting ("donar de baixa") an application authorized assignment.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppAuthorizedDeleteDTO {

    /** Free-text reason captured when deleting the assignment. */
    private String observation;
}
