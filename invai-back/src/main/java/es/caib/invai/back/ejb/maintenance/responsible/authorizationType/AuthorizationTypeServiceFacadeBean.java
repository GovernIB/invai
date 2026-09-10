package es.caib.invai.back.ejb.maintenance.responsible.authorizationType;

import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeOutputDTO;
import es.caib.invai.back.service.mapper.maintenance.responsible.authorizationType.AuthorizationTypeMapper;
import es.caib.invai.back.persistence.repository.maintenance.responsible.authorizationType.AuthorizationTypeCriteria;
import es.caib.invai.back.persistence.repository.maintenance.responsible.authorizationType.AuthorizationTypeRepository;
import es.caib.invai.back.service.facade.maintenance.responsible.authorizationType.AuthorizationTypeService;
import es.caib.invai.back.service.model.maintenance.responsible.authorizationType.AuthorizationType;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Facade service implementation for administrative AuthorizationTypes.
 *
 * @since 1.0.3
 */
@Service
@Slf4j
@Transactional
public class AuthorizationTypeServiceFacadeBean implements AuthorizationTypeService {

    /** Mapper used to convert between AuthorizationType domain models, entities and DTOs. */
    @Autowired
    private AuthorizationTypeMapper authorizationTypeMapper;

    /** Repository port used to access AuthorizationType persistence operations. */
    @Autowired
    private AuthorizationTypeRepository authorizationTypeRepository;

    /**
     * Retrieves an authorization type by its identifier.
     *
     * @param id the authorization type identifier
     * @return the matching authorization type as an output DTO
     * @throws BusinessRuleException if no authorization type exists with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    public AuthorizationTypeOutputDTO getById(Long id) {
        log.debug("Facade: Fetching authorization type by ID: {}", id);
        AuthorizationType authorizationType = authorizationTypeRepository.findById(id);

        if (authorizationType == null) {
            throw new BusinessRuleException(Constants.ERR_AUTHORIZATIONTYPE_NOT_FOUND);
        }

        return authorizationTypeMapper.toResponse(authorizationType);
    }

    /**
     * Retrieves a paginated list of authorization types matching the given filter criteria.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching authorization types as output DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AuthorizationTypeOutputDTO> getAll(AuthorizationTypeCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching authorization types via pagination boundaries");
        Page<AuthorizationType> domainPage = authorizationTypeRepository.findAll(filter, pageable);
        return domainPage.map(authorizationTypeMapper::toResponse);
    }

    /**
     * Creates a new authorization type after sanitizing the input and checking for name duplicates.
     *
     * @param inputDTO the data for the new authorization type
     * @return the created authorization type as an output DTO
     * @throws BusinessRuleException if an active authorization type with the same name already exists
     */
    @Override
    public AuthorizationTypeOutputDTO create(AuthorizationTypeInputDTO inputDTO) {
        log.info("Facade: Creating new authorization type record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (authorizationTypeRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.ERR_AUTHORIZATIONTYPE_DUPLICATED);
        }

        AuthorizationType model = authorizationTypeMapper.toModelFromInput(inputDTO);

        AuthorizationType savedModel = authorizationTypeRepository.create(model);
        return authorizationTypeMapper.toResponse(savedModel);
    }

    /**
     * Updates an existing authorization type, validating existence and name uniqueness.
     *
     * @param id the identifier of the authorization type to update
     * @param inputDTO the new data to apply
     * @return the updated authorization type as an output DTO
     * @throws BusinessRuleException if the authorization type does not exist or the name is already used by another record
     */
    @Override
    public AuthorizationTypeOutputDTO update(Long id, AuthorizationTypeInputDTO inputDTO) {
        log.info("Facade: Updating authorization type with ID: {}", id);

        AuthorizationType existing = authorizationTypeRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_AUTHORIZATIONTYPE_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (authorizationTypeRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.ERR_AUTHORIZATIONTYPE_DUPLICATED);
        }

        authorizationTypeMapper.updateModelFromInput(inputDTO, existing);
        return authorizationTypeMapper.toResponse(authorizationTypeRepository.update(existing, id));
    }

    /**
     * Logically deletes an authorization type by stamping its deletion timestamp and author.
     *
     * @param id the identifier of the authorization type to delete
     * @throws BusinessRuleException if the authorization type does not exist or is already deleted
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting authorization type with ID: {}", id);
        AuthorizationType existing = authorizationTypeRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_AUTHORIZATIONTYPE_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_AUTHORIZATIONTYPE_NOT_ACTIVE);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        authorizationTypeRepository.delete(existing);
    }

    /**
     * Reactivates a previously deleted authorization type by clearing its deletion timestamp and author.
     *
     * @param id the identifier of the authorization type to reactivate
     * @return the reactivated authorization type as an output DTO
     * @throws BusinessRuleException if the authorization type does not exist or is already active
     */
    @Override
    public AuthorizationTypeOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating authorization type with ID: {}", id);
        AuthorizationType existing = authorizationTypeRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_AUTHORIZATIONTYPE_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_AUTHORIZATIONTYPE_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        AuthorizationType updatedModel = authorizationTypeRepository.update(existing, id);
        return authorizationTypeMapper.toResponse(updatedModel);
    }
}
