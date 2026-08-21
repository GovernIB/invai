package es.caib.invai.back.persistence.model.application.responsibleAuthorized.core;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

/**
 * Persistent aggregate root entity anchoring the "Responsables i Autoritzats" tab for a corporate
 * application, mirroring the {@code AppInformationSystemDb}/{@code AppDevelopment} pattern: one
 * anchor row per application, referenced by both {@code AppResponsible} and
 * {@code AppAuthorized} records instead of the application directly.
 *
 * @since 1.0.3
 */
@Entity
@Table(name = "INV_APP_RESPONSIBLE_AUTHORIZED")
@Getter
@Setter
public class AppResponsibleAuthorizedEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Primary key unique identifier. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_RESPONSIBLE_AUTHORIZED_SEQ")
    @SequenceGenerator(name = "INV_APP_RESPONSIBLE_AUTHORIZED_SEQ", sequenceName = "INV_APP_RESPONSIBLE_AUTHORIZED_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Corporate application this "Responsables i Autoritzats" anchor belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPLICATION_ID", nullable = false)
    private ApplicationEntity application;
}
