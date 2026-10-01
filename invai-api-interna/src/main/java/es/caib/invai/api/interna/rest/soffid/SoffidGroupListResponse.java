package es.caib.invai.api.interna.rest.soffid;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Raw wire DTO mapping the SCIM 2.0 {@code ListResponse} envelope returned by the Soffid
 * {@code GET /scim2/v1/Group/} search endpoint.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@NoArgsConstructor
public class SoffidGroupListResponse {

    /** Total number of results matching the search, across all pages. */
    private long totalResults;

    /** The page of matching Soffid groups, mapped from the SCIM {@code Resources} (capital R) field. */
    @JsonProperty("Resources")
    private List<SoffidGroup> resources;
}
