package es.caib.invai.back.persistence.repository.maintenance.security.webContext;

import es.caib.invai.back.service.model.maintenance.security.webContext.WebContext;
import es.caib.invai.back.persistence.model.maintenance.security.webContext.WebContextEntity;
import es.caib.invai.back.persistence.model.maintenance.security.webContext.WebContextAudEntity;
import es.caib.invai.back.service.mapper.maintenance.security.webContext.WebContextMapper;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link WebContextRepository}.
 *
 * @since 1.0.4
 */
@Repository
@Slf4j
public class WebContextRepositoryAdapter implements WebContextRepository {

    /** JPA repository used for CRUD operations on live web context entities. */
    @Autowired
    private WebContextJPARepository webContextJPARepository;

    /** JPA repository used to persist audit snapshots of web context changes. */
    @Autowired
    private WebContextAudJPARepository webContextAudJPARepository;

    /** Mapper used to convert between WebContext domain models and entities. */
    @Autowired
    private WebContextMapper webContextMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public WebContext findById(Long id) {
        return webContextJPARepository.findById(id)
                .map(webContextMapper::toModel)
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<WebContext> findAll(WebContextCriteria filter, Pageable pageable) {
        Specification<WebContextEntity> spec = WebContextSpecification.filterByCriteria(filter);
        return webContextJPARepository.findAll(spec, pageable).map(webContextMapper::toModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return webContextJPARepository.existsByNameAndDeletedAtIsNull(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id) {
        return webContextJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebContext create(WebContext webContext) {
        log.info("Repository: Persisting new web context entity into database");
        WebContextEntity entity = webContextMapper.toEntity(webContext);
        entity = webContextJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return webContextMapper.toModel(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebContext update(WebContext webContext, Long id) {
        log.info("Repository: Merging changes into existing web context record for ID: {}", id);
        WebContextEntity entity = webContextMapper.toEntity(webContext);
        entity.setId(id);
        entity = webContextJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return webContextMapper.toModel(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(WebContext webContext) {
        log.info("Repository: Soft deleting web context entity with ID: {}", webContext.getId());
        WebContextEntity entity = webContextMapper.toEntity(webContext);
        entity.setId(webContext.getId());
        entity = webContextJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Builds and persists an audit snapshot capturing the current state of the given entity
     * along with the operation that triggered it.
     *
     * @param entity the web context entity whose state is being audited
     * @param action the type of operation performed (e.g. {@code INSERT}, {@code UPDATE}, {@code DELETE})
     */
    private void saveAuditRecord(WebContextEntity entity, String action) {
        WebContextAudEntity aud = new WebContextAudEntity();

        aud.setWebContextId(entity.getId());
        aud.setName(entity.getName());
        aud.setNameEs(entity.getNameEs());

        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        webContextAudJPARepository.save(aud);
    }
}
