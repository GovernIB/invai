package es.caib.invai.back.interna.maintenance.systems.environment.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) wrapping outgoing infrastructure environment state metadata payload models.
 *
 * @since 1.0.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EnvironmentOutputDTO {

    /** Primary persistent storage unique sequence row reference. */
    private Long id;

    /** Mnemonic alphanumeric code identifying the execution environment. */
    private String code;

    /** Descriptive descriptive text name in Catalan language. */
    private String name;

    /** Descriptive descriptive text name in Spanish language. */
    private String nameEs;

    /** Timestamp of logical soft-deletion, or {@code null} if the record is active. */
    private LocalDateTime deletedAt;
}