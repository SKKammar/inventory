package com.example.inventory.controller;

import com.example.inventory.dto.AuthRequestDTO;
import com.example.inventory.dto.AuthResponseDTO;
import com.example.inventory.dto.SignupRequestDTO;
import com.example.inventory.dto.ApiResponseDTO;
import com.example.inventory.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    /**
     * Login endpoint
     */
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and get access token")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> login(@Valid @RequestBody AuthRequestDTO loginRequest) {
        logger.info("Login request for user: {}", loginRequest.getUsername());

        AuthResponseDTO authResponse = authService.authenticateUser(loginRequest);

        ApiResponseDTO<AuthResponseDTO> response = ApiResponseDTO.<AuthResponseDTO>builder()
                .success(true)
                .message("Login successful")
                .data(authResponse)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Signup endpoint
     */
    @PostMapping("/signup")
    @Operation(summary = "Register new user", description = "Create a new user account")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> signup(@Valid @RequestBody SignupRequestDTO signupRequest) {
        logger.info("Signup request for user: {}", signupRequest.getUsername());

        AuthResponseDTO authResponse = authService.registerUser(signupRequest);

        ApiResponseDTO<AuthResponseDTO> response = ApiResponseDTO.<AuthResponseDTO>builder()
                .success(true)
                .message("User registered successfully")
                .data(authResponse)
                .statusCode(201)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Refresh token endpoint
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> refreshToken(@RequestParam String refreshToken) {
        logger.info("Refresh token request");

        AuthResponseDTO authResponse = authService.refreshAccessToken(refreshToken);

        ApiResponseDTO<AuthResponseDTO> response = ApiResponseDTO.<AuthResponseDTO>builder()
                .success(true)
                .message("Access token refreshed successfully")
                .data(authResponse)
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidate refresh token")
    public ResponseEntity<ApiResponseDTO<?>> logout(@RequestParam String refreshToken) {
        logger.info("Logout request");

        authService.logout(refreshToken);

        ApiResponseDTO<?> response = ApiResponseDTO.builder()
                .success(true)
                .message("Logged out successfully")
                .statusCode(200)
                .timestamp(System.currentTimeMillis())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}