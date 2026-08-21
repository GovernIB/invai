package es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) carrying the optional reason recorded when deleting
 * an AppResponsible assignment.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppResponsibleDeleteDTO {

    /** Free-text reason captured when deleting the assignment. */
    private String observation;
}
