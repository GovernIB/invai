package es.caib.invai.back.service.mapper.catalog.admUnit;

import es.caib.invai.back.interna.catalog.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.back.rest.dir3.UnidadRest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct translation utility converting a raw DIR3CAIB tree node into its outbound
 * {@link AdmUnitOutputDTO} representation.
 *
 * @since 1.0.5
 */
@Mapper(componentModel = "spring")
public interface AdmUnitMapper {

    /**
     * Maps a raw DIR3CAIB tree node onto an {@link AdmUnitOutputDTO}, carrying its parent code and
     * hierarchy depth. Prefers the co-official denomination for {@code name} when DIR3CAIB
     * publishes one, falling back to the plain denomination otherwise.
     *
     * @param unidadRest the raw DIR3CAIB tree node to convert
     * @return the mapped output DTO
     */
    @Mapping(target = "code", source = "codigo")
    @Mapping(target = "name", expression = "java(unidadRest.getDenominacionCooficial() != null ? unidadRest.getDenominacionCooficial() : unidadRest.getDenominacion())")
    @Mapping(target = "parentCode", source = "codUnidadSuperior")
    @Mapping(target = "level", source = "nivelJerarquico")
    AdmUnitOutputDTO toResponse(UnidadRest unidadRest);
}
