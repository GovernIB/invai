package es.caib.invai.back.persistence.model.type;

import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.BlobJdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.spi.TypeConfiguration;

import java.io.Serial;

/**
 * JDBC type for {@link java.sql.Blob} attributes that binds the content as a stream instead of as a
 * {@code Blob}. The Oracle 12.2 driver deployed in the application server casts any {@code Blob} given
 * to {@code setBlob} to its own {@code oracle.sql.BLOB}, which fails for the proxies Hibernate builds.
 * Reading is unchanged, so the attribute is still loaded as a locator and the bytes are only read when asked for.
 *
 * @since 1.0.6
 */
public class StreamBlobJdbcType implements JdbcType {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Hibernate's own BLOB type that binds through {@code setBinaryStream}. */
    private static final JdbcType DELEGATE = BlobJdbcType.STREAM_BINDING;

    @Override
    public int getJdbcTypeCode() {
        return DELEGATE.getJdbcTypeCode();
    }

    @Override
    public String getFriendlyName() {
        return DELEGATE.getFriendlyName();
    }

    @Override
    public <T> JavaType<T> getJdbcRecommendedJavaTypeMapping(Integer length, Integer scale, TypeConfiguration typeConfiguration) {
        return DELEGATE.getJdbcRecommendedJavaTypeMapping(length, scale, typeConfiguration);
    }

    @Override
    public Class<?> getPreferredJavaTypeClass(WrapperOptions options) {
        return DELEGATE.getPreferredJavaTypeClass(options);
    }

    @Override
    public <X> ValueBinder<X> getBinder(JavaType<X> javaType) {
        return DELEGATE.getBinder(javaType);
    }

    @Override
    public <X> ValueExtractor<X> getExtractor(JavaType<X> javaType) {
        return DELEGATE.getExtractor(javaType);
    }
}
