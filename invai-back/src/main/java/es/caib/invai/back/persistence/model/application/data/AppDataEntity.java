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
 * covering both its "Open Data" and "Reutilització" tabs. Holds no swagger/OpenAPI data of its own:
 * only the {@link #observation} and the URL of each tab's swagger when a person typed one in
 * ({@code null} means the default URL, derived from the application's {@code code}). Whether a URL
 * works is never stored: it is queried every time it is needed (see {@code
 * es.caib.invai.back.ejb.application.data.AppDataServiceFacadeBean}).
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

    /** URL of the swagger of the "Open Data" tab typed in by hand; {@code null} means the tab uses the default one, derived from the application's {@code code}. */
    @Column(name = "OPEN_DATA_URL", length = 500)
    private String openDataUrl;

    /** URL of the swagger of the "Reutilització" tab typed in by hand; {@code null} means the tab uses the default one, derived from the application's {@code code}. */
    @Column(name = "REUSE_URL", length = 500)
    private String reuseUrl;

}
