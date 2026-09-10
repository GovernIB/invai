package es.caib.invai.back.ejb.catalog.securityLevel;

import es.caib.invai.back.interna.catalog.securityLevel.DTO.SecurityLevelOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.securityLevel.SecurityLevelRepository;
import es.caib.invai.back.service.mapper.catalog.securityLevel.SecurityLevelMapper;
import es.caib.invai.back.service.model.catalog.securityLevel.SecurityLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SecurityLevelServiceFacadeBean}, verifying that the read-only lookup
 * dictionary delegates correctly to the mocked {@link SecurityLevelRepository} and
 * {@link SecurityLevelMapper} collaborators.
 */
@ExtendWith(MockitoExtension.class)
class SecurityLevelServiceFacadeBeanTest {

    @Mock
    private SecurityLevelMapper securityLevelMapper;

    @Mock
    private SecurityLevelRepository securityLevelRepository;

    @InjectMocks
    private SecurityLevelServiceFacadeBean securityLevelServiceFacadeBean;

    @Test
    void getAll_delegatesToRepositoryAndMapsList() {
        SecurityLevel securityLevel = new SecurityLevel();
        securityLevel.setId(1L);
        securityLevel.setName("Alt");
        securityLevel.setNameEs("Alt");

        List<SecurityLevel> models = List.of(securityLevel);
        List<SecurityLevelOutputDTO> mapped = List.of(new SecurityLevelOutputDTO());

        when(securityLevelRepository.findAll()).thenReturn(models);
        when(securityLevelMapper.toResponseList(models)).thenReturn(mapped);

        List<SecurityLevelOutputDTO> result = securityLevelServiceFacadeBean.getAll();

        assertEquals(mapped, result);
    }
}
