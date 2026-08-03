package es.caib.invai.api.service.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain entity model configuring catalog technologies organized under an architecture layer.
 *
 * @since 1.0.2
 */
@Getter
@Setter
public class Technology {

    private Long id;

    private String name;

    private Layer layer;

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
