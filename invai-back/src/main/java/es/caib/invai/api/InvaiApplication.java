package es.caib.invai.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;

/**
 * Main application entry point and configuration bootstrap configuration for the INVAI Core API engine.
 * <p>
 * Extends {@link SpringBootServletInitializer} to enable traditional web archive (WAR) deployment topology
 * inside external servlet containers (e.g., JBoss EAP, WildFly, Apache Tomcat), whilst preserving
 * standard embedded standalone execution pathways.
 * </p>
 * <p>
 * Note: Auto-configurations for default reactive relational persistence layers ({@link DataSourceAutoConfiguration}
 * and {@link HibernateJpaAutoConfiguration}) are explicitly excluded here. Connection pools and transaction boundaries
 * are bound via custom configuration modules targeting isolated infrastructure parameters.
 * </p>
 *
 * @since 1.0.0
 */
@SpringBootApplication(exclude = {
		DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class
})
@PropertySource("file:${es.caib.invai.system.properties}")
public class InvaiApplication extends SpringBootServletInitializer {

	/** Internal structural logging engine reference used to emit infrastructure operational metrics. */
	private static final Logger log = LoggerFactory.getLogger(InvaiApplication.class);

	/**
	 * Standard Java application execution entry loop providing immediate standalone microservice
	 * initialization strategies via an embedded runtime framework engine.
	 *
	 * @param args runtime string parameter arguments map array passed via system execution threads
	 */
	public static void main(String[] args) {
		SpringApplication.run(InvaiApplication.class, args);
	}

	/**
	 * Orchestrates structural source linkage overrides when loading the execution context within
	 * external traditional servlet containers.
	 *
	 * @param builder localized configuration engine builder instance
	 * @return the modified configuration builder targeting this application bootstrap node
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		log.info("configure() executed - initializing external servlet container topology hook");
		return builder.sources(InvaiApplication.class);
	}
}