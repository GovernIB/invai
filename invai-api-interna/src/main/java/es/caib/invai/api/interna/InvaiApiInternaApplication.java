package es.caib.invai.api.interna;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application entry point and configuration bootstrap configuration for the INVAI
 * Integrations API (Soffid, DIR3CAIB), called server-to-server by {@code invai-back}.
 * <p>
 * Extends {@link SpringBootServletInitializer} to enable traditional web archive (WAR) deployment
 * topology inside external servlet containers (e.g., JBoss EAP, WildFly, Apache Tomcat), whilst
 * preserving standard embedded standalone execution pathways.
 * </p>
 *
 * @since 1.0.4
 */
@SpringBootApplication
@EnableScheduling
@PropertySource("file:${es.caib.invai.system.properties}")
@OpenAPIDefinition(
		info = @Info(
				title = "INVAI – API INTERNA (Integracions)",
				description = "Servei d'integracions externes (Soffid, DIR3CAIB) de l'inventari d'aplicacions informàtiques de les Illes Balears (INVAI).",
				version = "1.0.4"
		),
		servers = @Server(url = "/invaiapi/interna")
)
public class InvaiApiInternaApplication extends SpringBootServletInitializer {

	/** Internal structural logging engine reference used to emit infrastructure operational metrics. */
	private static final Logger log = LoggerFactory.getLogger(InvaiApiInternaApplication.class);

	/**
	 * Standard Java application execution entry loop providing immediate standalone microservice
	 * initialization strategies via an embedded runtime framework engine.
	 *
	 * @param args runtime string parameter arguments map array passed via system execution threads
	 */
	public static void main(String[] args) {
		SpringApplication.run(InvaiApiInternaApplication.class, args);
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
		return builder.sources(InvaiApiInternaApplication.class);
	}
}
