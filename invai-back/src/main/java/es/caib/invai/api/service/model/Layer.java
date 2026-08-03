package es.caib.invai.api.service.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain entity model configuring architecture layers assignable to technologies.
 *
 * @since 1.0.2
 */
@Getter
@Setter
public class Layer {

    private Long id;

    private String name;

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
