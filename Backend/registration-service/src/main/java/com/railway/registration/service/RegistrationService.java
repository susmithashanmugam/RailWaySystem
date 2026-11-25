
package com.railway.registration.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.railway.registration.dto.CreateStaffRequest;
import com.railway.registration.dto.RegistrationRequest;
import com.railway.registration.dto.RegistrationResponse;
import com.railway.registration.entity.Role;
import com.railway.registration.entity.User;
import com.railway.registration.exception.DuplicateResourceException;
import com.railway.registration.exception.ResourceNotFoundException;
import com.railway.registration.repository.RoleRepository;
import com.railway.registration.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegistrationResponse registerPassenger(
            RegistrationRequest request) {

        // Check whether email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                "Email is already registered"
            );
        }

        // Check whether phone number already exists
        if (userRepository.existsByPhoneNumber(
                request.getPhoneNumber())) {

            throw new DuplicateResourceException(
                "Phone number is already registered"
            );
        }

        // Find passenger role
        Role passengerRole = roleRepository
                .findByName("ROLE_PASSENGER")
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Passenger role not found"
                    )
                );

        // Create user entity
        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        // Encrypt password using BCrypt
        user.setPassword(
            passwordEncoder.encode(request.getPassword())
        );

        user.setRole(passengerRole);

        // Save user
        User savedUser = userRepository.save(user);

        // Return response
        return new RegistrationResponse(
            savedUser.getId(),
            savedUser.getFullName(),
            savedUser.getEmail(),
            savedUser.getRole().getName(),
            "Registration successful"
        );
    }

@Transactional
public RegistrationResponse createStaff(CreateStaffRequest request) {

    if (userRepository.existsByEmail(request.getEmail())) {
        throw new DuplicateResourceException("Email is already registered");
    }

    if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
        throw new DuplicateResourceException("Phone number is already registered");
    }

    Role staffRole = roleRepository.findByName("ROLE_STAFF")
            .orElseThrow(() ->
                    new ResourceNotFoundException("ROLE_STAFF not found"));

    User staff = new User();
    staff.setFullName(request.getFullName());
    staff.setEmail(request.getEmail());
    staff.setPhoneNumber(request.getPhoneNumber());
    staff.setPassword(passwordEncoder.encode(request.getPassword()));
    staff.setRole(staffRole);

    User savedStaff = userRepository.save(staff);

    return new RegistrationResponse(
            savedStaff.getId(),
            savedStaff.getFullName(),
            savedStaff.getEmail(),
            savedStaff.getPhoneNumber(),
            savedStaff.getRole().getName()
    );
}

}
