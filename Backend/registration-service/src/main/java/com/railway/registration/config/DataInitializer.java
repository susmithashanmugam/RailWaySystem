
package com.railway.registration.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.railway.registration.entity.Role;
import com.railway.registration.repository.RoleRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeRoles(
            RoleRepository roleRepository) {

        return args -> {

            createRole(roleRepository, "ROLE_ADMIN");

            createRole(roleRepository, "ROLE_PASSENGER");

            createRole(roleRepository, "ROLE_STAFF");
        };
    }

    private void createRole(
            RoleRepository roleRepository,
            String roleName) {

        if (roleRepository.findByName(roleName).isEmpty()) {

            Role role = new Role();
            role.setName(roleName);

            roleRepository.save(role);
        }
    }
}
