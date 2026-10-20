package es.caib.invai.back.ejb.application.integration.connection;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.exception.SoffidClientException;
import es.caib.invai.back.exception.SoffidTimeoutException;
import es.caib.invai.back.interna.application.integration.connection.DTO.AppIntegrationConnectionInputDTO;
import es.caib.invai.back.interna.application.integration.connection.DTO.AppIntegrationConnectionOutputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.application.integration.core.AppIntegrationRepository;
import es.caib.invai.back.persistence.repository.application.integration.connection.AppIntegrationConnectionCriteria;
import es.caib.invai.back.persistence.repository.application.integration.connection.AppIntegrationConnectionRepository;
import es.caib.invai.back.persistence.repository.application.integration.requiredRole.AppIntegrationRequiredRoleRepository;
import es.caib.invai.back.persistence.repository.maintenance.development.technology.TechnologyRepository;
import es.caib.invai.back.persistence.repository.maintenance.integration.externalSystem.ExternalSystemRepository;
import es.caib.invai.back.rest.soffid.SoffidClient;
import es.caib.invai.back.rest.soffid.SoffidRole;
import es.caib.invai.back.service.facade.application.integration.connection.AppIntegrationConnectionService;
import es.caib.invai.back.service.mapper.application.integration.connection.AppIntegrationConnectionMapper;
import es.caib.invai.back.service.model.application.integration.connection.AppIntegrationConnection;
import es.caib.invai.back.service.model.application.integration.requiredRole.AppIntegrationRequiredRole;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Facade service implementing CRUD for integration connections. {@link #getAll} additionally
 * enriches each connection with its live-resolved required role details ({@link #getRequiredRoles})
 * and granted roles ({@link #getGrantedRoles}), comparing them via {@link #hasMismatch} to compute
 * {@code rolesMismatch}; being a read, it is free to let a {@link BusinessRuleException} propagate if
 * Soffid can't be reached. {@link #create}/{@link #update}, by contrast, never attempt this live
 * resolution at all - unlike a read, a write has already taken effect by the time it would run, and
 * a transient Soffid outage must not roll back an otherwise-successful write (mirrors {@code
 * AppDataServiceFacadeBean}, which likewise only resolves its live OpenAPI/reuse documents from
 * {@code getById}, never from {@code create}/{@code update}).
 * <p>
 * Only the Soffid role id is ever persisted for a required role (see {@link
 * AppIntegrationRequiredRole}): the frontend already resolved the full role details via {@code
 * SoffidClient.searchRoles} when the user picked it, so nothing is re-validated against Soffid on
 * writing. The id set travels inside {@link AppIntegrationConnectionInputDTO#getRequiredRoleIds()}; on
 * creation every id is simply inserted (see {@link #createRequiredRole}) since a brand-new connection
 * has nothing to reconcile against, and on update the requested id set is compared against what's
 * persisted (see {@link #updateRequiredRoles}): an unchanged set touches nothing at all,
 * otherwise only the added/removed ids are touched. The name/system/description of each required
 * role is only ever resolved live from Soffid on read, exactly like {@code grantedRoles}, never
 * persisted.
 * </p>
 *
 * @since 1.0.5
 */
@Service
@Slf4j
@Transactional
public class AppIntegrationConnectionServiceFacadeBean implements AppIntegrationConnectionService {

    @Autowired
    private AppIntegrationConnectionMapper appIntegrationConnectionMapper;

    @Autowired
    private AppIntegrationConnectionRepository appIntegrationConnectionRepository;

    @Autowired
    private AppIntegrationRequiredRoleRepository appIntegrationRequiredRoleRepository;

    @Autowired
    private SoffidClient soffidClient;

    /** Repository port used to validate the owning "Integracio" anchor actually exists. */
    @Autowired
    private AppIntegrationRepository appIntegrationRepository;

    /** Repository port used to validate an optional "Sistema" application reference actually exists. */
    @Autowired
    private ApplicationRepository applicationRepository;

    /** Repository port used to validate an optional "Sistema" external system reference actually exists. */
    @Autowired
    private ExternalSystemRepository externalSystemRepository;

    /** Repository port used to validate the referenced technology actually exists. */
    @Autowired
    private TechnologyRepository technologyRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AppIntegrationConnectionOutputDTO> getAll(Long appIntegrationId, AppIntegrationConnectionCriteria criteria, Pageable pageable) {
        log.debug("Facade: Fetching application integration connections for anchor ID: {}", appIntegrationId);
        Page<AppIntegrationConnection> domainPage = appIntegrationConnectionRepository.findAll(appIntegrationId, criteria, pageable);
        return domainPage.map(domain -> {
            AppIntegrationConnectionOutputDTO response = appIntegrationConnectionMapper.toResponse(domain);
            List<SoffidRole> requiredRoles = getRequiredRoles(domain.getId());
            List<SoffidRole> grantedRoles = getGrantedRoles(domain.getUsername());
            response.setRequiredRoles(requiredRoles);
            response.setGrantedRoles(grantedRoles);
            response.setRolesMismatch(hasMismatch(requiredRoles, grantedRoles));
            return response;
        });
    }

    @Override
    public AppIntegrationConnectionOutputDTO create(AppIntegrationConnectionInputDTO inputDTO) {
        log.info("Facade: Persisting new application integration connection for anchor ID: {}", inputDTO.getAppIntegrationId());

        Utils.sanitize(inputDTO);
        requireExactlyOneSystem(inputDTO);
        requireReferencesExist(inputDTO);

        AppIntegrationConnection domainModel = appIntegrationConnectionMapper.toModelFromInput(inputDTO);
        AppIntegrationConnection savedModel = appIntegrationConnectionRepository.create(domainModel);

        inputDTO.getRequiredRoleIds().forEach(x -> createRequiredRole(savedModel.getId(), x));

        return appIntegrationConnectionMapper.toResponse(savedModel);
    }

    @Override
    public AppIntegrationConnectionOutputDTO update(Long id, AppIntegrationConnectionInputDTO inputDTO) {
        log.info("Facade: Updating application integration connection ID: {}", id);

        AppIntegrationConnection existingModel = appIntegrationConnectionRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_INTEGRATION_CONNECTION_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);
        requireExactlyOneSystem(inputDTO);

        appIntegrationConnectionMapper.updateModelFromInput(inputDTO, existingModel);
        AppIntegrationConnection updatedModel = appIntegrationConnectionRepository.update(existingModel, id);

        updateRequiredRoles(id, inputDTO.getRequiredRoleIds());

        return appIntegrationConnectionMapper.toResponse(updatedModel);
    }

    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting application integration connection ID: {}", id);

        AppIntegrationConnection existingModel = appIntegrationConnectionRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_INTEGRATION_CONNECTION_NOT_FOUND);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appIntegrationConnectionRepository.delete(existingModel);

        for (AppIntegrationRequiredRole requiredRole : appIntegrationRequiredRoleRepository.findAllActiveByAppIntegrationConnectionId(id)) {
            deleteRequiredRole(requiredRole);
        }
    }

    /**
     * Requires exactly one of {@code applicationId}/{@code externalSystemId} to be set - neither
     * both (ambiguous) nor neither (no system at all).
     *
     * @param inputDTO the (already sanitized) payload to check
     * @throws BusinessRuleException if zero or both are set
     */
    private void requireExactlyOneSystem(AppIntegrationConnectionInputDTO inputDTO) {
        boolean hasApplication = inputDTO.getApplicationId() != null;
        boolean hasExternalSystem = inputDTO.getExternalSystemId() != null;
        if (!hasApplication && !hasExternalSystem) {
            throw new BusinessRuleException(Constants.ERR_APP_INTEGRATION_CONNECTION_SYSTEM_REQUIRED);
        }
        if (hasApplication && hasExternalSystem) {
            throw new BusinessRuleException(Constants.ERR_APP_INTEGRATION_CONNECTION_SYSTEM_AMBIGUOUS);
        }
    }

    /**
     * Validates that every FK id referenced by the payload actually exists: the owning anchor, the
     * technology, and whichever ONE of application/external system {@link #requireExactlyOneSystem}
     * confirmed is set.
     *
     * @param inputDTO the (already sanitized, already exactly-one-system-checked) payload to check
     * @throws BusinessRuleException if any referenced id doesn't exist
     */
    private void requireReferencesExist(AppIntegrationConnectionInputDTO inputDTO) {
        if (appIntegrationRepository.findById(inputDTO.getAppIntegrationId()) == null) {
            throw new BusinessRuleException(Constants.ERR_APP_INTEGRATION_NOT_FOUND);
        }
        if (inputDTO.getApplicationId() != null && applicationRepository.findById(inputDTO.getApplicationId()) == null) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }
        if (inputDTO.getExternalSystemId() != null && externalSystemRepository.findById(inputDTO.getExternalSystemId()) == null) {
            throw new BusinessRuleException(Constants.ERR_EXTERNALSYSTEM_NOT_FOUND);
        }
        if (technologyRepository.findById(inputDTO.getTechnologyId()) == null) {
            throw new BusinessRuleException(Constants.ERR_TECHNOLOGY_NOT_FOUND);
        }
    }

    /**
     * Resolves {@code connectionId}'s persisted required role ids into their full Soffid role
     * details (name/system/description).
     *
     * @param connectionId identifier of the connection whose required roles to resolve
     * @return the connection's required role details
     * @throws BusinessRuleException if Soffid can't be reached
     */
    private List<SoffidRole> getRequiredRoles(Long connectionId) {
        List<Long> ids = appIntegrationRequiredRoleRepository
                .findAllActiveByAppIntegrationConnectionId(connectionId).stream()
                .map(AppIntegrationRequiredRole::getRoleId)
                .toList();

        try {
            return ids.isEmpty() ? List.of() : soffidClient.getRolesByIds(ids);
        } catch (SoffidTimeoutException | SoffidClientException e) {
            log.error("Could not resolve required role details for connection ID: {}", connectionId, e);
            throw new BusinessRuleException(Constants.ERR_REQUIREDROLE_UNAVAILABLE);
        }
    }

    /**
     * Resolves the roles Soffid currently reports as granted to the given account.
     *
     * @param username the Soffid account name to resolve granted roles for
     * @return the roles currently granted to {@code username}
     * @throws BusinessRuleException if Soffid can't be reached
     */
    private List<SoffidRole> getGrantedRoles(String username) {
        try {
            return soffidClient.getGrantedRoles(username);
        } catch (SoffidTimeoutException | SoffidClientException e) {
            log.error("Could not resolve granted roles for username '{}'", username, e);
            throw new BusinessRuleException(Constants.ERR_GRANTEDROLE_UNAVAILABLE);
        }
    }

    /**
     * Reconciles a connection's whole required-role set against the requested ids: if the persisted
     * and requested id sets are identical, nothing is touched at all. Otherwise, soft-deletes any
     * persisted row whose role id is no longer requested and inserts a new row for any requested
     * role id with no persisted row yet; ids present in both sets are left untouched.
     *
     * @param appIntegrationConnectionId identifier of the owning connection
     * @param requestedRoleIds      the connection's full requested set of required role ids
     */
    private void updateRequiredRoles(Long appIntegrationConnectionId, List<Long> requestedRoleIds) {
        List<AppIntegrationRequiredRole> currentRoles = appIntegrationRequiredRoleRepository
                .findAllActiveByAppIntegrationConnectionId(appIntegrationConnectionId);

        Set<Long> currentRoleIds = currentRoles.stream().map(AppIntegrationRequiredRole::getRoleId).collect(Collectors.toSet());
        Set<Long> requestedIds = Set.copyOf(requestedRoleIds);
        if (currentRoleIds.equals(requestedIds)) {
            return;
        }

        for (AppIntegrationRequiredRole currentRole : currentRoles) {
            if (!requestedIds.contains(currentRole.getRoleId())) {
                deleteRequiredRole(currentRole);
            }
        }

        for (Long requestedId : requestedIds) {
            if (!currentRoleIds.contains(requestedId)) {
                createRequiredRole(appIntegrationConnectionId, requestedId);
            }
        }
    }

    /**
     * Persists a single new required role row as a child of the given connection.
     *
     * @param appIntegrationConnectionId identifier of the owning connection
     * @param roleId                the Soffid role id to persist
     */
    private void createRequiredRole(Long appIntegrationConnectionId, Long roleId) {
        AppIntegrationRequiredRole newModel = new AppIntegrationRequiredRole();
        newModel.setRoleId(roleId);
        AppIntegrationConnection parentRef = new AppIntegrationConnection();
        parentRef.setId(appIntegrationConnectionId);
        newModel.setAppIntegrationConnection(parentRef);
        appIntegrationRequiredRoleRepository.create(newModel);
    }

    /**
     * Soft-deletes a single required role row, stamping the deletion audit fields.
     *
     * @param requiredRole the required role to soft-delete
     */
    private void deleteRequiredRole(AppIntegrationRequiredRole requiredRole) {
        requiredRole.setDeletedAt(LocalDateTime.now());
        requiredRole.setDeletedBy(Utils.resolveCurrentUsername());
        appIntegrationRequiredRoleRepository.delete(requiredRole);
    }

    /**
     * Compares required roles against granted roles by Soffid role id (symmetric difference):
     * {@code true} if any required role is missing from the granted set, or the granted set
     * carries any role beyond what's required.
     *
     * @param requiredRoles the connection's required roles, as resolved by {@link #getRequiredRoles}
     * @param grantedRoles  the roles Soffid currently reports as granted
     * @return whether the two sets differ
     */
    private boolean hasMismatch(List<SoffidRole> requiredRoles, List<SoffidRole> grantedRoles) {
        Set<Long> requiredIds = requiredRoles.stream().map(SoffidRole::getId).collect(Collectors.toSet());
        Set<Long> grantedIds = grantedRoles.stream().map(SoffidRole::getId).collect(Collectors.toSet());
        return !requiredIds.equals(grantedIds);
    }
}
