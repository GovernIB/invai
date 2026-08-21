package es.caib.invai.back.persistence.repository.maintenance.systems.serverType;

import es.caib.invai.back.persistence.model.maintenance.systems.serverType.LkupServerTypeEntity;
import es.caib.invai.back.service.mapper.maintenance.systems.serverType.ServerTypeMapper;
import es.caib.invai.back.service.model.maintenance.systems.serverType.ServerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link ServerTypeRepository}.
 *
 * @since 1.0.2
 */
@Repository
@Slf4j
public class ServerTypeRepositoryAdapter implements ServerTypeRepository {

    /** JPA repository providing read-only query access to the server type lookup entity. */
    @Autowired
    private ServerTypeJPARepository serverTypeJPARepository;

    /** Mapper converting between the server type entity and its business domain model. */
    @Autowired
    private ServerTypeMapper serverTypeMapper;

    @Override
    public List<ServerType> findAll() {
        log.info("Repository: Fetching every server type lookup entry");
        List<LkupServerTypeEntity> entities = serverTypeJPARepository.findAll(Sort.by("name"));
        return serverTypeMapper.toModelList(entities);
    }
}
