package com.eternify.backend.authentication.service.user_impl;

import com.eternify.backend.authentication.dto.AuthenticationRequest;
import com.eternify.backend.authentication.dto.AuthenticationResponse;
import com.eternify.backend.authentication.filter.JwtConstant;
import com.eternify.backend.authentication.service.AuthenticationService;
import com.eternify.backend.common.exception.LockedUserException;
import com.eternify.backend.common.exception.UserNotFoundException;
import com.eternify.backend.common.exception.WrongPasswordException;
import com.eternify.backend.user.model.CustomUserDetails;
import com.eternify.backend.user.model.User;
import com.eternify.backend.user.service.UserService;
import com.eternify.backend.util.JwtUtils;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    @Autowired
    private UserService userService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AuthenticationManager authenticationManager;

    private final LockedUserException lockedUserException = new LockedUserException("Too many wrong attempts. Account has already been locked.");
    private final WrongPasswordException wrongPasswordException = new WrongPasswordException("Wrong password");

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletRequest httpServletRequest) {
        final int FAILURE_LIMIT = 15;
        User user = userService.getUserByEmail(request.getEmail());
        if (user == null) {
            throw new UserNotFoundException(request.getEmail());
        }
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            user.setFailedAttempt(user.getFailedAttempt() + 1);
            if (user.getFailedAttempt() > FAILURE_LIMIT) {
                user.setFailedAttempt(0);
                user.setActive(false);
            }
            mongoTemplate.save(user);
            if (!user.isActive()) {
                throw lockedUserException;
            }
            throw wrongPasswordException;
        }
        if (!user.isActive()) {
            throw lockedUserException;
        }
        String jwt = JwtUtils.generateJwtToken(user.getEmail());
        String refresh = JwtUtils.generateRefreshToken(user.getEmail());
        return new AuthenticationResponse(jwt, refresh);
    }

    @Override
    public String refreshToken(String refreshToken) {
        if (refreshToken != null && refreshToken.startsWith(JwtConstant.JWT_TOKEN_PREFIX)) {
            String token = refreshToken.substring(JwtConstant.JWT_TOKEN_PREFIX.length());
            String username;

            try {
                username = JwtUtils.extractRefreshUsername(token);
            } catch (JwtException e) {
                return null;
            }

            CustomUserDetails customUserDetails;

            try {
                customUserDetails = (CustomUserDetails) userService.loadUserByUsername(username);
            } catch (UsernameNotFoundException e) {
                return null;
            }

            if (!customUserDetails.getUser().isActive()) {
                return null;
            }

            if (JwtUtils.validateRefreshToken(token, customUserDetails)) {
                return JwtUtils.generateJwtToken(username);
            }
        }

        return null;
    }
}
