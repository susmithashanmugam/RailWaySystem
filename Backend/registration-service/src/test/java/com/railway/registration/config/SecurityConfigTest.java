
package com.railway.registration.config;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

class SecurityConfigTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(TestSecurityConfiguration.class);

    @Test
    void securityConfig_ShouldCreatePasswordEncoderAndFilterChain() {

        contextRunner.run(context -> {

            assertNull(context.getStartupFailure());

            assertTrue(context.containsBean("passwordEncoder"));
            assertTrue(context.containsBean("securityFilterChain"));

            PasswordEncoder encoder =
                    context.getBean(PasswordEncoder.class);

            assertNotNull(encoder);

            String rawPassword = "Test@123";
            String encodedPassword = encoder.encode(rawPassword);

            assertNotEquals(rawPassword, encodedPassword);
            assertTrue(encoder.matches(rawPassword, encodedPassword));

            SecurityFilterChain filterChain =
                    context.getBean(SecurityFilterChain.class);

            assertNotNull(filterChain);
        });
    }

    @Configuration
    @EnableWebSecurity
    @Import(SecurityConfig.class)
    static class TestSecurityConfiguration {
    }
}
