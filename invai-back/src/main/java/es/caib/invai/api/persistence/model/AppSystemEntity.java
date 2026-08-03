package es.caib.invai.api.persistence.model;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "INV_APP_SYSTEM")
@Getter
@Setter
public class AppSystemEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_SYSTEM_SEQ")
    @SequenceGenerator(name = "INV_APP_SYSTEM_SEQ", sequenceName = "INV_APP_SYSTEM_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INFORMATION_SYSTEM_DB", nullable = false)
    private AppInformationSystemDbEntity informationSystemDb;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SYSTEM_ID", nullable = false)
    private SystemEntity system;
}