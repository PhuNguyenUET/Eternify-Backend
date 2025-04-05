package com.eternify.backend.authentication.controller;


import com.eternify.backend.authentication.dto.AuthenticationRequest;
import com.eternify.backend.authentication.dto.AuthenticationResponse;
import com.eternify.backend.authentication.service.user_impl.AuthenticationServiceImpl;
import com.eternify.backend.common.api.ApiResponse;
import com.eternify.backend.user.dto.UserRegisterDTO;
import com.eternify.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "user_authentication")
@RequestMapping("/api/user/v1")
public class UserAuthenticationController {

    @Value("${api.token}")
    private String apiToken;

    private final AuthenticationServiceImpl userAuthenticationService;
    private final UserService userService;

    @PostMapping("/authenticate")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = AuthenticationResponse.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> authenticate (@RequestHeader("X-auth-token") String token,
                                                     @RequestBody AuthenticationRequest request, HttpServletRequest httpRequest) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Authentication successful", userAuthenticationService.authenticate(request, httpRequest)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register (@RequestHeader("X-auth-token") String token,
                                                 @RequestBody UserRegisterDTO request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            userService.register(request);
            return ResponseEntity.ok(ApiResponse.success("Registration success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/artist_register")
    public ResponseEntity<ApiResponse> artistRegister (@RequestHeader("X-auth-token") String token,
                                                 @RequestBody UserRegisterDTO request) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            userService.artistRegister(request);
            return ResponseEntity.ok(ApiResponse.success("Registration success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/refresh_jwt")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = String.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> refreshToken (@RequestHeader("X-auth-token") String token,
                                                   @RequestBody String refresh) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            String jwtToken = userAuthenticationService.refreshToken(refresh);
            if (jwtToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(HttpServletResponse.SC_UNAUTHORIZED, "Invalid refresh token"));
            }
            return ResponseEntity.ok(ApiResponse.success(jwtToken));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}

