package es.caib.invai.api.interna;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for the servlet-container bootstrap hook of {@link InvaiApiInternaApplication}.
 */
class InvaiApiInternaApplicationTest {

    @Test
    void configure_registersTheApplicationAsSource() {
        SpringApplicationBuilder builder = new InvaiApiInternaApplication().configure(new SpringApplicationBuilder());

        assertTrue(builder.build().getAllSources().stream()
                .anyMatch(source -> source.toString().contains(InvaiApiInternaApplication.class.getName())));
    }
}
