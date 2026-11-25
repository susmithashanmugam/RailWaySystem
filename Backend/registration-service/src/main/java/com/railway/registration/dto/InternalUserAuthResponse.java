
package com.railway.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InternalUserAuthResponse {

    private Long userId;

    private String email;

    private String passwordHash;

    private String role;
}
