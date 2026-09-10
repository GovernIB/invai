package es.caib.invai.back.ejb.maintenance.security.identityProvider;

import es.caib.invai.back.interna.maintenance.security.identityProvider.DTO.IdentityProviderInputDTO;
import es.caib.invai.back.interna.maintenance.security.identityProvider.DTO.IdentityProviderOutputDTO;
import es.caib.invai.back.service.mapper.maintenance.security.identityProvider.IdentityProviderMapper;
import es.caib.invai.back.persistence.repository.maintenance.security.identityProvider.IdentityProviderCriteria;
import es.caib.invai.back.persistence.repository.maintenance.security.identityProvider.IdentityProviderRepository;
import es.caib.invai.back.service.facade.maintenance.security.identityProvider.IdentityProviderService;
import es.caib.invai.back.service.model.maintenance.security.identityProvider.IdentityProvider;
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
 * Facade service implementation for administrative IdentityProviders.
 *
 * @since 1.0.4
 */
@Service
@Slf4j
@Transactional
public class IdentityProviderServiceFacadeBean implements IdentityProviderService {

    /** Mapper used to convert between IdentityProvider domain models, entities and DTOs. */
    @Autowired
    private IdentityProviderMapper identityProviderMapper;

    /** Repository port used to access IdentityProvider persistence operations. */
    @Autowired
    private IdentityProviderRepository identityProviderRepository;

    /**
     * Retrieves an identity provider by its identifier.
     *
     * @param id the identity provider identifier
     * @return the matching identity provider as an output DTO
     * @throws BusinessRuleException if no identity provider exists with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    public IdentityProviderOutputDTO getById(Long id) {
        log.debug("Facade: Fetching identity provider by ID: {}", id);
        IdentityProvider identityProvider = identityProviderRepository.findById(id);

        if (identityProvider == null) {
            throw new BusinessRuleException(Constants.ERR_IDENTITYPROVIDER_NOT_FOUND);
        }

        return identityProviderMapper.toResponse(identityProvider);
    }

    /**
     * Retrieves a paginated list of identity providers matching the given filter criteria.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching identity providers as output DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<IdentityProviderOutputDTO> getAll(IdentityProviderCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching identity providers via pagination boundaries");
        Page<IdentityProvider> domainPage = identityProviderRepository.findAll(filter, pageable);
        return domainPage.map(identityProviderMapper::toResponse);
    }

    /**
     * Creates a new identity provider after sanitizing the input and checking for name duplicates.
     *
     * @param inputDTO the data for the new identity provider
     * @return the created identity provider as an output DTO
     * @throws BusinessRuleException if an active identity provider with the same name already exists
     */
    @Override
    public IdentityProviderOutputDTO create(IdentityProviderInputDTO inputDTO) {
        log.info("Facade: Creating new identity provider record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (identityProviderRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.ERR_IDENTITYPROVIDER_DUPLICATED);
        }

        IdentityProvider model = identityProviderMapper.toModelFromInput(inputDTO);

        IdentityProvider savedModel = identityProviderRepository.create(model);
        return identityProviderMapper.toResponse(savedModel);
    }

    /**
     * Updates an existing identity provider, validating existence and name uniqueness.
     *
     * @param id the identifier of the identity provider to update
     * @param inputDTO the new data to apply
     * @return the updated identity provider as an output DTO
     * @throws BusinessRuleException if the identity provider does not exist or the name is already used by another record
     */
    @Override
    public IdentityProviderOutputDTO update(Long id, IdentityProviderInputDTO inputDTO) {
        log.info("Facade: Updating identity provider with ID: {}", id);

        IdentityProvider existing = identityProviderRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_IDENTITYPROVIDER_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (identityProviderRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.ERR_IDENTITYPROVIDER_DUPLICATED);
        }

        identityProviderMapper.updateModelFromInput(inputDTO, existing);
        return identityProviderMapper.toResponse(identityProviderRepository.update(existing, id));
    }

    /**
     * Logically deletes an identity provider by stamping its deletion timestamp and author.
     *
     * @param id the identifier of the identity provider to delete
     * @throws BusinessRuleException if the identity provider does not exist or is already deleted
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting identity provider with ID: {}", id);
        IdentityProvider existing = identityProviderRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_IDENTITYPROVIDER_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_IDENTITYPROVIDER_NOT_ACTIVE);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        identityProviderRepository.delete(existing);
    }

    /**
     * Reactivates a previously deleted identity provider by clearing its deletion timestamp and author.
     *
     * @param id the identifier of the identity provider to reactivate
     * @return the reactivated identity provider as an output DTO
     * @throws BusinessRuleException if the identity provider does not exist or is already active
     */
    @Override
    public IdentityProviderOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating identity provider with ID: {}", id);
        IdentityProvider existing = identityProviderRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_IDENTITYPROVIDER_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_IDENTITYPROVIDER_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        IdentityProvider updatedModel = identityProviderRepository.update(existing, id);
        return identityProviderMapper.toResponse(updatedModel);
    }
}
