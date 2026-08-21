package es.caib.invai.back.service.model.maintenance.responsible.roleTransfer;

/**
 * Discriminates which underlying entity a role-transfer item refers to: an {@code AppResponsible}
 * assignment or an {@code AppAuthorized} assignment.
 *
 * @since 1.0.3
 */
public enum RoleAssignmentType {
    RESPONSIBLE,
    AUTHORIZED
}
