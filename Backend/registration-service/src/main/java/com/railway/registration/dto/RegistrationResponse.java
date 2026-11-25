
package com.railway.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegistrationResponse {

    private Long userId;

    private String fullName;

    private String email;

    private String role;

    private String message;
}
