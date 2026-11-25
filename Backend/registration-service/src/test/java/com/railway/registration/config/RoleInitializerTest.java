
package com.railway.registration.config;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.railway.registration.entity.Role;
import com.railway.registration.entity.User;
import com.railway.registration.repository.RoleRepository;
import com.railway.registration.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RoleInitializerTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationArguments applicationArguments;

    private RoleInitializer roleInitializer;

    private Role adminRole;

    @BeforeEach
    void setUp() {

        roleInitializer = new RoleInitializer(
                roleRepository,
                userRepository,
                passwordEncoder
        );

        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ROLE_ADMIN");
    }

    private void enableAdminBootstrap() {

        ReflectionTestUtils.setField(
                roleInitializer, "adminBootstrapEnabled", true);

        ReflectionTestUtils.setField(
                roleInitializer, "adminFullName", "Test Admin");

        ReflectionTestUtils.setField(
                roleInitializer, "adminEmail", "ADMIN@gmail.com");

        ReflectionTestUtils.setField(
                roleInitializer, "adminPhone", "9876543210");

        ReflectionTestUtils.setField(
                roleInitializer, "adminPassword", "Admin@123");
    }

    private void mockExistingRoles() {

        when(roleRepository.findByName(anyString()))
                .thenAnswer(invocation -> {
                    String name = invocation.getArgument(0);

                    if ("ROLE_ADMIN".equals(name)) {
                        return Optional.of(adminRole);
                    }

                    Role role = new Role();
                    role.setName(name);

                    return Optional.of(role);
                });
    }

    // Test 1: Create missing roles

    @Test
    void run_ShouldCreateMissingRoles() {

        when(roleRepository.findByName(anyString()))
                .thenReturn(Optional.empty());

        roleInitializer.run(applicationArguments);

        verify(roleRepository, times(3)).save(any(Role.class));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    // Test 2: Existing roles should not be created again

    @Test
    void run_ShouldNotCreateExistingRoles() {

        mockExistingRoles();

        roleInitializer.run(applicationArguments);

        verify(roleRepository, never()).save(any(Role.class));
        verifyNoInteractions(userRepository);
    }

    // Test 3: Admin bootstrap disabled

    @Test
    void run_AdminBootstrapDisabled_ShouldNotCreateAdmin() {

        mockExistingRoles();

        ReflectionTestUtils.setField(
                roleInitializer, "adminBootstrapEnabled", false);

        roleInitializer.run(applicationArguments);

        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    // Test 4: Successful admin creation

    @Test
    void run_ShouldCreateInitialAdmin() {

        enableAdminBootstrap();
        mockExistingRoles();

        when(userRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.empty());

        when(userRepository.existsByPhoneNumber("9876543210"))
                .thenReturn(false);

        when(passwordEncoder.encode("Admin@123"))
                .thenReturn("encodedPassword");

        roleInitializer.run(applicationArguments);

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User savedAdmin = captor.getValue();

        assertEquals("Test Admin", savedAdmin.getFullName());
        assertEquals("admin@gmail.com", savedAdmin.getEmail());
        assertEquals("9876543210", savedAdmin.getPhoneNumber());
        assertEquals("encodedPassword", savedAdmin.getPassword());
        assertEquals(adminRole, savedAdmin.getRole());

        verify(passwordEncoder).encode("Admin@123");
    }

    // Test 5: Missing admin credentials

    @Test
    void run_MissingAdminCredentials_ShouldThrowException() {

        mockExistingRoles();

        ReflectionTestUtils.setField(
                roleInitializer, "adminBootstrapEnabled", true);

        ReflectionTestUtils.setField(
                roleInitializer, "adminFullName", "");

        assertThrows(
                IllegalStateException.class,
                () -> roleInitializer.run(applicationArguments)
        );

        verifyNoInteractions(userRepository);
    }

    // Test 6: Existing admin account

    @Test
    void run_AdminAlreadyExists_ShouldNotCreateAnotherAdmin() {

        enableAdminBootstrap();
        mockExistingRoles();

        User existingAdmin = new User();
        existingAdmin.setRole(adminRole);

        when(userRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.of(existingAdmin));

        roleInitializer.run(applicationArguments);

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    // Test 7: Email belongs to non-admin account

    @Test
    void run_EmailBelongsToNonAdmin_ShouldThrowException() {

        enableAdminBootstrap();
        mockExistingRoles();

        Role passengerRole = new Role();
        passengerRole.setName("ROLE_PASSENGER");

        User existingUser = new User();
        existingUser.setRole(passengerRole);

        when(userRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.of(existingUser));

        assertThrows(
                IllegalStateException.class,
                () -> roleInitializer.run(applicationArguments)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    // Test 8: Admin phone number already exists

    @Test
    void run_DuplicateAdminPhone_ShouldThrowException() {

        enableAdminBootstrap();
        mockExistingRoles();

        when(userRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.empty());

        when(userRepository.existsByPhoneNumber("9876543210"))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> roleInitializer.run(applicationArguments)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    // Test 9: Admin role not found

    @Test
    void run_AdminRoleNotFound_ShouldThrowException() {

        enableAdminBootstrap();

        when(roleRepository.findByName(anyString()))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.empty());

        when(userRepository.existsByPhoneNumber("9876543210"))
                .thenReturn(false);

        assertThrows(
                IllegalStateException.class,
                () -> roleInitializer.run(applicationArguments)
        );

        verify(userRepository, never()).save(any(User.class));
    }
}
