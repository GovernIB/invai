package es.caib.invai.back.persistence.repository.application.core;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object (DTO) capturing search filters and evaluation metrics used
 * to build dynamic database queries targeting application registry assets.
 * Evaluates basic property properties, foreign identity indexes, and composite full-text searches.
 *
 * @since 1.0.1
 */
@Getter
@Setter
public class ApplicationCriteria {

    /** Acronym prefix substring filter applied to the application's prefix. */
    private String prefix;
    /** Name substring filter applied to the application's name. */
    private String applicationName;
    /** Foreign key filter restricting results to a specific taxonomy category. */
    private Long categoryId;
    /** Foreign key filter restricting results to a specific system type. */
    private Long systemTypeId;
    /** Foreign key filter restricting results to a specific operational field. */
    private Long fieldId;
    /** Foreign key filter restricting results to a specific commission. */
    private Long commissionId;
    /** DIR3CAIB code filter restricting results to a specific administrative unit. */
    private String admUnitCode;
    /** Foreign key filter restricting results to a specific lifecycle status. */
    private Long statusId;
    /** Substring filter applied to the application's description. */
    private String description;
    /** Flag restricting results to applications flagged as incomplete. */
    private Boolean incomplete;

    private List<Long> incompleteApplicationIds;

    /** Foreign key filter restricting results to applications with an active responsible assignment for this person. */
    private Long responsibleId;
    /** Foreign key filter restricting results to applications linked to this database (via any of their systems' information-system groupings). */
    private Long databaseId;
    /** Foreign key filter restricting results to applications hosted on this server, whether via a system or a database. */
    private Long serverId;
    /** Foreign key filter restricting results to applications deployed in this environment, whether via a system's or a database's host server. */
    private Long environmentId;

    /** Alphanumeric text token applied across multiple structural string fields during comprehensive global queries. */
    private String quickSearch;

    /**
     * DIR3CAIB codes of administrative units whose name matches {@link #quickSearch}, resolved by
     * the facade against the live DIR3CAIB cached tree before the query is built (an admin unit's
     * name is no longer stored locally, so it can't be searched via a SQL join). Internal only —
     * any client-supplied value is overwritten before use.
     */
    private List<String> quickSearchAdmUnitCodes;
}