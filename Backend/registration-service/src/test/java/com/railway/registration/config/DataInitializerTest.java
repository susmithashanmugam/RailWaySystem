
package com.railway.registration.config;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;

import com.railway.registration.entity.Role;
import com.railway.registration.repository.RoleRepository;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private RoleRepository roleRepository;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        dataInitializer = new DataInitializer();
    }

    @Test
    void initializeRoles_ShouldCreateMissingRoles() throws Exception {

        when(roleRepository.findByName(anyString()))
                .thenReturn(Optional.empty());

        CommandLineRunner runner =
                dataInitializer.initializeRoles(roleRepository);

        runner.run();

        verify(roleRepository).findByName("ROLE_ADMIN");
        verify(roleRepository).findByName("ROLE_PASSENGER");
        verify(roleRepository).findByName("ROLE_STAFF");

        verify(roleRepository, times(3)).save(any(Role.class));
    }

    @Test
    void initializeRoles_ShouldNotCreateExistingRoles() throws Exception {

        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");

        Role passengerRole = new Role();
        passengerRole.setName("ROLE_PASSENGER");

        Role staffRole = new Role();
        staffRole.setName("ROLE_STAFF");

        when(roleRepository.findByName("ROLE_ADMIN"))
                .thenReturn(Optional.of(adminRole));

        when(roleRepository.findByName("ROLE_PASSENGER"))
                .thenReturn(Optional.of(passengerRole));

        when(roleRepository.findByName("ROLE_STAFF"))
                .thenReturn(Optional.of(staffRole));

        CommandLineRunner runner =
                dataInitializer.initializeRoles(roleRepository);

        runner.run();

        verify(roleRepository, never()).save(any(Role.class));
    }
}
