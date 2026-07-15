package es.caib.invai.api.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Domain entity defining organizational oversight and evaluation Working Commissions
 * managing architectural review boards.
 *
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Commission {

    /** Unique administrative evaluation group primary index identity. */
    private Long id;

    /** Official organizational department committee layout string designation (typically Catalan). */
    private String name;

    /** Translated committee variant layout naming explicitly matching Spanish context configurations. */
    private String nameEs;

    /** Relational lifecycle timeline metric isolating historical soft-deletion points. */
    private LocalDateTime deletedAt;

    /** Enterprise directory user account indicator referencing record deactivation operators. */
    private String deletedBy;
}