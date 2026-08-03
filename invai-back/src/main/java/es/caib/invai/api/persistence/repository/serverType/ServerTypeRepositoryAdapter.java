package es.caib.invai.api.persistence.repository.serverType;

import es.caib.invai.api.persistence.model.catalog.LkupServerTypeEntity;
import es.caib.invai.api.service.mapper.ServerTypeMapper;
import es.caib.invai.api.service.model.ServerType;
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

    @Autowired
    private ServerTypeJPARepository serverTypeJPARepository;

    @Autowired
    private ServerTypeMapper serverTypeMapper;

    @Override
    public List<ServerType> findAll() {
        log.info("Repository: Fetching every server type lookup entry");
        List<LkupServerTypeEntity> entities = serverTypeJPARepository.findAll(Sort.by("name"));
        return serverTypeMapper.toModelList(entities);
    }
}
