package es.caib.invai.back.interna.application.integration.connection.DTO;

import es.caib.invai.back.utils.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Inbound payload for creating or updating an integration connection. Exactly one of {@code
 * applicationId}/{@code externalSystemId} must be set - enforced by the facade (see {@code
 * AppIntegrationConnectionServiceFacadeBean}), not by bean validation, since neither field alone can be
 * marked required. {@code requiredRoleIds} carries the connection's full set of required roles, by
 * Soffid role id only - the frontend already resolved each id's name/system/description via {@code
 * SoffidClient#searchRoles} when the user picked it, so only the id is persisted; the full details
 * are resolved live from Soffid again on every read (see {@code
 * AppIntegrationConnectionServiceFacadeBean#getRequiredRoles}), never trusted from the
 * client and never re-validated against Soffid on write. It is never managed independently (see
 * {@code AppIntegrationConnectionServiceFacadeBean#updateRequiredRoles}), always replacing the
 * connection's whole required-role set on create/update in the same transaction - {@code @NotEmpty}
 * below rejects a connection with zero required roles on both create and update.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppIntegrationConnectionInputDTO {

    /** Identifier of the integration anchor this connection belongs to; required, immutable after creation. */
    @NotNull(message = "{" + Constants.VALIDATION_APPINTEGRATIONCONNECTION_APPINTEGRATIONID + "}")
    private Long appIntegrationId;

    /** Identifier of the other system, when it is an application already registered in the inventory. */
    private Long applicationId;

    /** Identifier of the other system, when it is outside the inventory. */
    private Long externalSystemId;

    /** Identifier of the technology used by this integration; required. */
    @NotNull(message = "{" + Constants.VALIDATION_APPINTEGRATIONCONNECTION_TECHNOLOGYID + "}")
    private Long technologyId;

    /** Soffid username, as returned by the Soffid user search; required. */
    @NotBlank(message = "{" + Constants.VALIDATION_APPINTEGRATIONCONNECTION_USERNAME + "}")
    private String username;

    /** Soffid role ids required by this integration (1...N); required, replaces the whole set on update. */
    @NotEmpty(message = "{" + Constants.VALIDATION_APPINTEGRATIONCONNECTION_REQUIREDROLES + "}")
    private List<Long> requiredRoleIds;
}
