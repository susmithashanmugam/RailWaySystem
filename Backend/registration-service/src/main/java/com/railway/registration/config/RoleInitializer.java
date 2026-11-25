
package com.railway.registration.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.railway.registration.entity.Role;
import com.railway.registration.entity.User;
import com.railway.registration.repository.RoleRepository;
import com.railway.registration.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class RoleInitializer implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(RoleInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.enabled:false}")
    private boolean adminBootstrapEnabled;

    @Value("${app.bootstrap.admin.full-name:}")
    private String adminFullName;

    @Value("${app.bootstrap.admin.email:}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.phone:}")
    private String adminPhone;

    @Value("${app.bootstrap.admin.password:}")
    private String adminPassword;

    public RoleInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        List<String> roles = List.of(
                "ROLE_ADMIN",
                "ROLE_PASSENGER",
                "ROLE_STAFF"
        );

        for (String roleName : roles) {

            if (roleRepository.findByName(roleName).isEmpty()) {

                Role role = new Role();
                role.setName(roleName);

                roleRepository.save(role);

                log.info("Created role: {}", roleName);
            }
        }

        log.info("Role initialization completed.");

        if (adminBootstrapEnabled) {
            createInitialAdmin();
        }
    }

    private void createInitialAdmin() {

        if (isBlank(adminFullName)
                || isBlank(adminEmail)
                || isBlank(adminPhone)
                || isBlank(adminPassword)) {

            throw new IllegalStateException(
                    "Admin bootstrap is enabled, but one or more "
                    + "Admin credentials are missing."
            );
        }

        String email = adminEmail.trim().toLowerCase();
        String phone = adminPhone.trim();

        var existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {

            if ("ROLE_ADMIN".equals(
                    existingUser.get().getRole().getName())) {

                log.info("Initial Admin account already exists.");
                return;
            }

            throw new IllegalStateException(
                    "The configured Admin email already belongs "
                    + "to a non-admin account."
            );
        }

        if (userRepository.existsByPhoneNumber(phone)) {

            throw new IllegalStateException(
                    "The configured Admin phone number is already registered."
            );
        }

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException(
                        "ROLE_ADMIN was not initialized."
                ));

        User admin = new User();

        admin.setFullName(adminFullName.trim());
        admin.setEmail(email);
        admin.setPhoneNumber(phone);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(adminRole);

        userRepository.save(admin);

        log.info("Initial Admin account created successfully.");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
