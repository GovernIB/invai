package es.caib.invai.api.service.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Domain Model object representing pure host server infrastructure logic schemas.
 *
 * @since 1.0.2
 */
@Getter
@Setter
public class Server {

    private Long id;
    private String name;
    private Environment environment;
    private ServerType serverType;

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
