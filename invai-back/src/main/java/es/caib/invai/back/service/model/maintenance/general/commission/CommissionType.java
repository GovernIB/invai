package es.caib.invai.back.service.model.maintenance.general.commission;

import lombok.Getter;

/**
 * Enumeration defining the valid structural classification levels for a governance commission panel.
 * <p>
 * Used across presentation, domain, and persistence layers to enforce strong typing
 * and restrict administrative roles to predefined corporate governance scopes.
 * </p>
 *
 * @author invai-team
 * @since 1.0.1
 */
@Getter
public enum CommissionType {

    /**
     * Technical level commission panel.
     * Focused on architecture baselines, system integrations, and development standards.
     */
    TECNICA,

    /**
     * Superior or executive level commission panel.
     * Focused on strategic investments, budget approvals, and high-level digital transformation milestones.
     */
    SUPERIOR
}