package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.dir3;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.dir3.Dir3ValidationAudEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.dir3.Dir3ValidationEntity;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.dir3.Dir3ValidationMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.dir3.Dir3Validation;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure outbound adapter implementing {@link Dir3ValidationRepository}, translating
 * between {@link Dir3Validation} domain models and {@link Dir3ValidationEntity} JPA entities, and
 * writing a historical {@link Dir3ValidationAudEntity} row on every create/update.
 *
 * @since 1.0.5
 */
@Repository
@Slf4j
public class Dir3ValidationRepositoryAdapter implements Dir3ValidationRepository {

    /** JPA repository providing CRUD access to the DIR3 validation entity. */
    @Autowired
    private Dir3ValidationJPARepository dir3ValidationJPARepository;
    /** JPA repository used to persist the historical audit trail for the DIR3 validation entity. */
    @Autowired
    private Dir3ValidationAudJPARepository dir3ValidationAudJPARepository;
    /** Mapper converting between the DIR3 validation entity and its business domain model. */
    @Autowired
    private Dir3ValidationMapper dir3ValidationMapper;

    /** {@inheritDoc} */
    @Override
    public Dir3Validation create(Dir3Validation dir3Validation) {
        Dir3ValidationEntity entity = dir3ValidationMapper.toEntity(dir3Validation);
        entity = dir3ValidationJPARepository.save(entity);
        saveAuditRecord(entity, "INSERT");
        return dir3ValidationMapper.toModel(entity);
    }

    /** {@inheritDoc} */
    @Override
    public Dir3Validation update(Dir3Validation dir3Validation, Long id) {
        Dir3ValidationEntity entity = dir3ValidationMapper.toEntity(dir3Validation);
        entity.setId(id);
        entity = dir3ValidationJPARepository.save(entity);
        saveAuditRecord(entity, "UPDATE");
        return dir3ValidationMapper.toModel(entity);
    }

    /** {@inheritDoc} */
    @Override
    public Dir3Validation findById(Long id) {
        return dir3ValidationJPARepository.findById(id).map(dir3ValidationMapper::toModel).orElse(null);
    }

    /**
     * Builds and persists a historical audit trail row mirroring the current state of the given
     * DIR3 validation entity, tagged with the type of mutation that triggered it.
     *
     * @param entity the DIR3 validation entity state to snapshot
     * @param action the type of mutation being recorded (e.g. {@code "INSERT"}, {@code "UPDATE"})
     */
    private void saveAuditRecord(Dir3ValidationEntity entity, String action) {
        Dir3ValidationAudEntity aud = new Dir3ValidationAudEntity();
        aud.setDir3ValidationId(entity.getId());
        aud.setDir3StatusId(entity.getDir3Status().getId());
        aud.setManualValidatedAt(entity.getManualValidatedAt());
        aud.setManualValidatedBy(entity.getManualValidatedBy());
        aud.setReason(entity.getReason());
        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());
        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());
        dir3ValidationAudJPARepository.save(aud);
    }
}
