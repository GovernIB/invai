package es.caib.invai.back.interna.auth.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) telling the front which areas of the application the logged-in user
 * can use, so it can show only the tabs and menus that won't answer with HTTP 403. Computed from
 * the same rules the endpoints are secured with.
 *
 * @since 1.0.7
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionsDTO {

    /** Systems and databases tab and its maintenances. */
    private boolean systems;

    /** Development tab and its maintenances. */
    private boolean development;

    /** Security tab, including the assignment of the Soffid roles. */
    private boolean security;

    /** Open data and reuse tab. */
    private boolean data;

    /** Accessibility tab and its maintenances. */
    private boolean accessibility;

    /** Integration tab and its maintenances. */
    private boolean integration;

    /** Documentation tab and its maintenance. */
    private boolean documentation;

    /** Consultation of the DIR3 audit of an application. */
    private boolean dir3;

    /** Responsibles tab in read mode (also true for who can change it). */
    private boolean responsibleRead;

    /** Responsibles tab in write mode, and the manual validation of its DIR3. */
    private boolean responsibleWrite;

    /** Everything: application management, general and responsible maintenances, and the launch of the DIR3 audit. */
    private boolean superUser;
}
