package es.caib.invai.api.persistence.model;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "INV_APP_DATABASE")
@Getter
@Setter
public class AppDatabaseEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_DATABASE_SEQ")
    @SequenceGenerator(name = "INV_APP_DATABASE_SEQ", sequenceName = "INV_APP_DATABASE_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INFORMATION_SYSTEM_DB", nullable = false)
    private AppInformationSystemDbEntity informationSystemDb;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATABASE_ID", nullable = false)
    private DatabaseEntity database;
}