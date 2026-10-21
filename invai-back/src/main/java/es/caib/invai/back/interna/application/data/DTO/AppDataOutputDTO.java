package es.caib.invai.back.interna.application.data.DTO;

import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Outbound representation of a data anchor, covering both its "Open Data" and "Reutilització"
 * tabs. On {@code getById}, for each tab ({@code openDataUrl} and {@code reuseUrl}) it gives the
 * URL of its swagger - the one a person typed in, or else the default one. Whether a URL works is
 * not part of it: that is asked, URL by URL, to the check endpoint. {@code create} and {@code
 * update} leave both URLs {@code null}.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppDataOutputDTO {

    /** Unique identification pointer of this data anchor. */
    private Long id;

    /** Corporate application this data anchor belongs to. */
    private ApplicationOutputDTO application;

    /** Free-text observations about this application's open data / external REST API. */
    private String observation;

    /** The URL of the swagger of the "Open Data" tab: the one typed in, or else the default one. */
    private String openDataUrl;

    /** The URL of the swagger of the "Reutilització" tab: the one typed in, or else the default one. */
    private String reuseUrl;

    /** Timestamp at which this record was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
