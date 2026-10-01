package es.caib.invai.back.persistence.model.application.integration.core;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

/**
 * Persistent entity mapping the "Integracio" anchor owned by a corporate {@link
 * ApplicationEntity}. Holds only the free-text observation - the actual system/technology/user/
 * role data lives in its rows ({@code AppIntegrationConnectionEntity}).
 *
 * @since 1.0.5
 */
@Entity
@Table(name = "INV_APP_INTEGRATION")
@Getter
@Setter
public class AppIntegrationEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identification pointer of this integration anchor. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_INTEGRATION_SEQ")
    @SequenceGenerator(name = "INV_APP_INTEGRATION_SEQ", sequenceName = "INV_APP_INTEGRATION_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Corporate application this integration anchor belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPLICATION_ID", nullable = false)
    private ApplicationEntity application;

    /** Free-text observations about this application's integrations. */
    @Lob
    @Column(name = "OBSERVATION")
    private String observation;
}
