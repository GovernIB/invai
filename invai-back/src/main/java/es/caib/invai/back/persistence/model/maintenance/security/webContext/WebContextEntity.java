package es.caib.invai.back.persistence.model.maintenance.security.webContext;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

/**
 * JPA persistent entity representing web contexts assignable within the Manteniments / Seguretat catalog.
 * Implements historical auditing field inheritances by extending {@link BaseEntity}.
 *
 * @since 1.0.4
 */
@Entity
@Table(name = "INV_WEB_CONTEXT")
@Getter
@Setter
public class WebContextEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique auto-generated primary key of the web context. */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_web_context_seq")
    @SequenceGenerator(
            name = "inv_web_context_seq",
            sequenceName = "INV_WEB_CONTEXT_SEQ",
            allocationSize = 1
    )
    private Long id;

    /** Web context descriptive label string (e.g. "Firmar peticiones"). */
    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    /** Spanish localized descriptive name of the web context. */
    @Column(name = "NAME_ES", length = 100)
    private String nameEs;
}
