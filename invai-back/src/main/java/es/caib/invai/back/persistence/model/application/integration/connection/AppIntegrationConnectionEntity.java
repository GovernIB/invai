package es.caib.invai.back.persistence.model.application.integration.connection;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.integration.core.AppIntegrationEntity;
import es.caib.invai.back.persistence.model.maintenance.development.technology.TechnologyEntity;
import es.caib.invai.back.persistence.model.maintenance.integration.externalSystem.ExternalSystemEntity;

import java.io.Serial;

/**
 * Persistent entity mapping one row of the "Integracio" tab's table: the other system this
 * application integrates with (either {@link #application}, an application already registered in
 * the inventory, or {@link #externalSystem}, one that isn't - exactly one of the two must be set,
 * validated in the facade, not enforced by a DB constraint), the technology used, and a snapshot
 * of the Soffid user performing the integration.
 *
 * @since 1.0.5
 */
@Entity
@Table(name = "INV_APP_INTEGRATION_CONN")
@Getter
@Setter
public class AppIntegrationConnectionEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identification pointer of this integration connection. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_INTEGRATION_CONN_SEQ")
    @SequenceGenerator(name = "INV_APP_INTEGRATION_CONN_SEQ", sequenceName = "INV_APP_INTEGRATION_CONN_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Integration anchor this connection belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_INTEGRATION_ID", nullable = false)
    private AppIntegrationEntity appIntegration;

    /** The other system, when it is an application already registered in the inventory. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPLICATION_ID")
    private ApplicationEntity application;

    /** The other system, when it is outside the inventory. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXTERNAL_SYSTEM_ID")
    private ExternalSystemEntity externalSystem;

    /** Technology used by this integration. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TECHNOLOGY_ID", nullable = false)
    private TechnologyEntity technology;

    /** Snapshot of the Soffid username at the time it was selected. */
    @Column(name = "USERNAME", nullable = false, length = 100)
    private String username;
}
