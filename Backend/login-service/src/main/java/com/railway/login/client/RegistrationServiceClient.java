
package com.railway.login.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.railway.login.dto.InternalUserAuthResponse;

@FeignClient(name = "registration-service")
public interface RegistrationServiceClient {

    @GetMapping("/internal/auth/users/{email}")
    InternalUserAuthResponse getUserForAuthentication(
            @PathVariable("email") String email,
            @RequestHeader("X-INTERNAL-API-KEY") String apiKey
    );
}
