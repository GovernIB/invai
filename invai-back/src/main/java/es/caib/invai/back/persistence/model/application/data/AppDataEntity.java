package es.caib.invai.back.persistence.model.application.data;

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
 * Persistent entity mapping the "data" anchor owned by a corporate {@link ApplicationEntity},
 * covering both its "Open Data" and "Reutilització" tabs. Holds no swagger/OpenAPI data of its
 * own — that is fetched live on every read (see {@code
 * es.caib.invai.back.ejb.application.data.AppDataServiceFacadeBean}), independently for each tab:
 * from either the application's own external REST API (derived from its {@code code}), or, when
 * {@link #useOpenDataUrl}/{@link #useReuseUrl} is {@code true}, from the explicitly configured
 * {@link #openDataUrl}/{@link #reuseUrl} instead — only {@link #observation} and the four URL
 * override fields are actually persisted.
 *
 * @since 1.0.5
 */
@Entity
@Table(name = "INV_APP_DATA")
@Getter
@Setter
public class AppDataEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identification pointer of this data anchor. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_DATA_SEQ")
    @SequenceGenerator(name = "INV_APP_DATA_SEQ", sequenceName = "INV_APP_DATA_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Corporate application this data anchor belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPLICATION_ID", nullable = false)
    private ApplicationEntity application;

    /** Free-text observations about this application's open data / external REST API. */
    @Column(name = "OBSERVATION", length = 4000)
    private String observation;

    /** Explicit OpenAPI document URL to query instead of the application-name-derived one, used only when {@link #useOpenDataUrl} is {@code true}. */
    @Column(name = "OPEN_DATA_URL", length = 500)
    private String openDataUrl;

    /** Whether to query {@link #openDataUrl} instead of deriving the URL from the application's own {@code code}. Defaults to {@code false}. */
    @Column(name = "USE_OPEN_DATA_URL", nullable = false)
    private boolean useOpenDataUrl;

    /** Explicit reuse document URL to query instead of the application-name-derived one, used only when {@link #useReuseUrl} is {@code true}. */
    @Column(name = "REUSE_URL", length = 500)
    private String reuseUrl;

    /** Whether to query {@link #reuseUrl} instead of deriving the URL from the application's own {@code code}. Defaults to {@code false}. */
    @Column(name = "USE_REUSE_URL", nullable = false)
    private boolean useReuseUrl;
}
