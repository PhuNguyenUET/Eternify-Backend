package com.eternify.backend.authentication.service;

import com.eternify.backend.authentication.dto.AuthenticationRequest;
import com.eternify.backend.authentication.dto.AuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletRequest httpServletRequest);

    String refreshToken(String refreshToken);
}
