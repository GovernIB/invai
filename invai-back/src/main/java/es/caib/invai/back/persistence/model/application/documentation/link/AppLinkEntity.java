package es.caib.invai.back.persistence.model.application.documentation.link;

import es.caib.invai.back.persistence.model.BaseEntity;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.catalog.appSection.LkupAppSectionEntity;
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
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * JPA persistent entity representing a link of an application (the "Enllaços" list of the
 * Documentació tab).
 *
 * @since 1.0.6
 */
@Entity
@Table(name = "INV_APP_LINK")
@Getter
@Setter
public class AppLinkEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique auto-generated database identifier of the link. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_LINK_SEQ")
    @SequenceGenerator(name = "INV_APP_LINK_SEQ", sequenceName = "INV_APP_LINK_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Application the link belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPLICATION_ID", nullable = false)
    private ApplicationEntity application;

    /** Title of the link. */
    @Column(name = "TITLE", nullable = false, length = 250)
    private String title;

    /** Optional description of the link. */
    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    /** Section of the application the link belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SECTION_ID", nullable = false)
    private LkupAppSectionEntity section;

    /** Address the link points to. */
    @Column(name = "URL", nullable = false, length = 500)
    private String url;
}
