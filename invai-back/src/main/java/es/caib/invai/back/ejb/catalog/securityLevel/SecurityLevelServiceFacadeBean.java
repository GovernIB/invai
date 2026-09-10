package es.caib.invai.back.ejb.catalog.securityLevel;

import es.caib.invai.back.interna.catalog.securityLevel.DTO.SecurityLevelOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.securityLevel.SecurityLevelRepository;
import es.caib.invai.back.service.facade.catalog.securityLevel.SecurityLevelService;
import es.caib.invai.back.service.mapper.catalog.securityLevel.SecurityLevelMapper;
import es.caib.invai.back.service.model.catalog.securityLevel.SecurityLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Facade service implementation for reading the Security level lookup dictionary.
 *
 * @since 1.0.4
 */
@Service
@Slf4j
@Transactional
public class SecurityLevelServiceFacadeBean implements SecurityLevelService {

    /** Mapper used to translate domain models into outbound response DTOs. */
    @Autowired
    private SecurityLevelMapper securityLevelMapper;

    /** Repository port used to read the security level lookup dictionary. */
    @Autowired
    private SecurityLevelRepository securityLevelRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SecurityLevelOutputDTO> getAll() {
        log.debug("Facade: Fetching every security level lookup entry");
        List<SecurityLevel> securityLevels = securityLevelRepository.findAll();
        return securityLevelMapper.toResponseList(securityLevels);
    }
}
