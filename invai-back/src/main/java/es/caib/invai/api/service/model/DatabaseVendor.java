package es.caib.invai.api.service.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Domain Model object representing pure database vendor/type catalog logic schemas.
 *
 * @since 1.0.2
 */
@Getter
@Setter
public class DatabaseVendor {

    private Long id;
    private String name;
    private Integer defaultPort;

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
