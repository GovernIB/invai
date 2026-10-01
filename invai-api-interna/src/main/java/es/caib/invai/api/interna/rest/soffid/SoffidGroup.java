package es.caib.invai.api.interna.rest.soffid;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Raw wire DTO mapping a single Soffid SCIM 2.0 {@code Group} resource. Only the fields needed to
 * resolve a group's DIR3CAIB code are modelled; every other SCIM attribute ({@code parentGroup},
 * {@code section}, {@code meta}, etc.) is intentionally ignored.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@NoArgsConstructor
public class SoffidGroup {

    /** Soffid's own numeric identifier for the group; not used to resolve the DIR3CAIB code. */
    private Long id;

    /**
     * The group's SCIM name (e.g. {@code "sgaip"}), matching a user's {@code primaryGroup} - a
     * short internal code, never the DIR3CAIB code itself (see {@link #attributes} for that).
     */
    private String name;

    /**
     * Soffid's free-form extension attribute bag for the group. The group's DIR3CAIB code, when
     * one is assigned, lives under the {@code "DIR3"} key here (e.g. {@code {"DIR3": "A04027054"}})
     * - Never in {@link #name}. Values are typed as {@code Object} rather than {@code String}
     * defensively, since this bag is entirely free-form on Soffid's side; only the {@code "DIR3"}
     * entry is ever read, via {@code String.valueOf(...)}. {@code null} (rather than an empty map)
     * when the group carries no extension attributes at all.
     */
    private Map<String, Object> attributes;
}
