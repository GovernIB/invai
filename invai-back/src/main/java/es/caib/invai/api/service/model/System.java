package es.caib.invai.api.service.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class System {
    /** Unique system asset entry sequence key. */
    private Long id;

    /** Resolved host server this system runs on. */
    private Server server;

    /** System instance name, connection identifier or node routing string. */
    private String instance;

    /** Operational connection network port number. */
    private Integer port;

    /** Release version tag representing current deployment state. */
    private String version;

    /** Short definition narrative detailing system context targets. */
    private String description;

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
}