package es.caib.invai.back.interna.integrations.dir3.admUnit.DTO;

import lombok.*;

/**
 * Data Transfer Object (DTO) used to represent the outgoing payload of an Administrative Unit.
 * <p>
 * This object filters and exposes core database fields to client applications, shielding the
 * underlying JPA domain entity structures.
 * </p>
 *
 * @since 1.0.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdmUnitOutputDTO {

    /** The unique corporate alphanumeric code identifying the administrative unit in DIR3CAIB. */
    private String code;

    /** The official localized descriptor name of the administrative unit (Catalan). */
    private String name;

    /**
     * DIR3CAIB code of this unit's parent in the tree, or {@code null} when this unit is the
     * tree's own root. Only populated by the tree-browsing endpoint; always {@code null} for
     * denomination-search results.
     */
    private String parentCode;

    /**
     * 1-based depth of this unit within the requested tree (1 = the tree's own root). Only
     * populated by the tree-browsing endpoint; always {@code null} for denomination-search results.
     */
    private Integer level;
}