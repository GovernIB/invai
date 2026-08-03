package es.caib.invai.api.service.mapper;

import es.caib.invai.api.interna.maintenance.databaseVendor.DTO.DatabaseVendorInputDTO;
import es.caib.invai.api.interna.maintenance.databaseVendor.DTO.DatabaseVendorOutputDTO;
import es.caib.invai.api.persistence.model.DatabaseVendorEntity;
import es.caib.invai.api.service.model.DatabaseVendor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * DatabaseVendor database entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring")
public interface DatabaseVendorMapper {

    /**
     * Converts a database relational layer entity into a plain business domain model.
     */
    DatabaseVendor toModel(DatabaseVendorEntity entity);

    /**
     * Converts a domain business layer model into a relational database entity representation.
     */
    DatabaseVendorEntity toEntity(DatabaseVendor model);

    /**
     * Converts a domain model configuration into an outbound presentation layer REST DTO.
     */
    DatabaseVendorOutputDTO toResponse(DatabaseVendor model);

    /**
     * Constructs a pure domain business structure from incoming input payload parameter DTOs.
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    DatabaseVendor toModelFromInput(DatabaseVendorInputDTO inputDTO);

    /**
     * Merges update parameters from an API input payload DTO into an existing active domain business model.
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(DatabaseVendorInputDTO inputDTO, @MappingTarget DatabaseVendor model);

    /**
     * Convenience reference lookup utility initializing a detached shallow domain reference.
     */
    default DatabaseVendorOutputDTO map(Long value) {
        if (value == null) {
            return null;
        }
        DatabaseVendorOutputDTO dto = new DatabaseVendorOutputDTO();
        dto.setId(value);
        return dto;
    }

    /**
     * Direct pass-through mapping helper to avoid recursion or duplication conflicts.
     */
    default DatabaseVendor map(DatabaseVendor value) {
        return value;
    }
}
