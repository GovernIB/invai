package es.caib.invai.api.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Domain aggregate model representing a corporate provider (company) assigned to a
 * specific {@link AppDevelopment} module.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppProvider {
    private Long id;
    private AppDevelopment appDevelopment;
    private String companyName;
    private Role role;
    private LocalDateTime startDate;
    private LocalDateTime expireDate;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
