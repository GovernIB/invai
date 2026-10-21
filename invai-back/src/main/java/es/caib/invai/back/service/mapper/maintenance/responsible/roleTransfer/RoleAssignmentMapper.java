package es.caib.invai.back.service.mapper.maintenance.responsible.roleTransfer;

import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleAssignmentOutputDTO;
import es.caib.invai.back.service.mapper.catalog.responsibleType.ResponsibleTypeMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct translation utility building the {@link RoleAssignmentOutputDTO} rows of the "Transferència
 * de rols" screen out of the {@link AppResponsible} and {@link AppAuthorized} domain models. Each row
 * is scoped to the application its assignment belongs to; the responsible type is only filled for
 * responsible rows, and the authorization types only for authorized rows.
 * <p>
 * The authorization types of an authorized row are not read from the model (they live in a join table):
 * the caller resolves them and passes them in, so this mapper never touches a repository.
 * </p>
 *
 * @since 1.0.6
 */
@Mapper(componentModel = "spring", uses = ResponsibleTypeMapper.class)
public interface RoleAssignmentMapper {

    /**
     * Converts a responsible assignment into its role-transfer row.
     *
     * @param responsible the active responsible assignment to convert
     * @return the {@code RESPONSIBLE} row, with its application and responsible type
     */
    @Mapping(target = "type", constant = "RESPONSIBLE")
    @Mapping(target = "applicationId", source = "appResponsibleAuthorized.application.id")
    @Mapping(target = "applicationName", source = "appResponsibleAuthorized.application.name")
    @Mapping(target = "authorizationTypes", ignore = true)
    RoleAssignmentOutputDTO fromResponsible(AppResponsible responsible);

    /**
     * Converts an authorized assignment into its role-transfer row.
     *
     * @param authorized         the active authorized assignment to convert
     * @param authorizationTypes the authorization types currently attached to it, already resolved by the caller
     * @return the {@code AUTHORIZED} row, with its application and the given authorization types
     */
    @Mapping(target = "id", source = "authorized.id")
    @Mapping(target = "type", constant = "AUTHORIZED")
    @Mapping(target = "applicationId", source = "authorized.appResponsibleAuthorized.application.id")
    @Mapping(target = "applicationName", source = "authorized.appResponsibleAuthorized.application.name")
    @Mapping(target = "responsibleType", ignore = true)
    @Mapping(target = "authorizationTypes", source = "authorizationTypes")
    RoleAssignmentOutputDTO fromAuthorized(AppAuthorized authorized, List<AuthorizationTypeOutputDTO> authorizationTypes);
}
