package es.caib.invai.back.persistence.repository.maintenance.documentation.documentRolePermission;

import es.caib.invai.back.persistence.model.maintenance.documentation.documentRolePermission.DocumentRolePermissionAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link DocumentRolePermissionAudEntity} snapshots.
 *
 * @since 1.0.7
 */
@Repository
public interface DocumentRolePermissionAudJPARepository extends JpaRepository<DocumentRolePermissionAudEntity, Long> {
}
