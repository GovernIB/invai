package es.caib.invai.api.interna.rest.soffid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Wire DTO carrying one page of Soffid results (users or roles) back to {@code invai-back},
 * flattened from Soffid's own SCIM {@code ListResponse} envelope down to just what a caller needs
 * to rebuild a {@code Page}: the page's content and the total match count across every page.
 *
 * @param <T> the type of resource in this page ({@code SoffidUser}/{@code SoffidRole})
 * @since 1.0.4
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SoffidPageResponse<T> {

    /** The matching resources on this page. */
    private List<T> content;

    /** Total number of results matching the search, across all pages. */
    private long totalElements;
}
