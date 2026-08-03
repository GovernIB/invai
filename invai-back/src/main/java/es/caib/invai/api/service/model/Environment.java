package es.caib.invai.api.service.model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain Model object representing pure system execution environment logic schemas.
 *
 * @since 1.0.1
 */
@Getter
@Setter
public class Environment {

    private Long id;
    private String code;
    private String name;
    private String nameEs;

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
}