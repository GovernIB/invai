package es.caib.invai.api.interna.application.system_database.database.DTO;

import es.caib.invai.api.interna.application.system_database.core.DTO.AppInformationSystemDbOutputDTO;
import es.caib.invai.api.interna.maintenance.database.DTO.DatabaseOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Outbound transport payload detailing technical context mappings of the target
 * database entity definitions.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppDatabaseOutputDTO implements Serializable {

    private static final long serialVersionUID = 849302847192847291L;

    private Long id;
    private AppInformationSystemDbOutputDTO informationSystemDb;
    private DatabaseOutputDTO database;
    private LocalDateTime deletedAt;
}
