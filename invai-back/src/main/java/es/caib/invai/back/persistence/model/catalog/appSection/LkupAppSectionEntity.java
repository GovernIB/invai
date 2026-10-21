package es.caib.invai.back.persistence.model.catalog.appSection;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

/**
 * JPA persistent entity representing one entry of the fixed lookup of the application sections
 * (tabs) a document or link can be filed under.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@Entity
@Table(name = "INV_LKUP_APP_SECTION")
public class LkupAppSectionEntity {

    /** Unique identifier of the section entry. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_LKUP_APP_SECTION_SEQ")
    @SequenceGenerator(name = "INV_LKUP_APP_SECTION_SEQ", sequenceName = "INV_LKUP_APP_SECTION_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** Fixed technical code of the section (e.g. {@code "SECURITY"}). */
    @Column(name = "CODE", nullable = false)
    private String code;

    /** Display label of the section (Catalan). */
    @Column(name = "NAME", nullable = false)
    private String name;

    /** Display label of the section in Spanish. */
    @Column(name = "NAME_ES", nullable = false)
    private String nameEs;
}
