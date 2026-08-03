package es.caib.invai.api.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Domain aggregate model representing the main software development module detail
 * for a corporate {@link Application} within a specific deployment {@link Environment}.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppDevelopment {
    private Long id;
    private Application application;
    private Environment environment;
    private Modality modality;
    private String code;
    private StandardAdaption standardAdaption;
    private LocalDateTime revisionDate;
    private String observation;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
