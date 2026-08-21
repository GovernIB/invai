package es.caib.invai.back.persistence.model.application.responsibleAuthorized.type;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.authorized.AppAuthorizedEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.authorizationType.AuthorizationTypeEntity;

/**
 * JPA persistent intermediate (join) entity linking an {@link AppAuthorizedEntity} anchor
 * to one of the multiple {@link AuthorizationTypeEntity} catalog values it may hold. Deliberately
 * lightweight (no audit trail, no soft-delete): attaching/detaching a type is a plain
 * insert/hard-delete of this row.
 *
 * @since 1.0.3
 */
@Entity
@Table(name = "INV_APP_AUTHORIZED_TYPE_LINK")
@Getter
@Setter
public class AppAuthorizedTypeLinkEntity {

    /** Primary key unique identifier. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_AUTHORIZED_TYPE_LINK_SEQ")
    @SequenceGenerator(name = "INV_APP_AUTHORIZED_TYPE_LINK_SEQ", sequenceName = "INV_APP_AUTHORIZED_TYPE_LINK_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Authorized person anchor this authorization type is attached to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_AUTHORIZED_ID", nullable = false)
    private AppAuthorizedEntity appAuthorized;

    /** Authorization type catalog value attached to the authorized person. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AUTHORIZATION_TYPE_ID", nullable = false)
    private AuthorizationTypeEntity authorizationType;
}
