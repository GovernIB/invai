package es.caib.invai.back.persistence.model.catalog.documentStatus;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

/**
 * JPA persistent entity representing one entry of the fixed lookup of the states of a document.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@Entity
@Table(name = "INV_LKUP_DOCUMENT_STATUS")
public class LkupDocumentStatusEntity {

    /** Unique identifier of the state entry. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_LKUP_DOCUMENT_STATUS_SEQ")
    @SequenceGenerator(name = "INV_LKUP_DOCUMENT_STATUS_SEQ", sequenceName = "INV_LKUP_DOCUMENT_STATUS_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Fixed technical code of the state (e.g. {@code "ACTIVE"}). */
    @Column(name = "CODE", nullable = false)
    private String code;

    /** Display label of the state (Catalan). */
    @Column(name = "NAME", nullable = false)
    private String name;

    /** Display label of the state in Spanish. */
    @Column(name = "NAME_ES", nullable = false)
    private String nameEs;
}
