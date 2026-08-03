package es.caib.invai.api.interna.application.development.core.DTO;

import es.caib.invai.api.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.api.interna.maintenance.environment.DTO.EnvironmentOutputDTO;
import es.caib.invai.api.service.model.Modality;
import es.caib.invai.api.service.model.StandardAdaption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Outbound transport payload detailing the main application development module state,
 * aggregating resolved application, environment, and lookup entity snapshots.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevelopmentOutputDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private ApplicationOutputDTO application;
    private EnvironmentOutputDTO environment;
    private Modality modality;
    private String code;
    private StandardAdaption standardAdaption;
    private LocalDateTime revisionDate;
    private String observation;
    private LocalDateTime deletedAt;
}
