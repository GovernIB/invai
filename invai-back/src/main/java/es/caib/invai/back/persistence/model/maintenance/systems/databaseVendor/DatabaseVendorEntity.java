package es.caib.invai.back.persistence.model.maintenance.systems.databaseVendor;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import es.caib.invai.back.persistence.model.BaseEntity;

import java.io.Serial;

/**
 * JPA entity mapping database vendor/type catalog records from the {@code INV_DATABASE_VENDOR} table.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_DATABASE_VENDOR")
@Getter
@Setter
public class DatabaseVendorEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Primary key mapped to sequence generator {@code INV_DATABASE_VENDOR_SEQ}. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_DATABASE_VENDOR_SEQ")
    @SequenceGenerator(name = "INV_DATABASE_VENDOR_SEQ", sequenceName = "INV_DATABASE_VENDOR_SEQ", allocationSize = 1)
    @Column(name = "ID", unique = true, nullable = false)
    private Long id;

    /** Name of the database vendor/type (e.g., Oracle, PostgreSQL). */
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    /** Default connection port for the database vendor. */
    @Column(name = "DEFAULT_PORT")
    private Integer defaultPort;
}
