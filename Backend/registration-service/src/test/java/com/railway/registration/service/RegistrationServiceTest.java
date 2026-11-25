
package com.railway.registration.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.railway.registration.dto.RegistrationRequest;
import com.railway.registration.dto.CreateStaffRequest;
import com.railway.registration.dto.RegistrationResponse;

import com.railway.registration.entity.Role;
import com.railway.registration.entity.User;

import com.railway.registration.exception.DuplicateResourceException;
import com.railway.registration.exception.ResourceNotFoundException;

import com.railway.registration.repository.RoleRepository;
import com.railway.registration.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationService registrationService;

    private RegistrationRequest request;
    private CreateStaffRequest staffRequest;

    private Role passengerRole;
    private Role staffRole;

    @BeforeEach
    void setUp() {

        // Passenger request
        request = new RegistrationRequest();
        request.setFullName("Susmitha");
        request.setEmail("susmitha@gmail.com");
        request.setPhoneNumber("9876543210");
        request.setPassword("Railway@123");

        // Staff request
        staffRequest = new CreateStaffRequest();
        staffRequest.setFullName("Railway Staff");
        staffRequest.setEmail("staff@gmail.com");
        staffRequest.setPhoneNumber("9876543211");
        staffRequest.setPassword("Staff@12345");

        // Passenger role
        passengerRole = new Role();
        passengerRole.setId(2L);
        passengerRole.setName("ROLE_PASSENGER");

        // Staff role
        staffRole = new Role();
        staffRole.setId(3L);
        staffRole.setName("ROLE_STAFF");
    }

    // ==========================================
    // PASSENGER REGISTRATION TESTS
    // ==========================================

    // Test 1: Successful passenger registration

    @Test
    void registerPassenger_Success() {

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(userRepository.existsByPhoneNumber(request.getPhoneNumber()))
                .thenReturn(false);

        when(roleRepository.findByName("ROLE_PASSENGER"))
                .thenReturn(Optional.of(passengerRole));

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("hashedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });

        RegistrationResponse response =
                registrationService.registerPassenger(request);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("Susmitha", response.getFullName());
        assertEquals("susmitha@gmail.com", response.getEmail());
        assertEquals("ROLE_PASSENGER", response.getRole());
        assertEquals("Registration successful", response.getMessage());

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        assertEquals("hashedPassword", savedUser.getPassword());
        assertEquals(passengerRole, savedUser.getRole());

        verify(passwordEncoder).encode(request.getPassword());
    }

    // Test 2: Duplicate passenger email

    @Test
    void registerPassenger_DuplicateEmail() {

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> registrationService.registerPassenger(request)
        );

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(roleRepository);
    }

    // Test 3: Duplicate passenger phone number

    @Test
    void registerPassenger_DuplicatePhone() {

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(userRepository.existsByPhoneNumber(request.getPhoneNumber()))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> registrationService.registerPassenger(request)
        );

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(roleRepository);
    }

    // Test 4: Passenger role not found

    @Test
    void registerPassenger_RoleNotFound() {

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(userRepository.existsByPhoneNumber(request.getPhoneNumber()))
                .thenReturn(false);

        when(roleRepository.findByName("ROLE_PASSENGER"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> registrationService.registerPassenger(request)
        );

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    // ==========================================
    // STAFF REGISTRATION TESTS
    // ==========================================

    // Test 5: Successful staff creation
    // Assertions reflect the existing service implementation.

    @Test
    void createStaff_Success() {

        when(userRepository.existsByEmail(staffRequest.getEmail()))
                .thenReturn(false);

        when(userRepository.existsByPhoneNumber(staffRequest.getPhoneNumber()))
                .thenReturn(false);

        when(roleRepository.findByName("ROLE_STAFF"))
                .thenReturn(Optional.of(staffRole));

        when(passwordEncoder.encode(staffRequest.getPassword()))
                .thenReturn("hashedStaffPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User staff = invocation.getArgument(0);
                    staff.setId(5L);
                    return staff;
                });

        RegistrationResponse response =
                registrationService.createStaff(staffRequest);

        assertNotNull(response);
        assertEquals(5L, response.getUserId());
        assertEquals("Railway Staff", response.getFullName());
        assertEquals("staff@gmail.com", response.getEmail());

        // Existing service maps phone number to role.
        assertEquals("9876543211", response.getRole());

        // Existing service maps staff role to message.
        assertEquals("ROLE_STAFF", response.getMessage());

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User savedStaff = captor.getValue();

        assertEquals("hashedStaffPassword", savedStaff.getPassword());
        assertEquals(staffRole, savedStaff.getRole());

        verify(passwordEncoder).encode(staffRequest.getPassword());
    }

    // Test 6: Duplicate staff email

    @Test
    void createStaff_DuplicateEmail() {

        when(userRepository.existsByEmail(staffRequest.getEmail()))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> registrationService.createStaff(staffRequest)
        );

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(roleRepository);
    }

    // Test 7: Duplicate staff phone number

    @Test
    void createStaff_DuplicatePhone() {

        when(userRepository.existsByEmail(staffRequest.getEmail()))
                .thenReturn(false);

        when(userRepository.existsByPhoneNumber(staffRequest.getPhoneNumber()))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> registrationService.createStaff(staffRequest)
        );

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(roleRepository);
    }

    // Test 8: Staff role not found

    @Test
    void createStaff_RoleNotFound() {

        when(userRepository.existsByEmail(staffRequest.getEmail()))
                .thenReturn(false);

        when(userRepository.existsByPhoneNumber(staffRequest.getPhoneNumber()))
                .thenReturn(false);

        when(roleRepository.findByName("ROLE_STAFF"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> registrationService.createStaff(staffRequest)
        );

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }
}
