package es.caib.invai.back.ejb.maintenance.security.securityRole;

import es.caib.invai.back.interna.maintenance.security.securityRole.DTO.SecurityRoleOutputDTO;
import es.caib.invai.back.rest.soffid.SoffidClient;
import es.caib.invai.back.service.facade.maintenance.security.securityRole.SecurityRoleService;
import es.caib.invai.back.service.mapper.maintenance.security.securityRole.SecurityRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade service implementation for Security Roles (SecurityRole). Currently limited to
 * proxying the Soffid role search: local CRUD is not exposed yet, pending a future iteration.
 *
 * @since 1.0.4
 */
@Service
@Slf4j
@Transactional
public class SecurityRoleServiceFacadeBean implements SecurityRoleService {

    /** Port used to list/search assignable roles against the Soffid SCIM API. */
    @Autowired
    private SoffidClient soffidClient;

    /** MapStruct mapper converting each Soffid role into the outbound DTO. */
    @Autowired
    private SecurityRoleMapper securityRoleMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<SecurityRoleOutputDTO> searchSoffid(String name, Pageable pageable) {
        log.debug("Facade: Listing/searching assignable Soffid roles (name='{}')", name);

        return soffidClient.searchRoles(name, pageable).map(securityRoleMapper::toResponseFromSoffid);
    }
}
