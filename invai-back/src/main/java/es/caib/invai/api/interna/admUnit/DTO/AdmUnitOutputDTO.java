package es.caib.invai.api.interna.admUnit.DTO;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) used to represent the outgoing payload of an Administrative Unit.
 * <p>
 * This object filters and exposes core database fields to client applications, shielding the
 * underlying JPA domain entity structures.
 * </p>
 *
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdmUnitOutputDTO {

    /** The persistent database primary key identity identifier. */
    private Long id;

    /** The unique corporate alphanumeric code identifying the administrative unit. */
    private String code;

    /** The official localized descriptor name of the administrative unit (Catalan). */
    private String name;

    /** The translated Spanish localized descriptor name of the administrative unit. */
    private String nameEs;
}