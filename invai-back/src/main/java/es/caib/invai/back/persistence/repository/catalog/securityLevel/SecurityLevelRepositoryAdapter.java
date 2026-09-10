package es.caib.invai.back.persistence.repository.catalog.securityLevel;

import es.caib.invai.back.persistence.model.catalog.securityLevel.LkupSecurityLevelEntity;
import es.caib.invai.back.service.mapper.catalog.securityLevel.SecurityLevelMapper;
import es.caib.invai.back.service.model.catalog.securityLevel.SecurityLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link SecurityLevelRepository}.
 *
 * @since 1.0.4
 */
@Repository
@Slf4j
public class SecurityLevelRepositoryAdapter implements SecurityLevelRepository {

    /** Spring Data JPA repository providing raw access to the security level lookup table. */
    @Autowired
    private SecurityLevelJPARepository securityLevelJPARepository;

    /** Mapper used to translate between persistent entities and the domain model. */
    @Autowired
    private SecurityLevelMapper securityLevelMapper;

    @Override
    public List<SecurityLevel> findAll() {
        log.debug("Repository: Fetching every security level lookup entry");
        List<LkupSecurityLevelEntity> entities = securityLevelJPARepository.findAll(Sort.by("name"));
        return securityLevelMapper.toModelList(entities);
    }

    @Override
    public SecurityLevel findById(Long id) {
        log.debug("Repository: Fetching security level lookup entry by ID: {}", id);
        return securityLevelJPARepository.findById(id)
                .map(securityLevelMapper::toModel)
                .orElse(null);
    }
}
