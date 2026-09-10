package es.caib.invai.back.rest.dir3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Raw JSON representation of a DIR3CAIB {@code UnidadRest}, as returned by the
 * {@code obtenerArbolUnidades} REST operation. Only the fields this codebase actually consumes
 * are declared; {@code UnidadRest} carries many more (address, historicosUO, contactos, ...) that
 * are silently ignored via {@link JsonIgnoreProperties}.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnidadRest {

    /** Code of the organizational unit. */
    private String codigo;

    /** Denomination (name) of the organizational unit. */
    private String denominacion;

    /** Co-official denomination of the organizational unit, when one exists. */
    private String denominacionCooficial;

    /** Code of the parent organizational unit, or {@code null}/root marker for the tree's root. */
    private String codUnidadSuperior;

    /** 1-based depth of this unit within the requested tree (1 = the tree's own root). */
    private Integer nivelJerarquico;
}
