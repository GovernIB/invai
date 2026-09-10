package es.caib.invai.back.rest.soffid;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Raw wire DTO mapping the SCIM 2.0 {@code ListResponse} envelope returned by the Soffid
 * {@code GET /scim2/v1/Role/} search endpoint.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@NoArgsConstructor
public class SoffidRoleListResponse {

    /** Total number of results matching the search, across all pages. */
    private long totalResults;

    /** 1-based index of the first result included in this page. */
    private long startIndex;

    /** Number of results included in this page. */
    private long itemsPerPage;

    /** The page of matching Soffid roles, mapped from the SCIM {@code Resources} (capital R) field. */
    @JsonProperty("Resources")
    private List<SoffidRole> resources;
}
